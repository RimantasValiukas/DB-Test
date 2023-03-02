package lt.code.academy;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import static com.mongodb.client.model.Filters.*;

import static com.mongodb.client.model.Updates.*;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.Scanner;

public class BankAccount {
    Scanner sc;
    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection<User> usersCollection;
    private User user;

    public BankAccount() {
        sc = new Scanner(System.in);
        CodecRegistry registry = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), registry);
        MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(codecRegistry).build();
        client = MongoClients.create(settings);
        database = client.getDatabase("bank");
        usersCollection = database.getCollection("users", User.class);
    }

    private void putMoney() {
        System.out.println("Kiek pinigu norite inesti:");
        double sum = Double.parseDouble(sc.nextLine());
        user.setBalance(user.getBalance() + sum);
        double userBalance = user.getBalance();
        usersCollection.updateOne(eq("_id", user.getId()), set("balance", userBalance));
        System.out.printf("Saskaita papildyta, saskaitos likutis: %s eur%n", userBalance);
    }

    private void checkBalance() {
        System.out.printf("Jusu pinigu likutis yra %s eur%n", user.getBalance());
    }

    private void transferMoney() {
        System.out.println("Iveskite vartotojo varda, kam norite pervesti pinigus:");
        String receiver = sc.nextLine();
        User receiverUser = usersCollection.find(eq("userName", receiver)).first();
        if (receiverUser == null) {
            System.out.println("Tokio vartotojo nera");
            return;
        }
        System.out.println("Iveskite pervedama suma:");
        double sum = Double.parseDouble(sc.nextLine());
        double userBalance = user.getBalance();
        if (userBalance >= sum) {
            user.setBalance(userBalance - sum);
            receiverUser.setBalance(receiverUser.getBalance() + sum);
            usersCollection.updateOne(eq("_id", user.getId()), set("balance", user.getBalance()));
            usersCollection.updateOne(eq("_id", receiverUser.getId()), set("balance", receiverUser.getBalance()));
            System.out.printf("Jus pervedete %s eur vartotojui %s %s%n Jusu pinigu likutis %s%n",
                    sum, receiverUser.getName(), receiverUser.getLastName(), user.getBalance());
            return;
        }
        System.out.println("Nepakankamas pinigu likutis");
    }

    private void userAction(String action) {
        switch (action) {
            case "1" -> transferMoney();
            case "2" -> checkBalance();
            case "3" -> putMoney();
            case "4" -> System.out.println("Programa baige darba");
            default -> System.out.println("Tokios funkcijos nera");
        }
    }

    private void menu() {
        String action;
        do {
            System.out.println("""
                1 - Pervesti pinigus
                2 - Pasitikrinti saskaitos likuti
                3 - Ideti pinigu
                4 - Iseiti
                """);
            action = sc.nextLine();
            userAction(action);
        } while (!action.equals("4"));
    }

    public void welcomeMenu() {
        System.out.println("""
                1 - registracija
                2 - prisijungti""");
        String action = sc.nextLine();
        switch (action) {
            case "1" -> registration();
            case "2" -> login();
            default -> System.out.println("Tokios funkcijos nera");
        }

    }

    public void registration() {
        System.out.println("Iveskite prisijungimo varda");
        String userName = sc.nextLine();
        System.out.println("Iveskite savo varda");
        String name = sc.nextLine();
        System.out.println("Iveskite savo pavarde");
        String lastName = sc.nextLine();
        System.out.println("Iveskite pradine suma pinigu");
        double sum = Double.parseDouble(sc.nextLine());
        FindIterable<User> users = usersCollection.find();
        for (User u: users) {
            if (u.getUserName().equals(userName)) {
                System.out.println("Toks vartotojo vardas jau uzimtas");
                welcomeMenu();
            }
        }
        User newUser = new User(null, userName, name, lastName, sum);
        usersCollection.insertOne(newUser);
        welcomeMenu();
    }

    public void login() {
        System.out.println("Iveskite prisijungimo varda:");
        String userName = sc.nextLine();
        FindIterable<User> users = usersCollection.find();
        for (User u: users) {
            if (u.getUserName().equals(userName)) {
                user = u;
                System.out.println("Sekmigai prisijungete");
                menu();
                return;
            }
        }
        System.out.println("Tokio vartotojo nera");
        welcomeMenu();
    }

}

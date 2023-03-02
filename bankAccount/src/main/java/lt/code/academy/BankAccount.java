package lt.code.academy;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import static com.mongodb.client.model.Filters.*;

import static com.mongodb.client.model.Updates.*;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.InputMismatchException;
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
        double sum = getCorrectNumber("Kiek pinigu norite įnešti:");
        user.setBalance(user.getBalance() + sum);
        double userBalance = user.getBalance();
        usersCollection.updateOne(eq("_id", user.getId()), set("balance", userBalance));
        System.out.printf("Jūsų sąskaita papildyta, saskaitos likutis: %s eur%n", userBalance);
    }

    private void checkBalance() {
        System.out.printf("Jūsų pinigų likutis yra %s eur%n", user.getBalance());
    }

    private void transferMoney() {
        System.out.println("Iveskite vartotojo vardą, kam norite pervesti pinigus:");
        String receiver = sc.nextLine();
        User receiverUser = usersCollection.find(eq("userName", receiver)).first();
        if (receiverUser == null) {
            System.out.println("Tokio vartotojo nėra");
            return;
        }
        double sum = getCorrectNumber("Įveskite pervedamą sumą");
        double userBalance = user.getBalance();
        if (userBalance >= sum) {
            user.setBalance(userBalance - sum);
            receiverUser.setBalance(receiverUser.getBalance() + sum);
            usersCollection.updateOne(eq("_id", user.getId()), set("balance", user.getBalance()));
            usersCollection.updateOne(eq("_id", receiverUser.getId()), set("balance", receiverUser.getBalance()));
            System.out.printf("Jūs pervedėte %s eur vartotojui %s %s%nJūsų pinigų likutis %s%n",
                    sum, receiverUser.getName(), receiverUser.getLastName(), user.getBalance());
            return;
        }
        System.out.println("Nepakankamas pinigų likutis");
    }

    private void userAction(String action) {
        switch (action) {
            case "1" -> transferMoney();
            case "2" -> checkBalance();
            case "3" -> putMoney();
            case "4" -> System.out.println("Programa baigė darbą");
            default -> System.out.println("Tokios funkcijos nėra");
        }
    }

    private void menu() {
        String action;
        do {
            System.out.println("""
                1 - Pervesti pinigus kitam vartotojui
                2 - Pasitikrinti sąskaitos likutį
                3 - Įnešti pinigus
                4 - Išeiti
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
            default -> System.out.println("Tokios funkcijos nėra");
        }

    }

    private void registration() {
        System.out.println("Įveskite prisijungimo vardą");
        String userName = sc.nextLine();
        System.out.println("Įveskite savo vardą");
        String name = sc.nextLine();
        System.out.println("Įveskite savo pavardę");
        String lastName = sc.nextLine();
        double sum = getCorrectNumber("Įveskite pradinę sumą pinigu:");
        FindIterable<User> users = usersCollection.find();
        for (User u: users) {
            if (u.getUserName().equals(userName)) {
                System.out.println("Toks vartotojo vardas jau užimtas");
                welcomeMenu();
            }
        }
        User newUser = new User(null, userName, name, lastName, sum);
        usersCollection.insertOne(newUser);
        welcomeMenu();
    }

    private void login() {
        while (user == null) {
            System.out.println("Įveskite prisijungimo vardą:");
            String userName = sc.nextLine();
            user = usersCollection.find(eq("userName", userName)).first();
            if (user == null) {
                System.out.println("Tokio vartotojo nėra");
            }
        }
        System.out.printf("Sėkmingai prisijungėte %s %s%n", user.getName(), user.getLastName());
        menu();
    }

    private double getCorrectNumber(String text) {
        while (true){
            try {
                System.out.println(text);
                return Double.parseDouble(sc.nextLine());
            } catch (Exception e) {
                System.out.println("Blogas formatas");
            }
        }
    }

}

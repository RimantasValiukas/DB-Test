package lt.code.academy;

import org.bson.types.ObjectId;

public class User {
    private ObjectId id;
    private String userName;
    private String name;
    private String lastName;
    private double balance;

    public User() {}

    public User(ObjectId id, String userName, String name, String lastName, double balance) {
        this.id = id;
        this.userName = userName;
        this.name = name;
        this.lastName = lastName;
        this.balance = balance;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}

package org.example;


import java.util.Scanner;

import org.bson.Document;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase database = mongoClient.getDatabase("testdb");
        MongoCollection<Document> usersCollection = database.getCollection("users");
        MongoCollection<Document> transactionsCollection = database.getCollection("transactions");

        System.out.println("Vartotojo registracija");
        System.out.print("El. paštas: ");
        String email = scanner.nextLine();
        System.out.print("Slaptažodis: ");
        String password = scanner.nextLine();
        System.out.print("Balansas: ");
        double balance = Double.parseDouble(scanner.nextLine());

        if (usersCollection.find(new Document("email", email)).first() != null) {
            System.out.println("Klaida: šis el. paštas jau yra naudojamas!");
            return;
        }


        Document newUser = new Document("email", email).append("password", password).append("balance", balance);
        usersCollection.insertOne(newUser);

        // Pinigų forma
        System.out.println("\nPinigų siuntimas");
        System.out.print("Siuntėjo el. paštas: ");
        String senderEmail = scanner.nextLine();
        System.out.print("Gavėjo el. paštas: ");
        String receiverEmail = scanner.nextLine();
        System.out.print("Suma: ");
        double amount = Double.parseDouble(scanner.nextLine());

        Document sender = usersCollection.find(new Document("email", senderEmail)).first();
        if (sender == null) {
            System.out.println("Klaida: tokio siuntėjo nėra!");
            return;
        }
        double senderBalance = sender.getDouble("balance");
        if (senderBalance < amount) {
            System.out.println("Klaida: nepakanka lėšų!");
            return;
        }


        double senderNewBalance = senderBalance - amount;
        usersCollection.updateOne(new Document("email", senderEmail), new Document("$set", new Document("balance", senderNewBalance)));

        Document receiver = usersCollection.find(new Document("email", receiverEmail)).first();
        if (receiver == null) {
            System.out.println("Klaida: tokio gavėjo nėra!");
            return;
        }
        double receiverBalance = receiver.getDouble("balance");
        double receiverNewBalance = receiverBalance + amount;
        usersCollection.updateOne(new Document("email", receiverEmail), new Document("$set", new Document("balance", receiverNewBalance)));

        // Sukuriame naują transakcijos įrašą ir įrašome jį į duomenų bazę
        Document transaction = new Document("sender", senderEmail).append("receiver");
    }
}
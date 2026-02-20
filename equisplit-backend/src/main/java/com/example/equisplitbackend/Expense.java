package com.example.equisplitbackend;

import jakarta.persistence.*;

@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private int amount;
    private String description;
    private String category;
    private String friends; // comma-separated friends

    public Expense() {}

    public Expense(int amount, String description, String category, String friends) {
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.friends = friends;
    }

    // Getters and setters
    public Integer getId() { return id; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getFriends() { return friends; }
    public void setFriends(String friends) { this.friends = friends; }
}
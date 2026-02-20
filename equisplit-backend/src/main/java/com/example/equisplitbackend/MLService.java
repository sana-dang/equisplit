package com.example.equisplitbackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MLService {

    @Autowired
    private ExpenseRepository expenseRepository;

    public String predictNextExpense() {

        Iterable<Expense> expenses = expenseRepository.findAll();

        int total = 0;
        int count = 0;

        for (Expense e : expenses) {
            total += e.getAmount();
            count++;
        }

        if (count == 0) {
            return "No expenses yet to predict.";
        }

        int average = total / count;

        return "Predicted next expense amount: ₹" + average;
    }
}
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;

public class Main extends Application {

    // ===== DATABASE CONFIG =====
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/equisplit";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "SANADANG";

    private final ObservableList<String> expenseList = FXCollections.observableArrayList();
    private int total = 0;
    private final Label totalLabel = new Label("Total: ₹0");

    @Override
    public void start(Stage stage) {

        Label title = new Label("EquiSplit – Expense Manager");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        TextField friendField = new TextField();
        friendField.setPromptText("Friend Name");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Food", "Travel", "Rent", "Others");
        categoryBox.setPromptText("Category");

        Button addBtn     = new Button("Add Expense");
        Button deleteBtn  = new Button("Delete Selected");
        Button predictBtn = new Button("Predict Next Expense");

        ListView<String> listView = new ListView<>(expenseList);
        listView.setPrefHeight(220);

        loadExpensesFromDB();

        // ===== ADD EXPENSE =====
        addBtn.setOnAction(e -> {
            try {
                int amount = Integer.parseInt(amountField.getText().trim());
                String desc = descriptionField.getText().trim();
                String cat  = categoryBox.getValue();
                String friend = friendField.getText().trim();

                if (desc.isEmpty() || cat == null || friend.isEmpty()) {
                    showAlert("Fill all fields");
                    return;
                }

                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     PreparedStatement ps = conn.prepareStatement(
                             "INSERT INTO expenses(amount, description, category, friend) VALUES (?, ?, ?, ?)")) {

                    ps.setInt(1, amount);
                    ps.setString(2, desc);
                    ps.setString(3, cat);
                    ps.setString(4, friend);
                    ps.executeUpdate();
                }

                String row = "₹" + amount + " | " + cat + " | " + desc + " | " + friend;
                expenseList.add(row);

                total += amount;
                totalLabel.setText("Total: ₹" + total);

                amountField.clear();
                descriptionField.clear();
                friendField.clear();
                categoryBox.setValue(null);

            } catch (Exception ex) {
                showAlert("Invalid input");
            }
        });

        // ===== DELETE EXPENSE =====
        deleteBtn.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            try {
                String[] p = selected.split(" \\| ");
                int amount = Integer.parseInt(p[0].replace("₹", "").trim());

                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     PreparedStatement ps = conn.prepareStatement(
                             "DELETE FROM expenses WHERE amount=? AND category=? AND description=? AND friend=? LIMIT 1")) {

                    ps.setInt(1, amount);
                    ps.setString(2, p[1].trim());
                    ps.setString(3, p[2].trim());
                    ps.setString(4, p[3].trim());
                    ps.executeUpdate();
                }

                expenseList.remove(selected);
                total -= amount;

                if (total < 0) total = 0;
                totalLabel.setText("Total: ₹" + total);

            } catch (Exception ex) {
                showAlert("Delete failed");
            }
        });

        // ===== ML PREDICTION =====
        predictBtn.setOnAction(e -> {
            try {
                URL url = new URL("http://localhost:8080/api/ml/predict");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String response = br.readLine();
                br.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("ML Prediction");
                alert.setContentText(response);
                alert.show();

            } catch (Exception ex) {
                showAlert("Backend / ML service not running");
            }
        });

        HBox buttons = new HBox(10, addBtn, deleteBtn, predictBtn);

        VBox root = new VBox(
                12,
                title,
                amountField,
                descriptionField,
                categoryBox,
                friendField,
                buttons,
                listView,
                totalLabel
        );
        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 560, 600));
        stage.setTitle("EquiSplit");
        stage.show();
    }

    // ===== LOAD DB DATA =====
    private void loadExpensesFromDB() {
        expenseList.clear();
        total = 0;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT amount, description, category, friend FROM expenses")) {

            while (rs.next()) {
                int amount = rs.getInt("amount");
                String row = "₹" + amount + " | " +
                        rs.getString("category") + " | " +
                        rs.getString("description") + " | " +
                        rs.getString("friend");

                expenseList.add(row);
                total += amount;
            }

            totalLabel.setText("Total: ₹" + total);

        } catch (Exception ignored) {}
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
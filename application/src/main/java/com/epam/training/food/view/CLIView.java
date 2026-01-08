package com.epam.training.food.view;

import com.epam.training.food.domain.Credentials;
import com.epam.training.food.domain.Customer;
import com.epam.training.food.domain.Food;
import com.epam.training.food.domain.Order;
import com.epam.training.food.values.FoodSelection;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

import static java.lang.Integer.parseInt;

public class CLIView implements View {
    private final Scanner scanner;
    public CLIView(){
        scanner = new Scanner(System.in);
    }

    @Override
    public Credentials readCredentials() {
        Credentials credentials = new Credentials();
        System.out.print("Enter customer name: ");
        credentials.setUserName(scanner.nextLine().trim());
        System.out.print("Enter customer password: ");
        credentials.setPassword(scanner.nextLine().trim());
        return credentials;
    }

    @Override
    public void printWelcomeMessage(Customer customer) {
        System.out.println("Welcome " + customer.getName() + ". Your balance is: " + customer.getBalance() + " EUR.");
        System.out.println();
    }

    @Override
    public void printAllFoods(List<Food> foods) {
        System.out.println("Foods offered today: ");
        for (Food food : foods){
            System.out.println(" - " + food.getName() + " " + food.getPrice() + " EUR each.");
        }
        System.out.println();
    }

    @Override
    public FoodSelection readFoodSelection(List<Food> foods) {
        System.out.println("Please enter the name and amount of food (separated by comma) you would like to buy: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()){
            return FoodSelection.NONE;
        }
        String[] parts = input.split(",");
        if (parts.length != 2){
            throw new IllegalArgumentException("Illegal input format");
        }
        String foodName = parts[0].trim();
        int amount = parseInt(parts[1].trim());

        Food selectedFood = foods.stream().filter( f -> f.getName().equalsIgnoreCase(foodName) ).findFirst().orElseThrow();
        return new FoodSelection(selectedFood,amount);
    }

    @Override
    public void printAddedToCart(Food food, int pieces) {
        System.out.println("Added " + pieces + " piece(s) of " + food.getName() + " to the cart.");
        System.out.println();
    }

    @Override
    public void printErrorMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void printOrderCreatedStatement(Order order, BigDecimal balance) {
        List<String> foodNames = order.getOrderItems().stream().map( fd -> fd.getFood().getName()).toList();
        System.out.println("Order (items: " + foodNames  + ", price: " + order.getPrice() + " EUR, " +
                "timestamp: " + order.getTimestampCreated().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss")) + ") has been confirmed.");
        System.out.println("Your balance is " + balance + " EUR.");
        System.out.println("Thank you for your purchase.");
    }
}

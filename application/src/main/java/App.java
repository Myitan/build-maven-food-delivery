package com.epam.training.food;

import com.epam.training.food.data.FileDataStore;
import com.epam.training.food.domain.Credentials;
import com.epam.training.food.domain.Customer;
import com.epam.training.food.domain.Food;
import com.epam.training.food.service.AuthenticationException;
import com.epam.training.food.service.DefaultFoodDeliveryService;
import com.epam.training.food.service.FoodDeliveryService;
import com.epam.training.food.service.LowBalanceException;
import com.epam.training.food.values.FoodSelection;
import com.epam.training.food.view.CLIView;
import com.epam.training.food.view.View;

import java.util.List;

public class App {
    public static void main(String[] args) {
        String inputFolderPath = "test";
        FileDataStore fileDataStore = new FileDataStore(inputFolderPath);
        fileDataStore.init();

        FoodDeliveryService foodDeliveryService = new DefaultFoodDeliveryService(fileDataStore);
        View view = new CLIView();

        try {
            Credentials credentials = view.readCredentials();
            Customer customer = foodDeliveryService.authenticate(credentials);

            List<Food> availableFoods = foodDeliveryService.listAllFood();
            view.printWelcomeMessage(customer);
            view.printAllFoods(availableFoods);

            boolean shopping = true;
            while (shopping) {
                try {
                    FoodSelection selection = view.readFoodSelection(availableFoods);

                    if (selection == FoodSelection.NONE || selection.amount() == -1) {
                        shopping = false;
                    } else {
                        foodDeliveryService.updateCart(customer, selection.food(), selection.amount());
                        view.printAddedToCart(selection.food(), selection.amount());
                    }
                } catch (LowBalanceException e) {
                    view.printErrorMessage(e.getMessage());
                } catch (IllegalArgumentException e) {
                    view.printErrorMessage("Invalid input " + e.getMessage());
                }
            }

            if (!customer.getCart().getOrderItems().isEmpty()) {
                var order = foodDeliveryService.createOrder(customer);
                view.printOrderCreatedStatement(order, customer.getBalance());
            } else {
                System.out.println("No items in cart. Order not created.");
            }

        } catch (AuthenticationException e) {
            System.out.println("Authentication failed: " + e.getMessage());
        }
    }
}
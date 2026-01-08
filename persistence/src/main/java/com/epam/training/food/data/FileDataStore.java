package com.epam.training.food.data;

import com.epam.training.food.domain.Customer;
import com.epam.training.food.domain.Food;
import com.epam.training.food.domain.Order;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileDataStore implements DataStore {
    private final String baseDirPath;
    private List<Customer> customers;
    private List<Food> foods;
    private final List<Order> orders;

    public FileDataStore(String inputFolderPath) {
        this.baseDirPath = inputFolderPath;
        this.customers = new ArrayList<>();
        this.foods = new ArrayList<>();
        this.orders = new ArrayList<>();
    }

    public void init() {
        String customersPath = Paths.get(this.baseDirPath,"customers.csv").toString();
        String foodsPath = Paths.get(this.baseDirPath,"foods.csv").toString();
        CustomerReader customerReader = new CustomerReader();
        FoodReader foodReader = new FoodReader();
        this.customers = customerReader.read(customersPath);
        this.foods = foodReader.read(foodsPath);
    }

    @Override
    public List<Customer> getCustomers() {
        return customers;
    }

    @Override
    public List<Food> getFoods() {
        return foods;
    }

    @Override
    public List<Order> getOrders() {
        return orders;
    }

    @Override
    public Order createOrder(Order order) {
/*
                           I guess if we do not want to use stream then this loop also works
        long maxId = -1;
        for (Order o : orders) {
            if (o.getOrderId() > maxId) {
                maxId = o.getOrderId();
            }
        }
        long id = maxId + 1;
*/
        long id = orders.stream().mapToLong(Order::getOrderId).max().orElse(-1) +1;
        order.setOrderId(id);
        order.setTimestampCreated(LocalDateTime.now());
        this.orders.add(order);

        Customer customer = customers.stream()
                .filter(c -> c.getId() == order.getCustomerId())
                .findFirst()
                .orElse(null);
        if (customer != null){
            customer.getOrders().add(order);
        }
        writeOrders();
        return order;
    }

    @Override
    public void writeOrders() {
        String outputPath = Paths.get(this.baseDirPath,"orders.csv").toString();
        OrderWriter orderWriter = new OrderWriter();
        orderWriter.writeOrders(orders,outputPath);
    }
}

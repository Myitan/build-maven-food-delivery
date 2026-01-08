package com.epam.training.food.service;

import com.epam.training.food.data.DataStore;
import com.epam.training.food.domain.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultFoodDeliveryService implements FoodDeliveryService {
    private final DataStore dataStore;

    public DefaultFoodDeliveryService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public Customer authenticate(Credentials credentials) throws AuthenticationException {
/*
                            It might be clearer to read for some people
        for (Customer c : dataStore.getCustomers()) {
            if (c.getUserName().equals(credentials.getUserName()) &&
                    c.getPassword().equals(credentials.getPassword())) {
                return c;
            }
        }
        throw new AuthenticationException("Invalid login credentials");
*/

        return dataStore.getCustomers().stream()
                .filter(c -> c.getUserName().equals(credentials.getUserName()) &&
                        c.getPassword().equals(credentials.getPassword()))
                .findFirst()
                .orElseThrow(() -> new AuthenticationException("Invalid login credentials"));
    }

    @Override
    public List<Food> listAllFood() {
        return dataStore.getFoods();
    }

    @Override
    public void updateCart(Customer customer, Food food, int pieces) throws LowBalanceException {
        Cart cart = customer.getCart();
        if (pieces < 0){
            throw new IllegalArgumentException("Pieces cannot be negative");
        }
        Optional<OrderItem> orderItem = cart.getOrderItems().stream().filter( oi -> oi.getFood().getName()
                .equals(food.getName())).findFirst();
        if (pieces == 0){
            if ( orderItem.isEmpty() ){
                throw new IllegalArgumentException("Cannot remove. No food in the cart");
            }
            cart.getOrderItems().remove(orderItem.get());
        }else{
            BigDecimal itemPrice = food.getPrice().multiply(BigDecimal.valueOf(pieces));
            BigDecimal currentCartPrice = calculateCartPrice(cart);

            if (orderItem.isPresent()){
                currentCartPrice = currentCartPrice.subtract(orderItem.get().getPrice());
            }
            BigDecimal newCartPrice = currentCartPrice.add(itemPrice);

            if (customer.getBalance().compareTo(newCartPrice) < 0){
                throw new LowBalanceException("Unable to add order for " + food + " ,because it would exceed available balance");
            }
            if (orderItem.isPresent()){
                OrderItem item = orderItem.get();
                item.setPieces(pieces);
                item.setPrice(itemPrice);
            }else {
                OrderItem newItem = new OrderItem(food,pieces,itemPrice);
                cart.getOrderItems().add(newItem);
            }
        }

        cart.setPrice(calculateCartPrice(cart));
    }

    @Override
    public Order createOrder(Customer customer) throws IllegalStateException {
        Cart cart = customer.getCart();
        if (cart.getOrderItems().isEmpty()){
            throw new IllegalStateException("Cart is empty!");
        }
        Order order = new Order(customer);
        if (customer.getBalance().compareTo(order.getPrice()) < 0){
            throw new LowBalanceException("Insufficient balance for order");
        }
        Order createdOrder = dataStore.createOrder(order);
        customer.setBalance(customer.getBalance().subtract(createdOrder.getPrice()));
        cart.setOrderItems(new ArrayList<>());
        cart.setPrice(BigDecimal.ZERO);
        customer.getOrders().add(createdOrder);
        return createdOrder;
    }

    private BigDecimal calculateCartPrice(Cart cart){

/*
       return cart.getOrderItems().stream().map(OrderItem::getPrice)
               .reduce(BigDecimal.ZERO, BigDecimal::add);
*/

        BigDecimal currentCartPrice = BigDecimal.ZERO;
        for (OrderItem item : cart.getOrderItems()) {
            BigDecimal price = item.getPrice();
            currentCartPrice = currentCartPrice.add(price);
        }
        return currentCartPrice;
    }
}
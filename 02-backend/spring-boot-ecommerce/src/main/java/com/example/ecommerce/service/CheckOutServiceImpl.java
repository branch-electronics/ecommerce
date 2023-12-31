package com.example.ecommerce.service;

import com.example.ecommerce.dao.CustomerRepository;
import com.example.ecommerce.dto.PaymentInfo;
import com.example.ecommerce.dto.Purchase;
import com.example.ecommerce.dto.PurchaseResponse;
import com.example.ecommerce.entity.Customer;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CheckOutServiceImpl implements CheckoutService{

    private CustomerRepository customerRepository;

    @Autowired
    public CheckOutServiceImpl(CustomerRepository customerRepository,
                               @Value("${stripe.key.secret}") String secretKey) {

        this.customerRepository = customerRepository;

        // initialize Stripe API with secret key
        //Stripe.apiKey = secretKey;
    }
    @Override
    @Transactional
    public PurchaseResponse placeOrder(Purchase purchase) {

        Order order=purchase.getOrder();

        String orderTrackingNumber=generateOrderTrackingNumber();
        order.setOrderTrackingNumber(orderTrackingNumber);

        Set<OrderItem> orderItems=purchase.getOrderItems();
        orderItems.forEach(item->order.add(item));

        order.setBillingAddress(purchase.getBillingAddress());
        order.setShippingAddress(purchase.getShippingAddress());

        Customer customer=purchase.getCustomer();

        String theEmail=customer.getEmail();
        Customer customerFromDB=customerRepository.findByEmail(theEmail);
        if(customerFromDB!=null){
            customer=customerFromDB;
        }
        customer.add(order);

        customerRepository.save(customer);

        return new PurchaseResponse(orderTrackingNumber);
    }

    private String generateOrderTrackingNumber() {
        return UUID.randomUUID().toString();
    }

}

## CafeMate (Cafe Management System)

Modern cafe management system for handling users, menu items, orders, cart, and payments, built using Java Swing, MySQL, and JDBC.
The system allows users to sign up, log in, browse menu items by category, add items to cart, place orders, make payments, and view order history. Admins can manage menu items and view orders.

## What it does

User registration and login with validation

Menu browsing by category (coffee, snacks, desserts, etc.)

Add-to-cart with quantity selection

Cart summary with subtotal and total calculation

Checkout with payment options (Cash / Online)

Order confirmation with unique Order ID

View profile with order history

Admin features for menu and order management

## Application Screens (Java Swing)

Login Page – User authentication

Signup Page – New user registration

Dashboard / Menu Page – Category-wise food listing

Cart Page – Selected items and quantities

Payment Page – Payment method selection

Order Success Page – Order confirmation

Profile Page – User details and order history

Admin Panel – Menu and order management

## Modules
User Module

Signup with name, email, phone, password

Login with credentials

View profile and order history

Menu Module

Category-based food listing

Item price display

Ratings (optional)

Quantity selection

Cart Module

Add/remove items

Update quantity

Auto total calculation

Prevent checkout if cart is empty

Order Module

Generate unique order ID

Store order date & time

Save order items and total

Order status tracking

Payment Module

Cash on Delivery

Online payment (demo flow)

Payment success/failure handling

Admin Module

Add / update / delete menu items

View all orders

Manage categories

## Tech Stack

Frontend: Java Swing

Backend: Java (JDBC)

Database: MySQL

Architecture: MVC pattern

IDE: Eclipse / IntelliJ IDEA

Build Tool: JDK 8+

## Environment Setup
Prerequisites

Java JDK 8 or above

MySQL Server

IDE (Eclipse / IntelliJ)

## Sample Flow

User signs up and logs in

Browses cafe menu by category

Adds items to cart with quantity

Proceeds to checkout

Selects payment method

Order ID generated and saved

User views order in profile

## Security Notes

Passwords stored using hashing

Validation on login and signup

Prevent checkout when cart is empty

Order ID generated uniquely

## Common Features Validation

✔ Empty cart checkout prevention

✔ Quantity-based pricing

✔ Duplicate item handling

✔ Order timestamp

✔ Payment status tracking

## Common Scripts / Classes

Main.java – Application entry

LoginPage.java

SignupPage.java

MenuPage.java

CartPage.java

PaymentPage.java

OrderSuccessPage.java

ProfilePage.java

AdminPanel.java

DBConnection.java

## Troubleshooting

DB connection error: Check MySQL service & credentials

Login fails: Verify email & password

Order not saved: Check foreign key constraints

UI not loading: Ensure correct Swing thread usage

## Future Enhancements

Online payment gateway integration

Order tracking (Preparing / Ready / Delivered)

PDF invoice generation

Role-based admin authentication

Web-based version (Spring Boot)
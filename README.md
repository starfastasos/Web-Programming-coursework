# SupermarketApp - Supermarket Management System

## Table of Contents
1. [Project Description](#project-description)
2. [Key Features](#key-features)
3. [Installation Guide](#installation-guide)
4. [Usage Instructions](#usage-instructions)
   * [User Workflow](#user-workflow)
5. [Additional Information](#additional-information)

---
# SupermarketApp – Supermarket Management System

## Table of Contents

* Project Description
* Key Features
* Installation Guide
* Usage Instructions

  * User Workflow
* Additional Information

## Project Description

This project is an Android-based Supermarket Management System designed to help users organize their shopping activities, monitor expenses, and manage product purchases efficiently. The application was developed as part of the **"Web Programming"** course at the **University of Piraeus** for the academic year **2024–2025**.

## Key Features

### Product Catalog

* Browse products organized into categories such as Fresh Food, Dairy Products, Frozen Foods, and Cleaning Supplies.
* View product images, descriptions, prices, and availability information.
* Search and filter products by category, price, and active offers.

### Product Details

* Access detailed information for each product, including ingredients, nutritional values, pricing information, and available offers.

### Shopping List (Cart)

* Add and remove products from the shopping cart.
* Modify product quantities.
* View real-time estimated total purchase cost.

### Wishlist

* Save products for future purchases.
* Manage and remove products from the wishlist.

### Purchase History

* Review previous purchases.
* Repeat past shopping lists with a single action.
* Compare expenses on a weekly or monthly basis.

### Offers and Discounts

* Products on promotion are clearly highlighted.
* Users can easily identify available discounts and special offers.

### Localization

* Full support for both Greek and English languages.
* Automatic language selection based on device settings.

### Data Storage and Reliability

* Local data persistence using Room and SQLite.
* Database migration support.
* Integrated exception handling mechanisms to ensure application stability.

## Installation Guide

The application can be opened directly in Android Studio.

1. Extract the project folder `MySupermarketApplication`.
2. Open Android Studio and select **Open an Existing Project**.
3. Choose the `MySupermarketApplication` folder.
4. Wait for Gradle synchronization to complete.
5. Run the application on an Android emulator or a physical Android device.

## Usage Instructions

### User Workflow

1. Launch the application.
2. Browse the product catalog.
3. Search and filter products by category, price, or available offers.
4. View detailed information for any product.
5. Add products to the wishlist for future purchases.
6. Add products to the shopping cart and adjust quantities as needed.
7. Complete a purchase and save it to the purchase history.
8. Review previous purchases and spending statistics.

## Additional Information

The application is implemented using Kotlin and Jetpack Compose, following a layered architecture consisting of:

* UI Layer (Jetpack Compose)
* Domain Layer (ViewModel)
* Data Layer (Room Database and SQLite)

Additional technologies include Kotlin Coroutines, Navigation Compose, Gson, and Lifecycle Components.

For further details regarding the system architecture, database design, implementation process, and user manual, please refer to the accompanying **Report.pdf** document included in the repository.

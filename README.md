# Simple HR Management System

A desktop-based HR Management System built with JavaFX and a MySQL database. This application allows for managing employees and departments, including full CRUD (Create, Read, Update, Delete) operations and a notification system for salary increments.

## Features

* **Dashboard:** View a snapshot of all employees.
* **Employee Management:**
    * Add, update, and delete employee records.
    * Assign employees to dynamic departments.
    * Set individual salary increment policies (Yearly/Half-Yearly).
    * Record employee joining dates.
* **Department Management:**
    * Add, update, and delete departments.
    * Prevents deletion of departments that are currently in use.
* **Salary Increment Notifications:**
    * A notification system on the dashboard to check which employees are due for a salary increment based on their joining date and individual policy.

## Technology Stack

* **Language:** Java 19
* **Framework:** JavaFX 19
* **Database:** MySQL
* **Build Tool:** Apache Maven

## Database Setup

To run this project, you need to set up the `hrsystem` database. Run the following SQL commands in your MySQL client (e.g., MySQL Workbench, DBeaver, or the command line).

```sql
-- 1. Create the Database
CREATE DATABASE IF NOT EXISTS hrsystem;

-- 2. Select the database to use
USE hrsystem;

-- 3. Create the 'departments' table
CREATE TABLE IF NOT EXISTS departments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- 4. Create the 'employees' table
CREATE TABLE IF NOT EXISTS employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    department VARCHAR(255),
    salary DECIMAL(10, 2) NOT NULL,
    join_date DATE,
    increment_policy VARCHAR(20) DEFAULT 'Yearly'
);

-- 5. (Optional) Insert some initial data to get started
INSERT INTO departments (name) VALUES 
('IT'), 
('Human Resources'), 
('Finance'), 
('Marketing');

-- You can add employees through the application interface.

```
How to Run
First, ensure you have set up the database and updated the credentials in ```src/main/java/com/hrsystem/hrsystem/util/Database.java.``` Then, run the following commands in your terminal:
```
# 1. Clone the repository (replace with your repository URL)
git clone https://github.com/masumkazibd/hr-system.git

# 2. Navigate into the project directory
cd hr-system

# 3. Compile the project and install dependencies
mvn clean install

# 4. Run the application
mvn javafx:run
````

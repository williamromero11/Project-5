# Project-5
This project implements a two-tier client-server database application developed in Java using JDBC and MySQL. The system provides a graphical user interface that allows authenticated users to execute SQL commands against a backend database. The application demonstrates secure database connectivity, role-based access control, and dynamic SQL execution through a client interface.

The program connects to a MySQL database server and allows users to log in with different credentials corresponding to different roles. Based on the account used, users are granted specific database privileges when executing queries.

![accountant](https://raw.githubusercontent.com/williamromero11/Project-5/main/Project-5/Accountant/Screenshot%202026-03-08%20103026.png)

# Key Features

-Java Swing graphical client interface

-JDBC connection to MySQL database server

-Execution of SQL commands from the client interface

-Role-based database access using separate user credentials

-Display of query results in table format

-Error handling for invalid SQL statements

-Secure credential loading through configuration files

# Technologies Used

-Java

-Java Swing (GUI)

-JDBC

-MySQL

-SQL

![accountant](https://raw.githubusercontent.com/williamromero11/Project-5/main/Project-5/Accountant/Screenshot%202026-03-08%20103152.png)

# System Architecture

Client Application (Java Swing GUI)
            |
            | JDBC
            |
MySQL Database Server

# User Roles

The application supports multiple user accounts with different database privileges.

Example roles used in testing:

User	Purpose
Client 1	Executes general SQL queries
Client 2	Executes restricted queries
Accountant	Performs accounting-related database operations

Each role connects using different credentials stored in configuration files.

# How the Application Works

-User launches the SQL client application.

-The program loads database connection credentials.

-The user enters SQL commands in the GUI.

-The application sends the command to the MySQL server using JDBC.

-Results are returned and displayed in a results table.

-Invalid queries or permission errors are displayed in the interface.

# Project Structure 
```
SQLClient/
│
├── SQLClient.java
├── db.properties
├── client1.properties
├── client2.properties
├── accountant.properties
│
└── screenshots/
    ├── login.png
    ├── query_execution.png
    ├── result_table.png
```
 
# Example SQL Queries

SELECT * FROM suppliers;

SELECT name, status
FROM suppliers
WHERE status > 10;

INSERT INTO shipments VALUES ('S1','P1','J1',200);

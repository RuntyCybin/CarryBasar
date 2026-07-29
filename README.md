# CarryBasar

## Authentication
- JWT token dependencies added
- JWT token configuration added

## Database
- Database scripts are in /resources/scripts.sql

## About the app
- Only the TRANSPORTER role can accept orders
- Only the TRANSPORTER role can check the accepted orders
- The TRANSPORTER user can only see his orders
- Only the CARRY role can create orders

## Compilation & Deployment
- To compile the project
```
mvn clean install
```
- To run the project
```
mvn spring-boot:run
```
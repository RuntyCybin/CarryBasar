# CarryBasar

- JWT token dependencies added
- JWT token configuration added
- Database scripts are in /resources/scripts.sql 
- Only the TRANSPORTER role can accept orders
- Only the TRANSPORTER role can check the accepted orders
- Only NORMAL role can create orders
- The user can only see his orders
- To compile the project
```
mvn clean install
```
- To run the project
```
mvn spirng-boot:run
```
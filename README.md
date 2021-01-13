GLA Ilr
=======

The GLA Skills Gateway is a web based system that stores ILR (Individual Learning Records) data, processes validation and calculations on them and feeds the results to OPS system.


Components of this application

•	Web Application	: An HTML 5 dynamic web application built using Spring Boot and Thymeleaf.
•	Web Service API : A set of RESTful web services implemented in Java using the Spring Boot framework, and deployed to Amazon Elastic Beanstalk servers.
•	Database : A PostgreSQL relational database, utilising Amazonâ€™s Relational Database Service (RDS).
•	JasperSoft Server : Ad-hoc Reporting System
•	OPS Integration : Interface with OPS for SSO and pushing ILR data.
•	DfE Integration : Spring Integration with the DfE FTP server to auto download ILR files

Software Architecture

User Interface Layer:

•	HTML5, CSS and JavaScript
•	Rendered using Spring Boot and Thymeleaf
•	Chart.js (https://www.chartjs.org/)

Service layer:

•	RESTful Web Service API
•	Implemented in Java using Spring Boot framework
•	Database access via JPA
•	Security via Spring Security
•	REST interface with OPS for SSO and pushing ILR data

Backend layer

•	PostgreSQL Relational Database
•	Database schema managed using Liquibase



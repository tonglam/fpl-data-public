# Overall
**[Fpl-data](https://github.com/tonglam/fpl-data-public)** fetches data from the *[Fantasy Premier League](https://fantasy.premierleague.com/)* servers, cleans and transforms the data, and then stores it in MySQL and Redis. 

Data processing occurs in three scenarios: every day, match day, and every gameweek, implemented using Spring Boot schedules for these tasks.

# Tech Stack
- **Java 20 + Spring Boot 3**
- MySQL for data storage
- Redis for caching
- Maven for build and dependency management
- Logback for logging
- Jasypt for encryption and decryption of sensitive data
- Jenkins for CI/CD

Note: You may need to install Browser Extension to view the following mermaid diagram.

For Chrome: [Mermaid Previewer](https://chromewebstore.google.com/detail/oidjnlhbegipkcklbdfnbkikplpghfdl)

# Flow Diagram

# Modules

## Service

## DB

## Caching

## Scheduled Task

## AOP
The usage of AOP in the project is to log the service behaviors without modifying the business logic.

## Logging
To facilitate better maintenance, the logging in the project is designed to be flexible and user-friendly. 
The project utilizes **logback** as the logging framework and **slf4j** as the logging facade. 
The logback configuration file, *logback-spring.xml*, is located in the resources folder and is tailored for flexibility and ease of use.

Logs are separated into three files:
- *interface.log*: Time-based, rolled daily, used for monitoring the requests and responses of the interfaces, specifically HTTP calls between this project and others.
- *task.log*: Time-based, rolled daily, used for monitoring scheduled tasks.
- *fpl-data.log*: Time-based, rolled daily, used for monitoring the business logic of the project.

# Deployment
This project is deployed on a Linux server. The deployment process is automated using **Jenkins**.

The deployment process is as follows:
- Jenkins pulls the code from the **GitHub repository**.
- Jenkins builds the project using **Maven**.
- Jenkins publishes the built artifact to the server via **Plugin publish Over SSH**.

# Designing Vital Services

# Who use it?
[Fpl-data](https://github.com/tonglam/fpl-data-public) provides the ability to fetch transformed and cleaned Fantasy Premier League data.

There are primarily two projects utilizing this data:
- **[FPL](https://github.com/tonglam/fpl-public)**: The backend project for the website and WeChat Mini Program *LetLetMe*. 
It uses the data processed by **Fpl-data(https://github.com/tonglam/fpl-data-public)** to offer services for users to view their scoring, ranking, and summary reports for their Fantasy Premier League team.
- **[TelegramBot](https://github.com/tonglam/telegramBot-public)**: A Java-based project that offers users the service to retrieve their FPL data via the Telegram bot *letletme*.
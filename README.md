# Overall
**[Fpl-data](https://github.com/tonglam/fpl-data-public)** fetches data from the *[Fantasy Premier League](https://fantasy.premierleague.com/)* servers, cleans and transforms the data, and then stores it in MySQL and Redis. 

Data processing occurs in three scenarios: every day, match day, and every gameweek, implemented using Spring Boot schedules for these tasks.

In the initial stages, the services in this project were originally designed within the **[FPL](https://github.com/tonglam/fpl-public)** project. 
As the project expanded, these services were extracted and relocated to this dedicated project, a strategic move aimed at enhancing maintenance and scalability.

At present, these two projects operate independently, with **[Fpl-data](https://github.com/tonglam/fpl-data-public)** handling data,
while **[FPL](https://github.com/tonglam/fpl-public)** is responsible for delivering services to users. 
This architectural setup has proven effective for over two years.

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
Spring Boot schedules prove effective for this project, and I opted not to introduce additional scheduling frameworks such as xxl-job, Quartz, or others to maintain simplicity.

### Daily Task
These tasks handle the data that needs to be fetched and processed every day.

#### Event Task
Given that the Premier League may adjust the schedule at any time during the season, the event data needs daily updates to ensure alignment with the real schedule in the database.

#### Player Value Task
One of the most important components of the _FPL_ game is the player's value, which affects the strategy of playing the game massively. 
The player value will be updated at 9:30 am BST, so the purpose of this task is to fetch the changing of the player value in time and show to the user.

#### Player Value Info Task
Simultaneously with the player value update, the player value info is also refreshed. 
This information includes the likelihood of playing in the next round, transfer in and out numbers, and more.

#### Player Stat Task
Player stats, such as goals, assists, and clean sheets, are updated daily.

#### Entry Info Task
As _FPL_ allows players to change their team name and username, this task ensures the accurate representation of this information in the database, maintaining synchronization with real data.

#### Tournament Info Task
Tournaments in _LetLetMe_ allow users to establish custom tournaments to compete with friends. 
This task updates tournament information, accommodating changes in basic details.

### Match Day Task
These tasks manage data changes during match days, primarily focusing on data dependent on match results

#### Event Live Task
Among the critical tasks in the project, the Event Live Task provides real-time data for every player in the event. 
This data encompasses the player's points, minutes played, goals, assists, and more. 
It forms the basis for calculating live scores for each user's FPL team, contributing to live scores and rankings within their respective tournaments. 
Due to server limitations, the update period for this task is set to 5 minutes, deviating from the real-time updates during actual matches.

#### Event Live Summary Task
The Event Live Summary task consolidates player event live stats from gameweek 1 to the current gameweek into a single record.

#### Event Live Explain Task
The Event Live Explain Task offers detailed insights into a player's live event data, explaining how they accumulated points. This includes information on playing minutes, goals, assists, clean sheets, and more.

#### Event Overall Result Task
This task updates the overall results of the event, encompassing highest scores, chips played, most-captained players, most-transferred players in the event, and more.

### Gameweek Tournament Task
These tasks are designed to update the tournaments results for every player in the tournaments after every match day, including:
- player's points, ranking details
- player's picks, chips played, starting XI, and captain
- player's transfers, hits, and additional details

### Gameweek Report Task
Reports are generated for each tournament after every gameweek, showcasing the ranking, points, and other relevant details. 
These reports play a crucial role in data analysis and visualization for users.

## AOP
The usage of AOP in the project is to log the service behaviors without modifying the business logic.

## Logging
To facilitate better maintenance, the logging in the project is designed to be flexible and user-friendly. 
The project utilizes **Logback** as the logging framework and **Slf4j** as the logging facade. 
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
It uses the data processed by **[Fpl-data](https://github.com/tonglam/fpl-data-public)** to offer services for users to view their scoring, ranking, and summary reports for their Fantasy Premier League team.
- **[TelegramBot](https://github.com/tonglam/telegramBot-public)**: A Java-based project that offers users the service to retrieve their FPL data via the Telegram bot *letletme*.
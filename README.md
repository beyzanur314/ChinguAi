🤖 ChinguAI - Intelligent Chat Assistant
ChinguAI is a modern web application built with Spring Boot and integrated with the Groq API. It enables users to interact seamlessly with AI while persisting chat histories in a database.

🚀 Key Features
Groq AI Integration: Leverages Spring Boot backend architecture to generate fast and intelligent AI responses via the Groq API.
Chat History Management: Stores user prompts and AI response logs securely in MS SQL Server using the ChatMessage entity.
Secure API Key Handling: Utilizes Environment Variables (${GROQ_API_KEY}) to prevent hardcoding sensitive credentials into the codebase.
User-Friendly UI: Simple, interactive, and responsive web chat interface built with Thymeleaf and HTML templates.
🛠️ Tech Stack
Backend: Java 17+, Spring Boot 3
AI Integration: Groq API / Spring AI
Database: MS SQL Server
ORM: Spring Data JPA / Hibernate
Frontend: HTML5, CSS3, Thymeleaf
Build Tool: Maven
📋 Prerequisites
Ensure you have the following installed before running the application locally:

Java Development Kit (JDK 17 or higher)
Apache Maven
MS SQL Server
A valid Groq API Key
⚙️ Installation & Setup
1. Clone the Repository
git clone [https://github.com/beyzanur314/ChinguAi.git](https://github.com/beyzanur314/ChinguAi.git)
cd ChinguAi
2. Configure the Database
Verify and update your SQL Server credentials in src/main/resources/application.properties:

Properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ChinguAiDB;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.jpa.hibernate.ddl-auto=update
3. Set Up Environment Variables
To keep your API key secure, export your Groq API key as an environment variable before running the application:

Windows (CMD / PowerShell):

DOS
set GROQ_API_KEY=your_actual_groq_api_key
Linux / macOS / Git Bash:

Bash
export GROQ_API_KEY="your_actual_groq_api_key"
4. Build & Run
Run the application using the Maven wrapper:

Bash
./mvnw spring-boot:run
Once started, access the chat application in your browser at http://localhost:8080.

📂 Project Structure
Plaintext
ChinguAi/
├── src/
│   ├── main/
│   │   ├── java/com/beyzanur/chingu_ai/
│   │   │   ├── model/         # Entity classes (ChatMessage)
│   │   │   ├── repository/    # Data access layer (JPA Repositories)
│   │   │   ├── service/       # Business logic & AI interaction services
│   │   │   └── controller/    # Web and REST Controllers
│   │   └── resources/
│   │       ├── templates/     # UI views (chat.html)
│   │       └── application.properties
│   └── test/                  # Unit and integration tests
├── pom.xml
└── README.md
👩‍💻 Author
Beyzanur Altındal - https://github.com/beyzanur314

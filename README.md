# 📨 Real-Time Chat Application - Backend


This is the **backend service** for the real-time messaging application, built with **Spring Boot**,
**WebSockets (STOMP)**, and **JWT Authentication**. It handles user authentication, message storage,
WebSocket communication, and chat management.

---

## 🚀 Features
- 🔑 **JWT Authentication** (Login, Logout, Password Reset)
- 📩 **Real-Time Messaging** (One-on-One & Group Chats)
- 👥 **Group Management** (Create, Add/Remove Users, Assign Admins)
- 📌 **Message History & Pagination**
- 📡 **WebSocket-Based Messaging**
- 🗄️ **Spring Data JPA with PostgreSQL**

---

## 🛠️ Tech Stack
| Technology | Description |
|------------|------------|
| **Spring Boot** | Backend framework |
| **Spring WebSockets (STOMP)** | Real-time messaging |
| **Spring Security + JWT** | Authentication |
| **Spring Data JPA** | ORM for database access |
| **PostgreSQL** | Database |
| **Maven** | Build tool |

---

## 📂 Project Structure
###
```
backend/
│── src/
│   ├── main/
│   │   ├── java/com/chat_app/
│   │   │   ├── config/           # WebSocket & Security Configurations
│   │   │   ├── controllers/      # REST Controllers (Auth, Chat, Message)
│   │   │   ├── dto/              # Data Transfer Objects (DTOs)
│   │   │   ├── models/           # Entity Models (User, Chat, Message)
│   │   │   ├── repository/       # Spring Data JPA Repositories
│   │   │   ├── services/         # Business Logic (UserService, ChatService)
│   │   │   ├── websocket/        # WebSocket Configuration & Message Handling
│   │   ├── resources/
│   │   │   ├── application.properties  # Database & WebSocket Configurations
│── pom.xml             # Maven Dependencies
│── README.md           # Project Documentation
```
###

---

## 🔧 Setup & Installation

### 1️⃣ Prerequisites
Before starting, ensure you have the following installed:
- **Java 17+**
- **PostgreSQL** (or another database)
- **Maven** (run `mvn -v` to check)

### 2️⃣ Clone Repository
```sh
git clone https://github.com/Vigneshkumar-D/chat-app-java.git
cd chat-app-java
```

### 3️⃣ Configure Database
Modify `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/chatdb
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update
```

### 4️⃣ Build & Run
```sh
mvn clean install
mvn spring-boot:run
```
The server will run on: **`http://localhost:4001`**

---

## 🔌 WebSocket Endpoints
| Action | Endpoint |
|--------|---------|
| **Connect to WebSocket** | `ws://localhost:4001/ws` |
| **Subscribe to private messages** | `/user/queue/message/{chatId}` |
| **Subscribe to group messages** | `/topic/chat/group/{chatId}` |
| **Send a message** | `/app/chat.sendMessage` |

---


## **API Endpoints**

### **1. Chat Controller (`/api/chats`)**
| Method | Endpoint | Description |
|--------|---------|-------------|
| **GET** | `/api/chats/{chatId}` | Fetch a specific chat by ID |
| **PUT** | `/api/chats/{chatId}` | Update a chat by ID |
| **POST** | `/api/chats` | Create a new chat |
| **GET** | `/api/chats/list/{userId}` | Fetch the chat list for a user |

### **2. User Controller (`/api/users`)**
| Method | Endpoint | Description |
|--------|---------|-------------|
| **POST** | `/api/users/register` | Register a new user |
| **GET** | `/api/users` | Fetch all users |
| **GET** | `/api/users/current-user` | Fetch the currently logged-in user |

### **3. Authentication Controller (`/api/auth`)**
| Method | Endpoint | Description |
|--------|---------|-------------|
| **POST** | `/api/auth/logout` | Logout user |
| **POST** | `/api/auth/login` | Login user |
| **POST** | `/api/auth/forget-password` | Request password reset |
| **POST** | `/api/auth/confirm-reset` | Confirm password reset |

### **4. Message Controller (`/api/messages`)**
| Method | Endpoint | Description |
|--------|---------|-------------|
| **GET** | `/api/messages/{id}` | Fetch a message by ID |
| **GET** | `/api/messages/chat/{chatId}` | Fetch all messages for a chat |

---

## 🔥 WebSocket Integration Example (Frontend)
To connect to the WebSocket in a **React frontend**, use **SockJS & StompJS**:
```javascript
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const socket = new SockJS("http://localhost:4001/ws");
const stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    onConnect: (frame) => {
        console.log("Connected:", frame);

        // Subscribe to a private chat
        stompClient.subscribe(`/user/queue/message/47`, (message) => {
            console.log("Received Message:", JSON.parse(message.body));
        });
    }
});

stompClient.activate(); ```

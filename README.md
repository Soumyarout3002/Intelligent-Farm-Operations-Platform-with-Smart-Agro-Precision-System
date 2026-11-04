# Intelligent Farm Operations Platform with Smart Agro Precision System

A web-based farm management platform designed to help farmers manage farm activities, monitor weather, track resources, and receive notifications.

## Technologies Used

### Frontend
- React
- Vite
- JavaScript
- React Router
- Lucide React

### Backend
- Java 17+
- Spring Boot
- Maven
- Spring Data JPA
- MySQL

## Prerequisites
- Git
- Node.js and npm
- Java JDK 17 or later
- MySQL Server

## 1. Clone the Repository

    git clone -b varshini --single-branch https://github.com/Soumyarout3002/Intelligent-Farm-Operations-Platform-with-Smart-Agro-Precision-System.git

## 2. Database Setup

Start MySQL and create the database:

    CREATE DATABASE farmverse_nexus;

## 3. Backend Setup

Open the backend directory:

    cd backend
    Copy-Item .env.example .env
    notepad .env

Enter your actual MySQL password in the .env file:

    DB_URL=jdbc:mysql://localhost:3306/farmverse_nexus
    DB_USERNAME=root
    DB_PASSWORD=your_actual_mysql_password

Save the file and run:

    .\mvnw.cmd spring-boot:run

Backend URL: http://localhost:8080

## 4. Frontend Setup

Open another terminal in the cloned project folder:

    cd frontend
    Copy-Item .env.example .env
    npm ci
    npm run dev

Open the local URL displayed by Vite, usually:

http://localhost:5173

## 5. Weather and Notifications

Weather Monitoring and Notifications use sample data by default. Do not disable sample mode unless the corresponding backend API endpoints have been implemented.

## 6. Important Notes

- Each team member must create their own backend/.env and frontend/.env files.
- Never upload actual passwords or secret credentials to GitHub.
- The .env.example files contain configuration templates.
- Keep both backend and frontend running while using the application.
- Run npm ci inside the frontend directory after cloning.

## 7. Git Branch

The integration is maintained in the varshini branch.

To get the latest updates:

    git pull origin varshini
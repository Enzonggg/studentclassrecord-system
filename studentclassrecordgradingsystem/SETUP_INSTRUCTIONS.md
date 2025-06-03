# Setup Instructions for Student Class Record Grading System

## Prerequisites

1. Java 11 or higher
2. Maven

## Installation

1. Clone the repository
2. Run the application using:

   ```bash
   mvn clean javafx:run
   ```

   Or use the provided `run.bat` script on Windows.

## Default Logins

The system comes with three pre-configured user accounts:

### Admin Account

- Username: admin
- Password: admin123

### Teacher Account

- Username: teacher
- Password: teacher123

### Student Account

- Username: student
- Password: student123

## Running the Application

1. Open a command prompt in the project directory
2. Run the following command:

   ```bash
   mvn clean javafx:run
   ```

   Or use the provided `run.bat` script

## Troubleshooting

If you encounter any issues:

1. Ensure Java 11 or higher is installed and properly configured
2. Verify that JAVA_HOME environment variable is set correctly
3. Try running with admin privileges if needed

## Notes

This version uses in-memory storage and will not persist data between application restarts.

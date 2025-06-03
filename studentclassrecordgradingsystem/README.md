# Student Class Record Grading System

A JavaFX application for managing student grades, classes, and academic records.

## Features

- Multi-user system with Admin, Teacher, and Student roles
- Class management and enrollment
- Assignment creation and grading
- Student grade tracking and reporting
- Dashboard with key metrics for each user role

## Technical Details

This application is built using:

- Java 11
- JavaFX 17
- In-memory data storage
- Maven for dependency management and building

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven

### Installation

1. Clone the repository
2. Run the application using:

   ```bash
   mvn javafx:run
   ```

   Or use the provided `run.bat` script on Windows.

### Default Admin Login

- Username: admin
- Password: admin123

## Project Structure

- `src/main/java/com/grading/` - Java source code
  - `admin/` - Admin-specific controllers
  - `common/` - Shared functionality
  - `model/` - Data models
  - `student/` - Student-specific controllers
  - `teacher/` - Teacher-specific controllers
  - `util/` - Utility classes
- `src/main/resources/` - Resources
  - `css/` - Stylesheets
  - `fxml/` - UI layout files
  - `images/` - Application images

## Data Storage

This application uses in-memory storage, which means:

- All data is stored in memory while the application is running
- Data will be reset when the application is restarted
- No external database is required

## License

This project is licensed under the MIT License - see the LICENSE file for details.

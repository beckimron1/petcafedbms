To run this code, you must compile and run both insertSampleData.java to create the tables and insert the sample data into SQL.

Specific instructions on how to run these files can be seen in the file headers of each individual program.

All commands below assume you are in the CS460Prog4 directory

javac PetCafeDatabaseProgram/src/insertSampleData.java
java -cp /usr/lib/oracle/19.8/client64/lib/ojdbc8.jar:PetCafeDatabaseProgram/src insertSampleData abduvaliev a6534

javac PetCafeDatabaseProgram/src/petcafe/DBUtil.java PetCafeDatabaseProgram/src/petcafe/PetCafeApp.java
java -cp /usr/lib/oracle/19.8/client64/lib/ojdbc8.jar:PetCafeDatabaseProgram/src petcafe.PetCafeApp abduvaliev a6534


The Workload Distribution is as follows:

AJ Cronin
 - Created Sample Data for Each Table
 - Created "Create" Statements for each table in initializeTables.SQL
 - Created insertSampleData.java
 - Documented and Commented both insertSampleData.java and PetCafeApp.java
 - Logical Design and Normalization Analysis in Design Report

Amirkhon Makhkamov
 - Created Constraints in initializeTables.SQL
 - Created Foreign Key References in initializeTables.SQL
 - Created Functionality for all Add, Update, and Removals of tables
 - Created Functionality for all query reports
 - Created Functionality and Design Report for custom query

Imronbek Abduvaliev
 - Created Initial ER Diagram
 - Updated ER Diagram Based on TA Feedback
 - Tested Code on Lectura
 - Fixed bugs and made changes to the code and SQL queries
 - Created ER Diagram / Conceptual Design section of design report


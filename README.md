# Real Estate Office DBMS

A Java Swing desktop application for a real-estate office, backed by MySQL.

## Requirements
- JDK (includes `javac` and `jar`)
- MySQL Server

## Database setup
1. Create a MySQL database named `real_estate`.
2. Run the SQL schema/setup script supplied with your course project, if applicable, to create the required tables.
3. Configure the database using `REAL_ESTATE_DB_URL`, `REAL_ESTATE_DB_USER`, and `REAL_ESTATE_DB_PASSWORD`. The URL defaults to localhost database `real_estate`; username defaults to `root`; password defaults to empty. Never commit real credentials.

## Build and run
```bash
chmod +x build.sh
./build.sh
java -jar RealEstateUI.jar
```

The MySQL Connector/J driver is in `lib/` and is referenced by the JAR manifest.

## Structure
- `src/` Java source
- `lib/` MySQL JDBC driver
- `out/` generated classes (created by build; ignored by Git)
- `RealEstateUI.jar` packaged application

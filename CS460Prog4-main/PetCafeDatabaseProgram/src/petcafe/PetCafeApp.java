package petcafe;

/*
 * PetCafeApp.java -- This program is for the user interactive part of the Pet Cafe Project.
 *                    Users should be able to add, update, and remove to the member, pet,
 *                    menu, reservation, health record, adoption, and event tables.
 *                    In addition, the user can choose from several queries to get information
 *                    about the database according to the Prog4 spec.
 * 
 * To compile and execute this program on lectura:
 *
 *   Add the Oracle JDBC driver to your CLASSPATH environment variable:
 *
 *         export CLASSPATH=/usr/lib/oracle/19.8/client64/lib/ojdbc8.jar:${CLASSPATH}
 *
 *     (or whatever shell variable set-up you need to perform to add the
 *     JAR file to your Java CLASSPATH)
 *
 *   Compile this file:
 *
 *         javac JDBC.java
 *
 *   Finally, run the program:
 *
 *         java JDBC <oracle username> <oracle password>
 *
 * As of 12/7/2025 there are no known bugs. Each input checks that values are within bounds.
 * 
 * Author: Amirkhon Makhkamov
 * Course: CSc 460 - Database Design
 * Assignment: Program #4 - Pet Cafe
 * Instructor: L. McCann
 * TAs: J. Shen, U. Upadhyay
 * Due Date: December 8th, 2025
 */


import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class PetCafeApp {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection();
             Scanner in = new Scanner(System.in)) {

            System.out.println("Welcome to the Pet Cafe Management System!");

            boolean running = true;
            while (running) {
                System.out.println("\n-- Main Menu --");
                System.out.println("1. Members");
                System.out.println("2. Pets & Adoptions");
                System.out.println("3. Reservations");
                System.out.println("4. Orders");
                System.out.println("5. Events");
                System.out.println("6. Health Records");
                System.out.println("7. Reports");
                System.out.println("0. Exit");
                System.out.print("Choice: ");

                String choice = in.nextLine().trim();
                switch (choice) {
                    case "1":
                        memberMenu(conn, in);
                        break;
                    case "2":
                        petMenu(conn, in);
                        break;
                    case "3":
                        reservationMenu(conn, in);
                        break;
                    case "4":
                        orderMenu(conn, in);
                        break;
                    case "5":
                        eventMenu(conn, in);
                        break;
                    case "6":
                        healthMenu(conn, in);
                        break;
                    case "7":
                        reportsMenu(conn, in);
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            }

            System.out.println("Goodbye!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    /*---------------------------------------------------------------------
    |  Method nextId(conn, table, col)
    |
    |  Purpose:  Takes in a table and finds its max id so that the next created one
    |            is one higher than the previous max.
    |
    |  Pre-condition:  conn is established correctly. table is an existing table.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      table    -- Name of SQL table.
    |      col      -- index that new id is found in
    |
    *-------------------------------------------------------------------*/

    private static int nextId(Connection conn, String table, String col)
            throws SQLException {
        String sql = "SELECT NVL(MAX(" + col + "),0) + 1 FROM " + table;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /*---------------------------------------------------------------------
    |  Method readInt(in, prompt)
    |
    |  Purpose:  Reads in the next integer from the user's input
    |
    |  Pre-condition:  Scanner is establish correctly, prompt is correct for current query.
    |
    |  Parameters:
    |      in       -- The keyboard input scanner
    |      prompt   -- The prompt asked for specific query.
    |
    *-------------------------------------------------------------------*/

    private static int readInt(Scanner in, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method readOptionalInt(in, prompt)
    |
    |  Purpose:  Reads in the next integer from the user's input but optionally.
    |
    |  Pre-condition:  Scanner is establish correctly, prompt is correct for current query.
    |
    |  Parameters:
    |      in       -- The keyboard input scanner
    |      prompt   -- The prompt asked for specific query.
    |
    *-------------------------------------------------------------------*/

    private static Integer readOptionalInt(Scanner in, String prompt) {
        while (true) {
            System.out.print(prompt + " (blank for null): ");
            String s = in.nextLine().trim();
            if (s.isEmpty()) return null;
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer or leave blank.");
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method readString(in, prompt)
    |
    |  Purpose:  Reads in the next String from the user's input.
    |
    |  Pre-condition:  Scanner is establish correctly, prompt is correct for current query.
    |
    |  Parameters:
    |      in       -- The keyboard input scanner
    |      prompt   -- The prompt asked for specific query.
    |
    *-------------------------------------------------------------------*/
    private static String readString(Scanner in, String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    /*---------------------------------------------------------------------
    |  Method readDate(in, prompt)
    |
    |  Purpose:  Reads in the next Date from the user's input.
    |
    |  Pre-condition:  Scanner is establish correctly, prompt is correct for current query.
    |
    |  Parameters:
    |      in       -- The keyboard input scanner
    |      prompt   -- The prompt asked for specific query.
    |
    *-------------------------------------------------------------------*/

    private static Date readDate(Scanner in, String prompt) {
        while (true) {
            System.out.print(prompt + " (yyyy-MM-dd, blank for today): ");
            String s = in.nextLine().trim();
            if (s.isEmpty()) {
                return Date.valueOf(LocalDate.now());
            }
            try {
                LocalDate ld = LocalDate.parse(s, DATE_FMT);
                return Date.valueOf(ld);
            } catch (Exception e) {
                System.out.println("Invalid date format.");
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method readOptionalDate(in, prompt)
    |
    |  Purpose:  Reads in the next Date from the user's input, but optionally.
    |
    |  Pre-condition:  Scanner is establish correctly, prompt is correct for current query.
    |
    |  Parameters:
    |      in       -- The keyboard input scanner
    |      prompt   -- The prompt asked for specific query.
    |
    *-------------------------------------------------------------------*/

    private static Date readOptionalDate(Scanner in, String prompt) {
        while (true) {
            System.out.print(prompt + " (yyyy-MM-dd, blank for null): ");
            String s = in.nextLine().trim();
            if (s.isEmpty()) return null;
            try {
                LocalDate ld = LocalDate.parse(s, DATE_FMT);
                return Date.valueOf(ld);
            } catch (Exception e) {
                System.out.println("Invalid date format.");
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method readDateTime(in, prompt)
    |
    |  Purpose:  Reads in the next date time from the user's input.
    |
    |  Pre-condition:  Scanner is establish correctly, prompt is correct for current query.
    |
    |  Parameters:
    |      in       -- The keyboard input scanner
    |      prompt   -- The prompt asked for specific query.
    |
    *-------------------------------------------------------------------*/

    private static Timestamp readDateTime(Scanner in, String prompt) {
        while (true) {
            System.out.print(prompt + " (yyyy-MM-dd HH:mm): ");
            String s = in.nextLine().trim();
            try {
                LocalDateTime dt = LocalDateTime.parse(s, DATETIME_FMT);
                return Timestamp.valueOf(dt);
            } catch (Exception e) {
                System.out.println("Invalid datetime format.");
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method memberMenu(conn, in)
    |
    |  Purpose:  Displays the options for interacting with member table including adding,
    |            updating, deleting, and displaying information.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void memberMenu(Connection conn, Scanner in) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Member Menu ---");
            System.out.println("1. Add member");
            System.out.println("2. Update member");
            System.out.println("3. Delete member");
            System.out.println("4. List members");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String c = in.nextLine().trim();
            try {
                switch (c) {
                    case "1":
                        addMember(conn, in);
                        break;
                    case "2":
                        updateMember(conn, in);
                        break;
                    case "3":
                        deleteMember(conn, in);
                        break;
                    case "4":
                        listMembers(conn);
                        break;
                    case "0":
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    /*---------------------------------------------------------------------
    |  Method addMember(conn, in)
    |
    |  Purpose:  Adds a new member to the member table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void addMember(Connection conn, Scanner in) throws SQLException {
        int id = nextId(conn, "Member", "member_id");
        String name = readString(in, "Name: ");
        String phone = readString(in, "Phone (optional): ");
        if (phone.isEmpty()) phone = null;
        String email = readString(in, "Email (optional): ");
        if (email.isEmpty()) email = null;
        Date dob = readOptionalDate(in, "Date of birth");
        String emergency = readString(in, "Emergency contact (optional): ");
        if (emergency.isEmpty()) emergency = null;
        Integer tierId = readOptionalInt(in, "Tier ID");

        String sql = "INSERT INTO Member " +
                "(member_id, name, phone, email, date_of_birth, emergency_contact, tier_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, name);
            if (phone == null) ps.setNull(3, Types.VARCHAR);
            else ps.setString(3, phone);
            if (email == null) ps.setNull(4, Types.VARCHAR);
            else ps.setString(4, email);
            if (dob == null) ps.setNull(5, Types.DATE);
            else ps.setDate(5, dob);
            if (emergency == null) ps.setNull(6, Types.VARCHAR);
            else ps.setString(6, emergency);
            if (tierId == null) ps.setNull(7, Types.INTEGER);
            else ps.setInt(7, tierId);
            ps.executeUpdate();
        }
        System.out.println("Member added with ID " + id);
    }

    /*---------------------------------------------------------------------
    |  Method updateMember(conn, in)
    |
    |  Purpose:  Updates an existing member in the member table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void updateMember(Connection conn, Scanner in) throws SQLException {
        int id = readInt(in, "Member ID to update: ");
        String phone = readString(in, "New phone (blank to keep): ");
        String email = readString(in, "New email (blank to keep): ");
        Integer tierId = readOptionalInt(in, "New tier ID");

        String sql = "UPDATE Member SET " +
                "phone = NVL(?, phone), " +
                "email = NVL(?, email), " +
                "tier_id = NVL(?, tier_id) " +
                "WHERE member_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (phone.isEmpty()) ps.setNull(1, Types.VARCHAR);
            else ps.setString(1, phone);
            if (email.isEmpty()) ps.setNull(2, Types.VARCHAR);
            else ps.setString(2, email);
            if (tierId == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, tierId);
            ps.setInt(4, id);
            int rows = ps.executeUpdate();
            if (rows == 0) System.out.println("No such member.");
            else System.out.println("Member updated.");
        }
    }

    /*---------------------------------------------------------------------
    |  Method deleteMember(conn, in)
    |
    |  Purpose:  Deletes an existing member in the member table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void deleteMember(Connection conn, Scanner in) throws SQLException {
        int id = readInt(in, "Member ID to delete: ");

        String q1 = "SELECT COUNT(*) FROM Reservation " +
                "WHERE member_id = ? AND status IN ('BOOKED','IN_PROGRESS')";
        String q2 = "SELECT COUNT(*) FROM Adoption_Application " +
                "WHERE member_id = ? AND status = 'PENDING'";
        String q3 = "SELECT COUNT(*) FROM Customer_Order " +
                "WHERE member_id = ? AND payment_status <> 'PAID'";

        try (PreparedStatement ps1 = conn.prepareStatement(q1);
             PreparedStatement ps2 = conn.prepareStatement(q2);
             PreparedStatement ps3 = conn.prepareStatement(q3)) {

            ps1.setInt(1, id);
            ps2.setInt(1, id);
            ps3.setInt(1, id);
            try (ResultSet r1 = ps1.executeQuery();
                 ResultSet r2 = ps2.executeQuery();
                 ResultSet r3 = ps3.executeQuery()) {
                r1.next();
                r2.next();
                r3.next();
                if (r1.getInt(1) > 0 || r2.getInt(1) > 0 || r3.getInt(1) > 0) {
                    System.out.println("Cannot delete member: active reservations, " +
                            "pending applications, or unpaid orders exist.");
                    return;
                }
            }
        }

        String del = "DELETE FROM Member WHERE member_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(del)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) System.out.println("No such member.");
            else System.out.println("Member and related records deleted.");
        }
    }

    /*---------------------------------------------------------------------
    |  Method listMembers(conn)
    |
    |  Purpose:  Lists the information about all members in the member table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database.
    |
    *-------------------------------------------------------------------*/

    private static void listMembers(Connection conn) throws SQLException {
        String sql = "SELECT member_id, name, phone, email, tier_id FROM Member ORDER BY member_id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("%d: %s, phone=%s, email=%s, tier=%s%n",
                        rs.getInt("member_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("tier_id"));
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method petMenu(conn, in)
    |
    |  Purpose:  Displays the options for interacting with pet table including adding,
    |            updating, deleting, and displaying information.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void petMenu(Connection conn, Scanner in) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Pets & Adoptions Menu ---");
            System.out.println("1. Add pet");
            System.out.println("2. Update pet");
            System.out.println("3. Delete pet");
            System.out.println("4. Add adoption application");
            System.out.println("5. Update adoption application");
            System.out.println("6. Delete/Withdraw adoption application");
            System.out.println("7. Record adoption");
            System.out.println("8. List pets");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String c = in.nextLine().trim();
            try {
                switch (c) {
                    case "1":
                        addPet(conn, in);
                        break;
                    case "2":
                        updatePet(conn, in);
                        break;
                    case "3":
                        deletePet(conn, in);
                        break;
                    case "4":
                        addAdoptionApplication(conn, in);
                        break;
                    case "5":
                        updateAdoptionApplication(conn, in);
                        break;
                    case "6":
                        deleteOrWithdrawAdoptionApplication(conn, in);
                        break;
                    case "7":
                        recordAdoption(conn, in);
                        break;
                    case "8":
                        listPets(conn);
                        break;
                    case "0":
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method addPet(conn, in)
    |
    |  Purpose:  Adds a new pet to the pet table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void addPet(Connection conn, Scanner in) throws SQLException {
        int id = nextId(conn, "Pet", "pet_id");
        String name = readString(in, "Pet name: ");
        String species = readString(in, "Species: ");
        String breed = readString(in, "Breed (optional): ");
        if (breed.isEmpty()) breed = null;
        Integer age = readOptionalInt(in, "Age in years");
        Date arrival = readDate(in, "Date of arrival");
        String temperament = readString(in, "Temperament (optional): ");
        if (temperament.isEmpty()) temperament = null;
        String special = readString(in, "Special needs (optional): ");
        if (special.isEmpty()) special = null;
        String status = readString(in,
                "Status (AVAILABLE, IN_CARE, AVAILABLE_FOR_ADOPTION, ADOPTED, DECEASED): ");
        Integer roomId = readOptionalInt(in, "Current room ID");

        String sql = "INSERT INTO Pet " +
                "(pet_id, name, species, breed, age, date_of_arrival, temperament," +
                " special_needs, status, current_room_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setString(3, species);
            if (breed == null) ps.setNull(4, Types.VARCHAR);
            else ps.setString(4, breed);
            if (age == null) ps.setNull(5, Types.INTEGER);
            else ps.setInt(5, age);
            ps.setDate(6, arrival);
            if (temperament == null) ps.setNull(7, Types.VARCHAR);
            else ps.setString(7, temperament);
            if (special == null) ps.setNull(8, Types.VARCHAR);
            else ps.setString(8, special);
            ps.setString(9, status);
            if (roomId == null) ps.setNull(10, Types.INTEGER);
            else ps.setInt(10, roomId);
            ps.executeUpdate();
        }
        System.out.println("Pet added with ID " + id);
    }

    /*---------------------------------------------------------------------
    |  Method updatePet(conn, in)
    |
    |  Purpose:  Updates an existing pet in the pet table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void updatePet(Connection conn, Scanner in) throws SQLException {
        int id = readInt(in, "Pet ID to update: ");
        String status = readString(in, "New status (blank to keep): ");
        String temperament = readString(in, "New temperament (blank to keep): ");
        Integer roomId = readOptionalInt(in, "New current room ID");

        String sql = "UPDATE Pet SET " +
                "status = NVL(?, status), " +
                "temperament = NVL(?, temperament), " +
                "current_room_id = NVL(?, current_room_id) " +
                "WHERE pet_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (status.isEmpty()) ps.setNull(1, Types.VARCHAR);
            else ps.setString(1, status);
            if (temperament.isEmpty()) ps.setNull(2, Types.VARCHAR);
            else ps.setString(2, temperament);
            if (roomId == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, roomId);
            ps.setInt(4, id);
            int rows = ps.executeUpdate();
            if (rows == 0) System.out.println("No such pet.");
            else System.out.println("Pet updated.");
        }
    }

    /*---------------------------------------------------------------------
    |  Method deletePet(conn, in)
    |
    |  Purpose:  Deletes an existing pet in the pet table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void deletePet(Connection conn, Scanner in) throws SQLException {
        int id = readInt(in, "Pet ID to delete: ");

        String qStatus = "SELECT status FROM Pet WHERE pet_id = ?";
        String status;
        try (PreparedStatement ps = conn.prepareStatement(qStatus)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No such pet.");
                    return;
                }
                status = rs.getString(1);
            }
        }

        if (!status.equals("ADOPTED") && !status.equals("DECEASED")) {
            System.out.println("Pet can be deleted only if ADOPTED or DECEASED.");
            return;
        }

        String qApps = "SELECT COUNT(*) FROM Adoption_Application " +
                "WHERE pet_id = ? AND status = 'PENDING'";
        String qHealth = "SELECT COUNT(*) FROM Health_Record " +
                "WHERE pet_id = ? AND status = 'ACTIVE' " +
                "AND (next_due_date IS NULL OR next_due_date >= TRUNC(SYSDATE))";
        String qFollow = "SELECT COUNT(*) FROM Adoption " +
                "WHERE pet_id = ? AND follow_up_schedule > TRUNC(SYSDATE)";

        try (PreparedStatement ps1 = conn.prepareStatement(qApps);
             PreparedStatement ps2 = conn.prepareStatement(qHealth);
             PreparedStatement ps3 = conn.prepareStatement(qFollow)) {
            ps1.setInt(1, id);
            ps2.setInt(1, id);
            ps3.setInt(1, id);
            try (ResultSet r1 = ps1.executeQuery();
                 ResultSet r2 = ps2.executeQuery();
                 ResultSet r3 = ps3.executeQuery()) {
                r1.next();
                r2.next();
                r3.next();
                if (r1.getInt(1) > 0 || r2.getInt(1) > 0 || r3.getInt(1) > 0) {
                    System.out.println("Cannot delete pet: pending applications, " +
                            "active health records, or future follow-ups exist.");
                    return;
                }
            }
        }

        String del = "DELETE FROM Pet WHERE pet_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(del)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) System.out.println("No such pet.");
            else System.out.println("Pet and related records deleted.");
        }
    }

    /*---------------------------------------------------------------------
    |  Method listPets(conn)
    |
    |  Purpose:  Lists the information about all pets in the pet table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database.
    |
    *-------------------------------------------------------------------*/

    private static void listPets(Connection conn) throws SQLException {
        String sql = "SELECT pet_id, name, species, status FROM Pet ORDER BY pet_id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("%d: %s (%s), status=%s%n",
                        rs.getInt("pet_id"),
                        rs.getString("name"),
                        rs.getString("species"),
                        rs.getString("status"));
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method addAdoptionApplication(conn, in)
    |
    |  Purpose:  Adds a new adoption application to the adoption application table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void addAdoptionApplication(Connection conn, Scanner in) throws SQLException {
        int appId = nextId(conn, "Adoption_Application", "application_id");
        int memberId = readInt(in, "Member ID: ");
        int petId = readInt(in, "Pet ID: ");
        int staffId = readInt(in, "Assigned adoption coordinator staff ID: ");

        String sql = "INSERT INTO Adoption_Application " +
                "(application_id, member_id, pet_id, submitted_date, status, reviewed_by) " +
                "VALUES (?, ?, ?, TRUNC(SYSDATE), 'PENDING', ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appId);
            ps.setInt(2, memberId);
            ps.setInt(3, petId);
            ps.setInt(4, staffId);
            ps.executeUpdate();
        }
        System.out.println("Adoption application created with ID " + appId);
    }

    /*---------------------------------------------------------------------
    |  Method updateAdoptionApplication(conn, in)
    |
    |  Purpose:  Updates an existing adoption application in the adoption application table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void updateAdoptionApplication(Connection conn, Scanner in) throws SQLException {
        int appId = readInt(in, "Application ID to update: ");
        String status = readString(in, "New status (PENDING, APPROVED, REJECTED, WITHDRAWN): ");
        String notes = readString(in, "Notes (optional): ");
        if (notes.isEmpty()) notes = null;

        String sql = "UPDATE Adoption_Application SET status = ?, review_date = TRUNC(SYSDATE), " +
                "notes = ? WHERE application_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (notes == null) ps.setNull(2, Types.VARCHAR);
            else ps.setString(2, notes);
            ps.setInt(3, appId);
            int rows = ps.executeUpdate();
            if (rows == 0) System.out.println("No such application.");
            else System.out.println("Application updated.");
        }
    }

    /*---------------------------------------------------------------------
    |  Method deleteAdoptionApplication(conn, in)
    |
    |  Purpose:  Deletes an existing adoption application in the adoption application table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void deleteOrWithdrawAdoptionApplication(Connection conn, Scanner in)
            throws SQLException {
        int appId = readInt(in, "Application ID: ");

        String q = "SELECT review_date, status FROM Adoption_Application WHERE application_id = ?";
        Date reviewDate;
        String status;
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, appId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No such application.");
                    return;
                }
                reviewDate = rs.getDate("review_date");
                status = rs.getString("status");
            }
        }

        if (reviewDate == null) {
            String confirm = readString(in,
                    "No review has begun. Delete application as 'submitted in error'? (y/n): ");
            if (confirm.equalsIgnoreCase("y")) {
                String del = "DELETE FROM Adoption_Application WHERE application_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(del)) {
                    ps.setInt(1, appId);
                    ps.executeUpdate();
                }
                System.out.println("Application deleted.");
            } else {
                System.out.println("Aborted.");
            }
        } else {
            if (!status.equals("WITHDRAWN")) {
                String upd = "UPDATE Adoption_Application SET status = 'WITHDRAWN' " +
                        "WHERE application_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(upd)) {
                    ps.setInt(1, appId);
                    ps.executeUpdate();
                }
                System.out.println("Application marked as WITHDRAWN.");
            } else {
                System.out.println("Application is already WITHDRAWN.");
            }
        }
    }

    
    /*---------------------------------------------------------------------
    |  Method recordAdoption(conn, in)
    |
    |  Purpose:  Creates entry in Adoption table if application is approved
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void recordAdoption(Connection conn, Scanner in) throws SQLException {
        int appId = readInt(in, "Approved application ID: ");

        String q = "SELECT member_id, pet_id, status FROM Adoption_Application " +
                "WHERE application_id = ?";
        int memberId, petId;
        String status;
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, appId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No such application.");
                    return;
                }
                memberId = rs.getInt("member_id");
                petId = rs.getInt("pet_id");
                status = rs.getString("status");
            }
        }

        if (!"APPROVED".equals(status)) {
            System.out.println("Application is not APPROVED.");
            return;
        }

        int adoptionId = nextId(conn, "Adoption", "adoption_id");
        double fee = Double.parseDouble(
                readString(in, "Adoption fee (e.g., 50.00): "));
        Date followUp = readOptionalDate(in, "Follow-up date");

        String ins = "INSERT INTO Adoption " +
                "(adoption_id, application_id, pet_id, member_id, adoption_date," +
                " adoption_fee, follow_up_schedule) " +
                "VALUES (?, ?, ?, ?, TRUNC(SYSDATE), ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(ins)) {
            ps.setInt(1, adoptionId);
            ps.setInt(2, appId);
            ps.setInt(3, petId);
            ps.setInt(4, memberId);
            ps.setDouble(5, fee);
            if (followUp == null) ps.setNull(6, Types.DATE);
            else ps.setDate(6, followUp);
            ps.executeUpdate();
        }

        String updPet = "UPDATE Pet SET status = 'ADOPTED' WHERE pet_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(updPet)) {
            ps.setInt(1, petId);
            ps.executeUpdate();
        }

        System.out.println("Adoption recorded with ID " + adoptionId);
    }

    /*---------------------------------------------------------------------
    |  Method reservationMenu(conn, in)
    |
    |  Purpose:  Displays the options for interacting with reservation table including adding,
    |            updating, deleting, and displaying information.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void reservationMenu(Connection conn, Scanner in) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Reservation Menu ---");
            System.out.println("1. Add reservation");
            System.out.println("2. Update status / check-out");
            System.out.println("3. Cancel reservation");
            System.out.println("4. List reservations");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String c = in.nextLine().trim();
            try {
                switch (c) {
                    case "1":
                        addReservation(conn, in);
                        break;
                    case "2":
                        updateReservationStatus(conn, in);
                        break;
                    case "3":
                        cancelReservation(conn, in);
                        break;
                    case "4":
                        listReservations(conn);
                        break;
                    case "0":
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method addReservation(conn, in)
    |
    |  Purpose:  Adds a new reservation to the reservation table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void addReservation(Connection conn, Scanner in) throws SQLException {
        int resId = nextId(conn, "Reservation", "reservation_id");
        int memberId = readInt(in, "Member ID: ");
        int roomId = readInt(in, "Room ID: ");
        Date date = readDate(in, "Reservation date");
        Timestamp start = readDateTime(in, "Start time");
        int duration = readInt(in, "Duration minutes (60-120): ");
        Integer tierId = readOptionalInt(in, "Tier ID at time of visit (blank to use member's current tier)");

        if (tierId == null) {
            String q = "SELECT tier_id FROM Member WHERE member_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(q)) {
                ps.setInt(1, memberId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int t = rs.getInt(1);
                        if (!rs.wasNull()) tierId = t;
                    }
                }
            }
        }

        int maxCap;
        String qCap = "SELECT max_capacity FROM Room WHERE room_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(qCap)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No such room.");
                    return;
                }
                maxCap = rs.getInt(1);
            }
        }

        String qOverlap =
                "SELECT COUNT(*) FROM Reservation " +
                        "WHERE room_id = ? " +
                        "AND reservation_date = ? " +
                        "AND status IN ('BOOKED','IN_PROGRESS') " +
                        "AND (start_time < ? + NUMTODSINTERVAL(?, 'MINUTE') " +
                        "AND start_time + NUMTODSINTERVAL(duration_minutes, 'MINUTE') > ?)";

        try (PreparedStatement ps = conn.prepareStatement(qOverlap)) {
            ps.setInt(1, roomId);
            ps.setDate(2, date);
            ps.setTimestamp(3, start);
            ps.setInt(4, duration);
            ps.setTimestamp(5, start);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int count = rs.getInt(1);
                if (count >= maxCap) {
                    System.out.println("Room is at capacity for that time.");
                    return;
                }
            }
        }

        String ins = "INSERT INTO Reservation " +
                "(reservation_id, member_id, room_id, reservation_date, start_time," +
                " duration_minutes, status, tier_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'BOOKED', ?)";
        try (PreparedStatement ps = conn.prepareStatement(ins)) {
            ps.setInt(1, resId);
            ps.setInt(2, memberId);
            ps.setInt(3, roomId);
            ps.setDate(4, date);
            ps.setTimestamp(5, start);
            ps.setInt(6, duration);
            if (tierId == null) ps.setNull(7, Types.INTEGER);
            else ps.setInt(7, tierId);
            ps.executeUpdate();
        }
        System.out.println("Reservation created with ID " + resId);
    }

    /*---------------------------------------------------------------------
    |  Method updateReservation(conn, in)
    |
    |  Purpose:  Updates an existing reservation in the reservation table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void updateReservationStatus(Connection conn, Scanner in) throws SQLException {
        int resId = readInt(in, "Reservation ID: ");
        String status = readString(in, "New status (BOOKED, IN_PROGRESS, COMPLETED, CANCELLED): ");
        String setCheckout = readString(in, "Set check-out time to now? (y/n): ");

        String sql = "UPDATE Reservation SET status = ?, " +
                "check_out_time = CASE WHEN ? = 'Y' THEN SYSTIMESTAMP ELSE check_out_time END " +
                "WHERE reservation_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, setCheckout.equalsIgnoreCase("y") ? "Y" : "N");
            ps.setInt(3, resId);
            int rows = ps.executeUpdate();
            if (rows == 0) System.out.println("No such reservation.");
            else System.out.println("Reservation updated.");
        }
    }

    /*---------------------------------------------------------------------
    |  Method cancelReservation(conn, in)
    |
    |  Purpose:  Deletes an existing reservation in the reservation table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void cancelReservation(Connection conn, Scanner in) throws SQLException {
        int resId = readInt(in, "Reservation ID to cancel: ");

        String q = "SELECT start_time FROM Reservation WHERE reservation_id = ?";
        Timestamp start;
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, resId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No such reservation.");
                    return;
                }
                start = rs.getTimestamp(1);
            }
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (!now.before(start)) {
            System.out.println("Cannot cancel past or ongoing reservation.");
            return;
        }

        String qOrders = "SELECT COUNT(*) FROM Customer_Order WHERE reservation_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(qOrders)) {
            ps.setInt(1, resId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    System.out.println("Cannot cancel: orders exist for this reservation.");
                    return;
                }
            }
        }

        String del = "DELETE FROM Reservation WHERE reservation_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(del)) {
            ps.setInt(1, resId);
            ps.executeUpdate();
        }
        System.out.println("Reservation cancelled and deleted.");
    }

    /*---------------------------------------------------------------------
    |  Method listReservations(conn)
    |
    |  Purpose:  Lists the information about all reservations in the reservation table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database.
    |
    *-------------------------------------------------------------------*/

    private static void listReservations(Connection conn) throws SQLException {
        String sql = "SELECT reservation_id, member_id, room_id, reservation_date, " +
                "start_time, status FROM Reservation ORDER BY reservation_id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("%d: member=%d room=%d date=%s status=%s%n",
                        rs.getInt("reservation_id"),
                        rs.getInt("member_id"),
                        rs.getInt("room_id"),
                        rs.getDate("reservation_date"),
                        rs.getString("status"));
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method orderMenu(conn, in)
    |
    |  Purpose:  Displays the options for interacting with order table including adding,
    |            updating, deleting, and displaying information.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void orderMenu(Connection conn, Scanner in) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Order Menu ---");
            System.out.println("1. Create order");
            System.out.println("2. Mark order paid");
            System.out.println("3. Delete order");
            System.out.println("4. List orders");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String c = in.nextLine().trim();
            try {
                switch (c) {
                    case "1":
                        createOrder(conn, in);
                        break;
                    case "2":
                        markOrderPaid(conn, in);
                        break;
                    case "3":
                        deleteOrder(conn, in);
                        break;
                    case "4":
                        listOrders(conn);
                        break;
                    case "0":
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method createOrder(conn, in)
    |
    |  Purpose:  Adds a new order to the order table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void createOrder(Connection conn, Scanner in) throws SQLException {
        int orderId = nextId(conn, "Customer_Order", "order_id");
        int memberId = readInt(in, "Member ID: ");
        Integer resId = readOptionalInt(in, "Reservation ID (optional)");

        String ins = "INSERT INTO Customer_Order " +
                "(order_id, member_id, reservation_id, order_time, total_price, payment_status) " +
                "VALUES (?, ?, ?, SYSTIMESTAMP, 0, 'UNPAID')";
        try (PreparedStatement ps = conn.prepareStatement(ins)) {
            ps.setInt(1, orderId);
            ps.setInt(2, memberId);
            if (resId == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, resId);
            ps.executeUpdate();
        }

        while (true) {
            Integer itemId = readOptionalInt(in, "Menu item ID to add (blank to finish): ");
            if (itemId == null) break;
            int qty = readInt(in, "Quantity: ");

            String qPrice = "SELECT base_price FROM Menu_Item WHERE item_id = ?";
            double unitPrice;
            try (PreparedStatement ps = conn.prepareStatement(qPrice)) {
                ps.setInt(1, itemId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("No such menu item.");
                        continue;
                    }
                    unitPrice = rs.getDouble(1);
                }
            }

            String insItem = "INSERT INTO Order_Item " +
                    "(order_id, item_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insItem)) {
                ps.setInt(1, orderId);
                ps.setInt(2, itemId);
                ps.setInt(3, qty);
                ps.setDouble(4, unitPrice);
                ps.executeUpdate();
            }
            System.out.println("Item added.");
        }

        finalizeOrderTotal(conn, orderId);
        System.out.println("Order created with ID " + orderId);
    }

    /*---------------------------------------------------------------------
    |  Method finalizeOrderTotal(conn, in)
    |
    |  Purpose:  Updates order totals to reflect discounts.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void finalizeOrderTotal(Connection conn, int orderId) throws SQLException {
        String qBase = "SELECT SUM(quantity * unit_price) " +
                "FROM Order_Item WHERE order_id = ?";
        double baseTotal = 0.0;
        try (PreparedStatement ps = conn.prepareStatement(qBase)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) baseTotal = rs.getDouble(1);
            }
        }

        String qTier =
                "SELECT mt.discount_rate " +
                        "FROM Customer_Order co " +
                        "JOIN Member m ON co.member_id = m.member_id " +
                        "JOIN Membership_Tier mt ON m.tier_id = mt.tier_id " +
                        "WHERE co.order_id = ?";
        Double discountRate = null;
        try (PreparedStatement ps = conn.prepareStatement(qTier)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) discountRate = rs.getDouble(1);
            }
        }

        double finalTotal = baseTotal;
        if (discountRate != null) {
            finalTotal = baseTotal * (1.0 - discountRate / 100.0);
        }

        String upd = "UPDATE Customer_Order SET total_price = ?, payment_status = 'UNPAID' " +
                "WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(upd)) {
            ps.setDouble(1, finalTotal);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    /*---------------------------------------------------------------------
    |  Method markOrderPaid(conn, in)
    |
    |  Purpose:  Updates an existing order in the order table to be set to paid.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/
    
    private static void markOrderPaid(Connection conn, Scanner in) throws SQLException {
        int orderId = readInt(in, "Order ID to mark PAID: ");
        String sql = "UPDATE Customer_Order SET payment_status = 'PAID' WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            int rows = ps.executeUpdate();
            if (rows == 0) System.out.println("No such order.");
            else System.out.println("Order marked as PAID.");
        }
    }

    /*---------------------------------------------------------------------
    |  Method deleteOrder(conn, in)
    |
    |  Purpose:  Deletes an existing order in the order table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void deleteOrder(Connection conn, Scanner in) throws SQLException {
        int orderId = readInt(in, "Order ID to delete: ");
        String q = "SELECT payment_status FROM Customer_Order WHERE order_id = ?";
        String status;
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No such order.");
                    return;
                }
                status = rs.getString(1);
            }
        }

        if (!"UNPAID".equals(status)) {
            System.out.println("Order can be deleted only if UNPAID (created in error).");
            return;
        }

        String delOrder = "DELETE FROM Customer_Order WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(delOrder)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
        System.out.println("Order deleted.");
    }

    /*---------------------------------------------------------------------
    |  Method listOrders(conn)
    |
    |  Purpose:  Lists the information about all orders in the order table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database.
    |
    *-------------------------------------------------------------------*/

    private static void listOrders(Connection conn) throws SQLException {
        String sql = "SELECT order_id, member_id, total_price, payment_status " +
                "FROM Customer_Order ORDER BY order_id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("%d: member=%d total=%.2f status=%s%n",
                        rs.getInt("order_id"),
                        rs.getInt("member_id"),
                        rs.getDouble("total_price"),
                        rs.getString("payment_status"));
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method eventMenu(conn, in)
    |
    |  Purpose:  Displays the options for interacting with event table including adding,
    |            updating, deleting, and displaying information.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void eventMenu(Connection conn, Scanner in) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Events Menu ---");
            System.out.println("1. Add event");
            System.out.println("2. Register member for event");
            System.out.println("3. Update attendance status");
            System.out.println("4. Update registration payment status");
            System.out.println("5. Delete event booking (if refunded & in advance)");
            System.out.println("6. List events");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String c = in.nextLine().trim();
            try {
                switch (c) {
                    case "1":
                        addEvent(conn, in);
                        break;
                    case "2":
                        registerForEvent(conn, in);
                        break;
                    case "3":
                        updateEventAttendance(conn, in);
                        break;
                    case "4":
                        updateEventPaymentStatus(conn, in);
                        break;
                    case "5":
                        deleteEventRegistration(conn, in);
                        break;
                    case "6":
                        listEvents(conn);
                        break;
                    case "0":
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method addEvent(conn, in)
    |
    |  Purpose:  Adds a new event to the event table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void addEvent(Connection conn, Scanner in) throws SQLException {
        int id = nextId(conn, "Event", "event_id");
        String title = readString(in, "Title: ");
        String desc = readString(in, "Description (optional): ");
        if (desc.isEmpty()) desc = null;
        int roomId = readInt(in, "Room ID: ");
        Date date = readDate(in, "Event date");
        Timestamp start = readDateTime(in, "Start time");
        Timestamp end = readDateTime(in, "End time");
        int maxAtt = readInt(in, "Max attendees: ");
        String type = readString(in, "Event type (optional): ");
        if (type.isEmpty()) type = null;
        Integer staffId = readOptionalInt(in, "Coordinating staff ID");

        String sql = "INSERT INTO Event " +
                "(event_id, title, description, room_id, event_date, start_time, " +
                "end_time, max_attendees, event_type, staff_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, title);
            if (desc == null) ps.setNull(3, Types.VARCHAR);
            else ps.setString(3, desc);
            ps.setInt(4, roomId);
            ps.setDate(5, date);
            ps.setTimestamp(6, start);
            ps.setTimestamp(7, end);
            ps.setInt(8, maxAtt);
            if (type == null) ps.setNull(9, Types.VARCHAR);
            else ps.setString(9, type);
            if (staffId == null) ps.setNull(10, Types.INTEGER);
            else ps.setInt(10, staffId);
            ps.executeUpdate();
        }
        System.out.println("Event added with ID " + id);
    }

    /*---------------------------------------------------------------------
    |  Method registerForEvent(conn, in)
    |
    |  Purpose:  Allows user to register member for specific event.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/


    private static void registerForEvent(Connection conn, Scanner in) throws SQLException {
        int memberId = readInt(in, "Member ID: ");
        int eventId = readInt(in, "Event ID: ");

        String qEvent = "SELECT max_attendees FROM Event WHERE event_id = ?";
        int maxAtt;
        try (PreparedStatement ps = conn.prepareStatement(qEvent)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No such event.");
                    return;
                }
                maxAtt = rs.getInt(1);
            }
        }

        String qCount = "SELECT COUNT(*) FROM Event_Registration WHERE event_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(qCount)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) >= maxAtt) {
                    System.out.println("Event is at capacity.");
                    return;
                }
            }
        }

        String ins = "INSERT INTO Event_Registration " +
                "(member_id, event_id, registration_date, attendance_status, payment_status) " +
                "VALUES (?, ?, TRUNC(SYSDATE), 'REGISTERED', 'UNPAID')";
        try (PreparedStatement ps = conn.prepareStatement(ins)) {
            ps.setInt(1, memberId);
            ps.setInt(2, eventId);
            ps.executeUpdate();
        }
        System.out.println("Member registered for event.");
    }

    /*---------------------------------------------------------------------
    |  Method updateEventAttendance(conn, in)
    |
    |  Purpose:  Updates event registration status.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/


    private static void updateEventAttendance(Connection conn, Scanner in) throws SQLException {
        int memberId = readInt(in, "Member ID: ");
        int eventId = readInt(in, "Event ID: ");
        String status = readString(in,
                "New attendance status (REGISTERED, ATTENDED, NO_SHOW, CANCELLED): ");

        String sql = "UPDATE Event_Registration SET attendance_status = ? " +
                "WHERE member_id = ? AND event_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, memberId);
            ps.setInt(3, eventId);
            int rows = ps.executeUpdate();
            if (rows == 0) System.out.println("No such registration.");
            else System.out.println("Attendance status updated.");
        }
    }

    /*---------------------------------------------------------------------
    |  Method updateEventPaymentStatus(conn, in)
    |
    |  Purpose:  Updates an existing event's payment status.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/


    private static void updateEventPaymentStatus(Connection conn, Scanner in) throws SQLException {
        int memberId = readInt(in, "Member ID: ");
        int eventId = readInt(in, "Event ID: ");
        String status = readString(in, "New payment status (UNPAID, PAID, REFUNDED): ");

        String sql = "UPDATE Event_Registration SET payment_status = ? " +
                "WHERE member_id = ? AND event_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, memberId);
            ps.setInt(3, eventId);
            int rows = ps.executeUpdate();
            if (rows == 0) System.out.println("No such registration.");
            else System.out.println("Payment status updated.");
        }
    }

    /*---------------------------------------------------------------------
    |  Method deleteEventRegistration(conn, in)
    |
    |  Purpose:  Deletes an existing event registration in the event registration table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void deleteEventRegistration(Connection conn, Scanner in) throws SQLException {
        int memberId = readInt(in, "Member ID: ");
        int eventId = readInt(in, "Event ID: ");

        String q = "SELECT e.start_time, r.payment_status " +
                "FROM Event e JOIN Event_Registration r " +
                "ON e.event_id = r.event_id " +
                "WHERE r.member_id = ? AND r.event_id = ?";
        Timestamp start;
        String paymentStatus;
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, memberId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No such registration.");
                    return;
                }
                start = rs.getTimestamp("start_time");
                paymentStatus = rs.getString("payment_status");
            }
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (now.before(start) && "REFUNDED".equals(paymentStatus)) {
            String del = "DELETE FROM Event_Registration WHERE member_id = ? AND event_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(del)) {
                ps.setInt(1, memberId);
                ps.setInt(2, eventId);
                ps.executeUpdate();
            }
            System.out.println("Booking deleted (refunded & in advance).");
        } else {
            String upd = "UPDATE Event_Registration SET attendance_status = 'CANCELLED' " +
                    "WHERE member_id = ? AND event_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(upd)) {
                ps.setInt(1, memberId);
                ps.setInt(2, eventId);
                ps.executeUpdate();
            }
            System.out.println("Booking not deleted; marked as CANCELLED instead.");
        }
    }

    /*---------------------------------------------------------------------
    |  Method listEvents(conn)
    |
    |  Purpose:  Lists the information about all events in the event table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database.
    |
    *-------------------------------------------------------------------*/

    private static void listEvents(Connection conn) throws SQLException {
        String sql = "SELECT event_id, title, event_date, start_time, max_attendees " +
                "FROM Event ORDER BY event_date, start_time";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("%d: %s on %s (max %d)%n",
                        rs.getInt("event_id"),
                        rs.getString("title"),
                        rs.getDate("event_date"),
                        rs.getInt("max_attendees"));
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method healthMenu(conn, in)
    |
    |  Purpose:  Displays the options for interacting with health table including adding,
    |            updating, deleting, and displaying information.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void healthMenu(Connection conn, Scanner in) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Health Records Menu ---");
            System.out.println("1. Add health record");
            System.out.println("2. Update health record");
            System.out.println("3. List health records for pet");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String c = in.nextLine().trim();
            try {
                switch (c) {
                    case "1":
                        addHealthRecord(conn, in);
                        break;
                    case "2":
                        updateHealthRecord(conn, in);
                        break;
                    case "3":
                        listHealthRecordsForPet(conn, in);
                        break;
                    case "0":
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method addHealthRecord(conn, in)
    |
    |  Purpose:  Adds a new health to the health table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void addHealthRecord(Connection conn, Scanner in) throws SQLException {
        int id = nextId(conn, "Health_Record", "record_id");
        int petId = readInt(in, "Pet ID: ");
        Integer staffId = readOptionalInt(in, "Staff ID (vet/handler, optional)");
        Date recordDate = readDate(in, "Record date");
        String type = readString(in, "Record type (vaccination, checkup, etc.): ");
        String notes = readString(in, "Notes (optional): ");
        if (notes.isEmpty()) notes = null;
        Date nextDue = readOptionalDate(in, "Next due date");

        String sql = "INSERT INTO Health_Record " +
                "(record_id, pet_id, staff_id, record_date, record_type, " +
                "notes, next_due_date, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, petId);
            if (staffId == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, staffId);
            ps.setDate(4, recordDate);
            ps.setString(5, type);
            if (notes == null) ps.setNull(6, Types.VARCHAR);
            else ps.setString(6, notes);
            if (nextDue == null) ps.setNull(7, Types.DATE);
            else ps.setDate(7, nextDue);
            ps.executeUpdate();
        }
        System.out.println("Health record added with ID " + id);
    }

    /*---------------------------------------------------------------------
    |  Method updateHealthRecord(conn, in)
    |
    |  Purpose:  Updates an existing health in the health table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void updateHealthRecord(Connection conn, Scanner in) throws SQLException {
        int id = readInt(in, "Health record ID to update: ");
        String notes = readString(in, "New notes (blank to keep): ");
        Date nextDue = readOptionalDate(in, "New next due date");
        String status = readString(in, "New status (ACTIVE, VOID, CORRECTED; blank to keep): ");

        String sql = "UPDATE Health_Record SET " +
                "notes = NVL(?, notes), " +
                "next_due_date = NVL(?, next_due_date), " +
                "status = NVL(?, status) " +
                "WHERE record_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (notes.isEmpty()) ps.setNull(1, Types.VARCHAR);
            else ps.setString(1, notes);
            if (nextDue == null) ps.setNull(2, Types.DATE);
            else ps.setDate(2, nextDue);
            if (status.isEmpty()) ps.setNull(3, Types.VARCHAR);
            else ps.setString(3, status);
            ps.setInt(4, id);
            int rows = ps.executeUpdate();
            if (rows == 0) System.out.println("No such health record.");
            else System.out.println("Health record updated.");
        }
    }

    /*---------------------------------------------------------------------
    |  Method listHealthRecordsForPet(conn)
    |
    |  Purpose:  Lists the information about all health records in the health table.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database.
    |
    *-------------------------------------------------------------------*/

    private static void listHealthRecordsForPet(Connection conn, Scanner in) throws SQLException {
        int petId = readInt(in, "Pet ID: ");
        String sql = "SELECT record_id, record_date, record_type, status, next_due_date " +
                "FROM Health_Record WHERE pet_id = ? " +
                "ORDER BY record_date";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, petId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("Record %d on %s: type=%s status=%s next_due=%s%n",
                            rs.getInt("record_id"),
                            rs.getDate("record_date"),
                            rs.getString("record_type"),
                            rs.getString("status"),
                            rs.getDate("next_due_date"));
                }
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method reportMenu(conn, in)
    |
    |  Purpose:  Displays the options for different reports so that queries may
    |            happen.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void reportsMenu(Connection conn, Scanner in) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Reports Menu ---");
            System.out.println("1. Adoption applications for a pet");
            System.out.println("2. Visit history for a member");
            System.out.println("3. Upcoming events with available capacity");
            System.out.println("4. Top members by total spend (custom query)");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String c = in.nextLine().trim();
            try {
                switch (c) {
                    case "1":
                        reportAdoptionApplicationsForPet(conn, in);
                        break;
                    case "2":
                        reportVisitHistory(conn, in);
                        break;
                    case "3":
                        reportUpcomingEventsWithCapacity(conn);
                        break;
                    case "4":
                        reportTopMembers(conn, in);
                        break;
                    case "0":
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method reportAdoptionApplicationsForPet(conn, in)
    |
    |  Purpose:  Displays all applications for a specific pet.
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void reportAdoptionApplicationsForPet(Connection conn, Scanner in)
            throws SQLException {
        int petId = readInt(in, "Pet ID: ");
        String sql = "SELECT a.application_id, m.name AS member_name, " +
                "a.submitted_date, a.status, s.name AS coordinator " +
                "FROM Adoption_Application a " +
                "JOIN Member m ON a.member_id = m.member_id " +
                "LEFT JOIN Staff s ON a.reviewed_by = s.staff_id " +
                "WHERE a.pet_id = ? " +
                "ORDER BY a.submitted_date";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, petId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("App %d by %s on %s: status=%s, coordinator=%s%n",
                            rs.getInt("application_id"),
                            rs.getString("member_name"),
                            rs.getDate("submitted_date"),
                            rs.getString("status"),
                            rs.getString("coordinator"));
                }
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method reportVisitHistory(conn, in)
    |
    |  Purpose:  Reports the visitation history for a given member
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void reportVisitHistory(Connection conn, Scanner in)
            throws SQLException {
        int memberId = readInt(in, "Member ID: ");
        String sql = "SELECT r.reservation_id, r.reservation_date, r.start_time, " +
                "rm.room_name, mt.tier_name, " +
                "NVL(SUM(co.total_price),0) AS total_spent " +
                "FROM Reservation r " +
                "JOIN Room rm ON r.room_id = rm.room_id " +
                "LEFT JOIN Membership_Tier mt ON r.tier_id = mt.tier_id " +
                "LEFT JOIN Customer_Order co ON co.reservation_id = r.reservation_id " +
                "WHERE r.member_id = ? " +
                "GROUP BY r.reservation_id, r.reservation_date, r.start_time, " +
                "rm.room_name, mt.tier_name " +
                "ORDER BY r.reservation_date, r.start_time";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("Res %d on %s in room %s tier=%s total_spent=%.2f%n",
                            rs.getInt("reservation_id"),
                            rs.getDate("reservation_date"),
                            rs.getString("room_name"),
                            rs.getString("tier_name"),
                            rs.getDouble("total_spent"));
                }
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method reportUpcomingEventsWithCapacity(conn)
    |
    |  Purpose:  Reports all upcoming events along with their max capacity
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |
    *-------------------------------------------------------------------*/

    private static void reportUpcomingEventsWithCapacity(Connection conn)
            throws SQLException {
        String sql = "SELECT e.event_id, e.title, e.event_date, e.start_time, " +
                "r.room_name, NVL(COUNT(er.member_id),0) AS registered, " +
                "e.max_attendees, s.name AS coordinator " +
                "FROM Event e " +
                "JOIN Room r ON e.room_id = r.room_id " +
                "LEFT JOIN Event_Registration er " +
                "ON e.event_id = er.event_id AND er.attendance_status <> 'CANCELLED' " +
                "LEFT JOIN Staff s ON e.staff_id = s.staff_id " +
                "WHERE e.event_date >= TRUNC(SYSDATE) " +
                "GROUP BY e.event_id, e.title, e.event_date, e.start_time, " +
                "r.room_name, e.max_attendees, s.name " +
                "HAVING NVL(COUNT(er.member_id),0) < e.max_attendees " +
                "ORDER BY e.event_date, e.start_time";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("Event %d: %s on %s in %s (%d/%d registered, coord=%s)%n",
                        rs.getInt("event_id"),
                        rs.getString("title"),
                        rs.getDate("event_date"),
                        rs.getString("room_name"),
                        rs.getInt("registered"),
                        rs.getInt("max_attendees"),
                        rs.getString("coordinator"));
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method reportTopMembers(conn, in)
    |
    |  Purpose:  Reports the top members that spent at least N amount of money
    |
    |  Pre-condition:  conn is established correctly. Scanner is established correctly.
    |
    |  Post-condition: No exception is thrown when getting the results of the query
    |
    |  Parameters:
    |      conn     -- The connection object representing the connection to the database
    |      in       -- Scanner representing keyboard input.
    |
    *-------------------------------------------------------------------*/

    private static void reportTopMembers(Connection conn, Scanner in)
            throws SQLException {
        System.out.print("Minimum total spend to include (e.g. 50.0): ");
        double minSpend;
        try {
            minSpend = Double.parseDouble(in.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount, using 0.");
            minSpend = 0.0;
        }

        String sql = "SELECT m.member_id, m.name, mt.tier_name, " +
                "COUNT(DISTINCT r.reservation_id) AS visits, " +
                "NVL(SUM(co.total_price),0) AS total_spent " +
                "FROM Member m " +
                "LEFT JOIN Membership_Tier mt ON m.tier_id = mt.tier_id " +
                "LEFT JOIN Reservation r ON r.member_id = m.member_id " +
                "LEFT JOIN Customer_Order co ON co.member_id = m.member_id " +
                "GROUP BY m.member_id, m.name, mt.tier_name " +
                "HAVING NVL(SUM(co.total_price),0) >= ? " +
                "ORDER BY total_spent DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, minSpend);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("Member %d: %s tier=%s visits=%d total_spent=%.2f%n",
                            rs.getInt("member_id"),
                            rs.getString("name"),
                            rs.getString("tier_name"),
                            rs.getInt("visits"),
                            rs.getDouble("total_spent"));
                }
            }
        }
    }
}

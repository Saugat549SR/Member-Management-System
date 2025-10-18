package com.gym.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Scanner;

import com.gym.model.Member;
import com.gym.model.Performance;
import com.gym.model.PersonalTrainingMember;
import com.gym.model.PremiumMember;
import com.gym.model.RegularMember;
import com.gym.repository.MemberRepository;
import com.gym.storage.CsvStorage;

public class GymApp {

    // Scanner and repository
    private final Scanner in = new Scanner(System.in);
    private final MemberRepository repo = new MemberRepository();
    private final CsvStorage storage = new CsvStorage();

    // File paths for data storage
    private final String DATA_DIR     = "data";
    private final String MEMBERS_FILE = DATA_DIR + "/members.csv";
    private final String PERF_FILE    = DATA_DIR + "/performances.csv";

    public static void main(String[] args) {
        new GymApp().run();
    }

    private void run() {
        ensureDataDir(); // Create data folder if missing
        while (true) {
            printMenu();
            int choice = Input.readInt(in, "Please choose an option: ");
            switch (choice) {
                case 1 -> optionLoadRecordsIntoRepository();
                case 2 -> optionAddMemberAndSave();
                case 3 -> optionUpdateMemberAndSave();     // includes recording performance + edit details
                case 4 -> optionDeleteMemberAndSave();
                case 5 -> optionLoadFileAndQueryOnly();
                case 6 -> { System.out.println("Goodbye!"); return; }
                default -> System.out.println("Invalid choice.");
            }
            System.out.println();
        }
    }

    private void printMenu() { // Displays main menu
        System.out.println("===== Member Management System =====");
        System.out.println("1. Load records in a new file to access latest records");
        System.out.println("2. Add new members and save to a new file");
        System.out.println("3. Update member information and save to a new file");
        System.out.println("4. Delete member and save to a new file");
        System.out.println("5. Load new file to view / query member details");
        System.out.println("6. Exit");
    }

    // Load CSV into repository
    private void optionLoadRecordsIntoRepository() {
        String membersPath = Input.readLine(in,
                "Path to MEMBERS CSV (Enter for default " + MEMBERS_FILE + "): ");
        if (membersPath.isBlank()) membersPath = MEMBERS_FILE;

        String perfPath = Input.readLine(in,
                "Path to PERFORMANCES CSV (Enter for default " + PERF_FILE + ", or leave empty to skip): ");
        boolean usePerf = true;
        if (perfPath.isBlank()) {
            boolean useDefault = Input.readYesNo(in, "Use default performances file? (y/n): ");
            if (useDefault) perfPath = PERF_FILE;
            else usePerf = false;
        }

        try {
            List<Member> members = storage.loadMembers(membersPath);
            if (usePerf) {
                List<Performance> perfs = storage.loadPerformances(perfPath);
                storage.attachPerformancesToMembers(members, perfs);
            }
            repo.replaceAllMembers(members);
            System.out.println("Loaded into repository: " + repo.getAllMembers().size() + " members.");
        } catch (IOException e) {
            System.out.println("Failed to load: " + e.getMessage());
        }
    }

    // Add a member and save to fixed CSVs
    private void optionAddMemberAndSave() {
        Member m = createMemberInteractively();
        if (m == null) return;

        boolean ok = repo.addMember(m);
        if (!ok) {
            System.out.println("Could not add member (duplicate ID or null).");
            return;
        }
        saveSnapshot();  // overwrites data/members.csv and data/performances.csv
    }

    // Update / Convert / Record performance / Edit personal details
    private void optionUpdateMemberAndSave() {
        String id = Input.readString(in, "Enter Member ID to update: ");
        Member old = repo.findMemberById(id);
        if (old == null) {
            System.out.println("Member not found.");
            return;
        }

        System.out.println("Current: " + old.getSummary());
        System.out.println("-- Update --");
        System.out.println("1. Change base fee (keep same type)");
        System.out.println("2. Convert to Regular");
        System.out.println("3. Convert to Personal Training");
        System.out.println("4. Convert to Premium");
        System.out.println("5. Record monthly performance");
        System.out.println("6. Cancel");
        System.out.println("7. Edit personal details (name, age, join date)"); // NEW

        int choice = Input.readInt(in, "Choose: ");
        switch (choice) {
            case 1 -> {
                double newBaseFee = Input.readDouble(in,
                        "New base fee (current $" + String.format("%.2f", old.getBaseFee()) + "): ");
                convertKeepTypeWithNewBase(old, newBaseFee);
                saveSnapshot();
                System.out.println("Updated and saved.");
            }
            case 2 -> {
                double base = Input.readDouble(in, "Base fee: ");
                Member updated = new RegularMember(
                        old.getMemberId(), old.getFirstName(), old.getLastName(),
                        old.getAge(), old.getJoinDate(), base);
                copyPerfAndReplace(old, updated);
                saveSnapshot();
                System.out.println("Converted to Regular and saved.");
            }
            case 3 -> {
                double base = Input.readDouble(in, "Base fee: ");
                int sessions = Input.readInt(in, "Sessions per month: ");
                double per = Input.readDouble(in, "Fee per session: ");
                Member updated = new PersonalTrainingMember(
                        old.getMemberId(), old.getFirstName(), old.getLastName(),
                        old.getAge(), old.getJoinDate(), base, sessions, per);
                copyPerfAndReplace(old, updated);
                saveSnapshot();
                System.out.println("Converted to PT and saved.");
            }
            case 4 -> {
                double base = Input.readDouble(in, "Base fee: ");
                boolean spa = Input.readYesNo(in, "Spa access (y/n)? ");
                double premium = spa ? Input.readDouble(in, "Premium service fee: ") : 0.0;
                Member updated = new PremiumMember(
                        old.getMemberId(), old.getFirstName(), old.getLastName(),
                        old.getAge(), old.getJoinDate(), base, spa, premium);
                copyPerfAndReplace(old, updated);
                saveSnapshot();
                System.out.println("Converted to Premium and saved.");
            }
            case 5 -> {
                optionRecordPerformance(old); // handles its own save
            }
            case 6 -> System.out.println("Cancelled.");
            case 7 -> { // NEW
                editPersonalDetails(old);
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    private void convertKeepTypeWithNewBase(Member old, double newBase) {
        Member updated;
        if (old instanceof RegularMember) {
            updated = new RegularMember(old.getMemberId(), old.getFirstName(), old.getLastName(),
                    old.getAge(), old.getJoinDate(), newBase);
        } else if (old instanceof PersonalTrainingMember pt) {
            updated = new PersonalTrainingMember(old.getMemberId(), old.getFirstName(), old.getLastName(),
                    old.getAge(), old.getJoinDate(), newBase, pt.getSessionsPerMonth(), pt.getFeePerSession());
        } else if (old instanceof PremiumMember pm) {
            updated = new PremiumMember(old.getMemberId(), old.getFirstName(), old.getLastName(),
                    old.getAge(), old.getJoinDate(), newBase, pm.hasSpaAccess(), pm.getPremiumServiceFee());
        } else {
            updated = new RegularMember(old.getMemberId(), old.getFirstName(), old.getLastName(),
                    old.getAge(), old.getJoinDate(), newBase);
        }
        copyPerfAndReplace(old, updated);
    }

    private void copyPerfAndReplace(Member old, Member updated) {
        for (Performance p : old.getPerformanceHistory()) {
            updated.addOrReplacePerformance(p);
        }
        // replace in repository
        if (!repo.replaceMember(old.getMemberId(), updated)) {
            repo.deleteMember(old.getMemberId());
            repo.addMember(updated);
        }
    }

    private void optionDeleteMemberAndSave() {
        if (repo.isEmpty()) {
            System.out.println("Repository is empty. Load or add members first.");
            return;
        }
        String id = Input.readLine(in, "Enter Member ID to delete: ");
        boolean ok = repo.deleteMember(id);
        System.out.println(ok ? "Deleted." : "Member not found.");
        if (ok) saveSnapshot();
    }

    // Viewer for a CSV without touching repo
    private void optionLoadFileAndQueryOnly() {
        String membersPath = Input.readLine(in,
                "Path to MEMBERS CSV to view (Enter for default " + MEMBERS_FILE + "): ");
        if (membersPath.isBlank()) membersPath = MEMBERS_FILE;

        // Ask for performances file (default to PERF_FILE)
        String perfPath = Input.readLine(in,
                "Path to PERFORMANCES CSV to view (Enter for default " + PERF_FILE + ", or leave empty to skip): ");

        try {
            List<Member> list = storage.loadMembers(membersPath);

            // attach performances if a path is provided (or use default)
            if (perfPath.isBlank()) {
                boolean useDefault = Input.readYesNo(in, "Use default performances file? (y/n): ");
                if (useDefault) perfPath = PERF_FILE;
            }
            if (!perfPath.isBlank()) {
                List<Performance> perfs = storage.loadPerformances(perfPath);
                storage.attachPerformancesToMembers(list, perfs);
            }

            if (list.isEmpty()) {
                System.out.println("No members found in file.");
                return;
            }

            while (true) {
                System.out.println("\n-- Viewer --");
                System.out.println("1. List all");
                System.out.println("2. Find by ID");
                System.out.println("3. Search by name");
                System.out.println("4. Back");
                int c = Input.readInt(in, "Choose: ");
                if (c == 1) {
                    for (Member m : list) System.out.println(m.getSummary());
                } else if (c == 2) {
                    String id = Input.readLine(in, "ID: ");
                    Member m = list.stream()
                            .filter(mm -> mm.getMemberId().equalsIgnoreCase(id))
                            .findFirst().orElse(null);
                    if (m == null) {
                        System.out.println("Not found.");
                    } else {
                        System.out.println(m.getSummary());
                        boolean calc = Input.readYesNo(in, "Calculate fee for a month? (y/n): ");
                        if (calc) {
                            YearMonth ym = readYearMonth("Month (YYYY-MM): ");
                            if (ym == null) {
                                System.out.println("Invalid month.");
                            } else {
                                printFeeBreakdown(m, ym);
                            }
                        }
                    }
                } else if (c == 3) {
                    String name = Input.readLine(in, "Name contains: ").toLowerCase();
                    list.stream()
                        .filter(m -> (m.getFirstName() + " " + m.getLastName())
                                .toLowerCase().contains(name))
                        .forEach(m -> System.out.println(m.getSummary()));
                } else if (c == 4) {
                    break;
                } else {
                    System.out.println("Invalid.");
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to load: " + e.getMessage());
        }
    }

    // Record monthly performance (and save)
    private void optionRecordPerformance(Member m) {
        System.out.println("Recording performance for: " + m.getSummary());

        YearMonth ym = readYearMonth("Month (YYYY-MM): ");
        if (ym == null) {
            System.out.println("Invalid month.");
            return;
        }
        boolean achieved = Input.readYesNo(in, "Goal achieved (y/n)? ");

        int rating;
        while (true) {
            rating = Input.readInt(in, "Rating (1-5): ");
            if (rating >= 1 && rating <= 5) break;
            System.out.println("Enter a number between 1 and 5.");
        }

        String notes = Input.readLine(in, "Notes (optional): ");

        Performance p = new Performance(m.getMemberId(), ym, achieved, rating, notes);
        m.addOrReplacePerformance(p);
        saveSnapshot();
        System.out.println("Performance saved.");
    }

    // Helpers

    private Member createMemberInteractively() {
        System.out.println("Choose type: 1=Regular, 2=Personal Training, 3=Premium");
        int t = Input.readInt(in, "Type: ");

        String first = Input.readLine(in, "First name: ");
        String last  = Input.readLine(in, "Last name: ");
        int age      = Input.readInt(in, "Age: ");
        LocalDate join = Input.readLocalDate(in, "Join date (YYYY-MM-DD): ");
        double base  = Input.readDouble(in, "Base fee: ");

        return switch (t) {
            case 1 -> new RegularMember(first, last, age, join, base);
            case 2 -> {
                int sessions = Input.readInt(in, "Sessions/month: ");
                double feePer = Input.readDouble(in, "Fee per session: ");
                yield new PersonalTrainingMember(first, last, age, join, base, sessions, feePer);
            }
            case 3 -> {
                boolean spa = Input.readYesNo(in, "Spa access (y/n)? ");
                double premium = Input.readDouble(in, "Premium service fee: ");
                yield new PremiumMember(first, last, age, join, base, spa, premium);
            }
            default -> {
                System.out.println("Invalid type.");
                yield null;
            }
        };
    }

    private YearMonth readYearMonth(String prompt) {
        String s = Input.readLine(in, prompt).trim();
        try {
            return YearMonth.parse(s); // expects YYYY-MM
        } catch (Exception e) {
            return null;
        }
    }

    private void printFeeBreakdown(Member m, YearMonth month) {
        System.out.println("\nMonthly Fee Breakdown for " + month + ":");
        System.out.println("ID: " + m.getMemberId() + " | " + m.getFirstName() + " " + m.getLastName() +
                " | Joined: " + m.getJoinDate() + " | Base Fee: $" + String.format("%.2f", m.getBaseFee()));

        Performance p = m.getPerformance(month);

        if (p == null) {
            System.out.println("(No performance record for " + month + ")");
        }

        double base = m.getBaseFee();
        double extra = 0.0;
        double perfDiscount = 0.0;
        double lowPenalty = 0.0;

        if (m instanceof PersonalTrainingMember pt) {
            extra += pt.getSessionsPerMonth() * pt.getFeePerSession();
            System.out.println("PT Extra:       $" + String.format("%.2f", extra));
        }
        if (m instanceof PremiumMember pm) {
            extra += pm.getPremiumServiceFee();
            System.out.println("Premium Extra: $" + String.format("%.2f", pm.getPremiumServiceFee()));
        }

        double subtotal = base + extra;
        System.out.println("Subtotal:       $" + String.format("%.2f", subtotal));

        if (p != null) {
            if (p.getGoalAchieved()) {
                perfDiscount = subtotal * 0.10;
                System.out.println("Performance Discount (10%): -$" + String.format("%.2f", perfDiscount));
            }
            if (p.getRating() <= 2) {
                lowPenalty = 10;
                System.out.println("Low Rating Penalty: +$10.00");
            }
        }

        double total = subtotal - perfDiscount + lowPenalty;
        System.out.println("-------------------------------");
        System.out.println("Total:          $" + String.format("%.2f", total));
    }

    private void saveSnapshot() {
        try {
            storage.saveMembersToFile(repo.getAllMembers(), MEMBERS_FILE);
            storage.savePerformancesOfMembersToFile(repo.getAllMembers(), PERF_FILE);
            System.out.println("Saved to fixed files:");
            System.out.println(" - " + MEMBERS_FILE);
            System.out.println(" - " + PERF_FILE);
        } catch (IOException e) {
            System.out.println("Failed to save: " + e.getMessage());
        }
    }

    private void ensureDataDir() {
        try { Files.createDirectories(Paths.get(DATA_DIR)); }
        catch (IOException ignored) {}
    }

    /**
     * Edit first name, last name, age, and join date.
     * Keeps memberId, base fee, and subtype-specific fields the same.
     */
    private void editPersonalDetails(Member old) {
        System.out.println("\n-- Edit Personal Details --");
        System.out.println("(Press Enter to keep existing value)");

        // First name
        String firstIn = Input.readLine(in, "First name [" + old.getFirstName() + "]: ").trim();
        String first = firstIn.isBlank() ? old.getFirstName() : firstIn;

        // Last name
        String lastIn = Input.readLine(in, "Last name [" + old.getLastName() + "]: ").trim();
        String last = lastIn.isBlank() ? old.getLastName() : lastIn;

        // Age
        int age = old.getAge();
        String ageIn = Input.readLine(in, "Age [" + old.getAge() + "]: ").trim();
        if (!ageIn.isBlank()) {
            try {
                age = Integer.parseInt(ageIn);
                if (age <= 0) {
                    System.out.println("Age must be positive. Keeping old value.");
                    age = old.getAge();
                }
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number. Keeping old age.");
                age = old.getAge();
            }
        }

        // Join date
        LocalDate join = old.getJoinDate();
        String dateIn = Input.readLine(in, "Join date (YYYY-MM-DD) [" + old.getJoinDate() + "]: ").trim();
        if (!dateIn.isBlank()) {
            try {
                join = LocalDate.parse(dateIn);
            } catch (Exception ex) {
                System.out.println("Invalid date. Keeping old join date.");
                join = old.getJoinDate();
            }
        }

        // Rebuild a member of the SAME subtype, preserving subtype fields + base fee
        Member updated;
        if (old instanceof RegularMember) {
            updated = new RegularMember(old.getMemberId(), first, last, age, join, old.getBaseFee());
        } else if (old instanceof PersonalTrainingMember pt) {
            updated = new PersonalTrainingMember(
                    old.getMemberId(), first, last, age, join,
                    old.getBaseFee(), pt.getSessionsPerMonth(), pt.getFeePerSession());
        } else if (old instanceof PremiumMember pm) {
            updated = new PremiumMember(
                    old.getMemberId(), first, last, age, join,
                    old.getBaseFee(), pm.hasSpaAccess(), pm.getPremiumServiceFee());
        } else {
            // fallback to regular if unknown
            updated = new RegularMember(old.getMemberId(), first, last, age, join, old.getBaseFee());
        }

        // Preserve performance history
        for (Performance p : old.getPerformanceHistory()) {
            updated.addOrReplacePerformance(p);
        }

        // Replace in repo and save
        if (!repo.replaceMember(old.getMemberId(), updated)) {
            repo.deleteMember(old.getMemberId());
            repo.addMember(updated);
        }

        saveSnapshot();
        System.out.println("Personal details updated and saved.");
    }
}

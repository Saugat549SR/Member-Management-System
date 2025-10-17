package com.gym.storage;

import com.gym.model.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class CsvStorage {
    //Save member detail to new csv file
	public String saveMembersToNewFile(List<Member> members, String directory) throws IOException {
        Objects.requireNonNull(members, "members");
        Path dir = ensureDirectory(directory);
        String fileName = "members_" + nowStamp() + ".csv";
        Path file = dir.resolve(fileName);

        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            // header
            w.write("id,type,firstName,lastName,age,joinDate,baseFee,sessionsPerMonth,feePerSession,spaAccess,premiumServiceFee");
            w.newLine();

            for (Member m : members) {
                String type;
                String sessions = "";
                String feePerSession = "";
                String spaAccess = "";
                String premiumFee = "";

                if (m instanceof RegularMember) {
                    type = "REGULAR";
                } else if (m instanceof PersonalTrainingMember) {
                    type = "PT";
                    PersonalTrainingMember pt = (PersonalTrainingMember) m;
                    sessions = String.valueOf(pt.getSessionsPerMonth());
                    feePerSession = formatDouble(pt.getFeePerSession());
                } else if (m instanceof PremiumMember) {
                    type = "PREMIUM";
                    PremiumMember pm = (PremiumMember) m;
                    spaAccess = String.valueOf(pm.hasSpaAccess());
                    premiumFee = formatDouble(pm.getPremiumServiceFee());
                } else {
                    // Unknown subtype: skip row
                    continue;
                }

                w.write(String.join(",",
                        m.getMemberId(),
                        type,
                        esc(m.getFirstName()),
                        esc(m.getLastName()),
                        String.valueOf(m.getAge()),
                        m.getJoinDate().toString(),
                        formatDouble(m.getBaseFee()),
                        sessions,
                        feePerSession,
                        spaAccess,
                        premiumFee
                ));
                w.newLine();
            }
        }
        return file.toString();
    }

    //Save performances to a NEW timestamped file in the given directory. Returns the new file path. 
    public String savePerformancesToNewFile(List<Performance> performances, String directory) throws IOException {
        Objects.requireNonNull(performances, "performances");
        Path dir = ensureDirectory(directory);
        String fileName = "performances_" + nowStamp() + ".csv";
        Path file = dir.resolve(fileName);

        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            // header
            w.write("memberId,month,goalAchieved,rating,notes");
            w.newLine();

            for (Performance p : performances) {
                w.write(String.join(",",
                        p.getMemberId(),
                        p.getMonth().toString(),                       // YYYY-MM
                        String.valueOf(p.getGoalAchieved()),
                        String.valueOf(p.getRating()),
                        esc(p.getNotes())
                ));
                w.newLine();
            }
        }
        return file.toString();
    }

    // flatten performances from a list of members and save them.
    public String savePerformancesOfMembersToNewFile(List<Member> members, String directory) throws IOException {
        List<Performance> all = new ArrayList<>();
        for (Member m : members) {
            all.addAll(m.getPerformanceHistory());
        }
        return savePerformancesToNewFile(all, directory);
    }

    // Load members from CSV path
    public List<Member> loadMembers(String filePath) throws IOException {
        List<Member> result = new ArrayList<>();
        Path p = Paths.get(filePath);
        if (!Files.exists(p)) return result;

        try (BufferedReader r = Files.newBufferedReader(p)) {
            String header = r.readLine(); // skip header
            String line;
            while ((line = r.readLine()) != null) {
                String[] cols = splitCsv(line, 11); // expect 11 columns
                if (cols.length < 6) continue;

                String id        = safe(cols, 0);
                String type      = safe(cols, 1).toUpperCase(Locale.ROOT).trim();
                String firstName = unesc(safe(cols, 2));
                String lastName  = unesc(safe(cols, 3));
                int age          = parseIntSafe(safe(cols, 4), 0);
                LocalDate join   = LocalDate.parse(safe(cols, 5));
                double baseFee   = parseDoubleSafe(safe(cols, 6), 0.0);

                Member m;
                switch (type) {
                case "REGULAR":
                    m = new RegularMember(id, firstName, lastName, age, join, baseFee);
                    break;
                case "PT":
                    int sessions = parseIntSafe(safe(cols, 7), 0);
                    double perSession = parseDoubleSafe(safe(cols, 8), 0.0);
                    m = new PersonalTrainingMember(id, firstName, lastName, age, join, baseFee, sessions, perSession);
                    break;
                case "PREMIUM":
                    boolean spa = parseBooleanSafe(safe(cols, 9));
                    double premium = parseDoubleSafe(safe(cols, 10), 0.0);
                    m = new PremiumMember(id, firstName, lastName, age, join, baseFee, spa, premium);
                    break;
                default:
                    continue;
            }
                result.add(m);
            }
        }
        return result;
    }

    //Load performances from CSV path
    public List<Performance> loadPerformances(String filePath) throws IOException {
        List<Performance> result = new ArrayList<>();
        Path p = Paths.get(filePath);
        if (!Files.exists(p)) return result;

        try (BufferedReader r = Files.newBufferedReader(p)) {
            String header = r.readLine(); // skip header
            String line;
            while ((line = r.readLine()) != null) {
                String[] cols = splitCsv(line, 5); // expect 5 columns

                if (cols.length < 4) continue;

                String memberId   = safe(cols, 0);
                YearMonth month   = parseYearMonthSafe(safe(cols, 1));
                boolean achieved  = parseBooleanSafe(safe(cols, 2));
                int rating        = parseIntSafe(safe(cols, 3), 3);
                String notes      = cols.length >= 5 ? unesc(safe(cols, 4)) : "";

                if (month == null) month = YearMonth.now();

                Performance perf = new Performance(memberId, month, achieved, rating, notes);
                result.add(perf);
            }
        }
        return result;
    }



    private static Path ensureDirectory(String directory) throws IOException {
        Path dir = (directory == null || directory.isBlank())
                ? Paths.get(".")
                : Paths.get(directory);
        if (!Files.exists(dir)) Files.createDirectories(dir);
        return dir;
    }


    private static String nowStamp() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%04d%02d%02d_%02d%02d%02d",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                now.getHour(), now.getMinute(), now.getSecond());
   
    }

    private static String formatDouble(double d) {
        return String.format(Locale.ROOT, "%.2f", d);
    }

   
    private static String esc(String s) {
        if (s == null) return "";
        String v = s.replace("\"", "\"\"");
        return "\"" + v + "\""; // wrap in quotes for safety
    }

    private static String unesc(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s;
    }

   
    private static String[] splitCsv(String line, int expectedCols) {
        List<String> cols = new ArrayList<>(expectedCols);
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                // toggle or escape
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"'); // escaped quote
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                cols.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        cols.add(cur.toString());
        return cols.toArray(new String[0]);
    }

    //static methods
    private static String safe(String[] arr, int idx) {
        return idx >= 0 && idx < arr.length ? arr[idx] : "";
    }

    private static int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private static double parseDoubleSafe(String s, double def) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; }
    }

    private static boolean parseBooleanSafe(String s) {
        return "true".equalsIgnoreCase(s.trim()) || "yes".equalsIgnoreCase(s.trim());
    }

    private static YearMonth parseYearMonthSafe(String s) {
        try { return YearMonth.parse(s.trim()); } catch (Exception e) { return null; }
    }

   //attaching performance to members
    public void attachPerformancesToMembers(List<Member> members, List<Performance> performances) {
        Map<String, Member> map = members.stream()
                .collect(Collectors.toMap(Member::getMemberId, m -> m, (a, b) -> a));

        for (Performance p : performances) {
            Member m = map.get(p.getMemberId());
            if (m != null) {
                m.addOrReplacePerformance(p);
            }
        }
    }
    //saving member data to file
    public String saveMembersToFile(List<Member> members, String filePath) throws IOException {
        Objects.requireNonNull(members, "members");
        Path file = resolveTarget(filePath);

        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            w.write("id,type,firstName,lastName,age,joinDate,baseFee," +
                    "sessionsPerMonth,feePerSession,spaAccess,premiumServiceFee");
            w.newLine();

            for (Member m : members) {
                String type;
                String sessions = "";
                String feePerSession = "";
                String spaAccess = "";
                String premiumFee = "";

                if (m instanceof RegularMember) {
                    type = "REGULAR";
                } else if (m instanceof PersonalTrainingMember pt) {
                    type = "PT";
                    sessions = String.valueOf(pt.getSessionsPerMonth());
                    feePerSession = formatDouble(pt.getFeePerSession());
                } else if (m instanceof PremiumMember pm) {
                    type = "PREMIUM";
                    spaAccess = String.valueOf(pm.hasSpaAccess());
                    premiumFee = formatDouble(pm.getPremiumServiceFee());
                } else {
                    continue;
                }

                w.write(String.join(",",
                        m.getMemberId(),
                        type,
                        esc(m.getFirstName()),
                        esc(m.getLastName()),
                        String.valueOf(m.getAge()),
                        m.getJoinDate().toString(),
                        formatDouble(m.getBaseFee()),
                        sessions,
                        feePerSession,
                        spaAccess,
                        premiumFee
                ));
                w.newLine();
            }
        }
        return file.toString();
    }

    //saving different performance to file
    public String savePerformancesToFile(List<Performance> performances, String filePath) throws IOException {
        Objects.requireNonNull(performances, "performances");
        Path file = resolveTarget(filePath);

        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            w.write("memberId,month,goalAchieved,rating,notes");
            w.newLine();

            for (Performance p : performances) {
                w.write(String.join(",",
                        p.getMemberId(),
                        p.getMonth().toString(),                // YYYY-MM
                        String.valueOf(p.getGoalAchieved()),
                        String.valueOf(p.getRating()),
                        esc(p.getNotes())
                ));
                w.newLine();
            }
        }
        return file.toString();
    }

   //saving performance of member to file
    public String savePerformancesOfMembersToFile(List<Member> members, String filePath) throws IOException {
        List<Performance> all = new ArrayList<>();
        for (Member m : members) {
            all.addAll(m.getPerformanceHistory());
        }
        return savePerformancesToFile(all, filePath);
    }

    
    private Path resolveTarget(String filePath) throws IOException {
        Path target = Paths.get(filePath);
        Path parent = target.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        return target;
    }

}


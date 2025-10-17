package com.gym.main;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Scanner;

final class Input {
    private Input() {}

    static String readLine(Scanner in, String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    static int readInt(Scanner in, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(in.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Enter a valid integer.");
            }
        }
    }

    static double readDouble(Scanner in, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(in.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Enter a valid number.");
            }
        }
    }

    static boolean readYesNo(Scanner in, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim().toLowerCase();
            if (s.startsWith("y")) return true;
            if (s.startsWith("n")) return false;
            System.out.println("Please answer y/n.");
        }
    }

    static LocalDate readLocalDate(Scanner in, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return LocalDate.parse(in.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Format must be YYYY-MM-DD.");
            }
        }
    }

    static YearMonth readYearMonth(Scanner in, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return YearMonth.parse(in.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Format must be YYYY-MM.");
            }
        }
    }
    public static String readString(Scanner in, String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }
}
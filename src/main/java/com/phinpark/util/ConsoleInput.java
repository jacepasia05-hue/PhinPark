package com.phinpark.util;

import java.util.Scanner;

public class ConsoleInput {

	public static String readLineString(Scanner scanner) {
		String line;
		do {
			line = scanner.nextLine();
		} while (line.trim().isEmpty());
		return line;
	}

	public static String readString(Scanner scanner) {
		return scanner.next();
	}

	public static double readDouble(Scanner scanner) {
		double inputDouble;
		boolean validInput = false;
		while (!validInput) {
			try {
				String input = readLineString(scanner);
				inputDouble = Double.parseDouble(input);
				return inputDouble;
			} catch (NumberFormatException e) {
				System.out.println("Please enter a valid double number:");
			}
		}
		return 0;
	}

	public static int readInt(Scanner scanner) {
		int inputInt;
		boolean validInput = false;
		while (!validInput) {
			try {
				String input = readLineString(scanner);
				inputInt = Integer.parseInt(input);
				return inputInt;
			} catch (NumberFormatException e) {
				System.out.println("Please enter a valid integer number:");
			}
		}
		return 0;
	}
}

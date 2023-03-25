package readability;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("You must provide a valid path for the file containing your text.");
            return;
        }
        try {
            final var path = Paths.get(args[0]);
            final var text = Files.readString(path);
            final var readability = new Readability(text);
            System.out.println("The text is:");
            System.out.println(text);
            System.out.println();
            // readability.testSyllables();
            System.out.println(readability.formattedTextStatisticsData());
            System.out.println("Enter the score you want to calculate (ARI, FK, SMOG, CL, all): ");
            final String scoreChoiceInput = new Scanner(System.in).nextLine();
            if (scoreChoiceInput.equalsIgnoreCase("all")) {
                int ageSum = 0;
                for (var algorithm : Readability.ReadabilityAlgorithm.values()) {
                    final double score = readability.readabilityScore(algorithm);
                    final int age = Readability.getUpperAge((int) score);
                    ageSum += age;
                    System.out.println(readability.formattedReadabilityScore(algorithm));
                }
                double ageAvg = ageSum / (double) Readability.ReadabilityAlgorithm.values().length;
                System.out.printf("This text should be understood in average by %.2f year olds.", ageAvg);
            } else {
                try {
                    final var algorithmChoice = Readability.ReadabilityAlgorithm.valueOf(scoreChoiceInput.toUpperCase());
                    System.out.println(readability.formattedReadabilityScore(algorithmChoice));
                } catch (IllegalArgumentException iae) {
                    System.out.println("Invalid algorithm choice");
                }
            }
        } catch (InvalidPathException ipe) {
            System.out.println("Invalid path");
        } catch (IOException ioe) {
            System.out.println("Cannot read file");
        } catch (IllegalArgumentException iae) {
            System.out.println(iae.getMessage());
        }
    }
}
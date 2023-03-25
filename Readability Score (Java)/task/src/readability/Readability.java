package readability;

import java.util.regex.Pattern;

public class Readability {
    private final String text;
    public enum ReadabilityAlgorithm {
        ARI("Automated Readability Index"),
        FK("Flesch–Kincaid readability tests"),
        SMOG("Simple Measure of Gobbledygook"),
        CL("Coleman-Liau index");

        private final String description;

        ReadabilityAlgorithm(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private static void validateText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }
    }

    public Readability(String text) {
        validateText(text);
        this.text = text;
    }

    public double readabilityScore(ReadabilityAlgorithm algorithm) {
        switch (algorithm) {
            case ARI: return readabilityScore();
            case FK: return fleshKincaidReadability();
            case SMOG: return smog();
            case CL: return colemanLiauIndex();
            default: return 0;
        }
    }

    private double colemanLiauIndex() {
        double avgCharsPer100Words = charsCount() * 100.0 / wordCount();
        double avgSentencesPer100Words = sentenceCount() * 100.0 / wordCount();
        return Math.round((0.0588 * avgCharsPer100Words - 0.296 * avgSentencesPer100Words - 15.8) * 100.0) / 100.0;
    }

    private double fleshKincaidReadability() {
        long words = wordCount();
        long sentences = sentenceCount();
        long syllables = syllablesCount();
        double readability = Math.round((0.39 * words / sentences + 11.8 * syllables / words - 15.59) * 100.0) / 100.0;
        return Math.max(0.0, readability);   // if readability is less than 0, we return 0
    }

    private double smog() {
        long polysyllables = polysyllablesCount();
        long sentences = sentenceCount();
        return Math.round((1.043 * Math.sqrt(polysyllables * 30.0 / sentences) + 3.1291) * 100.0) / 100.0;
    }

    private double readabilityScore() {
        long chars = charsCount();
        long words = wordCount();
        long sentences = sentenceCount();
        double readability = Math.round((4.71 * chars / words + 0.5 * words / sentences - 21.43) * 100.0) / 100.0;
        return Math.max(0.0, readability);   // if readability is less than 0, we return 0
    }

    public long charsCount() {
        var charPattern = Pattern.compile("\\S");
        var charMatcher = charPattern.matcher(text);
        return charMatcher.results().count();
    }

    public long wordCount() {
        var wordPattern = Pattern.compile("\\S+");
        var wordMatcher = wordPattern.matcher(text);
        return wordMatcher.results().count();
    }

    public  long sentenceCount() {
        var sentencePattern = Pattern.compile("[^.?!]+");
        var sentenceMatcher = sentencePattern.matcher(text);
        return sentenceMatcher.results().count();
    }

    private static long syllablesPerWord(String word) {
        var syllablePattern = Pattern.compile("(?i)[aeiouy](?=[^aeiouy\\W])|[aiouy](?=\\b)");
        var syllableMatcher = syllablePattern.matcher(word);
        var syllableCount = syllableMatcher.results().count();
        return syllableCount == 0L ? 1L : syllableCount;
    }

    public long syllablesCount() {
        var wordPattern = Pattern.compile("\\S+");
        var wordMatcher = wordPattern.matcher(text);
        return wordMatcher.results()
                .mapToLong(matchResult -> syllablesPerWord(matchResult.group()))
                .sum();
    }

//    public void testSyllables() {
//        var wordPattern = Pattern.compile("\\S+");
//        var wordMatcher = wordPattern.matcher(text);
//        var sum = 0;
//        while (wordMatcher.find()) {
//            var word = wordMatcher.group();
//            sum += syllablesPerWord(word);
//            System.out.printf("%s: %d [%d]%n", word, syllablesPerWord(word), sum);
//        }
//    }

    public long polysyllablesCount() {
        var wordPattern = Pattern.compile("\\S+");
        var wordMatcher = wordPattern.matcher(text);
        return wordMatcher.results()
                .mapToLong(matchResult -> syllablesPerWord(matchResult.group()))
                .filter(syllables -> syllables > 2)
                .count();
    }

    public static String ageBracket(int readabilityTruncated) {
        int lowerAge = getLowerAge(readabilityTruncated);
        int upperAge = getUpperAge(readabilityTruncated);
        return String.format("%d-%d", lowerAge, upperAge);
    }

    public static int getUpperAge(int readabilityTruncated) {
        return readabilityTruncated > 13 ? 22 : readabilityTruncated + 6;
    }

    public static int getLowerAge(int readabilityTruncated) {
        return readabilityTruncated + 5;
    }

    public String formattedTextStatisticsData() {
        String template = """
                Words: %d
                Sentences: %d
                Characters: %d
                Syllables: %d
                Polysyllables: %d
                """;
        return template.formatted(
                wordCount(),
                sentenceCount(),
                charsCount(),
                syllablesCount(),
                polysyllablesCount()
        );
    }

    public String formattedReadabilityScore(ReadabilityAlgorithm algorithm) {
        final var template = "%s: %.2f (about %d-year-olds).\n";
        final double score = readabilityScore(algorithm);
        return template.formatted(
                algorithm.getDescription(),
                score,
                getUpperAge((int) score)
        );
    }
}

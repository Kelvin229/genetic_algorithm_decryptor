package ga;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class represents a genetic algorithm that solves the problem of finding
 * the encryption key for a given text and key length. The algorithm evolves a
 * population of decryption keys by applying crossover, mutation, and selection
 * operations to improve the fitness of candidate solutions.
 *
 */
public class GeneticAlgorithm {

    private static final Random random = new Random();
    private static int POP_SIZE, TOURNAMENT_SIZE, CHROMOSOME_LENGTH;
    private static double  MUTATION_RATE;
    private static String ENCRYPTED_TEXT;

    public GeneticAlgorithm(
            int popSize, double mutationRate, int tournamentSize,
            int chromosomeLength, String encryptedText
    ){
        POP_SIZE = popSize;
        MUTATION_RATE = mutationRate;
        TOURNAMENT_SIZE = tournamentSize;
        CHROMOSOME_LENGTH = chromosomeLength;
        ENCRYPTED_TEXT = encryptedText;
    }//Constructor

    /**
     * This method performs one-point crossover operation between two parent solutions to create
     * two child solutions.
     *
     * @param parent1 The first parent solution.
     * @param parent2 The second parent solution.
     * @param child1  The first child solution (output parameter).
     * @param child2  The second child solution (output parameter).
     */
    public static void onePointCrossover(char[] parent1, char[] parent2, char[] child1, char[] child2) {
        int crossoverPoint = random.nextInt(CHROMOSOME_LENGTH);

        for (int i = 0; i < crossoverPoint; i++) {
            child1[i] = parent1[i];
            child2[i] = parent2[i];
        }

        for (int i = crossoverPoint; i < CHROMOSOME_LENGTH; i++) {
            child1[i] = parent2[i];
            child2[i] = parent1[i];
        }
    }//onePointCrossover

    /**
     * This method performs Uniform crossover operation between two parent solutions to create
     * two child solutions.
     *
     * @param parent1 The first parent solution.
     * @param parent2 The second parent solution.
     * @param child1  The first child solution (output parameter).
     * @param child2  The second child solution (output parameter).
     */
    public static void uniformcrossover(char[] parent1, char[] parent2, char[] child1, char[] child2) {
        for (int i = 0; i < CHROMOSOME_LENGTH; i++) {
            if (random.nextBoolean()) {
                child1[i] = parent1[i];
                child2[i] = parent2[i];
            } else {
                child1[i] = parent2[i];
                child2[i] = parent1[i];
            }
        }
    }//uniformcrossover

    /**
     * This method performs scramble mutation operation on a chromosome. Scramble mutation
     * randomly selects a subset of genes and shuffles their positions within the
     * subset.
     *
     * @param chromosome The chromosome to be mutated.
     */
    public static void scrambleMutation(char[] chromosome) {
        int startIndex = random.nextInt(CHROMOSOME_LENGTH);
        int endIndex = ThreadLocalRandom.current().nextInt(startIndex, CHROMOSOME_LENGTH);
        char[] subset = new char[endIndex - startIndex + 1];

        for (int i = startIndex, j = 0; i <= endIndex; i++, j++) {
            subset[j] = chromosome[i];
        }

        for (int i = 0; i < subset.length; i++) {
            int swapIndex = random.nextInt(subset.length);
            char temp = subset[i];
            subset[i] = subset[swapIndex];
            subset[swapIndex] = temp;
        }

        for (int i = startIndex, j = 0; i <= endIndex; i++, j++) {
            chromosome[i] = subset[j];
        }
    }//scrambleMutation

    /**
     * This method performs mutation operation on two child solutions with a certain mutation
     * rate. The mutation operation may include scramble mutation on individual
     * children.
     *
     * @param child1 The first child solution.
     * @param child2 The second child solution.
     */
    public static void mutateChildren(char[] child1, char[] child2) {
        if (random.nextDouble() < MUTATION_RATE) {
            scrambleMutation(child1);
        }
        if (random.nextDouble() < MUTATION_RATE) {
            scrambleMutation(child2);
        }
    }//mutateChildren

    /**
     * This method calculates the frequency of each letter in the given encrypted text.
     *
     * @param encryptedText The text for which character frequencies need to be calculated.
     * @return An array representing the frequency of each letter (a to z) in the text.
     */
    private static int[] calculateCharacterFrequencies(String encryptedText) {
        int[] frequencies = new int[26]; // Array to store character frequencies (a to z)

        for (char c : encryptedText.toCharArray()) {
            if (Character.isLetter(c)) {
                int index = Character.toLowerCase(c) - 'a';
                frequencies[index]++;
            }
        }

        return frequencies;
    }//calculateCharacterFrequencies

    /**
     * This method initializes the population of candidate solutions randomly.
     *
     * @return A 2D array representing the initial population of candidate
     *         solutions.
     */
    public static char[][] initializePopulation() {
        char[][] population = new char[POP_SIZE][CHROMOSOME_LENGTH];
        int[] characterFrequencies = calculateCharacterFrequencies(ENCRYPTED_TEXT);

        for (int i = 0; i < POP_SIZE; i++) {
            for (int j = 0; j < CHROMOSOME_LENGTH; j++) {
                population[i][j] = getRandomCharBasedOnFrequencies(characterFrequencies);
            }
        }
        return population;
    }//initializePopulation

    /**
     * This method generates a random character based on the given frequencies.
     *
     * @param frequencies An array representing the frequencies of characters.
     * @return A randomly selected character based on the provided frequencies.
     */
    private static char getRandomCharBasedOnFrequencies(int[] frequencies) {
        int totalFrequency = 0;
        for (int frequency : frequencies) {
            totalFrequency += frequency;
        }

        int randomValue = random.nextInt(totalFrequency) + 1;
        int cumulativeFrequency = 0;

        for (int i = 0; i < frequencies.length; i++) {
            cumulativeFrequency += frequencies[i];
            if (randomValue <= cumulativeFrequency) {
                return (char) ('a' + i);
            }
        }
        return 'a';
    }//getRandomCharBasedOnFrequencies

    /**
     * This method evaluates the fitness of each candidate solution in the given population.
     *
     * @param population The population of candidate solutions to be evaluated.
     * @return An array containing the fitness values for each candidate solution.
     */
    public static double[] evaluatePopulation(char[][] population) {
        double[] fitnessValues = new double[POP_SIZE];
        for (int i = 0; i < POP_SIZE; i++) {
            String key = new String(population[i]);
            String decryptedText = Evaluation.decrypt(key, ENCRYPTED_TEXT);

            if (decryptedText != null) {
                fitnessValues[i] = Evaluation.fitness(key, decryptedText);
            } else {
                fitnessValues[i] = Double.MAX_VALUE;
            }
        }
        return fitnessValues;
    }//evaluatePopulation

    /**
     * This method selects a parent solution from the given population using tournament
     * selection. it involves randomly selecting a subset of
     * candidates and choosing the best candidate from the subset based on their
     * fitness values.
     *
     * @param population     The population of candidate solutions.
     * @param fitnessValues  The fitness values corresponding to each candidate
     *                       solution.
     * @return The selected parent solution.
     */
    public static char[] selectParent(char[][] population, double[] fitnessValues) {
        int tournamentSize = TOURNAMENT_SIZE;
        int[] tournamentIndices = new int[tournamentSize];

        for (int i = 0; i < tournamentSize; i++) {
            tournamentIndices[i] = random.nextInt(POP_SIZE);
        }

        int bestIndex = tournamentIndices[0];
        for (int i = 1; i < tournamentSize; i++) {
            if (fitnessValues[tournamentIndices[i]] < fitnessValues[bestIndex]) {
                bestIndex = tournamentIndices[i];
            }
        }

        return population[bestIndex].clone();
    }//selectParent

    /**
     * This method finds the index of the best solution in the given population based on their
     * fitness values.
     *
     * @param fitnessValues The fitness values corresponding to each candidate
     *                      solution.
     * @return The index of the best solution in the population.
     */
    public static int findBestSolutionIndex(double[] fitnessValues) {
        int bestIndex = 0;
        for (int i = 1; i < POP_SIZE; i++) {
            if (fitnessValues[i] < fitnessValues[bestIndex]) {
                bestIndex = i;
            }
        }
        return bestIndex;
    }//findBestSolutionIndex
}//GeneticAlgorithm


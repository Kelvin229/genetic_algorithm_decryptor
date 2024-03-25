import ga.Evaluation;
import ga.GeneticAlgorithm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * The Main class represents the entry point of the genetic algorithm application for decryption.
 * It allows users to input parameters such as crossover rate, mutation rate, tournament size,
 * population size, and maximum number of generations through the console. The genetic algorithm
 * is then applied to decrypt an encrypted text provided in a data file. The algorithm aims to find
 * an optimal decryption key that minimizes the fitness score, indicating a close match between
 * the decrypted text and the expected plaintext.
 *
 * @author Kelvin Odinamadu
 * @course COSC COSC 3P71
 * @assignment #2
 * @student Id 7063571
 * @version    1.0
 * @since     06/11/2023
 *
 * citation:
 * 1) Lecture slides on genetic algorithm
 *
 */
public class Main {
    private static final double FITNESS_THRESHOLD = 0.001;
    private static final String PATH = "src/Data2.txt";;
    private static final Random random = new Random();


    /**
     * This method reads the key length from the specified file.
     *
     * @param filePath Path to the file containing the key length information.
     * @return The key length read from the file.
     * @throws IOException If an I/O error occurs while reading the file or if the key length is invalid.
     */
    public static int getKeyLengthFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        List<String> lines = Files.readAllLines(path);
        try {
            return Integer.parseInt(lines.get(0).trim());
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new IOException("Invalid key length in the file: " + filePath);
        }
    }//getKeyLengthFromFile

    /**
     * This method reads the encrypted text from the specified file.
     *
     * @param keyLength Length of the decryption key.
     * @return The encrypted text read from the file.
     * @throws IOException If an I/O error occurs while reading the file or if the encrypted text is invalid.
     */
    public static String readEncryptedTextFromFile(int keyLength) throws IOException {
        Path path = Paths.get(PATH);
        List<String> lines;

        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            throw new IOException("Error reading the file: " + PATH, e);
        }
        if (lines.size() < 2) {
            throw new IOException("Invalid file format: " + PATH);
        }
        StringBuilder encryptedTextBuilder = new StringBuilder();
        for (int i = 1; i < lines.size(); i++) {
            encryptedTextBuilder.append(lines.get(i).trim());
        }
        String encryptedText = encryptedTextBuilder.toString();
        if (encryptedText.isEmpty()) {
            throw new IOException("Invalid encrypted text in the file: " + PATH);
        }
        return encryptedText;
    }//readEncryptedTextFromFile

    /**
     * This method runs a genetic algorithm for solving the decryption problem.
     *
     * @param POP_SIZE         The size of the population.
     * @param CROSSOVER_RATE   The probability of crossover during reproduction.
     * @param CHROMOSOME_LENGTH The length of each individual chromosome.
     * @param ENCRYPTED_TEXT   The encrypted text that needs to be decrypted.
     * @param MAX_GEN          The maximum number of generations to run the genetic algorithm.
     * @param seed             The seed for the random number generator to ensure reproducibility.
     * @param MUTATION_RATE    The probability of mutation in the offspring.
     * @return A list of GenerationData objects representing data for each generation during the algorithm's execution.
     */
    public static List<GenerationData> runGeneticAlgorithm(
            int POP_SIZE, double CROSSOVER_RATE,int CHROMOSOME_LENGTH, String ENCRYPTED_TEXT,
            int MAX_GEN, long seed, double MUTATION_RATE
    ) {
        random.setSeed(seed);
        List<GenerationData> generationDataList = new ArrayList<>();

        char[][] population = GeneticAlgorithm.initializePopulation();
        double bestFitness = Double.MAX_VALUE;

        for (int gen = 1; gen <= MAX_GEN; gen++) {
            double[] fitnessValues = GeneticAlgorithm.evaluatePopulation(population);
            char[][] newPopulation = new char[POP_SIZE][CHROMOSOME_LENGTH];
            double[] newFitnessValues = new double[POP_SIZE];

            for (int i = 0; i < POP_SIZE; i += 2) {
                char[] parent1 = GeneticAlgorithm.selectParent(population, fitnessValues);
                char[] parent2 = GeneticAlgorithm.selectParent(population, fitnessValues);
                char[] child1 = parent1.clone();
                char[] child2 = parent2.clone();

                if (random.nextDouble() < CROSSOVER_RATE) {
                    GeneticAlgorithm.uniformcrossover(parent1, parent2, child1, child2);
                    GeneticAlgorithm.onePointCrossover(parent1, parent2, child1, child2);
                }

                GeneticAlgorithm.mutateChildren( child1, child2);

                newPopulation[i] = child1;
                newPopulation[i + 1] = child2;

                newFitnessValues[i] = Evaluation.fitness(new String(child1), ENCRYPTED_TEXT);
                newFitnessValues[i + 1] = Evaluation.fitness(new String(child2), ENCRYPTED_TEXT);
            }

            double minFitness = Double.MAX_VALUE;
            for (int i = 0; i < POP_SIZE; i++) {
                if (newFitnessValues[i] < minFitness) {
                    minFitness = newFitnessValues[i];
                }
            }

            double averageFitness = 0;
            for (double fitness : newFitnessValues) {
                averageFitness += fitness;
            }
            averageFitness /= POP_SIZE;

            if (bestFitness - minFitness > FITNESS_THRESHOLD) {
                bestFitness = minFitness;
            }

            System.out.println("Generation: " + gen + " - Best Fitness: " + minFitness);
            System.out.println("Generation: " + gen + " - Average Population Fitness: " + averageFitness);

            population = newPopulation;

            GenerationData generationData = new GenerationData();
            generationData.setGenerationNumber(gen);
            generationData.setBestFitness(minFitness);
            generationData.setAverageFitness(averageFitness);
            generationData.setCrossOverRate(CROSSOVER_RATE);
            generationData.setMutationRate(MUTATION_RATE);
            generationData.setPopulation(POP_SIZE);

            generationDataList.add(generationData);
        }
        return generationDataList;
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter crossover rate (0-100)%: ");
        double CROSSOVER_RATE = scanner.nextDouble() / 100.0;

        System.out.print("Enter mutation rate (0-100)%: ");
        double MUTATION_RATE = scanner.nextDouble() / 100.0;

        int TOURNAMENT_SIZE = random.nextInt(4) + 2;

        System.out.print("Enter population size: ");
        int POP_SIZE = scanner.nextInt();

        System.out.print("Enter maximum number of generations: ");
        int MAX_GEN = scanner.nextInt();

        int CHROMOSOME_LENGTH = getKeyLengthFromFile(PATH);
        String ENCRYPTED_TEXT = readEncryptedTextFromFile(CHROMOSOME_LENGTH).trim();


        System.out.println("Datafile: " + PATH );
        System.out.println("Population Size: " + POP_SIZE );
        System.out.println("Mutation Rate: " + MUTATION_RATE );
        System.out.println("Tournament Size: " + TOURNAMENT_SIZE );
        System.out.println("Chromosome Length: " + CHROMOSOME_LENGTH );

        GeneticAlgorithm GA = new GeneticAlgorithm(
                POP_SIZE, MUTATION_RATE,
                TOURNAMENT_SIZE, CHROMOSOME_LENGTH, ENCRYPTED_TEXT
        );

        int numRuns = 5;
        for (int i = 0; i < numRuns; i++) {
            long seed = System.currentTimeMillis();
            List<GenerationData> generationDataList = runGeneticAlgorithm(
                    POP_SIZE, CROSSOVER_RATE,CHROMOSOME_LENGTH, ENCRYPTED_TEXT, MAX_GEN,seed,MUTATION_RATE
            );

            String csvFilePath = "src/output.csv";
            CSVWriter.appendDataToCSV(generationDataList, csvFilePath);
        }

        System.out.println("All runs completed. Data has been written to the CSV file.");
    }//main
}//Main

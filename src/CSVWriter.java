import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * The CSVWriter class is responsible for appending data to a CSV file.
 */
public class CSVWriter {

    /**
     * This method appends generation data to a CSV file. If the file does not exist or is empty,
     * it writes the CSV header as well.
     *
     * @param generationDataList A list of GenerationData objects containing data
     *                           for each generation.
     * @param filePath           The path to the CSV file.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    public static void appendDataToCSV(List<GenerationData> generationDataList, String filePath) throws IOException {
        boolean writeHeader = false;

        // Check if the file does not exist or if it is empty
        if (!Files.exists(Path.of(filePath)) || Files.size(Path.of(filePath)) == 0) {
            writeHeader = true;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
            if (writeHeader) {
                // Write CSV header if the file is empty
                writer.append("Generation,Best Fitness,Average Fitness,Cross-over Rate,Mutation Rate,Population\n");
            }

            for (GenerationData generationData : generationDataList) {
                writer.append(String.valueOf(generationData.getGenerationNumber())).append(",");
                writer.append(String.valueOf(generationData.getBestFitness())).append(",");
                writer.append(String.valueOf(generationData.getAverageFitness())).append(",");
                writer.append(String.valueOf(generationData.getCrossOverRate())).append(",");
                writer.append(String.valueOf(generationData.getMutationRate())).append(",");
                writer.append(String.valueOf(generationData.getPopulation())).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing data to CSV: " + e.getMessage());
            throw e;
        }
    }//appendDataToCSV
}//CSVWriter

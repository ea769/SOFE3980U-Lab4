package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class App {
    private static class MultiClassMetrics {
        double crossEntropy;
        int[][] confusionMatrix = new int[5][5];
    }

    public static void main(String[] args) {
        MultiClassMetrics metrics = evaluateFile("model.csv");
        if (metrics == null) {
            return;
        }

        System.out.println("CE =" + metrics.crossEntropy);
        System.out.println("Confusion matrix");
        System.out.println("\t\ty=1\t\ty=2\t\ty=3\t\ty=4\t\ty=5");
        for (int predictedClass = 0; predictedClass < 5; predictedClass++) {
            System.out.println(
                "\ty^=" + (predictedClass + 1)
                + "\t" + metrics.confusionMatrix[predictedClass][0]
                + "\t" + metrics.confusionMatrix[predictedClass][1]
                + "\t" + metrics.confusionMatrix[predictedClass][2]
                + "\t" + metrics.confusionMatrix[predictedClass][3]
                + "\t" + metrics.confusionMatrix[predictedClass][4]
            );
        }
    }

    private static MultiClassMetrics evaluateFile(String filePath) {
        List<String[]> allData;
        try {
            FileReader fileReader = new FileReader(filePath);
            CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build();
            allData = csvReader.readAll();
            csvReader.close();
        } catch (Exception e) {
            System.out.println("Error reading the CSV file: " + filePath);
            return null;
        }

        MultiClassMetrics metrics = new MultiClassMetrics();
        int n = allData.size();

        for (String[] row : allData) {
            int yTrue = Integer.parseInt(row[0]) - 1;
            double[] probabilities = new double[5];
            int predictedClass = 0;
            double maxProbability = -1.0;

            for (int i = 0; i < 5; i++) {
                probabilities[i] = Double.parseDouble(row[i + 1]);
                if (probabilities[i] > maxProbability) {
                    maxProbability = probabilities[i];
                    predictedClass = i;
                }
            }

            metrics.crossEntropy += -Math.log(probabilities[yTrue]);
            metrics.confusionMatrix[predictedClass][yTrue]++;
        }

        metrics.crossEntropy /= n;
        return metrics;
    }
}

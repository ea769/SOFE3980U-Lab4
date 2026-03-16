package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class App {
    private static final float EPSILON = 1e-7f;

    private static class RegressionMetrics {
        float mse;
        float mae;
        float mare;
    }

    public static void main(String[] args) {
        String[] files = {"model_1.csv", "model_2.csv", "model_3.csv"};
        RegressionMetrics[] results = new RegressionMetrics[files.length];

        for (int i = 0; i < files.length; i++) {
            results[i] = evaluateFile(files[i]);
            if (results[i] == null) {
                return;
            }
        }

        int bestMseIndex = 0;
        int bestMaeIndex = 0;
        int bestMareIndex = 0;

        for (int i = 1; i < results.length; i++) {
            if (results[i].mse < results[bestMseIndex].mse) bestMseIndex = i;
            if (results[i].mae < results[bestMaeIndex].mae) bestMaeIndex = i;
            if (results[i].mare < results[bestMareIndex].mare) bestMareIndex = i;
        }

        System.out.println("According to MSE, The best model is " + files[bestMseIndex]);
        System.out.println("According to MAE, The best model is " + files[bestMaeIndex]);
        System.out.println("According to MARE, The best model is " + files[bestMareIndex]);
    }

    private static RegressionMetrics evaluateFile(String filePath) {
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

        float sumSquaredError = 0.0f;
        float sumAbsoluteError = 0.0f;
        float sumAbsoluteRelativeError = 0.0f;
        int n = allData.size();

        for (String[] row : allData) {
            float yTrue = Float.parseFloat(row[0]);
            float yPredicted = Float.parseFloat(row[1]);
            float error = yTrue - yPredicted;

            sumSquaredError += error * error;
            sumAbsoluteError += Math.abs(error);
            sumAbsoluteRelativeError += Math.abs(error) / (Math.abs(yTrue) + EPSILON);
        }

        RegressionMetrics metrics = new RegressionMetrics();
        metrics.mse = sumSquaredError / n;
        metrics.mae = sumAbsoluteError / n;
        metrics.mare = sumAbsoluteRelativeError / n;

        System.out.println("for " + filePath);
        System.out.println("\tMSE =" + metrics.mse);
        System.out.println("\tMAE =" + metrics.mae);
        System.out.println("\tMARE =" + metrics.mare);
        return metrics;
    }
}

package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class App {
    private static final double EPSILON = 1e-15;
    private static final double THRESHOLD = 0.5;

    private static class BinaryMetrics {
        double bce;
        int tp;
        int fp;
        int tn;
        int fn;
        double accuracy;
        double precision;
        double recall;
        double f1Score;
        double aucRoc;
    }

    public static void main(String[] args) {
        String[] files = {"model_1.csv", "model_2.csv", "model_3.csv"};
        BinaryMetrics[] results = new BinaryMetrics[files.length];

        for (int i = 0; i < files.length; i++) {
            results[i] = evaluateFile(files[i]);
            if (results[i] == null) {
                return;
            }
        }

        int bestBceIndex = 0;
        int bestAccuracyIndex = 0;
        int bestPrecisionIndex = 0;
        int bestRecallIndex = 0;
        int bestF1Index = 0;
        int bestAucIndex = 0;

        for (int i = 1; i < results.length; i++) {
            if (results[i].bce < results[bestBceIndex].bce) bestBceIndex = i;
            if (results[i].accuracy > results[bestAccuracyIndex].accuracy) bestAccuracyIndex = i;
            if (results[i].precision > results[bestPrecisionIndex].precision) bestPrecisionIndex = i;
            if (results[i].recall > results[bestRecallIndex].recall) bestRecallIndex = i;
            if (results[i].f1Score > results[bestF1Index].f1Score) bestF1Index = i;
            if (results[i].aucRoc > results[bestAucIndex].aucRoc) bestAucIndex = i;
        }

        System.out.println("According to BCE, The best model is " + files[bestBceIndex]);
        System.out.println("According to Accuracy, The best model is " + files[bestAccuracyIndex]);
        System.out.println("According to Precision, The best model is " + files[bestPrecisionIndex]);
        System.out.println("According to Recall, The best model is " + files[bestRecallIndex]);
        System.out.println("According to F1 score, The best model is " + files[bestF1Index]);
        System.out.println("According to AUC ROC, The best model is " + files[bestAucIndex]);
    }

    private static BinaryMetrics evaluateFile(String filePath) {
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

        BinaryMetrics metrics = new BinaryMetrics();
        int n = allData.size();
        int positiveCount = 0;
        int negativeCount = 0;
        int[] trueLabels = new int[n];
        double[] predictedScores = new double[n];

        for (int i = 0; i < n; i++) {
            String[] row = allData.get(i);
            int yTrue = Integer.parseInt(row[0]);
            double yPredicted = Double.parseDouble(row[1]);

            trueLabels[i] = yTrue;
            predictedScores[i] = yPredicted;

            double clippedPrediction = Math.min(Math.max(yPredicted, EPSILON), 1.0 - EPSILON);
            metrics.bce += -(yTrue * Math.log(clippedPrediction) + (1 - yTrue) * Math.log(1.0 - clippedPrediction));

            int predictedClass = yPredicted >= THRESHOLD ? 1 : 0;
            if (yTrue == 1) {
                positiveCount++;
                if (predictedClass == 1) metrics.tp++;
                else metrics.fn++;
            } else {
                negativeCount++;
                if (predictedClass == 1) metrics.fp++;
                else metrics.tn++;
            }
        }

        metrics.bce /= n;
        metrics.accuracy = (double) (metrics.tp + metrics.tn) / n;
        metrics.precision = (double) metrics.tp / (metrics.tp + metrics.fp);
        metrics.recall = (double) metrics.tp / (metrics.tp + metrics.fn);
        metrics.f1Score = 2.0 * metrics.precision * metrics.recall / (metrics.precision + metrics.recall);

        double[] x = new double[101];
        double[] y = new double[101];
        for (int i = 0; i <= 100; i++) {
            double threshold = i / 100.0;
            int tpAtThreshold = 0;
            int fpAtThreshold = 0;
            for (int j = 0; j < n; j++) {
                if (predictedScores[j] >= threshold) {
                    if (trueLabels[j] == 1) tpAtThreshold++;
                    else fpAtThreshold++;
                }
            }
            y[i] = (double) tpAtThreshold / positiveCount;
            x[i] = (double) fpAtThreshold / negativeCount;
        }

        for (int i = 1; i <= 100; i++) {
            metrics.aucRoc += (y[i - 1] + y[i]) * Math.abs(x[i - 1] - x[i]) / 2.0;
        }

        System.out.println("for " + filePath);
        System.out.println("\tBCE =" + metrics.bce);
        System.out.println("\tConfusion matrix");
        System.out.println("\t\t\ty=1\t\ty=0");
        System.out.println("\t\ty^=1\t" + metrics.tp + "\t" + metrics.fp);
        System.out.println("\t\ty^=0\t" + metrics.fn + "\t" + metrics.tn);
        System.out.println("\tAccuracy =" + metrics.accuracy);
        System.out.println("\tPrecision =" + metrics.precision);
        System.out.println("\tRecall =" + metrics.recall);
        System.out.println("\tf1 score =" + metrics.f1Score);
        System.out.println("\tauc roc =" + metrics.aucRoc);
        return metrics;
    }
}

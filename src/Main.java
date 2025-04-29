/*
 * Natural Language Processor
 * Authors: Ethan Kulawiak, Paul Zegarek, Jean LaFrance, Isaias Barreto
 * 
 */

package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
    
    public static void main(String args[]) {
        String dataPath = "Training.txt";               //File path
        List<Sentence> dataset = readFile(dataPath);    //Whole data set

        List<String[][]> testSet;                       //Test set of 1000 sentence
        HashMap<String, List<Pair>> frequencyTable;     //PoS Tag frequencies for unigram and bigram

        //Used in calculating accuracy
        int totalWords = 0;
        int correctTags = 0;
        double accuracy = 100.0;

        try {
            //Create BufferedWriter for accuracy report
            BufferedWriter defaultOnlyWriter = new BufferedWriter(new FileWriter("AccuracyReport_DefaultOnly.txt"));
            BufferedWriter defaultPlusUnigramWriter = new BufferedWriter(new FileWriter("AccuracyReport_DefaultPlusUnigram.txt"));
            BufferedWriter originalWriter = new BufferedWriter(new FileWriter("AccuracyReport_Original.txt"));

            defaultOnlyWriter.write("Default Only Tagging Accuracy Report\n\n");
            defaultPlusUnigramWriter.write("Default + Unigram Tagging Accuracy Report\n\n");
            originalWriter.write("Original (Bigram + Unigram + Default) Tagging Accuracy Report\n\n");

            // Jacknife procedure using 1000 words an iteration as a test set
            for (int i = 0; i < dataset.size(); i += 1000) {
                int end = Math.min(i + 1000, dataset.size());

                //Create test set used to test the training
                testSet = getTestSet(dataset.subList(i, end), false);

                //Create training set that doesn't include test set
                List<Sentence> trainingSet = new ArrayList<>();
                trainingSet.addAll(dataset.subList(0, i));
                trainingSet.addAll(dataset.subList(end, dataset.size()));
                frequencyTable = buildFrequencyTable(trainingSet);      //Build frequency table from training set

                //Tag test sets
                List<String[][]> defaultOnlyTagged = tagTestSet(testSet, frequencyTable, "default");
                List<String[][]> defaultPlusUnigramTagged = tagTestSet(testSet, frequencyTable, "unigram");
                List<String[][]> originalTagged = tagTestSet(testSet, frequencyTable, "original");

                // Update accuracy information
                List<String[][]> testSetAccurate = getTestSet(dataset.subList(i, end), true);

                double defaultOnlyAcc = calculateAccuracy(defaultOnlyTagged, testSetAccurate);
                double defaultPlusUnigramAcc = calculateAccuracy(defaultPlusUnigramTagged, testSetAccurate);
                double originalAcc = calculateAccuracy(originalTagged, testSetAccurate);

                // Construct accuracy report output Strings
                String iterationString = String.format("Iteration %,d:\nTest Range: %,d-%,d of %,d\n", i/1000, i, end, dataset.size());

                defaultOnlyWriter.write(iterationString);
                defaultOnlyWriter.write(String.format("Accuracy: %%%.2f\n\n", defaultOnlyAcc));

                defaultPlusUnigramWriter.write(iterationString);
                defaultPlusUnigramWriter.write(String.format("Accuracy: %%%.2f\n\n", defaultPlusUnigramAcc));

                originalWriter.write(iterationString);
                originalWriter.write(String.format("Accuracy: %%%.2f\n\n", originalAcc));
            }

            // Close writers
            defaultOnlyWriter.close();
            defaultPlusUnigramWriter.close();
            originalWriter.close();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    // Adds tags to a test set List<String[][]> based on the strategy
    private static List<String[][]> tagTestSet(List<String[][]> testSet, HashMap<String, List<Pair>> frequencyTable, String strategy) {
        List<String[][]> taggedTestSet = new ArrayList<>();
    
        // Deep copy the testSet to avoid modifying the original
        for (String[][] sentence : testSet) {
            String[][] copiedSentence = new String[sentence.length][2];
            for (int i = 0; i < sentence.length; i++) {
                copiedSentence[i][0] = sentence[i][0];
                copiedSentence[i][1] = sentence[i][1];
            }
            taggedTestSet.add(copiedSentence);
        }
    
        for (String[][] testSentence : taggedTestSet) {
            for (int j = 0; j < testSentence.length; j++) {
                String currentWord = testSentence[j][0];
                boolean tagged = false;
    
                if (strategy.equals("original")) {
                    // Bigram first
                    if (j > 0) {
                        String bigramKey = testSentence[j-1][1] + "|" + currentWord;
                        if (frequencyTable.containsKey(bigramKey)) {
                            tagged = true;
                            testSentence[j][1] = findMostFrequentTag(frequencyTable.get(bigramKey));
                        }
                    }
                    // Then unigram
                    if (!tagged && frequencyTable.containsKey(currentWord)) {
                        tagged = true;
                        testSentence[j][1] = findMostFrequentTag(frequencyTable.get(currentWord));
                    }
                } else if (strategy.equals("unigram")) {
                    // Only unigram fallback
                    if (frequencyTable.containsKey(currentWord)) {
                        tagged = true;
                        testSentence[j][1] = findMostFrequentTag(frequencyTable.get(currentWord));
                    }
                }
    
                if (!tagged) {
                    // Default tag will be NN
                    testSentence[j][1] = "NN";
                }
            }
        }
        return taggedTestSet;
    }

    // Helper to find the most frequent tag from a list
    private static String findMostFrequentTag(List<Pair> tags) {
        Pair posMatch = new Pair("", 0);
        for (Pair tag : tags) {
            if (tag.getValue() > posMatch.getValue()) {
                posMatch = tag;
            }
        }
        return posMatch.getKey();
    }

    // Calculates accuracy between tagged and accurate test sets
    private static double calculateAccuracy(List<String[][]> taggedSet, List<String[][]> accurateSet) {
        int total = 0;
        int correct = 0;

        for (int i = 0; i < taggedSet.size(); i++) {
            String[][] taggedSentence = taggedSet.get(i);
            String[][] accurateSentence = accurateSet.get(i);

            total += taggedSentence.length;
            for (int j = 0; j < taggedSentence.length; j++) {
                if (taggedSentence[j][1].equals(accurateSentence[j][1])) {
                    correct++;
                }
            }
        }
        return (double) correct / total * 100.0;
    }
    // Returns a frequency table containing each word in
    // Parameter should be a sublist of elements from original dataset minus the test set
    private static HashMap<String, List<Pair>> buildFrequencyTable(List<Sentence> dataset) {
        HashMap<String, List<Pair>> posTagger = new HashMap<String, List<Pair>>();
        
        for (Sentence sentence : dataset) {
            List<Word> words = sentence.getWords();

            for (int i = 0; i < words.size(); i++) {
                String word = words.get(i).text();
                String posTag = words.get(i).posTag();

                //Unigram tagger
                posTagger.compute(word, (key, pairs) -> {
                    //Initiate ArrayList if not already
                    if (pairs == null) {
                        pairs = new ArrayList<>();
                    }

                    //Update existing pair with posTag key
                    boolean updated = false;
                    for (Pair pair : pairs) {
                        if (pair.getKey().equals(posTag)) {
                            pair.increment();   // Increase occurence count
                            updated = true;
                            break;
                        }
                    }

                    //Add new pair if no existing one
                    if (!updated) {
                        pairs.add(new Pair(posTag, 1));
                    }
                    return pairs;
                });

                //Bigram tagger for i > 0
                if (i > 0) {
                    // Key will contain previous tag and current word for context ex. "NN|walked"
                    String previousTag = words.get(i-1).posTag();
                    String bigram = previousTag + "|" + word;

                    posTagger.compute(bigram, (key, pairs) -> {
                        //Initiate ArrayList if not already
                        if (pairs == null) {
                            pairs = new ArrayList<>();
                        }
    
                        //Update existing pair with posTag key
                        boolean updated = false;
                        for (Pair pair : pairs) {
                            if (pair.getKey().equals(posTag)) {
                                pair.increment();   // Increase occurence count
                                updated = true;
                                break;
                            }
                        }

                        //Add new pair if no existing one
                        if (!updated) {
                            pairs.add(new Pair(posTag, 1));
                        }
                        return pairs;
                    });
                }
            }
        }

        return posTagger;
    }

    // Returns a list of 'sentences', each a 2D array of words with an empty element for use in tagging
    // Parameter testSentences should be a sublist of 1000 elements from original dataset
    // Parameter includeOriginalTags is used for checking accuracy of training
    private static List<String[][]> getTestSet(List<Sentence> testSentences, boolean includeOriginalTags) {
        List<String[][]> testset = new ArrayList<>();

        // Reformat Sentence objects
        for (int i = 0; i < testSentences.size(); i++) {
            List<Word> words = testSentences.get(i).getWords();
            String[][] newSentence = new String[words.size()][2];
            testset.add(newSentence);

            for (int j = 0; j < words.size(); j++) {
                newSentence[j][0] = words.get(j).text();

                if (includeOriginalTags) {
                    newSentence[j][1] = words.get(j).posTag();
                }
            }
        }

        return testset;
    }

    // Read file and return an array of Sentences. Each Sentence will contain an array of Word Records
    private static List<Sentence> readFile(String path) {
        List<Sentence> dataset = new ArrayList<>();     //The entire dataset in array

        try {
            //Create BufferedReader for data
            BufferedReader reader = new BufferedReader(new FileReader(path));

            String lineSentence;
            while((lineSentence = reader.readLine()) != null) {
                //Build Sentence object out of elements on the readline
                Sentence sentence = new Sentence();
                String[] pairs = lineSentence.split("\t");

                for (String pair : pairs) {
                    String[] parts = pair.split("\\\\");
                    sentence.addWord(parts[0], parts[1]);
                }

                dataset.add(sentence);
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return dataset;
    }
}
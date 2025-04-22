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
            BufferedWriter writer = new BufferedWriter(new FileWriter("AccuracyReport.txt"));
            writer.write("Bigram Tagger Accuracy Report\n\n");

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

                //Tag test set
                List<String[][]> taggedTestSet = tagTestSet(testSet, frequencyTable);

                // Update accuracy information
                int wordCount = 0;
                int correct = 0;
                List<String[][]> testSetAccurate = getTestSet(dataset.subList(i, end), true);
                for (int j = 0; j < taggedTestSet.size(); j++) {
                    String[][] testSentence = taggedTestSet.get(j);
                    String[][] testSentenceAccurate = testSetAccurate.get(j);

                    wordCount += testSentence.length;   //Update word count
                    for (int k = 0; k < testSentence.length; k++) {
                        if (testSentence[k][1].equals(testSentenceAccurate[k][1])) {
                            correct++;                  //Increment number of correct tags
                        }
                    }
                }

                // Construct accuracy report output Strings
                double currentAccuracy = (double) correct / wordCount * 100;
                String iterationString = String.format("Iteration %,d:\nTest Range: %,d-%,d of %,d\n", i/1000, i, end, dataset.size());
                String accuracyString = String.format("Accuracy: %,d out of %,d words tagged correctly. (%%%.2f)\n\n", correct, wordCount, currentAccuracy);

                writer.write(iterationString);
                writer.write(accuracyString);

                // Update global accuracy information
                totalWords += wordCount;
                correctTags += correct;
                accuracy = (double) correctTags / totalWords * 100;
            }
            
            writer.write(String.format("Final Result:\nTotal Words: %,d\nTotal Correctly Tagged: %,d\nAccuracy: %%%.2f", totalWords, correctTags, accuracy));
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    // Adds tags to a test set List<String[][]>
    // Parameter testSet should be obtained from the getTestSet() method.
    // Parameter frequencyTable should be obtained from the buildFrequencyTable() method
    private static List<String[][]> tagTestSet(List<String[][]> testSet, HashMap<String, List<Pair>> frequencyTable) {
        List<String[][]> taggedTestSet = testSet;

        for (String[][] testSentence : taggedTestSet) {
            for (int j = 0; j < testSentence.length; j++) {
                String currentWord = testSentence[j][0];

                // Bigram
                if (j > 0) {
                    //Construct bigram key (PoS|Word) for frequency table lookup
                    String bigramKey = testSentence[j-1][1] + "|" + currentWord;

                    //Check if table contains bigram key
                    if (frequencyTable.containsKey(bigramKey)) {
                        //Find tag with highest frequency
                        Pair posMatch = new Pair("", 0);
                        for (Pair tag : frequencyTable.get(bigramKey)) {
                            if (tag.getValue() > posMatch.getValue()) posMatch = tag;   //Swap posMatch if number of occurences is higher
                        }

                        testSentence[j][1] = posMatch.getKey();     //Add PoS tag to our test set
                        continue;
                    }
                }

                // Unigram
                //Check if table contains unigram key
                if (frequencyTable.containsKey(currentWord)) {
                    //Find tag with highest frequency
                    Pair posMatch = new Pair("", 0);
                    for (Pair tag : frequencyTable.get(currentWord)) {
                        if (tag.getValue() > posMatch.getValue()) posMatch = tag;       //Swap posMatch if number of occurences is higher
                    }
                    testSentence[j][1] = posMatch.getKey();         //Add PoS tag to our test set
                }
                else {
                    //Default tag will be NN
                    testSentence[j][1] = "NN";
                }
            }
        }

        return taggedTestSet;
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
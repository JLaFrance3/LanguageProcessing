/*
 * Natural Language Processor
 * Authors: Ethan Kulawiak, Paul Zegarek, Jean LaFrance, Isaias Barreto
 * 
 */

package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
    
    public static void main(String args[]) {
        String path = "Training.txt";
        List<Sentence> dataset = readFile(path);

        HashMap<String, List<Pair>> posTagger = buildFrequencyTable(dataset);

        
        
        
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

    // Returns a 2D array of sentences and their words obtained from passed List<Sentence>
    // Parameter should be a sublist of 1000 elements from original dataset
    private static List<String>[] getTestSet(List<Sentence> testSentences) {
        List<String>[] testset = new ArrayList[testSentences.size()];

        // Reformat Sentence objects
        for (int i = 0; i < testSentences.size(); i++) {
            testset[i] = new ArrayList<String>();

            for (Word word : testSentences.get(i).getWords()) {
                testset[i].add(word.text());
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
                    String[] parts = pair.split("\\");
                    sentence.addWord(parts[0], parts[1]);
                }

                dataset.add(sentence);
                reader.close();
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return dataset;
    }
}
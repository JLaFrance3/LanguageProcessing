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
import java.util.regex.Pattern;

public class Main {
    
    public static void main(String args[]) {
        String path = "Training.txt";
        List<Sentence> dataset = readFile(path);

        HashMap<String, List<Pair<String, Integer>>> posTagger = new HashMap<String, List<Pair<String, Integer>>>();

        for (Sentence sentence : dataset) {
            for (int i = 0; i < sentence.getWords().size(); i++) {
                List<Word> words = sentence.getWords();
                StringBuilder keyBigram = new StringBuilder();

                // Add key:val for unigram tagger
                
            }
        }
        
        
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
                    String[] parts = pair.split("\\\\");
                }

                dataset.add(sentence);
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return dataset;
    }

    public class Pair<K, V> {
        private final K key;
        private V value;
    
        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
    
        public K getKey() {
            return key;
        }
    
        public V getValue() {
            return value;
        }
    }
}
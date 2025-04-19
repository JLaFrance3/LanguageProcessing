package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
    
    public static void main(String args[]) {
        String path = "Training.txt";
        List<Sentence> dataset = readFile(path);

        System.out.println(dataset.get(0).toString());
    }

    private static List<Sentence> readFile(String path) {
        List<Sentence> dataset = new ArrayList<>();     //The entire dataset in array

        try {
            //Create BufferedReader for data
            BufferedReader reader = new BufferedReader(new FileReader(path));

            String lineSentence;
            while((lineSentence = reader.readLine()) != null) {
                //Build Sentence object out of elements on the read line
                Sentence sentence = new Sentence();
                String[] pairs = lineSentence.split("\t");

                for (String pair : pairs) {
                    String[] parts = pair.split("\\\\");

                    // Do not include punctuation
                    //https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
                    if (!Pattern.matches("\\p{Punct}\\w*", parts[0])) {
                        sentence.addWord(parts[0], parts[1]);
                    }
                }

                dataset.add(sentence);
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return dataset;
    }
}
package src;

import java.util.HashMap;

public class Main {
    
    public static void main(String args[]) {
        String path = "ner_dataset2.csv";

        HashMap<String, HashMap<String, Integer>> map = readFile(path);
    }

    private static HashMap<String, HashMap<String, Integer>> readFile(String path) {
        //TODO: this
        return new HashMap<String, HashMap<String, Integer>>();
    }
}

package src;

// Sentence class builds list of Word records in a specific sentence

import java.util.ArrayList;
import java.util.List;

public class Sentence {
    private final List<Word> words = new ArrayList<>();

    public Sentence() {}

    public void addWord(String text, String posTag) {
        this.words.add(new Word(text, posTag));
    }

    public List<Word> getWords() {
        return words;
    }

    @Override
    public String toString() {
        StringBuilder sentenceString = new StringBuilder();

        for (Word word : words) {
            sentenceString
                .append(word.text())
                .append("\\")
                .append(word.posTag())
                .append(" ");
        }
        return sentenceString.toString();
    }
}

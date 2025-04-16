import csv

def convert_csv_to_txt(csv_path, test_path, train_path):
    sentences = {}
    current_sentence = []
    current_id = None

    with open(csv_path, encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        
        for row in reader:
            sentence_id = row.get('Sentence #') or current_id
            word = row.get('Word', "").strip()
            tag = row.get('POS', "").strip()

            if sentence_id != current_id:
                if current_id is not None:
                    sentences[current_id] = current_sentence
                current_id = sentence_id
                current_sentence = []

            current_sentence.append(f"{word}\\{tag}")

        # Add the last sentence
        if current_id and current_sentence:
            sentences[current_id] = current_sentence

    all_sentences = list(sentences.values())

    with open(test_path, 'w', encoding='utf-8') as f:
        for s in all_sentences[:1000]:
            f.write('\t'.join(s) + '\n')

    with open(train_path, 'w', encoding='utf-8') as f:
        for s in all_sentences[1000:]:
            f.write('\t'.join(s) + '\n')

    print(f"Done! Wrote {len(all_sentences[:1000])} test and {len(all_sentences[1000:])} training sentences.")

# Run it
convert_csv_to_txt("ner_dataset2.csv", "test.txt", "training.txt")
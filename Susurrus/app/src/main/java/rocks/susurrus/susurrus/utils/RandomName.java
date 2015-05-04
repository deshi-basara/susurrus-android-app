package rocks.susurrus.susurrus.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class for generating random user names.
 */
public class RandomName {

    private final String logIndicator = "utils/RandomName";

    /**
     * Lists
     */
    private List<String> nouns = new ArrayList<String>();
    private List<String> adjectives = new ArrayList<String>();
    private int nounsCount;
    private int adjectivesCount;

    /**
     * Class constructor.
     * Initiates word-lists
     * @param context Activity-context from which the object is created.
     */
    public RandomName(Context context) {
        Log.d(logIndicator, "RandomName()");

        AssetManager aManager = context.getAssets();

        // load available dictionaries
        try {
            loadDict("adjectives.txt", this.adjectives, aManager);
            loadDict("nouns.txt", this.nouns, aManager);
        } catch(IOException e) {
            e.printStackTrace();
            throw new Error(e);
        }

        // get size of word-lists
        this.nounsCount = this.nouns.size();
        this.adjectivesCount = this.adjectives.size();
    }

    /**
     * Picks random words form the dictionaries and builds a unique
     * name.
     * @return Generated username
     */
    public String generate() {
        Random rand = new Random();
        // pick random words
        String randNoun = this.nouns.get(rand.nextInt(this.nounsCount + 1));
        String randAdjective = this.adjectives.get(rand.nextInt(this.adjectivesCount + 1));

        // random adjective + noun-uppercase first letter + noun + random number
        String randName = randAdjective + randNoun.substring(0,1).toUpperCase() +
                randNoun.substring(1) + rand.nextInt(100 + 1);

        return randName;
    }

    /**
     * Parse a dictionary-file and adds every line to the handed List.
     * @param dictName Name of the dictionary in the assets-folder
     * @param list ArrayList for insertion
     * @param aManager Reference to the android-assetManager
     */
    private void loadDict(String dictName, List<String> list, AssetManager aManager) throws IOException {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(aManager.open("dict/" + dictName)));
        try {
            String line;
            while((line = reader.readLine()) != null) {
                list.add(line);
            }
        } finally {
            reader.close();
        }
    }
}

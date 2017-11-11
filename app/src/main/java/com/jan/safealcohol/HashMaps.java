package com.jan.safealcohol;


import android.widget.Button;

import java.util.HashMap;

public class HashMaps {

    private static HashMap<String, Float> levelMap = new HashMap<>();
    private static HashMap<String, Float> percentMap = new HashMap<>();
    private static HashMap<String, Float> amountMap = new HashMap<>();
    private static HashMap<String, String> picturesMap = new HashMap<>();
    private static HashMap<String, String> buttonDefault = new HashMap<>();
    private static HashMap<String, String> buttonPressed = new HashMap<>();

    // https://en.wikipedia.org/wiki/Drunk_driving_law_by_country

    /**
     *  Data in the HashMap below - needs to be multiplied by 10 --> To get units [g of alcohol]/[kg of blood]
     *
     */

    public static HashMap<String, Float> createLevelMap(){
        levelMap.put("Kyrgyzstan", 0.05f);
        levelMap.put("Mongolia", 0.05f);
        levelMap.put("Turkmenistan", 0f);
        levelMap.put("China", 0.02f);
        levelMap.put("Hong Kong", 0.05f);
        levelMap.put("Japan", 0.03f);
        levelMap.put("Republic of Korea", 0.05f);
        levelMap.put("Taiwan", 0.05f);
        levelMap.put("Afghanistan", 0f);
        levelMap.put("India", 0.03f);
        levelMap.put("Nepal", 0f);
        levelMap.put("Pakistan", 0f);
        levelMap.put("Sri Lanka", 0.06f);
        levelMap.put("Brunei", 0f);
        levelMap.put("Cambodia", 0.01f);
        levelMap.put("Laos", 0.08f);
        levelMap.put("Indonesia", 0f);
        levelMap.put("Malaysia", 0.08f);
        levelMap.put("Philippines", 0.05f);
        levelMap.put("Singapore", 0.08f);
        levelMap.put("Thailand", 0.05f);
        levelMap.put("Vietnam", 0.05f);
        levelMap.put("Turkey", 0.05f);
        levelMap.put("Armenia", 0.04f);
        levelMap.put("Iran", 0f);
        levelMap.put("Israel", 0.024f);
        levelMap.put("Jordan", 0.05f);
        levelMap.put("Kuwait", 0f);
        levelMap.put("Lebanon", 0.02f);
        levelMap.put("Saudi Arabia", 0f);
        levelMap.put("Syria", 0.08f);
        levelMap.put("UAE", 0f);
        levelMap.put("Angola", 0.06f);
        levelMap.put("Algeria", 0.02f);
        levelMap.put("Benin", 0.05f);
        levelMap.put("Cape Verde", 0.08f);
        levelMap.put("Central African Republic", 0.08f);
        levelMap.put("Comoros", 0f);
        levelMap.put("Congo", 0.01f);
        levelMap.put("Egypt", 0.05f);
        levelMap.put("Equatorial Guinea", 0.15f);
        levelMap.put("Eritrea", 0.03f);
        levelMap.put("Gambia", -1f);
        levelMap.put("Guinea-Bissau", 0.15f);
        levelMap.put("Kenya", -1f);
        levelMap.put("Libya", 0f);
        levelMap.put("Malawi", 0.08f);
        levelMap.put("Mauritius", 0.05f);
        levelMap.put("Morocco", 0f);
        levelMap.put("Namibia", 0.05f);
        levelMap.put("Niger", -1f);
        levelMap.put("Nigeria", 0.05f);
        levelMap.put("Seychelles", 0.08f);
        levelMap.put("South Africa", 0.05f);
        levelMap.put("Togo", -1f);
        levelMap.put("Uganda", 0.08f);
        levelMap.put("Tanzania", 0.08f);
        levelMap.put("Zambia", 0.08f);
        levelMap.put("Canada", 0.05f);
        levelMap.put("Mexico", 0.04f);
        levelMap.put("USA", 0.05f);
        levelMap.put("Bahamas", 0.06f);
        levelMap.put("Cuba", 0.05f);
        levelMap.put("Dominican Republic", 0.03f);
        levelMap.put("Jamaica", 0.08f);
        levelMap.put("Trinidad and Tobago", 0.08f);
        levelMap.put("Belize", 0.08f);
        levelMap.put("Costa Rica", 0.05f);
        levelMap.put("El Salvador", 0.05f);
        levelMap.put("Guatemala", 0.08f);
        levelMap.put("Honduras", 0.07f);
        levelMap.put("Nicaragua", 0.05f);
        levelMap.put("Panama", 0.08f);
        levelMap.put("Argentina", 0f);
        levelMap.put("Bolivia", 0.05f);
        levelMap.put("Brazil", 0f);
        levelMap.put("Chile", 0.08f);
        levelMap.put("Colombia", 0f);
        levelMap.put("Ecuador", 0.03f);
        levelMap.put("Guyana", 0.08f);
        levelMap.put("Paraguay", 0f);
        levelMap.put("Peru", 0.05f);
        levelMap.put("Suriname", 0.05f);
        levelMap.put("Uruguay", 0f);
        levelMap.put("Venezuela", 0.08f);
        levelMap.put("Albania", 0.01f);
        levelMap.put("Austria", 0.05f);
        levelMap.put("Belarus", 0.03f);
        levelMap.put("Bosnia and Herzegovina", 0.03f);
        levelMap.put("Bulgaria", 0.05f);
        levelMap.put("Czech Republic", 0f);
        levelMap.put("Croatia", 0.05f);
        levelMap.put("Cyprus", 0.05f);
        levelMap.put("Denmark", 0.05f);
        levelMap.put("Finland", 0.05f);
        levelMap.put("France", 0.05f);
        levelMap.put("Georgia", 0.02f);
        levelMap.put("Germany", 0.05f);
        levelMap.put("Gibraltar", 0.05f);
        levelMap.put("Greece", 0.05f);
        levelMap.put("Hungary", 0f);
        levelMap.put("Iceland", 0.04f);
        levelMap.put("Ireland", 0.05f);
        levelMap.put("Italy", 0.05f);
        levelMap.put("Latvia", 0.05f);
        levelMap.put("Lithuania", 0.04f);
        levelMap.put("Luxembourg", 0.02f);
        levelMap.put("Macedonia", 0.05f);
        levelMap.put("Moldova", 0.03f);
        levelMap.put("Malta", 0.08f);
        levelMap.put("Netherlands", 0.05f);
        levelMap.put("Norway", 0.02f);
        levelMap.put("Poland", 0.02f);
        levelMap.put("Portugal", 0.05f);
        levelMap.put("Romania", 0.02f);
        levelMap.put("Russian Federation", 0.03f);
        levelMap.put("Serbia", 0.03f);
        levelMap.put("Slovakia", 0f);
        levelMap.put("Slovenia", 0.05f);
        levelMap.put("Spain", 0.05f);
        levelMap.put("Switzerland", 0.05f);
        levelMap.put("Scotland", 0.05f);
        levelMap.put("England", 0.08f);
        levelMap.put("Wales", 0.08f);
        levelMap.put("Northern Ireland", 0.08f);
        levelMap.put("Ukraine", 0.02f);
        levelMap.put("Australia", 0.05f);
        levelMap.put("New Zealand", 0.05f);
        levelMap.put("French Polynesia", 0.05f);
        levelMap.put("Micronesia", 0.05f);

        return levelMap;

    }

    public static HashMap<String, Float> createPercentageMap(){
        percentMap.put("radlerButton", 2.5f);
        percentMap.put("beerButton", 5.0f);
        percentMap.put("wineButton", 12.0f);
        percentMap.put("liquorButton", 15.0f);
        percentMap.put("distilledButton", 40.0f);
        percentMap.put("customButton", 0.0f);

        return percentMap;
    }

    public static HashMap<String, Float> createAmountMap(){
        amountMap.put("radlerButton", 5f);
        amountMap.put("beerButton", 5f);
        amountMap.put("wineButton", 1f);
        amountMap.put("liquorButton", 0.5f);
        amountMap.put("distilledButton", 0.5f);
        amountMap.put("customButton", 0.0f);

        return amountMap;
    }

    public static HashMap<String, String> createPicturesMap(){
        picturesMap.put("Radler (2.5%)", "radler");
        picturesMap.put("Beer (5.0%)", "beer");
        picturesMap.put("Liquor (10%)", "liquor");
        picturesMap.put("Wine (12%)", "wine");
        picturesMap.put("Distilled spirit (40%)", "distilled");
        picturesMap.put("Custom", "custom");

        return picturesMap;
    }


    // TO UNCOMMENT - THE RIGHT HASH MAP !!!
    public static HashMap<String, String> createDBNamesMap(){
        buttonDefault.put("radlerButton", "Radler (2.5%)");
        buttonDefault.put("beerButton", "Beer (5.0%)");
        buttonDefault.put("liquorButton", "Liquor (15%)");
        buttonDefault.put("wineButton", "Wine (12%)");
        buttonDefault.put("distilledButton", "Distilled spirit (40%)");
        buttonDefault.put("customButton", "Custom drink");

        return buttonDefault;
    }

    public static HashMap<String, String> createButtonDefaultMap(){
        buttonDefault.put("radlerButton", "radler_default");
        buttonDefault.put("beerButton", "beer_default");
        buttonDefault.put("liquorButton", "liquor_default");
        buttonDefault.put("wineButton", "wine_default");
        buttonDefault.put("distilledButton", "distilled_default");
        buttonDefault.put("customButton", "custom_default");

        return buttonDefault;
    }

    // TO UNCOMMENT - THE RIGHT HASH MAP !!!
    public static HashMap<String, String> createButtonPressedMap(){
        buttonPressed.put("radlerButton", "radler_pressed");
        buttonPressed.put("beerButton", "beer_pressed");
        buttonPressed.put("liquorButton", "liquor_pressed");
        buttonPressed.put("wineButton", "wine_pressed");
        buttonPressed.put("distilledButton", "distilled_pressed");
        buttonPressed.put("customButton", "custom_pressed");

        return buttonPressed;
    }
}

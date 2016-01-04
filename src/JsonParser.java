import java.io.*;
import java.util.*;

// Created by Kevin Wei
// Multiple print lines/functions have been commented out and placed left align
// These were originally for personal debugging purposes

public class JsonParser {

    private static int braces = 0;

    private static int openBraces = 0;

    private static boolean checkBracesLeft = true;

    private static Map<String, Object> tempMap = new HashMap<String, Object>();

    private static String lastKey;

    private static Stack<String> keyStack = new Stack<String>();

    private static boolean inner = false;

    private static boolean firstTimeInserting = true;

    private static String masterKey = null;

    public static void reader(String fileLocation, List<String> jsonLines) {
        File file = new File(fileLocation);
        BufferedReader br = null;
        try {
            FileReader fr = new FileReader(file);
            br = new BufferedReader(fr);

            String line;
            while ((line = br.readLine()) != null) {
                //read and add each line to list of strings
                jsonLines.add(line);
                //System.out.println(jsonLines.get(i));
                //i++;
            }

        } catch (FileNotFoundException e) {
            System.out.println("Json file not found: " + file.toString());
        } catch (IOException e) {
            System.out.println("Unable to read Json file: " + file.toString());
        }
        try {
            br.close();
        } catch (IOException e) {
            System.out.println("Unable to close file: " + file.toString());
        } catch (NullPointerException ex) {
            // File was likely never opened
        }
    }

    public static void countBraces(String countMe)
    {
        for (int i = 0; i < countMe.length(); i++) // parse each char in each line of json
        {
            if (countMe.charAt(i) == '{')
            {
                openBraces++;
            }
            else
                continue;
        }
    }

    public static void getKeys (String keyMe, Stack<String> getKeys)
    {
        String key = null;
        int i;
        int spaceCounter = 0;
        for (i = 0; i < keyMe.length(); i++) // search for keys
        {
            if (openBraces-1 == getKeys.size() || keyMe.length()<=1)
                return;
            // line ends with { or :
            if (keyMe.charAt(keyMe.length() - 1) == '{' || keyMe.charAt(keyMe.length() - 1) == ':')
            {
                for (int j=0; j < keyMe.length(); j++)
                {
                    if (keyMe.charAt(j) == ' ')
                        spaceCounter++;
                }
                if (spaceCounter == keyMe.length()-1)
                    return;

                Scanner getKey = new Scanner(keyMe);
                getKey.useDelimiter("\"");
                key = getKey.next();
                while (key.charAt(0) == ' ' || key.charAt(0) == '{') {
                    key = getKey.next();
                }
//System.out.println("Pushing key: " + key);
                getKeys.push(key);
                break;
            }
            else
                continue;
        }
    }

    public static void parser(List<String> jsonLines, Map<String, Object> outputMap)
    {
        int bracesLeft = 0;
        for (int i = 0; i < jsonLines.size(); i++) {
            // Exit if empty string
            if (jsonLines.get(i).length() == 0)
                continue;
            String key = null, value;
            // Traverse to ith open brace
            for (int j = 0; j < (jsonLines.get(i)).length(); j++) {
                if ((jsonLines.get(i)).charAt(j) == '{' && checkBracesLeft && bracesLeft <= openBraces) {
                    bracesLeft++;
                    continue; //check next character
                }
                // Traversed to the ith open brace
                else if (bracesLeft == openBraces) {
                    // Only modify bracesLeft counter if need to decrement
                    checkBracesLeft = false;
                    // Add to a new map until encounter closed curly brace or previous key
                    // Not interested in spaces
                    if ((jsonLines.get(i)).charAt(j) == ' ') {
                        continue;
                    }
                    // Insert this map into popped key
                    else if ((jsonLines.get(i)).charAt(j) == '}' || (jsonLines.get(i)).charAt(((jsonLines.get(i)).length() - 1)) == ':') {
                        String lastKey = keyStack.pop();

                        if ((outputMap.get(lastKey) != null && !firstTimeInserting) || (tempMap.isEmpty())) {
//System.out.println("B) INSERTING outputMap into outputMap's key: " + lastKey);
//System.out.println("outputMap's value at " + lastKey + " is: " + outputMap.get(lastKey));
                            Map tempMapValue = new HashMap();
                            tempMapValue.putAll(tempMap);
                            Map outputMapValue = new HashMap();
                            outputMapValue.putAll(outputMap);

                            outputMap.clear();
                            outputMap.put(lastKey, outputMapValue);
//System.out.println("*** tempMap:");
//printMap(tempMap);
//System.out.println("*** outputMap:");
//printMap(outputMap);
                        }
                            // Add newly created map
                            Map mapValue = new HashMap();
                            mapValue.putAll(tempMap);
                            tempMap.clear(); //placement problem

//System.out.println("A) INSERTING tempMap into outputMap's key: " + lastKey);

                            if (!outputMap.containsKey(lastKey))
                            {
                                //put if outputMap does not contain interesting content
//System.out.println("outputMap contains nothing. do not fear being overwritten");
                                if (mapValue.size() == 1) {
                                    outputMap.put(lastKey, mapValue);
//System.out.println("*** tempMap:");
//printMap(mapValue);
//System.out.println("*** outputMap:");
//printMap(outputMap);
                                }
                                else{
                                    List<Object> outputList = new ArrayList<Object>(outputMap.values());
                                    outputList.clear();
                                    // add tempMap (aka mapValue) to outputList one at a time
                                    // get mapValue entries and add to outputList one at a time
                                    Set<String> mapValuekeys = mapValuekeys = mapValue.keySet();
                                    for (String s : mapValuekeys) {
                                        Map insertThisMap = new HashMap();
//System.out.println("Inserting key: " + s + " | value: " + mapValue.get(s) + " into outputList");
                                        insertThisMap.put(s, mapValue.get(s));
                                        outputList.add(insertThisMap);
                                    }
                                    // give back value to lastKey
                                    outputMap.put(lastKey, outputList);
//System.out.println("*** tempMap:");
//printMap(mapValue);
//System.out.println("*** outputMap:");
//printMap(outputMap);
                                }
                            }
                            else
                            {
                                // make sure interesting outputMap content does not get overwritten
//System.out.println("outputMap contains " + lastKey + " do not overwrite!");
                                // populate outputList list with existing values
                                List<Object> outputList = new ArrayList<Object>(outputMap.values());
                                // add tempMap (aka mapValue) to outputList one at a time
                                //outputList.add(mapValue);
                                // get mapValue entries and add to outputList one at a time
                                Set<String> mapValuekeys = mapValue.keySet();

                                for (String s : mapValuekeys) {
                                    Map insertThisMap = new HashMap();
//System.out.println("Inserting key: " + s + " | value: " + mapValue.get(s) + " into outputList");
                                    insertThisMap.put(s, mapValue.get(s));
                                    outputList.add(insertThisMap);
                                }
                                // give back value to lastKey
                                outputMap.put(lastKey, outputList);
//System.out.println("*** tempMap:");
//printMap(mapValue);
//System.out.println("*** outputMap:");
//printMap(outputMap);
                            }
                        firstTimeInserting = false;
                        openBraces--;
                        checkBracesLeft = true;
                        if (openBraces == 1) {
                            if (outputMap.size() > 1) {
//System.out.println("PLACING REMAINING OUTPUT MAP IN: " + lastKey);

//System.out.println("*** tempMap (before):");
//printMap(mapValue);
//System.out.println("*** outputMap (before):");
//printMap(outputMap);

                                Map outputMapValue = new HashMap();
                                outputMapValue.putAll(outputMap);

                                // populate outputList list with existing values
                                List<Object> outputList = new ArrayList<Object>();

//for (int a = 0; a < outputList.size(); a++)
//System.out.println("The "+a+ " entry in outputList is: "+outputList.get(a));
//System.out.println();
                                // get outputMapValue entries and add to outputList one at a time
                                Set<String> mapValuekeys = outputMapValue.keySet();

                                for (String s : mapValuekeys) {
                                    Map insertThisMap = new HashMap();
                                    if (s == lastKey)
                                    {
                                        Object insertionKey = outputMapValue.get(s);
                                        HashMap<String, String> map = (HashMap<String, String>) insertionKey;
//System.out.println("2) Inserting map into outputList: " + map);
                                        outputList.add(map);
                                        for (int a = 0; a < outputList.size(); a++) {
//System.out.println("The "+a+ " entry in outputList is: "+outputList.get(a));
                                        }
//System.out.println();
                                    }
                                    else {
                                        insertThisMap.put(s, outputMapValue.get(s));
//System.out.println("1) Inserting map into outputList: " + insertThisMap);
                                        outputList.add(insertThisMap);
                                        for (int a = 0; a < outputList.size(); a++) {
//System.out.println("The "+a+ " entry in outputList is: "+outputList.get(a));
                                        }
//System.out.println();
                                    }
                                }
                                // give back value to lastKey
                                outputMap.clear();
                                outputMap.put(lastKey, outputList);
//System.out.println("*** tempMap (after):");
//printMap(mapValue);
//System.out.println("*** outputMap (after):");
//printMap(outputMap);
                            }
                            return;
                        }
                        else {
                            parser(jsonLines, outputMap);
                        }
                    }
                    // Get Key
                    else if ((jsonLines.get(i)).charAt(j) == '\"') {
                        Scanner getKey = new Scanner((jsonLines.get(i)));
                        getKey.useDelimiter("\"");
                        key = getKey.next();
                        while (key.charAt(0) == ' ') {
                            key = getKey.next();
                        }
                        if (key.charAt(0) == '{')
                        {
                            firstTimeInserting = false;
                            openBraces--;
                            checkBracesLeft = true;
                            parser(jsonLines, outputMap);
                            break;
                        }
                        j = j + key.length() + 1;
                        continue;
                    }
                    // Get value
                    else if ((jsonLines.get(i)).charAt(j) == ':') {
                        Scanner getVal = new Scanner((jsonLines.get(i)));
                        getVal.useDelimiter("\"");
                        value = getVal.next();
                        // This will be the key
                        while (((String) value).charAt(0) == ' ') {
                            value = getVal.next();
                        }
                        value = getVal.next();
                        // Get the value
                        // Get rid of the ": "
                        if (((String) value).charAt(0) == ':' && ((String) value).length() == 2 && ((String) value).charAt(1) == ' ') {
                            while (((String) value).charAt(0) == ':') {
                                value = getVal.next();
                            }
                        } else {
                            value = ((String) value).substring(2);
                        }
                        // Remove possible comma, indicating more data
                        int possibleComma = (((String) value).length()) - 1;
                        if (((String) value).charAt(possibleComma) == ',') {
                            value = ((String) value).substring(0, possibleComma);
                        }
                        // If value is a {
                        if (value.charAt(0) == '{')
                        {
                            if (bracesLeft == 1) {
//System.out.println("======PLACING REMAINING OUTPUT MAP IN: " + key);

//System.out.println("*** tempMap (before):");
//printMap(tempMap);
//System.out.println("*** outputMap (before):");
//printMap(outputMap);

                                Map outputMapValue = new HashMap();
                                outputMapValue.putAll(outputMap);

                                // populate outputList list with existing values
                                List<Object> outputList = new ArrayList<Object>();

                                for (int a = 0; a < outputList.size(); a++) {
//System.out.println("The " + a + " entry in outputList is: " + outputList.get(a));
                                }
//System.out.println();
                                // get outputMapValue entries and add to outputList one at a time
                                Set<String> mapValuekeys = outputMapValue.keySet();

                                for (String s : mapValuekeys) {
                                    Map insertThisMap = new HashMap();
                                    if (s == key) {
                                        Object insertionKey = outputMapValue.get(s);
                                        HashMap<String, String> map = (HashMap<String, String>) insertionKey;
//System.out.println("2) Inserting map into outputList: " + map);
                                        outputList.add(map);
                                        for (int a = 0; a < outputList.size(); a++) {
//System.out.println("The " + a + " entry in outputList is: " + outputList.get(a));
                                        }
//System.out.println();
                                    } else {
                                        insertThisMap.put(s, outputMapValue.get(s));
//System.out.println("1) Inserting map into outputList: " + insertThisMap);
                                        outputList.add(insertThisMap);
                                        for (int a = 0; a < outputList.size(); a++) {
//System.out.println("The " + a + " entry in outputList is: " + outputList.get(a));
                                        }
//System.out.println();
                                    }
                                }
                                // give back value to key
                                outputMap.clear();
                                outputMap.put(key, outputList);
//System.out.println("*** tempMap (after):");
//printMap(tempMap);
//System.out.println("*** outputMap (after):");
//printMap(outputMap);
                                return;
                            }

                                //put tempmap in outputmap
                                List<Object> outputList = new ArrayList<Object>();
                                Set<String> tempMapKeys = tempMap.keySet();
                                for (String s : tempMapKeys) {
                                    outputMap.put(s, tempMap.get(s));
                                }
//System.out.println("*** tempMap:");
//printMap(tempMap);
//System.out.println("*** outputMap:");
//printMap(outputMap);

                            firstTimeInserting = false;
                            openBraces--;
                            checkBracesLeft = true;
                            parser(jsonLines, outputMap);
                            break;
                        }
                        else {
                            // Insert into map
//System.out.println("Inserting Key: " + key + " | Value: " + value);
                            tempMap.put(key, value);
//System.out.println("*** tempMap:");
//printMap(tempMap);
//System.out.println("*** outputMap:");
//printMap(outputMap);
                            break;
                        }
                    }
                } else
                    continue;
            }
        }
    }

    public static void printMap(Map<String, Object> parsedMap){
        for (String name : parsedMap.keySet())
        {
            String key = name.toString();
            String testValue = parsedMap.get(name).toString();
            System.out.println("Key: " + key + " | Value: " + testValue);
        }
        System.out.println();
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter the JSON file to parse (For testing purposes -- parseMe.json): "); //user input: parseMe.json
        String fileName = input.nextLine();
        //String fileName = "parseMe.json";
        List<String> jsonLines = new ArrayList<>();
        reader(fileName, jsonLines);
        System.out.println();

        // The following was for testing purposes only:
        //jsonLines.add("{");
        //jsonLines.add("  \"debug\": \"on\",");
        //jsonLines.add("  \"window\": {");
        //jsonLines.add("  {");
        //jsonLines.add("    \"title\": \"sample\",");
        //jsonLines.add("    \"size\": 500");
        //jsonLines.add("  }");
        //jsonLines.add("}");

        // Count open braces
        for (int i = 0; i < jsonLines.size(); i++) //count open braces
        {
            countBraces(jsonLines.get(i));
        }
        //System.out.println("There are " + openBraces + " open braces.");

        // Get keys
        for (int i = 0; i < jsonLines.size(); i++) //count open braces
        {
            getKeys (jsonLines.get(i), keyStack);
        }
        //while (!keyStack.isEmpty()) {
        //    String key = keyStack.pop();
        //    System.out.println("Keys: " + key);
        //}
        //System.out.println();

        // Store parsed results in parsedMap
        Map<String, Object> parsedMap = new HashMap<String, Object>();
        // Parse the json
        parser(jsonLines, parsedMap);
        System.out.println();

        // Print everything in parsedMap
        System.out.println("***** PRINTING ALL CONTENT OF parsedMAP *****");
        printMap(parsedMap);

        System.out.println("The above single-line print statement confirms the json file has been parsed.");
        System.out.println();

        // Test the parsed map
        Object key1 = parsedMap.get("widget");
        List<Object> parsedList = (List<Object>) key1;
        // parsedList is now the value of widget
        //System.out.println("Printing all values in parsedMap's widget key:");
        //for (int a = 0; a < parsedList.size(); a++)
        //    System.out.println("Entry "+a+ " in parsedList is: "+ parsedList.get(a));
        //System.out.println();

        System.out.println("Accessing some random elements of the parsed map:");
        //System.out.println();

        // text's onMouseUp
        Object tlm = parsedList.get(2);
        HashMap<String, Object> textListMap = (HashMap<String, Object>) tlm;
        Object tl = textListMap.get("text");
        List<Map> textList = (List<Map>) tl;
        //System.out.println("Printing all values in textList: " + textList);
        Object im = textList.get(6);
        HashMap<String, Integer> onMouseUpMap = (HashMap<String, Integer>) im;
        System.out.println("SUCCESS! widget's text's key \"onMouseUp\" has value: " + onMouseUpMap.get("onMouseUp"));
        //System.out.println();

        // image's hOffset
        Object ilm = parsedList.get(0);
        HashMap<String, Object> imageListMap = (HashMap<String, Object>) ilm;
        Object il = imageListMap.get("image");
        List<Map> imageList = (List<Map>) il;
        //System.out.println("Printing all values in imageList: " + imageList);
        Object hm = imageList.get(4);
        HashMap<String, Integer> hOffsetMap = (HashMap<String, Integer>) hm;
        System.out.println("SUCCESS! widget's image's key \"hOffset\" has value: " + hOffsetMap.get("hOffset"));
        //System.out.println();

        // window's title
        Object wlm = parsedList.get(3);
        HashMap<String, Object> windowListMap = (HashMap<String, Object>) wlm;
        Object wl = windowListMap.get("window");
        List<Map> windowList = (List<Map>) wl;
        //System.out.println("Printing all values in windowList: " +windowList);
        Object tm = windowList.get(2);
        HashMap<String, String> titleMap = (HashMap<String, String>) tm;
        System.out.println("SUCCESS! widget's window's key \"title\" has value: " + titleMap.get("title"));
        assert titleMap.get("title").equals("Sample Konfabulator Widget") : "Error 2";
        //System.out.println();

        // debug on
        Map<String, String> debugOn = new HashMap<String, String>();
        debugOn.put("debug", "on");
        if (parsedList.get(1).equals(debugOn))
            System.out.println("SUCCESS! widget's \"debug\" has value: \"on\"");
        assert parsedList.get(1).equals(debugOn) : "Error 1";
        System.out.println();

/*
        //===parseMe.json===

        System.out.println();
        assert parsedMap.get("debug").equals("on") : "Error 1";
        System.out.println("Value of \"debug\" is: " + parsedMap.get("debug"));
        Object key1 = parsedMap.get("window");
        HashMap<String, String> map = (HashMap<String, String>) key1;
        Object value = map.get("title");
        System.out.println("Value of \"title\" is: " + value);
        value = map.get("size");
        System.out.println("Value of \"size\" is: " + value);

        System.out.println();
        System.out.println("Printing all contents of inner map:");
        for (String name : map.keySet())
        {
            String key = name.toString();
            String value2 = map.get(name).toString();
            System.out.println("Key: " + key + " | Value: " + value2);
        }
*/
    }
}
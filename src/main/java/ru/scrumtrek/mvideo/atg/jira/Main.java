package ru.scrumtrek.mvideo.atg.jira;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Created by Δενθρ on 02.12.2015.
 */
public class Main {
    //Delimiter used in CSV file
    private static final String COMMA_DELIMITER = ";";
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static DateFormat javaFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static DateFormat excelFormat = new SimpleDateFormat("dd.MM.yy' 'HH:mm");

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        JSONParser parser = new JSONParser();

        FileWriter writer = null;
        try {
            File outFile = new File("resultR7_15.csv");

            // This will output the full path where the file will be written to...
            System.out.println(outFile.getCanonicalPath());

            writer = new FileWriter(outFile);

            Object obj = parser.parse(new FileReader(
                    "e:\\projects\\Jira\\search_R7_15"));

            JSONObject jsonObject = (JSONObject) obj;

            JSONArray issues = (JSONArray) jsonObject.get("issues");

            Iterator<JSONObject> issueIterator = issues.iterator();
            while (issueIterator.hasNext()) {
                JSONObject issue = issueIterator.next();
//                String started = ((JSONObject) issue.get("fields")).get("created").toString();
//                String resolved = started;
                String started = null;
                String resolved = null;
                String closed = null;

                JSONObject changelog = (JSONObject) issue.get("changelog");
                JSONArray histories = (JSONArray) changelog.get("histories");
                Collections.sort(histories, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        return ((String) ((JSONObject) o1).get("created")).compareTo(
                                (String) ((JSONObject) o2).get("created"));
                    }
                });
                Iterator<JSONObject> historyIterator = histories.iterator();
                while (historyIterator.hasNext()) {
                    JSONObject hystory = historyIterator.next();
                    String historyCreated = (String) hystory.get("created");
                    Iterator<JSONObject> itemIterator = ((JSONArray) hystory.get("items")).iterator();
                    while (itemIterator.hasNext()) {
                        JSONObject item = itemIterator.next();
                        //System.out.println(historyCreated);
                        if (item.get("field").equals("status")) {
                            // We need the first date
                            if (started == null && item.get("toString").equals("In Progress")) {
                                started = historyCreated;
                            } else if (item.get("toString").equals("Resolved")) {
                                resolved = historyCreated; // We need the last date
                            } else if (item.get("toString").equals("Closed")) {
                                closed = historyCreated; // We need the last date
                            }
                        }
                    }
                }
                if (started != null && (resolved != null || closed != null)) {
                    if (resolved == null ||
                            resolved != null && closed != null && started.compareTo(resolved) == 1) {
                        resolved = closed;
                    }
                    writer.append(issue.get("id").toString());
                    writer.append(COMMA_DELIMITER);
                    writer.append(issue.get("key").toString());
                    writer.append(COMMA_DELIMITER);
                    JSONObject issueType = (JSONObject) ((JSONObject) issue.get("fields")).get("issuetype");
                    writer.append(issueType.get("id").toString());
                    writer.append(COMMA_DELIMITER);
                    writer.append(issueType.get("name").toString());
                    writer.append(COMMA_DELIMITER);
                    writer.append(issueType.get("description").toString());
                    writer.append(COMMA_DELIMITER);
                    writer.append(formatDate(started));
                    writer.append(COMMA_DELIMITER);
                    writer.append(formatDate(resolved));
                    writer.append(COMMA_DELIMITER);
                    //System.out.println();
                    writer.append(NEW_LINE_SEPARATOR);
                }
            }
            //writer.write("Hello world!");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    private static String formatDate(String started) throws ParseException {
        return String.valueOf(excelFormat.format(javaFormat.parse(started)));
    }
}

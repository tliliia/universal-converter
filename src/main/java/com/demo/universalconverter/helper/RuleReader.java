package com.demo.universalconverter.helper;

import com.demo.universalconverter.UniversalConverterApplication;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Component;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;

@Component
public class RuleReader {

    private Map<String, LinkedList<PairWithRatio>> rules;
    private String rulesCSV = UniversalConverterApplication.filePath;

    public RuleReader() {
        rules = new HashMap<>();
        readRules();
    }

    public Map<String, LinkedList<PairWithRatio>> readRules()  {
        if (rules.size() == 0) {
            rules = createFromFile(new File(rulesCSV));
        }
        return rules;
    }

    public boolean existRulesForKeys(Collection keys){
        return rules.keySet().containsAll(keys);
    }

    public List<PairWithRatio> getRulesForUnit(String unit){
        if (rules.containsKey(unit)){
            return rules.get(unit);
        }
        return null;
    }

    public boolean isLoaded(){
        return rules.size() == 0;
    }

    private Map<String, LinkedList<PairWithRatio>> createFromFile(File fileName) {
        try (CSVReader csvReader = new CSVReader(new java.io.FileReader(fileName, Charset.forName("UTF-8")))) {
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                if (row.length < 3)
                    throw new IllegalArgumentException();

                PairWithRatio newPair = new PairWithRatio(row[0], row[1], new BigDecimal(row[2]));
                if (rules.containsKey(row[0])) {
                    List<PairWithRatio> temp = rules.get(row[0]);
                    temp.add(newPair);
                } else {
                    LinkedList<PairWithRatio> pairs = new LinkedList();
                    pairs.add(newPair);
                    rules.put(row[0], pairs);
                }

                if (rules.containsKey(row[1])) {
                    List<PairWithRatio> temp = rules.get(row[1]);
                    temp.add(newPair);
                } else {
                    LinkedList<PairWithRatio> pairs = new LinkedList();
                    pairs.add(newPair);
                    rules.put(row[1], pairs);
                }
            }
        } catch (CsvException | IOException e) {
            e.printStackTrace();
        }
        return rules;
    }
}

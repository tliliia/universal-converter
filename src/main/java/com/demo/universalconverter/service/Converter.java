package com.demo.universalconverter.service;

import com.demo.universalconverter.helper.PairWithRatio;
import com.demo.universalconverter.helper.RuleReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class Converter {

@Autowired
    private RuleReader ruleReader;
    static MathContext ctx = new MathContext(15, RoundingMode.UP);
    static String notFoundString = "Используются неизвестные единицы измерения";
    static String badRequestString = "Невозможно осуществить такое преобразование";

    public ResponseEntity convert(String from, String to) {

        AbstractMap.SimpleEntry<List<String>, List<String>> p = prepare(from, to);
        if (p == null) {
            return  new ResponseEntity<>(badRequestString, HttpStatus.BAD_REQUEST);
        }

        List<String> numerator = p.getKey();
        List<String> denumerator = p.getValue();
        //разное число единиц измерений
        if (numerator.size() != denumerator.size()) {
            return new ResponseEntity<>(badRequestString, HttpStatus.BAD_REQUEST);
        }
        // убрать числовые коэффициенты
        numerator = numerator.stream().filter(s -> !"1".equals(s)).collect(Collectors.toList());
        denumerator = denumerator.stream().filter(s -> !"1".equals(s)).collect(Collectors.toList());

        //величины нет в файле правил
        if (!ruleReader.existRulesForKeys(numerator) && !ruleReader.existRulesForKeys(denumerator)) {
            return new ResponseEntity<>(notFoundString, HttpStatus.BAD_REQUEST);
        }

        BigDecimal coef = new BigDecimal(1);
        Iterator<String> numIter = numerator.iterator();
        while (numIter.hasNext()) {
            String source = numIter.next();//м.б.числа

            Iterator<String> denumIter = denumerator.iterator();
            while (denumIter.hasNext()) {
                String target = denumIter.next();
                if (source.equals(target)) {
                    numIter.remove();
                    denumIter.remove();
                } else {
                    BigDecimal ratio = findRatio(source, target);
                    if (ratio != null) {
                        coef = coef.multiply(ratio);
                        numIter.remove();
                        denumIter.remove();
                    }
                }
            }
        }
        if (numerator.size() > 0) {//остался коэффициент
            try {
                double d = Double.parseDouble(numerator.get(0));
                coef = coef.multiply(new BigDecimal(d));
                numIter.remove();
            } catch (NumberFormatException e) {

            }
        }
        if (denumerator.size() > 0) {//остался коэффициент
            try {
                double d = Double.parseDouble(denumerator.get(0));
                coef = coef.multiply(new BigDecimal(d));
                numIter.remove();
            } catch (NumberFormatException e) {

            }
        }
        if (numerator.size() == 0 && denumerator.size() == 0) {//дробь сократилась
            return new ResponseEntity<>(with15DecimalPlaces(coef), HttpStatus.OK);
        }
        return new ResponseEntity<>(badRequestString, HttpStatus.NOT_FOUND);
    }

    private String with15DecimalPlaces(BigDecimal val) {
        DecimalFormatSymbols  dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.###############");
        df.setDecimalFormatSymbols(dfs);
        df.setDecimalSeparatorAlwaysShown(true);
        return df.format(val);
    }

    /*
     *  метод переводит строки from, to вида a/b = c/d к одной дроби, ad/cb.
     *  Возвращает список размерностей числителя и знаменателя
     * */
    private AbstractMap.SimpleEntry<List<String>, List<String>> prepare(String from, String to) {
        from = from.replaceAll("\\s+", "");
        to = to.replaceAll("\\s+", "");

        if (from.length() == 0 && to.length() ==0) {
            return null;
        }
        List<String> fromUnits = Arrays.asList(from.split("/"));
        List<String> toUnits = Arrays.asList(to.split("/"));

        if (fromUnits.size() > 2 || toUnits.size() >  2){//неверная запись дроби
            return null;
        }

        if (fromUnits.get(0).length() == 0 || toUnits.get(0).length() == 0) {//пустой числитель
            return null;
        }

        List<String> jointNumerator = new LinkedList();
        List<String> jointDenumerator = new LinkedList();

        if (fromUnits.size() == 2) {//дробь
            jointDenumerator.addAll(Arrays.asList(fromUnits.get(1).split("\\*")));
        }
        List<String> f = Arrays.asList(fromUnits.get(0).split("\\*"));
        jointNumerator.addAll(f);

        if (toUnits.size() == 2) {
            jointNumerator.addAll(Arrays.asList(toUnits.get(1).split("\\*")));
        }
        jointDenumerator.addAll(Arrays.asList(toUnits.get(0).split("\\*")));

        jointNumerator = jointNumerator.stream().filter(s -> s.length() > 0).collect(Collectors.toList());
        jointDenumerator = jointDenumerator.stream().filter(s -> s.length() > 0).collect(Collectors.toList());
        return new AbstractMap.SimpleEntry<List<String>, List<String>>(jointNumerator, jointDenumerator);
    }

    /*
     * Метод возвращает коэффициент преобразования величины s в t. Если преобразование невозможно вернет null
     * */
    private BigDecimal findRatio(String s, String t) {
        AbstractMap.SimpleEntry<Map<PairWithRatio, Short>, Short> wayWithLabel = findWaveBFSWay(s, t);

        if (wayWithLabel != null) {
            Map<PairWithRatio, Short> visited = wayWithLabel.getKey();
            Short label = wayWithLabel.getValue();

            String tempUnit = t;
            BigDecimal ratio = new BigDecimal(1);
            while (!tempUnit.equals(s)) {
                List<PairWithRatio> adjUnits = ruleReader.getRulesForUnit(tempUnit);
                if (adjUnits != null) {
                    for (PairWithRatio u : adjUnits) {
                        if (visited.containsKey(u) && visited.get(u) == label) {
                            ratio = ratio.multiply(u.getRatioForSource(tempUnit), ctx);
                            tempUnit = u.getAnotherUnit(tempUnit);
                            label--;
                        }
                    }
                }
            }
            return ratio;
        }
        return null;
    }

    /*
     * обход в ширину соседних величин для поиска t  с отметкой удаленности от стартовой вершины s
     * если t недостижима из s вернем null
     * */
    private AbstractMap.SimpleEntry<Map<PairWithRatio, Short>, Short> findWaveBFSWay(String s, String t) {
        Map<PairWithRatio, Short> visited = new HashMap();
        short label = -1;
        LinkedList<String> newFront = new LinkedList();
        newFront.add(s);
        LinkedList<String> oldFront;

        while (newFront.size() != 0) {
            oldFront = newFront;
            newFront = new LinkedList();
            label++;
            while (oldFront.size() != 0) {
                String unit = oldFront.poll();
                List<PairWithRatio> adjUnits = ruleReader.getRulesForUnit(unit);
                if (adjUnits != null) {
                    for (PairWithRatio u : adjUnits) {
                        if (visited.get(u) == null) {
                            visited.put(u, label);
                            newFront.add(u.getAnotherUnit(unit));
                        }
                        if (u.hasEntry(t)) {
                            return new AbstractMap.SimpleEntry<Map<PairWithRatio, Short>, Short>(visited, label);
                        }
                    }
                }
            }
        }
        //не нашли перехода, иначе вышли бы раньше
        return null;
    }
}

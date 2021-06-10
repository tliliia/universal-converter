package com.demo.universalconverter.helper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class PairWithRatio {
    public String source;
    public String target;
    public BigDecimal ratio;
    static MathContext ctx = new MathContext(15, RoundingMode.UP);

    PairWithRatio(String s, String t, BigDecimal v) {
        source = s;
        target = t;
        ratio = v;
    }

    public BigDecimal getRatioForSource(String s) {
        if (s.equals(source)) {
            return new BigDecimal(1).divide(ratio, ctx);
        }
        return ratio;
    }

    public String getAnotherUnit(String s) {
        if (s.equals(source)) {
            return target;
        }
        return source;
    }

    public boolean hasEntry(String s) {
        return s.equals(source) || s.equals(target);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PairWithRatio that = (PairWithRatio) o;
        return source.equals(that.source) && target.equals(that.target) ||
                source.equals(that.target) && target.equals(that.source);
    }

    @Override
    public int hashCode() {
        return source.hashCode() + target.hashCode();
    }

    @Override
    public String toString() {
        return "(" + source + "/" + target + ")";
    }
}

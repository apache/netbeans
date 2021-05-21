package org.netbeans.test.java.hints;

import java.awt.Color;
import java.math.BigInteger;

public enum Test201360 {
    ONE,TWO,THREE,FOUR;
    public Color getColor(final int number) {
        return getColor();
    }
    public Color getColor(final int number, final Color failedColor) {
        return BigInteger.valueOf(number).isProbablePrime(20) ? Color.GREEN : failedColor;
    }
}
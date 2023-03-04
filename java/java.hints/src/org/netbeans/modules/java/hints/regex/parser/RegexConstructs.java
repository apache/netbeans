/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.hints.regex.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 *
 * @author SANDEEMI
 */
public class RegexConstructs {

    public static abstract class RegEx {

    }

    public static class AnyChar extends RegEx {

    }

    public static class Blank extends RegEx {

    }

    public static class CharClass extends RegEx {

        private final boolean isNegation;
        private final boolean isIntersection;
        private final List<RegEx> charClassList;
        private final List<Character> allChars;
        private final List<Character> negationChars;

        public CharClass(boolean isNegation, boolean isIntersection) {
            this.isNegation = isNegation;
            this.isIntersection = isIntersection;
            this.charClassList = new ArrayList<>();
            allChars = new ArrayList<>();
            negationChars = new ArrayList<>();
            for (int c = 32; c <= 126; c++) {
                negationChars.add((char) c);
            }
        }

        public void addToClass(RegEx... nextChar) {
            for (RegEx next : nextChar) {
                this.charClassList.add(next);
                if (next instanceof Primitive) {
                    allChars.add((Character) ((Primitive) next).getCh());
                    negationChars.remove((Character) ((Primitive) next).getCh());
                } else if (next instanceof Range) {
                    char from = ((Range) next).getFrom();
                    char to = ((Range) next).getTo();
                    for (char c = from; c <= to; c++) {
                        allChars.add((Character) c);
                        negationChars.remove((Character) c);
                    }
                } else if (next instanceof CharClass) {
                    CharClass charClass = (CharClass) next;
                    if (charClass.isIntersection) {
                        handleIntersection(charClass);
                    } else {
                        handleUnion(charClass);
                    }
                }
            }
        }

        private void handleUnion(CharClass charClass) {
            List<Character> allCharInt;
            if (charClass.isNegation()) {
                allCharInt = charClass.getNegationList();
            } else {
                allCharInt = charClass.getAllChar();
            }
            ListIterator<Character> listIterator = allCharInt.listIterator();
            while (listIterator.hasNext()) {
                Character c = listIterator.next();
                if (!allChars.contains(c)) {
                    allChars.add(c);
                    negationChars.remove(c);
                }
            }
        }

        private void handleIntersection(CharClass charClass) {

            List<Character> allCharInt;
            if (charClass.isNegation()) {
                allCharInt = charClass.getNegationList();
            } else {
                allCharInt = charClass.getAllChar();
            }
            ListIterator<Character> listIterator = allChars.listIterator();
            while (listIterator.hasNext()) {
                Character c = listIterator.next();
                if (!allCharInt.contains(c)) {
                    listIterator.remove();
                    negationChars.add(c);
                }
            }
        }

        public boolean isNegation() {
            return isNegation;
        }

        public List<RegEx> getCharClassList() {
            return charClassList;
        }

        public List<Character> getAllChar() {
            return allChars;
        }

        public List<Character> getNegationList() {
            return negationChars;
        }
    }

    public static class CapturingGroup extends Group {

        private int groupNo;

        public CapturingGroup(int groupNo, RegEx regex, RegExParser regExParser) {
            super(regex);
            this.groupNo = groupNo;
            regExParser.getGroupMap().put(groupNo, regex);
        }

        public int getGroupNo() {
            return groupNo;
        }

        public void setGroupNo(int groupNo) {
            this.groupNo = groupNo;
        }
    }

    public static class Choice extends RegEx {

        private ArrayList<RegEx> choice;

        public Choice(ArrayList<RegEx> choice) {
            this.choice = choice;
        }

        public static Choice subChoices(RegEx... regex) {
            ArrayList<RegEx> alternatives = new ArrayList<>();
            for (RegEx r : regex) {
                if (r instanceof Choice) {
                    alternatives.addAll(((Choice) r).getChoice());
                } else {
                    alternatives.add(r);
                }
            }

            return new Choice(alternatives);
        }

        public ArrayList<RegEx> getChoice() {
            return choice;
        }

        public void setChoice(ArrayList<RegEx> choice) {
            this.choice = choice;
        }
    }

    public static class Concat extends RegEx {

        private ArrayList<RegEx> concat;

        public ArrayList<RegEx> getConcat() {
            return concat;
        }

        public void setConcat(ArrayList<RegEx> concat) {
            this.concat = concat;
        }

        public Concat(ArrayList<RegEx> seq) {
            this.concat = seq;
        }
    }

    public static class GreedyBound extends RegEx {

        private final RegEx internal;
        private int min;
        private boolean maxPresent;
        private int max;

        public boolean isMaxPresent() {
            return maxPresent;
        }

        public void setMaxPresent(boolean maxPresent) {
            this.maxPresent = maxPresent;
        }

        public GreedyBound(RegEx base) {
            this.internal = base;
        }

        public int getMin() {
            return min;
        }

        public void setMin(int min) {
            this.min = min;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public RegEx getInternal() {
            return internal;
        }

    }

    public static class Group extends RegEx {

        private RegEx internal;

        public Group(RegEx regex) {
            this.internal = regex;
        }

        public RegEx getInternal() {
            return internal;
        }

        public void setInternal(RegEx internal) {
            this.internal = internal;
        }

    }

    public static enum GroupType {
        POSITIVE_LOOKAHEAD,
        NEGATIVE_LOOKAHEAD,
        POSITIVE_LOOKBEHIND,
        NEGATIVE_LOOKBEHIND,
        CAPTURE_GROUP,
        NON_CAPTURE_GROUP;

        public boolean isNegative() {
            return this == NEGATIVE_LOOKAHEAD || this == NEGATIVE_LOOKBEHIND;
        }

        public boolean isPositive() {
            return this == POSITIVE_LOOKAHEAD || this == POSITIVE_LOOKBEHIND;
        }
    }

    public static class NamedGroup extends CapturingGroup {

        private String groupName;

        public NamedGroup(int groupNo, String groupName, RegEx regex, RegExParser regExParser) {
            super(groupNo, regex, regExParser);
            this.groupName = groupName;
            regExParser.getNamedGroupMap().put(groupName, regex);
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

    }

    public static class OneOrMore extends RegEx {

        private RegEx internal;

        public OneOrMore(RegEx base) {
            this.internal = base;
        }

        public RegEx getInternal() {
            return internal;
        }
    }

    public static class Optional extends RegEx {

        private RegEx internal;

        public Optional(RegEx base) {
            this.internal = base;
        }

        public RegEx getInternal() {
            return internal;
        }

    }

    public static class Posix extends CharClass {

        private String posixName;

        public Posix(String posixName, boolean isNegation, boolean isIntersection) {
            super(isNegation, isIntersection);
            this.posixName = posixName;
        }

        public String getPosixName() {
            return posixName;
        }

    }

    public static class Primitive extends RegEx {

        private char ch;

        public Primitive(char esc) {
            this.ch = esc;
        }

        public char getCh() {
            return ch;
        }
    }

    public static class Range extends RegEx {

        private char from;
        private char to;

        public Range(char from, char to) {
            this.from = from;
            this.to = to;
        }

        public char getFrom() {
            return from;
        }

        public char getTo() {
            return to;
        }

    }

    public static class Repetition extends RegEx {

        private RegEx internal;

        public Repetition(RegEx internal) {
            this.internal = internal;
        }

        public RegEx getInternal() {
            return internal;
        }

    }

    public static class Sequence extends RegEx {

        public final RegEx firstSeq;
        public final RegEx secondSeq;

        public Sequence(RegEx first, RegEx second) {
            this.firstSeq = first;
            this.secondSeq = second;
        }

    }

    public static class SpecialChar {

        public static Map<Character, RegEx> sChars = buildHashMap('t', new Primitive('\t'),
                'n', new Primitive('\n'),
                'r', new Primitive('\r'),
                'f', new Primitive('\f'),
                '\\', new Primitive('\\'),
                's', buildWhiteSpaceEscape(false),
                'd', buildDigitEscape(false),
                'w', buildAlphaNumericEscape(false),
                'S', buildWhiteSpaceEscape(true),
                'D', buildDigitEscape(true),
                'W', buildAlphaNumericEscape(true)
        );

        public static RegEx buildWhiteSpaceEscape(boolean negation) {

            CharClass charClass = new CharClass(negation, false);

            charClass.addToClass(new Primitive('\t'),
                    new Primitive('\n'),
                    new Primitive('\f'),
                    new Primitive('\r'),
                    new Primitive(' '));
            return charClass;
        }

        public static RegEx buildDigitEscape(boolean negation) {
            CharClass charClass = new CharClass(negation, false);
            charClass.addToClass(new Range('0', '9'));
            return charClass;
        }

        public static RegEx buildAlphaNumericEscape(boolean negation) {
            CharClass charClass = new CharClass(false, false);
            charClass.addToClass(new Range('1', '9'),
                    new Range('a', 'z'),
                    new Range('A', 'Z'),
                    new Primitive('_'));

            return charClass;
        }

        public static HashMap<Character, RegEx> buildHashMap(Object... data) {
            HashMap<Character, RegEx> result = new HashMap<Character, RegEx>();

            if (data.length % 2 != 0) {
                throw new IllegalArgumentException("Odd number of arguments");
            }

            Character key = null;
            Integer step = -1;

            for (Object value : data) {
                step++;
                switch (step % 2) {
                    case 0:
                        if (value == null) {
                            throw new IllegalArgumentException("Null key value");
                        }
                        key = (Character) value;
                        continue;
                    case 1:
                        result.put(key, (RegEx) value);
                        break;
                }
            }

            return result;
        }

    }

    public static class SpecialConstructGroup extends Group {

        private GroupType groupType;

        public GroupType getGroupType() {
            return groupType;
        }

        public void setGroupType(GroupType groupType) {
            this.groupType = groupType;
        }

        public SpecialConstructGroup(GroupType groupType, RegEx regex) {
            super(regex);
            this.groupType = groupType;
        }

    }

}

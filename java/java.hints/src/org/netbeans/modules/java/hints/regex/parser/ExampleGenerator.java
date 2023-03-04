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
import java.util.List;
import java.util.Random;
import org.netbeans.modules.java.hints.regex.parser.RegexConstructs.*;

/**
 *
 * @author sandeemi
 */
public class ExampleGenerator {

    private final RegEx regEx;
    private Random rnd;

    private boolean DOT_ALL = false;

    public ExampleGenerator(RegEx regEx) {
        this.rnd = new Random();
        this.regEx = regEx;
    }

    public void setDOT_ALL(boolean DOT_ALL) {
        this.DOT_ALL = DOT_ALL;
    }

    public ArrayList<String> generate(int n) {

        ArrayList<String> result = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            StringBuilder sb = new StringBuilder();
            buildExample(regEx, sb);
            result.add(sb.toString());
        }

        return result;
    }

    private void buildExample(RegEx regex, StringBuilder sb) {

        if (regex instanceof Blank) {
            return;
        }

        if (regex instanceof Primitive) {
            sb.append(((Primitive) regex).getCh());
            return;
        }

        if (regex instanceof Repetition) {
            int nextInt = rnd.nextInt(10);
            RegEx internal = ((Repetition) regex).getInternal();

            for (int i = 0; i < nextInt; i++) {
                buildExample(internal, sb);
            }

            return;
        }

        if (regex instanceof OneOrMore) {
            int nextInt = rnd.nextInt(10) + 1;

            RegEx internal = ((OneOrMore) regex).getInternal();

            for (int i = 0; i < nextInt; i++) {
                buildExample(internal, sb);
            }

            return;
        }

        if (regex instanceof Group) {
            if (regex instanceof SpecialConstructGroup) {
                GroupType groupType = ((SpecialConstructGroup) regex).getGroupType();
                if (groupType.isNegative() || groupType.isPositive()) {
                    return;
                }
            }
            RegEx internal = ((Group) regex).getInternal();
            buildExample(internal, sb);

            return;
        }
        if (regex instanceof Optional) {
            boolean nextBool = rnd.nextBoolean();

            RegEx internal = ((Optional) regex).getInternal();

            if (nextBool) {
                buildExample(internal, sb);
            }

            return;
        }

        if (regex instanceof GreedyBound) {

            RegEx internal = ((GreedyBound) regex).getInternal();
            int min = ((GreedyBound) regex).getMin();
            boolean maxPresent = ((GreedyBound) regex).isMaxPresent();
            int max = ((GreedyBound) regex).getMax();

            int nextInt;

            if (maxPresent) {
                nextInt = rnd.nextInt(max - min + 1) + min;
            } else {
                nextInt = rnd.nextInt(10) + min;
            }

            for (int i = 0; i < nextInt; i++) {
                buildExample(internal, sb);
            }

            return;
        }

        if (regex instanceof AnyChar) {
            int nextInt = rnd.nextInt(126 - 32) + 32;
            sb.append((char) nextInt);
            return;
        }

        if (regex instanceof CharClass) {

            CharClass charClass = (CharClass) regex;
            List<Character> charClassList = charClass.isNegation() ? charClass.getNegationList() : charClass.getAllChar();

            int n = charClassList.size();
            sb.append(charClassList.get(rnd.nextInt(n)));

            return;
        }

        if (regex instanceof Range) {
            char from = ((Range) regex).getFrom();
            char to = ((Range) regex).getTo();

            int length = to - from;

            sb.append((char) (from + rnd.nextInt(length + 1)));

            return;
        }

        if (regex instanceof Choice) {
            ArrayList<RegEx> choices = ((Choice) regex).getChoice();

            int n = choices.size();

            buildExample(choices.get(rnd.nextInt(n)), sb);

            return;
        }

        if (regex instanceof Concat) {
            ArrayList<RegEx> concatNodes = ((Concat) regex).getConcat();
            concatNodes.forEach((RegEx nodes) -> {
                buildExample(nodes, sb);
            });

        }
    }
}

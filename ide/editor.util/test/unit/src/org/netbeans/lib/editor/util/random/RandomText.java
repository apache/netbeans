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

package org.netbeans.lib.editor.util.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class RandomText {

    public static RandomText chars(String chars, double ratio) {
        return new RandomText(Collections.singletonList(new Item(chars, ratio)), ratio, null, 0d);
    }

    public static RandomText phrase(String phrase, double ratio) {
        return new RandomText(null, 0d, Collections.singletonList(new Item(phrase, ratio)), ratio);
    }

    public static RandomText join(RandomText... randomTexts) {
        ArrayList<Item> charItems = new ArrayList<Item>();
        double charItemsRatioSum = 0d;
        ArrayList<Item> phraseItems = new ArrayList<Item>();
        double phraseItemsRatioSum = 0d;
        for (RandomText randomText : randomTexts) {
            charItems.addAll(randomText.charItems);
            charItemsRatioSum += randomText.charItemsRatioSum;
            phraseItems.addAll(randomText.phraseItems);
            phraseItemsRatioSum += randomText.phraseItemsRatioSum;
        }
        charItems.trimToSize();
        phraseItems.trimToSize();
        return new RandomText(charItems, charItemsRatioSum, phraseItems, phraseItemsRatioSum);
    }

    public static RandomText lowerCaseAZ(double ratio) {
        return chars("abcdefghijklmnopqrstuvwxyz", ratio);
    }

    public static RandomText upperCaseAZ(double ratio) {
        return chars("ABCDEFGHIJKLMNOPQRSTUVWXYZ", ratio);
    }

    public static RandomText digit09(double ratio) {
        return chars("0123456789", ratio);
    }

    public static RandomText spaceTabNewline(double ratio) {
        return chars(" \t\n", ratio);
    }


    private final List<Item> charItems;

    private final double charItemsRatioSum;

    private final List<Item> phraseItems;

    private final double phraseItemsRatioSum;

    private RandomText(List<Item> charItems, double charItemsRatioSum, List<Item> phraseItems, double phraseItemsRatioSum) {
        this.charItems = (charItems != null) ? charItems : Collections.<Item>emptyList();
        this.charItemsRatioSum = charItemsRatioSum;
        this.phraseItems = (phraseItems != null) ? phraseItems : Collections.<Item>emptyList();
        this.phraseItemsRatioSum = phraseItemsRatioSum;
    }

    public char randomChar(Random random) {
        if (charItems.size() == 0) {
            throw new IllegalStateException("No random chars defined."); // NOI18N
        }
        return randomItem(random, charItems, charItemsRatioSum).randomChar(random);
    }

    public String randomText(Random random, int length) {
        StringBuilder sb = new StringBuilder(length);
        while (--length >= 0) {
            sb.append(randomChar(random));
        }
        return sb.toString();
    }

    public String randomPhrase(Random random) {
        if (phraseItems.size() == 0) {
            throw new IllegalStateException("No random phrases defined."); // NOI18N
        }
        return randomItem(random, phraseItems, phraseItemsRatioSum).text;
    }

    private static Item randomItem(Random random, List<Item> items, double ratioSum) {
        assert (items.size() > 0); // Prevent infinite loop
        while (true) { // A cycle should prevent rounding errors problems
            double r = random.nextDouble() * ratioSum;
            for (Item item : items) {
                r -= item.ratio;
                if (r <= 0) {
                    return item;
                }
            }
        }
    }

    private static final class Item {

        public Item(String text, double ratio) {
            this.text = text;
            this.ratio = ratio;
        }

        /**
         * Acts for two purposes - as a phrase text to be returned
         * or as a group of chars where one of them will be chosen by randomChar().
         */
        final String text;

        /**
         * Ratio with which this item will be chosen.
         */
        final double ratio;

        char randomChar(Random random) {
            int index = random.nextInt(text.length());
            return text.charAt(index);
        }

    }


}

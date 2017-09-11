/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

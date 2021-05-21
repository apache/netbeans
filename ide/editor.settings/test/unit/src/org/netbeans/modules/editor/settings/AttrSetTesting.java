/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.editor.settings;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import junit.framework.TestCase;
import org.netbeans.lib.editor.util.random.RandomTestContainer;
import org.netbeans.lib.editor.util.random.RandomTestContainer.Context;
import org.netbeans.lib.editor.util.random.RandomText;

/**
 * Random testing support for fold managers.
 *
 * @author Miloslav Metelka
 */
final class AttrSetTesting {

    // Key followed by possible values as object-array
    public static final String ADD = "AttrSet-Add";

    public static final String MERGE = "AttrSet-Merge";

    public static final String FORGET = "AttrSet-Forget";

    public static final Object[] KEY_VALUES = new Object[] {
        // Shared key-values follow
        StyleConstants.Foreground, new Object[] { Color.red, Color.green, Color.blue },
        StyleConstants.Background, new Object[] { Color.red, Color.green, Color.blue },
        StyleConstants.FontFamily, new Object[] { "Monospaced", "Arial", "Helvetica" },
        StyleConstants.FontSize, new Object[] { 10, 11, 13, 15 },
        StyleConstants.Subscript, new Object[] { Boolean.TRUE, Boolean.FALSE },
        StyleConstants.Superscript, new Object[] { Boolean.TRUE, Boolean.FALSE },
        // Extra non-shared key-values follow
        "ExtraKeyColor", new Object[] { Color.cyan, Color.magenta, Color.yellow, Color.red },
        "ExtraKeyBoolean", new Object[] { Boolean.TRUE, Boolean.FALSE },
        "ExtraKeyInt", new Object[] { 5, 10, 15, 20 },
        "ExtraKey1", new Object[] { "one", "two", "three" },
        "ExtraKey2", new Object[] { "one", "two", "three" },
        "ExtraKey3", new Object[] { "one", "two", "three" },
    };

    private static final int MAX_ATTR_COUNT = 6;

    private static final int MAX_MERGE_SETS_COUNT = 4;

    public static RandomTestContainer createContainer() throws Exception {
        RandomTestContainer container = new RandomTestContainer();
        container.putProperty(SimpleWeakSet.class, new SimpleWeakSet<Object>()); // Weak map
        container.putProperty(List.class, new ArrayList<Object>()); // List of the items (strongly referenced)
        container.addOp(new AddOp());
        container.addOp(new ForgetOp());
        container.addOp(new MergeOp());
        container.addCheck(new AttrsCheck());

        RandomText randomText = RandomText.join(
                RandomText.lowerCaseAZ(3),
                RandomText.spaceTabNewline(1),
                RandomText.phrase(" \n\n\n", 1),
                RandomText.phrase(" \t\tabcdef\t", 1)
        );
        container.putProperty(RandomText.class, randomText);
        return container;
    }

    public static RandomTestContainer.Round addRoundPreferAdd(RandomTestContainer container, int opCount) throws Exception {
        RandomTestContainer.Round round = container.addRound();
        round.setOpCount(opCount);
        round.setRatio(ADD, 6);
        round.setRatio(MERGE, 3);
        round.setRatio(FORGET, 5);
        return round;
    }

    public static RandomTestContainer.Round addRoundPreferRemove(RandomTestContainer container, int opCount) throws Exception {
        RandomTestContainer.Round round = container.addRound();
        round.setOpCount(opCount);
        round.setRatio(ADD, 4);
        round.setRatio(MERGE, 2);
        round.setRatio(FORGET, 8);
        return round;
    }

    public static AttrSet add(Context context, Object... keyValuePairs) throws Exception {
        @SuppressWarnings("unchecked")
        List<Item> list = (List<Item>) context.getInstance(List.class);
        MutableAttributeSet expected = new SimpleAttributeSet();
        for (int i = keyValuePairs.length; i > 0;) {
            Object value = keyValuePairs[--i];
            Object key = keyValuePairs[--i];
            expected.addAttribute(key, value);
        }
        AttrSet attrSet = AttrSet.get(keyValuePairs);
        Item item = new Item(expected, attrSet);
        list.add(item);
        StringBuilder sb = context.logOpBuilder();
        if (sb != null) {
            sb.append("Add[").append(list.size() - 1).append("]: ").append(expected);
            context.logOp(sb);
        }
        return attrSet;
    }

    public static void forget(Context context, int listIndex) throws Exception {
        @SuppressWarnings("unchecked")
        List<Item> list = (List<Item>) context.getInstance(List.class);
        Object element = list.remove(listIndex);

        StringBuilder sb = context.logOpBuilder();
        if (sb != null) {
            sb.append("Remove[").append(listIndex).append("]: ").append(element).append("\n");
            context.logOp(sb);
        }
    }

    public static void merge(Context context, int... indexes) throws Exception {
        @SuppressWarnings("unchecked")
        List<Item> list = (List<Item>) context.getInstance(List.class);
        StringBuilder sb = context.logOpBuilder();
        if (sb != null) {
            sb.append("Merge[");
        }
        MutableAttributeSet mutableAttributeSet = new SimpleAttributeSet();
        AttrSet[] attrSets = new AttrSet[indexes.length];
        for (int i = indexes.length - 1; i >= 0; i--) {
            int index = indexes[i];
            if (sb != null) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(index);
            }
            Item item = list.get(index);
            attrSets[i] = item.attrSet;
            mutableAttributeSet.addAttributes(item.expected);
        }
        AttrSet mergedAttrSet = AttrSet.merge(attrSets);
        Item mergedItem = new Item(mutableAttributeSet, mergedAttrSet);
        list.add(mergedItem);

        if (sb != null) {
            sb.append("]\n");
            context.logOp(sb);
        }
    }

    private static final class AddOp extends RandomTestContainer.Op {

        public AddOp() {
            super(ADD);
        }

        @Override
        protected void run(Context context) throws Exception {
            Random random = context.container().random();
            int attrCount = random.nextInt(MAX_ATTR_COUNT + 1);
            Object[] keyValuePairs = new Object[attrCount << 1];
            for (int i = 0; i < attrCount; i++) {
                int index = random.nextInt(KEY_VALUES.length >> 1);
                Object key = KEY_VALUES[i << 1];
                Object[] values = (Object[]) KEY_VALUES[(i << 1) + 1];
                int valueIndex = random.nextInt(values.length);
                Object value = values[valueIndex];
                keyValuePairs[i << 1] = key;
                keyValuePairs[(i << 1) + 1] = value;
            }
            add(context, keyValuePairs);
        }

    }

    private static final class ForgetOp extends RandomTestContainer.Op {

        public ForgetOp() {
            super(FORGET);
        }

        @Override
        protected void run(Context context) throws Exception {
            @SuppressWarnings("unchecked")
            List<Object> list = context.getInstance(List.class);
            if (list.size() > 0) {
                Random random = context.container().random();
                int elementIndex = random.nextInt(list.size());
                forget(context, elementIndex);
            }
        }

    }

    private static final class MergeOp extends RandomTestContainer.Op {

        public MergeOp() {
            super(MERGE);
        }

        @Override
        protected void run(Context context) throws Exception {
            @SuppressWarnings("unchecked")
            List<Object> list = context.getInstance(List.class);
            if (list.size() > 0) {
                Random random = context.container().random();
                int attrCount = 1 + random.nextInt(MAX_MERGE_SETS_COUNT);
                int[] indexes = new int[attrCount];
                for (int i = 0; i < attrCount; i++) {
                    int index = random.nextInt(list.size());
                    indexes[i] = index;
                }
                merge(context, indexes);
            }
        }

    }

    private static int equal;
    private static int notEqual;

    private static final class AttrsCheck extends RandomTestContainer.Check {

        @Override
        protected void check(final Context context) throws Exception {
            @SuppressWarnings("unchecked")
            List<Item> list = (List<Item>) context.getInstance(List.class);
            for (Item item : list) {
                item.attrSet.checkIntegrity();
                item.check();
                for (Item item2 : list) {
                    boolean expected = item2.expected.equals(item.expected);
                    boolean tested = item2.attrSet.equals(item.attrSet);
                    if (expected) {
                        equal++;
                    } else {
                        notEqual++;
                    }
                    if (expected != tested) {
                        throw new IllegalStateException("Equality: expected=" + expected + " != tested=" + tested);
                    }
                }
            }
//            if (context.opCount() % 100 == 0) {
//                System.err.println("Stats: opCount=" + context.opCount()
//                        + ", equal=" + equal + ", notEqual=" + notEqual);
//            }
        }

    }

    private static final class Item {

        final AttributeSet expected;

        final AttrSet attrSet;

        Item(AttributeSet expected, AttrSet attrSet) {
            assert (expected != null) : "Null expected";
            assert (attrSet != null) : "Null attrSet";
            this.expected = expected;
            this.attrSet = attrSet;
        }

        public void check() {
            Enumeration<?> keys1 = expected.getAttributeNames();
            Enumeration<?> keys2 = attrSet.getAttributeNames();
            Set<?> keys1Set = new HashSet<Object>(Collections.list(keys1));
            Set<?> keys2Set = new HashSet<Object>(Collections.list(keys2));
            if (!keys1Set.equals(keys2Set)) {
                throw new IllegalStateException("Key sets not equal: expected: " + keys1Set + "\ntested: " + keys2Set);
            }
            for (Object key : keys1Set) {
                Object value1 = expected.getAttribute(key);
                Object value2 = attrSet.getAttribute(key);
                if (value1 == null) {
                    throw new IllegalStateException("Internal error: value for key=" + key + " is null");
                }
                if (!value1.equals(value2)) {
                    throw new IllegalStateException("Values differ for key=" + key + " expected: " + value1 + ", test: " + value2);
                }
            }
        }

        @Override
        public String toString() {
            return "attrSet: " + attrSet;
        }

    }

}

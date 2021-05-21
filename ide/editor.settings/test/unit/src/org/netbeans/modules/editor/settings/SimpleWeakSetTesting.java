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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import junit.framework.TestCase;
import org.netbeans.lib.editor.util.random.RandomTestContainer;
import org.netbeans.lib.editor.util.random.RandomTestContainer.Context;
import org.netbeans.lib.editor.util.random.RandomText;

/**
 * Random testing support for simple weak set.
 *
 * @author Miloslav Metelka
 */
final class SimpleWeakSetTesting {

    public static final String ADD = "SimpleWeakSet-Add";

    public static final String ADD_HASH_0 = "SimpleWeakSet-AddHash0";

    public static final String ADD_HASH_1 = "SimpleWeakSet-AddHash1";

    public static final String ADD_HASH_33 = "SimpleWeakSet-AddHash33";

    public static final String REMOVE = "SimpleWeakSet-Remove";

    public static final String FORGET = "SimpleWeakSet-Forget";

    public static final String SIZE = "SimpleWeakSet-Size";

    private static final String REMOVED_LIST = "SimpleWeakSet-RemovedList";

    public static RandomTestContainer createContainer() throws Exception {
        RandomTestContainer container = new RandomTestContainer();
        container.putProperty(SimpleWeakSet.class, new SimpleWeakSet<Object>()); // Weak map
        container.putProperty(List.class, new ArrayList<Object>()); // List of the items (strongly referenced)
        container.putProperty(REMOVED_LIST, new ArrayList<Object>()); // List of items that were removed (reset during check)
        container.addOp(new AddOp());
        container.addOp(new AddHash0Op());
        container.addOp(new AddHash1Op());
        container.addOp(new AddHash33Op());
        container.addOp(new RemoveOp());
        container.addOp(new ForgetOp());
        container.addOp(new SizeOp());
        container.addCheck(new SetsCheck());

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
        round.setRatio(ADD_HASH_0, 2);
        round.setRatio(ADD_HASH_1, 1);
        round.setRatio(ADD_HASH_33, 1);
        round.setRatio(REMOVE, 3);
        round.setRatio(FORGET, 3);
        round.setRatio(SIZE, 2);
        return round;
    }

    public static RandomTestContainer.Round addRoundPreferRemove(RandomTestContainer container, int opCount) throws Exception {
        RandomTestContainer.Round round = container.addRound();
        round.setOpCount(opCount);
        round.setRatio(ADD, 4);
        round.setRatio(ADD_HASH_0, 1);
        round.setRatio(ADD_HASH_1, 1);
        round.setRatio(ADD_HASH_33, 1);
        round.setRatio(REMOVE, 4);
        round.setRatio(FORGET, 4);
        round.setRatio(SIZE, 2);
        return round;
    }

    public static void addElement(Context context, Object element) throws Exception {
        @SuppressWarnings("unchecked")
        SimpleWeakSet<Object> simpleWeakSet = context.getInstance(SimpleWeakSet.class);
        @SuppressWarnings("unchecked")
        List<Object> list = context.getInstance(List.class);
        list.add(element);
        simpleWeakSet.getOrAdd(element, null);

        StringBuilder sb = context.logOpBuilder();
        if (sb != null) {
            sb.append("Add[").append(list.size() - 1).append("]: ").append(element).append("\n");
            context.logOp(sb);
        }
    }

    public static void removeElement(Context context, int listIndex) throws Exception {
        @SuppressWarnings("unchecked")
        SimpleWeakSet<Object> simpleWeakSet = context.getInstance(SimpleWeakSet.class);
        @SuppressWarnings("unchecked")
        List<Object> removedList = (List<Object>) context.getProperty(REMOVED_LIST);
        @SuppressWarnings("unchecked")
        List<Object> list = context.getInstance(List.class);
        Object element = list.remove(listIndex);
        removedList.add(element);
        Object removedElement = simpleWeakSet.remove(element);
        TestCase.assertEquals(element, removedElement);

        StringBuilder sb = context.logOpBuilder();
        if (sb != null) {
            sb.append("Remove[").append(listIndex).append("]: ").append(element).append("\n");
            context.logOp(sb);
        }
    }

    public static void forgetElement(Context context, int listIndex) throws Exception {
        @SuppressWarnings("unchecked")
        List<Object> list = context.getInstance(List.class);
        Object element = list.remove(listIndex);

        StringBuilder sb = context.logOpBuilder();
        if (sb != null) {
            sb.append("Forget[").append(listIndex).append("]: ").append(element).append("\n");
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
            // Generate hashCode to maintain test reproducibility since with different (native) hashCode
            // the placement of the object in a hash set could be affected.
            int hashCode = random.nextInt();
            Object element = new TestObject(hashCode);
            addElement(context, element);
        }

    }

    private static final class AddHash0Op extends RandomTestContainer.Op {

        public AddHash0Op() {
            super(ADD_HASH_0);
        }

        @Override
        protected void run(Context context) throws Exception {
            Object element = new TestObject(0);
            addElement(context, element);
        }

    }

    private static final class AddHash1Op extends RandomTestContainer.Op {

        public AddHash1Op() {
            super(ADD_HASH_1);
        }

        @Override
        protected void run(Context context) throws Exception {
            Object element = new TestObject(1);
            addElement(context, element);
        }

    }

    private static final class AddHash33Op extends RandomTestContainer.Op {

        public AddHash33Op() {
            super(ADD_HASH_33);
        }

        @Override
        protected void run(Context context) throws Exception {
            Object element = new TestObject(33);
            addElement(context, element);
        }

    }

    private static final class RemoveOp extends RandomTestContainer.Op {

        public RemoveOp() {
            super(REMOVE);
        }

        @Override
        protected void run(Context context) throws Exception {
            @SuppressWarnings("unchecked")
            List<Object> list = context.getInstance(List.class);
            if (list.size() > 0) {
                Random random = context.container().random();
                int elementIndex = random.nextInt(list.size());
                removeElement(context, elementIndex);
            }
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
                forgetElement(context, elementIndex);
//                System.gc();
            }
        }

    }

    private static final class SizeOp extends RandomTestContainer.Op {

        public SizeOp() {
            super(SIZE);
        }

        @Override
        protected void run(Context context) throws Exception {
            // Check size of list and set
            @SuppressWarnings("unchecked")
            SimpleWeakSet<Object> simpleWeakSet = context.getInstance(SimpleWeakSet.class);
            @SuppressWarnings("unchecked")
            List<Object> list = context.getInstance(List.class);
            System.gc();
            Thread.sleep(1);
            int listSize = list.size();
            int setSize = simpleWeakSet.size();
            if (listSize != setSize) {
                // It may be affected by GC => get real size by returning as list
                setSize = simpleWeakSet.asList().size();
                if (listSize != setSize) {
                    throw new IllegalStateException("listSize=" + listSize + " != setSize=" + setSize);
                }
            }
        }

    }

    private static final class SetsCheck extends RandomTestContainer.Check {

        @Override
        protected void check(final Context context) throws Exception {
            @SuppressWarnings("unchecked")
            SimpleWeakSet<Object> simpleWeakSet = context.getInstance(SimpleWeakSet.class);
            @SuppressWarnings("unchecked")
            List<Object> list = context.getInstance(List.class);
            @SuppressWarnings("unchecked")
            List<Object> removedList = (List<Object>) context.getProperty(REMOVED_LIST);
            for (Object e : list) {
                Object testE;
                if (!e.equals(testE = simpleWeakSet.getOrAdd(e, null))) {
                    throw new IllegalStateException("e=" + e + " not-equal to testE=" + testE);
                }
            }
            for (Object e : removedList) {
                if (simpleWeakSet.contains(e)) {
                    throw new IllegalStateException("Object " + e + " incorrectly contained in simpleWeakSet.");
                }
            }
            removedList.clear();
        }

    }

    private static final class TestObject {

        private final int hashCode;

        TestObject() {
            this.hashCode = super.hashCode();
        }

        TestObject(int hashCode) {
            this.hashCode = hashCode;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public String toString() {
            return "TestObject: H=" + hashCode + " @ " + System.identityHashCode(this);
        }

    }

}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
        container.putProperty(SimpleWeakSet.class, new SimpleWeakSet()); // Weak map
        container.putProperty(List.class, new ArrayList()); // List of the items (strongly referenced)
        container.putProperty(REMOVED_LIST, new ArrayList()); // List of items that were removed (reset during check)
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
        SimpleWeakSet simpleWeakSet = context.getInstance(SimpleWeakSet.class);
        List list = context.getInstance(List.class);
        list.add(element);
        simpleWeakSet.getOrAdd(element, null);

        StringBuilder sb = context.logOpBuilder();
        if (sb != null) {
            sb.append("Add[").append(list.size() - 1).append("]: ").append(element).append("\n");
            context.logOp(sb);
        }
    }

    public static void removeElement(Context context, int listIndex) throws Exception {
        SimpleWeakSet simpleWeakSet = context.getInstance(SimpleWeakSet.class);
        List removedList = (List) context.getProperty(REMOVED_LIST);
        List list = context.getInstance(List.class);
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
        List list = context.getInstance(List.class);
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
            List list = context.getInstance(List.class);
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
            List list = context.getInstance(List.class);
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
            SimpleWeakSet simpleWeakSet = context.getInstance(SimpleWeakSet.class);
            List list = context.getInstance(List.class);
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
            SimpleWeakSet simpleWeakSet = context.getInstance(SimpleWeakSet.class);
            List list = context.getInstance(List.class);
            List removedList = (List) context.getProperty(REMOVED_LIST);
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

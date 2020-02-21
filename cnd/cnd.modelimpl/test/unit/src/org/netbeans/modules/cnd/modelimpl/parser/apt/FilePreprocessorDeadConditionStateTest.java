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

package org.netbeans.modules.cnd.modelimpl.parser.apt;

import org.netbeans.modules.cnd.modelimpl.csm.core.FilePreprocessorConditionState;
import org.netbeans.modules.cnd.test.CndBaseTestCase;

/**
 *
 */
public class FilePreprocessorDeadConditionStateTest extends CndBaseTestCase {

    public FilePreprocessorDeadConditionStateTest() {
        super("dead blocks test");
    }

    public void testDeadBlocksComparision() throws Exception {
        APTBasedPCStateBuilder stateBuilder1 = new APTBasedPCStateBuilder("state1");
        stateBuilder1.addBlockImpl(30, 60);
        stateBuilder1.addBlockImpl(10, 20);
        stateBuilder1.addBlockImpl(70, 80);
        FilePreprocessorConditionState state1 = stateBuilder1.build();

        APTBasedPCStateBuilder stateBuilder2 = new APTBasedPCStateBuilder("state2");
        stateBuilder2.addBlockImpl(10, 20);
        stateBuilder2.addBlockImpl(70, 80);
        FilePreprocessorConditionState state2 = stateBuilder2.build();

        FilePreprocessorConditionState biggest = new APTBasedPCStateBuilder("biggest").addBlockImpl(5, 90).build();

        FilePreprocessorConditionState state4 = new APTBasedPCStateBuilder("state4").addBlockImpl(40, 50).build();

        FilePreprocessorConditionState state5 = new APTBasedPCStateBuilder("state5").addBlockImpl(10, 20).addBlockImpl(40, 50).build();

        FilePreprocessorConditionState state6 = new APTBasedPCStateBuilder("state6").addBlockImpl(30, 40).addBlockImpl(50, 60).build();

        FilePreprocessorConditionState state7 = new APTBasedPCStateBuilder("state7").addBlockImpl(50, 60).addBlockImpl(70, 80).build();

        FilePreprocessorConditionState empty = new APTBasedPCStateBuilder("empty").build();

        assertTrue("state can replace itself " + state1, state1.isBetterOrEqual(state1));
        assertTrue("state can replace itself " + state2, state2.isBetterOrEqual(state2));
        assertTrue("state can replace itself " + biggest, biggest.isBetterOrEqual(biggest));
        assertTrue("state can replace itself " + state4, state4.isBetterOrEqual(state4));
        assertTrue("state can replace itself " + state5, state5.isBetterOrEqual(state5));
        assertTrue("state can replace itself " + state6, state6.isBetterOrEqual(state6));
        assertTrue("state can replace itself " + state7, state7.isBetterOrEqual(state7));
        assertTrue("state can replace itself " + empty, empty.isBetterOrEqual(empty));

        assertTrue("state1:"+state1 + " must replace " + biggest, state1.isBetterOrEqual(biggest));
        assertFalse("state1:"+state1 + " is not replaceable by " + biggest, biggest.isBetterOrEqual(state1));

        assertTrue("state2:"+state2 + " must replace " + state1, state2.isBetterOrEqual(state1));
        assertFalse("state2:"+state2 + " is not replaceable by " + state1, state1.isBetterOrEqual(state2));

        assertTrue("state4:"+state4 + " must replace " + state1, state4.isBetterOrEqual(state1));
        assertFalse("state4:"+state4 + " is not replaceable by " + state1, state1.isBetterOrEqual(state4));

        assertTrue("state4:"+state4 + " must replace " + biggest, state4.isBetterOrEqual(biggest));
        assertFalse("state4:"+state4 + " is not replaceable by " + biggest, biggest.isBetterOrEqual(state4));

        assertTrue("empty:"+empty + " must replace " + state1, empty.isBetterOrEqual(state1));
        assertFalse("empty:"+empty + " is not replaceable by " + state1, state1.isBetterOrEqual(empty));

        assertTrue("empty:"+empty + " must replace " + state2, empty.isBetterOrEqual(state2));
        assertFalse("empty:"+empty + " is not replaceable by " + state2, state2.isBetterOrEqual(empty));

        assertTrue("empty:"+empty + " must replace " + biggest, empty.isBetterOrEqual(biggest));
        assertFalse("empty:"+empty + " is not replaceable by " + biggest, biggest.isBetterOrEqual(empty));
        
        assertTrue("empty:"+empty + " must replace " + state4, empty.isBetterOrEqual(state4));
        assertFalse("empty:"+empty + " is not replaceable by " + state4, state4.isBetterOrEqual(empty));

        assertFalse("state4:"+state4 + " is not comaprable with " + state2, state4.isBetterOrEqual(state2));
        assertFalse("state2:"+state2 + " is not comaprable with " + state4, state2.isBetterOrEqual(state4));

        assertFalse("state5:" + state5 + " is not comaprable with " + state2, state5.isBetterOrEqual(state2));
        assertFalse("state2:" + state2 + " is not comaprable with " + state5, state2.isBetterOrEqual(state5));

        assertFalse("state6:" + state6 + " is not comaprable with " + state2, state6.isBetterOrEqual(state2));
        assertFalse("state2:" + state2 + " is not comaprable with " + state6, state2.isBetterOrEqual(state6));

        assertTrue("state4:" + state4 + " must replace " + state5, state4.isBetterOrEqual(state5));
        assertFalse("state4:" + state4 + " is not replaceable by " + state5, state5.isBetterOrEqual(state4));

        assertTrue("["+10+"-"+20+"] is in active block of " + state4, state4.isInActiveBlock(10, 20));

        assertFalse("state7:" + state7 + " is not comaprable with " + state2, state7.isBetterOrEqual(state2));
        assertFalse("state2:" + state2 + " is not comaprable with " + state7, state2.isBetterOrEqual(state7));
        assertFalse("state7:" + state7 + " is not comaprable with " + state6, state7.isBetterOrEqual(state6));
        assertFalse("state6:" + state6 + " is not comaprable with " + state7, state6.isBetterOrEqual(state7));
    }
}

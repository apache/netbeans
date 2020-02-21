/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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

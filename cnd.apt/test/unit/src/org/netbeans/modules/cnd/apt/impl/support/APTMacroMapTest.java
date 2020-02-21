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
package org.netbeans.modules.cnd.apt.impl.support;

import java.util.Arrays;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Test;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.api.PPMacroMap.State;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.openide.util.CharSequences;

/**
 *
 */
public class APTMacroMapTest {
    private static final CharSequence GUARD = CharSequences.create("GUARD");
    private static final CharSequence KEY1 = CharSequences.create("KEY1");
    private static final CharSequence VALUE1 = CharSequences.create("VALUE1");
    private static final CharSequence KEY2 = CharSequences.create("KEY2");
    private static final CharSequence KEY2_FUN = CharSequences.create(KEY2.toString() + "(x, y)");
    private static final CharSequence VALUE2 = CharSequences.create("VALUE2 + x + y");
    
    @Test
    public void testGetMacro() {
        APTDefine defineKey = APTUtils.createAPTDefine(KEY1 + "=" + VALUE1);
        APTDefine funDefineKey = APTUtils.createAPTDefine(KEY2_FUN + "=" + VALUE1);
        APTToken tokKey = APTUtils.createIDENT(CharSequences.create(KEY1));
        APTFileMacroMap mmap1 = new APTFileMacroMap(null, Arrays.asList(GUARD.toString()));
        APTToken tokGuard = APTUtils.createIDENT(CharSequences.create(GUARD));
        APTMacro macro1 = mmap1.getMacro(tokGuard);
        assertNotNull(macro1);
        assertEquals("different name", tokGuard.getTextID(), macro1.getName().getTextID());
        assertFalse(macro1.isFunctionLike());
        APTBaseMacroMap.StateImpl state1 = (APTBaseMacroMap.StateImpl) mmap1.getState();
        mmap1.undef(null, tokGuard);
        assertNull(mmap1.getMacro(tokGuard));
        assertNull(mmap1.active.getMacro(CharSequences.create("NO_SUCH_KEY")));
        assertEquals(0, mmap1.active.getAll().size());
        Map<CharSequence, APTMacro> allMacros = mmap1.active.getAll();
        assertTrue(allMacros.isEmpty());
        Map<CharSequence, APTMacro> allMacrosState1 = state1.snap.getAll();
        assertEquals(1, allMacrosState1.size());
        assertEquals(macro1, allMacrosState1.get(CharSequences.create(GUARD)));
        mmap1.define(null, defineKey, APTMacro.Kind.DEFINED);
        assertNotNull(mmap1.getMacro(tokKey));
        assertFalse(mmap1.isDefined(GUARD));
        mmap1.define(null, funDefineKey, APTMacro.Kind.DEFINED);
        assertFalse(mmap1.isDefined(GUARD));
        State state2 = mmap1.getState();
        assertFalse(mmap1.isDefined(GUARD));
        APTFileMacroMap fromState1 = new APTFileMacroMap();
        fromState1.setState(state1);
        assertNotNull(fromState1.getMacro(tokGuard));
        assertNull(fromState1.getMacro(tokKey));
        assertNotNull(mmap1.getMacro(tokKey));
        assertEquals(2, mmap1.active.getAll().size());
        mmap1.define(null, macro1.getDefineNode(), APTMacro.Kind.DEFINED);
        assertEquals(3, mmap1.active.getAll().size());
    }
}

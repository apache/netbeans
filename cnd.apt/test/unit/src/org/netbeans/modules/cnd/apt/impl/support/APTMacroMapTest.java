/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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

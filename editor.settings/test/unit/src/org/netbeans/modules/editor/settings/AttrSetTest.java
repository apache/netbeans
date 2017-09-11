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

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.StyleConstants;
import org.netbeans.junit.Filter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.random.RandomTestContainer;

/**
 * Test of SimpleWeakSet functionality.
 *
 *  @author Miloslav Metelka
 */
public class AttrSetTest extends NbTestCase {

    public AttrSetTest(String testName) {
        super(testName);
        Filter filter = new Filter();
        filter.setIncludes(new Filter.IncludeExclude[]{new Filter.IncludeExclude("testFixed", "")});
//        setFilter(filter);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    public void testFixed() throws Exception {
        RandomTestContainer container = AttrSetTesting.createContainer();
        container.setLogOp(true);
        AttrSet empty = AttrSet.get(new Object[0]); // Empty
        AttrSet empty2 = AttrSet.get(new Object[0]); // Empty
        assertSame(empty, empty2);
        AttrSet fgr = AttrSet.get(StyleConstants.Foreground, Color.red);
        AttrSet fgr2 = AttrSet.get(StyleConstants.Foreground, new Color(255, 0, 0));
        AttrSet fgr3 = AttrSet.get(StyleConstants.Foreground, Color.red, StyleConstants.Foreground, Color.blue);
        assertSame(fgr, fgr2);
        assertSame(fgr, fgr3);
        AttrSet fgrA = AttrSet.get("MyKey", "MyValue", StyleConstants.Foreground, Color.red);
        AttrSet fgrB = AttrSet.get(StyleConstants.Foreground, Color.red, "MyKey", "MyValue");
        assertSame(fgrA, fgrB);
        AttrSet fgb = AttrSet.get(StyleConstants.Foreground, Color.blue);
        AttrSet m = AttrSet.merge(fgb, fgr);
        assertSame(m, fgb);
        AttrSet fgg = AttrSet.get(StyleConstants.Foreground, Color.green);
        m = AttrSet.merge(fgg, fgb, fgr);
        assertSame(m, fgg);
        AttrSet bgb = AttrSet.get(StyleConstants.Background, Color.blue);
        m = AttrSet.merge(fgr, bgb);
        assertEquals(2, m.getAttributeCount());
        assertEquals(Color.red, m.getAttribute(StyleConstants.Foreground));
        assertEquals(Color.blue, m.getAttribute(StyleConstants.Background));
        AttrSet fgrBgb = AttrSet.get(StyleConstants.Foreground, Color.red, StyleConstants.Background, Color.blue);
        assertSame(fgrBgb, m);
        m = AttrSet.merge(fgr, bgb);
        AttrSet fontSub = AttrSet.get("MyKey", "MyValue", StyleConstants.FontFamily, "Monospaced",
                StyleConstants.FontSize, 11, StyleConstants.Bold, true, StyleConstants.Subscript, true);
        m = AttrSet.merge(fontSub, fgrBgb);
        AttrSet as1 = AttrSet.get(StyleConstants.Foreground, Color.red, StyleConstants.Foreground, Color.blue);
        assertEquals(Color.red, as1.getAttribute(StyleConstants.Foreground));

        AttrSet myKeyValue = AttrSet.get("MyKey", "MyValue"); // not cached
        AttrSet myKeyValue1 = AttrSet.get("MyKey1", "MyValue1"); // not cached
        AttrSet myKeyValue1Copy = AttrSet.get("MyKey1", "MyValue1"); // not cached
        // Current impl will return the same object for the following although
        // these are extra attrs but they are weakly cached from the AttrSet.EMTY.
        assertSame(myKeyValue1, myKeyValue1Copy);
        AttrSet em = AttrSet.merge(myKeyValue1, myKeyValue);
        AttrSet em2 = AttrSet.merge(myKeyValue1Copy, myKeyValue);
        if (em != em2) {
            fail("Expecting same instance when merging with equal attr set. Test update necessary.");
        }
    }

    public void testRandom() throws Exception {
        RandomTestContainer container = AttrSetTesting.createContainer();
        container.setLogOp(true);
        int opCount = 1000;
        AttrSetTesting.addRoundPreferAdd(container, opCount);
        AttrSetTesting.addRoundPreferRemove(container, opCount);
        container.runInit(1274962532005L);
        container.runOps(9);
        container.runOps(1);
        container.runOps(0);

        container.runInit(1274381066314L);
        container.runOps(0); // Run till end

        container.run(0L);
//        container.runOps(263);
//        container.runOps(1);

    }

}

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
package org.netbeans.modules.cnd.repository.impl;

import java.util.concurrent.atomic.AtomicBoolean;
import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.keys.TestLargeKey;
import org.netbeans.modules.cnd.repository.keys.TestSmallKey;
import org.netbeans.modules.cnd.repository.keys.TestValue;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;

/**
 * Tests Repository.tryGet()
 *
 */
public class TryGetTest extends GetPutTestBase {

    public TryGetTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(TryGetTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testTryGet() {
        _test(new TestSmallKey("small_1", getUnitID()), new TestValue("small_obj_1"));
        _test(new TestLargeKey("large_1", getUnitID()), new TestValue("large_obj_1"));

    }
    private final AtomicBoolean readFlag = new AtomicBoolean(false);

    @Override
    public void onReadHook() {
        readFlag.set(true);
    }

    @Override
    public void onWriteHook() {
    }

    @Override
    public void onRemoveHook() {
    }
    
    private void _test(Key key, TestValue value) {

        Repository.startup(0);
        Repository.put(key, value);

        Persistent v2 = Repository.get(key);
        assertNotNull(v2);
        assertEquals(value, v2);

        readFlag.set(false);

//        RepositoryTestUtils.debugClear();

        //v2 = _tryGet(key);
        //assertNull(v2);
    }
}

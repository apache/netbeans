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
package org.netbeans.modules.cnd.utils.cache;

import java.util.Collections;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class TinyMapsTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testCreateMap() {
        for (int initialCapacity = 0; initialCapacity < 65; initialCapacity++) {
            Map<?, ?> result = TinyMaps.createMap(initialCapacity);
            Assert.assertNotNull(result);
            if (initialCapacity == 0) {
                Assert.assertSame("have to be empty collection " + result.getClass().getSimpleName(), Collections.emptyMap(), result);
            } else if (initialCapacity == 1) {
                Assert.assertTrue("have to be TinySingletonMap " + result.getClass().getSimpleName(), result instanceof TinySingletonMap<?,?>);
            } else if (initialCapacity == 2) {
                Assert.assertTrue("have to be TinyTwoValuesMap " + result.getClass().getSimpleName(), result instanceof TinyTwoValuesMap<?,?>);
            } else if (initialCapacity <= 4) {
                Assert.assertTrue("have to be TinyMap4 " + result.getClass().getSimpleName(), result instanceof TinyMaps.TinyMap4<?,?>);
            } else if (initialCapacity <= 6) {
                Assert.assertTrue("have to be TinyMap6 " + result.getClass().getSimpleName(), result instanceof TinyMaps.TinyMap6<?,?>);
            } else if (initialCapacity <= 8) {
                Assert.assertTrue("have to be TinyMap8 " + result.getClass().getSimpleName(), result instanceof TinyMaps.TinyMap8<?,?>);
            } else if (initialCapacity <= 16) {
                Assert.assertTrue("have to be TinyMap16 " + result.getClass().getSimpleName(), result instanceof TinyMaps.TinyMap16<?,?>);
            } else if (initialCapacity <= 32) {
                Assert.assertTrue("have to be TinyMap16 " + result.getClass().getSimpleName(), result instanceof TinyMaps.TinyHashMap32<?,?>);
            } else if (initialCapacity <= 64) {
                Assert.assertTrue("have to be TinyMap16 " + result.getClass().getSimpleName(), result instanceof TinyMaps.TinyHashMap64<?,?>);
            } else if (initialCapacity <= 128) {
                Assert.assertTrue("have to be TinyMap16 " + result.getClass().getSimpleName(), result instanceof TinyMaps.TinyHashMap128<?,?>);
            } else {
                Assert.assertTrue("have to be TinyHashMap " + result.getClass().getSimpleName(), result instanceof TinyMaps.TinyHashMap<?,?>);
            }
        }
    }

    @Test
    public void testExpandForNextKey() {
        System.out.println("expandForNextKey");
        Map<Object, Object> orig = null;
        Object newElem = null;
        Map expResult = null;
        Map result = TinyMaps.expandForNextKey(orig, newElem);
//        Assert.assertEquals(expResult, result);
    }
}

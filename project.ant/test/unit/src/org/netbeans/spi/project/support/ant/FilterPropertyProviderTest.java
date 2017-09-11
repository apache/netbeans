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

package org.netbeans.spi.project.support.ant;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import org.netbeans.junit.NbTestCase;
import org.openide.util.test.MockChangeListener;

/**
 * @author Jesse Glick
 */
public class FilterPropertyProviderTest extends NbTestCase {

    public FilterPropertyProviderTest(String name) {
        super(name);
    }

    public void testDelegatingPropertyProvider() throws Exception {
        AntBasedTestUtil.TestMutablePropertyProvider mpp = new AntBasedTestUtil.TestMutablePropertyProvider(new HashMap<String,String>());
        DPP dpp = new DPP(mpp);
        MockChangeListener l = new MockChangeListener();
        dpp.addChangeListener(l);
        assertEquals("initially empty", Collections.emptyMap(), dpp.getProperties());
        mpp.defs.put("foo", "bar");
        mpp.mutated();
        l.assertEvent();
        assertEquals("now right contents", Collections.singletonMap("foo", "bar"), dpp.getProperties());
        AntBasedTestUtil.TestMutablePropertyProvider mpp2 = new AntBasedTestUtil.TestMutablePropertyProvider(new HashMap<String,String>());
        mpp2.defs.put("foo", "bar2");
        dpp.setDelegate_(mpp2);
        l.msg("got a change from new delegate").assertEvent();
        assertEquals("right contents from new delegate", Collections.singletonMap("foo", "bar2"), dpp.getProperties());
        mpp2.defs.put("foo", "bar3");
        mpp2.mutated();
        l.msg("got a change in new delegate").assertEvent();
        assertEquals("right contents", Collections.singletonMap("foo", "bar3"), dpp.getProperties());
        Reference<?> r = new WeakReference<Object>(mpp);
        mpp = null;
        assertGC("old delegates can be collected", r);
        r = new WeakReference<Object>(dpp);
        dpp = null; // but not mpp2
        assertGC("delegating PP can be collected when delegate is not", r); // #50572
    }

    private static final class DPP extends FilterPropertyProvider {
        public DPP(PropertyProvider pp) {
            super(pp);
        }
        public void setDelegate_(PropertyProvider pp) {
            setDelegate(pp);
        }
    }

}

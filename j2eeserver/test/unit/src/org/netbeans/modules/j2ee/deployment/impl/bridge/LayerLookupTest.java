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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.deployment.impl.bridge;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Hejl
 */
public class LayerLookupTest extends NbTestCase {

    private static final String TEST_FOLDER = "LayerLookupTest"; // NOI18N

    public LayerLookupTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Lookup.getDefault().lookup(ModuleInfo.class);
    }

    public void testLookupConsulted() throws IOException {
        FileObject servers = FileUtil.createFolder(FileUtil.getConfigRoot(),
                TEST_FOLDER);
        FileObject testLookup = FileUtil.createData(servers, "TestLookup.instance"); // NOI18N

        Map<Class<?>, Object> lookups = new HashMap<Class<?>, Object>();
        lookups.put(String.class, "Test"); // NOI18N
        lookups.put(Integer.class, Integer.valueOf(0));
        lookups.put(Character.class, Character.valueOf('a')); // NOI18N
        lookups.put(Double.class, Double.valueOf(0.0));

        TestLookup lookupInstance = new TestLookup(lookups);

        testLookup.setAttribute("instanceOf", Lookup.class.getName()); // NOI18N
        testLookup.setAttribute("instanceCreate", lookupInstance); // NOI18N

        Lookup lookup = Lookups.forPath(TEST_FOLDER);

        lookup(lookup, (String) lookups.get(String.class), String.class);
        lookup(lookup, (Integer) lookups.get(Integer.class), Integer.class);
        lookup(lookup, (Character) lookups.get(Character.class), Character.class);
        lookup(lookup, (Double) lookups.get(Double.class), Double.class);
        lookup(lookup, (String) lookups.get(String.class), String.class);
    }

    private <T> void lookup(Lookup lookup, T expected, Class<T> clazz) {
        assertEquals(expected, lookup.lookup(clazz));

        Collection<? extends T> instances = lookup.lookup(new Lookup.Template<T>(clazz)).allInstances();
        assertEquals(1, instances.size());
        assertEquals(expected, instances.iterator().next());
    }
}

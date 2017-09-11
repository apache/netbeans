/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.core.startup;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static junit.framework.Assert.assertTrue;
import org.netbeans.JarClassLoader;
import org.netbeans.Stamps;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class UpdateAllResourcesTest extends NbTestCase{
    public UpdateAllResourcesTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());
        System.getProperties().remove("org.netbeans.core.update.all.resources");
        resetStamps();
    }

    public void testByDefaultTheArchiveIsUpdated() {
        assertTrue("Update was scheduled", Main.updateAllResources());
    }

    public void testNeverUpdate() {
        System.setProperty("org.netbeans.core.update.all.resources", "never");
        assertFalse("Update was not", Main.updateAllResources());
    }

    public void testUpdateAsNotPopulated() throws Exception {
        System.setProperty("org.netbeans.core.update.all.resources", "missing");
        populateCache(false);
        assertFalse("No previous all-resources.dat", JarClassLoader.isArchivePopulated());
        assertTrue("Performs the update", Main.updateAllResources());
    }

    public void testDontUpdateWhenPopulated() throws Exception {
        System.setProperty("org.netbeans.core.update.all.resources", "missing");
        populateCache(true);
        assertFalse("No need to update, everything is populated", Main.updateAllResources());
    }

    private static void populateCache(boolean prep) throws Exception {
        Method init = JarClassLoader.class.getDeclaredMethod("initializeCache");
        init.setAccessible(true);
        init.invoke(null);

        Field fld = JarClassLoader.class.getDeclaredField("archive");
        fld.setAccessible(true);
        Object obj = fld.get(null);
        assertNotNull("Archive is initialized", obj);
        
        Constructor<? extends Object> cnstr = obj.getClass().getDeclaredConstructor(boolean.class);
        cnstr.setAccessible(true);
        fld.set(null, cnstr.newInstance(prep));
        
        assertEquals("Previous all-resources.dat", prep, JarClassLoader.isArchivePopulated());
    }
    private static void resetStamps() throws Exception {
        final Method m = Stamps.class.getDeclaredMethod("main", String[].class);
        m.setAccessible(true);
        m.invoke(null, (Object) new String[]{"reset"});
    }
}

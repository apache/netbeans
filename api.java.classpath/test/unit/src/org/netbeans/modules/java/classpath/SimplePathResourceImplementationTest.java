/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.classpath;

import java.io.File;
import java.net.URL;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class SimplePathResourceImplementationTest extends NbTestCase {

    public SimplePathResourceImplementationTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testVerify() throws Exception {
        try {
            SimplePathResourceImplementation.verify(null,null);
            assertTrue("Verify should fail for null",false);
        } catch (IllegalArgumentException e) {
        }
        try {
            SimplePathResourceImplementation.verify(new URL("file:///tmp/foo.jar"),null);
            assertTrue("Verify should fail for non jar protocol file",false);
        } catch (IllegalArgumentException e) {
        }
        try {
            File f = new File(getWorkDir(),"test.jar");
            f.createNewFile();
            SimplePathResourceImplementation.verify(new URL(BaseUtilities.toURI(f).toString()+'/'),null);
            assertTrue("Verify should fail for non jar protocol file",false);
        } catch (IllegalArgumentException e) {
        }
        try {
            SimplePathResourceImplementation.verify(new URL("file:///tmp/foo"),null);
            assertTrue("Verify should fail for URLs not ending by /",false);
        } catch (IllegalArgumentException e) {
        }
        try {
            SimplePathResourceImplementation.verify(new URL("file:///tmp/../net/foo"),null);
            assertTrue("Verify should fail for URLs having ..",false);
        } catch (IllegalArgumentException e) {
        }
        try {
            SimplePathResourceImplementation.verify(new URL("file:///tmp/./foo"),null);
            assertTrue("Verify should fail for URLs having .",false);
        } catch (IllegalArgumentException e) {
        }
        try {
            SimplePathResourceImplementation.verify(new URL("file:////server/share/path/"),null);            
        } catch (IllegalArgumentException e) {
            assertTrue("Verify should not fail for UNC URL.",false);
        }
        try {
            SimplePathResourceImplementation.verify(new URL("file:////server/share/path/../foo/"),null);
            assertTrue("Verify should fail for UNC URLs having ..",false);
        } catch (IllegalArgumentException e) {
        }
        try {
            final File wd = getWorkDir();
            final File strangeFolder = new File(wd,"strange.jar");  //NOI18N
            strangeFolder.mkdirs();
            SimplePathResourceImplementation.verify(Utilities.toURI(strangeFolder).toURL(),null);
        } catch (IllegalArgumentException e) {
            assertTrue("Verify should not fail for .jar folder",false);
        }
        try {
            final File wd = getWorkDir();
            final File strangeFile = new File(wd,".jar");
            final URL url = FileUtil.urlForArchiveOrDir(strangeFile);
            SimplePathResourceImplementation.verify(url, null);
        } catch (IllegalArgumentException e) {
            assertTrue("Verify should not fail for .jar folder",false);
        }

        try {
            final File wd = getWorkDir();
            final File strangeFile = new File(wd,".jar");
            strangeFile.createNewFile();
            SimplePathResourceImplementation.verify(Utilities.toURI(strangeFile).toURL(), null);
            assertTrue("Verify should fail for .jar file",false);
        } catch (IllegalArgumentException e) {
        }
    }

}

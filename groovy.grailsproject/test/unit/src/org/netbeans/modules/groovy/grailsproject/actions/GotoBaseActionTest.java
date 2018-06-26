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
package org.netbeans.modules.groovy.grailsproject.actions;

import java.io.File;
import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Janicek
 */
public class GotoBaseActionTest extends NbTestCase {

    private static class GotoBaseActionImpl extends GotoBaseAction {

        public GotoBaseActionImpl(String name) {
            super(name);
        }

        @Override
        protected FileObject getTargetFO(String fileName, FileObject sourceFO) {
            return null;
        }

        @Override
        protected String getTargetFilePath(String filename, FileObject sourceFO) {
            return null;
        }
    }

    private static final GotoBaseAction gotoAction = new GotoBaseActionImpl("GotoBaseActionTest");


    public GotoBaseActionTest(String name) {
        super(name);
    }

    @Test
    public void testFindPackagePath1() throws IOException {
        File folder = new File(getWorkDir(), "/whatever/grails-app/domain/packagename");
        File file = new File(folder, "SomeDomainClass.groovy");

        setupFolder(folder);
        setupTestFile(file);
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file)); //NOI18N

        assertEquals("packagename", gotoAction.findPackagePath(fo));
    }

    @Test
    public void testFindPackagePath2() throws IOException {
        File folder = new File(getWorkDir(), "/whatever/grails-app/domain/packagename/secondarypkg");
        File file = new File(folder, "AnotherDomainClass.groovy");

        setupFolder(folder);
        setupTestFile(file);
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file)); //NOI18N

        assertEquals("packagename" + File.separator + "secondarypkg", gotoAction.findPackagePath(fo));
    }

    private void setupFolder(File folder) {
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                // If we are not able to create folders, we can't continue
                fail("Test folder couldn't be created for some reason!");
            }
        }
    }

    private void setupTestFile(File testFile) {
        if (!testFile.exists()) {
            try {
                if (!testFile.createNewFile()) {
                    fail("Testfile couldn't be created for some reason!");
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}

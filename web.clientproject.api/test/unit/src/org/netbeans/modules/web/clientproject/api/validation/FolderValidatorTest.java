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
package org.netbeans.modules.web.clientproject.api.validation;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.common.api.ValidationResult;

public class FolderValidatorTest extends NbTestCase {

    private File folder;


    public FolderValidatorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        File projectDir = new File(getWorkDir(), "project123");
        assertTrue(projectDir.mkdir());
        folder = new File(projectDir, "public_html");
        assertTrue(folder.mkdir());
    }

    public void testValidate() {
        ValidationResult result = new FolderValidator()
                .validateFolder(folder)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testNoFolder() {
        ValidationResult result = new FolderValidator()
                .validateFolder(null)
                .getResult();
        assertFalse(result.getErrors().isEmpty());
        assertEquals(FolderValidator.FOLDER, result.getErrors().get(0).getSource());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testFileAsFolder() throws Exception {
        File readme = new File(folder, "readme.txt");
        assertTrue(readme.createNewFile());
        ValidationResult result = new FolderValidator()
                .validateFolder(readme)
                .getResult();
        assertFalse(result.getErrors().isEmpty());
        assertEquals(FolderValidator.FOLDER, result.getErrors().get(0).getSource());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testNonExistingFolder() throws Exception {
        File subdir = new File(folder, "subdir");
        assertFalse(subdir.exists());
        ValidationResult result = new FolderValidator()
                .validateFolder(subdir)
                .getResult();
        assertFalse(result.getErrors().isEmpty());
        assertEquals(FolderValidator.FOLDER, result.getErrors().get(0).getSource());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testNonExistingFolderWithCustomName() throws Exception {
        String dirName = "sdf5465fd4sdf";
        File subdir = new File(folder, "subdir");
        assertFalse(subdir.exists());
        ValidationResult result = new FolderValidator()
                .validateFolder(subdir, dirName)
                .getResult();
        assertFalse(result.getErrors().isEmpty());
        ValidationResult.Message error = result.getErrors().get(0);
        assertEquals(FolderValidator.FOLDER, error.getSource());
        assertTrue(error.getMessage(), error.getMessage().contains(dirName));
        assertTrue(result.getWarnings().isEmpty());
    }

}
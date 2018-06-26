/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.completion;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class PHPCodeCompletion253290Test extends PHPCodeCompletionTestBase {

    public PHPCodeCompletion253290Test(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/tests253290/"))
            })
        );
    }

    public void testTraitedTraitInstance() throws Exception {
        checkCompletion("testfiles/completion/lib/tests253290/issue253290.php", "$this->^privateTraitedTraitField;", false);
    }

    public void testTraitedTraitStatic() throws Exception {
        checkCompletion("testfiles/completion/lib/tests253290/issue253290.php", "TraitedTrait::^$privateStaticBaseTrait1Field;", false);
    }

    public void testTraitedTraitStaticNotEnclosed() throws Exception {
        checkCompletion("testfiles/completion/lib/tests253290/issue253290.php", "TraitedTrait::^$publicStaticTraitedTraitField;", false);
    }

    public void testMultipleUsedTraitInstance() throws Exception {
        checkCompletion("testfiles/completion/lib/tests253290/issue253290.php", "$this->^privateMultipleUsedTraitField;", false);
    }

    public void testMultipleUsedTraitStatic() throws Exception {
        checkCompletion("testfiles/completion/lib/tests253290/issue253290.php", "MultipleUsedTrait::^$privateStaticMultipleUsedTraitField;", false);
    }

    public void testMultipleUsedTraitStaticNotEnclosed() throws Exception {
        checkCompletion("testfiles/completion/lib/tests253290/issue253290.php", "MultipleUsedTrait::^$publicStaticMultipleUsedTraitField;", false);
    }

    public void testClassInstance() throws Exception {
        checkCompletion("testfiles/completion/lib/tests253290/issue253290.php", "$this->^privateBaseClassField;", false);
    }

    public void testClassInstanceNotEnclosed() throws Exception {
        checkCompletion("testfiles/completion/lib/tests253290/issue253290.php", "$baseClass->^publicBaseClassMethod();", false);
    }

    public void testClassStatic() throws Exception {
        checkCompletion("testfiles/completion/lib/tests253290/issue253290.php", "BaseClass::^$privateStaticBaseTrait1Field;", false);
    }

    public void testClassStaticNotEnclosed() throws Exception {
        checkCompletion("testfiles/completion/lib/tests253290/issue253290.php", "BaseClass::^$publicStaticBaseTrait1Field;", false);
    }

    public void testExtendedClassInstance() throws Exception {
        // cannot use private methods and fields of the parent class and the traits
        checkCompletion("testfiles/completion/lib/tests253290/issue253290.php", "$this->^privateExtendedClassField;", false);
    }

    public void testExtendedClassInstanceNotEnclosed() throws Exception {
        checkCompletion("testfiles/completion/lib/tests253290/issue253290.php", "$extendedClass->^publicExtendedClassMethod();", false);
    }

    public void testExtendedClassStatic() throws Exception {
        checkCompletion("testfiles/completion/lib/tests253290/issue253290.php", "ExtendedClass::^privateStaticExtendedClassMethod();", false);
    }

    public void testExtendedClassStaticNotEnclosed() throws Exception {
        checkCompletion("testfiles/completion/lib/tests253290/issue253290.php", "ExtendedClass::^$publicStaticBaseTrait1Field;", false);
    }

    public void testTraitedExtendedClassInstance() throws Exception {
        // cannot use private methods and fields of the parent class and the traits
        checkCompletion("testfiles/completion/lib/tests253290/issue253290.php", "$this->^privateTraitedExtendedClassField;", false);
    }

    public void testTraitedExtendedClassInstanceNotEnclosed() throws Exception {
        checkCompletion("testfiles/completion/lib/tests253290/issue253290.php", "$traitedExtendedClass->^publicTraitedExtendedClassMethod();", false);
    }

    public void testTraitedExtendedClassStatic() throws Exception {
        checkCompletion("testfiles/completion/lib/tests253290/issue253290.php", "TraitedExtendedClass::^$privateStaticBaseTrait2Field;", false);
    }

    public void testTraitedExtendedClassStaticNotEnclosed() throws Exception {
        checkCompletion("testfiles/completion/lib/tests253290/issue253290.php", "TraitedExtendedClass::^$publicStaticBaseTrait1Field;", false);
    }

}

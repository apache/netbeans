/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

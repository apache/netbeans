/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.editor.codegen;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.JTextArea;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class SelectedPropertyMethodsCreatorTest extends PHPTestBase {

    public SelectedPropertyMethodsCreatorTest(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), getTestFolderPath()))
            })
        );
    }

    private CGSInfo getCgsInfo(String caretLine) {
        assert caretLine != null;
        FileObject testFile = getTestFile(getTestPath());
        Source testSource = getTestSource(testFile);
        JTextArea ta = new JTextArea(testSource.getDocument(false));
        int caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
        ta.setCaretPosition(caretOffset);
        return CGSInfo.getCGSInfo(ta);
    }

    private <T extends Property> List<T> selectAllProperties(List<T> properties) {
        for (Property property : properties) {
            property.setSelected(true);
        }
        return properties;
    }

    private <T extends Property> List<T> selectProperties(List<T> properties, String... mask) {
        boolean anySelected = false;
        for (Property property : properties) {
            String name = property.getName();
            for (String string : mask) {
                if (name.contains(string)) {
                    anySelected = true;
                    property.setSelected(true);
                }
            }
        }
        assertTrue("Something should be selected from " + Arrays.toString(mask), anySelected);
        return properties;
    }

    private String getBaseName() {
        String name = getName();
        int indexOf = name.indexOf('_');
        if (indexOf == -1) {
            return name;
        }
        return name.substring(0, indexOf);
    }

    private String getTestFolderPath() {
        return "testfiles/codegen/" + getBaseName();
    }

    private String getTestPath() {
        return getTestFolderPath() + "/" + getBaseName()+ ".php";
    }

    private void checkResult(String result) throws Exception {
        assertDescriptionMatches(getTestPath(), result, false, "." + getName() + ".codegen");
    }

    public void testInstancePropertyGetter() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}");
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(selectAllProperties(cgsInfo.getPossibleGetters()), new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)));
    }

    public void testInstancePropertySetter() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}");
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(selectAllProperties(cgsInfo.getPossibleSetters()), new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)));
    }

    public void testInstancePropertySetterWithFluentInterface() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}");
        cgsInfo.setFluentSetter(true);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(selectAllProperties(cgsInfo.getPossibleSetters()), new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)));
    }

    public void testClassPropertyGetter() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}");
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(selectAllProperties(cgsInfo.getPossibleGetters()), new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)));
    }

    public void testClassPropertySetter() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}");
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(selectAllProperties(cgsInfo.getPossibleSetters()), new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)));
    }

    public void testClassPropertySetterWithFluentInterface() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}");
        cgsInfo.setFluentSetter(true);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(selectAllProperties(cgsInfo.getPossibleSetters()), new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)));
    }

    public void testClassPropertyGetterWithoutPublic() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}");
        cgsInfo.setPublicModifier(false);
        checkResult(new SelectedPropertyMethodsCreator().create(selectAllProperties(cgsInfo.getPossibleGetters()), new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)));
    }

    public void testClassPropertySetterWithoutPublic() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}");
        cgsInfo.setPublicModifier(false);
        checkResult(new SelectedPropertyMethodsCreator().create(selectAllProperties(cgsInfo.getPossibleSetters()), new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)));
    }

    public void testInstanceImplementMethod_01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar implements Foo {^");
        cgsInfo.setPhpVersion(PhpVersion.PHP_70);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    public void testInstanceImplementMethod_02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar implements Foo {^");
        cgsInfo.setPhpVersion(PhpVersion.PHP_55);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    // #270237
    public void testInstanceImplementMethodWithNullableType_01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar implements Foo {^");
        cgsInfo.setPhpVersion(PhpVersion.PHP_71);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    public void testInstanceImplementMethodWithNullableType_02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar implements Foo {^");
        cgsInfo.setPhpVersion(PhpVersion.PHP_55);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    public void testInstanceOverrideMethod_01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar extends Foo {^");
        cgsInfo.setPhpVersion(PhpVersion.PHP_70);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    public void testInstanceOverrideMethod_02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar extends Foo {^");
        cgsInfo.setPhpVersion(PhpVersion.PHP_56);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    // #270237
    public void testInstanceOverrideMethodWithNullableType_01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar extends Foo {^");
        cgsInfo.setPhpVersion(PhpVersion.PHP_71);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    public void testInstanceOverrideMethodWithNullableType_02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar extends Foo {^");
        cgsInfo.setPhpVersion(PhpVersion.PHP_56);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    public void testGetterWithType_01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Foo {^");
        cgsInfo.setPhpVersion(PhpVersion.PHP_70);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleGetters()), new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)));
    }

    public void testGetterWithType_02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Foo {^");
        cgsInfo.setPhpVersion(PhpVersion.PHP_55);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleGetters()), new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)));
    }

    public void testGetterWithMoreTypes_01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Foo {^");
        cgsInfo.setPhpVersion(PhpVersion.PHP_70);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleGetters()), new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)));
    }

    public void testGetterWithMoreTypes_02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Foo {^");
        cgsInfo.setPhpVersion(PhpVersion.PHP_56);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleGetters()), new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)));
    }

    public void testIssue267227() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class DerivedClass extends BaseClass {^");
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "__construct", "myMethod"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    // PHP 7.4
    public void testSerializeUnserializeMagicMethod() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Foo {^");
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "__serialize", "__unserialize"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

}

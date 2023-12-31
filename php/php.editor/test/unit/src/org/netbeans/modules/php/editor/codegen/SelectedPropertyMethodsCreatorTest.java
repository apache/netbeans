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
        return getCgsInfo(caretLine, PhpVersion.PHP_56);
    }

    private CGSInfo getCgsInfo(String caretLine, PhpVersion phpVersion) {
        assert caretLine != null;
        FileObject testFile = getTestFile(getTestPath());
        Source testSource = getTestSource(testFile);
        JTextArea ta = new JTextArea(testSource.getDocument(false));
        int caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
        ta.setCaretPosition(caretOffset);
        return CGSInfo.getCGSInfo(ta, phpVersion);
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
        CGSInfo cgsInfo = getCgsInfo("class Bar implements Foo {^", PhpVersion.PHP_70);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    public void testInstanceImplementMethod_02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar implements Foo {^", PhpVersion.PHP_55);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    // #270237
    public void testInstanceImplementMethodWithNullableType_01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar implements Foo {^", PhpVersion.PHP_71);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    public void testInstanceImplementMethodWithNullableType_02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar implements Foo {^", PhpVersion.PHP_55);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    public void testInstanceOverrideMethod_01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar extends Foo {^", PhpVersion.PHP_70);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    public void testInstanceOverrideMethod_02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar extends Foo {^", PhpVersion.PHP_56);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    // #270237
    public void testInstanceOverrideMethodWithNullableType_01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar extends Foo {^", PhpVersion.PHP_71);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    public void testInstanceOverrideMethodWithNullableType_02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar extends Foo {^", PhpVersion.PHP_56);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    public void testInstanceOverrideMethodWithGuessingBoolType_01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar extends Foo {^", PhpVersion.PHP_70);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    public void testInstanceOverrideMethodWithGuessingBoolType_02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar extends Foo {^", PhpVersion.PHP_56);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    public void testInstanceOverrideMethodWithGuessingArrayType_01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar extends Foo {^", PhpVersion.PHP_70);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    public void testInstanceOverrideMethodWithGuessingArrayType_02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Bar extends Foo {^", PhpVersion.PHP_56);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "myFoo"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    public void testGetterWithType_01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Foo {^", PhpVersion.PHP_70);
        cgsInfo.setPublicModifier(false);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleGetters()), new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)));
    }

    public void testGetterWithType_02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Foo {^", PhpVersion.PHP_55);
        cgsInfo.setPublicModifier(false);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleGetters()), new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)));
    }

    public void testGetterWithMoreTypes_01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Foo {^", PhpVersion.PHP_70);
        cgsInfo.setPublicModifier(false);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleGetters()), new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)));
    }

    public void testGetterWithMoreTypes_02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Foo {^", PhpVersion.PHP_56);
        cgsInfo.setPublicModifier(false);
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
        CGSInfo cgsInfo = getCgsInfo("class Foo {^", PhpVersion.PHP_74);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "__serialize", "__unserialize"), new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)));
    }

    // NETBEANS-53
    // getter
    public void testTypedPropertiesGetter_PHP56() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_56);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(selectAllProperties(cgsInfo.getPossibleGetters()), new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)));
    }

    public void testTypedPropertiesGetter_PHP70() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_70);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(selectAllProperties(cgsInfo.getPossibleGetters()), new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)));
    }

    public void testTypedPropertiesGetter_PHP71() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_71);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(selectAllProperties(cgsInfo.getPossibleGetters()), new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)));
    }

    public void testTypedPropertiesGetter_PHP74() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_74);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(selectAllProperties(cgsInfo.getPossibleGetters()), new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)));
    }

    // setter
    public void testTypedPropertiesSetter_PHP56() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_56);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(selectAllProperties(cgsInfo.getPossibleSetters()), new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)));
    }

    public void testTypedPropertiesSetter_PHP70() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_70);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(selectAllProperties(cgsInfo.getPossibleSetters()), new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)));
    }

    public void testTypedPropertiesSetter_PHP70Fluent() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_70);
        cgsInfo.setPublicModifier(true);
        cgsInfo.setFluentSetter(true);
        checkResult(new SelectedPropertyMethodsCreator().create(selectAllProperties(cgsInfo.getPossibleSetters()), new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)));
    }

    public void testTypedPropertiesSetter_PHP71() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_71);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(selectAllProperties(cgsInfo.getPossibleSetters()), new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)));
    }

    public void testTypedPropertiesSetter_PHP71Fluent() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_71);
        cgsInfo.setPublicModifier(true);
        cgsInfo.setFluentSetter(true);
        checkResult(new SelectedPropertyMethodsCreator().create(selectAllProperties(cgsInfo.getPossibleSetters()), new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)));
    }

    public void testTypedPropertiesSetter_PHP74() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_74);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(selectAllProperties(cgsInfo.getPossibleSetters()), new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)));
    }

    // constructor
    public void testTypedPropertiesConstructor_PHP56() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_56);
        cgsInfo.setPublicModifier(true);
        selectAllProperties(cgsInfo.getProperties());
        checkResult(CGSGenerator.GenType.CONSTRUCTOR.getTemplateText(cgsInfo));
    }

    public void testTypedPropertiesConstructor_PHP70() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_70);
        cgsInfo.setPublicModifier(true);
        selectAllProperties(cgsInfo.getProperties());
        checkResult(CGSGenerator.GenType.CONSTRUCTOR.getTemplateText(cgsInfo));
    }

    public void testTypedPropertiesConstructor_PHP71() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_71);
        cgsInfo.setPublicModifier(true);
        selectAllProperties(cgsInfo.getProperties());
        checkResult(CGSGenerator.GenType.CONSTRUCTOR.getTemplateText(cgsInfo));
    }

    public void testTypedPropertiesConstructor_PHP74() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_74);
        cgsInfo.setPublicModifier(true);
        selectAllProperties(cgsInfo.getProperties());
        checkResult(CGSGenerator.GenType.CONSTRUCTOR.getTemplateText(cgsInfo));
    }

    // NETBEANS-4443 PHP 8.0
    // the same results regardless of versions if union type is used
    // constructor
    public void testUnionTypesConstructor_PHP80() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_80);
        cgsInfo.setPublicModifier(true);
        selectAllProperties(cgsInfo.getProperties());
        checkResult(CGSGenerator.GenType.CONSTRUCTOR.getTemplateText(cgsInfo));
    }

    public void testUnionTypesConstructor_PHP74() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_74);
        cgsInfo.setPublicModifier(true);
        selectAllProperties(cgsInfo.getProperties());
        checkResult(CGSGenerator.GenType.CONSTRUCTOR.getTemplateText(cgsInfo));
    }

    // getter
    public void testUnionTypesGetter_PHP80() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_80);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleGetters()),
                new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)
        ));
    }

    public void testUnionTypesGetter_PHP74() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_74);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleGetters()),
                new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)
        ));
    }

    // setter
    public void testUnionTypesSetter_PHP80() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_80);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleSetters()),
                new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)
        ));
    }

    public void testUnionTypesSetter_PHP74() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_74);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleSetters()),
                new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)
        ));
    }

    public void testUnionTypesOverrideMethod01_PHP80() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Child extends OverrideMethodTest {^", PhpVersion.PHP_80);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testUnionTypesOverrideMethod01_PHP74() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Child extends OverrideMethodTest {^", PhpVersion.PHP_74);
        List<MethodProperty> possibleMethods = cgsInfo.getPossibleMethods();
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(possibleMethods, "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testUnionTypesOverrideMethod02_PHP80() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Child extends OverrideMethodTest {^", PhpVersion.PHP_80);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testUnionTypesOverrideMethod02_PHP74() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Child extends OverrideMethodTest {^", PhpVersion.PHP_74);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testUnionTypesOverrideMethod03_PHP80() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Child extends OverrideMethodTest {^", PhpVersion.PHP_80);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testUnionTypesOverrideMethod03_PHP74() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Child extends OverrideMethodTest {^", PhpVersion.PHP_74);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testUnionTypesOverrideMethodSpecialTypes01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Grandchild extends Child {^", PhpVersion.PHP_80);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "test"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testUnionTypesOverrideMethodSpecialTypes02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Grandchild extends Child {^", PhpVersion.PHP_80);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "test"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testUnionTypesOverrideMethodSpecialTypes03() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Grandchild extends Child {^", PhpVersion.PHP_80);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "test"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testUnionTypesOverrideMethodSpecialTypes04() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Grandchild extends Child {^", PhpVersion.PHP_80);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "test"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testUnionTypesImplementMethod01_PHP80() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Implement implements ImplementMethodTest {^", PhpVersion.PHP_80);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "test"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testUnionTypesImplementMethod01_PHP74() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Implement implements ImplementMethodTest {^", PhpVersion.PHP_74);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "test"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testUnionTypesImplementMethod02_PHP80() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Implement implements ImplementMethodTest {^", PhpVersion.PHP_80);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testUnionTypesImplementMethod02_PHP74() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Implement implements ImplementMethodTest {^", PhpVersion.PHP_74);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testUnionTypesImplementMethod03_PHP80() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Implement implements ImplementMethodTest {^", PhpVersion.PHP_80);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testUnionTypesImplementMethod03_PHP74() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Implement implements ImplementMethodTest {^", PhpVersion.PHP_74);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testUnionTypesImplementMethod04_PHP80() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Implement extends ImplementMethodTest {^", PhpVersion.PHP_80);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testUnionTypesImplementMethod04_PHP74() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Implement extends ImplementMethodTest {^", PhpVersion.PHP_74);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testStaticReturnTypeOverrideMethod01_PHP80() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class ChildClass extends ParentClass {^", PhpVersion.PHP_80);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "test"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testStaticReturnTypeImplementMethod01_PHP80() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class TestClass implements TestInterface {^", PhpVersion.PHP_80);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "test"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testMixedTypeOverrideMethod01_PHP80() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Child extends MixedType {^", PhpVersion.PHP_80);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "test"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testMixedTypeImplementMethod01_PHP80() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Child implements MixedType {^", PhpVersion.PHP_80);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "test"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testConstructorPropertyPromotionGetter_01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^// test1", PhpVersion.PHP_80);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleGetters()),
                new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)
        ));
    }

    public void testConstructorPropertyPromotionGetter_02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^// test2", PhpVersion.PHP_80);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleGetters()),
                new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)
        ));
    }

    public void testConstructorPropertyPromotionGetter_03() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^// test3", PhpVersion.PHP_80);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleGetters()),
                new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)
        ));
    }

    public void testConstructorPropertyPromotionSetter_01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^// test1", PhpVersion.PHP_80);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleSetters()),
                new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)
        ));
    }

    public void testConstructorPropertyPromotionSetter_02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^// test2", PhpVersion.PHP_80);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleSetters()),
                new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)
        ));
    }

    public void testConstructorPropertyPromotionSetter_03() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^// test3", PhpVersion.PHP_80);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleSetters()),
                new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)
        ));
    }

    public void testConstructorPropertyPromotionOverrideConstructor_01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Child extends ConstructorPropertyPromotionParent {^", PhpVersion.PHP_80);
        List<MethodProperty> possibleMethods = cgsInfo.getPossibleMethods();
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(possibleMethods, "__construct"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testNetbeans5370_01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class TestClass1 extends TestAbstractClass {^", PhpVersion.PHP_70);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "test"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testNetbeans5370_02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class TestClass2 implements TestInterface {^", PhpVersion.PHP_70);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "test"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    // NETBEANS-5599 PHP 8.1
    public void testIntersectionTypesConstructor() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_81);
        cgsInfo.setPublicModifier(true);
        selectAllProperties(cgsInfo.getInstanceProperties());
        checkResult(CGSGenerator.GenType.CONSTRUCTOR.getTemplateText(cgsInfo));
    }

    public void testIntersectionTypesGetter() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_81);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleGetters()),
                new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)
        ));
    }

    public void testIntersectionTypesSetter() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_81);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleSetters()),
                new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)
        ));
    }

    public void testIntersectionTypesOverrideMethod01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Child extends OverrideMethodTest {^", PhpVersion.PHP_81);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testIntersectionTypesOverrideMethod02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Child extends OverrideMethodTest {^", PhpVersion.PHP_81);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testIntersectionTypesOverrideMethod03() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Child extends OverrideMethodTest {^", PhpVersion.PHP_81);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testIntersectionTypesImplementMethod01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Implement implements ImplementMethodTest {^", PhpVersion.PHP_81);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "test"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testIntersectionTypesImplementMethod02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Implement implements ImplementMethodTest {^", PhpVersion.PHP_81);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testIntersectionTypesImplementMethod03() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Implement implements ImplementMethodTest {^", PhpVersion.PHP_81);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testIntersectionTypesImplementMethod04() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Implement extends ImplementMethodTest {^", PhpVersion.PHP_81);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    // GH-4725 PHP 8.2
    public void testDNFTypesConstructor() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_82);
        cgsInfo.setPublicModifier(true);
        selectAllProperties(cgsInfo.getInstanceProperties());
        checkResult(CGSGenerator.GenType.CONSTRUCTOR.getTemplateText(cgsInfo));
    }

    public void testDNFTypesGetter() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_82);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleGetters()),
                new SinglePropertyMethodCreator.SingleGetterCreator(cgsInfo)
        ));
    }

    public void testDNFTypesSetter() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("^}", PhpVersion.PHP_82);
        cgsInfo.setPublicModifier(true);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectAllProperties(cgsInfo.getPossibleSetters()),
                new SinglePropertyMethodCreator.SingleSetterCreator(cgsInfo)
        ));
    }

    public void testDNFTypesOverrideMethod01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Child extends OverrideMethodTest {^", PhpVersion.PHP_82);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testDNFTypesOverrideMethod02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Child extends OverrideMethodTest {^", PhpVersion.PHP_82);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testDNFTypesOverrideMethod03() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Child extends OverrideMethodTest {^", PhpVersion.PHP_82);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testDNFTypesImplementMethod01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Implement implements ImplementMethodTest {^", PhpVersion.PHP_82);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "test"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testDNFTypesImplementMethod02() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Implement implements ImplementMethodTest {^", PhpVersion.PHP_82);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testDNFTypesImplementMethod03() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Implement implements ImplementMethodTest {^", PhpVersion.PHP_82);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testDNFTypesImplementMethod04() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("class Implement extends ImplementMethodTest {^", PhpVersion.PHP_82);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "testMethod"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testOverrideAttribute01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("    // test^", PhpVersion.PHP_83);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "test"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testOverrideAttribute01_PHP82() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("    // test^", PhpVersion.PHP_82);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "test"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testOverrideAttributeAnonClass01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("    // test^", PhpVersion.PHP_83);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "test"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testOverrideAttributeAnonClass01_PHP82() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("    // test^", PhpVersion.PHP_82);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "test"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testOverrideAttributeEnum01() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("    // test^", PhpVersion.PHP_83);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), ""),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testOverrideAttributeEnum01_PHP82() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("    // test^", PhpVersion.PHP_82);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), ""),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testMagicMethods01_PHP56() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("    // test^", PhpVersion.PHP_56);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), ""),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testMagicMethods01_PHP70() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("    // test^", PhpVersion.PHP_70);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), ""),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testMagicMethods01_PHP71() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("    // test^", PhpVersion.PHP_71);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), ""),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testMagicMethods01_PHP72() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("    // test^", PhpVersion.PHP_72);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), ""),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testMagicMethods01_PHP73() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("    // test^", PhpVersion.PHP_73);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), ""),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testMagicMethods01_PHP74() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("    // test^", PhpVersion.PHP_74);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), ""),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testMagicMethods01_PHP80() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("    // test^", PhpVersion.PHP_80);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), ""),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testMagicMethods01_PHP81() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("    // test^", PhpVersion.PHP_81);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), ""),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testMagicMethods01_PHP82() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("    // test^", PhpVersion.PHP_82);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), ""),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testMagicMethods01_PHP83() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("    // test^", PhpVersion.PHP_83);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), ""),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testMagicMethodToString01_PHP83() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("    // test^", PhpVersion.PHP_83);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "__toString"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }

    public void testMagicMethodToString02_PHP83() throws Exception {
        CGSInfo cgsInfo = getCgsInfo("    // test^", PhpVersion.PHP_83);
        checkResult(new SelectedPropertyMethodsCreator().create(
                selectProperties(cgsInfo.getPossibleMethods(), "__toString"),
                new SinglePropertyMethodCreator.InheritedMethodCreator(cgsInfo)
        ));
    }
}

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
package org.netbeans.modules.php.editor.elements;

import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.impl.ModelTestBase;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class TypeNameResolverImplTest extends ModelTestBase {

    private static final int TEST_TIMEOUT = Integer.getInteger("nb.php.test.timeout", 100000); //NOI18N

    public TypeNameResolverImplTest(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return TEST_TIMEOUT;
    }

    private int getResolvingOffset(final String preparedTestFile) {
        int indexOf = preparedTestFile.indexOf("/*^*/");
        assert indexOf != -1;
        return indexOf;
    }

    public void testNull_01() throws Exception {
        QualifiedName toResolve = QualifiedName.create("\\Test\\Omg");
        QualifiedName expected = QualifiedName.create("\\Test\\Omg");
        QualifiedName actual = TypeNameResolverImpl.forNull().resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testNull_02() throws Exception {
        QualifiedName toResolve = QualifiedName.create("Test\\Omg");
        QualifiedName expected = QualifiedName.create("Test\\Omg");
        QualifiedName actual = TypeNameResolverImpl.forNull().resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testNull_03() throws Exception {
        QualifiedName toResolve = QualifiedName.create("Omg");
        QualifiedName expected = QualifiedName.create("Omg");
        QualifiedName actual = TypeNameResolverImpl.forNull().resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testUnqualifiedName_01() throws Exception {
        QualifiedName toResolve = QualifiedName.create("Omg");
        QualifiedName expected = QualifiedName.create("Omg");
        QualifiedName actual = TypeNameResolverImpl.forUnqualifiedName().resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testUnqualifiedName_02() throws Exception {
        QualifiedName toResolve = QualifiedName.create("Foo\\Omg");
        QualifiedName expected = QualifiedName.create("Omg");
        QualifiedName actual = TypeNameResolverImpl.forUnqualifiedName().resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testUnqualifiedName_03() throws Exception {
        QualifiedName toResolve = QualifiedName.create("Foo\\Bar\\Omg");
        QualifiedName expected = QualifiedName.create("Omg");
        QualifiedName actual = TypeNameResolverImpl.forUnqualifiedName().resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testUnqualifiedName_04() throws Exception {
        QualifiedName toResolve = QualifiedName.create("\\Foo\\Bar\\Omg");
        QualifiedName expected = QualifiedName.create("Omg");
        QualifiedName actual = TypeNameResolverImpl.forUnqualifiedName().resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testFullyQualifiedName_01() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testFullyQualifiedName_01.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("Omg");
        QualifiedName expected = QualifiedName.create("\\Test\\Omg");
        QualifiedName actual = TypeNameResolverImpl.forFullyQualifiedName(namespaceScope, offset).resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testQualifiedName_01() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testQualifiedName_01.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("\\Test\\Foo");
        QualifiedName expected = QualifiedName.create("Foo");
        QualifiedName actual = TypeNameResolverImpl.forQualifiedName(namespaceScope, offset).resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testQualifiedName_02() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testQualifiedName_02.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("\\Foo\\Bar\\Baz\\Bat");
        QualifiedName expected = QualifiedName.create("Bar\\Baz\\Bat");
        QualifiedName actual = TypeNameResolverImpl.forQualifiedName(namespaceScope, offset).resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testQualifiedName_03() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testQualifiedName_03.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("\\Test2\\Omg");
        QualifiedName expected = QualifiedName.create("Omg");
        QualifiedName actual = TypeNameResolverImpl.forQualifiedName(namespaceScope, offset).resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testQualifiedName_04() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testQualifiedName_04.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("\\Foo\\Bar\\Baz\\Bat");
        QualifiedName expected = QualifiedName.create("Baz\\Bat");
        QualifiedName actual = TypeNameResolverImpl.forQualifiedName(namespaceScope, offset).resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testQualifiedName_05() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testQualifiedName_05.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("\\Foo\\Bar\\Baz\\Bat");
        QualifiedName expected = QualifiedName.create("Bar\\Baz\\Bat");
        QualifiedName actual = TypeNameResolverImpl.forQualifiedName(namespaceScope, offset).resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testQualifiedName_06() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testQualifiedName_06.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("\\Foo\\Bar\\Baz\\Bat");
        QualifiedName expected = QualifiedName.create("Foo\\Bar\\Baz\\Bat");
        QualifiedName actual = TypeNameResolverImpl.forQualifiedName(namespaceScope, offset).resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testQualifiedName_07() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testQualifiedName_07.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("\\Foo\\Bar\\Baz\\Bat");
        QualifiedName expected = QualifiedName.create("Foo\\Bar\\Baz\\Bat");
        QualifiedName actual = TypeNameResolverImpl.forQualifiedName(namespaceScope, offset).resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testQualifiedName_08() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testQualifiedName_08.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("Bar\\Baz\\Bat");
        QualifiedName expected = QualifiedName.create("Test\\Bar\\Baz\\Bat");
        QualifiedName actual = TypeNameResolverImpl.forQualifiedName(namespaceScope, offset).resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testQualifiedName_09() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testQualifiedName_09.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("Bat");
        QualifiedName expected = QualifiedName.create("Test\\Bat");
        QualifiedName actual = TypeNameResolverImpl.forQualifiedName(namespaceScope, offset).resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testQualifiedName_10() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testQualifiedName_10.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("Alias\\Bat");
        QualifiedName expected = QualifiedName.create("Alias\\Bat");
        QualifiedName actual = TypeNameResolverImpl.forQualifiedName(namespaceScope, offset).resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testQualifiedName_11() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testQualifiedName_11.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("Bar\\Baz\\Bat");
        QualifiedName expected = QualifiedName.create("Bar\\Baz\\Bat");
        QualifiedName actual = TypeNameResolverImpl.forQualifiedName(namespaceScope, offset).resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testQualifiedName_12() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testQualifiedName_12.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("\\Test\\Foo\\Bar\\Baz");
        QualifiedName expected = QualifiedName.create("Bar\\Baz");
        QualifiedName actual = TypeNameResolverImpl.forQualifiedName(namespaceScope, offset).resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testSmartName_fail_01() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testSmartName_fail.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("Bat");
        try {
            TypeNameResolverImpl.forSmartName(namespaceScope, offset).resolve(toResolve);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    public void testSmartName_fail_02() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testSmartName_fail.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("Baz\\Bat");
        try {
            TypeNameResolverImpl.forSmartName(namespaceScope, offset).resolve(toResolve);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    public void testSmartName_01() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testSmartName_01.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("\\Foo\\Bar\\Baz\\Bat");
        QualifiedName expected = QualifiedName.create("Bar\\Baz\\Bat");
        QualifiedName actual = TypeNameResolverImpl.forSmartName(namespaceScope, offset).resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testSmartName_02() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testSmartName_02.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("\\Omg\\Fq\\Name");
        QualifiedName expected = QualifiedName.create("\\Omg\\Fq\\Name");
        QualifiedName actual = TypeNameResolverImpl.forSmartName(namespaceScope, offset).resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testSmartName_03() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testSmartName_03.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("\\Foo\\Bar\\Baz\\Bat");
        QualifiedName expected = QualifiedName.create("Baz\\Bat");
        QualifiedName actual = TypeNameResolverImpl.forSmartName(namespaceScope, offset).resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testSmartName_04() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testSmartName_04.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("\\Foo\\Bar\\Baz\\Bat");
        QualifiedName expected = QualifiedName.create("Bat");
        QualifiedName actual = TypeNameResolverImpl.forSmartName(namespaceScope, offset).resolve(toResolve);
        assertEquals(expected, actual);
    }

    public void testSmartName_05() throws Exception {
        String filePath = "testfiles/elements/typenameresolver/testSmartName_05.php";
        String preparedTestFile = prepareTestFile(filePath);
        Model model = getModel(getTestSource(filePath));
        int offset = getResolvingOffset(preparedTestFile);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);

        QualifiedName toResolve = QualifiedName.create("\\Test\\Foo\\Bar\\Baz");
        QualifiedName expected = QualifiedName.create("Bar\\Baz");
        QualifiedName actual = TypeNameResolverImpl.forSmartName(namespaceScope, offset).resolve(toResolve);
        assertEquals(expected, actual);
    }

}

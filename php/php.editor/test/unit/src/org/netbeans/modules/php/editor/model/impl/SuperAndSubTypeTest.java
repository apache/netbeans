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
package org.netbeans.modules.php.editor.model.impl;

import java.util.List;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TypeScope;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class SuperAndSubTypeTest extends ModelTestBase {

    public SuperAndSubTypeTest(String testName) {
        super(testName);
    }

    private Model getModel() throws Exception {
        return getModel(getTestSource("testfiles/model/superandsubtype/" + getName() + ".php"));
    }

    private TypeScope getSuperType(final Model model) {
        List<? extends TypeScope> findSuperTypes = model.getIndexScope().findTypes(QualifiedName.create("\\TestNameSpace\\Super"));
        assertFalse(findSuperTypes.isEmpty());
        TypeScope possibleSuperType = ModelUtils.getFirst(findSuperTypes);
        assertNotNull(possibleSuperType);
        return possibleSuperType;
    }

    private TypeScope getSubType(final Model model) {
        List<? extends TypeScope> findSubTypes = model.getIndexScope().findTypes(QualifiedName.create("\\TestNameSpace\\Sub"));
        assertFalse(findSubTypes.isEmpty());
        TypeScope possibleSubType = ModelUtils.getFirst(findSubTypes);
        assertNotNull(possibleSubType);
        return possibleSubType;
    }

    public void testSuperType_01() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSuperType.isSuperTypeOf(possibleSubType));
    }

    public void testSuperType_02() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSuperType.isSuperTypeOf(possibleSubType));
    }

    public void testSuperType_03() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertFalse(possibleSuperType.isSuperTypeOf(possibleSubType));
    }

    public void testSuperType_04() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSuperType.isSuperTypeOf(possibleSubType));
    }

    public void testSuperType_05() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSuperType.isSuperTypeOf(possibleSubType));
    }

    public void testSuperType_06() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertFalse(possibleSuperType.isSuperTypeOf(possibleSubType));
    }

    public void testSuperType_07() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSuperType.isSuperTypeOf(possibleSubType));
    }

    public void testSuperType_08() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSuperType.isSuperTypeOf(possibleSubType));
    }

    public void testSuperType_09() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSuperType.isSuperTypeOf(possibleSubType));
    }

    public void testSuperType_10() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertFalse(possibleSuperType.isSuperTypeOf(possibleSubType));
    }

    public void testSuperType_11() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertFalse(possibleSuperType.isSuperTypeOf(possibleSubType));
    }

    public void testSuperType_12() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSuperType.isSuperTypeOf(possibleSubType));
    }

    public void testSuperType_13() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertFalse(possibleSuperType.isSuperTypeOf(possibleSubType));
    }

    public void testSuperType_14() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertFalse(possibleSuperType.isSuperTypeOf(possibleSubType));
    }

    public void testSuperType_15() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSuperType.isSuperTypeOf(possibleSubType));
    }

    public void testSuperType_16() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSuperType.isSuperTypeOf(possibleSubType));
    }

    public void testSubType_01() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSubType.isSubTypeOf(possibleSuperType));
    }

    public void testSubType_02() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSubType.isSubTypeOf(possibleSuperType));
    }

    public void testSubType_03() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertFalse(possibleSubType.isSubTypeOf(possibleSuperType));
    }

    public void testSubType_04() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSubType.isSubTypeOf(possibleSuperType));
    }

    public void testSubType_05() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSubType.isSubTypeOf(possibleSuperType));
    }

    public void testSubType_06() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertFalse(possibleSubType.isSubTypeOf(possibleSuperType));
    }

    public void testSubType_07() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSubType.isSubTypeOf(possibleSuperType));
    }

    public void testSubType_08() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSubType.isSubTypeOf(possibleSuperType));
    }

    public void testSubType_09() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSubType.isSubTypeOf(possibleSuperType));
    }

    public void testSubType_10() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertFalse(possibleSubType.isSubTypeOf(possibleSuperType));
    }

    public void testSubType_11() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertFalse(possibleSubType.isSubTypeOf(possibleSuperType));
    }

    public void testSubType_12() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSubType.isSubTypeOf(possibleSuperType));
    }

    public void testSubType_13() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertFalse(possibleSubType.isSubTypeOf(possibleSuperType));
    }

    public void testSubType_14() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertFalse(possibleSubType.isSubTypeOf(possibleSuperType));
    }

    public void testSubType_15() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSubType.isSubTypeOf(possibleSuperType));
    }

    public void testSubType_16() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertTrue(possibleSubType.isSubTypeOf(possibleSuperType));
    }

    public void testIssue217175() throws Exception {
        Model model = getModel();
        TypeScope possibleSuperType = getSuperType(model);
        TypeScope possibleSubType = getSubType(model);

        assertFalse(possibleSuperType.isSuperTypeOf(possibleSubType));
    }

}

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
package org.netbeans.modules.css.prep.editor;

import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.editor.ProjectTestBase;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.css.prep.editor.model.CPElementHandle;
import org.netbeans.modules.css.prep.editor.model.CPElementType;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class CPCssIndexModelTest extends ProjectTestBase {

    public CPCssIndexModelTest(String name) {
        super(name, "testProject");
    }

    public void testIndexingAndQueryingOfVarsAndMixins() throws IOException {
        FileObject file = getTestFile(getSourcesFolderName() + "/lib1.scss");
        Project project = FileOwnerQuery.getOwner(file);
        assertNotNull(project);

        CssIndex index = CssIndex.create(project);
        assertNotNull(index);

        CPCssIndexModel indexModel = (CPCssIndexModel)index.getIndexModel(CPCssIndexModel.Factory.class, file);
        assertNotNull(indexModel);

        Collection<CPElementHandle> variables = indexModel.getVariables();
        assertNotNull(variables);

        assertEquals(2, CPUtils.filter(variables, CPElementType.VARIABLE_GLOBAL_DECLARATION).size());
        assertEquals(1, CPUtils.filter(variables, CPElementType.VARIABLE_DECLARATION_IN_BLOCK_CONTROL).size());
        assertEquals(2, CPUtils.filter(variables, CPElementType.VARIABLE_USAGE).size());

        Collection<CPElementHandle> mixins = indexModel.getMixins();
        assertNotNull(mixins);
        assertEquals(2, CPUtils.filter(mixins, CPElementType.MIXIN_DECLARATION).size());
        assertEquals(1, CPUtils.filter(mixins, CPElementType.MIXIN_USAGE).size());

    }

    public void testEncodeDecodeElementId() {
        String elementId = "styleSheet/body/bodyItem|1/selectorsGroup/selector/elementName";
        String enc = CPCssIndexModel.encodeElementId(elementId);
//        System.out.println(enc);
        String dec = CPCssIndexModel.decodeElementId(enc);
        assertFalse(enc.equals(dec));
        assertEquals(elementId, dec);
    }

}
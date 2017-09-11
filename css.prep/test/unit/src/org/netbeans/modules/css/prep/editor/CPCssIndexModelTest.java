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
package org.netbeans.modules.css.prep.editor;

import org.netbeans.modules.css.prep.editor.CPCssIndexModel;
import org.netbeans.modules.css.prep.editor.CPUtils;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
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
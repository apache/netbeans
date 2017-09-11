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
package org.netbeans.modules.css.model.impl;

import static junit.framework.Assert.assertEquals;
import org.netbeans.modules.css.model.api.Element;
import org.netbeans.modules.css.model.api.ElementHandle;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelTestBase;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.SelectorsGroup;
import org.netbeans.modules.css.model.api.StyleSheet;

/**
 *
 * @author marekfukala
 */
public class ElementHandleImplTest extends ModelTestBase {
    
    public ElementHandleImplTest(String name) {
        super(name);
    }

//    public void testGetElementId() {
//        Model model = createModel("div { color: red }\n .clz { color:blue } ");
//        StyleSheet styleSheet = getStyleSheet(model);
//        
//        Rule rule = styleSheet.getBody().getRules().get(0);
//        String path = ElementHandleImpl.createPath((ModelElement)rule);
//        assertEquals("StyleSheet/Body/BodyItem/Rule", path);
//
//        rule = styleSheet.getBody().getRules().get(1);
//        path = ElementHandleImpl.createPath((ModelElement)rule);
//        assertEquals("StyleSheet/Body/BodyItem|2/Rule", path);
//        
//    }
    
    public void testResolveElementHandle_to_same_model() {
        String code = "div { color: red }\n .clz { color:blue }";
        
        Model model = createModel(code);
        StyleSheet styleSheet = getStyleSheet(model);
        
        Rule rule = styleSheet.getBody().getRules().get(1);
        ElementHandle handle = rule.getElementHandle();
        assertNotNull(handle);
        
        //resolve to the same model
        Element resolved = handle.resolve(model);
        assertNotNull(resolved);
        assertSame(rule, resolved);
        
    }
    
    public void testResolveElementHandle_to_different_model() {
        String code = "div { color: red }\n .clz { color:blue }";
        
        Model model = createModel(code);
        StyleSheet styleSheet = getStyleSheet(model);
        
        Rule rule = styleSheet.getBody().getRules().get(1);
        ElementHandle handle = rule.getElementHandle();
        assertNotNull(handle);
        
        code = "xxx { color: fuchsia }\n .clz { color:green }";
        model = createModel(code);
        
        //resolve to the new model
        Element resolved = handle.resolve(model);
        assertNotNull(resolved);
        assertTrue(resolved instanceof Rule);
        Rule resolvedRule = (Rule)resolved;
        SelectorsGroup selectorsGroup = resolvedRule.getSelectorsGroup();
        assertNotNull(selectorsGroup);
        assertEquals(".clz", model.getElementSource(selectorsGroup).toString());
        
    }
    
}
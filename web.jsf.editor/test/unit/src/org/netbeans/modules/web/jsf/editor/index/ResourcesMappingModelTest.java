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
package org.netbeans.modules.web.jsf.editor.index;

import java.util.ArrayList;
import java.util.List;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsf.editor.index.ResourcesMappingModel.Resource;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ResourcesMappingModelTest extends NbTestCase {

    public ResourcesMappingModelTest(String name) {
        super(name);
    }

    @Test
    public void testParseResourceFromString() throws Exception {
        String resString = "stylesheet@test.css:;script@file.js:jsLib";
        List<ResourcesMappingModel.Resource> resources = new ArrayList(ResourcesMappingModel.parseResourcesFromString(resString));
        assertEquals(2, resources.size());

        ResourcesMappingModel.Resource res1 = resources.get(0);
        assertEquals(ResourcesMappingModel.ResourceType.STYLESHEET, res1.getType());
        assertEquals("test.css", res1.getName());
        assertEquals("", res1.getLibrary());

        ResourcesMappingModel.Resource res2 = resources.get(1);
        assertEquals(ResourcesMappingModel.ResourceType.SCRIPT, res2.getType());
        assertEquals("file.js", res2.getName());
        assertEquals("jsLib", res2.getLibrary());
    }

    @Test
    public void testIsExpression() throws Exception {
        String expression = "#{exp}";
        assertTrue(ResourcesMappingModel.isELExpression(expression));

        expression = "${asdfasd}";
        assertTrue(ResourcesMappingModel.isELExpression(expression));

        expression = "@{asdfasd}";
        assertFalse(ResourcesMappingModel.isELExpression(expression));

        expression = "<?php echo 'aaa'; ?>";
        assertFalse(ResourcesMappingModel.isELExpression(expression));

        expression = "${resource";
        assertFalse(ResourcesMappingModel.isELExpression(expression));

        expression = "${resource['']}";
        assertTrue(ResourcesMappingModel.isELExpression(expression));
    }

    @Test
    public void testIsJsfResource() throws Exception {
        String expression = "#{exp}";
        assertFalse(ResourcesMappingModel.isJsfResource(expression));

        expression = "${resource['']}";
        assertTrue(ResourcesMappingModel.isJsfResource(expression));

        expression = "@{resource['ddd']}";
        assertFalse(ResourcesMappingModel.isJsfResource(expression));

        expression = "#{resource['test.css']}";
        assertTrue(ResourcesMappingModel.isJsfResource(expression));

        expression = "#{resource['lib:test.css']}";
        assertTrue(ResourcesMappingModel.isJsfResource(expression));

        expression = "${resource['AAA']";
        assertFalse(ResourcesMappingModel.isJsfResource(expression));
    }

    @Test
    public void testParseResource() throws Exception {
        Resource res = ResourcesMappingModel.parseResource(ResourcesMappingModel.ResourceType.SCRIPT, "#{resource['lib:test.js']}");
        assertEquals("lib", res.getLibrary());
        assertEquals("test.js", res.getName());
        assertEquals(ResourcesMappingModel.ResourceType.SCRIPT, res.getType());

        res = ResourcesMappingModel.parseResource(ResourcesMappingModel.ResourceType.STYLESHEET, "#{resource['folder/test.css']}");
        assertEquals("", res.getLibrary());
        assertEquals("folder/test.css", res.getName());
        assertEquals(ResourcesMappingModel.ResourceType.STYLESHEET, res.getType());
    }



}

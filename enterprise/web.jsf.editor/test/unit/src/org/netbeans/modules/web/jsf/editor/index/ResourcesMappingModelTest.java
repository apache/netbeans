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
package org.netbeans.modules.web.jsf.editor.index;

import java.util.ArrayList;
import java.util.List;
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
        List<ResourcesMappingModel.Resource> resources = new ArrayList<>(ResourcesMappingModel.parseResourcesFromString(resString));
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

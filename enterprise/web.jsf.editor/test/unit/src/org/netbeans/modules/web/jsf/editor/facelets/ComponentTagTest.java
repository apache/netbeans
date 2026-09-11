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
package org.netbeans.modules.web.jsf.editor.facelets;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.Tag;

/**
 *
 * @author mfukala@netbeans.org
 */
public class ComponentTagTest extends NbTestCase {

    public ComponentTagTest(String name) {
        super(name);
    }

    public void testAll() {
        assertNull(ComponentTag.wrap(null));
        Tag t = ComponentTag.wrap(new TagImpl("test", null, Collections.emptyMap()));
        assertSame(t, ComponentTag.wrap(t));
        
        assertNotNull(t.getAttribute("class"));
        assertNotNull(t.getAttribute("rendered"));
        
        assertFalse(t.hasNonGenenericAttributes());
        
        Collection<Attribute> attrs = t.getAttributes();
        assertNotNull(attrs);
        assertFalse(attrs.isEmpty());
        
        Attribute id = t.getAttribute("id");
        assertNotNull(id);
        assertEquals("id", id.getName());
        assertFalse(id.isRequired());
        assertNull(id.getType());
        assertNotNull(id.getDescription());
        assertEquals("The component identifier", id.getDescription());
        
    }

    public void testGenericAttributesDescription() {
        Tag t = ComponentTag.wrap(new TagImpl("test", null, Collections.emptyMap()));
        for(Attribute ga : t.getAttributes()) {
            assertNotNull(ga.getDescription());
            assertFalse(ga.getDescription().isEmpty());
        }
    }
    
    public void testTagImplHasNoComponentDefaults() {
        Attribute declared = new Attribute.DefaultAttribute("testAttr", null, false);
        Tag rawTag = new TagImpl("test", null, Map.of(declared.getName(), declared));
        // Raw TagImpl must only have its declared attributes
        assertEquals(1, rawTag.getAttributes().size());
        assertNotNull(rawTag.getAttribute("testAttr"));
        assertNull(rawTag.getAttribute("rendered"));
        assertNull(rawTag.getAttribute("transient"));
    }   

    public void testComponentTagPrecedenceOverDefaults() {
        // If a tag legitimately declares an "id" attribute with its own description (and required true)
        String customIdDesc = "Custom ID description";
        Attribute customId = new Attribute.DefaultAttribute("id", customIdDesc, true);
        Tag compTag = ComponentTag.wrap(new TagImpl("test", null, Map.of(customId.getName(), customId)));
        // Declared attribute must take precedence over the generic default
        Attribute resolvedId = compTag.getAttribute("id");
        assertNotNull(resolvedId);
        assertEquals(customIdDesc, resolvedId.getDescription());
        assertTrue(resolvedId.isRequired());
        // getAttributes should return only the declared id attribute
        List<Attribute> idAttributes = compTag.getAttributes().stream().filter(att -> att.getName().equals("id")).toList();
        assertEquals(1, idAttributes.size());
        assertEquals(customIdDesc, idAttributes.get(0).getDescription());
        // Other default attributes are still present
        assertNotNull(compTag.getAttribute("rendered"));
        assertNotNull(compTag.getAttribute("transient"));
    }

    public void testHasNonGenericAttributes() {
        Tag emptyTag = new TagImpl("test", null, Collections.emptyMap());
        assertFalse(ComponentTag.wrap(emptyTag).hasNonGenenericAttributes());

        Tag nonEmptyTag = new TagImpl("test", null, Map.of("custom", new Attribute.DefaultAttribute("custom", null, false)));
        assertTrue(ComponentTag.wrap(nonEmptyTag).hasNonGenenericAttributes());
    }

}

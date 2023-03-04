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
package org.netbeans.modules.web.jsf.editor.facelets;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.Tag;


/**
 *
 * @author mfukala@netbeans.org
 */
public class GenericTagTest extends NbTestCase {

    public GenericTagTest(String name) {
        super(name);
    }

    public void testAll() {
        Tag t = new GenericTagImpl();
        
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
        Tag t = new GenericTagImpl();
        for(Attribute ga : t.getAttributes()) {
            assertNotNull(ga.getDescription());
            assertFalse(ga.getDescription().isEmpty());
        }
    }
    
    private static class GenericTagImpl extends GenericTag {

            @Override
            public String getName() {
                return "test";
            }

            @Override
            public String getDescription() {
                return null;
            }

    }
    
}

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

package org.netbeans.modules.bugtracking.commons;

import org.netbeans.modules.bugtracking.commons.FindTypesSupport;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author tomas
 */
public class FindTypesSupportTest extends TestCase {

    public void testIsCamelCase() {
        String str = "Camel";
        List<Integer> l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(0, l.get(0).intValue());
        assertEquals(str.length(), l.get(1).intValue());
        assertEquals("Camel", str.substring(l.get(0).intValue(), l.get(1).intValue()));
                
        str = "CamelCase";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(0, l.get(0).intValue());
        assertEquals(str.length(), l.get(1).intValue());
        assertEquals("CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        str = "CamelCase.";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(0, l.get(0).intValue());
        assertEquals("CamelCase".length(), l.get(1).intValue());
        assertEquals("CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        str = "TripleCamelCase";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(0, l.get(0).intValue());
        assertEquals(str.length(), l.get(1).intValue());
        assertEquals("TripleCamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        str = "org.CamelCase";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(0, l.get(0).intValue());
        assertEquals(str.length(), l.get(1).intValue());
        assertEquals("org.CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        str = "org.camel.CamelCase";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(0, l.get(0).intValue());
        assertEquals(str.length(), l.get(1).intValue());
        assertEquals("org.camel.CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        str = " CamelCase ";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(1, l.get(0).intValue());
        assertEquals("CamelCase".length() + 1, l.get(1).intValue());
        assertEquals("CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        str = "\tCamelCase\t";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(1, l.get(0).intValue());
        assertEquals("CamelCase".length() + 1, l.get(1).intValue());
        assertEquals("CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        str = "\nCamelCase\n";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(1, l.get(0).intValue());
        assertEquals("CamelCase".length()+1, l.get(1).intValue());
        assertEquals("CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        str = " org.camel.CamelCase ";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(1, l.get(0).intValue());
        assertEquals("org.camel.CamelCase".length() + 1, l.get(1).intValue());
        assertEquals("org.camel.CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        String prefix = " '";
        String sufix = "' ";
        str = prefix + "org.camel.CamelCase" + sufix;
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(prefix.length(), l.get(0).intValue());
        assertEquals((prefix + "org.camel.CamelCase").length(), l.get(1).intValue());
        assertEquals("org.camel.CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));

        prefix = " \"";
        sufix = "\" ";
        str = prefix + "org.camel.CamelCase" + sufix;
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(prefix.length(), l.get(0).intValue());
        assertEquals((prefix + "org.camel.CamelCase").length(), l.get(1).intValue());
        assertEquals("org.camel.CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        prefix = ".";
        sufix = ".";
        str = prefix + "CamelCase" + sufix;
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(prefix.length(), l.get(0).intValue());
        assertEquals((prefix + "CamelCase").length(), l.get(1).intValue());
        assertEquals("CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        prefix = ".";
        sufix = ".";
        str = prefix + "org.camel.CamelCase" + sufix;
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(prefix.length(), l.get(0).intValue());
        assertEquals((prefix + "org.camel.CamelCase").length(), l.get(1).intValue());
        assertEquals("org.camel.CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        str = "CamelCase CamelCase";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(4, l.size());
        assertEquals(0, l.get(0).intValue());
        assertEquals("CamelCase".length(), l.get(1).intValue());
        assertEquals("CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        assertEquals("CamelCase ".length(), l.get(2).intValue());
        assertEquals(str.length(), l.get(3).intValue());
        assertEquals("CamelCase", str.substring(l.get(2).intValue(), l.get(3).intValue()));
        
        prefix = " ";
        sufix = " ";
        String mid = " ";
        str = prefix + "org.camel.CamelCase" + mid + "org.camel.CamelCase" + sufix;
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(4, l.size());
        assertEquals(prefix.length(), l.get(0).intValue());
        assertEquals((prefix + "org.camel.CamelCase").length(), l.get(1).intValue());
        assertEquals("org.camel.CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        assertEquals((prefix + "org.camel.CamelCase" + mid).length(), l.get(2).intValue());
        assertEquals(str.length() - sufix.length(), l.get(3).intValue());
        assertEquals("org.camel.CamelCase", str.substring(l.get(2).intValue(), l.get(3).intValue()));
        
        prefix = " a ";
        sufix = " a ";
        mid = " a a a a ";
        str = prefix + "org.camel.CamelCase" + mid + "org.camel.CamelCase" + sufix;
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(4, l.size());
        assertEquals(prefix.length(), l.get(0).intValue());
        assertEquals((prefix + "org.camel.CamelCase").length(), l.get(1).intValue());
        assertEquals("org.camel.CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        assertEquals((prefix + "org.camel.CamelCase" + mid).length(), l.get(2).intValue());
        assertEquals(str.length() - sufix.length(), l.get(3).intValue());
        assertEquals("org.camel.CamelCase", str.substring(l.get(2).intValue(), l.get(3).intValue()));
        
        
        str = "camel";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertTrue(l.isEmpty());
        
        str = " camel camel ";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertTrue(l.isEmpty());
        
        str = " camel.camel ";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertTrue(l.isEmpty());
        
        str = "camel.camel";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertTrue(l.isEmpty());
        
        str = ".camel.camel.";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertTrue(l.isEmpty());
        
    }

}

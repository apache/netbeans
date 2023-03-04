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

package org.netbeans.api.java.source;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
public class CodeStyleUtilsTest {

    @Test
    public void testAddPrefixSuffix() {
        CharSequence name = null;
        String prefix = null;
        String suffix = null;
        String result = CodeStyleUtils.addPrefixSuffix(name, prefix, suffix);
        assertEquals("", result);

        name = "name";
        result = CodeStyleUtils.addPrefixSuffix(name, prefix, suffix);
        assertEquals(name, result);
        
        suffix = "S";
        result = CodeStyleUtils.addPrefixSuffix(name, prefix, suffix);
        assertEquals("nameS", result);
        
        prefix = "$";
        result = CodeStyleUtils.addPrefixSuffix(name, prefix, suffix);
        assertEquals("$nameS", result);
        
        prefix = "s";
        result = CodeStyleUtils.addPrefixSuffix(name, prefix, suffix);
        assertEquals("sNameS", result);
        
        name = "__name";
        result = CodeStyleUtils.addPrefixSuffix(name, prefix, suffix);
        assertEquals("sNameS", result);
        
        name = null;
        result = CodeStyleUtils.addPrefixSuffix(name, prefix, suffix);
        assertEquals("sS", result);
    }

    @Test
    public void testRemovePrefixSuffix() {
        String prefix = null;
        String suffix = null;

        CharSequence name = "name";
        String result = CodeStyleUtils.removePrefixSuffix(name, prefix, suffix);
        assertEquals(name, result);
        
        suffix = "S";
        name = "nameS";
        result = CodeStyleUtils.removePrefixSuffix(name, prefix, suffix);
        assertEquals("name", result);
        
        prefix = "$";
        name = "$nameS";
        result = CodeStyleUtils.removePrefixSuffix(name, prefix, suffix);
        assertEquals("name", result);
        
        prefix = "s";
        name = "sNameS";
        result = CodeStyleUtils.removePrefixSuffix(name, prefix, suffix);
        assertEquals("name", result);
        
        prefix = "_";
        name = "__nameS";
        result = CodeStyleUtils.removePrefixSuffix(name, prefix, suffix);
        assertEquals("_name", result);
        
        prefix = "S";
        suffix = "s";
        name = "sNameS";
        result = CodeStyleUtils.removePrefixSuffix(name, prefix, suffix);
        assertEquals("sNameS", result);
    }

    @Test
    public void testGetCapitalizedName() {
        CharSequence name = "name";
        String result = CodeStyleUtils.getCapitalizedName(name);
        assertEquals("Name", result);
        
        name = "Name";
        result = CodeStyleUtils.getCapitalizedName(name);
        assertEquals("Name", result);
        
        name = "NAME";
        result = CodeStyleUtils.getCapitalizedName(name);
        assertEquals("NAME", result);
        
        name = "nAme";
        result = CodeStyleUtils.getCapitalizedName(name);
        assertEquals("nAme", result);
        
        name = "naMe";
        result = CodeStyleUtils.getCapitalizedName(name);
        assertEquals("NaMe", result);
    }

    @Test
    public void testGetDecapitalizedName() {
        CharSequence name = "name";
        String result = CodeStyleUtils.getDecapitalizedName(name);
        assertEquals("name", result);
        
        name = "Name";
        result = CodeStyleUtils.getDecapitalizedName(name);
        assertEquals("name", result);
        
        name = "NAME";
        result = CodeStyleUtils.getDecapitalizedName(name);
        assertEquals("NAME", result);
        
        name = "nAme";
        result = CodeStyleUtils.getDecapitalizedName(name);
        assertEquals("nAme", result);
        
        name = "NaMe";
        result = CodeStyleUtils.getDecapitalizedName(name);
        assertEquals("naMe", result);
    }
}

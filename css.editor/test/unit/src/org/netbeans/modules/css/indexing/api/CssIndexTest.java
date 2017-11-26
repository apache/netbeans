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

package org.netbeans.modules.css.indexing.api;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssIndexTest extends NbTestCase {

    public CssIndexTest(String name) {
        super(name);
    }
    
    public void testEncodeValueForRegexp() {
        assertEquals("", CssIndex.encodeValueForRegexp(""));
        assertEquals("a", CssIndex.encodeValueForRegexp("a"));
        assertEquals("\\\\", CssIndex.encodeValueForRegexp("\\"));
        assertEquals("a\\*b", CssIndex.encodeValueForRegexp("a*b"));
        assertEquals("a\\\\b", CssIndex.encodeValueForRegexp("a\\b"));
        assertEquals("\\.\\^\\$\\[\\]\\{\\}\\(\\)", CssIndex.encodeValueForRegexp(".^$[]{}()"));
    }

    public void testCreateImpliedFileName() {
        //no change
        assertEquals("index.scss", CssIndex.createImpliedFileName("index.scss", null, false));
        
        //imply underscope, keep ext
        assertEquals("_index.scss", CssIndex.createImpliedFileName("index.scss", null, true));
        
        //add just ext
        assertEquals("index.scss", CssIndex.createImpliedFileName("index", "scss", false));
        
        //mix
        assertEquals("_index.scss", CssIndex.createImpliedFileName("index", "scss", true));
        assertEquals("folder/index.scss", CssIndex.createImpliedFileName("folder/index", "scss", false));
        assertEquals("folder/_index.scss", CssIndex.createImpliedFileName("folder/index", "scss", true));
        assertEquals("folder1/folder2/index.scss", CssIndex.createImpliedFileName("folder1/folder2/index", "scss", false));
        assertEquals("folder1/folder2/_index.scss", CssIndex.createImpliedFileName("folder1/folder2/index", "scss", true));
        assertEquals("/folder/_index.scss", CssIndex.createImpliedFileName("/folder/index", "scss", true));
        assertEquals("/_index.scss", CssIndex.createImpliedFileName("/index", "scss", true));
        
        //extension exists but not scss or sass
        assertEquals("_pa.rtial.scss", CssIndex.createImpliedFileName("pa.rtial", "scss", true));
        assertEquals("folder/_pa.rtial.scss", CssIndex.createImpliedFileName("folder/pa.rtial", "scss", true));
        assertEquals("folder/_pa.rtial.scss", CssIndex.createImpliedFileName("folder/pa.rtial.scss", null, true));
        assertEquals("folder/pa.rtial", CssIndex.createImpliedFileName("folder/pa.rtial", null, false));
        
    }
}
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
package org.netbeans.modules.spellchecker;

import java.io.File;
import java.util.Collections;
import java.util.Locale;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.spellchecker.spi.dictionary.ValidityType;

/**
 *
 * @author Jan Lahoda
 */
public class DictionaryImplTest extends NbTestCase {
    
    public DictionaryImplTest(String testName) {
        super(testName);
    }

    public void testAlmostEmpty() throws Exception {
        clearWorkDir();
        
        File source = new File(getWorkDir(), "dictionary.cache");
        DictionaryImpl d = new DictionaryImpl(source, Locale.ENGLISH);
        
        assertEquals(ValidityType.INVALID, d.validateWord("dddd"));
        assertEquals(Collections.emptyList(), d.findProposals("dddd"));
        assertEquals(Collections.emptyList(), d.findValidWordsForPrefix("dddd"));
        
        d.addEntry("dddd");
        
        assertEquals(ValidityType.VALID, d.validateWord("dddd"));
        assertEquals(Collections.emptyList(), d.findProposals("dddd"));
        assertEquals(Collections.emptyList(), d.findValidWordsForPrefix("dddd"));
        assertEquals(Collections.singletonList("dddd"), d.findProposals("ddddd"));
        assertEquals(Collections.emptyList(), d.findValidWordsForPrefix("ddddd"));
        
        d.addEntry("ddddd");
        
        assertEquals(ValidityType.VALID, d.validateWord("dddd"));
        assertEquals(ValidityType.VALID, d.validateWord("ddddd"));
        assertEquals(Collections.emptyList(), d.findProposals("dddd"));
        assertEquals(Collections.emptyList(), d.findProposals("ddddd"));
    }
    
    public void testCapitalized() throws Exception {
        clearWorkDir();

        File source = new File(getWorkDir(), "dictionary.cache");
        DictionaryImpl d = new DictionaryImpl(source, Locale.ENGLISH);

        d.addEntry("Foo");
        d.addEntry("bar");

        assertEquals(ValidityType.VALID, d.validateWord("Foo"));
    }

    public void testSorting() throws Exception {
        clearWorkDir();

        File source = new File(getWorkDir(), "dictionary.cache");
        DictionaryImpl d = new DictionaryImpl(source, Locale.ENGLISH);

        d.addEntry("Zzz");
        d.addEntry("yyy");
        d.addEntry("xxx");
        d.addEntry("ttt");

        assertEquals(ValidityType.VALID, d.validateWord("Zzz"));
        assertEquals(ValidityType.VALID, d.validateWord("yyy"));
        assertEquals(ValidityType.VALID, d.validateWord("xxx"));
        assertEquals(ValidityType.VALID, d.validateWord("ttt"));
    }

}

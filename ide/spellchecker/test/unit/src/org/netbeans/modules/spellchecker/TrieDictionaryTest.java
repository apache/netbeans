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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.spellchecker.spi.dictionary.Dictionary;
import org.netbeans.modules.spellchecker.spi.dictionary.ValidityType;

/**
 *
 * @author lahvac
 */
public class TrieDictionaryTest extends NbTestCase {

    public TrieDictionaryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public void testValidateWord() throws Exception {
        SortedSet<String> data = new TreeSet<String>();
        
        data.add("add");
        data.add("remove");
        data.add("data");
        data.add("test");
                
        Dictionary d = constructTrie(data);
        
        assertEquals(ValidityType.VALID, d.validateWord("remove"));
        assertEquals(ValidityType.VALID, d.validateWord("add"));
        assertEquals(ValidityType.VALID, d.validateWord("data"));
        assertEquals(ValidityType.VALID, d.validateWord("test"));
        
        assertEquals(ValidityType.INVALID, d.validateWord("sdfgh"));
        assertEquals(ValidityType.INVALID, d.validateWord("sd"));
        assertEquals(ValidityType.INVALID, d.validateWord("s"));
        assertEquals(ValidityType.INVALID, d.validateWord("datax"));
        
        assertEquals(ValidityType.PREFIX_OF_VALID, d.validateWord("d"));
        assertEquals(ValidityType.PREFIX_OF_VALID, d.validateWord("da"));
        assertEquals(ValidityType.PREFIX_OF_VALID, d.validateWord("dat"));
        assertEquals(ValidityType.PREFIX_OF_VALID, d.validateWord("t"));
    }

    public void testFindProposals() throws Exception {
        SortedSet<String> data = new TreeSet<String>();
        
        data.add("add");
        data.add("remove");
        data.add("data");
        data.add("test");
        data.add("hello");
        data.add("saida");
        
        Dictionary d = constructTrie(data);
        
        assertEquals(Collections.singletonList("hello"), d.findProposals("hfllo"));
        assertEquals(Collections.singletonList("saida"), d.findProposals("safda"));
    }

    public void test150642() throws Exception {
        SortedSet<String> data = new TreeSet<String>();

        data.add("abc");
        data.add("aéc");

        Dictionary d = constructTrie(data);

        assertEquals(ValidityType.VALID, d.validateWord("abc"));
        assertEquals(ValidityType.VALID, d.validateWord("aéc"));

        assertEquals(ValidityType.PREFIX_OF_VALID, d.validateWord("a"));
        assertEquals(ValidityType.PREFIX_OF_VALID, d.validateWord("ab"));
        assertEquals(ValidityType.PREFIX_OF_VALID, d.validateWord("aé"));
    }

    public void testWordPrefixOfOther() throws Exception {
        SortedSet<String> data = new TreeSet<String>();

        data.add("Abc");
        data.add("Bcd");
        data.add("abcd");

        Dictionary d = constructTrie(data);

        assertEquals(ValidityType.VALID, d.validateWord("Abc"));
        assertEquals(ValidityType.PREFIX_OF_VALID, d.validateWord("Bc"));
    }

    private Dictionary constructTrie(SortedSet<String> data) throws Exception {
        clearWorkDir();

        File sourceFile = new File(getWorkDir(), "source");
        Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sourceFile), StandardCharsets.UTF_8));

        for (String d : data) {
            w.write(d);
            w.write("\n");
        }

        w.close();

        File trieFile = new File(getWorkDir(), "dict");

        return TrieDictionary.getDictionary(trieFile, Collections.<URL>singletonList(sourceFile.toURI().toURL()));
    }
}

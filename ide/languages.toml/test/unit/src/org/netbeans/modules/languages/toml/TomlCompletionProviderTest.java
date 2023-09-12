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
package org.netbeans.modules.languages.toml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.ViewFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.spi.editor.completion.CompletionTask;

/**
 *
 * @author lkishalmi
 */
public class TomlCompletionProviderTest extends TomlTestBase {

    public TomlCompletionProviderTest(String testName) {
        super(testName);
    }

    @Test
    public void testMatchKey1() {
        String key = "version";
        String prefix = "ver";
        String expResult = "version";
        String result = TomlCompletionProvider.matchKey(key, prefix);
        assertEquals(expResult, result);
    }

    @Test
    public void testMatchKey2() {
        String key = "cars.ford.focus";
        String prefix = "ford.fo";
        String expResult = "focus";
        String result = TomlCompletionProvider.matchKey(key, prefix);
        assertEquals(expResult, result);
    }

    @Test
    public void testNotMatchKey1() {
        String key = "version";
        String prefix = "var";
        String expResult = null;
        String result = TomlCompletionProvider.matchKey(key, prefix);
        assertEquals(expResult, result);
    }

    @Test
    public void testNotMatchKey2() {
        String key = "cars.ford.focus";
        String prefix = "ord.fo";
        String expResult = null;
        String result = TomlCompletionProvider.matchKey(key, prefix);
        assertEquals(expResult, result);
    }

    @Test
    public void testKeyPrefixOffset1() throws Exception {
        Document doc = getDocument("[fruits]\norange.color");
        int offset = 0;
        int expResult = 0;
        int result = TomlCompletionProvider.keyPrefixOffset(doc, offset);
        assertEquals(expResult, result);
    }

    @Test
    public void testKeyPrefixOffset2() throws Exception {
        Document doc = getDocument("[fruits]\norange.color");
        int offset = "[".length();
        int expResult = "[".length();
        int result = TomlCompletionProvider.keyPrefixOffset(doc, offset);
        assertEquals(expResult, result);
    }

    @Test
    public void testKeyPrefixOffset3() throws Exception {
        Document doc = getDocument("[fruits]\norange.color");
        int offset = "[fru".length();
        int expResult = "[".length();
        int result = TomlCompletionProvider.keyPrefixOffset(doc, offset);
        assertEquals(expResult, result);
    }


    @Test
    public void testKeyPrefixOffset4() throws Exception {
        Document doc = getDocument("[fruits]\norange.color");
        int offset = "[fruits]\nora".length();
        int expResult = "[fruits]\n".length();
        int result = TomlCompletionProvider.keyPrefixOffset(doc, offset);
        assertEquals(expResult, result);
    }


    @Test
    public void testKeyPrefixOffset5() throws Exception {
        Document doc = getDocument("[fruits]\norange.color");
        int offset = "[fruits]\norange.color".length();
        int expResult = "[fruits]\n".length();
        int result = TomlCompletionProvider.keyPrefixOffset(doc, offset);
        assertEquals(expResult, result);
    }

    @Test
    public void testKeyPrefixOffset6() throws Exception {
        Document doc = getDocument("msg=\"Hello");
        int offset = "msg=\"Hello".length();
        int expResult = "msg=\"Hello".length();
        int result = TomlCompletionProvider.keyPrefixOffset(doc, offset);
        assertEquals(expResult, result);
    }

    @Test
    public void testKeyPrefixOffset7() throws Exception {
        Document doc = getDocument("msg=\"Hello");
        int offset = "msg=\"".length();
        int expResult = "msg=\"".length();
        int result = TomlCompletionProvider.keyPrefixOffset(doc, offset);
        assertEquals(expResult, result);
    }
}

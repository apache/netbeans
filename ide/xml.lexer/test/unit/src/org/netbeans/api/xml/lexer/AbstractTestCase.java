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
package org.netbeans.api.xml.lexer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;

/**
 * The XMLTokenIdTest tests the parsing algorithm of XMLLexer.
 * Various tests include, sanity, regression, performance etc.
 * @author Samaresh (samaresh.panda@sun.com)
 */
public class AbstractTestCase extends NbTestCase {
    
    static final boolean DEBUG = true;
    
    public AbstractTestCase(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
    }

    @Override
    public void tearDown() {
    }

    
    /**
     * Parses a XML document using XMLLexer and loops through all tokens.
     * @param document
     * @throws java.lang.Exception
     */
    protected void parse(javax.swing.text.Document document) throws Exception {
        ((AbstractDocument)document).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(document);
            TokenSequence ts = th.tokenSequence();
            assert(true);
            while(ts.moveNext()) {
                Token token = ts.token();
                assert(token.id() != null);
                if(DEBUG) {
//                    System.out.println("Id :["+ token.id().name() +
//                            "] [Text :["+ token.text()+"]");
                }
            }
        } finally {
            ((AbstractDocument)document).readUnlock();
        }
    }
    
    protected Language getLanguage() {
        return XMLTokenId.language();
    }
        
    protected javax.swing.text.Document getDocument(String path) throws Exception {
        javax.swing.text.Document doc = getResourceAsDocument(path);
        //must set the language inside unit tests
        doc.putProperty(Language.class, getLanguage());
        return doc;
    }
                 
    protected static Document getResourceAsDocument(String path) throws Exception {
        InputStream in = XMLTokenIdTest.class.getResourceAsStream(path);
        Document sd = new BaseDocument(true, "text/xml"); //NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuffer sbuf = new StringBuffer();
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                sbuf.append(line);
                sbuf.append(System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        sd.insertString(0,sbuf.toString(),null);
        return sd;
    }
    
    protected <T extends Enum<T>> T[] readTokenIDs(Class<T> enumClass, String text) {
        List<T> arr = new ArrayList<>();
        for (String t : text.split(" *, *")) {
            T val = Enum.valueOf(enumClass, t);
            arr.add(val);
        }
        return (T[])arr.toArray();
        
    }

    /**
     * This test validates all tokens obtained by parsing test.xml against
     * an array of expected tokens.
     */
    public void assertTokenSequence(javax.swing.text.Document document, TokenId[] expectedIds) throws Exception {
        ((AbstractDocument)document).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(document);
            TokenSequence ts = th.tokenSequence();
            //assert(ts.tokenCount() == expectedIds.length);
            int index = 0;
            while(ts.moveNext()) {
                Token token = ts.token();
                if(DEBUG) {
                    System.out.println("Id :["+ token.id().name() +
                            "] [Text :["+ token.text()+"]");
                }
                assert(token.id() == expectedIds[index]);
                index++;
            }
        } finally {
            ((AbstractDocument)document).readUnlock();
        }
    }

}

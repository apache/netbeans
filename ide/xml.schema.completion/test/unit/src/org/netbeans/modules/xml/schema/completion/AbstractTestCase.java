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
package org.netbeans.modules.xml.schema.completion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.framework.*;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.schema.completion.util.CompletionContextImpl;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Samaresh
 */
public abstract class AbstractTestCase extends NbTestCase {
    
    protected String instanceResourcePath;
    protected FileObject instanceFileObject;
    protected BaseDocument instanceDocument;
    protected XMLSyntaxSupport support;
    
    public AbstractTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        
    }

    @Override
    protected void tearDown() throws Exception {
    }
    
    /**
     * Set up the test for a particular XML document
     * @param path the XML document
     * @see #setupCompletion(java.lang.String, java.lang.StringBuffer) 
     */
    protected void setupCompletion(String path) throws Exception {
        setupCompletion(path, null);
    }
    
    
    /**
     * Set up the test for a particular XML document
     * @param path the XML document
     * @param content the content to insert into the document
     * @see #setupCompletion(java.lang.String)
     */
    protected void setupCompletion(String path, StringBuffer content) throws Exception {
        this.instanceResourcePath = path;
        this.instanceFileObject = Util.getResourceAsFileObject(path);
        this.instanceDocument = Util.getResourceAsDocument(path);
        this.support = XMLSyntaxSupport.getSyntaxSupport(instanceDocument);
        if(content != null) {
            instanceDocument.remove(0, instanceDocument.getLength());
            instanceDocument.insertString(0, content.toString(), null);
        }
        instanceDocument.putProperty(Language.class, XMLTokenId.language());        
    }
    
    /**
     * Queries and returns a list of completion items.
     * Each unit test is supposed to evaluate this result.
     * @param caretOffset the caret offset at which code completion is invoked
     * @return the code completion results 
     */
    protected List<CompletionResultItem> query(int caretOffset) {
        assert(instanceFileObject  != null && instanceDocument != null);
        CompletionQuery instance = new CompletionQuery(instanceFileObject);
        return instance.getCompletionItems(instanceDocument, caretOffset);
    }
    
    protected void assertResult(List<CompletionResultItem> result,
            String... expectedResult) {
        if(result == null && expectedResult == null) {
            assert(true);
            return;
        }
        assert(result.size() == expectedResult.length);
        for(int i=0; i<expectedResult.length; i++) {
            boolean found = false;
            for(CompletionResultItem item : result) {
                String resultItem = item.getItemText();
                if(resultItem.equals(expectedResult[i])) {
                    found = true;
                    break;
                }
            }            
            assert(found);
        }
    }
    
    protected void assertResult(String[] result,
            String[] expectedResult) {
        if(result == null && expectedResult == null) {
            assert(true);
            return;
        }
        assert(result.length == expectedResult.length);
        for(int i=0; i<expectedResult.length; i++) {
            boolean found = false;
            for(String item : result) {
                if(item.equals(expectedResult[i])) {
                    found = true;
                    break;
                }
            }            
            assert(found);
        }
    }
    
    protected void assertContainSuggestions(List<CompletionResultItem> items, String... suggestions) {
        assertContainSuggestions(items, true, suggestions);
    }

    protected void assertDoesNotContainSuggestions(List<CompletionResultItem> items, boolean exact, String... suggestions) {
        if (items == null) {
            return;
        }
        if (exact && items.size() != suggestions.length) {
            return;
        }
        List<String> actual = new ArrayList<String>(items.size());
        for (CompletionResultItem item : items) {
            actual.add(item.getItemText());
        }
        List<String> not = new ArrayList<String>(Arrays.asList(suggestions));
        actual.removeAll(not);
        assertFalse("Unexpected suggestions", actual.size() != items.size());
    }
    
    protected void assertContainSuggestions(List<CompletionResultItem> items, boolean exact, String... suggestions) {
        assertNotNull(items);
        if (exact) {
            assertEquals("Number of suggestions does not match", suggestions.length, items.size());
        }
        List<String> actual = new ArrayList<String>(items.size());
        for (CompletionResultItem item : items) {
            actual.add(item.getItemText());
        }
        assertTrue("Expected suggestions not found", Arrays.asList(suggestions).containsAll(actual));
    }
    
    BaseDocument getDocument() {
        return instanceDocument;
    }
    
    XMLSyntaxSupport getXMLSyntaxSupport() {
        return support;
    }
    
    FileObject getFileObject() {
        return instanceFileObject;
    }
    
    CompletionContextImpl getContextAtOffset(int offset) {
        CompletionContextImpl context = new CompletionContextImpl(instanceFileObject, support, offset);
        context.initContext();
        return context;
    }

}

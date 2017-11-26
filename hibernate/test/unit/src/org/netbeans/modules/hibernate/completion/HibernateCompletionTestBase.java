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

package org.netbeans.modules.hibernate.completion;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import javax.tools.FileObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Dongmei Cao
 */
public class HibernateCompletionTestBase  extends NbTestCase{
      
    protected String instanceResourcePath;
    protected FileObject instanceFileObject;
    protected Document instanceDocument;
    
    public HibernateCompletionTestBase(String name) {
        super(name);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    @Override
    public void setUp() {
       
    }

    @After
    @Override
    public void tearDown() {
    }
    
    protected void setupCompletion(String path, StringBuffer buffer) throws Exception {
        this.instanceResourcePath = path;
        this.instanceDocument = Util.getResourceAsDocument(path);
        if(buffer != null) {
            instanceDocument.remove(0, instanceDocument.getLength());
            instanceDocument.insertString(0, buffer.toString(), null);
        }
        instanceDocument.putProperty(Language.class, XMLTokenId.language());        
    }
 
    protected void assertResult(List<HibernateCompletionItem> result,
            String[] expectedResult) {
        
        assertNotNull(result);
        assertNotNull(expectedResult);
            
        assert(result.size() == expectedResult.length);
        
        List<String> resultItemNames = new ArrayList<String>();
        for(HibernateCompletionItem item : result) {
            //System.out.println( "-----" + item.getDisplayText());
            resultItemNames.add(item.getDisplayText());
        }
        
        for(int i = 0; i < expectedResult.length; i ++) {
            boolean found = true;
            if(!resultItemNames.contains(expectedResult[i])) {
                found = false;
            }
            assertTrue("Not found " + expectedResult[i], found);
        }
    }
}

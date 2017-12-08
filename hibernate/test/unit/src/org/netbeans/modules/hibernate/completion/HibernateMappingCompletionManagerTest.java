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
import org.junit.Test;
import org.netbeans.spi.editor.completion.CompletionProvider;

/**
 *
 * @author dc151887
 */
public class HibernateMappingCompletionManagerTest extends HibernateCompletionTestBase {
    
    private static final String[] generatorClasses = new String[]{
            "increment", 
            "identity", 
            "sequence", 
            "hilo", 
            "seqhilo", 
            "uuid", 
            "guid", 
            "native", 
            "assigned",
            "select", 
            "foreign",
            "sequence-identity"
        };

    public HibernateMappingCompletionManagerTest(String name) {
        super(name);
    }

    /**
     * Test of completeAttributeValues method, of class HibernateMappingCompletionManager.
     */
    @Test
    public void testCompleteAttributeValues() throws Exception {
        System.out.println("completeAttributeValues");
        setupCompletion("resources/Person.hbm.xml", null);
        List<HibernateCompletionItem> items = query(314);
        String[] expectedResult = generatorClasses;
        assertResult(items, expectedResult);
    }

    /**
     * Test of completeAttributes method, of class HibernateMappingCompletionManager.
     */
    @Test
    public void testCompleteAttributes() {
        System.out.println("completeAttributes");
        // NOP
    }

    /**
     * Test of completeElements method, of class HibernateMappingCompletionManager.
     */
    @Test
    public void testCompleteElements() {
        System.out.println("completeElements");
        //NOP
    }
    
    private List<HibernateCompletionItem> query(int caretOffset) throws Exception {
        List<HibernateCompletionItem> completionItems = new ArrayList<HibernateCompletionItem>();
        assert(instanceDocument != null);
        HibernateMappingCompletionQuery instance = new HibernateMappingCompletionQuery(CompletionProvider.COMPLETION_QUERY_TYPE,
                caretOffset);
        instance.getCompletionItems(instanceDocument, caretOffset, completionItems);
        return completionItems;
    }

}

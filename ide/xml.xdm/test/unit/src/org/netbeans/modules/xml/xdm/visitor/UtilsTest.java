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

package org.netbeans.modules.xml.xdm.visitor;

import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.modules.xml.xdm.Util;

/**
 *
 * @author nam
 */
public class UtilsTest extends TestCase {
    
    public UtilsTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testReplaceDocument() throws Exception {
        Document doc1 = Util.getResourceAsDocument("visitor/old.xml");
        Document doc2 = Util.getResourceAsDocument("visitor/new.xml");
        String newString = doc2.getText(0, doc2.getLength());
        Utils.replaceDocument(doc1, newString);
        assertEquals(doc1.getText(0, doc1.getLength()), newString);
    }
}

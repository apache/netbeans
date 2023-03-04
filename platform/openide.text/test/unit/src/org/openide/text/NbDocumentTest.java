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

package org.openide.text;

import javax.swing.text.*;


import org.netbeans.junit.*;


/** Testing LineSet impl for CloneableEditorSupport.
 *
 * @author Jaroslav Tulach
 */
public class NbDocumentTest extends NbTestCase {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    private StyledDocument doc = new DefaultStyledDocument();

    public NbDocumentTest(String testName) {
        super(testName);
    }

    protected void setUp () {
	doc = createStyledDocument();
    }
    
    protected StyledDocument createStyledDocument() {
        return new DefaultStyledDocument();
    }


    public void testMarkGuardedAndBack() throws Exception {
        doc.insertString (0, "Line1\nLine2\n", null);
    
        assertEquals ("Document has correct number of lines ",
	        3, doc.getDefaultRootElement().getElementCount());
        
        NbDocument.markGuarded(doc, 0, doc.getLength());

        assertEquals ("Document has correct number of lines ",
	        3, doc.getDefaultRootElement().getElementCount());

        NbDocument.unmarkGuarded(doc, 0, doc.getLength());

        assertEquals ("Document has correct number of lines ",
	        3, doc.getDefaultRootElement().getElementCount());

    }
}

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
package org.netbeans.modules.editor.hints;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import org.netbeans.editor.BaseKit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class HintsControllerImplTest extends NbTestCase {
    
    public HintsControllerImplTest(String name) {
        super(name);
    }
    
    public void testComputeLineSpan() throws Exception {
        doTestComputeLineSpan(new DocumentCreator() {
            public Document createDocument() {
                return new NbEditorDocument(BaseKit.class);
            }
        });
        doTestComputeLineSpan(new DocumentCreator() {
            public Document createDocument() {
                return new DefaultStyledDocument();
            }
        });
    }

    public void testSubFixesLeak() throws Exception {
        class TestFix implements Fix {
            @Override public String getText() { return null; }
            @Override public ChangeInfo implement() throws Exception { return null; }
        }

        Fix main = new TestFix();
        Fix sub  = new TestFix();

        HintsControllerImpl.attachSubfixes(main, Collections.singletonList(sub));

        Reference<Fix> mainRef = new WeakReference<Fix>(main);
        Reference<Fix> subRef = new WeakReference<Fix>(sub);

        main = null;
        sub = null;

        assertGC("main", mainRef);
        assertGC("sub", subRef);
    }

    public void testSubFixesEquals() throws Exception {
        class TestFix implements Fix {
            @Override public String getText() { return null; }
            @Override public ChangeInfo implement() throws Exception { return null; }
            @Override public int hashCode() { return 0; }
            @Override public boolean equals(Object obj) {
                return obj != null && this.getClass() == obj.getClass();
            }
        }

        class SubFix implements Fix {
            @Override public String getText() { return null; }
            @Override public ChangeInfo implement() throws Exception { return null; }
        }

        Fix first = new TestFix();
        Fix firstSub = new SubFix();
        Fix second  = new TestFix();
        Fix secondSub  = new SubFix();

        HintsControllerImpl.attachSubfixes(first, Collections.singletonList(firstSub));
        HintsControllerImpl.attachSubfixes(second, Collections.singletonList(secondSub));

        assertSame(firstSub, HintsControllerImpl.getSubfixes(first).iterator().next());
        assertSame(secondSub, HintsControllerImpl.getSubfixes(second).iterator().next());
    }

    private void doTestComputeLineSpan(DocumentCreator creator) throws Exception {
        Document bdoc = creator.createDocument();
        
        bdoc.insertString(0, "  1234  \n 567\n567 \n456", null);
        
        assertSpan(bdoc, 1,  2,  6);
        assertSpan(bdoc, 2, 10, 13);
        assertSpan(bdoc, 3, 14, 17);
        assertSpan(bdoc, 4, 19, 22);
        assertSpan(bdoc, 5, 19, 22);
        assertSpan(bdoc, 6, 19, 22);
        
        bdoc = creator.createDocument();
        
        bdoc.insertString(0, "456", null);
        
        assertSpan(bdoc, 1, 0, 3);
        
        bdoc = creator.createDocument();
        
        bdoc.insertString(0, " ", null);
        
        assertSpan(bdoc, 1, 0, 0);
    }
    
    private static interface DocumentCreator {
        public Document createDocument();
    }
    
    private void assertSpan(Document doc, int lineNumber, int... expectedSpan) throws Exception {
        int[] returnedSpan = HintsControllerImpl.computeLineSpan(doc, lineNumber);
        
        assertTrue(Arrays.toString(returnedSpan), Arrays.equals(expectedSpan, returnedSpan));
    }
}

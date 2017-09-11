/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.java.source.parsing;

import java.io.Reader;
import java.io.Writer;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Lahoda
 */
public class SourceFileObjectTest extends NbTestCase {
    private static final RequestProcessor RP = new RequestProcessor(SourceFileObjectTest.class.getName(), 1, false, false);
    
    public SourceFileObjectTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDeadlock() throws Exception {
        clearWorkDir();
        
        FileObject work = FileUtil.toFileObject(getWorkDir());
        FileObject data = FileUtil.createData(work, "test.java");
        Document doc = DataObject.find(data).getLookup().lookup(EditorCookie.class).openDocument();
        SourceFileObject sfo = new SourceFileObject(
            new AbstractSourceFileObject.Handle(data, work),
            new FilterImplementation(doc),
            null,
            true);
    }
    
    private static final class FilterImplementation implements JavaFileFilterImplementation {

        private Document doc;

        public FilterImplementation(Document doc) {
            this.doc = doc;
        }
        
        public Reader filterReader(Reader r) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public CharSequence filterCharSequence(CharSequence charSequence) {
            try {
                RequestProcessor.Task t = RP.post(new Runnable() {
                    public void run() {
                        try {
                            doc.insertString(0, "1", null);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });

                assertTrue("Deadlock detected.", t.waitFinished(10000));
            } catch (InterruptedException e) {
                Exceptions.printStackTrace(e);
            }
            
            return charSequence;
        }

        public Writer filterWriter(Writer w) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addChangeListener(ChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeChangeListener(ChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
}

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

package org.netbeans.modules.editor.java;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.editor.indent.api.Reformat;

/**
 * Java formatter tests.
 *
 * @autor Miloslav Metelka
 */
public class JavaFormatterUnitTestCase extends JavaBaseDocumentUnitTestCase {

    public JavaFormatterUnitTestCase(String testMethodName) {
        super(testMethodName);
    }

    /**
     * Perform new-line insertion followed by indenting of the new line
     * by the formatter.
     * The caret position should be marked in the document text by '|'.
     */
    protected void indentNewLine() {
        // this actually only inserts \n in the document, the indentation is
        // hooked through the ExtKit.ExtDefaultKeyTypedAction.checkIndentHotChars(),
        // which calls f.getReformatBlock and f.reformat
        // IMO this should just be replaced by simple doc.insertString(getCaretOffset(), "\n", null)
        Indent indenter = Indent.get(getDocument());
        indenter.lock();
        try {
            int offset = indenter.indentNewLine(getCaretOffset());
            getCaret().setDot(offset);
        } catch (BadLocationException ble) {
            throw new IllegalStateException(ble);
        } finally {
            indenter.unlock();
        }
    }
    
    /**
     * Perform reformatting of the whole document's text.
     */
    protected void reformat() {
        final BaseDocument doc = getDocument();
        final Reformat formatter = Reformat.get(getDocument());
        formatter.lock();
        try {
            doc.runAtomic (new Runnable () {
                public void run () {
                    try {
                        formatter.reformat(0, doc.getLength());
                    } catch (BadLocationException e) {
                        e.printStackTrace(getLog());
                        fail(e.getMessage());
                    }
                }
            });
        } finally {
            formatter.unlock();
        }
    }
    
}

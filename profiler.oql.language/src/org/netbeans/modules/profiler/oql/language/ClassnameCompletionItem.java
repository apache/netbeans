/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.profiler.oql.language;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ClassnameCompletionItem implements CompletionItem {
    final private String text;
    final private String sortPrefix;
    final private int caret;
    final private int correction;
    final private String pkgName;
    final private String typeName;

    final private static Color fieldColor = Color.decode("0xa38000");

    public ClassnameCompletionItem(String sortPrefix, String text, int caretOffset) {
        this(sortPrefix, text, caretOffset, 0);
    }

    public ClassnameCompletionItem(String sortPrefix, String text, int caretOffset, int correction) {
        this.text = text;
        this.caret = caretOffset;
        this.correction = correction;
        this.sortPrefix = sortPrefix;
        int pkgPos = text.lastIndexOf('.');
        if (pkgPos > -1) {
            pkgName = text.substring(0, pkgPos);
            typeName = text.substring(pkgPos + 1);
        } else {
            pkgName = "";
            typeName = text;
        }
    }

    public CompletionTask createDocumentationTask() {
        return null;
    }

    public CompletionTask createToolTipTask() {
        return null;
    }

    public void defaultAction(JTextComponent component) {
        final BaseDocument doc = (BaseDocument) component.getDocument();
        doc.runAtomicAsUser(new Runnable() {

            @Override
            public void run() {
                try {
                    doc.remove(caret, correction);
                    doc.insertString(caret, text, null);
                } catch (BadLocationException ex) {
                    // shouldn't happen
                }
            }
        });
        //This statement will close the code completion box:
        Completion.get().hideAll();

    }

    public CharSequence getInsertPrefix() {
        return text;
    }

    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(typeName + " (" + text + ")", null, g, defaultFont);
    }

    public int getSortPriority() {
        return 0;
    }

    public CharSequence getSortText() {
        return sortPrefix + "_" + text;
    }

    public boolean instantSubstitution(JTextComponent component) {
        defaultAction(component);
        return true;
    }

    public void processKeyEvent(KeyEvent evt) {
        
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(null, "<b>"+typeName+"</b> <i>(" + text + ")</i>", null, g, defaultFont, (selected ? Color.white : fieldColor), width, height, selected);
    }

}

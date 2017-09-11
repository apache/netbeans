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

package org.netbeans.modules.xml.multiview;

import org.openide.ErrorManager;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;
import java.awt.*;

/**
 * Utils.java
 *
 * Created on November 16, 2004, 3:21 PM
 * @author mkuchtiak
 */
public class Utils {
    private static final int WAIT_FINISHED_TIMEOUT = 10000;

    /** This method update document in editor after change in beans hierarchy.
     * It takes old document and new document in String.
     * To preserve changes outside of root element only root element is replaced.
     * To avoid regeneration of whole document in text editor following steps are done:
     *  1) compare the begin of both documents (old one and new one)
     *     - find the first position where both documents differ
     *  2) do the same from the ends of documents
     *  3) remove old middle part of text (modified part) and insert new one
     *
     * @param doc original document
     * @param newDoc new value of whole document
     */
    public static boolean replaceDocument(final StyledDocument doc, final String newDoc) {
        if (doc == null) {
            return true;
        }
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    String origDocument = filterEndLines(doc.getText(0, doc.getLength()));
                    String newDocument = filterEndLines(newDoc);

                    if (origDocument.equals(newDocument)) {
                        // no change in document
                        return;
                    }

                    char[] origChars = origDocument.toCharArray();
                    char[] newcChars = newDocument.toCharArray();
                    int tailIndex = origChars.length;
                    int delta = newcChars.length - tailIndex;
                    int n = delta < 0 ? tailIndex + delta : tailIndex;
                    int offset;
                    for (offset = 0; offset < n; offset++) {
                        if (origChars[offset] != newcChars[offset]) {
                            break;
                        }
                    }
                    n = delta < 0 ? offset - delta : offset;
                    for (int i = tailIndex - 1; i >= n; i--) {
                        if (origChars[i] == newcChars[i + delta]) {
                            tailIndex = i;
                        } else {
                            break;
                        }
                    }

                    String s = newDocument.substring(offset, tailIndex + delta);
                    int length = tailIndex - offset;
                    if (doc instanceof AbstractDocument) {
                        ((AbstractDocument) doc).replace(offset, length, s, null);
                    } else {
                        if (length > 0) {
                            doc.remove(offset, length);
                        }
                        if (s.length() > 0) {
                            doc.insertString(offset, s, null);
                        }
                    }
                } catch (BadLocationException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        };
        NbDocument.runAtomic(doc, runnable);
        return true;
    }

    /** Filter characters #13 (CR) from the specified String
     * @param str original string
     * @return the string without #13 characters
     */
    private static String filterEndLines(String str) {
        char[] text = str.toCharArray();
        if (text.length == 0) {
            return "";
        }
        int pos = 0;
        for (int i = 0; i < text.length; i++) {
            char c = text[i];
            if (c != 13) {
                if (pos != i) {
                    text[pos] = c;
                }
                pos++;
            }
        }
        return new String(text, 0, pos);
    }

    /**
     * Sets focus to the next focusable component according to focus traversal policy
     * @param component currently focused component
     */
    public static void focusNextComponent(Component component) {
        Container focusCycleRoot = component.getFocusCycleRootAncestor();
        if (focusCycleRoot == null) {
            return;
        }
        final FocusTraversalPolicy focusTraversalPolicy = focusCycleRoot.getFocusTraversalPolicy();
        if (focusTraversalPolicy == null) {
            return;
        }
        final Component componentAfter = focusTraversalPolicy.getComponentAfter(focusCycleRoot, component);
        if (componentAfter != null) {
            componentAfter.requestFocus();
        }
    }

    /**
     * Scroll panel to make the component visible
     * @param component
     */
    public static void scrollToVisible(final JComponent component) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                component.scrollRectToVisible(new Rectangle(10, component.getHeight()));
            }
        });
    }

    /**
     * Make sure that the code will run in AWT dispatch thread
     * @param runnable
     */
    public static void runInAwtDispatchThread(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }

    }

    /**
     * Utility that sets border and traversal keys for JTextArea in JTextField style
     */
    public static void makeTextAreaLikeTextField(javax.swing.JTextArea ta, javax.swing.JTextField tf) {
        ta.setBorder(tf.getBorder());
        ta.setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                                 tf.getFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        ta.setFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                                 tf.getFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
    }

    public static void waitFinished(RequestProcessor.Task task) {
        if (task.getDelay() > 0 && !task.isFinished()) {
            try {
                task.waitFinished(WAIT_FINISHED_TIMEOUT);
            } catch (InterruptedException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

}

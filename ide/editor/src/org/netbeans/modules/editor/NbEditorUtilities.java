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

package org.netbeans.modules.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.JEditorPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.JumpList;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.openide.cookies.LineCookie;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.windows.TopComponent;
import org.openide.util.Lookup;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import java.util.MissingResourceException;
import java.awt.Toolkit;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.filesystems.FileObject;

/**
* Various utilities
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorUtilities {

    /** Get the dataobject from the document's StreamDescriptionProperty property. */
    public static DataObject getDataObject(Document doc) {
        Object sdp = doc == null ? null : doc.getProperty(Document.StreamDescriptionProperty);
        if (sdp instanceof DataObject) {
            return (DataObject)sdp;
        }
        return null;
    }

    /**
     * Verify whether the given document is still being actively used
     * by the corresponding editor support.
     */
    public static boolean isDocumentActive(Document doc) {
        DataObject dob = getDataObject(doc);
        if (dob != null) {
            EditorCookie editorCookie = (EditorCookie)dob.getCookie(EditorCookie.class);
            if (editorCookie != null) {
                Document ecDoc = editorCookie.getDocument(); // returns null if closed
                if (ecDoc == doc) { // actively used by ec
                    return true;
                }
            }
        }

        return false;
    }

    /** Get the fileobject from the document's StreamDescriptionProperty property. */
    public static FileObject getFileObject(Document doc) {
        Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
        if (sdp instanceof FileObject) {
            return (FileObject)sdp;
        }
        if (sdp instanceof DataObject) {
            return ((DataObject)sdp).getPrimaryFile();
        }
        return null;
    }

    /** This method is a composition of <code>Utilities.getIdentifierBlock()</code>
    * and <code>SyntaxSupport.getFunctionBlock()</code>.
    * @return null if there's no identifier at the given position.
    *   identifier block if there's identifier but it's not a function call.
    *   three member array for the case that there is an identifier followed
    *   by the function call character. The first two members are members
    *   of the identifier block and the third member is the second member
    *   of the function block.
    */
    public static int[] getIdentifierAndMethodBlock(BaseDocument doc, int offset)
    throws BadLocationException {
        int[] idBlk = Utilities.getIdentifierBlock(doc, offset);
        if (idBlk != null) {
            int[] funBlk = ((ExtSyntaxSupport)doc.getSyntaxSupport()).getFunctionBlock(idBlk);
            if (funBlk != null) {
                return new int[] { idBlk[0], idBlk[1], funBlk[1] };
            }
        }
        return idBlk;
    }

    /** Get the line object from the given position.
    * @param doc document for which the line is being retrieved
    * @param offset position in the document
    * @param original whether to retrieve the original line (true) before
    *   the modifications were done or the current line (false)
    * @return the line object
    * @deprecated Replaced by more generic method having {@link javax.swing.text.Document} parameter.
    */
    @Deprecated
    public static Line getLine(BaseDocument doc, int offset, boolean original) {
        DataObject dob = getDataObject(doc);
        if (dob != null) {
            LineCookie lc = (LineCookie)dob.getCookie(LineCookie.class);
            if (lc != null) {
                Line.Set lineSet = lc.getLineSet();
                if (lineSet != null) {
                    try {
                        int lineOffset = Utilities.getLineOffset(doc, offset);
                        return original
                               ? lineSet.getOriginal(lineOffset)
                               : lineSet.getCurrent(lineOffset);
                    } catch (BadLocationException e) {
                    }

                }
            }
        }
        return null;
    }

    /** Get the line object from the given position.
     * @param doc document for which the line is being retrieved
     * @param offset position in the document
     * @param original whether to retrieve the original line (true) before
     *   the modifications were done or the current line (false)
     * @return the line object
     */
    public static Line getLine(Document doc, int offset, boolean original) {
        DataObject dob = getDataObject(doc);
        if (dob != null) {
            LineCookie lc = (LineCookie)dob.getCookie(LineCookie.class);
            if (lc != null) {
                Line.Set lineSet = lc.getLineSet();
                if (lineSet != null) {
                    Element lineRoot = (doc instanceof AbstractDocument)
                        ? ((AbstractDocument)doc).getParagraphElement(0).getParentElement()
                        : doc.getDefaultRootElement();
                    int lineIndex = lineRoot.getElementIndex(offset);
                    return original
                           ? lineSet.getOriginal(lineIndex)
                           : lineSet.getCurrent(lineIndex);
                }
            }
        }
        return null;
    }

    /** Get the line object from the component's document and caret position */
    public static Line getLine(JTextComponent target, boolean original) {
        return getLine((BaseDocument)target.getDocument(),
                       target.getCaret().getDot(), original);
    }

    /** Get the top-component for the target copmonent */
    public static TopComponent getTopComponent(JTextComponent target) {
        return (TopComponent)SwingUtilities.getAncestorOfClass(TopComponent.class, target);
    }

    /** Get the top-component for the target copmonent */
    public static TopComponent getOuterTopComponent(JTextComponent target) {
        TopComponent tc = null;
        TopComponent parent = (TopComponent)SwingUtilities.getAncestorOfClass(TopComponent.class, target);
        while (parent != null) {
            tc = parent;
            parent = (TopComponent)SwingUtilities.getAncestorOfClass(TopComponent.class, tc);
        }
        return tc;
    }
    

    /** Add the jump-list entry for the for the component that's opened
    * over the given dataobject if any.
    */
    public static void addJumpListEntry(DataObject dob) {
        final EditorCookie ec = (EditorCookie)dob.getCookie(EditorCookie.class);
        if (ec != null) {
            final Timer timer = new Timer(500, null);
            timer.addActionListener(
                new ActionListener() {

                    private int countDown = 10;

                    public void actionPerformed(ActionEvent evt) {
                        SwingUtilities.invokeLater(
                            new Runnable() {
                                public void run() {
                                    if (--countDown >= 0) {
                                        JEditorPane[] panes = ec.getOpenedPanes();
                                        if (panes != null && panes.length > 0) {
                                            JumpList.checkAddEntry(panes[0]);
                                            timer.stop();
                                        }
                                    } else {
                                        timer.stop();
                                    }
                                }
                            }
                        );
                    }
                }
            );
            timer.start();
        }
    }

    /** Merge two string arrays into one. */
    public static String[] mergeStringArrays(String[] a1, String[] a2) {
        String[] ret = new String[a1.length + a2.length];
        for (int i = 0; i < a1.length; i++) {
            ret[i] = a1[i];
        }
        for (int i = 0; i < a2.length; i++) {
            ret[a1.length + i] = a2[i];
        }
        return ret;
    }

    /**
     * Gets the mime type of a document. If the mime type can't be determined
     * this method will return <code>null</code>. This method should work reliably
     * for Netbeans documents that have their mime type stored in a special
     * property. For any other documents it will probably just return <code>null</code>.
     * 
     * @param doc The document to get the mime type for.
     * 
     * @return The mime type of the document or <code>null</code>.
     * @see NbEditorDocument#MIME_TYPE_PROP
     */
    public static String getMimeType(Document doc) {
        return DocumentUtilities.getMimeType(doc);
    }

    /**
     * Gets the mime type of a document in <code>JTextComponent</code>. If
     * the mime type can't be determined this method will return <code>null</code>.
     * It tries to determine the document's mime type first and if that does not
     * work it uses mime type from the <code>EditorKit</code> attached to the
     * component.
     * 
     * @param component The component to get the mime type for.
     * 
     * @return The mime type of a document opened in the component or <code>null</code>.
     * @since 1.29
     */
    public static String getMimeType(JTextComponent component) {
        return DocumentUtilities.getMimeType(component);
    }
    
    /** Displays ErrorManager window with the localized message. If bundleKey parameter is not founded in bundle
     *  it is considered as displayable text value. */
    public static void invalidArgument(String bundleKey) {
        IllegalArgumentException iae=new IllegalArgumentException("Invalid argument"); //NOI18N
        Toolkit.getDefaultToolkit().beep();
        ErrorManager errMan=(ErrorManager)Lookup.getDefault().lookup(ErrorManager.class);
        
        if (errMan!=null) {
            errMan.annotate(iae, ErrorManager.USER, iae.getMessage(), getString(bundleKey), null, null); //NOI18N
        }
        throw iae;
    }
    
    private static String getString(String key) {
        try {
            return NbBundle.getBundle(NbEditorUtilities.class).getString(key);
        } catch (MissingResourceException e) {
            Logger.getLogger("global").log(Level.INFO,null, e);
            return key;
        }
    }


}

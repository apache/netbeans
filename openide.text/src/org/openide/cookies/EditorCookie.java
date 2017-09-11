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
package org.openide.cookies;

import org.openide.util.Task;

import java.io.IOException;

import javax.swing.text.StyledDocument;


/** Cookie defining standard operations with a text document and
* an editor that can display it.
* The cookie extends <code>LineCookie</code>
* because all implementations of editors should support access
* by lines.
* <P>
* The cookie provides interfaces for opening the file, closing the editor,
* background loading, saving of the document, and notification of modification.
* <P><STRONG>Warning:</STRONG> it is not guaranteed that the document
* returned from this cookie will persist for the full lifetime of the
* cookie. That is, if the editor window is closed and then reopened,
* it is possible for the document to change.
* The <CODE>{@link EditorCookie.Observable}</CODE> allows listening to
* changes of the state of the document. You should do this
* if you are listening to changes in the document itself, as otherwise
* you would get no notifications from a reopened document.
*
* @author Jaroslav Tulach
*/
public interface EditorCookie extends LineCookie {
    /** Instructs an editor to be opened. The operation can
    * return immediately and the editor may be opened later.
    * There can be more than one editor open, so one of them should be
    * arbitrarily chosen and selected (typically given focus).
    */
    public void open();

    /** Closes all opened editors (if the user agrees) and
    * flushes content of the document to file.
    *
    * @return <code>false</code> if the operation has been cancelled
    */
    public boolean close();

    /** Should load the document into memory. This is done
    * in a different thread. A task for the thread is returned
    * so other components can test whether the loading is finished or not.
    * <p><em>Note</em> that this does not involve opening the actual Editor window.
    * For that, use {@link #open}.
    *
    * @return task for control over the loading process
    */
    public Task prepareDocument();

    /** Get the document (and wait).
     * See the {@link org.openide.text Editor API} for details on how this document should behave.
    * <P>
    * If the document is not yet loaded the method blocks until
    * it is.
    * <p><em>Note</em> that this does not involve opening the actual Editor window.
    * For that, use {@link #open}.
    * 
    * <p>Method will throw {@link org.openide.util.UserQuestionException} exception
    * if file size is too big. This exception could be caught and 
    * its method {@link org.openide.util.UserQuestionException#confirmed} 
    * can be used for confirmation. You need to call {@link #openDocument}}
    * one more time after confirmation.
    *
    * @return the styled document for this cookie
    * @exception IOException if the document could not be loaded
    */
    public StyledDocument openDocument() throws IOException;

    /** Get the document (but do not block).
    * <p><em>Note</em> that this does not involve opening the actual Editor window.
    * For that, use {@link #open}.
    *
    * @return the document, or <code>null</code> if it has not yet been loaded
    */
    public StyledDocument getDocument();

    /** Save the document.
     * This is done in the current thread.
    * @exception IOException on I/O error
    */
    public void saveDocument() throws IOException;

    /** Test whether the document is modified.
    * @return <code>true</code> if the document is in memory and is modified; <code>false</code> otherwise
    */
    public boolean isModified();

    /** Get a list of all editor panes opened on this object.
    * The first item in the array should represent the component
    * that is currently selected or that was most recently selected.
    * (Typically, multiple panes will only be open as a result of cloning the editor component.)
    *
    * <p>The resulting panes are useful for a range of tasks;
    * most commonly, getting the current cursor position or text selection,
    * including the <code>Caret</code> object.
    * <p>This method may also be used to test whether an object is already open
    * in an editor, without actually opening it.
    *
    * @return an array of panes, or <code>null</code> if no pane is open from this file.
    *   In no case is an empty array returned.
    */
    public javax.swing.JEditorPane[] getOpenedPanes();

    /** The interface extends EditorCookie and allows observing changes
     * in state of the text document.
     *
     * @since 3.40
     */
    interface Observable extends EditorCookie {
        /** This property is fired when the result of {@link #getDocument}
         * has changed. Typically it is after closing the editor.
         */
        public static final String PROP_DOCUMENT = "document";

        /** This property is fired when modified state of the document
         * has changed. See {@link #isModified}.
         */
        public static final String PROP_MODIFIED = "modified";

        /** This property is fired when list of opened panes for the document
         * has changed. See {@link #getOpenedPanes}.
         */
        public static final String PROP_OPENED_PANES = "openedPanes";

        /** Add a PropertyChangeListener to the listener list.
         * @param l  the PropertyChangeListener to be added
         */
        void addPropertyChangeListener(java.beans.PropertyChangeListener l);

        /** Remove a PropertyChangeListener from the listener list.
         * @param l the PropertyChangeListener to be removed
         */
        void removePropertyChangeListener(java.beans.PropertyChangeListener l);
    }
}

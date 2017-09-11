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

package org.netbeans.modules.editor.bookmarks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;
import org.openide.modules.ModuleInstall;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;


/**
 * Module installation class for editor.
 *
 * @author Miloslav Metelka
 */
class EditorBookmarksModule extends ModuleInstall {

    private static final String         DOCUMENT_TRACKER_PROP = "EditorBookmarksModule.DOCUMENT_TRACKER_PROP"; //NOI18N
    
    private BookmarksInitializer        bookmarksInitializer;

    @Override
    public void restored () {
        if (bookmarksInitializer == null) {
            bookmarksInitializer = new BookmarksInitializer ();
        }
        RequestProcessor.getDefault().post(new Runnable () {
            @Override
            public void run () {
                for(JTextComponent c : EditorRegistry.componentList()) {
                    BookmarkList.get(c.getDocument()); // Initialize the bookmark list
                }
            }
        });
    }
    
    /**
     * Called when all modules agreed with closing and the IDE will be closed.
     */
    @Override
    public boolean closing () {
        // this used to be called from close(), but didn't save properly on JDK6,
        // no idea why, see #120880
        finish ();
        return super.closing ();
    }
    
    /**
     * Called when module is uninstalled.
     */
    @Override
    public void uninstalled () {
        finish ();
    }
    
    private void finish () {
        // Stop listening on projects closing
        BookmarksPersistence.get().endProjectsListening();
        if (bookmarksInitializer != null)
            bookmarksInitializer.destroy();
    }
    
    
    // innerclasses ............................................................
    
    private class BookmarksInitializer implements PropertyChangeListener, Runnable {

        private final PropertyChangeListener      documentListener;
        private final RequestProcessor.Task       myTask;
        
        @SuppressWarnings("LeakingThisInConstructor")
        BookmarksInitializer () {
            EditorRegistry.addPropertyChangeListener (this);
            documentListener = WeakListeners.propertyChange (this, null);
            myTask = BookmarksPersistence.get().createTask(this);
        }

        @Override
        public void propertyChange (PropertyChangeEvent evt) {
            // event for the editors tracker
            if (evt.getSource () == EditorRegistry.class) {
                if (evt.getPropertyName () == null || 
                    EditorRegistry.FOCUS_GAINED_PROPERTY.equals (evt.getPropertyName ())
                ) {
                    JTextComponent jtc = (JTextComponent) evt.getNewValue ();
                    PropertyChangeListener l = (PropertyChangeListener) jtc.getClientProperty (DOCUMENT_TRACKER_PROP);
                    if (l == null) {
                        jtc.putClientProperty (DOCUMENT_TRACKER_PROP, documentListener);
                        jtc.addPropertyChangeListener (documentListener);
                    }
                    myTask.schedule(100);
                }
                return;
            }

            // event for the document tracker
            if (evt.getSource () instanceof JTextComponent) {
                if (evt.getPropertyName () == null ||
                    "document".equals (evt.getPropertyName ())
                ) { //NOI18N
                    Document newDoc = (Document) evt.getNewValue ();
                    if (newDoc != null) {
                        myTask.schedule(100);
                    }
                }
            }
        }

        void destroy () {
            EditorRegistry.removePropertyChangeListener (this);
        }
        
        @Override
        public void run() {
            JTextComponent jtc = EditorRegistry.focusedComponent();
            if (jtc != null) {
                Document doc = jtc.getDocument();
                if (doc != null) {
                    BookmarkList.get (jtc.getDocument ()); // Initialize the bookmark list
                }
            }
        }
    }
}

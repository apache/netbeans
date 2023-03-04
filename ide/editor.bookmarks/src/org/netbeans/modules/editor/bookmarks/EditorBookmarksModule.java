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

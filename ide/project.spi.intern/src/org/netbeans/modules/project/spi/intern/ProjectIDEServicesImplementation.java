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
package org.netbeans.modules.project.spi.intern;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.Icon;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.UserQuestionException;

/**
 *
 * @author Tomas Stupka
 */
public interface ProjectIDEServicesImplementation {
    
    ////////////////////////////////////////////////////////////////////////////
    // FileBuiltQuery services
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Creates a FileBuiltQuerySource
     * @param sourceFile
     * @return 
     */
    public FileBuiltQuerySource createFileBuiltQuerySource(FileObject sourceFile);

    /**
     * Represents a source object for a FileBuiltQuery.Status as it doesn't 
     * necessarily be a FileObject - e.g. a DataObject is used in case of the
     * NetBeans implementation. 
     */
    public interface FileBuiltQuerySource {
        public static final String PROP_MODIFIED = "modified";
        public boolean isModified();
        public boolean isValid();
        public FileObject getFileObject();
        public void addPropertyChangeListener(PropertyChangeListener l);
        public void removePropertyChangeListener(PropertyChangeListener l);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Notify services
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Intended behavior.
     */
    public interface UserQuestionExceptionCallback {
        
        /**
         * Called later if the user accepted the question.
         */
        void accepted();
        
        /**
         * Called later if the user denied the question.
         */
        void denied();
        
        /**
         * Called later if the user accepted the question but there was in fact a problem.
         * @param e
         */
        void error(IOException e);
        
    }
    
    /**
     * Determines whether the provided IOException is a UserQuestionException - e.g. org.openide.util.UserQuestionException
     * @param ioe
     * @return 
     */
    public boolean isUserQuestionException(IOException ioe);

    /**
     * Handle a user question exception later (in the event thread).
     * Displays a dialog and invokes the appropriate method on the callback.
     * The callback will be notified in the event thread.
     * Use when catching {@link UserQuestionException} during {@link FileObject#lock}.
     * @param e
     * @param callback
     */
    public void handleUserQuestionException(IOException e, final UserQuestionExceptionCallback callback);
    
    /**
     * Presents the given message in a warning dialog
     * @param message 
     */
    public void notifyWarning(String message);
    
    ////////////////////////////////////////////////////////////////////////////
    // Misc utils & co.
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Loads an icon based on resource path.
     * Similar to {@link #loadImage(String, boolean)}, returns ImageIcon instead of Image.
     * 
     * <p>If the current look and feel is 'dark' (<code>UIManager.getBoolean("nb.dark.theme")</code>)
     * then the method first attempts to load image <i>&lt;original file name&gt;<b>_dark</b>.&lt;original extension&gt;</i>.
     * If such file doesn't exist the default one is loaded instead.
     * </p>
     * 
     * @param resource resource path of the icon (no initial slash)
     * @param localized localized resource should be used
     * @return ImageIcon or null, if the icon cannot be loaded.
     */
    public Icon loadIcon( String resource, boolean localized ); 

    public boolean isEventDispatchThread();
    
}

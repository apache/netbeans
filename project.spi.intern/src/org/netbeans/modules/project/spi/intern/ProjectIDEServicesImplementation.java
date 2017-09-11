/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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

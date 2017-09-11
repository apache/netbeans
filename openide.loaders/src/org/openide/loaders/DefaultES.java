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


package org.openide.loaders;


import java.io.IOException;

import org.netbeans.modules.openide.loaders.Unmodify;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node.Cookie;
import org.openide.text.DataEditorSupport;
import org.openide.windows.CloneableOpenSupport;


/** 
 * Basic editor support.
 *
 * @author Jaroslav Tulach
 */
final class DefaultES extends DataEditorSupport 
implements OpenCookie, EditCookie, EditorCookie.Observable, PrintCookie, CloseCookie, SaveAsCapable {
    /** SaveCookie for this support instance. The cookie is adding/removing 
     * data object's cookie set depending on if modification flag was set/unset. */
    private final SaveCookie saveCookie = new SaveCookieImpl();
    
    private CookieSet set;
    
    /** Constructor. 
     * @param obj data object to work on
     * @param set set to add/remove save cookie from
     */
    DefaultES (MultiDataObject obj, MultiDataObject.Entry entry, CookieSet set) {
        super(obj, null, new Environment(obj, entry));
        this.set = set;
        setMIMEType("text/plain"); // NOI18N
    }
    
    /** 
     * Overrides superclass method. Adds adding of save cookie if the document has been marked modified.
     * @return true if the environment accepted being marked as modified
     *    or false if it has refused and the document should remain unmodified
     */
    @Override
    protected boolean notifyModified () {
        if (!super.notifyModified()) 
            return false;

        addSaveCookie();

        return true;
    }

    /** Overrides superclass method. Adds removing of save cookie. */
    @Override
    protected void notifyUnmodified () {
        super.notifyUnmodified();
        removeSaveCookie(true);
    }
    
    @Override
    protected boolean asynchronousOpen() {
        return true;
    }

    @Override
    protected Pane createPane() {
        if (MultiDOEditor.isMultiViewAvailable()) {
            MultiDataObject mdo = (MultiDataObject) getDataObject();
            return MultiDOEditor.createMultiViewPane("text/plain", mdo); // NOI18N
        }
        return super.createPane();
    }
    
    /** Helper method. Adds save cookie to the data object. */
    private void addSaveCookie() {
        DataObject obj = getDataObject();

        // Adds save cookie to the data object.
        if(obj.getCookie(SaveCookie.class) == null) {
            set.add(saveCookie);
            obj.setModified(true);
        }
    }

    /** Helper method. Removes save cookie from the data object. */
    private void removeSaveCookie(boolean setModified) {
        DataObject obj = getDataObject();
        
        // Remove save cookie from the data object.
        Cookie cookie = obj.getCookie(SaveCookie.class);

        if(cookie != null && cookie.equals(saveCookie)) {
            set.remove(saveCookie);
            if (setModified) {
                obj.setModified(false);
            }
        }
    }

    
    /** Nested class. Environment for this support. Extends
     * <code>DataEditorSupport.Env</code> abstract class.
     */
    
    private static class Environment extends DataEditorSupport.Env {
        private static final long serialVersionUID = 5451434321155443431L;
        
        private MultiDataObject.Entry entry;
        
        /** Constructor. */
        public Environment(DataObject obj, MultiDataObject.Entry entry) {
            super(obj);
            this.entry = entry;
        }

        
        /** Implements abstract superclass method. */
        protected FileObject getFile() {
            return entry.getFile();
        }

        /** Implements abstract superclass method.*/
        protected FileLock takeLock() throws IOException {
            return entry.takeLock();
        }

        /** 
         * Overrides superclass method.
         * @return text editor support (instance of enclosing class)
         */
        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            DataObject obj = getDataObject ();
            DefaultES ret;
            if (obj instanceof DefaultDataObject) {
                ret = ((DefaultDataObject) obj).getCookie(DefaultES.class, true);
            } else {
                ret = getDataObject().getCookie(DefaultES.class);
            }
            
            // this is necessary as for large files, this methods sets flag that
            // prevents UserQuestionException
            super.findCloneableOpenSupport ();

            return ret;
        }
    } // End of nested Environment class.

    private class SaveCookieImpl implements SaveCookie, Unmodify {

        public SaveCookieImpl() {
        }

        /** Implements <code>SaveCookie</code> interface. */
        public void save() throws IOException {
            DefaultES.this.saveDocument();
        }

        @Override
        public void unmodify() {
            removeSaveCookie(false);
        }
    }

}

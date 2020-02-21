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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.asm.core.editor;

// This file was initially based on org.netbeans.modules.java.JavaEditor
// (Rev 61)
import java.io.*;

import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.cnd.asm.core.dataobjects.AsmDataObject;
import org.netbeans.modules.cnd.support.ReadOnlySupport;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.text.*;
import org.openide.loaders.DataObject;

import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node.Cookie;
import org.openide.windows.CloneableOpenSupport;

/**
 *  Simple editor for Asm files.
 */
public class AsmEditorSupport extends DataEditorSupport implements EditCookie,
        EditorCookie, EditorCookie.Observable, OpenCookie, CloseCookie, PrintCookie, ReadOnlySupport {

    /**
     *  Create a new Editor support for the given C/C++/Fortran source.
     *  @param entry The (primary) file entry representing the C/C++/f95 source file
     */
    public AsmEditorSupport(DataObject obj) {
        super(obj, null, new Environment(obj));
    }
  
    @Override
    protected Pane createPane() {
        return (CloneableEditorSupport.Pane) MultiViews.createCloneableMultiView(MIMENames.ASM_MIME_TYPE, getDataObject());
    }
    
    /** 
     * Overrides superclass method. Adds adding of save cookie if the document has been marked modified.
     * @return true if the environment accepted being marked as modified
     *    or false if it has refused and the document should remain unmodified
     */
    @Override
    protected boolean notifyModified() {
        if (!super.notifyModified()) {
            return false;
        }

        addSaveCookie();

        return true;
    }

    /** Overrides superclass method. Adds removing of save cookie. */
    @Override
    protected void notifyUnmodified() {
        super.notifyUnmodified();

        removeSaveCookie();
    }

    @Override
    protected boolean asynchronousOpen() {
        return true;
    }

    /** SaveCookie for this support instance. The cookie is adding/removing
     * data object's cookie set depending on if modification flag was set/unset. */
    private final SaveCookie saveCookie = new SaveCookie() {

        /** Implements <code>SaveCookie</code> interface. */
        public void save() throws IOException {
            AsmEditorSupport.this.saveDocument();
            //AsmEditorSupport.this.getDataObject().setModified(false);
        }
    };
    private boolean isReadOnly = false;

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.isReadOnly = readOnly;
    }
    
    /** Helper method. Adds save cookie to the data object. */
    private void addSaveCookie() {
        AsmDataObject obj = (AsmDataObject) getDataObject();

        // Adds save cookie to the data object.
        if (obj.getCookie(SaveCookie.class) == null) {
            obj.addSaveCookie(saveCookie);
        }
    }

    /** Helper method. Removes save cookie from the data object. */
    private void removeSaveCookie() {
        AsmDataObject obj = (AsmDataObject) getDataObject();

        // Remove save cookie from the data object.
        Cookie cookie = obj.getCookie(SaveCookie.class);

        if (cookie != null && cookie.equals(saveCookie)) {
            obj.removeSaveCookie(saveCookie);
        }
    }

    /** Nested class. Environment for this support. Extends <code>DataEditorSupport.Env</code> abstract class. */
    private static class Environment extends DataEditorSupport.Env {

        private static final long serialVersionUID = 3035543168452715818L;

        /** Constructor. */
        public Environment(DataObject obj) {
            super(obj);
        }

        /** Implements abstract superclass method. */
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        /** Implements abstract superclass method.*/
        protected FileLock takeLock() throws IOException {
            ReadOnlySupport readOnly = getDataObject().getLookup().lookup(ReadOnlySupport.class);
            if (readOnly != null && readOnly.isReadOnly()) {
                throw new IOException(); // for read only state method must throw IOException
            } else {
                return ((AsmDataObject) getDataObject()).getPrimaryEntry().takeLock();
            }
        }

        /** 
         * Overrides superclass method.
         * @return text editor support (instance of enclosing class)
         */
        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return getDataObject().getCookie(AsmEditorSupport.class);
        }
    } // End of nested Environment class.    
}

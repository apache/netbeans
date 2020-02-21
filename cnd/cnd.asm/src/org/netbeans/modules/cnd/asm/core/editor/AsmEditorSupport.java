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

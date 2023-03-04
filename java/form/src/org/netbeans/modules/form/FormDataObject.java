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


package org.netbeans.modules.form;

import java.io.IOException;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.Lookup;

/** The DataObject for forms.
 *
 * @author Ian Formanek, Petr Hamernik
 */
public class FormDataObject extends MultiDataObject {
    private transient EditorSupport formEditor;
    private transient OpenEdit openEdit;

    /** The entry for the .form file */
    FileEntry formEntry;

    //--------------------------------------------------------------------
    // Constructors

    static final long serialVersionUID =-975322003627854168L;

    public FormDataObject(FileObject ffo, FileObject jfo, FormDataLoader loader)
        throws DataObjectExistsException
    {
        super(jfo, loader);
        formEntry = (FileEntry)registerEntry(ffo);
        getCookieSet().assign( SaveAsCapable.class, new SaveAsCapable() {
            @Override
            public void saveAs(FileObject folder, String fileName) throws IOException {
                getFormEditorSupport().saveAs( folder, fileName );
            }
        });
    }

    //--------------------------------------------------------------------
    // Other methods

    @Override
    public <T extends Cookie> T getCookie(Class<T> type) {
        T retValue;
        
        if (OpenCookie.class.equals(type) || EditCookie.class.equals(type)) {
            if (openEdit == null)
                openEdit = new OpenEdit();
            retValue = type.cast(openEdit);
        } else if (!type.equals(Cookie.class) && type.isAssignableFrom(getFormEditorSupportClass())) {
            // Avoid calling synchronized getFormEditorSupport() when invoked from node lookup
            // initialization from cookies (asking for base Node.Cookie).
            retValue = (T) getFormEditorSupport();
        } else {
            retValue = super.getCookie(type);
        }
        return retValue;
    }
    
    @Override
    public Lookup getLookup() {
        return isValid() ? getNodeDelegate().getLookup() : Lookup.EMPTY;
    }

    private class OpenEdit implements OpenCookie, EditCookie {
        @Override
        public void open() {
            // open form editor with form designer selected
            getFormEditorSupport().openDesign();
        }
        @Override
        public void edit() {
            // open form editor with java editor selected (form not loaded)
            getFormEditorSupport().openSource();
        }
    }

    public FileObject getFormFile() {
        return formEntry.getFile();
    }

    public boolean isReadOnly() {
        FileObject javaFO = getPrimaryFile();
        FileObject formFO = formEntry.getFile();
        return !javaFO.canWrite() || !formFO.canWrite();
    }

    public boolean formFileReadOnly() {
        return !formEntry.getFile().canWrite();
    }

    public final CookieSet getCookies() {
        return getCookieSet();
    }

    private Class getFormEditorSupportClass() {
        return Lookup.getDefault().lookup(FormServices.class).getEditorSupportClass(this);
    }

    public synchronized EditorSupport getFormEditorSupport() {
        if (formEditor == null) {
            FormServices services = Lookup.getDefault().lookup(FormServices.class);
            formEditor = services.createEditorSupport(this);
        }
        return formEditor;
    }

    FileEntry getFormEntry() {
        return formEntry;
    }

    /** Provides node that should represent this data object. When a node for
     * representation in a parent is requested by a call to getNode(parent) it
     * is the exact copy of this node with only parent changed. This
     * implementation creates instance <CODE>DataNode</CODE>.  <P> This method
     * is called only once.
     *
     * @return the node representation for this data object
     * @see DataNode
     */
    @Override
    protected Node createNodeDelegate() {
        FormServices services = Lookup.getDefault().lookup(FormServices.class);
        return services.createFormDataNode(this);
    }

    //--------------------------------------------------------------------
    // Serialization

    private void readObject(java.io.ObjectInputStream is)
        throws java.io.IOException, ClassNotFoundException {
        is.defaultReadObject();
    }

    @Override
    protected DataObject handleCopyRename(DataFolder df, String name, String ext) throws IOException {
        FileObject fo = getPrimaryEntry().copyRename (df.getPrimaryFile (), name, ext);
        return DataObject.find( fo );
    }

}

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
    transient private EditorSupport formEditor;
    transient private OpenEdit openEdit;

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

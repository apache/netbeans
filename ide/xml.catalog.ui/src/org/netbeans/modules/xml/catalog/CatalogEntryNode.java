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
package org.netbeans.modules.xml.catalog;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URI;
import javax.swing.Action;
import org.netbeans.modules.xml.catalog.lib.URLEnvironment;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.netbeans.modules.xml.catalog.spi.CatalogWriter;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.DeleteAction;
import org.openide.actions.EditAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ViewAction;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.ViewCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.CloneableEditorSupport.Env;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;

/**
 * Node representing single catalog entry. It can be viewed.
 */
final class CatalogEntryNode extends BeanNode implements EditCookie, Node.Cookie {

    private transient ViewCookie myView;
    private boolean isCatalogWriter;
    private CatalogReader catalogReader;
    
    public CatalogEntryNode(CatalogEntry entry) throws IntrospectionException {        
        super(entry);
        getCookieSet().add(this);
        catalogReader = entry.getCatalog();

        if (catalogReader instanceof CatalogWriter) {
            isCatalogWriter = true;
        }
    }

    public boolean isCatalogWriter() {
        return isCatalogWriter;
    }

    public javax.swing.Action getPreferredAction() {
        if (isCatalogWriter) 
            return SystemAction.get(EditAction.class);
        else 
            return SystemAction.get(ViewAction.class);
    }
    
    public void edit() {
        try {
            URI uri = new URI(getSystemID());
            File file = new File(uri);
            FileObject fo = FileUtil.toFileObject(file);
            boolean editPossible=false;
            if (fo!=null) {
                DataObject obj = DataObject.find(fo);
                EditCookie editCookie = obj.getCookie(EditCookie.class);
                if (editCookie!=null) {
                    editPossible=true;
                    editCookie.edit();
                }
            }
            if (!editPossible)
                org.openide.DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                            NbBundle.getMessage(CatalogEntryNode.class, "MSG_CannotOpenURI",getSystemID()), //NOI18N
                            NotifyDescriptor.INFORMATION_MESSAGE));
        } catch (Throwable ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    private CatalogReader getCatalogReader() {
        return catalogReader;
    }
    
    public Action[] getActions(boolean context) {
        if (isCatalogWriter)
            return new Action[] {
                SystemAction.get(EditAction.class),
                SystemAction.get(DeleteAction.class),
                null,
                SystemAction.get(PropertiesAction.class)
            };
        else
            return new Action[] {
                SystemAction.get(ViewAction.class),
                null,
                SystemAction.get(PropertiesAction.class)
            };
    }

    public Node.Cookie getCookie(Class clazz) {
        if (ViewCookie.class.equals(clazz)) {
            return getViewCookie();
        }
        if (SaveAsCapable.class.equals(clazz)) {
            return new MySaveAsCookie();
        }
        if (InputStream.class.equals(clazz)) {
            return new MyInputStreamCookie();
        }
        return super.getCookie(clazz);
    }

    private ViewCookie getViewCookie() {
        String sys = getSystemID();

        if (sys == null) {
            return null;
        }
        if (myView == null) {                    
            ViewEnv env = new ViewEnv(getPublicID(), sys);
            myView = new ViewCookieImpl(env);
        }
        return myView;
    }

    private class MySaveAsCookie implements Node.Cookie, SaveAsCapable {
        public void saveAs(FileObject folder, String fileName) throws IOException {
            FileObject newFile = folder.getFileObject(fileName);

            if (newFile == null) {
                newFile = FileUtil.createData(folder, fileName);
            }
            OutputStream output = newFile.getOutputStream();
            InputStream input = new URL(getSystemID()).openStream();
            
            try {
                byte[] buffer = new byte[4096];

                while (input.available() > 0) {
                    output.write(buffer, 0, input.read(buffer));
                }
            }
            finally {
                input.close();
                output.close();
            }
        }
    }

    private class MyInputStreamCookie extends InputStream implements Node.Cookie {
        
        private MyInputStreamCookie() {
            try {
                myInputStream = new URL(getSystemID()).openStream();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int available() throws IOException {
            return myInputStream.available();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return myInputStream.read(b);
        }

        @Override
        public int read() throws IOException {
            return myInputStream.read();
        }

        @Override
        public synchronized void reset() throws IOException {
            myInputStream.reset();
        }

        private InputStream myInputStream;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    private String getPublicID() {
        return ((CatalogEntry) getBean()).getPublicID();
    }
    
    private String getSystemID() {
        return ((CatalogEntry) getBean()).getSystemID();
    }
    
    public String getShortDescription() {
        String displayName = getPublicID();
        if(displayName.startsWith("SCHEMA:")) //NOI18N
            displayName = displayName.substring("SCHEMA:".length()); //NOI18N
        return displayName;
    }

    public void destroy() throws IOException {
        super.destroy();
        if (isCatalogWriter) {
            CatalogWriter catalogWriter = (CatalogWriter)((CatalogEntry)getBean()).getCatalog();
            catalogWriter.registerCatalogEntry(getPublicID(),null);
        }
    }

    private class ViewCookieImpl extends CloneableEditorSupport implements EditorCookie, ViewCookie {

        ViewCookieImpl(Env env) {
            super(env);
        }
                                
        protected String messageName() {
            return NbBundle.getMessage(CatalogEntryNode.class, "MSG_opened_entity", getPublicID());  // NOI18N
        }
        
        protected String messageSave() {
            return NbBundle.getMessage(CatalogEntryNode.class, "MSG_ENTITY_SAVE", getPublicID());  // NOI18N
        }
        
        protected java.lang.String messageToolTip() {
            String publicID = getPublicID();
            if(publicID.startsWith("SCHEMA:")) //NOI18N
                publicID = publicID.substring("SCHEMA:".length()); //NOI18N
            return publicID;
        }

        protected java.lang.String messageOpening() {
            return NbBundle.getMessage(CatalogEntryNode.class, "MSG_ENTITY_OPENING", getPublicID()); // NOI18N
        }
        
        protected java.lang.String messageOpened() {
            return NbBundle.getMessage(CatalogEntryNode.class, "MSG_ENTITY_OPENED", getPublicID()); // NOI18N
        }

        protected CloneableEditor createCloneableEditor() {
            CloneableEditor editor = new CloneableEditor(this) {
                public @Override int getPersistenceType() {
                    return TopComponent.PERSISTENCE_NEVER;
                }
            };
            editor.setActivatedNodes(new Node[] {CatalogEntryNode.this});
            return editor;
        }

    }    

    private class ViewEnv extends URLEnvironment {

        private static final long serialVersionUID =-5031004511063404433L;
        
        ViewEnv (String publicId, String systemId) {
            super(publicId, systemId);
        }

        public org.openide.windows.CloneableOpenSupport findCloneableOpenSupport() {
            return (ViewCookieImpl) CatalogEntryNode.this.getCookie(ViewCookieImpl.class);
        }
    }
}

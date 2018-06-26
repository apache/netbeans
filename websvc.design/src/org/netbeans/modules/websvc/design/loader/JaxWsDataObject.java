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

package org.netbeans.modules.websvc.design.loader;

import java.io.IOException;
import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.java.JavaI18nSupport;
import org.netbeans.modules.websvc.design.javamodel.ProjectService;
import org.netbeans.modules.websvc.design.javamodel.Utils;
import org.netbeans.modules.websvc.design.multiview.MultiViewSupport;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.windows.CloneableOpenSupport;

public final class JaxWsDataObject extends MultiDataObject {
    private static final RequestProcessor RP = new RequestProcessor(JaxWsDataObject.class);
    
    private static final long serialVersionUID = -2635172073868722799L;

    public static final String CLASS_GIF = "org/netbeans/modules/websvc/design/resources/class.gif";
    
    private transient JaxWsJavaEditorSupport jes;    
    private transient MultiViewSupport mvc;
    private transient ProjectService service;
    
    public JaxWsDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        getCookieSet().assign( SaveAsCapable.class, new SaveAsCapable() {
            @Override
            public void saveAs( FileObject folder, String fileName ) throws IOException {
                createEditorSupport().saveAs( folder, fileName );
            }
        });
        getCookieSet().add(JaxWsJavaEditorSupport.class, new CookieSet.Factory() {
            @Override
            public <T extends Cookie> T createCookie(Class<T> klass) {
                return klass.cast(createEditorSupport());
            }
        });
        getCookieSet().add(MultiViewSupport.class, new CookieSet.Factory() {
            @Override
            public <T extends Cookie> T createCookie(Class<T> klass) {
                Cookie cake = createMultiViewCookie ();
                if (cake != null) {
                    return klass.cast(cake);
                } else {
                    return null;
                }
            }
        });
    }
    
    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
    
    private void lazyInitialize() {
        if(service==null) {
            service = Utils.getProjectService(this);
        }
    }

    public @Override Node createNodeDelegate() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                lazyInitialize();
            }
        });
        return new JaxWsDataNode(this);
    }

    @Override
    protected void handleDelete() throws java.io.IOException {
        super.handleDelete();
        if ( service != null ){
            service.cleanup();
        }
    }

    @Override
    protected DataObject handleCopyRename(DataFolder df, String name, String ext) throws IOException {
        FileObject fo = getPrimaryEntry ().copyRename (df.getPrimaryFile (), name, ext);
        DataObject dob = DataObject.find( fo );
        //TODO invoke refactoring here (if needed)
        return dob;
    }
    
    /* Getter for rename action.
    * @return true if the object can be renamed
    */
    @Override
    public boolean isRenameAllowed () {
        return false;
    }
    
    private synchronized JaxWsJavaEditorSupport createEditorSupport() {
        if (jes == null) {
            jes = new JaxWsJavaEditorSupport (this);
        }
        return jes;
    }            
    
    private synchronized MultiViewSupport createMultiViewCookie() {
        lazyInitialize();
        if (mvc == null) {
            createEditorSupport();
            if(getPrimaryFile().getAttribute("jax-ws-service-provider")==null) {
                mvc = new MultiViewSupport(service, this);
            }
        }
        return mvc;
    }            
    
    static class JaxWsDataNode extends DataNode {

        public JaxWsDataNode(DataObject dobj) {
            super(dobj, Children.LEAF);
            setIconBaseWithExtension(CLASS_GIF);
        }

        @Override
        public <T extends Cookie> T getCookie(Class<T> type) {
            //preferred action - open in source mode
            if (type.isAssignableFrom(OpenCookie.class)) {
                return type.cast(((JaxWsDataObject)getDataObject()).createEditorSupport());
            }
            return super.getCookie(type);
        }

    }

    public static final class JaxWsJavaEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie, LineCookie, PrintCookie, EditorCookie.Observable {
        
        private static final class Environment extends DataEditorSupport.Env {
            
            private static final long serialVersionUID = -1;
            
            private transient SaveSupport saveCookie = null;
            
            private final class SaveSupport implements SaveCookie {
                @Override
                public void save() throws java.io.IOException {
                    ((JaxWsJavaEditorSupport)findCloneableOpenSupport()).saveDocument();
                }
            }
            
            public Environment(JaxWsDataObject obj) {
                super(obj);
            }
            
            @Override
            protected FileObject getFile() {
                return this.getDataObject().getPrimaryFile();
            }
            
            @Override
            protected FileLock takeLock() throws java.io.IOException {
                return ((MultiDataObject)this.getDataObject()).getPrimaryEntry().takeLock();
            }
            
            public @Override CloneableOpenSupport findCloneableOpenSupport() {
                return (CloneableEditorSupport) ((JaxWsDataObject)this.getDataObject()).getCookie(EditorCookie.class);
            }
            
            
            public void addSaveCookie() {
                JaxWsDataObject javaData = (JaxWsDataObject) this.getDataObject();
                if (javaData.getCookie(SaveCookie.class) == null) {
                    if (this.saveCookie == null) {
                        this.saveCookie = new SaveSupport();
                    }
                    javaData.getCookieSet().add(this.saveCookie);
                    javaData.setModified(true);
                }
            }
            
            public void removeSaveCookie() {
                JaxWsDataObject javaData = (JaxWsDataObject) this.getDataObject();
                if (javaData.getCookie(SaveCookie.class) != null) {
                    javaData.getCookieSet().remove(this.saveCookie);
                    javaData.setModified(false);
                }
            }
        }
        
        public JaxWsJavaEditorSupport(JaxWsDataObject dataObject) {
            super(dataObject, new Environment(dataObject));
            setMIMEType(JaxWsDataLoader.JAVA_MIME_TYPE); // NOI18N
        }
        
        @Override 
        protected boolean notifyModified() {
            if (!super.notifyModified()) {
                return false;
            }
            ((Environment)this.env).addSaveCookie();
            return true;
        }
        
        @Override 
        protected void notifyUnmodified() {
            super.notifyUnmodified();
            ((Environment)this.env).removeSaveCookie();
        }

        @Override
        protected Pane createPane() {
            MultiViewSupport mvs = ((JaxWsDataObject) getDataObject()).getCookie(MultiViewSupport.class);
            if (mvs == null) return super.createPane();
            return (Pane) mvs.createMultiView();
        }
    
        @Override 
        public boolean close(boolean ask) {
            return super.close(ask);
        }

        @Override
        protected boolean asynchronousOpen() {
            return true;
        }
    }
    
   /** Factory for <code>JspI18nSupport</code>. */
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.i18n.I18nSupport.Factory.class)
    public static class Factory extends I18nSupport.Factory {
        
        /** Implements superclass abstract method. */
        @Override
        public I18nSupport createI18nSupport(DataObject dataObject) {
            return new JavaI18nSupport(dataObject);
        }
        
        /** Gets class of supported <code>DataObject</code>.
         * @return <code>JspDataObject</code> class or <code>null</code> 
         * if jsp module is not available */
        @Override
        public Class getDataObjectClass() {
            // XXX Cleaner should be this code dependend on java module
            // -> I18n API needed.
            try {
                return Class.forName(
                    "org.netbeans.modules.websvc.design.loader.JaxWsDataObject", // NOI18N
                    false,
                    Lookup.getDefault().lookup(ClassLoader.class)
                );
            } catch(ClassNotFoundException cnfe) {
                return null;
            }
        }

    } // End of class Factory.
}

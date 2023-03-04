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
package org.netbeans.modules.jshell.env;

import org.netbeans.modules.jshell.editor.ConsoleEditor;
import java.io.IOException;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.windows.CloneableOpenSupport;

/**
 *
 * @author sdedic
 */
@NbBundle.Messages({
    "JShellResolver=Java Shell Cosnole"
})
@MIMEResolver.ExtensionRegistration(
    position=222,
    displayName="#JShellResolver",
    extension="jsh",
    mimeType="text/x-repl",
    showInFileChooser = ""
)    
class JShellDataObject extends MultiDataObject {
    
    public JShellDataObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super(fo, loader);
        CookieSet cks = getCookieSet();
        cks.add(new Class[] {
                OpenCookie.class,
                EditorCookie.Observable.class,
                CloseCookie.class,
                LineCookie.class,
                SimpleES.class,
            }, new CookieSet.Factory() {
            private CloneableEditorSupport supp;
            public <T extends Node.Cookie> T createCookie(Class<T> klass) {
                if (supp != null) {
                    return klass.cast(supp);
                }
                return klass.cast(
                        /*
                        supp = DataEditorSupport.create(JShellDataObject.this, 
                                getPrimaryEntry(), getCookieSet(), 
                                () -> createPane())
                        );*/
                        supp = new SimpleES(JShellDataObject.this, getPrimaryEntry())
                );
            }
        });
    }
    
    private CloneableEditorSupport.Pane createPane0() {
        CloneableEditorSupport cls = (CloneableEditorSupport)getLookup().lookup(EditorCookie.class);
        CloneableEditor cle = new ConsoleEditor(cls, getLookup());
        return cle;
    }

    @Override
    public void setModified(boolean modif) {
        // just ignore...
    }
    
    private static class ESEnv extends DataEditorSupport.Env {
        private MultiDataObject.Entry entry;
        
        /** Constructor. */
        public ESEnv(DataObject obj, MultiDataObject.Entry entry) {
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
            return getDataObject().getCookie(SimpleES.class);
        }
    }
    
    public void reload() throws IOException {
        FileLock fl = getPrimaryFile().lock();
        try {
            getPrimaryFile().getOutputStream(fl).close();
        } finally {
            fl.releaseLock();
        }
        SimpleES es = getLookup().lookup(SimpleES.class);
        if (es != null) {
            es.reloadDocument();
        }
    }
    
    public final class SimpleES extends DataEditorSupport 
    implements OpenCookie, EditCookie, EditorCookie.Observable, 
    PrintCookie, CloseCookie, SaveAsCapable, LineCookie {
        
        /** Constructor. 
         * @param obj data object to work on
         * @param set set to add/remove save cookie from
         */
        public SimpleES (DataObject obj, MultiDataObject.Entry entry) {
            super(obj, obj.getLookup(), new ESEnv(obj, entry));
        }

        @Override
        protected boolean asynchronousOpen() {
            return true;
        }

        @Override
        protected CloneableEditorSupport.Pane createPane() {
            return createPane0();
        }

        protected boolean notifyModified () {
            return true;
        }

        protected void notifyUnmodified () {
        }

        @Override
        protected String messageHtmlName() {
            JShellEnvironment env = ShellRegistry.get().get(getPrimaryFile());
            if (env == null) {
                return super.messageHtmlName();
            }
            return env.getDisplayName();
        }

        @Override
        protected String messageName() {
            return super.messageName(); //To change body of generated methods, choose Tools | Templates.
        }
        
        public Task reloadDocument() {
            return super.reloadDocument();
        }
    }
}

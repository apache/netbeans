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

package org.netbeans.modules.csl.core;

import java.io.IOException;
import java.util.Collection;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.actions.Editable;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.csl.api.GsfLanguage;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.windows.CloneableOpenSupport;


public class GsfDataObject extends MultiDataObject {
    
    /** Used temporarily during file creation */
    private static Language templateLanguage;

    private GenericEditorSupport jes;
    private final Language language;
    
    public GsfDataObject(FileObject pf, MultiFileLoader loader, Language language) throws DataObjectExistsException {
        super(pf, loader);

        // If the user creates a file with a filename where we can't figure out the language
        // (e.g. the PHP New File wizard doesn't enforce a file extension, so if you create
        // a file named "pie.class" (issue 124044) the data loader doesn't know which language
        // to associate this with since it isn't a GSF file extension or mimetype). However
        // during template creation we know the language anyway so we can use it. On subsequent
        // IDE restarts the file won't be recognized so the user will have to rename or
        // add a new file extension to file type mapping.
        if (language == null) {
            language = templateLanguage;
        }
        this.language = language;
        getCookieSet().add(new Class[]{
                GenericEditorSupport.class, // NOI18N
                SaveAsCapable.class, Openable.class, EditorCookie.Observable.class, 
                PrintCookie.class, CloseCookie.class, Editable.class, LineCookie.class,
                DataEditorSupport.class, CloneableEditorSupport.class,
                CloneableOpenSupport.class
            }, new EditorSupportFactory());
    }
    
    public @Override Node createNodeDelegate() {
        return new GsfDataNode(this, language);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    public void setModified(boolean modif) {
        super.setModified(modif);
        if (!isModified()) {
            GenericEditorSupport ges = getLookup().lookup(GenericEditorSupport.class);
            // defect #203688, probably file deletion in parallel with DO's creation - not completed, so cookie not registered yet.
            if (ges != null) {
                ges.removeSaveCookie();
            }
        }
    }
    
    

    public @Override <T extends Cookie> T getCookie(Class<T> type) {
        return getCookieSet().getCookie(type);
    }

    @Override
    protected DataObject handleCopyRename(DataFolder df, String name, String ext) throws IOException {
        FileObject fo = getPrimaryEntry ().copyRename (df.getPrimaryFile (), name, ext);
        DataObject dob = DataObject.find( fo );
        //TODO invoke refactoring here (if needed)
        return dob;
    }

    protected @Override DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {
        if (name == null && language != null && language.getGsfLanguage().getPreferredExtension() != null) {
            // special case: name is null (unspecified or from one-parameter createFromTemplate)
            name = FileUtil.findFreeFileName(df.getPrimaryFile(),
                getPrimaryFile().getName(), language.getGsfLanguage().getPreferredExtension());
        } 
//        else if (!language.getGsfLanguage().isIdentifierChar(c) Utilities.isJavaIdentifier(name)) {
//            throw new IOException (NbBundle.getMessage(GsfDataObject.class, "FMT_Not_Valid_FileName", language.getDisplayName(), name));
//        }
        //IndentFileEntry entry = (IndentFileEntry)getPrimaryEntry();
        //entry.initializeIndentEngine();
        try {
            templateLanguage = language;
            DataObject retValue = super.handleCreateFromTemplate(df, name);
            FileObject fo = retValue.getPrimaryFile ();
            assert fo != null;
//        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//        String pkgName;
//        if (cp != null) {
//            pkgName = cp.getResourceName(fo.getParent(),'.',false);
//        }
//        else {
//            pkgName = "";   //NOI18N
//        }
//        renameJDO (retValue, pkgName, name, this.getPrimaryFile().getName());
            return retValue;
        } finally {
            templateLanguage = null;
        }
    }            
    
    
    private synchronized GenericEditorSupport createEditorSupport () {
        if (jes == null) {
            jes = new GenericEditorSupport(this, language);
        }
        return jes;
    }            
    
    public final class EditorSupportFactory implements CookieSet.Factory {
        @Override
        public <T extends Cookie> T createCookie(Class<T> klass) {
                if (
                klass.isAssignableFrom(DataEditorSupport.class) || 
                DataEditorSupport.class.isAssignableFrom(klass) || 
                klass.isAssignableFrom(Openable.class) || 
                klass.isAssignableFrom(Editable.class) || 
                klass.isAssignableFrom(EditorCookie.Observable.class) || 
                klass.isAssignableFrom(PrintCookie.class) || 
                klass.isAssignableFrom(CloseCookie.class) || 
                klass.isAssignableFrom(LineCookie.class)
            ) {
                return klass.cast(createEditorSupport());
            }
            return null;
        }
    }
    
    public static final class GenericEditorSupport extends DataEditorSupport 
    implements OpenCookie, EditCookie, EditorCookie, PrintCookie, 
               EditorCookie.Observable, SaveAsCapable, LineCookie,
               CloseCookie {

        @Override
        protected boolean asynchronousOpen() {
            return true;
        }
        
        private static class Environment extends DataEditorSupport.Env {
            
            private static final long serialVersionUID = -1;
            
            private transient SaveSupport saveCookie = null;
            
            private class SaveSupport implements SaveCookie {
                @Override
                public void save() throws java.io.IOException {
                    ((GenericEditorSupport)findCloneableOpenSupport()).saveDocument();
// DataObject.setModified() already called as part of saveDocument(). The save action is now asynchronous
// in the IDE and super.saveDocument() checks for possible extra document modifications performed during save
// and sets the DO.modified flag accordingly. Explicit call to DO.setModified() in this case may lead to data loss.
//                    getDataObject().setModified(false);
                }

                /**
                 * Human descriptive, localized name of the savable. It is
                 * advised that all implementations of Savable override the
                 * toString method to provide human readable name.
                 *
                 * @return human readable name representing the savable
                 */
                @Override
                public String toString() {
                    return getFile().getNameExt();
                }
            }
            
            public Environment(GsfDataObject obj) {
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
                return (CloneableEditorSupport) this.getDataObject().getLookup().lookup(CloneableEditorSupport.class);
            }
            
            
            public void addSaveCookie() {
                GsfDataObject javaData = (GsfDataObject) this.getDataObject();
                if (javaData.getCookie(SaveCookie.class) == null) {
                    if (this.saveCookie == null) {
                        this.saveCookie = new SaveSupport();
                    }
                    javaData.getCookieSet().add(this.saveCookie);
                    javaData.setModified(true);
                }
            }
            
            public void removeSaveCookie() {
                GsfDataObject javaData = (GsfDataObject) this.getDataObject();
                if (javaData.getCookie(SaveCookie.class) != null) {
                    javaData.getCookieSet().remove(this.saveCookie);
                    javaData.setModified(false);
                }
            }
        }

        private Language language;

        public GenericEditorSupport(GsfDataObject dataObject, Language language) {
            super(dataObject, null, new Environment(dataObject));
            // support complex MIME types:
            String mime = dataObject.getPrimaryFile().getMIMEType();
            Collection<Language> lngs = LanguageRegistry.getInstance().getApplicableLanguages(mime);
            if (lngs.contains(language)) {
                setMIMEType(mime);
            } else {
                setMIMEType(language.getMimeType());
            }
            this.language = language;
        }
        
        @Override
        protected Pane createPane() {
            if(language.useMultiview()) {
                return (CloneableEditorSupport.Pane) MultiViews.createCloneableMultiView(language.getMimeType(), getDataObject());
            } else {
                return super.createPane();
            }
        }
        
        protected @Override boolean notifyModified() {
            if (!super.notifyModified()) {
                return false;
            }
            ((Environment)this.env).addSaveCookie();
            return true;
        }
        
        
        protected @Override void notifyUnmodified() {
            super.notifyUnmodified();
            removeSaveCookie();
        }

        final void removeSaveCookie() {
            ((Environment)this.env).removeSaveCookie();
        }

//        protected @Override CloneableEditor createCloneableEditor() {
//            return new GsfEditor(this);
//        }
        
        public @Override boolean close(boolean ask) {
            return super.close(ask);
        }

        @Override
        protected EditorKit createEditorKit() {
            EditorKit kit = super.createEditorKit(); 
            if (kit instanceof CslEditorKit) {
                CslEditorKit csKit = (CslEditorKit)kit;
                csKit.applyContentType(getDataObject().getPrimaryFile().getMIMEType());
            }
            return kit;
        }

        @Override
        protected StyledDocument createStyledDocument (EditorKit kit) {
            StyledDocument doc = super.createStyledDocument(kit);
            // Enter the file object in to InputAtrributes. It can be used by lexer.
            InputAttributes attributes = new InputAttributes();
            FileObject fileObject = NbEditorUtilities.getFileObject(doc);
            final GsfLanguage lng = language.getGsfLanguage();
            if (lng != null) {
                attributes.setValue(lng.getLexerLanguage(), FileObject.class, fileObject, false);
            }
            doc.putProperty(InputAttributes.class, attributes);
            return doc;
        }

    }
}

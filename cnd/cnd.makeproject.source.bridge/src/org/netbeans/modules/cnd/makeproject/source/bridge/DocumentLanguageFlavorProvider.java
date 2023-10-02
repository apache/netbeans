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
package org.netbeans.modules.cnd.makeproject.source.bridge;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.Filter;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.project.CodeAssistance;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItem.LanguageFlavor;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsAdapter;
import org.netbeans.modules.cnd.api.project.NativeProjectSupport;
import org.netbeans.modules.cnd.spi.CndDocumentCodeStyleProvider;
import org.netbeans.modules.cnd.source.spi.CndSourcePropertiesProvider;
import org.netbeans.modules.cnd.utils.CndLanguageStandards;
import org.netbeans.modules.cnd.utils.CndLanguageStandards.CndLanguageStandard;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.lexer.MutableTextInput;
import org.netbeans.spi.lexer.TokenHierarchyControl;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.lookup.ServiceProvider;

/**
 * bridge which affects editor behavior based on options from makeproject.
 */
@ServiceProvider(path=CndSourcePropertiesProvider.REGISTRATION_PATH, service=CndSourcePropertiesProvider.class, position=1000)
public final class DocumentLanguageFlavorProvider implements CndSourcePropertiesProvider {
    private static final boolean TRACE = CndUtils.getBoolean("cnd.doc.flavor.trace", false); // NOI18N

    @Override
    public void addProperty(DataObject dob, StyledDocument doc) {
        ListenerImpl old = (ListenerImpl) doc.getProperty(ListenerImpl.class);
        if (old != null) {
            old.unregister();
        }
        // check if it should have C++11 flavor
        Language<?> language = (Language<?>) doc.getProperty(Language.class);
        if (language != CppTokenId.languageCpp() && language != CppTokenId.languageC() && language != CppTokenId.languageHeader()) {
            return;
        }
        FileObject primaryFile = dob.getPrimaryFile();
        // fast check using NativeFileItemSet
        NativeFileItemSet nfis = dob.getLookup().lookup(NativeFileItemSet.class);
        if (nfis != null) {
            for (NativeFileItem nativeFileItem : nfis.getItems()) {
                doc.putProperty(ListenerImpl.class, new ListenerImpl(doc, dob, nativeFileItem));
                setLanguage(nativeFileItem, doc);
                rebuildTH(doc);
                return;
            }
            if (primaryFile != null) {
                doc.putProperty(ListenerImpl.class, new ListenerImpl(doc, dob, primaryFile, nfis));
                setLanguage(primaryFile, doc);
                rebuildTH(doc);                
                return;
            }
        }
        if (primaryFile == null) {
            return;
        }
        if (setLanguage(primaryFile, doc)) {
            rebuildTH(doc);
            return;
        }
        Project owner = FileOwnerQuery.getOwner(primaryFile);
        if (owner == null) {
            return;
        }
        NativeProject np = owner.getLookup().lookup(NativeProject.class);
        if (np == null) {
            setDefaltLanguageFlavor(doc);
            return;
        }
        NativeFileItem nfi = np.findFileItem(primaryFile);
        if (nfi == null) {
            CndDocumentCodeStyleProvider cs = owner.getLookup().lookup(CndDocumentCodeStyleProvider.class);
            if (cs != null) {
                doc.putProperty(CndDocumentCodeStyleProvider.class, cs);
            }            
            setDefaltLanguageFlavor(doc);
            return;
        }
        doc.putProperty(ListenerImpl.class, new ListenerImpl(doc, dob, nfi));
        setLanguage(nfi, doc);
        rebuildTH(doc);
    }

    private void setDefaltLanguageFlavor(StyledDocument doc) {
        // stand-alone file without context
        String mime = (String) doc.getProperty("mimeType"); //NOI18N
        MIMEExtensions ee = MIMEExtensions.get(mime);
        if (ee == null) {
            return;
        }
        CndLanguageStandards.CndLanguageStandard defaultStandard = ee.getDefaultStandard();
        if (defaultStandard != null) {
            NativeFileItem.Language lang;
            switch (defaultStandard) {
                case C89:
                case C99:
                case C11:
                case C17:
                case C23:
                    if (MIMENames.isHeader(mime)) {
                        lang = NativeFileItem.Language.C_HEADER;
                    } else {
                        lang = NativeFileItem.Language.C;
                    }
                case CPP98:
                case CPP11:
                case CPP14:
                case CPP17:
                case CPP20:
                case CPP23:
                    if (MIMENames.isHeader(mime)) {
                        lang = NativeFileItem.Language.C_HEADER;
                    } else {
                        lang = NativeFileItem.Language.CPP;
                    }                    
                    break;
                default:
                    return;
            }
            NativeFileItem.LanguageFlavor flavor = NativeProjectSupport.cndStandardToItemFlavor(defaultStandard);
            tryToSetDocumentLanguage(lang, flavor, null, doc);
            rebuildTH(doc);
        }
        return;
    }
    
    private static boolean setLanguage(FileObject fo, StyledDocument doc) {
        CodeAssistance CAProvider = Lookup.getDefault().lookup(CodeAssistance.class);
        if (CAProvider != null) {
            Pair<NativeFileItem.Language, LanguageFlavor> pair = CAProvider.getHeaderLanguageFlavour(fo);
            return tryToSetDocumentLanguage(pair.first(), pair.second(), null, doc);
        } else {
            return false;
        }
    }
    
    private static boolean setLanguage(NativeFileItem nfi, StyledDocument doc) {
        final NativeProject nativeProject = nfi.getNativeProject();
        if (nativeProject != null) {
            final Lookup.Provider project = nativeProject.getProject();
            if (project != null) {
                Lookup lookup = project.getLookup();
                CndDocumentCodeStyleProvider cs = lookup.lookup(CndDocumentCodeStyleProvider.class);
                if (cs != null) {
                    doc.putProperty(CndDocumentCodeStyleProvider.class, cs);
                }
            }
        }
        return tryToSetDocumentLanguage(null, null, nfi, doc);
    }

    private static boolean tryToSetDocumentLanguage(NativeFileItem.Language itemLang, LanguageFlavor flavor, NativeFileItem nfi, StyledDocument doc) {
        if (itemLang == null) {
            itemLang = nfi.getLanguage();
        }
        if (flavor == null && nfi != null) {
            flavor = getLanguageFlavor(nfi);
        }
        Filter<?> filter = null;
        Language<?> language = null;        
        switch (itemLang) {
            case C:
                language = CppTokenId.languageC();
                break;
            case C_HEADER:
                language = CppTokenId.languageHeader();                
                break;
            case CPP:
                language = CppTokenId.languageCpp();
                break;
            case FORTRAN:
            case OTHER:
                return false;
        }
        assert language != null;
        CndLanguageStandard preferredStd = NativeProjectSupport.itemFlavorToCndStandard(flavor);
        filter = CndLexerUtilities.getFilter(language, preferredStd, doc);
        assert filter != null;
        doc.putProperty(Language.class, language);
        InputAttributes lexerAttrs = (InputAttributes) doc.getProperty(InputAttributes.class);
        if (TRACE) {
            System.err.println("DocumentLanguageFlavorProvider:" + doc + ":\n\t{" + itemLang + "} : Change Filter:" +  // NOI18N
                    lexerAttrs.getValue(LanguagePath.get(language), CndLexerUtilities.LEXER_FILTER) +
                    "=>" + filter + "; flavor=" + flavor); // NOI18N
        }
        lexerAttrs.setValue(language, CndLexerUtilities.LEXER_FILTER, filter, true);
        return true;
    }

    private static LanguageFlavor getLanguageFlavor(NativeFileItem nfi) {
        LanguageFlavor flavor = nfi.getLanguageFlavor();
        if (flavor == LanguageFlavor.UNKNOWN) {
            // Ask flavor of first start compilation unit.
            CodeAssistance CAProvider = Lookup.getDefault().lookup(CodeAssistance.class);
            if (CAProvider != null) {
                flavor = CAProvider.getStartFileLanguageFlavour(nfi).second();
            }
        }    
        return flavor;
    }

    private static void rebuildTH(final StyledDocument doc) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                BaseDocument bdoc = (BaseDocument) doc;
                try {
                    if (bdoc != null) {
                        bdoc.extWriteLock();
                        MutableTextInput mti = (MutableTextInput) bdoc.getProperty(MutableTextInput.class);
                        if (mti != null) {
                            TokenHierarchyControl thc = mti.tokenHierarchyControl();
                            if (thc != null) {
                                thc.rebuild();
                            }
                        }
                    }
                } finally {
                    if (bdoc != null) {
                        bdoc.extWriteUnlock();
                    }
                }
            }
        });
    }

    private final static class ListenerImpl extends NativeProjectItemsAdapter implements PropertyChangeListener {
        private final Reference<StyledDocument> docRef;
        private final String path;
        private final FileObject fo;
        private Reference<NativeProject> prjRef;
        private Reference<NativeFileItemSet> nfisRef;
        private LanguageFlavor languageFlavor;

        public ListenerImpl(StyledDocument doc, DataObject dob, NativeFileItem nativeFileItem) {
            this.docRef = new WeakReference<StyledDocument>(doc);
            this.fo = dob.getPrimaryFile();
            this.path = nativeFileItem.getAbsolutePath();
            NativeProject nativeProject = nativeFileItem.getNativeProject();
            this.prjRef = new WeakReference<NativeProject>(nativeProject);
            this.nfisRef = new WeakReference<NativeFileItemSet>(null);
            this.languageFlavor = getLanguageFlavor(nativeFileItem);
            if (nativeProject != null) {
                nativeProject.addProjectItemsListener(ListenerImpl.this);
            } else {
                System.err.println("no native project for " + nativeFileItem); 
            }
            EditorRegistry.addPropertyChangeListener(ListenerImpl.this);
            if (TRACE) System.err.println("DocumentLanguageFlavorProvider:" + path + " created Listener " + System.identityHashCode(ListenerImpl.this));
        }

        public ListenerImpl(StyledDocument doc, DataObject dob, FileObject primaryFile, NativeFileItemSet nfis) {
            this.docRef = new WeakReference<StyledDocument>(doc);
            this.fo = primaryFile;
            this.path = primaryFile.getPath();
            this.prjRef = new WeakReference<NativeProject>(null);
            this.nfisRef = new WeakReference<NativeFileItemSet>(nfis);
            EditorRegistry.addPropertyChangeListener(ListenerImpl.this);
            nfis.addPropertyChangeListener(ListenerImpl.this);
            if (TRACE) System.err.println("DocumentLanguageFlavorProvider:" + path + " created Listener " + System.identityHashCode(ListenerImpl.this));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (TRACE) System.err.println("DocumentLanguageFlavorProvider:" + path + " propertyChange Listener " + System.identityHashCode(this) + ": evt=" + evt);
            StyledDocument doc = docRef.get();
            NativeProject project = prjRef.get();
            NativeFileItemSet nfis = nfisRef.get();
            if (doc == null || (project == null && nfis == null)) {
                unregister();
                return;
            }
            if ("usedByCloneableEditor".equals(evt.getPropertyName())) { // NOI18N
                if (Boolean.FALSE.equals(evt.getNewValue())) {
                    unregister();
                }
            } else if (EditorRegistry.COMPONENT_REMOVED_PROPERTY.equals(evt.getPropertyName())) {
                JTextComponent oldValue = (JTextComponent) evt.getOldValue();
                if (oldValue != null && doc.equals(oldValue.getDocument())) {
                    unregister();
                }
            } else if (NativeFileItemSet.PROPERTY_ITEMS_CHANGED.equals(evt.getPropertyName())) {
                Collection<NativeFileItem> items = (Collection<NativeFileItem>) evt.getNewValue();
                if (items != null) {
                    for(NativeFileItem item : items) {
                        if (nfis != null) {
                            nfis.removePropertyChangeListener(this);
                        }
                        this.nfisRef = new WeakReference<NativeFileItemSet>(null);
                        NativeProject nativeProject = item.getNativeProject();
                        this.prjRef = new WeakReference<NativeProject>(nativeProject);
                        if (nativeProject != null) {
                            nativeProject.addProjectItemsListener(ListenerImpl.this);
                        } else {
                            System.err.println("no native project for " + item); 
                        }
                        filePropertiesChanged(item);
                        return;
                    }
                }
            }
        }

        private void unregister() {
            if (TRACE) System.err.println("DocumentLanguageFlavorProvider:" + "unregister Listener " + System.identityHashCode(this) + " for " + path);
            EditorRegistry.removePropertyChangeListener(this);
            NativeProject nativeProject = this.prjRef.get();
            if (nativeProject != null) {
                nativeProject.removeProjectItemsListener(this);
            }
            NativeFileItemSet nfis = nfisRef.get();
            if (nfis != null) {
                nfis.removePropertyChangeListener(this);
            }
            StyledDocument doc = docRef.get();
            if (doc != null) {
                doc.putProperty(ListenerImpl.class, null);
            }
        }

        @Override
        public void filesAdded(List<NativeFileItem> fileItems) {
            filesPropertiesChanged(fileItems);
        }

        private void filePropertiesChanged(NativeFileItem fileItem) {
            if (fileItem != null && path.equals(fileItem.getAbsolutePath())) {
                final StyledDocument doc = docRef.get();
                if (doc == null) {
                    unregister();
                    return;
                }
                if (TRACE) System.err.println("DocumentLanguageFlavorProvider:" + path + " Item Listener " + System.identityHashCode(this));
                LanguageFlavor newFlavor = getLanguageFlavor(fileItem);
                if (languageFlavor == null) {
                    if (newFlavor != null) {
                        setLanguage(fileItem, doc);
                        languageFlavor = newFlavor;
                        rebuildTH(doc);
                    }
                } else if (!languageFlavor.equals(newFlavor)) {
                    setLanguage(fileItem, doc);
                    languageFlavor = newFlavor;
                    rebuildTH(doc);
                }
            }
        }

        @Override
        public void filesPropertiesChanged(List<NativeFileItem> fileItems) {
            for (NativeFileItem nativeFileItem : fileItems) {
                filePropertiesChanged(nativeFileItem);
            }
        }

        @Override
        public void filesPropertiesChanged(NativeProject nativeProject) {
            if (nativeProject != null) {
                NativeFileItem findFileItem = nativeProject.findFileItem(fo);
                filePropertiesChanged(findFileItem);
            } else {
                unregister();
            }
        }

        @Override
        public void projectDeleted(NativeProject nativeProject) {
            unregister();
        }
    }
}

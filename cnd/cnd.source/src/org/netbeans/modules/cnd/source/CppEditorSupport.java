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
package org.netbeans.modules.cnd.source;

// This file was initially based on org.netbeans.modules.java.JavaEditor
// (Rev 61)
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.Filter;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.source.spi.CndPaneProvider;
import org.netbeans.modules.cnd.source.spi.CndSourcePropertiesProvider;
import org.netbeans.modules.cnd.support.ReadOnlySupport;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsFactory;
import org.netbeans.spi.editor.guards.GuardedSectionsProvider;
import org.netbeans.spi.lexer.MutableTextInput;
import org.netbeans.spi.lexer.TokenHierarchyControl;
import org.openide.awt.UndoRedo;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableOpenSupport;

/**
 *  C/C++/Fortran source-file extension for handling the Editor.
 *  If we plan to use guarded sections, we'd need to implement that
 *  here. For now, this is used to get toggle-breakpoint behavior.
 */
public class CppEditorSupport extends DataEditorSupport implements EditCookie,
        EditorCookie, EditorCookie.Observable, OpenCookie, CloseCookie, PrintCookie, ReadOnlySupport {

    private static final RequestProcessor RP = new RequestProcessor("slowDocumentPropertiesSetup", 1); // NOI18N
    /** SaveCookie for this support instance. The cookie is adding/removing 
     * data object's cookie set depending on if modification flag was set/unset. */
    private final SaveCookie saveCookie = new SaveCookie() {

        /** Implements <code>SaveCookie</code> interface. */
        @Override
        public void save() throws IOException {
            CppEditorSupport.this.saveDocument();
            //CppEditorSupport.this.getDataObject().setModified(false);
        }

        @Override
        public String toString() {
            return getDataObject().getPrimaryFile().getNameExt();
        }
    };

    private final InstanceContent ic;
    private boolean readonly;

    /**
     *  Create a new Editor support for the given C/C++/Fortran source.
     *  @param entry The (primary) file entry representing the C/C++/f95 source file
     */
    public CppEditorSupport(SourceDataObject obj, Node nodeDelegate) {
        super(obj, null, new Environment(obj));
        this.ic = obj.getInstanceContent();
        if (nodeDelegate != null) {
            this.ic.add(nodeDelegate);
        }
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
    
    /** Helper method. Adds save cookie to the data object. */
    private void addSaveCookie() {
        // Adds save cookie to the data object.
        ic.add(saveCookie);
    }

    /** Helper method. Removes save cookie from the data object. */
    private void removeSaveCookie() {
        // Remove save cookie from the data object.
        ic.remove(saveCookie);
    }

    @Override
    public boolean isReadOnly() {
        return readonly;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readonly = readOnly;
    }

    UndoRedo.Manager getUndoRedoImpl() {
        return super.getUndoRedo();
    }

    @Override
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        DataObject dao = getDataObject();
        FileObject fo = dao.getPrimaryFile();
        boolean resetLS = true;
        if (CndFileUtils.isLocalFileSystem(fo.getFileSystem())) {
            resetLS = false;
        } else {
            InputStream in = fo.getInputStream();
            int ch;
            loop: while ((ch = in.read()) != (-1)) {
                switch (ch) {
                    case '\n':
                    case '\r':
                        resetLS = false;
                        break loop;
                }
            }
            in.close();
        }
        GuardedSectionsProvider guardedProvider = getGuardedSectionsProvider(doc, kit);
        if (guardedProvider == null) {
            super.loadFromStreamToKit(doc, stream, kit);
        } else {
            Charset cs = FileEncodingQuery.getEncoding(fo);
            Reader reader = guardedProvider.createGuardedReader(stream, cs);
            try {
                kit.read(reader, doc, 0);
            } finally {
                reader.close();
            }
        }
        if (resetLS) {
            doc.putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n"); //NOI18N
        }
    }

    @Override
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        GuardedSectionsProvider guardedProvider = getGuardedSectionsProvider(doc, kit);
        if (guardedProvider != null) {
            Charset cs = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
            Writer writer = guardedProvider.createGuardedWriter(stream, cs);
            try {
                kit.write(writer, doc, 0, doc.getLength());
            } finally {
                writer.close();
            }
        } else {
            super.saveFromKitToStream(doc, kit, stream);
        }
    }

    private static final String EXTRA_DOCUMENT_PROPERTIES = "EXTRA_DOCUMENT_PROPERTIES"; // NOI18N
    private StyledDocument setupSlowDocumentProperties(StyledDocument doc) {
        assert !SwingUtilities.isEventDispatchThread();
        if (doc != null && !Boolean.TRUE.equals(doc.getProperty(EXTRA_DOCUMENT_PROPERTIES))) {
            // setup language flavor lexing attributes 
            Language<?> language = (Language<?>) doc.getProperty(Language.class);
            if (language != null) {
                InputAttributes lexerAttrs = (InputAttributes)doc.getProperty(InputAttributes.class);
                if (lexerAttrs == null) {
                    CndUtils.assertUnconditional("no language attributes for " + doc);
                    lexerAttrs = new InputAttributes();
                    doc.putProperty(InputAttributes.class, lexerAttrs);
                }
                Filter<?> filter = CndLexerUtilities.getDefaultFilter(language, doc);
                if (filter != null) {
                    lexerAttrs.setValue(language, CndLexerUtilities.LEXER_FILTER, filter, true);  // NOI18N
                } else {
                    CndUtils.assertUnconditional("no language filter for " + doc + " with language " + language);
                }
            } else {
                String mimeType = DocumentUtilities.getMimeType(doc);
                if (mimeType == null || !MIMENames.isHeaderOrCppOrC(mimeType)) {
                    // #255684 - Exception: no language for
                    // it's not our mime-type (may be deserialization of unknown extension)                    
                    return doc;
                }
                CndUtils.assertUnconditional("no language for " + doc);
            }
            // try to setup document's extra properties during non-EDT load if needed
            PropertiesProviders.addProperty(getDataObject(), doc);
            doc.putProperty(EXTRA_DOCUMENT_PROPERTIES, Boolean.TRUE);
            rebuildDocumentControls(doc);
        }
        return doc;
    }

    private static void rebuildDocumentControls(final StyledDocument doc) {
        if (doc instanceof BaseDocument) {
            // rebuild all controls managing document:
            //  - rebuild token hierarchy
            //  - anything else?
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    BaseDocument bdoc = (BaseDocument) doc;
                    try {
                        if (bdoc != null) {
                            bdoc.extWriteLock();
                            MutableTextInput mti = (MutableTextInput) doc.getProperty(MutableTextInput.class);
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
    }
    
    private final static class PropertiesProviders {

        private final static Collection<? extends CndSourcePropertiesProvider> providers = Lookups.forPath(CndSourcePropertiesProvider.REGISTRATION_PATH).lookupAll(CndSourcePropertiesProvider.class);

        static void addProperty(DataObject dobj, StyledDocument doc) {
            assert !SwingUtilities.isEventDispatchThread();
            for (CndSourcePropertiesProvider provider : providers) {
                provider.addProperty(dobj, doc);
            }
        }
    }

    private static class GuardedEditorSupportImpl implements GuardedEditorSupport {
        private final StyledDocument doc;
        public GuardedEditorSupportImpl(StyledDocument doc) {
            this.doc = doc;
        }
        @Override
        public StyledDocument getDocument() {
            return doc;
        }
    }
    
    private GuardedSectionsProvider getGuardedSectionsProvider(final StyledDocument doc, EditorKit kit) {
        Object o = doc.getProperty(GuardedSectionsProvider.class);
        if (o instanceof GuardedSectionsProvider) {
            return (GuardedSectionsProvider) o;
        }        
        String mimeType = kit.getContentType();
        CndUtils.assertTrueInConsole(mimeType != null, "unexpected null content type"); // NOI18N
        if (mimeType != null) {
            GuardedSectionsFactory gsf = GuardedSectionsFactory.find(mimeType);
            if (gsf != null) {
                GuardedSectionsProvider gsp = gsf.create(new GuardedEditorSupportImpl(doc));
                doc.putProperty(GuardedSectionsProvider.class, gsp);
                return gsp;
            }
        }
        return null;
    }
            
    @Override
    protected String documentID() {
        DataObject dataObject = getDataObject();
        if (dataObject != null && dataObject.isValid()) {
            // Netbeans use primitive names for top components id and use file name for editor top component id
            // So let differ threads TC and editor TC for threads.h
            // But best solution is use full file name for TC id
            return dataObject.getPrimaryFile().getNameExt();
        }
        return ""; // NOI18N
    }

    @Override
    protected Pane createPane() {

        // if there is a CndPaneProvider, us it
        CndPaneProvider paneProvider = Lookup.getDefault().lookup(CndPaneProvider.class);
        if (paneProvider != null) {
            Pane pane = paneProvider.createPane(this);
            if (pane != null) {
                return pane;
            }
        }
        return (CloneableEditorSupport.Pane) MultiViews.createCloneableMultiView(getDataObject().getPrimaryFile().getMIMEType(), getDataObject());
    }

    @Override
    public Task prepareDocument() {
        // refer to IZ233191 for the explanation of why this task is posted
        // each time
        return RP.post(new Runnable() {
            @Override
            public void run() {
                CppEditorSupport.super.prepareDocument().waitFinished();
                setupSlowDocumentProperties(CppEditorSupport.super.getDocument());
            }
        });
    }

    /** Nested class. Environment for this support. Extends <code>DataEditorSupport.Env</code> abstract class. */
    private static class Environment extends DataEditorSupport.Env {

        private static final long serialVersionUID = 3035543168452715818L;

        /** Constructor. */
        public Environment(DataObject obj) {
            super(obj);
        }

        /** Implements abstract superclass method. */
        @Override
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        /** Implements abstract superclass method.*/
        @Override
        protected FileLock takeLock() throws IOException {
            ReadOnlySupport readOnly = getDataObject().getLookup().lookup(ReadOnlySupport.class);
            if (readOnly != null && readOnly.isReadOnly()) {
                throw new IOException(); // for read only state method must throw IOException
            } else {
                return ((MultiDataObject) getDataObject()).getPrimaryEntry().takeLock();
            }
        }

        /** 
         * Overrides superclass method.
         * @return text editor support (instance of enclosing class)
         */
        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return getDataObject().getLookup().lookup(CppEditorSupport.class);
        }
    } // End of nested Environment class.    
}

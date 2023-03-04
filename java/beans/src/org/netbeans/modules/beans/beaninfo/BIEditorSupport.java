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

package org.netbeans.modules.beans.beaninfo;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.beans.beaninfo.GenerateBeanInfoAction.BeanInfoWorker;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsFactory;
import org.netbeans.spi.editor.guards.GuardedSectionsProvider;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport.Pane;
import org.openide.text.DataEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jan Pokorsky
 */
public final class BIEditorSupport extends DataEditorSupport
        implements OpenCookie, EditCookie, EditorCookie, PrintCookie, EditorCookie.Observable {
    
    private static final String MV_JAVA_ID = "java"; // NOI18N
    private static final String MV_BEANINFO_ID = "beaninfo"; // NOI18N
    private static final String MIME_BEAN_INFO = "text/x-java-beaninfo"; //NOI18N
    private BIGES guardedEditor;
    private GuardedSectionsProvider guardedProvider;
    private GenerateBeanInfoAction.BeanInfoWorker worker;

    private static final Set<BIEditorSupport> opened = Collections.synchronizedSet(new HashSet<BIEditorSupport>());
    private static Map<FileSystem,FileStatusListener> fsToStatusListener = new HashMap<FileSystem,FileStatusListener>();

    /**
     * The embracing multiview TopComponent (holds the form designer and
     * java editor) - we remeber the last active TopComponent (not all clones)
     */
    private CloneableTopComponent multiviewTC;
    private TopComponentsListener topComponentsListener;

    public BIEditorSupport(DataObject obj, CookieSet cookieSet) {
        super(obj, new Environment(obj, cookieSet));
        setMIMEType("text/x-java"); // NOI18N
    }
    
    public GuardedSectionManager getGuardedSectionManager() {
        try {
            StyledDocument doc = openDocument();
            return GuardedSectionManager.getInstance(doc);
        } catch (IOException ex) {
            throw (IllegalStateException) new IllegalStateException("cannot open document").initCause(ex); // NOI18N
        }
    }
    
    @Override
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit)
            throws IOException, BadLocationException {
        
        if (guardedEditor == null) {
            guardedEditor = new BIGES();
            GuardedSectionsFactory gFactory = GuardedSectionsFactory.find(((DataEditorSupport.Env) env).getMimeType());
            if (gFactory != null) {
                guardedProvider = gFactory.create(guardedEditor);
            }
        }
        
        if (guardedProvider != null) {
            guardedEditor.doc = doc;
            Charset c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
            Reader reader = guardedProvider.createGuardedReader(stream, c);
            try {
                kit.read(reader, doc, 0);
            } finally {
                reader.close();
            }
        } else {
            super.loadFromStreamToKit(doc, stream, kit);
        }
    }

    @Override
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream)
            throws IOException, BadLocationException {
        
        if (guardedProvider != null) {
            Charset c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
            Writer writer = guardedProvider.createGuardedWriter(stream, c);
            try {
                kit.write(writer, doc, 0, doc.getLength());
            } finally {
                writer.close();
            }
        } else {
            super.saveFromKitToStream(doc, kit, stream);
        }
    }

    @Override
    public void saveDocument() throws IOException {
        if (worker != null && worker.isModelModified()) {
            worker.generateSources();
            worker.waitFinished();
        }
        super.saveDocument();
    }
    
    @Override
    protected boolean notifyModified() {
        if (!super.notifyModified())
            return false;
        ((Environment)this.env).addSaveCookie();
        updateMVTCName();
        return true;
    }


    @Override
    protected void notifyUnmodified() {
        super.notifyUnmodified();
        ((Environment)this.env).removeSaveCookie();
        updateMVTCName();
    }

    @Override
    protected void notifyClosed() {
        opened.remove(this);
        if (opened.isEmpty()) {
            detachStatusListeners();
        }
        super.notifyClosed();
        worker = null;
        if (topComponentsListener != null) {
            TopComponent.getRegistry().removePropertyChangeListener(topComponentsListener);
            topComponentsListener = null;
        }
    }

    @Override
    protected Pane createPane() {
        DataObject dobj = getDataObject();
        if (dobj == null || !dobj.isValid()) {
            return super.createPane();
        }
        return (Pane) MultiViews.createCloneableMultiView(MIME_BEAN_INFO, getDataObject());
    }
    
    /** This is called by the multiview elements whenever they are created
     * (and given a observer knowing their multiview TopComponent). It is
     * important during deserialization and clonig the multiview - i.e. during
     * the operations we have no control over. But anytime a multiview is
     * created, this method gets called.
     */
    private void setTopComponent(TopComponent topComp) {
        multiviewTC = (CloneableTopComponent)topComp;
        updateMVTCName();

        if (topComponentsListener == null) {
            topComponentsListener = new TopComponentsListener();
            TopComponent.getRegistry().addPropertyChangeListener(topComponentsListener);
        }
        opened.add(this);
        try {
            addStatusListener(getDataObject().getPrimaryFile().getFileSystem());
        } catch (FileStateInvalidException fsiex) {
            Exceptions.printStackTrace(fsiex);
        }

    }
    
    private void addStatusListener(FileSystem fs) {
        FileStatusListener fsl = fsToStatusListener.get(fs);
        if (fsl == null) {
            fsl = new FileStatusListener() {
                @Override
                public void annotationChanged(FileStatusEvent ev) {
                    synchronized (opened) {
                        Iterator<BIEditorSupport> iter = opened.iterator();
                        while (iter.hasNext()) {
                            BIEditorSupport fes = iter.next();
                            if (ev.hasChanged(fes.getDataObject().getPrimaryFile())) {
                                fes.updateMVTCName();
                            }
                        }
                    }
                }
            };
            fs.addFileStatusListener(fsl);
            fsToStatusListener.put(fs, fsl);
        } // else do nothing - the listener is already added
    }
    
    private static void detachStatusListeners() {
        Iterator<Map.Entry<FileSystem, FileStatusListener>> iter = fsToStatusListener.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<FileSystem, FileStatusListener> entry = iter.next();
            FileSystem fs = entry.getKey();
            FileStatusListener fsl = entry.getValue();
            fs.removeFileStatusListener(fsl);
        }
        fsToStatusListener.clear();
    }
    
    private void updateMVTCName() {
        Runnable task = new Runnable() {
            public void run() {
                updateMVTCNameInAwt();
            }
        };
        
        if (EventQueue.isDispatchThread()) {
            task.run();
        } else {
            EventQueue.invokeLater(task);
        }
    }
    
    private void updateMVTCNameInAwt() {
        CloneableTopComponent topComp = multiviewTC;
        if (topComp != null) {
            String htmlname = messageHtmlName();
            String name = messageName();
            String tip = messageToolTip();
            for (CloneableTopComponent o : NbCollections.
                    iterable(topComp.getReference().getComponents())) {
                
                topComp.setHtmlDisplayName(htmlname);
                topComp.setDisplayName(name);
                topComp.setName(name);
                topComp.setToolTipText(tip);
            }
        }
    }
    
    static boolean isLastView(TopComponent tc) {
        if (!(tc instanceof CloneableTopComponent))
            return false;
        
        boolean oneOrLess = true;
        Enumeration<CloneableTopComponent> en = ((CloneableTopComponent)tc).getReference().getComponents();
        if (en.hasMoreElements()) {
            en.nextElement();
            if (en.hasMoreElements())
                oneOrLess = false;
        }
        return oneOrLess;
    }
    
    static BIEditorSupport findEditor(DataObject dobj) {
        return dobj.getLookup().lookup(BIEditorSupport.class);
    }
    
    final CloseOperationState canCloseElement(TopComponent tc) {
        // if this is not the last cloned java editor component, closing is OK
        if (!isLastView(tc)) {
            return CloseOperationState.STATE_OK;
        }

        if (!isModified()) {
            return CloseOperationState.STATE_OK;
        }

        AbstractAction save = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    saveDocument();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        save.putValue(Action.LONG_DESCRIPTION, NbBundle.getMessage(BIEditorSupport.class, "MSG_MODIFIED", getDataObject().getPrimaryFile().getNameExt()));

        // return a placeholder state - to be sure our CloseHandler is called
        return MultiViewFactory.createUnsafeCloseState(
                "ID_BEANINFO_CLOSING", // NOI18N
                save,
                MultiViewFactory.NOOP_CLOSE_ACTION);
    }
    
    
    @MultiViewElement.Registration(displayName = "#LAB_JavaSourceView",
        iconBase = JavaTemplates.JAVA_ICON,
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = MV_JAVA_ID,
        mimeType = MIME_BEAN_INFO,
        position = 10)
 
    public static final class JavaElement extends CloneableEditor implements MultiViewElement, Externalizable {
        
        private static final long serialVersionUID = 1L;
        private MultiViewElementCallback callback;

        public JavaElement(Lookup context) {
            super(context.lookup(DataEditorSupport.class));
            DataObject dataObject = context.lookup(DataObject.class);
            setActivatedNodes(new Node[]{dataObject.getNodeDelegate()});
        }

        /**
         * serialization stuff; do not use
         */
        private JavaElement() {
            super();
        }

        public JComponent getVisualRepresentation() {
            return this;
        }

        public JComponent getToolbarRepresentation() {
            JComponent toolbar = null;
            JEditorPane jepane = getEditorPane();
            if (jepane != null) {
                Document doc = jepane.getDocument();
                if (doc instanceof NbDocument.CustomToolbar) {
                    toolbar = ((NbDocument.CustomToolbar)doc).createToolbar(jepane);
                }
            }
            return toolbar;
        }

        public void setMultiViewCallback(MultiViewElementCallback callback) {
            this.callback = callback;
            BIEditorSupport editor = (BIEditorSupport) cloneableEditorSupport();
            editor.setTopComponent(callback.getTopComponent());
        }

        public CloseOperationState canCloseElement() {
            DataObject dataObject = callback.getTopComponent().getLookup().lookup(DataObject.class);
            BIEditorSupport editor = (BIEditorSupport) cloneableEditorSupport();
            return editor.canCloseElement(callback.getTopComponent());
        }

        @Override
        public void componentActivated() {
            super.componentActivated();
            BIEditorSupport editor = (BIEditorSupport) cloneableEditorSupport();
            if (editor.worker != null && editor.worker.isModelModified()) {
                editor.worker.generateSources();
            }
        }

        @Override
        public void componentDeactivated() {
            super.componentDeactivated();
        }

        @Override
        public void componentHidden() {
            super.componentHidden();
        }

        @Override
        public void componentShowing() {
            super.componentShowing();
        }

        @Override
        public void componentClosed() {
            // XXX copied from form module see issue 55818
            super.canClose(null, true);
            super.componentClosed();
        }
        
        @Override
        protected boolean closeLast() {
            return true;
        }

        @Override
        public void componentOpened() {
            super.componentOpened();
        }

        @Override
        public void updateName() {
            super.updateName();
            if (callback != null) {
                callback.updateTitle(getDisplayName());
            }
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal(out);
        }
        
    }
            
    @MultiViewElement.Registration (
        displayName="#LAB_BeanInfoEditorView",
        iconBase=JavaTemplates.JAVA_ICON,
        persistenceType=TopComponent.PERSISTENCE_NEVER,
        preferredID=MV_BEANINFO_ID,
        mimeType=MIME_BEAN_INFO,
        position=1000
    )
    public static final class BeanInfoElement extends CloneableTopComponent implements MultiViewElement, Externalizable {
        
        private static final long serialVersionUID = 1L;
        private MultiViewElementCallback callback;
        private DataObject dataObject;
        private boolean isInitialized = false;
        private final JPanel emptyToolbar = new JPanel();
        private BiPanel biPanel;
        
        public BeanInfoElement(Lookup lookup) {
            this.dataObject = lookup.lookup(DataObject.class);
            setActivatedNodes(new Node[]{dataObject.getNodeDelegate()});
        }

        /**
         * serialization stuff; do not use
         */
        private BeanInfoElement() {
        }

        public JComponent getVisualRepresentation() {
            return this;
        }
        
        public JComponent getToolbarRepresentation() {
            return emptyToolbar;
        }

        public void setMultiViewCallback(MultiViewElementCallback callback) {
            this.callback = callback;
            BIEditorSupport editor = findEditor(dataObject);
            editor.setTopComponent(callback.getTopComponent());
        }

        @Override
        public CloseOperationState canCloseElement() {
            BIEditorSupport editor = findEditor(dataObject);
            return editor.canCloseElement(callback.getTopComponent());
        }

        @Override
        public void componentActivated() {
            super.componentActivated();
        }

        @Override
        public void componentDeactivated() {
            super.componentDeactivated();
        }

        @Override
        public void componentHidden() {
            super.componentHidden();
        }

        @Override
        public void componentShowing() {
            super.componentShowing();
            initialize();
        }

        @Override
        public void componentClosed() {
            super.componentClosed();
        }

        @Override
        public void componentOpened() {
            super.componentOpened();
        }
        
        private void initialize() {
            if (!isInitialized) {
                setLayout(new BorderLayout());
                biPanel = new BiPanel();
                add(biPanel, BorderLayout.CENTER);
                isInitialized = true;
            } else {
                biPanel.setContext(new BiNode.Wait());
            }
            
            FileObject biFile = dataObject.getPrimaryFile();
            String name = biFile.getName();
            name = name.substring(0, name.length() - "BeanInfo".length()); // NOI18N
            FileObject javaFile = biFile.getParent().getFileObject(name, biFile.getExt());
            BIEditorSupport editor = findEditor(dataObject);
            if (javaFile != null) {
                final BeanInfoWorker beanInfoWorker = new GenerateBeanInfoAction.BeanInfoWorker(javaFile, biPanel);
                editor.worker = beanInfoWorker;
                beanInfoWorker.analyzePatterns().addTaskListener(new TaskListener() {

                    public void taskFinished(Task task) {
                        beanInfoWorker.updateUI();
                    }
                });
            } else {
                // notify missing source file
                biPanel.setContext(BiNode.createNoSourceNode(biFile));
            }
        }

        @Override
        public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
            super.readExternal(oi);
            dataObject = (DataObject) oi.readObject();
//            initialize();
        }

        @Override
        public void writeExternal(ObjectOutput oo) throws IOException {
            super.writeExternal(oo);
            oo.writeObject(dataObject);
        }

    }

    private static final class BIGES implements GuardedEditorSupport {
        
        StyledDocument doc = null;
        
        public StyledDocument getDocument() {
            return BIGES.this.doc;
        }
    }
    
    private static final class Environment extends DataEditorSupport.Env {

        private static final long serialVersionUID = -1;

        private final transient CookieSet cookieSet;
        private transient SaveSupport saveCookie = null;

        private final class SaveSupport implements SaveCookie {
            public void save() throws java.io.IOException {
                DataObject dobj = getDataObject();
                ((DataEditorSupport) findCloneableOpenSupport()).saveDocument();
                dobj.setModified(false);
            }
        }

        public Environment(DataObject obj, CookieSet cookieSet) {
            super(obj);
            this.cookieSet = cookieSet;
        }

        protected FileObject getFile() {
            return this.getDataObject().getPrimaryFile();
        }

        protected FileLock takeLock() throws java.io.IOException {
            return ((MultiDataObject)this.getDataObject()).getPrimaryEntry().takeLock();
        }

        public @Override CloneableOpenSupport findCloneableOpenSupport() {
            return findEditor(this.getDataObject());
        }


        public void addSaveCookie() {
            DataObject javaData = this.getDataObject();
            if (javaData.getCookie(SaveCookie.class) == null) {
                if (this.saveCookie == null)
                    this.saveCookie = new SaveSupport();
                this.cookieSet.add(this.saveCookie);
                javaData.setModified(true);
            }
        }

        public void removeSaveCookie() {
            DataObject javaData = this.getDataObject();
            if (javaData.getCookie(SaveCookie.class) != null) {
                this.cookieSet.remove(this.saveCookie);
                javaData.setModified(false);
            }
        }
    }
    
    private class TopComponentsListener implements PropertyChangeListener {
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (TopComponent.Registry.PROP_OPENED.equals(evt.getPropertyName())) {
                // Check closed top components
                @SuppressWarnings("unchecked")
                Set<TopComponent> closed = (Set<TopComponent>) evt.getOldValue();
                closed.removeAll((Set) evt.getNewValue());
                for (TopComponent o : closed) {
                    if (o instanceof CloneableTopComponent) {
                        final CloneableTopComponent topComponent = (CloneableTopComponent) o;
                        Enumeration<CloneableTopComponent> en = topComponent.getReference().getComponents();
                        if (multiviewTC == topComponent) {
                            if (en.hasMoreElements()) {
                                // Remember next cloned top component
                                multiviewTC = (CloneableTopComponent) en.nextElement();
                            } else {
                                // All cloned top components are closed
                                notifyClosed();
                            }
                        }
                    }
                }
            }
        }
        
    }

}

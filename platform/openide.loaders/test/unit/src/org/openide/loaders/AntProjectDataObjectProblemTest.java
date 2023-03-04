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

package org.openide.loaders;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.InstanceCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.openide.windows.CloneableOpenSupport;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AntProjectDataObjectProblemTest extends NbTestCase {
    private FileObject fo;
    private DataObject obj;
    private Loader l;

    public AntProjectDataObjectProblemTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        l = Loader.getLoader(Loader.class);
        AddLoaderManuallyHid.addRemoveLoader(l, true);
    }

    @Override
    protected void tearDown() throws Exception {
        AddLoaderManuallyHid.addRemoveLoader(l, false);
    }



    public void testInconsistentLookupIssue15153() throws IOException {
        fo = FileUtil.createData(new File(getWorkDir(), "build.xml"));

        obj = DataObject.find(fo);

        assertEquals("Correct object", AntProjectDataObject.class, obj.getClass());

        Collection<? extends Object> res;
        Node n = obj.getNodeDelegate();
        res = n.getLookup().lookupAll(FileEncodingQueryImplementation.class);
        assertEquals("No cookie: " + res, 0, res.size());
        res = n.getLookup().lookupAll(AntProjectCookie.class);
        assertEquals("One cookie: " + res, 1, res.size());
        res = n.getLookup().lookupAll(AntProjectCookie.ParseStatus.class);
        assertEquals("One cookie: " + res, 1, res.size());
        res = n.getLookup().lookupAll(Node.Cookie.class);
        assertEquals("two: " + res, 2, res.size());
        res = n.getLookup().lookupAll(DataObject.class);
        assertEquals("One cookie: " + res, 1, res.size());
        res = n.getLookup().lookupAll(ActionMap.class);
        assertEquals("No map: " + res, 0, res.size());
        res = n.getLookup().lookupAll(EditorCookie.class);
        assertEquals("One editor: " + res, 1, res.size());

        res = n.getLookup().lookupAll(AntProjectSupport.class);
        res = n.getLookup().lookupAll(AntProjectDataObject.class);
        res = n.getLookup().lookupAll(EditCookie.class);
        res = n.getLookup().lookupAll(InstanceCookie.class);
        res = n.getLookup().lookupAll(AntActionInstance.class);
        res = n.getLookup().lookupAll(MultiDataObject.class);
        res = n.getLookup().lookupAll(EditorCookie.Observable.class);
        res = n.getLookup().lookupAll(LineCookie.class);
        res = n.getLookup().lookupAll(PrintCookie.class);
        res = n.getLookup().lookupAll(OpenCookie.class);
        res = n.getLookup().lookupAll(Object.class);


        res = n.getLookup().lookupAll(AntProjectCookie.class);
        assertEquals("One cookie still: " + res, 1, res.size());
    }

    public static class Loader extends MultiFileLoader {

        public Loader() {
            super(AntProjectDataObject.class);
        }

        @Override
        protected FileObject findPrimaryFile(FileObject fo) {
            return fo;
        }

        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile)
        throws DataObjectExistsException, IOException {
            return new AntProjectDataObject(primaryFile, this);
        }

        @Override
        protected Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry(obj, primaryFile);
        }

        @Override
        protected Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }

    public static class AntProjectDataObject extends MultiDataObject
            implements PropertyChangeListener {

        public AntProjectDataObject(
            FileObject pf, MultiFileLoader loader
        ) throws DataObjectExistsException, IOException {
            super(pf, loader);
            CookieSet cookies = getCookieSet();
            cookies.add (new AntProjectDataEditor (this));
            FileObject prim = getPrimaryFile ();
            AntProjectCookie proj = new AntProjectSupport (prim);
            cookies.add (proj);
            if (proj.getFile () != null) {
                cookies.add (new AntActionInstance (proj));
            }
//            cookies.add(new CheckXMLSupport(DataObjectAdapters.inputSource(this)));
            addPropertyChangeListener (this);
        }

        @Override
        protected Node createNodeDelegate () {
            return new AntProjectNode (this);
        }

        @Override
        public Lookup getLookup() {
            return getCookieSet().getLookup();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            
        }
    }

    public interface AntProjectCookie extends Node.Cookie {
        /** Get the disk file for the build script.
         * @return the disk file, or null if none (but must be a file object)
         */
        File getFile ();
        /** Get the file object for the build script.
         * @return the file object, or null if none (but must be a disk file)
         */
        FileObject getFileObject ();
        /** Get the last parse-related exception, if there was one.
         * @return the parse exception, or null if it is valid
         */
        Throwable getParseException ();
        /** Add a listener to changes in the document.
         * @param l the listener to add
         */
        void addChangeListener (ChangeListener l);
        /** Remove a listener to changes in the document.
         * @param l the listener to remove
         */
        void removeChangeListener (ChangeListener l);

        /** Extended cookie permitting queries of parse status.
         * If only the basic cookie is available, you cannot
         * determine if a project is already parsed or not, and
         * methods which require it to be parsed for them to return
         * may block until a parse is complete.
         * @since 2.10
         */
        interface ParseStatus extends AntProjectCookie {
            /** Check whether the project is currently parsed.
             * Note that "parsed in error" is still considered parsed.
             * <p>If not parsed, then if and when it does later become
             * parsed, a change event should be fired. A project
             * might become unparsed after being parsed, due to e.g.
             * garbage collection; this need not fire any event.
             * <p>If the project is currently parsed, the methods
             * {@link AntProjectCookie#getDocument},
             * {@link AntProjectCookie#getProjectElement}, and
             * {@link AntProjectCookie#getParseException} should
             * not block.
             * @return true if this project is currently parsed
             */
            boolean isParsed();
        }

    }
    public static final class AntProjectNode extends DataNode implements ChangeListener {
        public AntProjectNode (DataObject obj) {
            this(obj, obj.getCookie(AntProjectCookie.class));
        }

        private AntProjectNode(DataObject obj, AntProjectCookie cookie) {
            super(obj, Children.LEAF);
            cookie.addChangeListener(WeakListeners.change(this, cookie));
        }

        public void stateChanged(ChangeEvent e) {
        }
    }
    static final class AntProjectDataEditor extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie.Observable, PrintCookie, ChangeListener {

        private boolean addedChangeListener = false;

        public AntProjectDataEditor (AntProjectDataObject obj) {
            super (obj, new AntEnv (obj));
            setMIMEType("text/ant+xml");
        }

        @Override
        protected boolean notifyModified () {
            if (!super.notifyModified ()) {
                return false;
            } else {
                AntEnv e = (AntEnv) env;
//                e.getAntProjectDataObject ().addSaveCookie (e);
                return true;
            }
        }

        @Override
        protected void notifyUnmodified () {
            super.notifyUnmodified ();
            AntEnv e = (AntEnv) env;
//            e.getAntProjectDataObject ().removeSaveCookie (e);
        }

        @Override
        protected String messageName() {
            String name = super.messageName();
            return annotateWithProjectName(name);
        }

        @Override
        protected String messageHtmlName () {
            String name = super.messageHtmlName();
            return name != null ? annotateWithProjectName(name) : null;
        }

        /** #25793 fix - adds project name to given ant script name if needed.
         * @return ant script name annotated with project name or ant script name unchanged
         */
        private String annotateWithProjectName (String name) {
            DataObject d = getDataObject();
            if (d.getPrimaryFile().getNameExt().equals("build.xml")) { // NOI18N
                // #25793: show project name in case the script name does not suffice
                if (!addedChangeListener) {
//                    cookie.addChangeListener(WeakListeners.change(this, cookie));
                    addedChangeListener = true;
                }
            }
            return name;
        }


        /**
         * Overridden to ensure that the displayName of the node in the editor has
         * the right annotation for build.xml files, so that the Navigator will display it.
         */
        @Override
        protected void initializeCloneableEditor(CloneableEditor editor) {
            super.initializeCloneableEditor(editor);
            editor.setActivatedNodes(new Node[] {
                new FilterNode(getDataObject().getNodeDelegate()) {
                    @Override
                    public String getDisplayName() {
                        return messageName();
                    }
                }
            });
        }

        public void stateChanged(ChangeEvent e) {
            // Project name might have changed. See messageName().
            updateTitles();
        }

        private static class AntEnv extends DataEditorSupport.Env implements SaveCookie {

            private static final long serialVersionUID = 6610627377311504616L;

            public AntEnv (AntProjectDataObject obj) {
                super (obj);
            }

            AntProjectDataObject getAntProjectDataObject () {
                return (AntProjectDataObject) getDataObject ();
            }

            @Override
            protected FileObject getFile () {
                return getDataObject ().getPrimaryFile ();
            }

            @Override
            protected FileLock takeLock () throws IOException {
                return ((AntProjectDataObject) getDataObject ()).getPrimaryEntry ().takeLock ();
            }

            public void save () throws IOException {
                ((AntProjectDataEditor) findCloneableOpenSupport ()).saveDocument ();
                getDataObject ().setModified (false);
            }

            @Override
            public CloneableOpenSupport findCloneableOpenSupport () {
                return (CloneableOpenSupport) getDataObject ().getCookie (EditCookie.class);
            }

        }

    }
    public static class AntProjectSupport 
    implements AntProjectCookie.ParseStatus, DocumentListener,
        /*FileChangeListener,*/ PropertyChangeListener {
        private FileObject fo;

        AntProjectSupport(FileObject prim) {
            this.fo = prim;
        }

        public void insertUpdate(DocumentEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeUpdate(DocumentEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void changedUpdate(DocumentEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void propertyChange(PropertyChangeEvent evt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public File getFile() {
            return FileUtil.toFile(fo);
        }

        public FileObject getFileObject() {
            return fo;
        }

        public Throwable getParseException() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }

        public boolean isParsed() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    public static class AntActionInstance implements
            InstanceCookie, Action,
            Presenter.Menu, Presenter.Toolbar,
            ChangeListener, PropertyChangeListener
    {

        private AntActionInstance(AntProjectCookie proj) {
        }

        public void stateChanged(ChangeEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void propertyChange(PropertyChangeEvent evt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String instanceName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Class<?> instanceClass() throws IOException, ClassNotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object instanceCreate() throws IOException, ClassNotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object getValue(String key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void putValue(String key, Object value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setEnabled(boolean b) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isEnabled() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public JMenuItem getMenuPresenter() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Component getToolbarPresenter() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}

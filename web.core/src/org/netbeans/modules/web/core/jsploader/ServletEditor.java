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

package org.netbeans.modules.web.core.jsploader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.ObjectInput;
import java.util.Date;
import java.beans.*;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import javax.swing.text.Document;
import javax.swing.Timer;
import javax.swing.event.CaretListener;
import javax.swing.text.Caret;
import javax.swing.event.CaretEvent;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.PrintCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataNode;
import org.openide.nodes.Node;
import org.openide.windows.CloneableOpenSupport;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.WeakListeners;
 
/** Editor for servlet files generated from JSP files. Main features:
 * <ul>
 * <li>All text is read-only (guarded) </li>
 * <li>The editor can work on different files, reloads after recomplilation.</li>
 * </ul>
 *
 * @author Petr Jiricka, Yury Kamen
 */

public class ServletEditor extends CloneableEditorSupport 
    implements EditorCookie.Observable, CloseCookie, PrintCookie, PropertyChangeListener {

    static final String ATTR_FILE_ENCODING = "Content-Encoding"; // NOI18N

    /** Create a new Editor support for the given Java source.
     * @param entry the (primary) file entry representing the Java source file
     */
    public ServletEditor(JspDataObject jspdo) {
        super(new JspEnv(jspdo));
        jspdo.addPropertyChangeListener(WeakListeners.propertyChange(this, jspdo));
    }
    
    @Override
    protected CloneableEditor createCloneableEditor () {
        return new ServletEditorComponent (this);
    }
    
    protected JspEnv jspEnv() {
        return (JspEnv)env;
    }
    
    /** Overriding the default loading from stream - need to look at encoding */
    @Override
    protected void loadFromStreamToKit (StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        FileObject fo = getServlet().getPrimaryFile();
        String encoding =  (String) fo.getAttribute(ATTR_FILE_ENCODING); //NOI18N
        
        if (encoding == null) {
            encoding = "ISO-8859-1"; // NOI18N
        }
        InputStreamReader reader = new InputStreamReader(stream, encoding);
        kit.read(reader, doc, 0);
    }

    protected JspServletDataObject getServlet() {
        return jspEnv().getJspDataObject().getServletDataObject();
    }

    /** Constructs message that should be displayed when the data object
    * is modified and is being closed.
    *
    * @return text to show to the user
    */
    protected String messageSave () {
        return "";
    }

    /** Constructs message that should be used to name the editor component.
    *
    * @return name of the editor
    */
    protected String messageName () {
        DataObject dobj = getServlet();
        if (dobj == null) return "...";   // NOI18N
        if (! dobj.isValid()) return ""; // NOI18N

        if(DataNode.getShowFileExtensions()) {
            return dobj.getPrimaryFile().getNameExt();
        } else {
            return dobj.getPrimaryFile().getName();
        }
    }
    
    /** Text to use as tooltip for component.
    *
    * @return text to show to the user
    */
    protected String messageToolTip () {
        DataObject dobj = getServlet();
        if (dobj != null) {
             //update tooltip
            return FileUtil.getFileDisplayName(dobj.getPrimaryFile());
        }
        else
            return "...";   // NOI18N
    }

    /** Message to display when an object is being opened.
    * @return the message or null if nothing should be displayed
    */
    protected String messageOpening () {
        DataObject obj = getServlet();
        if (obj == null)
            return "";

        return NbBundle.getMessage (ServletEditor.class , "CTL_ObjectOpen", // NOI18N
            obj.getPrimaryFile().getNameExt(),
            FileUtil.getFileDisplayName(obj.getPrimaryFile())
        );
    }
    

    /** Message to display when an object has been opened.
    * @return the message or null if nothing should be displayed
    */
    protected String messageOpened () {
        DataObject obj = getServlet();
        if (obj == null)
            return "";

        return NbBundle.getMessage (ServletEditor.class, "CTL_ObjectOpened", // NOI18N
            obj.getPrimaryFile().getNameExt(),
            FileUtil.getFileDisplayName(obj.getPrimaryFile())
        );
    }

    /** only accessibility method */
    MultiDataObject.Entry getJavaEntry() {
        return getServlet().getPrimaryEntry();
    }

    /** Let's the super method create the document and also annotates it
     * with Title and StreamDescription properities.
     *
     * @param kit kit to user to create the document
     * @return the document annotated by the properties
     */
    @Override
    protected StyledDocument createStyledDocument(EditorKit kit) {
        StyledDocument doc = super.createStyledDocument(kit);
        
        setDocumentProperties(doc);
        
        return doc;
    }
    
    /** Sets the correct properties of the given StyledDocument
     */
    void setDocumentProperties(Document doc) {
        DataObject obj = getServlet();
        if (obj != null) {
            //set document name property
            doc.putProperty(javax.swing.text.Document.TitleProperty, obj.getPrimaryFile().getPath());
            //set dataobject to stream desc property
            doc.putProperty(javax.swing.text.Document.StreamDescriptionProperty, obj);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(JspDataObject.PROP_SERVLET_DATAOBJECT)) {
            StyledDocument doc = getDocument();
            if (doc != null) {
                setDocumentProperties(doc);
            }
        }
    }    

    public static class ServletEditorComponent extends CloneableEditor {

        static final int SELECTED_NODES_DELAY = 1000; //copied from the JavaEditor
        Timer timerSelNodes;//copied from the JavaEditor
        /** Listener on caret movements */
        CaretListener caretListener; //copied from the JavaEditor
        /** The last caret offset position. */
        int lastCaretOffset = -1; //copied from the JavaEditor

        /** For externalization. */
        public ServletEditorComponent() {
            super();
        }

        public ServletEditorComponent(ServletEditor support) {
            super(support);
            init();
        }

        @Override
        protected void componentShowing() {
            super.componentShowing(); // just this initializes the pane now
            pane.setEditable(false);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            init();
        }

        protected JEditorPane getPane() {
            return pane;
        }
        
        /** Called after creation of this object and after deexternalization. */
        private void init() {
            //set the pane read only
            if ( null != getPane()) {
                getPane().setEditable(false);
            }
            //register a listener to set activated nodes after a change of the servlet
            ServletEditor se = (ServletEditor)cloneableEditorSupport();
            if (se != null) {
                se.jspEnv().addPropertyChangeListener(
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            if (JspEnv.PROP_TIME.equals(evt.getPropertyName()))
                                initializeJavaNodes();
                            ServletEditorComponent.this.updateName();
                        }
                    }
                );
            }
            initializeJavaNodes();
        }
        
        /* This method is called when parent window of this component has focus,
         * and this component is preferred one in it.
         *
         * copied from the JavaEditor
        */
        @Override
        protected void componentActivated () {
            pane.addCaretListener(caretListener);
            super.componentActivated (); 
        }
        
        /** Selects element at the given position. 
         *  
         *  copied from the JavaEditor
         */
        void selectElementsAtOffset(final int offset) {
            ServletEditor se = (ServletEditor)cloneableEditorSupport();
            if (se != null) {
                JspDataObject jspdo = se.jspEnv().getJspDataObject();
                if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                    setNodes(jspdo, offset);
                }
                else{
                    javax.swing.SwingUtilities.invokeLater(new NodesThread(jspdo, offset));
                }
            }
        }
        
        private class NodesThread implements Runnable {
            JspDataObject jspdo;
            int offset;
            
            NodesThread (JspDataObject jspdo, int offset){
                this.jspdo = jspdo;
                this.offset = offset;
            }
            
            public void run() {
                setNodes(jspdo, offset);
            }
        }
        
        private void setNodes (JspDataObject jspdo, final int offset){
            JspServletDataObject servlet = jspdo.getServletDataObject();
            if (servlet==null) {
                setActivatedNodes(new Node[] { jspdo.getNodeDelegate() });
                return;
            }
        }
        
        /** Obtain a support for this component 
         *  copied from the JavaEditor
         */
        private void initializeJavaNodes() {
    	    //This local is to keep javac 1.2 happy.
            ServletEditor se = (ServletEditor)cloneableEditorSupport();
            if (se != null) {
                timerSelNodes = new Timer(100, new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        if (lastCaretOffset == -1 && pane != null) {
                            Caret caret = pane.getCaret();
                            if (caret != null)
                                lastCaretOffset = caret.getDot();
                        }
                        selectElementsAtOffset(lastCaretOffset);
                    }
                });
                timerSelNodes.setInitialDelay(100);
                timerSelNodes.setRepeats(false);
                timerSelNodes.restart();
                caretListener = new CaretListener() { 
                    public void caretUpdate(CaretEvent e) {
                        restartTimerSelNodes(e.getDot());
                    }
                };
            }
            if (lastCaretOffset == -1 && pane != null) {
                Caret caret = pane.getCaret();
                if (caret != null) lastCaretOffset = caret.getDot();
            }
            selectElementsAtOffset(lastCaretOffset);
        }

        /**
         * Refreshes the activated node immediately. Provides system actions
         * based on the node activated at the time of popu invoke.
         *
         *copied from the JavaEditor
         */
        @Override
        public SystemAction[] getSystemActions() {
            selectElementsAtOffset(lastCaretOffset);
            timerSelNodes.stop();
            return super.getSystemActions();
        }
        
        /** Restart the timer which updates the selected nodes after the specified delay from
         * last caret movement.
         *
         * copied from the JavaEditor
        */
        void restartTimerSelNodes(int pos) {
            timerSelNodes.setInitialDelay(SELECTED_NODES_DELAY);
            timerSelNodes.restart();
            lastCaretOffset = pos;
        }

    } // JspServletEditorComponent
    
     private static class JspEnv extends FileChangeAdapter implements CloneableEditorSupport.Env,
        java.io.Serializable, PropertyChangeListener {
            
        private static final long serialVersionUID = -5748207023470614141L;
        
        /** JSP page for which we are displaying the servlets. */
        protected JspDataObject jspdo;
        
        private DataObject servlet;
        
        /** support for firing of property changes
        */
        private transient PropertyChangeSupport propSupp;

        public JspEnv(JspDataObject jspdo) {
            this.jspdo = jspdo;
            init();
        }
        
        private DataObject getServlet() {
            synchronized (jspdo) {
                DataObject newServlet = jspdo.getServletDataObject();
                if (servlet != newServlet) {
                    if (servlet != null) {
                        servlet.getPrimaryFile().removeFileChangeListener(this);
                    }
                    if (newServlet != null) {
                        newServlet.getPrimaryFile().addFileChangeListener(this);
                    }
                    servlet = newServlet;
                }
                return servlet;
            }
        }
        
//        private void readObject (ObjectInputStream ois)
//        throws IOException, ClassNotFoundException {
//            ois.defaultReadObject();
//            init();
//        }
        
        private void init() {
            jspdo.addPropertyChangeListener(WeakListeners.propertyChange (this, jspdo));
        }
        
        public JspDataObject getJspDataObject() {
            return jspdo;
        }
        
        public void propertyChange(PropertyChangeEvent ev) {
            if (JspDataObject.PROP_SERVLET_DATAOBJECT.equals (ev.getPropertyName())) {
                DataObject servlet = getServlet();
                if (servlet == null) {
                    firePropertyChange(PROP_VALID, Boolean.TRUE, Boolean.FALSE);
                }
                else {
                    firePropertyChange(PROP_TIME, null, null);
                }
            }
            
            firePropertyChange (
                ev.getPropertyName (),
                ev.getOldValue (),
                ev.getNewValue ()
            );
        }
        
        /** Fires property change.
        * @param name the name of property that changed
        * @param oldValue old value
        * @param newValue new value
        */
        protected void firePropertyChange (String name, Object oldValue, Object newValue) {
            prop ().firePropertyChange (name, oldValue, newValue);
        }
        
        /** Lazy getter for change support.
        */
        private PropertyChangeSupport prop () {
            synchronized (this) {
                if (propSupp == null) {
                    propSupp = new PropertyChangeSupport (this);
                }
                return propSupp;
            }
        }
        
        /** Obtains the input stream.
        * @exception IOException if an I/O error occures
        */
        public InputStream inputStream () throws IOException {
            DataObject servlet = getServlet();
            if (servlet != null)
                return servlet.getPrimaryFile().getInputStream();
            else
                return null;
        }

        /** Obtains the output stream.
        * @exception IOException if an I/O error occures
        */
        public OutputStream outputStream () throws IOException {
            // this file is read only, don't need an input stream
            return null;
        }

        /** The time when the data has been modified
        */
        public Date getTime () {
            DataObject servlet = getServlet();
            if (servlet != null)
                return servlet.getPrimaryFile().lastModified();
            else
                return new Date(System.currentTimeMillis());
        }

        /** Mime type of the document.
        * @return the mime type to use for the document
        */
        public String getMimeType () {
            return "text/x-java";   // NOI18N
        }
        /** Adds property listener.
        */
        public void addPropertyChangeListener (PropertyChangeListener l) {
            prop ().addPropertyChangeListener (l);
        }
        /** Removes property listener.
        */
        public void removePropertyChangeListener (PropertyChangeListener l) {
            prop ().removePropertyChangeListener (l);
        }

        /** Adds veto listener.
        */
        public void addVetoableChangeListener (VetoableChangeListener l) {
        }
        /** Removes veto listener.
        */
        public void removeVetoableChangeListener (VetoableChangeListener l) {
        }

        /** Test whether the support is in valid state or not.
        * It could be invalid after deserialization when the object it
        * referenced to does not exist anymore.
        *
        * @return true or false depending on its state
        */
        public boolean isValid () {
            DataObject servlet = getServlet();
            return (servlet != null);
        }
        /** Test whether the object is modified or not.
        * @return true if the object is modified
        */
        public boolean isModified () {
            return false;
        }

        /** Support for marking the environement modified.
        * @exception IOException if the environment cannot be marked modified
        *    (for example when the file is readonly), when such exception
        *    is the support should discard all previous changes
        */
        public void markModified () throws java.io.IOException {
            // do nothing
        }

        /** Reverse method that can be called to make the environment 
        * unmodified.
        */
        public void unmarkModified () {
        }

        /** Method that allows environment to find its 
        * cloneable open support.
        */
        public CloneableOpenSupport findCloneableOpenSupport () {
            return (CloneableOpenSupport)jspdo.getServletEditor();
        }
        
        // methods from FileChangeAdapter

        /** Handles <code>FileObject</code> deletion event. */
        @Override
        public void fileDeleted(FileEvent fe) {
            fe.getFile().removeFileChangeListener(this);
            
            jspdo.refreshPlugin(true);
        }
        
        /** Fired when a file is changed.
         * @param fe the event describing context where action has taken place
         */
        @Override
        public void fileChanged(FileEvent fe) {
            firePropertyChange(PROP_TIME, null, null);
        }
    }
    
}

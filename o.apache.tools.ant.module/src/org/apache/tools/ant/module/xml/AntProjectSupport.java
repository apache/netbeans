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

package org.apache.tools.ant.module.xml;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class AntProjectSupport implements AntProjectCookie.ParseStatus, DocumentListener, PropertyChangeListener, FileChangeListener {

    private static final Logger LOG = Logger.getLogger(AntProjectSupport.class.getName());
    
    private FileObject fo;

    private Document projDoc = null; // [PENDING] SoftReference
    private Throwable exception = null;
    private boolean parsed = false;
    private Reference<StyledDocument> styledDocRef = null;
    private final Object parseLock;

    private final ChangeSupport cs = new ChangeSupport(this);
    private Reference<EditorCookie.Observable> editorRef;
    
    private DocumentBuilder documentBuilder;
    //@GuardedBy("parseLock")
    private FileChangeListener fileListener;
    
    // milliseconds of quiet time after a textual document change after which
    // changes will be fired and the XML may be reparsed
    private static final int REPARSE_DELAY = 3000;

    public AntProjectSupport (FileObject fo) {
        this.fo = fo;
        parseLock = new Object ();
        rp = new RequestProcessor("AntProjectSupport[" + fo + "]"); // NOI18N
    }
  
    private synchronized EditorCookie.Observable getEditor() {
        FileObject file = getFileObject();
        if (file == null) return null;
        EditorCookie.Observable editor = editorRef == null ? null : editorRef.get();
        if (editor == null) {
            try {
                editor = DataObject.find(file).getLookup().lookup(EditorCookie.Observable.class);
                if (editor != null) {
                    editor.addPropertyChangeListener(WeakListeners.propertyChange(this, editor));
                    editorRef = new WeakReference<EditorCookie.Observable>(editor);
                }
            } catch (DataObjectNotFoundException donfe) {
                LOG.log(Level.INFO, "no editor for " + fo, donfe);
            }
        }
        return editor;
    }
    
    public File getFile () {
        FileObject file = getFileObject();
        if (file != null) {
            return FileUtil.toFile(file);
        } else {
            return null;
        }
    }
    
    public FileObject getFileObject () {
        if (fo != null && !fo.isValid()) { // #11065
            return null;
        }
        return fo;
    }
    
    public void setFile (File f) { // #11979
        fo = FileUtil.toFileObject(f);
        invalidate ();
    }
    
    public void setFileObject (FileObject fo) { // #11979
        this.fo = fo;
        invalidate ();
    }
    
    public boolean isParsed() {
        return parsed;
    }
    
    public Document getDocument () {
        synchronized (parseLock) {
            if (!parsed) {
                parseDocument();
            }
            if (projDoc == null) {
                return null;
            }
            // #111862: avoid returning the original
            try {
                return (Document) projDoc.cloneNode(true);
            } catch (DOMException x) {
                Logger.getLogger(AntProjectSupport.class.getName()).log(Level.INFO, "#154502: cloning document for " + this, x);
                return projDoc;
            }
        }
    }
    
    public Throwable getParseException () {
        synchronized (parseLock) {
            if (!parsed) {
                parseDocument();
            }
            return exception;
        }
    }
    
    /**
     * Make a DocumentBuilder object for use in this support.
     * Thread-safe, but of course the result is not.
     * @throws Exception for various reasons of configuration
     */
    private static synchronized DocumentBuilder createDocumentBuilder() throws Exception {
        //DocumentBuilderFactory factory = (DocumentBuilderFactory)Class.forName(XERCES_DOCUMENT_BUILDER_FACTORY).newInstance();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        documentBuilder.setErrorHandler(ErrHandler.DEFAULT);
        return documentBuilder;
    }
    
    /**
     * XML parser error handler; passes on all errors.
     */
    private static final class ErrHandler implements ErrorHandler {
        static final ErrorHandler DEFAULT = new ErrHandler();
        private ErrHandler() {}
        public void error(SAXParseException exception) throws SAXException {
            throw exception;
        }
        public void fatalError(SAXParseException exception) throws SAXException {
            throw exception;
        }
        public void warning(SAXParseException exception) throws SAXException {
            throw exception;
        }
    }
    
    /**
     * Utility method to get a properly configured XML input source for a script.
     */
    public static InputSource createInputSource(final FileObject fo, final StyledDocument document) throws IOException {
        if (fo != null) {
            DataObject d = DataObject.find(fo);
            if (!d.isModified()) {
                // #58194: no need to parse the live document.
                try {
                    InputSource s = new InputSource();
                    s.setSystemId(fo.getURL().toExternalForm());
                    s.setByteStream(fo.getInputStream());
                    return s;
                } catch (FileStateInvalidException e) {
                    assert false : e;
                }
            }
        }
        final String[] contents = new String[1];
        document.render(new Runnable() {
            public void run() {
                try {
                    contents[0] = document.getText(0, document.getLength());
                } catch (BadLocationException e) {
                    throw new AssertionError(e);
                }
            }
        });
        InputSource in = new InputSource(new StringReader(contents[0]));
        if (fo != null) { // #10348
            try {
                in.setSystemId(fo.getURL().toExternalForm());
            } catch (FileStateInvalidException e) {
                assert false : e;
            }
            // [PENDING] Ant's ProjectHelper has an elaborate set of work-
            // arounds for inconsistent parser behavior, e.g. file:foo.xml
            // works in Ant but not with Xerces parser. You must use just foo.xml
            // as the system ID. If necessary, Ant's algorithm could be copied
            // here to make the behavior match perfectly, but it ought not be necessary.
        }
        return in;
    }
    
    private void parseDocument () {
        assert Thread.holdsLock(parseLock); // so it is OK to use documentBuilder
        FileObject file = getFileObject ();
        LOG.log(Level.FINE, "AntProjectSupport.parseDocument: fo={0}", file);
        try {
            if (documentBuilder == null) {
                documentBuilder = createDocumentBuilder();
            }
            EditorCookie ed = getEditor ();
            Document doc = null;
            if (ed != null) {
                final StyledDocument document = ed.getDocument();
                if (document != null) {
                    if (file != null && fileListener != null) {
                        file.removeFileChangeListener(fileListener);
                    }
                    // add only one Listener (listeners for doc are hold in a List!)
                    if ((styledDocRef != null && styledDocRef.get () != document) || styledDocRef == null) {
                        document.addDocumentListener(this);
                        styledDocRef = new WeakReference<StyledDocument>(document);
                    }
                    InputSource in = createInputSource(file, document);
                    try {
                        doc = documentBuilder.parse(in);
                    } finally {
                        if (in.getByteStream() != null) {
                            in.getByteStream().close();
                        }
                    }
                }
            } 
            if (doc == null) {
                if (file != null) {
                    if (fileListener == null) {
                        fileListener = FileUtil.weakFileChangeListener(this, file);
                        file.addFileChangeListener(fileListener);
                    }
                    try (InputStreamReader reader = new InputStreamReader(
                        file.getInputStream(),
                        FileEncodingQuery.getEncoding(file))) {
                        InputSource in = new InputSource(reader);
                        in.setSystemId(file.toURL().toExternalForm());
                        doc = documentBuilder.parse(in);
                    }
                } else {
                    exception = new FileNotFoundException("Ant script probably deleted"); // NOI18N
                    return;
                }
            }
            projDoc = doc;
            exception = null;
        } catch (Exception e) {
            // leave projDoc the way it is...
            exception = e;
            if (!(exception instanceof SAXParseException)) {
                LOG.log(Level.INFO, "Strange parse error in " + this, exception);
            }
        }
        fireChangeEvent(false);
        parsed = true;
    }
    
    public Element getProjectElement () {
        Document doc = getDocument ();
        if (doc != null) {
            return doc.getDocumentElement ();
        } else {
            return null;
        }
    }
    
    @Override
    public boolean equals (Object o) {
        if (! (o instanceof AntProjectSupport)) return false;
        AntProjectSupport other = (AntProjectSupport) o;
        if (fo != null) {
            return fo.equals (other.fo);
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode () {
        return 27825 ^ (fo != null ? fo.hashCode() : 0);
    }
    
    @Override
    public String toString () {
        FileObject file = getFileObject ();
        if (file != null) {
            return file.toString();
        } else {
            return "<missing Ant script>"; // NOI18N
        }
    }
    
    public void addChangeListener (ChangeListener l) {
        cs.addChangeListener(l);
    }
    
    public void removeChangeListener (ChangeListener l) {
        cs.removeChangeListener(l);
    }
    
    private final RequestProcessor rp;
    private RequestProcessor.Task task = null;
    
    protected void fireChangeEvent(boolean delay) {
        LOG.log(Level.FINE, "AntProjectSupport.fireChangeEvent: fo={0}", fo);
        ChangeFirer f = new ChangeFirer();
        synchronized (this) {
            if (task == null) {
                task = rp.post(f, delay ? REPARSE_DELAY : 0);
            } else if (!delay) {
                task.schedule(0);
            }
        }
    }
    private final class ChangeFirer implements Runnable {
        public ChangeFirer() {}
        public void run () {
            LOG.log(Level.FINE, "AntProjectSupport.ChangeFirer.run: fo={0}", fo);
            synchronized (AntProjectSupport.this) {
                if (task == null) {
                    return;
                }
                task = null;
            }
            cs.fireChange();
        }
    }
    
    public void removeUpdate (DocumentEvent ev) {
        invalidate();
    }
    
    public void changedUpdate (DocumentEvent ev) {
        // Not to worry, just text attributes or something...
    }
    
    public void insertUpdate (DocumentEvent ev) {
        invalidate();
    }
    
    // Called when editor support changes state: #11616
    public void propertyChange(PropertyChangeEvent e) {
        if (EditorCookie.Observable.PROP_DOCUMENT.equals(e.getPropertyName())) {
            invalidate();
        }
    }

    @Override
    public void fileChanged(FileEvent fe) {
        invalidate();
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        invalidate();
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
    }
    
    protected final void invalidate () {
        LOG.log(Level.FINE, "AntProjectSupport.invalidate: fo={0}", fo);
        parsed = false;
        fireChangeEvent(true);
    }

}

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

package org.netbeans.modules.xml.multiview;

import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.windows.CloneableTopComponent;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.io.ReaderInputStream;
import org.openide.util.NbBundle;

import java.io.*;
import java.util.Enumeration;
import java.util.Date;
import java.lang.ref.WeakReference;
import org.netbeans.modules.xml.api.XmlFileEncodingQueryImpl;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;

/**
 * Base class for data objects that are used as a basis for
 * the xml multiview. Provides support for caching data ({@link DataCache}), switching
 * view, encoding and keeping track of currently active multiview element. Furthermore, it
 * associates <code>XmlMultiViewEditorSupport</code> with this data object.
 *
 * Created on October 5, 2004, 10:49 AM
 * @author  mkuchtiak
 */
public abstract class XmlMultiViewDataObject extends MultiDataObject implements CookieSet.Factory {
    
    public static final String PROP_DOCUMENT_VALID = "document_valid"; //NOI18N
    public static final String PROP_SAX_ERROR = "sax_error"; //NOI18N
    public static final String PROPERTY_DATA_MODIFIED = "data modified";  //NOI18N
    public static final String PROPERTY_DATA_UPDATED = "data changed";  //NOI18N
    protected XmlMultiViewEditorSupport editorSupport;
    private org.xml.sax.SAXException saxError;
    
    private final DataCache dataCache = new DataCache();
    private EncodingHelper encodingHelper = new EncodingHelper();
    private transient long timeStamp = 0;
    private transient WeakReference lockReference;
    
    
    private MultiViewElement activeMVElement;
    
    private final SaveCookie saveCookie = new SaveCookie() {
        /** Implements <code>SaveCookie</code> interface. */
        public void save() throws java.io.IOException {
            getEditorSupport().saveDocument();
        }
    };
    
    /** Creates a new instance of XmlMultiViewDataObject */
    public XmlMultiViewDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        getCookieSet().add(XmlMultiViewEditorSupport.class, this);
        getCookieSet().assign(FileEncodingQueryImplementation.class, new Object[]{XmlFileEncodingQueryImpl.singleton()});
    }
    
    protected EditorCookie createEditorCookie() {
        return getEditorSupport();
    }
    
    protected String getEditorMimeType() {
        return null;
    }

    public org.openide.nodes.Node.Cookie createCookie(Class clazz) {
        if (clazz.isAssignableFrom(XmlMultiViewEditorSupport.class)) {
            return getEditorSupport();
        } else {
            return null;
        }
    }
    
    /** Gets editor support for this data object. */
    protected synchronized XmlMultiViewEditorSupport getEditorSupport() {
        if(editorSupport == null) {
            editorSupport = new XmlMultiViewEditorSupport(this);
            editorSupport.getMultiViewDescriptions();
        }
        return editorSupport;
    }
    
    /** enables to switch quickly to XML perspective in multi view editor
     */
    public void goToXmlView() {
        getEditorSupport().goToXmlPerspective();
    }
    
    protected void setSaxError(org.xml.sax.SAXException saxError) {
        org.xml.sax.SAXException oldError = this.saxError;
        this.saxError=saxError;
        if (oldError==null) {
            if (saxError != null) {
                firePropertyChange(PROP_DOCUMENT_VALID, Boolean.TRUE, Boolean.FALSE);
            }
        } else {
            if (saxError == null) {
                firePropertyChange(PROP_DOCUMENT_VALID, Boolean.FALSE, Boolean.TRUE);
            }
        }
        
        String oldErrorMessage = getErrorMessage(oldError);
        String newErrorMessage = getErrorMessage(saxError);
        if (oldErrorMessage==null) {
            if (newErrorMessage!=null) {
                firePropertyChange(PROP_SAX_ERROR, null, newErrorMessage);
            }
        } else if (!oldErrorMessage.equals(newErrorMessage)) {
            firePropertyChange(PROP_SAX_ERROR, oldErrorMessage, newErrorMessage);
        }
    }
    
    private static String getErrorMessage(Exception e) {
        return e == null ? null : e.getMessage();
    }
    
    public org.xml.sax.SAXException getSaxError() {
        return saxError;
    }
    
    /** Icon for XML View */
    protected java.awt.Image getXmlViewIcon() {
        return ImageUtilities.loadImage("org/netbeans/modules/xml/multiview/resources/xmlObject.gif"); //NOI18N
    }
    
    /** MultiViewDesc for MultiView editor
     */
    protected DesignMultiViewDesc[] getMultiViewDesc() {
        return new DesignMultiViewDesc[0];
    }
    
    public void setLastOpenView(int index) {
        getEditorSupport().setLastOpenView(index);
    }
    
    /** provides renaming of super top component */
    protected FileObject handleRename(String name) throws IOException {
        FileObject retValue = super.handleRename(name);
        getEditorSupport().updateDisplayName();
        return retValue;
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
    
    /**
     * Set whether the object is considered modified.
     * Also fires a change event.
     * If the new value is <code>true</code>, the data object is added into a {@link #getRegistry registry} of opened data objects.
     * If the new value is <code>false</code>,
     * the data object is removed from the registry.
     */
    public void setModified(boolean modif) {
        super.setModified(modif);
        //getEditorSupport().updateDisplayName();
        if (modif) {
            // Add save cookie
            if (getCookie(SaveCookie.class) == null) {
                getCookieSet().add(saveCookie);
            }
        } else {
            // Remove save cookie
            if(saveCookie.equals(getCookie(SaveCookie.class))) {
                getCookieSet().remove(saveCookie);
            }
            
        }
    }
    
    public boolean canClose() {
        final CloneableTopComponent topComponent = ((CloneableTopComponent) getEditorSupport().getMVTC());
        if (topComponent != null){
            Enumeration enumeration = topComponent.getReference().getComponents();
            if (enumeration.hasMoreElements()) {
                enumeration.nextElement();
                if (enumeration.hasMoreElements()) {
                    return true;
                }
            }
        }
        FileLock lock;
        try {
            lock = waitForLock();
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return !isModified();
        }
        try {
            return !isModified();
        } finally {
            lock.releaseLock();
        }
    }
    
    public FileLock waitForLock() throws IOException {
        return waitForLock(10000);
    }
    
    public FileLock waitForLock(long timeout) throws IOException {
        long t = System.currentTimeMillis() + timeout;
        long sleepTime = 50;
        for (;;) {
            try {
                return dataCache.lock();
            } catch (IOException e) {
                if (System.currentTimeMillis() > t) {
                    throw (IOException) new IOException("Cannot wait for data lock for more than " + timeout + " ms").initCause(e); //NO18N
                }
                try {
                    Thread.sleep(sleepTime);
                    sleepTime = 3 * sleepTime / 2; 
                } catch (InterruptedException e1) {
                    //
                }
            }
        }
    }
    
    public org.netbeans.core.api.multiview.MultiViewPerspective getSelectedPerspective() {
        return getEditorSupport().getSelectedPerspective();
    }
    
    /** Enable to focus specific object in Multiview Editor
     *  The default implementation opens the XML View.
     */
    public void showElement(Object element) {
        getEditorSupport().edit();
    }
    
    /** Enable to get active MultiViewElement object
     */
    protected MultiViewElement getActiveMultiViewElement() {
        return activeMVElement;
    }
    void setActiveMultiViewElement(MultiViewElement element) {
        activeMVElement = element;
    }
    /** Opens the specific view
     * @param index multi-view index
     */
    public void openView(int index) {
        getEditorSupport().openView(index);
    }
    
    protected abstract String getPrefixMark();
    
    boolean acceptEncoding() throws IOException {
        String encoding = encoding();
        if (encodingDiffer(encoding)) {
            Object result = showChangeEncodingDialog(encoding);
            if (NotifyDescriptor.YES_OPTION.equals(result)) {
                encodingReset();
            } else if (NotifyDescriptor.NO_OPTION.equals(result)) {
                showUsingDifferentEncodingMessage(encoding);
            } else {
                return false;
            }
        }
        return true;
    }

    String encoding() throws IOException {
        encodingHelper.resetEncoding();
        DataCache dataCache = getDataCache();
        String s = dataCache.getStringData();
        String encoding = encodingHelper.detectEncoding(s.getBytes());
        return encoding;
    }

    boolean encodingDiffer(String encoding) {
        return !encodingHelper.getEncoding().equals(encoding);
    }

    String encodingMessage(String encoding) {
        return NbBundle.getMessage(XmlMultiViewDataObject.class,
                        "TEXT_TREAT_USING_DIFFERENT_ENCODING",
                        encoding, encodingHelper.getEncoding());
    }

    void encodingReset() {
        DataCache dataCache = getDataCache();
        String s = dataCache.getStringData();
        dataCache.setData(encodingHelper.setDefaultEncoding(s));
    }
    
    private void showUsingDifferentEncodingMessage(String encoding) {
        String message = encodingMessage(encoding);
        NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(message);
        descriptor.setTitle(getPrimaryFile().getPath());
        DialogDisplayer.getDefault().notify(descriptor);
    }
    
    private Object showChangeEncodingDialog(String encoding) {
        String message = NbBundle.getMessage(Utils.class, "TEXT_CHANGE_DECLARED_ENCODING", encoding,
                encodingHelper.getEncoding());
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(message, getPrimaryFile().getPath(),
                NotifyDescriptor.YES_NO_CANCEL_OPTION);
        return DialogDisplayer.getDefault().notify(descriptor);
    }
    
    public EncodingHelper getEncodingHelper() {
        return encodingHelper;
    }
    
    public DataCache getDataCache() {
        return dataCache;
    }

    /**
     * 
     * @return index for source view
     */
    protected int getXMLMultiViewIndex() {
        return 0;
    }
    
    /** Is that necesary for this class to be public ?
     *  It can be changed to interface
     */
    public class DataCache {
        
        // What about using the StringBuffer instead ?
        private transient String buffer = null;
        private long fileTime = 0;
        
        public void loadData() {
            FileObject file = getPrimaryFile();
            if (fileTime == file.lastModified().getTime()) {
                // on base of issue #132922
                // when the file is deleted or IO error happened, lastModified time
                // is zero and the buffer can stay uninitialized
                if (fileTime == 0) {
                    buffer = ""; //NOI18N
                }
                return;
            }
            try {
                FileLock dataLock = lock();
                loadData(file, dataLock);
            } catch (IOException e) {
                if (buffer == null) {
                    buffer = ""; //NOI18N
                }
            }
        }
        
        /**
         * Updates the data cache with the contents of the associated file. 
         * Unlike {@link #loadData()}, tries to use existing lock before attempting
         * to acquire a new lock.
         */
        public void reloadData() throws IOException{
            FileObject file = getPrimaryFile();
            if (fileTime == file.lastModified().getTime()) {
                return;
            }
            FileLock lock;
            synchronized (this) {
                lock = getLock();
                if (lock == null){
                    lock = lock();
                }
            }
            loadData(file, lock);
            
        }
        /** Does this method need to be public ?
         */
        public void loadData(FileObject file, FileLock dataLock) throws IOException {
            try {
                BufferedInputStream inputStream = new BufferedInputStream(file.getInputStream());
                String encoding = encodingHelper.detectEncoding(inputStream);
                if (!encodingHelper.getEncoding().equals(encoding)) {
                    showUsingDifferentEncodingMessage(encoding);
                }
                Reader reader = new InputStreamReader(inputStream, encodingHelper.getEncoding());
                long time;
                StringBuffer sb = new StringBuffer(2048);
                try {
                    char[] buf = new char[1024];
                    time = file.lastModified().getTime();
                    int i;
                    while ((i = reader.read(buf,0,1024)) != -1) {
                        sb.append(buf,0,i);
                    }
                } finally {
                    reader.close();
                }
                buffer = null;
                fileTime = time;
                setData(dataLock, sb.toString(), true);
            } finally {
                dataLock.releaseLock();
            }
        }
        /** Is the second argument necessary ?
         */
        public void setData(FileLock lock, String s, boolean modify) throws IOException {
            testLock(lock);
            boolean modified = isModified() || modify;
            long oldTimeStamp = timeStamp;
            if (setData(s)) {
                if (!modified) {
                    saveData(lock);
                    firePropertyChange(PROPERTY_DATA_UPDATED, new Long(oldTimeStamp), new Long(timeStamp));
                } else {
                    firePropertyChange(PROPERTY_DATA_MODIFIED, new Long(oldTimeStamp), new Long(timeStamp));
                }
            } 
        }
        
        private boolean setData(String s) {
            // ??? when this can happen
            if (s.equals(buffer)) {
                return false;
            }
            buffer = s;
            long newTimeStamp = new Date().getTime();
            if (newTimeStamp <= timeStamp) {
                newTimeStamp = timeStamp + 1;
            }
            timeStamp = newTimeStamp;
            fileTime = 0;
            return true;
        }
        
        public synchronized void saveData(FileLock dataLock) {
            if (buffer == null || fileTime == getPrimaryFile().lastModified().getTime()) {
                return;
            }
            
            try {
                XmlMultiViewEditorSupport editorSupport = getEditorSupport();
                if (editorSupport.getDocument() == null) {
                    XmlMultiViewEditorSupport.XmlEnv xmlEnv = editorSupport.getXmlEnv();
                    FileLock lock = null;
                    try {
                        lock = xmlEnv.takeLock();
                        OutputStream outputStream = getPrimaryFile().getOutputStream(lock);
                        Writer writer = new OutputStreamWriter(outputStream, encodingHelper.getEncoding());
                        try {
                            writer.write(buffer);
                        } finally {
                            writer.close();
                            xmlEnv.unmarkModified();
                            resetFileTime();
                        }
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                    }
                } else {
                    editorSupport.saveDocument(dataLock);
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        
        public FileLock lock() throws IOException {
            synchronized (this) {
                FileLock current = getLock();
                if (current != null) {
                    throw new FileAlreadyLockedException("File is already locked by [" + current + "]."); // NO18N
                }
                FileLock l = new FileLock();
                lockReference = new WeakReference(l);
                return l;
            }
        }
        
        private synchronized FileLock getLock() {
            // How this week reference can be useful ?
            FileLock l = lockReference == null ? null : (FileLock) lockReference.get();
            if (l != null && !l.isValid()) {
                l = null;
            }
            return l;
        }
        
        public String getStringData() {
            if (buffer == null) {
                loadData();
            }
            return buffer;
        }
        
        public byte[] getData() {
            try {
                return getStringData().getBytes(encodingHelper.getEncoding());
            } catch (UnsupportedEncodingException e) {
                ErrorManager.getDefault().notify(e);
                return null;  // should not happen
            }
        }
        
        public void setData(FileLock lock, byte[] data, boolean modify) throws IOException {
            encodingHelper.detectEncoding(data);
            setData(lock, new String(data, encodingHelper.getEncoding()), modify);
        }
        
        public long getTimeStamp() {
            return timeStamp;
        }
        
        public InputStream createInputStream() {
            try {
                encodingHelper.detectEncoding(getStringData().getBytes());
                return new ReaderInputStream(new StringReader(getStringData()), encodingHelper.getEncoding());
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
                return null;
            }
        }
        
        public Reader createReader() throws IOException {
            return new StringReader(getStringData());
        }
        
        public OutputStream createOutputStream() throws IOException {
            final FileLock dataLock = lock();
            return new ByteArrayOutputStream() {
                public void close() throws IOException {
                    try {
                        super.close();
                        setData(dataLock, toByteArray(), true);
                    } finally {
                        dataLock.releaseLock();
                    }
                }
            };
        }
        
        public OutputStream createOutputStream(final FileLock dataLock, final boolean modify) throws IOException {
            testLock(dataLock);
            return new ByteArrayOutputStream() {
                public void close() throws IOException {
                    super.close();
                    setData(dataLock, toByteArray(), modify);
                    if (!modify) {
                        dataCache.saveData(dataLock);
                    }
                }
            };
        }
        
        public Writer createWriter() throws IOException {
            final FileLock dataLock = lock();
            return new StringWriter() {
                public void close() throws IOException {
                    try {
                        super.close();
                        setData(dataLock, toString(), true);
                    } finally {
                        dataLock.releaseLock();
                    }
                }
            };
        }
        
        public Writer createWriter(final FileLock dataLock, final boolean modify) throws IOException {
            testLock(dataLock);
            return new StringWriter() {
                public void close() throws IOException {
                    super.close();
                    setData(dataLock, toString(), modify);
                    if (!modify) {
                        dataCache.saveData(dataLock);
                    }
                }
            };
        }
        
        public void testLock(FileLock lock) throws IOException {
            if (lock == null) {
                throw new IOException("Lock is null."); //NO18N
            } else if (lock != getLock()){
                throw new IOException("Invalid lock [" + lock + "]. Expected [" + getLock() + "]."); //NO18N
            }
        }
        
        public void resetFileTime() {
            fileTime = getPrimaryFile().lastModified().getTime();
        }
    }
    /** Access point for inheritors to verify document before close.
     *
     * @return true if document is valid, false otherwise
     */
    protected boolean verifyDocumentBeforeClose() {
        return true;
    }
}

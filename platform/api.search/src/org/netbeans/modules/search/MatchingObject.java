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

package org.netbeans.modules.search;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.search.TextDetail.DetailNode;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * Data structure holding a reference to the found object and information
 * whether occurences in the found object should be replaced or not.
 * 
 * @author  Marian Petras
 * @author  Tim Boudreau
 */
public final class MatchingObject implements Comparable<MatchingObject>,
        Selectable {

    public static final String PROP_INVALIDITY_STATUS =
            "invalidityStatus";                                         //NOI18N
    /** Fired when number of selected tex matches changes. */
    public static final String PROP_MATCHES_SELECTED
            = "matchesSelected";                                        //NOI18N
    /** Fired when the MatchingObject is selected or deselected. */
    public static final String PROP_SELECTED = "selected";              //NOI18N
    /** Fired when the matching object is removed (hidden) from results. */
    public static final String PROP_REMOVED = "removed";                //NOI18N
    /** Fired when some child of this object is removed from results. */
    public static final String PROP_CHILD_REMOVED = "child_removed";    //NOI18N

    /** */
    private static final Logger LOG =
            Logger.getLogger(MatchingObject.class.getName());
    
    /**
     * Char/byte buffer for reading/decoding file contents.
     */
    private static final int FILE_READ_BUFFER_SIZE = 4096;
    
    /** */
    private final ResultModel resultModel;
    /** */
    private FileObject fileObject;
    /** */
    private DataObject dataObject;
    /** */
    private long timestamp;
    /** */
    private int matchesCount = 0;
    /** */
    private Node nodeDelegate = null;
    /** */
    List<TextDetail> textDetails;
    
    /**
     * charset used for full-text search of the object.
     * It is {@code null} if the object was not full-text searched.
     */
    private Charset charset;
    
    /**
     * holds information on whether the {@code object} is selected
     * to be replaced or not.
     * Unless {@link #matchesSelection} is non-{@code null}, this field's
     * value also applies to the object's subnodes (if any).
     * 
     * @see  #matchesSelection
     */
    private boolean selected = true;
    /**
     * holds information on whether the node representing this object
     * is expanded or collapsed
     * 
     * @see  #markExpanded(boolean)
     */
    private boolean expanded = false;

    /** holds number of selected (checked) matches */
    private int selectedMatchesCount = 0;
    /** */
    private boolean valid = true;
    /** */
    private boolean refreshed = false;
    /** */
    private InvalidityStatus invalidityStatus = null;
    /** */
    private StringBuilder text;
    private final PropertyChangeSupport changeSupport =
            new PropertyChangeSupport(this);
    private FileListener fileListener;
    private final MatchSelectionListener matchSelectionListener =
            new MatchSelectionListener();

    /**
     * Creates a new {@code MatchingObject} with a reference to the found
     * object (returned by {@code SearchGroup}).
     * 
     * @param  fileObject  found object returned by the {@code SearchGroup}
     *                 (usually a {@code DataObject}) - must not be {@code null}
     * @param  charset  charset used for full-text search of the object,
     *                  or {@code null} if the object was not full-text searched
     * @exception  java.lang.IllegalArgumentException
     *             if the passed {@code object} is {@code null}
     */
    MatchingObject(ResultModel resultModel, FileObject fileObject,
            Charset charset, List<TextDetail> textDetails) {

        if (resultModel == null) {
            throw new IllegalArgumentException("resultModel = null");   //NOI18N
        }
        if (fileObject == null) {
            throw new IllegalArgumentException("object = null");        //NOI18N
        }

        this.textDetails = textDetails;
        this.resultModel = resultModel;
        this.charset = charset;
        this.fileObject = fileObject;
        
        dataObject = dataObject();
        timestamp = fileObject.lastModified().getTime();
        valid = (timestamp != 0L);

        if (dataObject != null) {
            matchesCount = computeMatchesCount();
            nodeDelegate = dataObject.getNodeDelegate();
        }
        setUpDataObjValidityChecking();
        if (textDetails != null && !textDetails.isEmpty()) {
            adjustTextDetails();
        }
    }

    /**
     * Set line number indent for text details.
     */
    private void adjustTextDetails() {
        TextDetail lastDetail = textDetails.get(textDetails.size() - 1);
        int maxLine = lastDetail.getLine();
        int maxDigits = countDigits(maxLine);
        for (TextDetail td : textDetails) {
            selectedMatchesCount += 1;
            int digits = countDigits(td.getLine());
            if (digits < maxDigits) {
                td.setLineNumberIndent(indent(maxDigits - digits));
            }
            td.addChangeListener(matchSelectionListener);
        }
    }

    /**
     * Get number of digits of a positive number.
     */
    private int countDigits(int number) {
        int digits = 0;
        while (number > 0) {
            number = number / 10;
            digits++;
        }
        return digits;
    }

    /**
     * Get string with spaces of length {@code chars}.
     */
    private String indent(int chars) {
        switch (chars) { // switch to compute common values faster
            case 1:
                return "&nbsp;&nbsp;";                                  //NOI18N
            case 2:
                return "&nbsp;&nbsp;&nbsp;&nbsp;";                      //NOI18N
            case 3:
                return "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";          //NOI18N
            default:
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < chars; i++) {
                    sb.append("&nbsp;&nbsp;");                          //NOI18N
                }
                return sb.toString();
        }
    }
    
    /**
     */
    private void setUpDataObjValidityChecking() {
        if (fileObject != null && fileObject.isValid()) {
            fileListener = new FileListener();
            fileObject.addFileChangeListener(fileListener);
        }
    }
    
    /**
     */
    void cleanup() {
        if(fileObject != null && fileListener != null) {
            fileObject.removeFileChangeListener(fileListener);
            fileListener = null;
        }
        dataObject = null;
        nodeDelegate = null;
        changeSupport.firePropertyChange(PROP_REMOVED, null, null);
    }

    private void setInvalid(InvalidityStatus invalidityStatus) {
        if (this.invalidityStatus == invalidityStatus) {
            return;
        }
        InvalidityStatus oldStatus = this.invalidityStatus;
        this.valid = false;
        this.invalidityStatus = invalidityStatus;
        if (fileObject != null && fileListener != null
                && invalidityStatus == InvalidityStatus.DELETED) {
            fileObject.removeFileChangeListener(fileListener);
        }
        changeSupport.firePropertyChange(PROP_INVALIDITY_STATUS,
                oldStatus, invalidityStatus);
    }
    
    /**
     * Is the {@code DataObject} encapsulated by this {@code MatchingObject}
     * valid?
     * 
     * @return  {@code true} if the {@code DataObject} is valid, false otherwise
     * @see  DataObject#isValid
     */
    public boolean isObjectValid() {
        // #190819
        return valid && dataObject != null ? dataObject.isValid() : false;
    }

    /**
     */
    public FileObject getFileObject() {
        return fileObject;
    }
    
    /**
     */
    @Override
    public void setSelected(boolean selected) {
        if (selected == this.selected) {
            return;
        }
        
        this.selected = selected;
        changeSupport.firePropertyChange(PROP_SELECTED, !selected, selected);
    }

    @Override
    public void setSelectedRecursively(boolean selected) {
        if (this.selected == selected) {
            return;
        }
        matchSelectionListener.setEnabled(false);
        int origMatchesSelected = selectedMatchesCount;
        try {
            if (textDetails != null) {
                for (TextDetail td : getTextDetails()) {
                    td.setSelectedRecursively(selected);
                }
            }
            setSelected(selected);
            selectedMatchesCount = selected ? getTextDetails().size() : 0;
            changeSupport.firePropertyChange(PROP_MATCHES_SELECTED,
                    origMatchesSelected,
                    selectedMatchesCount);
        } finally {
            matchSelectionListener.setEnabled(true);
        }
    }

    /**
     */
    @Override
    public boolean isSelected() {
        return selected;
    }

    /**
     * Stores information whether the node representing this object is expanded
     * or collapsed.
     * 
     * @param  expanded  {@code true} if the node is expanded,
     *                   {@code false} if the node is collapsed
     * @see  #isExpanded()
     */
    void markExpanded(boolean expanded) {
        this.expanded = expanded;
    }
    
    /**
     * Provides information whether the node representing this object
     * is expanded or collapsed.
     * 
     * @return  {@code true} if the node is expanded,
     *          {@code false} if the node is collapsed
     * @see  #markExpanded
     */
    boolean isExpanded() {
        return expanded;
    }
    
    
    /** Get the name (not the path) of the file */
    String getName() {        
        return getFileObject().getNameExt();
    }

    String getHtmlDisplayName() {
        return getFileObject().getNameExt();
    }

    /**
     */
    long getTimestamp() {
        return timestamp;
    }
    
    /**
     */
    String getDescription() { 
        return getFileObject().getParent().getPath();
    }

    /**
     */
    String getText() throws IOException {
         StringBuilder txt = text(false);
         return (txt != null)?  txt.toString() : null;
    }

    public List<TextDetail> getTextDetails() {
        return textDetails;
    }

    public int getDetailsCount() {
        if (textDetails == null) {
            return 0;
        } else {
            return textDetails.size();
        }
    }

    /**
     * @return {@codeDetailNode}s representing the matches, or
     * <code>null</code> if no matching string is known for this matching
     * object.
     * @see DetailNode
     */
    public Node[] getDetails() {

        if (textDetails == null) {
            return null;
        }

        List<Node> detailNodes = new ArrayList<>(textDetails.size());
        for (TextDetail txtDetail : textDetails) {
            detailNodes.add(new TextDetail.DetailNode(txtDetail, false, this));
        }

        return detailNodes.toArray(new Node[0]);
    }

    public Children getDetailsChildren(boolean replacing) {
        return new DetailsChildren(replacing, resultModel);
    }

    /**
     */
    FileLock lock() throws IOException {
        return getFileObject().lock();
    }

    /**
     * Reads the file if it has not been read already.
     * 
     * @author  TimBoudreau
     * @author  Marian Petras
     */
    StringBuilder text(boolean refreshCache) throws IOException {
        assert !EventQueue.isDispatchThread();

        if (refreshCache || (text == null)) {     
            if (charset == null) {
                text = new StringBuilder(getFileObject().asText());
            } else {
                text = new StringBuilder();
                CharsetDecoder decoder = charset.newDecoder();
                try (InputStream istm = getFileObject().getInputStream();
                        InputStreamReader isr = new InputStreamReader(istm,
                                decoder);
                        BufferedReader br = new BufferedReader(isr,
                                FILE_READ_BUFFER_SIZE)) {
                    int read;
                    char[] chars = new char[FILE_READ_BUFFER_SIZE];
                    while ((read = br.read(chars)) != -1) {
                        text.append(chars, 0, read);
                    }
                }
            }
        }      
        return text;
    }

    @Override
    public int compareTo(MatchingObject o) {
            if(o == null) {
                return Integer.MAX_VALUE;
            }
            return getName().compareToIgnoreCase(o.getName()); // locale?
    }

    /** Initialize DataObject from object. */
    private DataObject dataObject() {
        try {
            return DataObject.find(fileObject);
        } catch (DataObjectNotFoundException ex) {
            valid = false;
            return null;
        }
    }

    public DataObject getDataObject() {
        return dataObject;
    }

    public synchronized void addPropertyChangeListener(
            PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(
            PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public synchronized void addPropertyChangeListener(
            String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public synchronized void removePropertyChangeListener(
            String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Describes invalidity status of this item.
     */
    public enum InvalidityStatus {
        
        DELETED(true, "Inv_status_Err_deleted"),                        //NOI18N
        BECAME_DIR(true, "Inv_status_Err_became_dir"),                  //NOI18N
        CHANGED(false, "Inv_status_Err_changed"),                       //NOI18N
        TOO_BIG(false, "Inv_status_Err_too_big"),                       //NOI18N
        CANT_READ(false, "Inv_status_Err_cannot_read");                 //NOI18N
        
        /**
         * Is this invalidity the fatal one?
         * 
         * @see  #isFatal()
         */
        private final boolean fatal;
        /**
         * resource bundle key for the description
         * 
         * @see  #getDescription(String)
         */
        private final String descrBundleKey;
        
        /**
         * Creates an invalidity status.
         * 
         * @param  fatal  whether this status means that the invalidity is fatal
         * @see  #isFatal()
         */
        private InvalidityStatus(boolean fatal, String descrBundleKey) {
            this.fatal = fatal;
            this.descrBundleKey = descrBundleKey;
        }
        
        /**
         * Is this invalidity fatal such that the item should be removed
         * from the search results?
         */
        boolean isFatal() {
            return fatal;
        }

        /**
         * Provides human-readable description of this invalidity status.
         * 
         * @param  path  path or name of file that has this invalidity status
         * @return  description of the invalidity status with the given path
         *          or name embedded
         */
        String getDescription(String path) {
            return NbBundle.getMessage(getClass(), descrBundleKey, path);
        }
        
    }
    
    /**
     */
    InvalidityStatus checkValidity() {
        InvalidityStatus oldStatus = invalidityStatus;
        InvalidityStatus status = getFreshInvalidityStatus();
        if (status != null) {
            valid = false;
            invalidityStatus = status;
        }
        if (oldStatus != invalidityStatus) {
            changeSupport.firePropertyChange(PROP_INVALIDITY_STATUS,
                    oldStatus, invalidityStatus);
        }
        return status;
    }

    public InvalidityStatus getInvalidityStatus() {
        return invalidityStatus;
    }
    
    /**
     */
    String getInvalidityDescription() {
        String descr;
        
        InvalidityStatus status = getFreshInvalidityStatus();
        if (status != null) {
            descr = status.getDescription(getFileObject().getPath());
        } else {
            descr = null;
        }
        return descr;
    }
    
    /**
     * Check validity status of this item.
     * 
     * @return  an invalidity status of this item if it is invalid,
     *          or {@code null} if this item is valid
     * @author  Tim Boudreau
     * @author  Marian Petras
     */
    private InvalidityStatus getFreshInvalidityStatus() {
        log(FINER, "getInvalidityStatus()");                            //NOI18N
        FileObject f = getFileObject();
        if (!f.isValid()) {
            log(FINEST, " - DELETED");            
            return InvalidityStatus.DELETED;
        }
        if (f.isFolder()) {
            log(FINEST, " - BECAME_DIR");            
            return InvalidityStatus.BECAME_DIR;
        }
        
        long stamp = f.lastModified().getTime();
        if ((!refreshed && stamp > resultModel.getStartTime())
                || (refreshed && stamp > timestamp)) {
            log(SEVERE, "file's timestamp changed since start of the search");
            if (LOG.isLoggable(FINEST)) {
                final java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTimeInMillis(stamp);
                log(FINEST, " - file stamp:           " + stamp + " (" + cal.getTime() + ')');
                cal.setTimeInMillis(resultModel.getStartTime());
                log(FINEST, " - result model created: " + resultModel.getStartTime() + " (" + cal.getTime() + ')');
            }            
            return InvalidityStatus.CHANGED;
        }
        
        if (f.getSize() > Integer.MAX_VALUE) {            
            return InvalidityStatus.TOO_BIG;
        }
        
        if (!f.canRead()) {           
            return InvalidityStatus.CANT_READ;
        }
        
        return null;
    }
    
    /**
     */
    boolean isValid() {
        return valid;
    }

    /**
     * Update data object. Can be called when a module is enabled and new data
     * loader produces new data object.
     */
    public void updateDataObject(DataObject updatedDataObject) {
        FileObject updatedPF = updatedDataObject.getPrimaryFile();
        if (dataObject == null
                || dataObject.getPrimaryFile().equals(updatedPF)) {
            if (updatedPF.isValid()) {
                this.invalidityStatus = null;
                if (fileListener == null) {
                    this.fileListener = new FileListener();
                    updatedPF.addFileChangeListener(fileListener);
                } else if (updatedPF != dataObject.getPrimaryFile()) {
                    dataObject.getPrimaryFile().removeFileChangeListener(
                            fileListener);
                    updatedPF.addFileChangeListener(fileListener);
                }
                this.dataObject = updatedDataObject;
                this.nodeDelegate = updatedDataObject.getNodeDelegate();
                this.valid = true;
                for (TextDetail td : textDetails) {
                    td.updateDataObject(updatedDataObject);
                }
            }
        } else {
            throw new IllegalArgumentException(
                    "Expected data object for the same file");          //NOI18N
        }
    }

    /**
     */
    public InvalidityStatus replace() throws IOException {
        assert !EventQueue.isDispatchThread();
        assert isSelected();
        
        StringBuilder content = text(true);  //refresh the cache, reads the file
        List<TextDetail> textMatches = getTextDetails();
        int toReplace = 0;
        for (TextDetail td : textMatches) {
            toReplace += td.isSelected() ? 1 : 0;
        }
        if (toReplace == 0) {
            return null;
        }

        int offsetShift = 0;
        for (int i=0; i < textMatches.size(); i++) {
            TextDetail textDetail = textMatches.get(i);
            if (!textDetail.isSelected()){
                continue;
            }
            String matchedSubstring = content.substring(textDetail.getStartOffset() + offsetShift, textDetail.getEndOffset() + offsetShift);
            if (!matchedSubstring.equals(textDetail.getMatchedText())) {
                log(SEVERE, "file match part differs from the expected match");  //NOI18N
                if (LOG.isLoggable(FINEST)) {
                    log(SEVERE, " - expected line: \""                           //NOI18N
                                + textDetail.getMatchedText()
                                + '"');
                    log(SEVERE, " - file line:     \""                           //NOI18N
                                + matchedSubstring
                                + '"');
                }
                return InvalidityStatus.CHANGED;
            }

            String replacedString = resultModel.basicCriteria.getReplaceExpr();
            if (resultModel.basicCriteria.getSearchPattern().isRegExp()){
                Matcher m = resultModel.basicCriteria.getTextPattern().matcher(matchedSubstring);
                replacedString = m.replaceFirst(resultModel.basicCriteria.getReplaceString());
            } else if (resultModel.basicCriteria.isPreserveCase()) {
                replacedString = adaptCase(replacedString, matchedSubstring);
            }
            
            content.replace(textDetail.getStartOffset() + offsetShift, textDetail.getEndOffset() + offsetShift, replacedString);
            offsetShift += replacedString.length() - matchedSubstring.length();
        }
        return null;
    }
    
    /** Modify case of a string according to a case pattern. Used in "Search
     *  and replace" action when "Preserve case" option is checked. 
     * 
     * Code copied from method {@link 
     * org.netbeans.modules.editor.lib2.search.DocumentFinder#preserveCaseImpl
     * DocumentFinder.preserveCaseImpl}
     * in module editor.lib2.
     * 
     * @param value String that should modified.
     * @param casePattern Case pattern.
     * @return 
     */
    public static String adaptCase(String value, String casePattern) {
                                                
        if (casePattern.equals(casePattern.toUpperCase())) {
            return value.toUpperCase();
        } else if (casePattern.equals(casePattern.toLowerCase())) {
            return value.toLowerCase();
        } else if (Character.isUpperCase(casePattern.charAt(0))) {
            return Character.toUpperCase(value.charAt(0)) + value.substring(1);
        } else if (Character.isLowerCase(casePattern.charAt(0))) {
            if (casePattern.substring(1).equals(casePattern.substring(1).toUpperCase())) {
                return Character.toLowerCase(value.charAt(0)) + value.substring(1).toUpperCase();
            } else {
                return Character.toLowerCase(value.charAt(0)) + value.substring(1);
            }
        } else {
            return value;
        }
    }
    
    /** debug flag */
    private static final boolean REALLY_WRITE = true;

    /**
     */
    void write(final FileLock fileLock) throws IOException {
        if (text == null) {
            throw new IllegalStateException("Buffer is gone");          //NOI18N
        }
        
        if (REALLY_WRITE) {            
            try (Writer writer = new OutputStreamWriter(
                        fileObject.getOutputStream(fileLock),
                        charset)) {
                writer.write(makeStringToWrite());
            }
        } else {
            System.err.println("Would write to " + getFileObject().getPath());//NOI18N
            System.err.println(text);
        }
    }

    /**
     */
    private String makeStringToWrite() {
        return makeStringToWrite(text);
    }
    
    /**
     */
    static String makeStringToWrite(StringBuilder text) {
        return text.toString();
    }

    /**
     */
    private void log(Level logLevel, String msg) {
        String id = dataObject != null
                    ? dataObject.getName()
                    : fileObject.toString();
        if (LOG.isLoggable(logLevel)) {
            LOG.log(logLevel, "{0}: {1}", new Object[]{id, msg});       //NOI18N
        }
    }
    
    /** Returns name of this node.
     * @return name of this node.
     */
    @Override
    public String toString() {
        return super.toString() + "[" + getName()+ "]"; // NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MatchingObject other = (MatchingObject) obj;
        if (this.resultModel == other.resultModel
                || (this.resultModel != null
                && this.resultModel.equals(other.resultModel))) {
            return this.fileObject == other.fileObject
                    || (this.fileObject != null
                    && this.fileObject.equals(other.fileObject));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + (this.fileObject != null ? this.fileObject.hashCode() : 0);
        hash = 73 * hash + (this.resultModel != null ? this.resultModel.hashCode() : 0);
        return hash;
    }

    /** Get number of matches in this matching object.  */
    private int computeMatchesCount() {
        return resultModel.getDetailsCount(this);
    }

    /** Get file display name, e.g. for JTree tooltip. */
    String getFileDisplayName() {
        return FileUtil.getFileDisplayName(fileObject);
    }

    /** Return pre-computed matches count. */
    int getMatchesCount() {
        return matchesCount;
    }

    /** Return node delegate. */
    Node getNodeDelegate() {
        return nodeDelegate;
    }

    /**
     * Remove text detail, update precomputed values, inform listeners.
     */
    public void removeDetail(TextDetail textDetail) {
        boolean removed = textDetails.remove(textDetail);
        if (removed) {
            matchesCount = getDetailsCount();
            resultModel.removeDetailMatch(this, textDetail);
            changeSupport.firePropertyChange(PROP_CHILD_REMOVED, null, null);
        }
    }

    /**
     * Remove this matching object from its result model and inform listeners.
     */
    public void remove() {
        resultModel.remove(this);
    }

    /**
     * Refresh the node, use information from a {@link Def} instance.
     */
    public void refresh(Def def) {
        refreshed = true; // ignore result set timestamp
        this.charset = def.getCharset();
        FileObject origFileObject = fileObject;
        this.fileObject = def.getFileObject();
        this.textDetails = def.getTextDetails();

        dataObject = dataObject();
        timestamp = fileObject.lastModified().getTime();
        valid = (timestamp != 0L);

        if (dataObject == null) {
            return;
        }
        if (fileObject != origFileObject) {
            if (fileListener != null) {
                origFileObject.removeFileChangeListener(fileListener);
            }
            setUpDataObjValidityChecking();
        }
        nodeDelegate = dataObject.getNodeDelegate();

        Mutex.EVENT.writeAccess(() -> {
            int origSelectedMatches = selectedMatchesCount;
            selectedMatchesCount = 0;
            if (textDetails != null && !textDetails.isEmpty()) {
                adjustTextDetails();
            }
            
            changeSupport.firePropertyChange(PROP_MATCHES_SELECTED,
                    origSelectedMatches, selectedMatchesCount);
            if (matchesCount > 0) {
                setSelected(true);
            }
            InvalidityStatus origInvStat = invalidityStatus;
            invalidityStatus = null;
            changeSupport.firePropertyChange(PROP_INVALIDITY_STATUS,
                    origInvStat,
                    invalidityStatus);
        });
    }

    public BasicComposition getBasicComposition() {
        return this.resultModel.basicComposition;
    }

    /**
     * Bridge between new API and legacy implementation, will be deleted.
     */
    public static class Def {

        private FileObject fileObject;
        private Charset charset;
        private List<TextDetail> textDetails;

        public Def(FileObject fileObject, Charset charset, List<TextDetail> textDetails) {
            this.fileObject = fileObject;
            this.charset = charset;
            this.textDetails = textDetails;
        }

        public Charset getCharset() {
            return charset;
        }

        public void setCharset(Charset charset) {
            this.charset = charset;
        }

        public FileObject getFileObject() {
            return fileObject;
        }

        public void setFileObject(FileObject fileObject) {
            this.fileObject = fileObject;
        }

        public List<TextDetail> getTextDetails() {
            return textDetails;
        }

        public void setTextDetails(List<TextDetail> textDetails) {
            this.textDetails = textDetails;
        }
    }

    private class DetailsChildren extends Children.Keys<TextDetail> {

        private final boolean replacing;

        public DetailsChildren(boolean replacing, ResultModel model) {
            this.replacing = replacing;
            setKeys(getTextDetails());

            MatchingObject.this.addPropertyChangeListener(PROP_CHILD_REMOVED,
                    (PropertyChangeEvent evt) -> update());
        }

        @Override
        protected Node[] createNodes(TextDetail key) {
            return new Node[]{new TextDetail.DetailNode(key, replacing,
                MatchingObject.this)};
        }

        public void update() {
            setKeys(getTextDetails());
        }
    }

    private class FileListener extends FileChangeAdapter {

        @Override
        public void fileDeleted(FileEvent fe) {
            setInvalid(InvalidityStatus.DELETED);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            if (resultModel.basicCriteria.isSearchAndReplace()) {
                setInvalid(InvalidityStatus.CHANGED);
            }
        }
    }

    /**
     * Listener for changes in selection of child text details. It can be
     * disabled if the changes are going to be initiated by this MatchingObject.
     */
    private class MatchSelectionListener implements ChangeListener {

        private boolean enabled = true;

        @Override
        public void stateChanged(ChangeEvent e) {
            if (enabled) {
                TextDetail td = (TextDetail) e.getSource();
                int origMatchesSelected = selectedMatchesCount;
                selectedMatchesCount += td.isSelected() ? 1 : -1;
                changeSupport.firePropertyChange(PROP_MATCHES_SELECTED,
                        origMatchesSelected, selectedMatchesCount);
                if (selected && selectedMatchesCount == 0) {
                    setSelected(false);
                } else if (!selected && selectedMatchesCount > 0) {
                    setSelected(true);
                }
            }
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}

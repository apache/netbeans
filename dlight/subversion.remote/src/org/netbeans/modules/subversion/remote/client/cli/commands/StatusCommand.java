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
package org.netbeans.modules.subversion.remote.client.cli.commands;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.ISVNNotifyListener;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNConflictDescriptor;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.subversion.remote.client.cli.SvnCommand;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * 
 */
public class StatusCommand extends SvnCommand {

    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss Z"; //NOI18N
    private byte[] output;
    
    private final VCSFileProxy files[];
    private final boolean getAll;
    private final boolean descend;
    private final boolean checkUpdates;
    private final boolean ignoreExternals;

    public StatusCommand(FileSystem fileSystem, VCSFileProxy[] files, boolean getAll, boolean descend, boolean checkUpdates, boolean ignoreExternals) {
        super(fileSystem);
        this.files = files;
        this.getAll = getAll;
        this.descend = descend;
        this.checkUpdates = checkUpdates;
        this.ignoreExternals = ignoreExternals;
    }

    @Override
    protected boolean hasBinaryOutput() {
        return true;
    }        
    
    @Override
    protected boolean notifyOutput() {
        return false;
    }    
    
    @Override
    protected ISVNNotifyListener.Command getCommand() {
        return ISVNNotifyListener.Command.STATUS;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {
	arguments.add("status"); //NOI18N
        arguments.add("--xml"); //NOI18N
        if (getAll) {
            arguments.add("-v"); //NOI18N
            arguments.add("--no-ignore"); //NOI18N
        }
        if (!descend) {
            arguments.add("-N"); //NOI18N
        }
        if (checkUpdates) {
            arguments.add("-u"); //NOI18N
        }
        if (ignoreExternals) {
            arguments.add("--ignore-externals"); //NOI18N
        }
	arguments.add(files);        
    }

    @Override
    public void output(byte[] bytes) {
        output = bytes;
    }
    
    public Status[] getStatusValues() throws SVNClientException {
        if (output == null || output.length == 0) {
            return new Status[0];
        }
        try {
            XMLReader saxReader = XMLUtil.createXMLReader();

            XmlEntriesHandler xmlEntriesHandler = new XmlEntriesHandler();
            saxReader.setContentHandler(xmlEntriesHandler);
            saxReader.setErrorHandler(xmlEntriesHandler);
            InputSource source = new InputSource(new ByteArrayInputStream(output));

            saxReader.parse(source);
            return xmlEntriesHandler.getStatusValues();
            
        } catch (SAXException ex) {
            throw new SVNClientException(ex);
        } catch (IOException ex) {
            throw new SVNClientException(ex);
        }
        
    }
    
    private class XmlEntriesHandler extends DefaultHandler {
                        
        private final List<Status> statusValues = new ArrayList<>();        

        private static final String WC_ST_ELEMENT_NAME      = "wc-status";      // NOI18N
        private static final String ENTRY_ELEMENT_NAME      = "entry";          // NOI18N
        private static final String NAME_ELEMENT_NAME       = "name";           // NOI18N
        private static final String SIZE_ELEMENT_NAME       = "size";           // NOI18N
        private static final String COMMIT_ELEMENT_NAME     = "commit";         // NOI18N
        private static final String AUTHOR_ELEMENT_NAME     = "author";         // NOI18N        
        private static final String DATE_ELEMENT_NAME       = "date";           // NOI18N        
        private static final String LOCK_ELEMENT_NAME       = "lock";           // NOI18N        
        private static final String REPO_ST_ELEMENT_NAME    = "repos-status";   // NOI18N        
        private static final String TOKEN_ELEMENT_NAME      = "token";          // NOI18N        
        private static final String OWNER_ELEMENT_NAME      = "owner";          // NOI18N        
        private static final String COMMENT_ELEMENT_NAME    = "comment";        // NOI18N        
        private static final String CREATED_ELEMENT_NAME    = "created";        // NOI18N        
        private static final String EXPIRES_ELEMENT_NAME    = "expires";        // NOI18N        
        
        private static final String PATH_ATTRIBUTE          = "path";           // NOI18N        
        private static final String PROPS_ATTRIBUTE         = "props";          // NOI18N        
        private static final String ITEM_ATTRIBUTE          = "item";           // NOI18N                        
        private static final String WC_LOCKED_ATTRIBUTE     = "wc-locked";      // NOI18N        
        private static final String COPIED_ATTRIBUTE        = "copied";         // NOI18N        
        private static final String SWITCHED_ATTRIBUTE      = "switched";       // NOI18N                
        private static final String REVISION_ATTRIBUTE      = "revision";       // NOI18N        
        private static final String TREE_CONFLICT_ATTRIBUTE = "tree-conflicted";// NOI18N
        
        private static final String PATH_ATTR               = "path";           // NOI18N        
        private static final String WC_PROPS_ATTR           = "wcprops";        // NOI18N        
        private static final String REPO_PROPS_ATTR         = "repoprops";      // NOI18N        
        private static final String WC_ITEM_ATTR            = "wcitem";         // NOI18N                        
        private static final String REPO_ITEM_ATTR          = "repoitem";       // NOI18N                        
        private static final String WC_LOCKED_ATTR          = "wc-locked";      // NOI18N        
        private static final String WC_COPIED_ATTR          = "copied";         // NOI18N        
        private static final String WC_SWITCHED_ATTR        = "switched";       // NOI18N                
        private static final String WC_REVISION_ATTR        = "wcrevision";     // NOI18N        
        private static final String WC_TREE_CONFLICT_ATTR   = "tree-conflicted";// NOI18N
        private static final String CI_REVISION_ATTR        = "reporevision";   // NOI18N        

        private Map<String, String> values;
        private String tag;                

        @Override
        public void startElement(String uri, String localName, String qName, Attributes elementAttributes) throws SAXException {            
            tag = qName.trim();                
            if (ENTRY_ELEMENT_NAME.equals(qName)) {                        
                values = new HashMap<>();
                String path = elementAttributes.getValue(PATH_ATTRIBUTE);
                //path = Paths.get(path).toAbsolutePath().normalize().toString();
                path = VCSFileProxySupport.getResource(files[0], path).normalizeFile().getPath();
                values.put(PATH_ATTRIBUTE,      path);
            } else if (WC_ST_ELEMENT_NAME.equals(qName)) {                                
                values.put(WC_ITEM_ATTR,        elementAttributes.getValue(ITEM_ATTRIBUTE));
                values.put(WC_PROPS_ATTR,       elementAttributes.getValue(PROPS_ATTRIBUTE));
                values.put(WC_REVISION_ATTR,    elementAttributes.getValue(REVISION_ATTRIBUTE));
                values.put(WC_LOCKED_ATTR,      elementAttributes.getValue(WC_LOCKED_ATTRIBUTE));
                values.put(WC_COPIED_ATTR,      elementAttributes.getValue(COPIED_ATTRIBUTE));
                values.put(WC_SWITCHED_ATTR,    elementAttributes.getValue(SWITCHED_ATTRIBUTE));
                values.put(WC_TREE_CONFLICT_ATTR,      elementAttributes.getValue(TREE_CONFLICT_ATTRIBUTE));
            } else if (REPO_ST_ELEMENT_NAME.equals(qName)) {                                
                values.put(REPO_ITEM_ATTR,      elementAttributes.getValue(ITEM_ATTRIBUTE));
                values.put(REPO_PROPS_ATTR,     elementAttributes.getValue(PROPS_ATTRIBUTE));                
            } else if (COMMIT_ELEMENT_NAME.equals(qName)) {                                
                values.put(CI_REVISION_ATTR,    elementAttributes.getValue(REVISION_ATTRIBUTE));                
            }
            if(values != null) {
                values.put(tag, "");
            }                    
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if(values == null || tag == null) {
                return;
            }
            String s = toString(length, ch, start);
            String v = values.get(s);
            if (v == null) {
                values.put(tag, s);
            } else {
                values.put(tag, v + s);
            }
        }                
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {            
            tag = null;
            if (ENTRY_ELEMENT_NAME.equals(qName)) {                                
                if(values != null) {                    
                                    
                    String path = values.get(PATH_ATTRIBUTE);
                    
                    if (values.get(WC_ST_ELEMENT_NAME) == null) {
                        throw new SAXException("'wc-status' tag expected under 'entry'"); //NOI18N
                    }
                            
                    SVNStatusKind wcStatus = SVNStatusKind.fromString(values.get(WC_ITEM_ATTR));
                    SVNStatusKind wcPropsStatus = SVNStatusKind.fromString(values.get(WC_PROPS_ATTR));
                    SVNRevision.Number wcRev = getRevision(values.get(WC_REVISION_ATTR));
                    boolean locked =  getBoolean(values.get(WC_LOCKED_ATTR));       
                    boolean copied =  getBoolean(values.get(WC_COPIED_ATTR));       
                    boolean switched =  getBoolean(values.get(WC_SWITCHED_ATTR));       
                    boolean treeConflict = getBoolean(values.get(WC_TREE_CONFLICT_ATTR));
                    
                    SVNRevision.Number ciRev = getRevision(values.get(CI_REVISION_ATTR));                    
                    String author = values.get(AUTHOR_ELEMENT_NAME);                    
                    Date date = getDate(values.get(DATE_ELEMENT_NAME));
                    
                    String token = null;
                    String owner = null;
                    String lockComment = null;
                    Date lockCreated = null;
                    Date lockExpires = null;
                    if(values.get(LOCK_ELEMENT_NAME) != null) {
                        token = values.get(TOKEN_ELEMENT_NAME);
                        if (token == null) {
                            throw new SAXException("'token' tag expected under 'lock'"); //NOI18N
                        }
                        owner = values.get(OWNER_ELEMENT_NAME);
                        if (owner == null) {
                            throw new SAXException("'owner' tag expected under 'lock'"); //NOI18N
                        }
                        lockComment = values.get(COMMENT_ELEMENT_NAME);
                        lockCreated = getDate(values.get(CREATED_ELEMENT_NAME));
                        lockExpires = getDate(values.get(EXPIRES_ELEMENT_NAME));                    
                    }                 

                    SVNStatusKind repoStatus = SVNStatusKind.NONE;
                    SVNStatusKind repoPropsStatus = SVNStatusKind.NONE;
                    if(values.get(REPO_ST_ELEMENT_NAME) != null) {
                        repoStatus = SVNStatusKind.fromString(values.get(REPO_ITEM_ATTR));
                        repoPropsStatus = SVNStatusKind.fromString(values.get(REPO_PROPS_ATTR));
                    }                    
                    
                    statusValues.add(new Status(VCSFileProxySupport.getResource(files[0], path),
                        wcStatus, wcPropsStatus, wcRev, locked, copied, switched, 
                        ciRev, author, date, owner, lockComment, lockCreated, repoStatus, repoPropsStatus, treeConflict));
                }
                values = null;           
            } 
        }
                
        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }

        public Status[] getStatusValues() {            
            return statusValues != null ? statusValues.toArray(new Status[statusValues.size()]) : new Status[] {} ;
        }

        private boolean getBoolean(String value) {
            return value != null && value.trim().equals("true"); //NOI18N
        }

        private String toString(int length, char[] ch, int start) {
            char[] c = new char[length];
            System.arraycopy(ch, start, c, 0, length);
            return new String(c);
        }

        private Date getDate(String dateValue) {
            Date date = null;
            if (dateValue != null) {
                try {
                    date = new SimpleDateFormat(DATE_FORMAT).parse(dateValue);
                } catch (ParseException ex) {
                    // ignore
                } catch (NumberFormatException ex) {
                    Subversion.LOG.log(Level.INFO, dateValue, ex);
                } catch (Exception ex) {
                    Subversion.LOG.log(Level.SEVERE, "Cannot parse date: " + dateValue, ex); //NOI18N
                }
            }
            return date;
        }
        
        private SVNRevision.Number getRevision(String revisionValue) {
            SVNRevision.Number rev = null;
            if (revisionValue != null && !revisionValue.trim().equals("")) { //NOI18N
                try {
                    rev = new SVNRevision.Number(Long.parseLong(revisionValue));
                } catch (NumberFormatException e) {
                    // ignore
                    rev = new SVNRevision.Number(-1);
                }
            }
            return rev;
        }                
    }       

    public static class Status {
        private final VCSFileProxy path;        
        private final SVNStatusKind wcStatus;
        private final SVNStatusKind wcPropsStatus;
        private final SVNRevision.Number wcRev;
        private final boolean wcLocked;       
        private final boolean wcCopied;       
        private final boolean wcSwitched;                           
        private final boolean treeConflict;
        private final SVNRevision.Number commitRev;                    
        private final String author;                    
        private final Date changeDate;                    
        private final String lockOwner;
        private final String lockComment;
        private final Date lockCreated;
        private final SVNStatusKind repoStatus;
        private final SVNStatusKind repoPropsStatus;    
        public Status(VCSFileProxy path, SVNStatusKind wcStatus, SVNStatusKind wcPropsStatus, 
                SVNRevision.Number wcRev, boolean wcLocked, boolean wcCopied, boolean wcSwitched, 
                SVNRevision.Number commitRev, String author, Date changeDate, String lockOwner, 
                String lockComment, Date lockCreated, SVNStatusKind repoStatus, 
                SVNStatusKind repoPropsStatus, boolean treeConflict)
        {      
            this.path = path;
            this.wcStatus = wcStatus;
            this.wcPropsStatus = wcPropsStatus;
            this.wcRev = wcRev;
            this.wcLocked = wcLocked;
            this.wcCopied = wcCopied;
            this.wcSwitched = wcSwitched;
            this.commitRev = commitRev;
            this.author = author;
            this.changeDate = changeDate;
            this.lockOwner = lockOwner;
            this.lockComment = lockComment;
            this.lockCreated = lockCreated;
            this.repoStatus = repoStatus;
            this.repoPropsStatus = repoPropsStatus;
            this.treeConflict = treeConflict;
        }
        public String getAuthor() {
            return author;
        }
        public Date getChangeDate() {
            return changeDate;
        }
        public SVNRevision.Number getCommitRev() {
            return commitRev;
        }
        public String getLockComment() {
            return lockComment;
        }
        public Date getLockCreated() {
            return lockCreated;
        }
        public String getLockOwner() {
            return lockOwner;
        }
        public VCSFileProxy getPath() {
            return path;
        }
        public SVNStatusKind getRepoPropsStatus() {
            return repoPropsStatus;
        }
        public SVNStatusKind getRepoStatus() {
            return repoStatus;
        }
        public boolean isWcCopied() {
            return wcCopied;
        }
        public boolean isWcLocked() {
            return wcLocked;
        }
        public SVNStatusKind getWcPropsStatus() {
            return wcPropsStatus;
        }
        public SVNRevision.Number getWcRev() {
            return wcRev;
        }
        public SVNStatusKind getWcStatus() {
            return wcStatus;
        }
        public boolean isWcSwitched() {
            return wcSwitched;
        }

        public boolean hasTreeConflicts() {
            return treeConflict;
        }

        public SVNConflictDescriptor getConflictDescriptor() {
            return null;
        }

        @Override
        public String toString() {
            return path+"("+wcStatus+")";
        }
    }
}

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

package org.netbeans.modules.subversion.client.cli.commands;

import java.io.ByteArrayInputStream;
import java.io.File;
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
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.cli.SvnCommand;
import org.openide.xml.XMLUtil;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;
import org.tigris.subversion.svnclientadapter.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Tomas Stupka
 */
public class StatusCommand extends SvnCommand {

    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss Z"; //NOI18N
    private byte[] output;
    
    private final File files[];
    private final boolean getAll;
    private final boolean descend;
    private final boolean checkUpdates;
    private final boolean ignoreExternals;

    public StatusCommand(File[] files, boolean getAll, boolean descend, boolean checkUpdates, boolean ignoreExternals) {
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
    protected int getCommand() {
        return ISVNNotifyListener.Command.STATUS;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {
	arguments.add("status");
        arguments.add("--xml");
        if (getAll) {
            arguments.add("-v");
            arguments.add("--no-ignore"); 
        }
        if (!descend) {
            arguments.add("-N");
        }
        if (checkUpdates) {
            arguments.add("-u");
        }
        if (ignoreExternals) {
            arguments.add("--ignore-externals");
        }
	arguments.add(files);        
    }

    @Override
    public void output(byte[] bytes) {
        output = bytes;
    }
    
    public Status[] getStatusValues() throws SVNClientException {
        if (output == null || output.length == 0) return new Status[0];
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
                        
        private List<Status> statusValues = new ArrayList<Status>();        

        /*
        <!-- For "svn status" -->
        <!ENTITY % BOOL '(true | false) "false"'>

        <!ELEMENT status (target*)>

        <!ELEMENT target (entry*, against?)>
        <!-- path: target path -->
        <!ATTLIST target
          path CDATA #REQUIRED>

        <!ELEMENT entry (wc-status, repos-status?)>
        <!-- path: entry path -->
        <!ATTLIST entry
          path CDATA #REQUIRED>

        <!ELEMENT wc-status (commit?, lock?)>
        <!-- item: item/text status -->
        <!-- props: properites status -->
        <!-- revision: base revision numer -->
        <!-- wc-locked: WC dir locked? -->
        <!-- copied: add with history? -->
        <!-- switched: item switched relative to its parent? -->
        <!ATTLIST wc-status
          item (added | conflicted | deleted | ignored | modified |
          replaced | external | unversioned | incomplete | obstructed |
          normal | none) #REQUIRED
          props (conflicted | modified | normal | none) #REQUIRED
          revision CDATA #IMPLIED
          wc-locked %BOOL;
          copied %BOOL;
          switched %BOOL;
          tree-conflicted %BOOL;
        >

        <!ELEMENT repos-status (lock?)>
        <!-- item: repository status of the item -->
        <!-- props: repository status of the item's properties -->
        <!ATTLIST repos-status
          item (added | deleted | modified | replaced | none) #REQUIRED
          props (modified | none) #REQUIRED
        >

        <!ELEMENT commit (author?, date?)>
        <!-- revision: last committed revision -->
        <!ATTLIST commit revision CDATA #REQUIRED>
        <!ELEMENT author (#PCDATA)>  <!-- author -->
        <!ELEMENT date (#PCDATA)>  <!-- date in ISO format -->
   
        <!-- Lock info stored in WC or repos. -->
        <!ELEMENT lock (token, owner, comment?, created, expires?)>

        <!ELEMENT token (#PCDATA)>    <!-- lock token URI -->
        <!ELEMENT owner (#PCDATA)>    <!-- lock owner -->
        <!ELEMENT comment (#PCDATA)>  <!-- lock comment -->
        <!ELEMENT created (#PCDATA)>  <!-- creation date in ISO format -->
        <!ELEMENT expires (#PCDATA)>  <!-- expiration date in ISO format -->

        <!ELEMENT against EMPTY>
        <!-- revision: revision number at which the repository information was -->
        <!-- obtained -->
        <!ATTLIST against revision CDATA #REQUIRED>        
        */    
        
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
                values = new HashMap<String, String>();
                String path = elementAttributes.getValue(PATH_ATTRIBUTE);
                path = Paths.get(path).toAbsolutePath().normalize().toString();
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
                    
                    if (values.get(WC_ST_ELEMENT_NAME) == null) throw new SAXException("'wc-status' tag expected under 'entry'");
                            
                    SVNStatusKind wcStatus = SVNStatusKind.fromString(values.get(WC_ITEM_ATTR));
                    SVNStatusKind wcPropsStatus = SVNStatusKind.fromString(values.get(WC_PROPS_ATTR));
                    Number wcRev = getRevision(values.get(WC_REVISION_ATTR));
                    boolean locked =  getBoolean(values.get(WC_LOCKED_ATTR));       
                    boolean copied =  getBoolean(values.get(WC_COPIED_ATTR));       
                    boolean switched =  getBoolean(values.get(WC_SWITCHED_ATTR));       
                    boolean treeConflict = getBoolean(values.get(WC_TREE_CONFLICT_ATTR));
                    
                    Number ciRev = getRevision(values.get(CI_REVISION_ATTR));                    
                    String author = values.get(AUTHOR_ELEMENT_NAME);                    
                    Date date = getDate(values.get(DATE_ELEMENT_NAME));
                    
                    String token = null;
                    String owner = null;
                    String lockComment = null;
                    Date lockCreated = null;
                    Date lockExpires = null;
                    if(values.get(LOCK_ELEMENT_NAME) != null) {
                        token = values.get(TOKEN_ELEMENT_NAME);
                        if (token == null) throw new SAXException("'token' tag expected under 'lock'");
                        owner = values.get(OWNER_ELEMENT_NAME);
                        if (owner == null) throw new SAXException("'owner' tag expected under 'lock'");
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
                    
                    statusValues.add(new Status(
                        path, wcStatus, wcPropsStatus, wcRev, locked, copied, switched, 
                        ciRev, author, date, owner, lockComment, lockCreated, repoStatus, repoPropsStatus, treeConflict));
                }
                values = null;           
            } 
        }
                
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }

        public Status[] getStatusValues() {            
            return statusValues != null ? statusValues.toArray(new Status[0]) : new Status[] {} ;
        }

        private boolean getBoolean(String value) {
            return value != null && value.trim().equals("true");
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
                    Subversion.LOG.log(Level.SEVERE, "Cannot parse date: " + dateValue, ex);
                }
            }
            return date;
        }
        
        private Number getRevision(String revisionValue) {
            Number rev = null;
            if (revisionValue != null && !revisionValue.trim().equals("")) {
                try {
                    rev = new SVNRevision.Number(Long.parseLong(revisionValue));
                } catch (NumberFormatException e) {
                    // ignore
                    new SVNRevision.Number(-1);
                }
            }
            return rev;
        }                
    }       

    public class Status {
        private final String path;        
        private final SVNStatusKind wcStatus;
        private final SVNStatusKind wcPropsStatus;
        private final Number wcRev;
        private final boolean wcLocked;       
        private final boolean wcCopied;       
        private final boolean wcSwitched;                           
        private final boolean treeConflict;
        private final Number commitRev;                    
        private final String author;                    
        private final Date changeDate;                    
        private final String lockOwner;
        private final String lockComment;
        private final Date lockCreated;
        private final SVNStatusKind repoStatus;
        private final SVNStatusKind repoPropsStatus;        
        public Status(String path, SVNStatusKind wcStatus, SVNStatusKind wcPropsStatus, 
                Number wcRev, boolean wcLocked, boolean wcCopied, boolean wcSwitched, 
                Number commitRev, String author, Date changeDate, String lockOwner, 
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
        public Number getCommitRev() {
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
        public String getPath() {
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
        public Number getWcRev() {
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
    }
}

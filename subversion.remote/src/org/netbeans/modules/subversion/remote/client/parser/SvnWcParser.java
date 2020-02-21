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
package org.netbeans.modules.subversion.remote.client.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.ISVNInfo;
import org.netbeans.modules.subversion.remote.api.ISVNStatus;
import org.netbeans.modules.subversion.remote.api.SVNConflictDescriptor;
import org.netbeans.modules.subversion.remote.api.SVNNodeKind;
import org.netbeans.modules.subversion.remote.api.SVNScheduleKind;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.xml.sax.SAXException;

/**
 *
 * 
 */
public class SvnWcParser {
    
    /** Creates a new instance of SvnWcParser */ 
    public SvnWcParser() {
    }

    private WorkingCopyDetails getWCDetails(VCSFileProxy file) throws IOException, SAXException {   
        Map<String, String> attributes = EntriesCache.getInstance().getFileAttributes(file);
        return WorkingCopyDetails.createWorkingCopy(file, attributes);
    }

   /**
     * 
     */ 
    public ISVNStatus[] getStatus(VCSFileProxy path, boolean descend, boolean getAll) throws LocalSubversionException {        
        List<ISVNStatus> l = getStatus(path, descend);
        return l.toArray(new ISVNStatus[l.size()]);
    }

    private List<ISVNStatus> getStatus(VCSFileProxy path, boolean descend) throws LocalSubversionException {
        List<ISVNStatus> ret = new ArrayList<>(20);                        
        ret.add(getSingleStatus(path));
        
        VCSFileProxy[] children = getChildren(path);
        if(children != null) {
            for (int i = 0; i < children.length; i++) {
                if(!SvnUtils.isPartOfSubversionMetadata(children[i]) && !SvnUtils.isAdministrative(path)) {                                       
                    if(descend && children[i].isDirectory()) {                
                        ret.addAll(getStatus(children[i], descend));                
                    } else {
                        ret.add(getSingleStatus(children[i]));  // 
                    }                   
                }
            }        
        }        
        return ret;
    }

    /**
     * Returns an array of existed file's children plus all it's children from metadata
     */
    private VCSFileProxy[] getChildren (VCSFileProxy file) throws LocalSubversionException {
        VCSFileProxy[] children = file.listFiles();
        if (children != null) { // it is a folder, get all its children from metadata
            try {
                String[] entries = EntriesCache.getInstance().getChildren(file);
                Set<VCSFileProxy> childSet = new LinkedHashSet<>(children.length + entries.length);
                childSet.addAll(Arrays.asList(children));
                for (String name : entries) {
                    childSet.add(VCSFileProxy.createFileProxy(file, name));
                }
                children = childSet.toArray(new VCSFileProxy[childSet.size()]);
            } catch (IOException ex) {
                throw new LocalSubversionException(ex);
            } catch (SAXException ex) {
                throw new LocalSubversionException(ex);
            }
        }
        return children;
    }

    public ISVNStatus getSingleStatus(VCSFileProxy file) throws LocalSubversionException {
        SVNStatusKind finalTextStatus = SVNStatusKind.NORMAL;
        SVNStatusKind finalPropStatus = SVNStatusKind.NONE;

        try {
            WorkingCopyDetails wcDetails = getWCDetails(file);
            if (wcDetails.isHandled()) {               
                if (wcDetails.propertiesExist() ||                    // we either have some properties,
                    (wcDetails.getBasePropertiesFile() != null &&     // or there were some 
                     wcDetails.getBasePropertiesFile().exists()))    
                {
                    finalPropStatus = SVNStatusKind.NORMAL;
                    //See if props have been modified
                    if (wcDetails.propertiesModified()) {
                        finalPropStatus = SVNStatusKind.MODIFIED;
                    }
                }                
                if (wcDetails.isFile()) {
                    //Find Text Status
                    // XXX what if already added
                    if (wcDetails.textModified()) {
                        finalTextStatus = SVNStatusKind.MODIFIED;
                    }
                } 

                String value = wcDetails.getValue("schedule");  // NOI18N
                if (value != null) {
                    if (value.equals("add")) {  // NOI18N
                        finalTextStatus = SVNStatusKind.ADDED;
                        finalPropStatus = SVNStatusKind.NONE;
                    } else if (value.equals("delete")) {  // NOI18N
                        finalTextStatus = SVNStatusKind.DELETED;
                        finalPropStatus = SVNStatusKind.NONE;
                    } else if (value.equals("replace")) {  // NOI18N
                        finalTextStatus = SVNStatusKind.REPLACED;
                        finalPropStatus = SVNStatusKind.NONE;
                    }
                }
                
                // what if the file does not exist and is not deleted?
                // now status can be NORMAL, MODIFIED, ADDED, REPLACED, DELETED
                // it is missing then
                assert finalTextStatus.equals(SVNStatusKind.NORMAL)
                        || finalTextStatus.equals(SVNStatusKind.MODIFIED)
                        || finalTextStatus.equals(SVNStatusKind.ADDED)
                        || finalTextStatus.equals(SVNStatusKind.REPLACED)
                        || finalTextStatus.equals(SVNStatusKind.DELETED);
                if (!SVNStatusKind.DELETED.equals(finalTextStatus) && !file.exists() && !isUnderParent(file)) {
                    finalTextStatus = SVNStatusKind.MISSING;
                }

                value = wcDetails.getValue("deleted");  // NOI18N
                if (value != null) {
                    if (value.equals("true")) {  // NOI18N
                        finalTextStatus = SVNStatusKind.UNVERSIONED;
                        finalPropStatus = SVNStatusKind.NONE;
                    }
                }    

                String fileUrl = wcDetails.getValue("url");          // NOI18N        
                long revision = wcDetails.getLongValue("revision");             // NOI18N                         
                String nodeKind = wcDetails.getValue("kind", "normal");                  // NOI18N
                String lastCommitAuthor = wcDetails.getValue("last-author");  // NOI18N
                long lastChangedRevision = wcDetails.getLongValue("committed-rev");  // NOI18N
                Date lastCommittedDate = wcDetails.getDateValue("committed-date");              // NOI18N    

                boolean isCopied = wcDetails.getBooleanValue("copied");  // NOI18N
                String urlCopiedFrom = null;
                if (isCopied) {                                        
                    urlCopiedFrom = wcDetails.getValue("copyfrom-url");  // NOI18N  
                }

                VCSFileProxy conflictNew = null;
                VCSFileProxy conflictOld = null;
                VCSFileProxy conflictWorking = null;
                value = wcDetails.getValue("conflict-wrk");  // NOI18N
                if (value != null && value.length() > 0) {
                    conflictWorking = VCSFileProxy.createFileProxy(file.getParentFile(), value);
                }

                value = wcDetails.getValue("conflict-new");  // NOI18N
                if (value != null && value.length() > 0) {
                    conflictNew = VCSFileProxy.createFileProxy(file.getParentFile(), value);
                }
                value = wcDetails.getValue("conflict-old");  // NOI18N
                if (value != null && value.length() > 0) {
                    conflictOld = VCSFileProxy.createFileProxy(file.getParentFile(), value);
                }
                if ((conflictNew != null) || (conflictOld != null)) {
                    finalTextStatus = SVNStatusKind.CONFLICTED;                
                }

                Date lockCreationDate = wcDetails.getDateValue("lock-creation-date");  // NOI18N
                String lockComment = null;
                String lockOwner = null;
                if (lockCreationDate != null) {                        
                    lockComment = wcDetails.getValue("lock-comment");  // NOI18N
                    lockOwner = wcDetails.getValue("lock-owner");      // NOI18N
                }
                SVNConflictDescriptor conflictDesc = wcDetails.getConflictDescriptor();

                return new ParserSvnStatus(
                        file,
                        fileUrl,
                        revision,
                        nodeKind,
                        finalTextStatus,
                        finalPropStatus,
                        lastCommitAuthor,
                        lastChangedRevision,
                        lastCommittedDate,
                        isCopied,
                        urlCopiedFrom,
                        conflictNew,
                        conflictOld,
                        conflictWorking,
                        lockCreationDate,
                        lockComment,
                        lockOwner,
                        conflictDesc != null,
                        conflictDesc);
            } else {
                //File isn't handled.
                return new ParserSvnStatus(
                        file,                                
                        wcDetails.getValue("url"),            // NOI18N
                        0,
                        "unknown",                            // NOI18N   
                        SVNStatusKind.UNVERSIONED,
                        SVNStatusKind.UNVERSIONED,
                        null,
                        0,
                        null,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        false,
                        null);
            }

        } catch (IOException ex) {
            throw new LocalSubversionException(ex);
        } catch (SAXException ex) {
            throw new LocalSubversionException(ex);
        } catch (IllegalArgumentException ex) {
            throw new LocalSubversionException(ex);
        }
    }

    private boolean isUnderParent (VCSFileProxy file) {
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.fine("SvnWcParser:isUnderParent: 168248 hook");  //NOI18N
        }
        VCSFileProxy parentFile = file.getParentFile();
        if (parentFile != null) {
            VCSFileProxy[] children = parentFile.listFiles();
            if (children != null) {
                for (VCSFileProxy child : children) {
                    if (file.equals(child)) {
                        if (Subversion.LOG.isLoggable(Level.FINE)) {
                            Subversion.LOG.fine("SvnWcParser:isUnderParent: file " + file.getPath() + " seems to be a broken link"); //NOI18N
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ISVNInfo getInfoFromWorkingCopy(VCSFileProxy file) throws LocalSubversionException {

        ISVNInfo returnValue = null;
        try {
            WorkingCopyDetails wcDetails = getWCDetails(file);  // NOI18N
            if (wcDetails.isHandled()) {
                String fileUrl = wcDetails.getValue("url");               // NOI18N   
                String reposUrl = wcDetails.getValue("repos");  // NOI18N
                String reposUuid = wcDetails.getValue("uuid");  // NOI18N
                String schedule = wcDetails.getValue("schedule");  // NOI18N
                if (schedule == null) {
                    schedule = SVNScheduleKind.NORMAL.toString();
                }

                long revision = wcDetails.getLongValue("revision");     // NOI18N                
                boolean isCopied = wcDetails.getBooleanValue("copied");  // NOI18N
                String urlCopiedFrom = null;
                long revisionCopiedFrom = 0;
                if (isCopied) {
                    urlCopiedFrom = wcDetails.getValue("copyfrom-url");  // NOI18N
                    revisionCopiedFrom = wcDetails.getLongValue("copyfrom-rev");      // NOI18N               
                } 

                Date lastCommittedDate = wcDetails.getDateValue("committed-date");  // NOI18N
                long lastChangedRevision = wcDetails.getLongValue("committed-rev");     // NOI18N             
                String lastCommitAuthor = wcDetails.getValue("last-author");        // NOI18N          
                Date lastDatePropsUpdate = wcDetails.getDateValue("prop-time");     // NOI18N                          
                Date lastDateTextUpdate = wcDetails.getDateValue("text-time");  // NOI18N

                Date lockCreationDate = wcDetails.getDateValue("lock-creation-date");  // NOI18N
                String lockComment = null;
                String lockOwner = null;                
                if (lockCreationDate != null) {                    
                    lockComment = wcDetails.getValue("lock-comment");  // NOI18N
                    lockOwner = wcDetails.getValue("lock-owner");  // NOI18N
                }

                String nodeKind = wcDetails.getValue("kind", "normal");     // NOI18N             
                returnValue = new ParserSvnInfo(file, fileUrl, reposUrl, reposUuid,
                    SVNScheduleKind.fromString(schedule), revision, isCopied, urlCopiedFrom, revisionCopiedFrom,
                    lastCommittedDate, lastChangedRevision, lastCommitAuthor,
                    lastDatePropsUpdate, lastDateTextUpdate, lockCreationDate,
                    lockOwner, lockComment, SVNNodeKind.fromString(nodeKind), wcDetails.getPropertiesFile(), wcDetails.getBasePropertiesFile());
            } else {
                String fileUrl = wcDetails.getValue("url");  // NOI18N
                String reposUrl = wcDetails.getValue("repos");  // NOI18N
                String reposUuid = wcDetails.getValue("uuid");  // NOI18N
                returnValue = new ParserSvnInfo(file, fileUrl, reposUrl, reposUuid,
                    SVNScheduleKind.NORMAL, 0, false, null, 0, null, 0, null,
                    null, null, null, null, null, SVNNodeKind.UNKNOWN, null, null);
            }
        } catch (IOException ex) {
            throw new LocalSubversionException(ex);
        } catch (SAXException ex) {
            throw new LocalSubversionException(ex);
        }
        return returnValue;
    }

    public ISVNInfo getUnknownInfo (VCSFileProxy file) {
        return new ParserSvnInfo(file, null, null, null,
                    SVNScheduleKind.NORMAL, 0, false, null, 0, null, 0, null,
                    null, null, null, null, null, SVNNodeKind.UNKNOWN, null, null);
    }
    
}


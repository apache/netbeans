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
package org.netbeans.modules.subversion.client.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNConflictDescriptor;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNScheduleKind;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.xml.sax.SAXException;

/**
 *
 * @author Ed Hillmann
 */
public class SvnWcParser {
    
    /** Creates a new instance of SvnWcParser */ 
    public SvnWcParser() {
    }

    private WorkingCopyDetails getWCDetails(File file) throws IOException, SAXException {   
        Map<String, String> attributes = EntriesCache.getInstance().getFileAttributes(file);
        return WorkingCopyDetails.createWorkingCopy(file, attributes);
    }

   /**
     * 
     */ 
    public ISVNStatus[] getStatus(File path, boolean descend, boolean getAll) throws LocalSubversionException {        
        List<ISVNStatus> l = getStatus(path, descend);
        return l.toArray(new ISVNStatus[l.size()]);
    }

    private List<ISVNStatus> getStatus(File path, boolean descend) throws LocalSubversionException {
        List<ISVNStatus> ret = new ArrayList<ISVNStatus>(20);                        
        ret.add(getSingleStatus(path));
        
        File[] children = getChildren(path);
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
    private File[] getChildren (File file) throws LocalSubversionException {
        File[] children = file.listFiles();
        if (children != null) { // it is a folder, get all its children from metadata
            try {
                String[] entries = EntriesCache.getInstance().getChildren(file);
                Set<File> childSet = new LinkedHashSet<File>(children.length + entries.length);
                childSet.addAll(Arrays.asList(children));
                for (String name : entries) {
                    childSet.add(new File(file, name));
                }
                children = childSet.toArray(new File[childSet.size()]);
            } catch (IOException ex) {
                throw new LocalSubversionException(ex);
            } catch (SAXException ex) {
                throw new LocalSubversionException(ex);
            }
        }
        return children;
    }

    public ISVNStatus getSingleStatus(File file) throws LocalSubversionException {
        String finalTextStatus = SVNStatusKind.NORMAL.toString();
        String finalPropStatus = SVNStatusKind.NONE.toString();

        try {
            WorkingCopyDetails wcDetails = getWCDetails(file);
            if (wcDetails.isHandled()) {               
                if (wcDetails.propertiesExist() ||                    // we either have some properties,
                    (wcDetails.getBasePropertiesFile() != null &&     // or there were some 
                     wcDetails.getBasePropertiesFile().exists()))    
                {
                    finalPropStatus = SVNStatusKind.NORMAL.toString();
                    //See if props have been modified
                    if (wcDetails.propertiesModified()) {
                        finalPropStatus = SVNStatusKind.MODIFIED.toString();
                    }
                }                
                if (wcDetails.isFile()) {
                    //Find Text Status
                    // XXX what if already added
                    if (wcDetails.textModified()) {
                        finalTextStatus = SVNStatusKind.MODIFIED.toString();
                    }
                } 

                String value = wcDetails.getValue("schedule");  // NOI18N
                if (value != null) {
                    if (value.equals("add")) {  // NOI18N
                        finalTextStatus = SVNStatusKind.ADDED.toString();
                        finalPropStatus = SVNStatusKind.NONE.toString();
                    } else if (value.equals("delete")) {  // NOI18N
                        finalTextStatus = SVNStatusKind.DELETED.toString();
                        finalPropStatus = SVNStatusKind.NONE.toString();
                    } else if (value.equals("replace")) {  // NOI18N
                        finalTextStatus = SVNStatusKind.REPLACED.toString();
                        finalPropStatus = SVNStatusKind.NONE.toString();
                    }
                }
                
                // what if the file does not exist and is not deleted?
                // now status can be NORMAL, MODIFIED, ADDED, REPLACED, DELETED
                // it is missing then
                assert finalTextStatus.equals(SVNStatusKind.NORMAL.toString())
                        || finalTextStatus.equals(SVNStatusKind.MODIFIED.toString())
                        || finalTextStatus.equals(SVNStatusKind.ADDED.toString())
                        || finalTextStatus.equals(SVNStatusKind.REPLACED.toString())
                        || finalTextStatus.equals(SVNStatusKind.DELETED.toString());
                if (!SVNStatusKind.DELETED.toString().equals(finalTextStatus) && !file.exists() && !isUnderParent(file)) {
                    finalTextStatus = SVNStatusKind.MISSING.toString();
                }

                value = wcDetails.getValue("deleted");  // NOI18N
                if (value != null) {
                    if (value.equals("true")) {  // NOI18N
                        finalTextStatus = SVNStatusKind.UNVERSIONED.toString();
                        finalPropStatus = SVNStatusKind.NONE.toString();
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

                File conflictNew = null;
                File conflictOld = null;
                File conflictWorking = null;
                value = wcDetails.getValue("conflict-wrk");  // NOI18N
                if (value != null && ((String)value).length() > 0) {
                    conflictWorking = new File(file.getParentFile(), value);
                }

                value = wcDetails.getValue("conflict-new");  // NOI18N
                if (value != null && ((String)value).length() > 0) {
                    conflictNew = new File(file.getParentFile(), value);
                }
                value = wcDetails.getValue("conflict-old");  // NOI18N
                if (value != null && ((String)value).length() > 0) {
                    conflictOld = new File(file.getParentFile(), value);
                }
                if ((conflictNew != null) || (conflictOld != null)) {
                    finalTextStatus = SVNStatusKind.CONFLICTED.toString();                
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
                        SVNStatusKind.UNVERSIONED.toString(),
                        SVNStatusKind.UNVERSIONED.toString(),
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

    private boolean isUnderParent (File file) {
        Subversion.LOG.fine("SvnWcParser:isUnderParent: 168248 hook");  //NOI18N
        File parentFile = file.getParentFile();
        if (parentFile != null) {
            File[] children = parentFile.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (file.equals(child)) {
                        Subversion.LOG.fine("SvnWcParser:isUnderParent: file " + file.getAbsolutePath() + " seems to be a broken link"); //NOI18N
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ISVNInfo getInfoFromWorkingCopy(File file) throws LocalSubversionException {

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
                    schedule, revision, isCopied, urlCopiedFrom, revisionCopiedFrom,
                    lastCommittedDate, lastChangedRevision, lastCommitAuthor,
                    lastDatePropsUpdate, lastDateTextUpdate, lockCreationDate,
                    lockOwner, lockComment, nodeKind, wcDetails.getPropertiesFile(), wcDetails.getBasePropertiesFile());
            } else {
                String fileUrl = wcDetails.getValue("url");  // NOI18N
                String reposUrl = wcDetails.getValue("repos");  // NOI18N
                String reposUuid = wcDetails.getValue("uuid");  // NOI18N
                returnValue = new ParserSvnInfo(file, fileUrl, reposUrl, reposUuid,
                    SVNScheduleKind.NORMAL.toString(), 0, false, null, 0, null, 0, null,
                    null, null, null, null, null, SVNNodeKind.UNKNOWN.toString(), null, null);
            }
        } catch (IOException ex) {
            throw new LocalSubversionException(ex);
        } catch (SAXException ex) {
            throw new LocalSubversionException(ex);
        }
        return returnValue;
    }

    public ISVNInfo getUnknownInfo (File file) {
        return new ParserSvnInfo(file, null, null, null,
                    SVNScheduleKind.NORMAL.toString(), 0, false, null, 0, null, 0, null,
                    null, null, null, null, null, SVNNodeKind.UNKNOWN.toString(), null, null);
    }
    
}


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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.subversion.client.cli.SvnCommand;
import org.netbeans.modules.subversion.client.cli.SvnCommand.Arguments;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;
import org.tigris.subversion.svnclientadapter.SVNScheduleKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class InfoCommand extends SvnCommand {

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss Z");

    private enum InfoType {
        files,
        url
    }
    
    private List<String> output = new ArrayList<String>();
    private final SVNUrl url;
    private final File[] files;
    private final SVNRevision revision;
    private final SVNRevision pegging;

    private final InfoType type;
    
    public InfoCommand(SVNUrl url, SVNRevision revision, SVNRevision pegging) {
        this.url = url;
        this.revision = revision;
        this.pegging = pegging;
        
        files = null;
        
        type = InfoType.url;
    }
    
    public InfoCommand(File[] files, SVNRevision revision, SVNRevision pegging) {
        this.files = files;
        this.revision = revision;
        this.pegging = pegging;
        
        url = null;
        
        type = InfoType.files;
    }
    
    @Override
    protected boolean notifyOutput() {
        return false;
    }    
    
    @Override
    protected int getCommand() {
        return ISVNNotifyListener.Command.INFO;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {
        arguments.add("info");
        // XXX arguments.add("--xml");           
        if(revision != null) { 
            arguments.add(revision);
        }   
        switch(type) {
            case url :
                arguments.add(url, pegging);        
                break;
            case files:
                arguments.addFileArguments(files);               
                // XXX peg unsupported
                break;
            default:
                throw new IllegalStateException("Unsupported infotype: " + type);    
        }        
    }

    @Override
    public void outputText(String lineString) {
        if(lineString == null || lineString.trim().equals("")) {
            return;
        }
        output.add(lineString);
        super.outputText(lineString);
    }
    
    public ISVNInfo[] getInfo() throws SVNClientException {
        List<Info> infos = new ArrayList<Info>();        
        
        Map<String, String> map = null;
        
        StringBuffer comment = new StringBuffer();
        for (int i = 0; i < output.size(); i++) {

            String outputLine = output.get(i);
            if(outputLine == null || outputLine.trim().equals("")) {
                continue;
            }
            
            if(outputLine.startsWith("Path:")) {
                if(map != null) {
                    infos.add(new Info(map));            
                }
                map = new HashMap<String, String>();
            }
            
            int idx = outputLine.indexOf(':');            
            String info = outputLine.substring(0, idx);
            if (info.startsWith(INFO_LOCK_COMMENT)) {
                // comment is the last one, so let's finnish this
                while( ++i < output.size()) {                    
                    comment.append(output.get(i));
                    comment.append('\n');    
                }
                map.put(INFO_LOCK_COMMENT, comment.toString());
            }
            
            String infoValue = outputLine.substring(idx + 1);
            map.put(info, infoValue.trim());                                
        }
        if(map != null) {
            infos.add(new Info(map));
        }
        if (infos.size() == 0 && url != null) {
            org.netbeans.modules.subversion.Subversion.LOG.warning("InfoCommand: Map is null for: " + output);
        }
        return infos.toArray(new Info[0]);
    }
    
    private class Info implements ISVNInfo {
        private final Map<String, String> infoMap;

        public Info(Map<String, String> infoMap) {
            this.infoMap = infoMap;
        }
                        
	public SVNRevision.Number getRevision() {
            return getNumber(infoMap.get(INFO_REVISION));
	}
        
	public Date getLastDateTextUpdate() {
            return getDate(infoMap.get(INFO_TEXT_LAST_UPDATED));	
	}

	public String getUuid() {
            return infoMap.get(INFO_REPOSITORY_UUID);
	}

	public SVNUrl getRepository() {
            return getSVNUrl(infoMap.get(INFO_REPOSITORY));
	}

	public SVNScheduleKind getSchedule() {
            return SVNScheduleKind.fromString(infoMap.get(INFO_SCHEDULE));
	}

	public Date getLastDatePropsUpdate() {
            return getDate(infoMap.get(INFO_PROPS_LAST_UPDATED));
	}

	public boolean isCopied() {
            return (getCopyRev() != null) || (getCopyUrl() != null);
	}

	public Number getCopyRev() {
            return getNumber(infoMap.get(INFO_COPIED_FROM_REV));
	}

	public SVNUrl getCopyUrl() {
            return getSVNUrl(infoMap.get(INFO_COPIED_FROM_URL));
	}

        public Date getLockCreationDate() {
            return getDate(infoMap.get(INFO_LOCK_CREATION_DATE));
        }

        public String getLockOwner() {
            return infoMap.get(INFO_LOCK_OWNER);
        }

        public String getLockComment() {
            return infoMap.get(INFO_LOCK_COMMENT);
        }

        public File getConflictNew() {
            String path = infoMap.get(INFO_CONFLICT_CURRENT_BASE);
            return (path != null)? new File(getFile().getParent(), path).getAbsoluteFile() : null;
        }

        public File getConflictOld() {
            String path = infoMap.get(INFO_CONFLICT_PREVIOUS_BASE);
            return (path != null)? new File(getFile().getParent(), path).getAbsoluteFile() : null;
        }

        public File getConflictWorking() {
            String path = infoMap.get(INFO_CONFLICT_PREVIOUS_WORKING);
            return (path != null) ? new File(getFile().getParent(), path).getAbsoluteFile() : null;
        }
        
	public String getPath() {
            return infoMap.get(INFO_PATH);
	}
        
        public File getFile() {
            return new File(getPath()).getAbsoluteFile();
        }

	public SVNUrl getUrl() {
            return getSVNUrl(infoMap.get(INFO_URL));
	}

	public String getUrlString() {
            return infoMap.get(INFO_URL);
	}
        
	public Date getLastChangedDate() {
            return getDate(infoMap.get(INFO_LAST_CHANGED_DATE));
	}

	public SVNRevision.Number getLastChangedRevision() {
            return getNumber(infoMap.get(INFO_LAST_CHANGED_REVISION));
	}

	public String getLastCommitAuthor() {
            return infoMap.get(INFO_LAST_CHANGED_AUTHOR);
	}

	public SVNNodeKind getNodeKind() {
            return SVNNodeKind.fromString(infoMap.get(INFO_NODEKIND));
	}

        public int getDepth() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

//    Path: file
//    Name: file
//    URL: file:///data/work/src/netbeans-61/subversion/build/test/unit/data/repo/testInfoFile_wc/file
//    Repository Root: file:///data/work/src/netbeans-61/subversion/build/test/unit/data/repo
//    Repository UUID: 2daef3d9-18e1-4da2-9531-b29b2c99e754
//    Revision: 111
//    Node Kind: file
//    Last Changed Author: tomas
//    Last Changed Rev: 111
//    Last Changed Date: 2008-04-22 18:41:05 +0200 (Tue, 22 Apr 2008)

            
    private static final String INFO_PATH                       = "Path";                   // NOI18N
    private static final String INFO_URL                        = "URL";                    // NOI18N
    private static final String INFO_REVISION                   = "Revision";               // NOI18N
    private static final String INFO_REPOSITORY                 = "Repository Root";        // NOI18N
    private static final String INFO_NODEKIND                   = "Node Kind";              // NOI18N
    private static final String INFO_LAST_CHANGED_AUTHOR        = "Last Changed Author";    // NOI18N
    private static final String INFO_LAST_CHANGED_REVISION      = "Last Changed Rev";       // NOI18N
    private static final String INFO_LAST_CHANGED_DATE          = "Last Changed Date";      // NOI18N
    private static final String INFO_TEXT_LAST_UPDATED          = "Text Last Updated";      // NOI18N
    private static final String INFO_SCHEDULE                   = "Schedule";               
    private static final String INFO_COPIED_FROM_URL            = "Copied From URL";
    private static final String INFO_COPIED_FROM_REV            = "Copied From Rev";
    private static final String INFO_PROPS_LAST_UPDATED         = "Properties Last Updated";
    private static final String INFO_REPOSITORY_UUID            = "Repository UUID";        // NOI18N
    private static final String INFO_LOCK_OWNER                 = "Lock Owner";             // NOI18N    
    private static final String INFO_LOCK_CREATION_DATE         = "Lock Created";           // NOI18N
    private static final String INFO_LOCK_COMMENT               = "Lock Comment";           // NOI18N    

    private static final String INFO_CONFLICT_PREVIOUS_BASE     = "Conflict Previous Base File";
    private static final String INFO_CONFLICT_PREVIOUS_WORKING  = "Conflict Previous Working File";
    private static final String INFO_CONFLICT_CURRENT_BASE      = "Conflict Current Base File";
    
    private SVNUrl getSVNUrl(String url) {
        try {
            return new SVNUrl(url);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private SVNRevision.Number getNumber(String revision) {
        if (revision == null) {
            return null;
        }            
        try {
            return new SVNRevision.Number(Long.parseLong(revision));
        } catch (NumberFormatException e) {
            return new SVNRevision.Number(-1);
        }
    }

    private Date getDate(String date) {
        if (date == null){
            return null;   
        }            
        try {
            return dateFormat.parse(date);
        } catch (ParseException e1) {
            return null;
        }
    }

}

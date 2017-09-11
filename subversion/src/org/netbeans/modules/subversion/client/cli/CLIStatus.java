/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.client.cli;

import java.io.File;
import java.util.Date;
import org.netbeans.modules.subversion.client.cli.commands.StatusCommand.Status;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNConflictDescriptor;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNScheduleKind;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;

/**
 *
 * @author Tomas Stupka
 */
public class CLIStatus implements ISVNStatus {
    private Status status;
    private ISVNInfo info;
	
    CLIStatus(Status status, ISVNInfo info) {
        this.status = status;
        this.info = info;
    }
    
    CLIStatus(Status status, String path) {
        this.status = status;
        this.info = new UnversionedInfo(path);
    }

    public SVNUrl getUrl() {
        return info.getUrl();
    }

    public String getUrlString() {
        return info.getUrlString();
    }

    public Number getLastChangedRevision() {
        return info.getLastChangedRevision();
    }

    public Date getLastChangedDate() {
        return info.getLastChangedDate();
    }

    public String getLastCommitAuthor() {
        return info.getLastCommitAuthor();
    }

    public SVNStatusKind getTextStatus() {
        return status.getWcStatus();
    }

    public SVNStatusKind getRepositoryTextStatus() {
        return status.getRepoStatus();
    }

    public SVNStatusKind getPropStatus() {
        return status.getWcPropsStatus();
    }

    public SVNStatusKind getRepositoryPropStatus() {
        return status.getRepoPropsStatus();
    }

    public Number getRevision() {
        return info.getRevision(); 
    }

    public String getPath() {
        return info != null ? info.getFile().getAbsolutePath() : status.getPath();
    }

    public File getFile() {
        return info.getFile();
    }

    public SVNNodeKind getNodeKind() {
        return info.getNodeKind();
    }

    public boolean isCopied() {
        return status.isWcCopied();
    }

    public boolean isWcLocked() {
        return status.isWcLocked();
    }

    public boolean isSwitched() {
        return status.isWcSwitched();
    }

    public SVNUrl getUrlCopiedFrom() {
        return info.getCopyUrl();
    }

    public File getConflictNew() {
        return null;
    }

    public File getConflictOld() {
        return null;
    }

    public File getConflictWorking() {
        return null;
    }

    public String getLockOwner() {
        return status.getLockOwner();
    }

    public Date getLockCreationDate() {
        return status.getLockCreated();
    }

    public String getLockComment() {
        return status.getLockComment();
    }

    @Override
    public boolean hasTreeConflict() {
        return status.hasTreeConflicts();
    }

    @Override
    public SVNConflictDescriptor getConflictDescriptor() {
        return status.getConflictDescriptor();
    }

    public boolean isFileExternal() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getMovedFromAbspath () {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getMovedToAbspath () {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class UnversionedInfo implements ISVNInfo {
        private final File file;
        public UnversionedInfo(String path) {
            this.file = new File(path);
        }        
        public File getFile() {
            return file;
        }
        public SVNUrl getUrl() {
            return null;
        }
        public String getUrlString() {
            return null;
        }
        public String getUuid() {
            return null;
        }
        public SVNUrl getRepository() {
            return null;
        }
        public SVNScheduleKind getSchedule() {
            return null;
        }
        public SVNNodeKind getNodeKind() {
            return SVNNodeKind.UNKNOWN;
        }
        public String getLastCommitAuthor() {
            return null;
        }
        public Number getRevision() {
            return null;
        }
        public Number getLastChangedRevision() {
            return null;
        }
        public Date getLastChangedDate() {
            return null;
        }
        public Date getLastDateTextUpdate() {
            return null;
        }
        public Date getLastDatePropsUpdate() {
            return null;
        }
        public boolean isCopied() {
            return false;
        }
        public Number getCopyRev() {
            return SVNRevision.INVALID_REVISION;
        }
        public SVNUrl getCopyUrl() {
            return null;
        }
        public String getLockOwner() {
            return null;
        }
        public Date getLockCreationDate() {
            return null;
        }
        public String getLockComment() {
            return null;
        }

        public int getDepth() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
            
}

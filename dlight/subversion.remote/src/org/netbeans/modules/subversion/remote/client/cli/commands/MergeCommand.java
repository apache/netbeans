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

import java.io.IOException;
import org.netbeans.modules.subversion.remote.api.ISVNNotifyListener;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.cli.SvnCommand;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;

/**
 *
 * 
 */
public class MergeCommand extends SvnCommand {
    
    private final boolean rec;
    private final boolean force;
    private final boolean ignoreAncestry;
    private final boolean dry;
    private final SVNUrl startUrl;
    private final SVNUrl endUrl;
    private final SVNRevision startRev;
    private final SVNRevision endRev;
    private final VCSFileProxy file;

    public MergeCommand(FileSystem fileSystem, SVNUrl startUrl, SVNUrl endUrl, SVNRevision startRev, SVNRevision endRev, VCSFileProxy file, boolean rec, boolean force, boolean ignoreAncestry, boolean dry) {
        super(fileSystem);
        this.rec = rec;
        this.force = force;
        this.ignoreAncestry = ignoreAncestry;
        this.dry = dry;
        this.startUrl = startUrl;
        this.endUrl = endUrl;
        this.startRev = startRev;
        this.endRev = endRev;
        this.file = file;
    }
     
    @Override
    protected ISVNNotifyListener.Command getCommand() {
        return ISVNNotifyListener.Command.MERGE;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {
        arguments.add("merge"); //NOI18N
        if (!rec) {
            arguments.add("-N"); //NOI18N
        }
        if (force) {
            arguments.add("--force"); //NOI18N
        }
        if (dry) {
            arguments.add("--dry-run"); //NOI18N
        }        	        
        if (ignoreAncestry) {
            arguments.add("--ignore-ancestry"); //NOI18N
        }
        if (startUrl.equals(endUrl)) {
            arguments.add(startUrl);
            arguments.add(startRev, endRev);
        } else {
            arguments.add(startUrl, startRev);
            arguments.add(endUrl, endRev);
        }
        arguments.add(file);
        setCommandWorkingDirectory(file);        
    }

    @Override
    public void errorText(String line) {
        if (line.startsWith("svn: warning:")) { //NOI18N
            return;
        }
        super.errorText(line);
    }
    
}

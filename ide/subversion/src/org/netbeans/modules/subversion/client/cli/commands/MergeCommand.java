/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
import org.netbeans.modules.subversion.client.cli.SvnCommand;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
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
    private final File file;

    public MergeCommand(SVNUrl startUrl, SVNUrl endUrl, SVNRevision startRev, SVNRevision endRev, File file, boolean rec, boolean force, boolean ignoreAncestry, boolean dry) {
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
    protected int getCommand() {
        return ISVNNotifyListener.Command.MERGE;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {
        arguments.add("merge");
        if (!rec) {
            arguments.add("-N");
        }
        if (force) {
            arguments.add("--force");
        }
        if (dry) {
            arguments.add("--dry-run");
        }        	        
        if (ignoreAncestry) {
            arguments.add("--ignore-ancestry");
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
        if (line.startsWith("svn: warning:")) {
            return;
        }
        super.errorText(line);
    }
    
}

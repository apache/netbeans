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
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.cli.Parser.Line;
import org.netbeans.modules.subversion.client.cli.SvnCommand;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 *
 * @author Tomas Stupka
 */
public class CommitCommand extends SvnCommand {

    private final boolean keepLocks;
    private final boolean recursive;
    private final String message;
    private File[] files;
    private long revision = SVNRevision.SVN_INVALID_REVNUM;

    public CommitCommand(File[] files, boolean keepLocks, boolean recursive, String message) {
        this.keepLocks = keepLocks;
        this.recursive = recursive;
        this.message = message;
        this.files = files;
    }
       
    @Override
    protected int getCommand() {
        return ISVNNotifyListener.Command.COMMIT;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {        
        arguments.add("commit");
        if (keepLocks) {
            arguments.add("--no-unlock");                                
        }
        if(!recursive) {
            arguments.add("-N");
        }
        arguments.addMessage(message);	        
        arguments.addFileArguments(files);
        setCommandWorkingDirectory(files);
    }

    public long getRevision() {
        return revision;
    }

    @Override
    protected void notify(Line line) {
        if(line.getRevision() != -1) {
//          XXX can't rely on this - see also update cmd                        
//            if(revision != -1) {
//                Subversion.LOG.warning(
//                        "Revision notified more times : " + revision + ", " + 
//                        line.getRevision() + " for command " + getStringCommand());                
//            }
            revision = line.getRevision();            
        }
    }            
    
}

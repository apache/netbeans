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
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.cli.SvnCommand;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;

/**
 *
 * 
 */
public class ImportCommand extends SvnCommand {

    private final boolean recursive;
    private final String message;
    private final VCSFileProxy file;
    private final SVNUrl url;

    public ImportCommand(FileSystem fileSystem, VCSFileProxy file, SVNUrl url, boolean recursive, String message) {
        super(fileSystem);
        this.recursive = recursive;
        this.message = message;
        this.file = file;
        this.url = url;
    }
       
    @Override
    protected ISVNNotifyListener.Command getCommand() {
        return ISVNNotifyListener.Command.IMPORT;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {                     
        arguments.add("import"); //NOI18N
        arguments.add(file.getPath());
        arguments.addNonExistent(url);
        if(!recursive) {
            arguments.add("-N"); //NOI18N
        }
        arguments.addMessage(message);	        
        setCommandWorkingDirectory(file);
    }

//    @Override
//    protected void notify(Line line) {
//        if(line.getRevision() != -1) {
//            if(revision != -1) {
//                try {
//                    Subversion.LOG.warning("Revision notified more times : " + revision + ", " + line.getRevision() + " for command " + getStringCommand());
//                } catch (IOException ex) {
//                    // should not happen
//                }
//            }
//            revision = line.getRevision();            
//        }
//    }        
    

    
}

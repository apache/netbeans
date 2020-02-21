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
public class MoveCommand extends SvnCommand {

    private final SVNUrl fromUrl;
    private final SVNUrl toUrl;
    private final VCSFileProxy fromFile;    
    private final VCSFileProxy toFile;
    private final String msg;
    private final SVNRevision rev;
    private final boolean force;
    
    public MoveCommand(FileSystem fileSystem, VCSFileProxy fromFile, VCSFileProxy toFile, boolean force) {        
        super(fileSystem);
        this.fromFile = fromFile;
        this.toFile = toFile;
        this.force = force;        
        
        this.toUrl = null;        
        this.fromUrl = null;        
        this.msg = null;                  
        this.rev = null;                  
    }
    
    @Override
    protected ISVNNotifyListener.Command getCommand() {
        return ISVNNotifyListener.Command.MOVE;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {        
        arguments.add("move"); //NOI18N
        arguments.add(fromFile);
        arguments.add(toFile.getPath());
        if(force) {
            arguments.add("--force"); //NOI18N
        }
        setCommandWorkingDirectory(new VCSFileProxy[] {fromFile, toFile});                
    }    
}

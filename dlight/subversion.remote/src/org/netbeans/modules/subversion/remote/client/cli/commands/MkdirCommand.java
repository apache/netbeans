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
public class MkdirCommand extends SvnCommand {

    private enum MkdirType {
        url,
        file,
    }
        
    private final String message;
    private final SVNUrl url;
    private final VCSFileProxy file;
    private final MkdirType type;
    
    public MkdirCommand(FileSystem fileSystem, SVNUrl url, String message) {
        super(fileSystem);
        this.message = message;
        this.url = url;
        file = null;
        type = MkdirType.url;
    }
    
    public MkdirCommand(FileSystem fileSystem, VCSFileProxy file) {
        super(fileSystem);
        this.file = file;
        message = null;        
        url = null;
        type = MkdirType.file;
    }
       
    @Override
    protected ISVNNotifyListener.Command getCommand() {
        return ISVNNotifyListener.Command.MKDIR;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {                     
        arguments.add("mkdir"); //NOI18N
        switch(type) {
            case url:
                arguments.addMessage(message);	        
                arguments.add(url);
                break;
            case file:
                arguments.add(file);
                break;
            default:
                throw new IllegalStateException("Illegal mkdirtype: " + type); //NOI18N
        }        
    }
    
}

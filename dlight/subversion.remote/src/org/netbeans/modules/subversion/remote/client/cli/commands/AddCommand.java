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
import org.netbeans.modules.subversion.remote.client.cli.SvnCommand;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;

/**
 *
 * 
 */
public class AddCommand extends SvnCommand {

    private final boolean recursive;
    private final boolean force;
    private final VCSFileProxy[] files;

    public AddCommand(FileSystem fileSystem, VCSFileProxy[] files, boolean recursive, boolean force) {        
        super(fileSystem);
        this.recursive = recursive;
        this.force = force;
        this.files = files;
    }

    @Override
    protected ISVNNotifyListener.Command getCommand() {
        return ISVNNotifyListener.Command.ADD;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {
        arguments.add("add"); //NOI18N
        if (!recursive) {
            arguments.add("-N"); //NOI18N
        }			
        arguments.addFileArguments(files);        
        setCommandWorkingDirectory(files);        
    }

    @Override
    protected void config(VCSFileProxy configDir, String username, String password, Arguments arguments) {
        arguments.addConfigDir(configDir);        
    }
    
    @Override
    public void errorText(String line) {
        if (line.startsWith("svn: warning:")) {  //NOI18N // ignore warnings           
            return;
        }
        super.errorText(line);
    }    
}

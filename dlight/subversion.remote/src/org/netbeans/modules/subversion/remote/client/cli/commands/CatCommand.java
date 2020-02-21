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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class CatCommand extends SvnCommand {

    private enum CatType {
        url,
        file,
    }
    
    private final CatType type;
    
    private final SVNUrl url;
    private final VCSFileProxy file;    
    private final SVNRevision rev;
    private final SVNRevision pegRevision;
    
    private byte[] bytes;
    
    public CatCommand(FileSystem fileSystem, SVNUrl url, SVNRevision rev, SVNRevision pegRevision) {
        super(fileSystem);
        this.url = url;                
        this.rev = rev;        
        this.file = null;
        this.pegRevision = pegRevision;
        type = CatType.url;

    }
    
    public CatCommand(FileSystem fileSystem, VCSFileProxy file, SVNRevision rev) {
        super(fileSystem);
        this.file = file;
        this.rev = rev;        
        this.url = null;
        this.pegRevision = null;
        type = CatType.file;
    }

    public InputStream getOutput() {
        return new ByteArrayInputStream(bytes == null ? new byte[] {} : bytes);
    }
    
    @Override
    protected boolean hasBinaryOutput() {
        return true;
    }

    @Override
    protected boolean notifyOutput() {
        return false;
    }    
    
    @Override
    public void output(byte[] bytes) {
        this.bytes = bytes;
    }
    
    @Override
    protected ISVNNotifyListener.Command getCommand() {
        return ISVNNotifyListener.Command.CAT;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {        
        arguments.add("cat"); //NOI18N
        switch(type) {
            case url: 
                if (pegRevision == null) {
                    arguments.add(url);
                } else {
                    arguments.add(url, pegRevision);
                }
                break;
            case file:     
                arguments.add(file);
                setCommandWorkingDirectory(file);
                break;
            default :    
                throw new IllegalStateException("Illegal cattype: " + type); //NOI18N
        }                
        arguments.add(rev);
    }    
}

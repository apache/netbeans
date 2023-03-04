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
public class MoveCommand extends SvnCommand {
    

    private enum MoveType {
        url2url,
        file2file,
    }
    
    private final MoveType type;
    
    private final SVNUrl fromUrl;
    private final SVNUrl toUrl;
    private final File fromFile;    
    private final File toFile;
    private final String msg;
    private final SVNRevision rev;
    private final boolean force;
    
    public MoveCommand(SVNUrl fromUrl, SVNUrl toUrl, String msg, SVNRevision rev) {        
        this.fromUrl = fromUrl;
        this.toUrl = toUrl;
        this.msg = msg;
        this.rev = rev;    
        
        this.fromFile = null;
        this.toFile = null;                
        this.force = false;                
        
        type = MoveType.url2url;
    }   
    
    public MoveCommand(File fromFile, File toFile, boolean force) {        
        this.fromFile = fromFile;
        this.toFile = toFile;
        this.force = force;        
        
        this.toUrl = null;        
        this.fromUrl = null;        
        this.msg = null;                  
        this.rev = null;                  
        
        type = MoveType.file2file;
    }
    
    @Override
    protected int getCommand() {
        return ISVNNotifyListener.Command.MOVE;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {        
        arguments.add("move");        
        switch(type) {
            case url2url: 
                arguments.add(fromUrl);
                arguments.addNonExistent(toUrl);
                arguments.add(rev);                        
                arguments.addMessage(msg);  
                setCommandWorkingDirectory(new File("."));                
                break;
            case file2file:                     
                arguments.add(fromFile);
                arguments.add(toFile.getAbsolutePath());
                if(force) {
                    arguments.add("--force");                    
                }
                setCommandWorkingDirectory(new File[] {fromFile, toFile});                
                break;
            default :    
                throw new IllegalStateException("Illegal copytype: " + type);                             
        }        
    }    
}

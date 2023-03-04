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
public class CopyCommand extends SvnCommand {

    private enum CopyType {
        url2url,
        url2file,
        file2url,
        file2file,
    }
    
    private final CopyType type;
    
    private SVNUrl fromUrl;
    private SVNUrl toUrl;
    private File fromFile;    
    private File toFile;
    private String msg;
    private SVNRevision rev;
    private boolean makeParents;

    public static final String MAKE_PARENTS_ARGUMENT = "--parents";

    public CopyCommand(SVNUrl fromUrl, SVNUrl toUrl, String msg, SVNRevision rev) {
        this.fromUrl = fromUrl;
        this.toUrl = toUrl;
        this.msg = msg;
        this.rev = rev;
        type = CopyType.url2url;
    }

    public CopyCommand(SVNUrl fromUrl, SVNUrl toUrl, String msg, SVNRevision rev, boolean makeParents) {
        this.fromUrl = fromUrl;
        this.toUrl = toUrl;
        this.msg = msg;
        this.rev = rev;
        this.makeParents = makeParents;
        type = CopyType.url2url;
    }

    public CopyCommand(SVNUrl fromUrl, File toFile, SVNRevision rev) {        
        this.fromUrl = fromUrl;
        this.toFile = toFile;
        this.rev = rev;        
        type = CopyType.url2file;
    }
    
    public CopyCommand(File fromFile, SVNUrl toUrl, String msg) {
        this.fromFile = fromFile;
        this.toUrl = toUrl;
        this.msg = msg;
        type = CopyType.file2url;
    }

    public CopyCommand(File fromFile, File toFile) {
        this.fromFile = fromFile;
        this.toFile = toFile;
        type = CopyType.file2file;
    }

    public CopyCommand(File fromFile, SVNUrl toUrl, String msg, boolean makeParents) {
        this.fromFile = fromFile;
        this.toUrl = toUrl;
        this.msg = msg;
        this.makeParents = makeParents;
        type = CopyType.file2url;
    }
    
    @Override
    protected int getCommand() {
        return ISVNNotifyListener.Command.COPY;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {        
        arguments.add("copy");        
        switch(type) {
            case url2url: 
                arguments.add(fromUrl);
                arguments.addNonExistent(toUrl);
                if(rev != null) arguments.add(rev);                
                break;
            case url2file:     
                arguments.add(fromUrl);
                arguments.add(toFile.getAbsolutePath());
                if(rev != null) arguments.add(rev);                
                setCommandWorkingDirectory(toFile);                
                break;
            case file2url:                     
                arguments.add(fromFile);        
                arguments.addNonExistent(toUrl);
                setCommandWorkingDirectory(fromFile);                
                break;
            case file2file:
                arguments.add(fromFile);
                arguments.add(toFile);
                setCommandWorkingDirectory(fromFile);
                break;
            default :    
                throw new IllegalStateException("Illegal copytype: " + type);                             
        }                
        arguments.addMessage(msg);
        if (makeParents) {
            arguments.add(MAKE_PARENTS_ARGUMENT);
        }
    }    
}

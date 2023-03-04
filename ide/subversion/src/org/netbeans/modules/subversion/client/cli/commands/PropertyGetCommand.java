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
import java.util.List;
import org.netbeans.modules.subversion.client.cli.SvnCommand;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class PropertyGetCommand extends SvnCommand {

    private enum GetType {
        url,
        file
    }
    
    private final File file;    
    private final SVNUrl url;
    private final SVNRevision rev;
    private final SVNRevision peg;
    private final String name;
    private final GetType type;
    
    private byte[] bytes;
    
    public PropertyGetCommand(File file, String name) {        
        this.file = file;                
        this.name = name; 
        url = null;
        rev = null;
        peg = null;
        type = GetType.file;
    }
    
    public PropertyGetCommand(SVNUrl url, SVNRevision rev, SVNRevision peg, String name) {        
        this.url = url;                
        this.name = name; 
        this.rev = rev; 
        this.peg = peg; 
        file = null;
        type = GetType.url;
    }

    public byte[] getOutput() {
        return bytes == null ? new byte[] {} : bytes;
    }

    @Override
    protected boolean notifyOutput() {
        return false;
    }    
    
    @Override
    protected boolean hasBinaryOutput() {
        return true;
    }

    @Override
    public List<String> getCmdError() {
        return null;  // XXX don't throw errors to emulate svnCA behavior
    }
    
    @Override
    public void output(byte[] bytes) {
        this.bytes = bytes;
    }
    
    @Override
    protected int getCommand() {
        return ISVNNotifyListener.Command.PROPGET;
    }    
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {        
        arguments.add("propget");
	arguments.add("--strict");
	arguments.add(name);
        switch (type) {
            case file:
                arguments.add(file);        
                break;
            case url:
                arguments.add(rev);
                arguments.add(url, peg);        
                break;
            default: 
                throw new IllegalStateException("Illegal gettype: " + type);    
        }	
    }    
}

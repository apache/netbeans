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
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.subversion.remote.api.ISVNNotifyListener;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.cli.SvnCommand;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;

/**
 *
 * 
 */
public class ListPropertiesCommand extends SvnCommand {

    private enum ListType {
        url,
        file
    }
    
    private final List<String> output = new ArrayList<>();
    private final VCSFileProxy file;
    private final boolean rec;
    private final SVNUrl url;
    private final String rev;
    private final ListType type;
    
    public ListPropertiesCommand(FileSystem fileSystem, VCSFileProxy file, boolean rec) {
        super(fileSystem);
        this.file = file;
        this.rec = rec;
        url = null;
        rev = null;
        type = ListType.file;
    }
    
    public ListPropertiesCommand(FileSystem fileSystem, SVNUrl url, boolean rec) {
        this(fileSystem, url, null, rec);
    }

    public ListPropertiesCommand(FileSystem fileSystem, SVNUrl url, String revision, boolean rec) {
        super(fileSystem);
        this.url = url;
        this.rec = rec;
        file = null;
        rev = revision;
        type = ListType.url;
    }
    
    @Override
    protected boolean notifyOutput() {
        return false;
    }    
    
    @Override
    protected ISVNNotifyListener.Command getCommand() {
        return ISVNNotifyListener.Command.PROPLIST;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {
        arguments.add("proplist"); //NOI18N
        if (rec) {
            arguments.add("-R"); //NOI18N
        }			        
        switch (type) {
            case file:
                arguments.add(file);        
                break;
            case url:
                arguments.add(url);
                if (rev != null) {
                    arguments.add("-r"); //NOI18N
                    arguments.add(rev); //NOI18N
                }
                break;
            default:
                throw new IllegalStateException("Illegal gettype: " + type); //NOI18N
        }        
    }

    @Override
    public void outputText(String lineString) {
        if(lineString == null || 
           lineString.trim().equals("") || //NOI18N
           lineString.startsWith("Properties on '")) //NOI18N
        {
            return;
        }
        output.add(lineString.trim());
    }
    
    public List<String> getPropertyNames() throws SVNClientException {        
        return output;
    }

}

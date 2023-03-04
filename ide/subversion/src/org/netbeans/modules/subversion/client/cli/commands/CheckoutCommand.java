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
import org.netbeans.modules.subversion.client.cli.SvnCommand.Arguments;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class CheckoutCommand extends SvnCommand {

    private final SVNUrl url;
    private final File file;
    private final SVNRevision revision;
    private final boolean recursive;

    public CheckoutCommand(SVNUrl url, File path, SVNRevision revision, boolean recursive) {
        this.url = url;
        this.file = path;
        this.revision = revision;
        this.recursive = recursive;
    }
    
    @Override
    protected int getCommand() {
        return ISVNNotifyListener.Command.CHECKOUT;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {
        arguments.add("co");        
        arguments.add(revision);
        arguments.add(url); 
        arguments.add(file);
        if (!recursive) {
            arguments.add("-N");
        }
        setCommandWorkingDirectory(file);
    }
    
}

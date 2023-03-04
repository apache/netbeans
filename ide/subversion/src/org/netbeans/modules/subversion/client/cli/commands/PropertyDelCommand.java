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

/**
 *
 * @author Tomas Stupka
 */
public class PropertyDelCommand extends SvnCommand {
    
    private final File file;    
    private final String propName;
    private final boolean recursivelly;
    
    public PropertyDelCommand(File file, String propName, boolean recursivelly) {        
        this.file = file;
        this.propName = propName;
        this.recursivelly = recursivelly;
    }
    
    @Override
    protected int getCommand() {
        return ISVNNotifyListener.Command.PROPDEL;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {        
        arguments.add("propdel");                
        if(recursivelly) {
            arguments.add("-R");
        }        
        arguments.add(propName);
        arguments.add(file);                                 
    }    
}

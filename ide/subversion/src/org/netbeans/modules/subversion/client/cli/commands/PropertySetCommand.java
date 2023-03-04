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
public class PropertySetCommand extends SvnCommand {

    private enum PropType {
        string,
        file,
    }
    
    private final PropType type;
        
    private final File file;    
    private final String propName;
    private final String propValue;    
    private final File propFile;    
    private final boolean recursivelly;
    
    public PropertySetCommand(String propName, String propValue, File file, boolean recursivelly) {        
        this.file = file;
        this.propName = propName;
        this.propValue = propValue;
        this.recursivelly = recursivelly;
        propFile = null;
        type = PropType.string;
    }
    
    public PropertySetCommand(String propName, File propFile, File file, boolean recursivelly) {        
        this.file = file;
        this.propName = propName;
        this.propFile = propFile;
        this.recursivelly = recursivelly;
        propValue = null;
        type = PropType.file;
    }
       
    @Override
    protected int getCommand() {
        return ISVNNotifyListener.Command.PROPSET;
    }
    
    @Override
    protected boolean notifyOutput() {
        return false;
    }    
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {        
        arguments.add("propset");        
        if(recursivelly) {
            arguments.add("-R");
        }
        switch (type) {
            case string: 
                arguments.add(propName);
                /*  property values are set using an intermediate file, former setting directly through command line arguments as
                 *  'svn propset svn:ignore VALUE TARGET' was causing problems on windows platforms when values contained wild-cards.
                 *  The command line tried to expand the values and resolve them into existing files.
                */
                String propFileName = createTempCommandFile(propValue);
                arguments.add("-F");    // NOI18N
                arguments.add(propFileName);
                arguments.add(file);
                break;
            case file:     
                arguments.add(propName);
                arguments.add("-F");                
                arguments.add(propFile);		
                arguments.add(file);
                break;
            default :    
                throw new IllegalStateException("Illegal proptype: " + type);                             
        }        
    }    
}

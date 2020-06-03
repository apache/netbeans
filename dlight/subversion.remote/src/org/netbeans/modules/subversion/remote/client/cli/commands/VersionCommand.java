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
import java.util.logging.Level;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.ISVNNotifyListener;
import org.netbeans.modules.subversion.remote.client.cli.SvnCommand;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;

/**
 *
 * 
 */
public class VersionCommand extends SvnCommand {

    public VersionCommand(FileSystem fileSystem) {
        super(fileSystem);
    }

    private final List<String> output = new ArrayList<>();
    private boolean unsupportedVersion = false;
    private boolean supportedMetadataFormat = false;
    
    @Override
    protected ISVNNotifyListener.Command getCommand() {
        return ISVNNotifyListener.Command.UNDEFINED;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {
        arguments.add("--version"); //NOI18N
    }

    @Override
    protected void config(VCSFileProxy configDir, String username, String password, Arguments arguments) {
        arguments.addConfigDir(configDir);        
    }

    @Override
    public void outputText(String lineString) {
        if(lineString == null || lineString.trim().equals("")) {
            return;
        }
        output.add(lineString);
        super.outputText(lineString);
    }
    
    public boolean checkForErrors() {
        Integer exitCode = getExitCode();
        if ((exitCode == null) || !exitCode.equals(Integer.valueOf(0))) {
            return false;
        }

        boolean outputProduced = false;

        for (String string : output) {
            if ((string = string.trim()).length() == 0) {     //skip empty lines
                continue;
            }

            outputProduced = true;

            int pos = string.indexOf(" version "); //NOI18N
            if (pos > -1) {
                Subversion.LOG.log(Level.INFO, "Commandline client version: {0}", string.substring(pos + 9)); //NOI18N
            }

            if(string.indexOf("version 0.")  > -1 || //NOI18N
               string.indexOf("version 1.0") > -1 || //NOI18N
               string.indexOf("version 1.1") > -1 || //NOI18N
               string.indexOf("version 1.2") > -1 || //NOI18N
               string.indexOf("version 1.3") > -1 || //NOI18N
               string.indexOf("version 1.4") > -1) { //NOI18N
                unsupportedVersion = true;
                return false;
            } else if(string.indexOf("version 1.5")  > -1  //NOI18N
                    || string.indexOf("version 1.6") > -1) { //NOI18N
                supportedMetadataFormat = true;
            }
        }
        return outputProduced;
    }

    public boolean isUnsupportedVersion() {
        return unsupportedVersion;
    }

    public boolean isMetadataFormatSupported() {
        return supportedMetadataFormat;
    }
    
    public String getOutput() {
        StringBuilder sb = new StringBuilder();
        for (String string : output) {
            sb.append(string);
            sb.append('\n');
        }
        return sb.toString();
    }    
}

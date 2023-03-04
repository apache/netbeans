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

package org.netbeans.modules.cordova.updatetask;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.ExecTask;

/**
 * 
 * @author Jan Becicka
 */
public class ReadConfigTask extends CordovaTask {

    @Override
    public void execute() throws BuildException {
        try {
            File configFile = getConfigFile();
            SourceConfig config = new SourceConfig(configFile);
            final String id = config.getId();
            String pkg = id.substring(0, id.lastIndexOf(".")); // NOI18N
            setProperty("android.project.package", pkg); //NOI18N       
            setProperty("android.project.package.folder", pkg.replace(".", "/"));//NOI18N
            setProperty("project.name", config.getName()); // NOI18N
            setProperty("cordova.command",PluginTask.getCordovaCommand());
            final String path = PluginTask.isWin()?"env.Path":"env.PATH";
            final String pathKey = PluginTask.isWin()?"Path":"PATH";
            setProperty("cordova.path.key", pathKey);
            setProperty("cordova.path.value", PluginTask.isMac()?getMacPath(path):getProperty(path));
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }
    
    private String getMacPath(String path) {
        ExecTask exec = (ExecTask) getProject().createTask("exec");
        exec.setExecutable("/bin/bash");
        exec.createArg().setLine("-lc env");
        exec.setOutputproperty("mac.path");
        exec.execute();
        String property = getProperty("mac.path");
        if (property != null) {
            for (String line : property.split(System.getProperty("line.separator"))) {
                if (line.startsWith("PATH=")) {
                    return line.substring(5);
                }
            }
        }
        return getProperty(path);
    }
}

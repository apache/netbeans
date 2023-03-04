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

package org.netbeans.installer.utils.system.shortcut;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dmitry Lipin
 */
public class FileShortcut extends Shortcut {
    private File target;
    private boolean modifyPath;
    private List<String> arguments;
    
    public FileShortcut(String name, File target) {
        super(name);
        setTarget(target);    
        setArguments(new ArrayList<String>());
    }
    
    public boolean canModifyPath() {
        return modifyPath;
    }
    
    public void setModifyPath(final boolean modifyPath) {
        this.modifyPath = modifyPath;
    }
    
    public String getTargetPath() {
        return target.getPath();
    }    
    public File getTarget() {
        return target;
    }
    public void setTarget(File target) {
        this.target = target;
    }
    
    
    public List<String> getArguments() {
        return this.arguments;
    }
    
    public String getArgumentsString() {
        if (arguments.size() != 0) {
            StringBuilder builder = new StringBuilder();
            
            for (int i = 0; i < arguments.size(); i++) {
                builder.append(arguments.get(i));
                
                if (i != arguments.size() - 1) {
                    builder.append(" ");
                }
            }
            
            return builder.toString();
        }  else {
            return null;
        }
    }
    
    public void setArguments(final List<String> arguments) {
        this.arguments = arguments;
    }
    
    public void addArgument(final String argument) {
        arguments.add(argument);
    }
    
    public void removeArgument(final String argument) {
        arguments.remove(argument);
    }
    @Deprecated
    public File getExecutable() {
        return getTarget();
    }
    @Deprecated
    public String getExecutablePath() {
        return getTargetPath();
    }
    @Deprecated
    public void setExecutable(final File executable) {
        setTarget(executable);
    }
    @Deprecated
    public boolean canModifyExecutablePath() {
        return canModifyPath();
    }
    @Deprecated
    public void setModifyExecutablePath(final boolean modifyExecutablePath) {
        setModifyPath(modifyExecutablePath);
    }
}

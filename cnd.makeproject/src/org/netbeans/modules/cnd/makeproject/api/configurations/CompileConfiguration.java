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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import org.netbeans.modules.cnd.api.picklist.DefaultPicklistModel;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;

/**
 *
 */
public class CompileConfiguration implements Cloneable {
    private MakeConfiguration makeConfiguration;
    private ComboStringConfiguration compileCommandWorkingDir;
    private final DefaultPicklistModel compileCommandWorkingDirPicklist;
    private ComboStringConfiguration compileCommand;
    private final DefaultPicklistModel compileCommandPicklist;
    public static final String AUTO_FOLDER = "${AUTO_FOLDER}"; // NOI18N
    public static final String AUTO_COMPILE = "${AUTO_COMPILE}"; // NOI18N
    public static final String AUTO_MAKE = MakeArtifact.MAKE_MACRO;
    public static final String AUTO_ITEM_PATH = "${ITEM_PATH}"; // NOI18N
    public static final String AUTO_ITEM_NAME = "${ITEM_NAME}"; // NOI18N

    public CompileConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        
        compileCommandWorkingDirPicklist = new DefaultPicklistModel(10);
        compileCommandWorkingDirPicklist.addElement(AUTO_FOLDER);
        compileCommandWorkingDirPicklist.addElement("."); // NOI18N
        compileCommandWorkingDir = new ComboStringConfiguration(null, AUTO_FOLDER, compileCommandWorkingDirPicklist);
        
        compileCommandPicklist = new DefaultPicklistModel(10);
        compileCommandPicklist.addElement(AUTO_COMPILE);
        compileCommandPicklist.addElement(AUTO_MAKE+" "+AUTO_ITEM_NAME+".o"); // NOI18N
        compileCommand = new ComboStringConfiguration(null, AUTO_COMPILE, compileCommandPicklist); // NOI18N
    }
    
    public ComboStringConfiguration getCompileCommandWorkingDir() {
        return compileCommandWorkingDir;
    }

    public void setCompileCommandWorkingDir(ComboStringConfiguration compileCommandWorkingDir) {
        this.compileCommandWorkingDir = compileCommandWorkingDir;
    }

    public ComboStringConfiguration getCompileCommand() {
        return compileCommand;
    }
    
    public void setCompileCommand(ComboStringConfiguration compileCommand) {
        this.compileCommand = compileCommand;
    }

    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }

    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }

    @Override
    public CompileConfiguration clone() {
        CompileConfiguration clone = new CompileConfiguration(getMakeConfiguration());
        clone.setCompileCommandWorkingDir(getCompileCommandWorkingDir().clone());
        clone.setCompileCommand(getCompileCommand().clone());
        return clone;
    }

    void assign(CompileConfiguration compileConfiguration) {
         getCompileCommandWorkingDir().assign(compileConfiguration.getCompileCommandWorkingDir());
         getCompileCommand().assign(compileConfiguration.getCompileCommand());
    }
}

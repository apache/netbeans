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

package org.netbeans.modules.cnd.discovery.wizard.tree;

import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.discovery.api.ItemProperties.LanguageStandard;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.discovery.wizard.api.FileConfiguration;

/**
 *
 */
public class FileConfigurationImpl extends NodeConfigurationImpl implements FileConfiguration {
    private final SourceFileProperties sourceFile;
    
    public FileConfigurationImpl(SourceFileProperties source) {
        sourceFile = source;
    }

    @Override
    public String getCompilePath() {
        return sourceFile.getCompilePath();
    }

    @Override
    public String getFilePath() {
        return sourceFile.getItemPath();
    }

    @Override
    public String getCompileLine() {
        return sourceFile.getCompileLine();
    }

    @Override
    public String getFileName() {
        return sourceFile.getItemName();
    }

    @Override
    public List<String> getUserInludePaths() {
        return sourceFile.getUserInludePaths();
    }

    @Override
    public List<String> getUserInludeFiles() {
        return sourceFile.getUserInludeFiles();
    }

    @Override
    public Map<String,String> getUserMacros() {
        return sourceFile.getUserMacros();
    }
    
    @Override
    public List<String> getUndefinedMacros() {
        return sourceFile.getUndefinedMacros();
    }

    @Override
    public LanguageStandard getLanguageStandard() {
        return sourceFile.getLanguageStandard();
    }

    @Override
    public String getImportantFlags() {
        return sourceFile.getImportantFlags();
    }
}

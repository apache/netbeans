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
package org.netbeans.modules.cnd.makeproject.api;

import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectHelper;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.spi.project.ProjectConfigurationProvider;

/**
 *
 */
public interface MakeProject extends Project {
    public enum FormattingStyle {
        Global,
        Project,
        ClangFormat
    }
    
    MakeProjectHelper getHelper();
    Properties getProjectProperties(boolean b);
    void saveProjectProperties(Properties projectProperties, boolean b);

    String getSourceEncoding();
    void setSourceEncoding(String encName);

    CodeStyleWrapper getProjectFormattingStyle(String mime);
    FormattingStyle isProjectFormattingStyle();
    void setProjectFormattingStyle(FormattingStyle selected);
    void setProjectFormattingStyle(String C_MIME_TYPE, CodeStyleWrapper key);

    ExecutionEnvironment getFileSystemHost();
    
    ConfigurationDescriptorProvider getConfigurationDescriptorProvider();
    
    ProjectConfigurationProvider<Configuration> getProjectConfigurationProvider();
    MakeConfiguration getActiveConfiguration();
    ExecutionEnvironment getDevelopmentHost();
}

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
package org.netbeans.modules.cpplite.editor.spi;

import java.util.List;
import javax.swing.event.ChangeListener;

/**
 *
 * @author lahvac
 */
public interface CProjectConfigurationProvider {

    public ProjectConfiguration getProjectConfiguration();
    public void addChangeListener(ChangeListener listener);
    public void removeChangeListener(ChangeListener listener);

    //TODO: factory, accessor
    //TODO: listen on changes
    public static class ProjectConfiguration {
        public final String commandJsonPath;
        public final List<String> commandJsonCommand;
        public final String commandJsonContent;

        public ProjectConfiguration(String commandJsonPath) {
            this.commandJsonPath = commandJsonPath;
            this.commandJsonCommand = null;
            this.commandJsonContent = null;
        }

        public ProjectConfiguration(List<String> commandJsonCommand) {
            this.commandJsonPath = null;
            this.commandJsonCommand = commandJsonCommand;
            this.commandJsonContent = null;
        }

        public ProjectConfiguration(String commandJsonContent, boolean content) {
            this.commandJsonPath = null;
            this.commandJsonCommand = null;
            this.commandJsonContent = commandJsonContent;
        }
    }
}

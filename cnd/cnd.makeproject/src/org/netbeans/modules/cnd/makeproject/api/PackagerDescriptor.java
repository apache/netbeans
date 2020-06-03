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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;

public interface PackagerDescriptor {
    public interface ShellSciptWriter {
        public void writeShellScript(BufferedWriter bw, MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) throws IOException;
    }
    public String getName();
    public String getDisplayName();
    public boolean hasInfoList();
    public List<PackagerInfoElement> getDefaultInfoList(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration);
    public List<String> getOptionalInfoList();
    public boolean isOutputAFolder();
    public String getOutputFileName(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration);
    public String getOutputFileSuffix();
    public String getDefaultTool();
    public String getDefaultOptions();
    public String getTopDir(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration);
    public boolean supportsGroupAndOwner();
    public ShellSciptWriter getShellFileWriter();
}

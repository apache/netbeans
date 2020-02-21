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
package org.netbeans.modules.cnd.makeproject.packaging;

import java.util.ArrayList;
import org.netbeans.modules.cnd.makeproject.api.PackagerInfoElement;
import org.netbeans.modules.cnd.makeproject.api.PackagerDescriptor;
import java.util.List;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.openide.util.NbBundle;

/**
 *
 */
public class DummyPackager implements PackagerDescriptor {

    private final String name;

    public DummyPackager(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(CompilerSet2Configuration.class, "NOT_FOUND", name); // NOI18N // FIXUP: wrong bundle, but cannot change now after freeze
    }

    @Override
    public boolean hasInfoList() {
        return true;
    }

    @Override
    public List<PackagerInfoElement> getDefaultInfoList(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getOptionalInfoList() {
        return new ArrayList<>();
    }

    @Override
    public String getDefaultOptions() {
        return ""; // NOI18N
    }

    @Override
    public String getDefaultTool() {
        return ""; // NOI18N
    }

    @Override
    public boolean isOutputAFolder() {
        return true;
    }

    @Override
    public String getOutputFileName(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        return packagingConfiguration.getOutputName();
    }

    @Override
    public String getOutputFileSuffix() {
        return "";  // NOI18N
    }

    @Override
    public String getTopDir(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        return "";
    }

    @Override
    public boolean supportsGroupAndOwner() {
        return true;
    }

    @Override
    public ShellSciptWriter getShellFileWriter() {
        return null;
    }
}

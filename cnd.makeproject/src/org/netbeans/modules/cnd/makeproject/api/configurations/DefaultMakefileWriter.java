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

import java.io.IOException;
import java.io.Writer;
import org.netbeans.modules.cnd.makeproject.configurations.ConfigurationMakefileWriter;
import org.netbeans.modules.cnd.makeproject.spi.configurations.MakefileWriter;

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.makeproject.spi.configurations.MakefileWriter.class)
public class DefaultMakefileWriter implements MakefileWriter {

    /**
     * Writes first section of generated makefile
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    @Override
    public void writePrelude(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException {
        ConfigurationMakefileWriter.writePrelude(confDescriptor, conf, writer);
    }

    /**
     * Writes main build target
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    @Override
    public void writeBuildTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException {
        ConfigurationMakefileWriter.writeBuildTarget(confDescriptor, conf, writer);
    }

    /**
     * Writes build test target
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    @Override
    public void writeBuildTestTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException {
        ConfigurationMakefileWriter.writeBuildTestTarget(confDescriptor, conf, writer);
    }

    /**
     * Writes test target
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    @Override
    public void writeRunTestTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException {
        ConfigurationMakefileWriter.writeRunTestTarget(confDescriptor, conf, writer);
    }

    /**
     * Writes all compile targets (only for managed projects)
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    @Override
    public void writeCompileTargets(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException {
        ConfigurationMakefileWriter.writeCompileTargets(confDescriptor, conf, writer);
    }

    /**
     * Writes all compile test targets (only for managed projects)
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    @Override
    public void writeCompileTestTargets(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException {
        ConfigurationMakefileWriter.writeCompileTestTargets(confDescriptor, conf, writer);
    }

    /**
     * Writes link target (only for linked projects)
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    @Override
    public void writeLinkTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException {
        ConfigurationMakefileWriter.writeLinkTarget(confDescriptor, conf, writer);
    }

    /**
     * Writes link target (only for linked projects)
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    @Override
    public void writeLinkTestTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException {
        ConfigurationMakefileWriter.writeLinkTestTarget(confDescriptor, conf, writer);
    }

    /**
     * Writes writes archive target (only for archive projects)
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    @Override
    public void writeArchiveTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException {
        ConfigurationMakefileWriter.writeArchiveTarget(confDescriptor, conf, writer);
    }

    /**
     * Writes target for unmanaged projects
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    @Override
    public void writeMakefileTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException {
        ConfigurationMakefileWriter.writeMakefileTarget(confDescriptor, conf, writer);
    }

    /**
     * Writes target for QT projects
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    @Override
    public void writeQTTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException {
        ConfigurationMakefileWriter.writeQTTarget(confDescriptor, conf, writer);
    }

    /**
     * Writes clean target
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    @Override
    public void writeCleanTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException {
        ConfigurationMakefileWriter.writeCleanTarget(confDescriptor, conf, writer);
    }

    /**
     * Writes targets for sub projects
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    @Override
    public void writeSubProjectBuildTargets(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException {
        ConfigurationMakefileWriter.writeSubProjectBuildTargets(confDescriptor, conf, writer);
    }
    
    /**
     * Writes clean target for sub projects
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    @Override
    public void writeSubProjectCleanTargets(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException {
        ConfigurationMakefileWriter.writeSubProjectCleanTargets(confDescriptor, conf, writer);
    }

    /**
     * Writes dependency checking target
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    @Override
    public void writeDependencyChecking(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException {
        ConfigurationMakefileWriter.writeDependencyChecking(confDescriptor, conf, writer);
    }
}

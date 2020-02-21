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
package org.netbeans.modules.cnd.makeproject.spi.configurations;

import java.io.IOException;
import java.io.Writer;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;

public interface MakefileWriter {
    /**
     * Writes first section of generated makefile
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writePrelude(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes main build target
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeBuildTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes build test target
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeBuildTestTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes test target
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeRunTestTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes all compile targets (only for managed projects)
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeCompileTargets(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes all compile targets (only for managed projects)
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeCompileTestTargets(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes link target (only for linked projects)
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeLinkTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes link test target (only for linked projects)
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeLinkTestTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes writes archive target (only for archive projects)
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeArchiveTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes target for unmanaged projects
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeMakefileTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes target for QT projects
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param bw  output stream to generated makefile
     */
    void writeQTTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes clan target
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeCleanTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes targets for sub projects
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeSubProjectBuildTargets(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;

    /**
     * Writes clean target for sub projects
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeSubProjectCleanTargets(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;
    
    /**
     * Writes dependency checking target
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param writer  output stream to generated makefile
     */
    void writeDependencyChecking(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, Writer writer) throws IOException;
}

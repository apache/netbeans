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

package org.netbeans.modules.cnd.meson.project;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.spi.project.ProjectConfiguration;

public class Configuration implements ProjectConfiguration
{
    private Map<String, String> additionalArguments;
    private String buildDirectory;
    private String buildType;
    private String backend;
    private String name;
    private String runArguments;
    private String runDirectory;
    private String runExecutable;
    private String wrapMode;

    public Configuration(
        String name, String buildDirectory, String buildType, String backend,
        String wrapMode, String runDirectory, String runExecutable, String runArguments,
        Map<String, String> additionalArguments) {
        this.name = name;
        this.buildDirectory = buildDirectory;
        this.buildType = buildType;
        this.backend = backend;
        this.runArguments = runArguments;
        this.runDirectory = runDirectory;
        this.runExecutable = runExecutable;
        this.wrapMode = wrapMode;
        this.additionalArguments = additionalArguments;
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public Configuration(Configuration cfg) {
        name = cfg.name;
        buildDirectory = cfg.buildDirectory;
        buildType = cfg.buildType;
        backend = cfg.backend;
        runArguments = cfg.runArguments;
        runDirectory = cfg.runDirectory;
        runExecutable = cfg.runExecutable;
        wrapMode = cfg.wrapMode;
        additionalArguments = new HashMap<>();
        additionalArguments.putAll(cfg.additionalArguments);
    }

    public Configuration() {} // This is only here to make GSON happy

    @Override
    public String getDisplayName() {
        return name;
    }

    public String getAdditionalArgumentsFor(String action) {
        return additionalArguments.containsKey(action) ? additionalArguments.get(action) : "";
    }

    public String getBackend() {
        return backend;
    }

    public String getBuildDirectory() {
        return buildDirectory;
    }

    public String getBuildType() {
        return buildType;
    }

    public String getCompileCommandsJsonPath() {
        return Paths.get(getBuildDirectory(), "compile_commands.json").toString();
    }

    public String getRunArguments() {
        return runArguments;
    }

    public String getRunDirectory() {
        return runDirectory;
    }

    public String getRunExecutable() {
        return runExecutable;
    }

    public String getWrapMode() {
        return wrapMode;
    }

    public void setAdditionalArgumentsFor(String action, String arguments) {
        additionalArguments.put(action, arguments);
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    public void setBuildDirectory(String directory) {
        buildDirectory = directory;
    }

    public void setBuildType(String type) {
        buildType = type;
    }

    public void setDisplayName(String name) {
        this.name = name;
    }

    public void setRunArguments(String arguments) {
        runArguments = arguments;
    }

    public void setRunDirectory(String directory) {
        runDirectory = directory;
    }

    public void setRunExecutable(String executable) {
        runExecutable = executable;
    }

    public void setWrapMode(String mode) {
        wrapMode = mode;
    }

    public static Configuration getDefault() {
        return new Configuration(
            "default",
            Paths.get(MesonProject.BUILD_DIRECTORY, "default").toString(),
            "debug",
            "ninja",
            "default",
            "",
            "",
            "",
            new HashMap<>());
    }
}
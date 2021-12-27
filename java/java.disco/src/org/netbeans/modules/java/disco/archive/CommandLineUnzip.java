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
package org.netbeans.modules.java.disco.archive;

import java.io.File;

public class CommandLineUnzip extends AbstractCommandLineUnarchiver {

    @Override
    protected String[] getProbeCommand() {
        return new String[]{"unzip"};
    }

    @Override
    protected String getName() {
        return "unzip";
    }

    @Override
    protected String[] getCommand(File zipFile, File targetDir) {
        return new String[]{
            "unzip",
            "-o", //overwrite output, we expect the output dir to be empty
            zipFile.getAbsolutePath(),
            "-d",
            targetDir.getAbsolutePath()
        };
    }

    @Override
    public boolean isSupported(File input) {
        //TODO: We could just send the ArchiveType as argument
        return input.getName().toLowerCase().endsWith(".zip");
    }

}

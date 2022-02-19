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
package org.netbeans.modules.cnd.lsp.compilationdb;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 * CommandObjectBuilder builds a JSONObject representing a CommandObject as
 * defined in https://clang.llvm.org/docs/JSONCompilationDatabase.html
 */
final class CommandObjectBuilder {

    private static final Logger LOG = Logger.getLogger(CommandObjectBuilder.class.getName());

    private final Project project;
    private final StringBuilder command;
    private String file;
    private String output;

    CommandObjectBuilder(Project project) {
        this.project = project;
        this.command = new StringBuilder();
    }

    /**
     * Appends another piece of the 'command' entry in the command object. For
     * instance, you can add "-strip" or "-std=ansi".
     *
     * @param part The part to append at the end of the command line.
     * @return this
     */
    CommandObjectBuilder addCommandItem(String part) {
        if (part != null) {
            command.append(' ').append(part).append(' ');
        }
        return this;
    }

    /**
     * Sets the 'file' entry in the command object. For instance,
     * "/usr/home/user/project/folder/file.c"
     *
     * @param file The file. Absolute paths are recommended, but can be relative
     * to the project.getProjectDirectory().
     * @return this
     */
    CommandObjectBuilder setFile(String file) {
        this.file = Paths.get(file).toAbsolutePath().toString();
        return this;
    }

    /**
     * Sets the 'output' entry in the command object. For instance,
     * "/usr/home/user/project/build/platform/file.o"
     *
     * @param output The file. Absolute paths are recommended, but can be
     * relative to the project.getProjectDirectory().
     * @return this.
     */
    CommandObjectBuilder setOutput(String output) {
        this.output = Paths.get(output).toAbsolutePath().toString();
        return this;
    }

    String getFile() {
        return file;
    }

    /**
     * Builds a JSONObject representing this command object. NOTE: 'directory'
     * entry is automatically set to project.getProjectDirectory().
     *
     * @return The JSONObject representing this command object.
     * @throws IllegalStateException if a mandatory entri in the command object
     * is not present.
     */
    JSONObject build() throws IllegalStateException {
        if (file == null) {
            throw new IllegalStateException("Missing file");
        }
        if (command.length() == 0) {
            throw new IllegalStateException("No command");
        }

        JSONObject object = new JSONObject();

        // directory
        FileObject projectDirectory = project.getProjectDirectory();
        Path directory = Paths.get(projectDirectory.toURI());
        directory = directory.toAbsolutePath();
        LOG.log(Level.FINE, "Directory {0}", directory.toString());
        object.put("directory", directory.toString());

        // file
        LOG.log(Level.FINE, "File {0}", file);
        object.put("file", file);

        // command
        object.put("command", command.toString().trim());
        LOG.log(Level.FINE, "Command {0}", command.toString());

        // output (this one is optional)
        if (output != null) {
            object.put("output", output);
        }
        return object;
    }

}

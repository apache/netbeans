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
package org.netbeans.modules.extexecution.print;

import java.io.File;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.netbeans.spi.extexecution.open.FileOpenHandler;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * An OutputProcessor takes filename and line information
 * and produces hyperlinks. Actually resolving filenames
 * into real FileObjects is done lazily via user-supplied
 * FileLocators when the links are actually clicked.
 *
 * @author Tor Norbye, Petr Hejl
 */
public class FileListener implements OutputListener {

    private static final Logger LOGGER = Logger.getLogger(FileListener.class.getName());

    private final String file;

    private final int lineno;

    private final FileLocator fileLocator;

    private final FileOpenHandler handler;

    public FileListener(String file, int line, FileLocator fileLocator,
            FileOpenHandler handler) {

        if (line < 0) {
            line = 0;
        }

        // TODO : columns?
        this.file = file;
        this.lineno = line;
        this.fileLocator = fileLocator;
        this.handler = handler;
    }

    @Override
    public void outputLineAction(OutputEvent ev) {
        // Find file such and such and warp to it
        FileObject fo = findFile(file);

        if (fo != null) {
            handler.open(fo, lineno);
        }
    }

    private FileObject findFile(final String path) {
        if (fileLocator != null) {
            FileObject fo = fileLocator.find(path);
            if (fo != null) {
                return fo;
            }
        }

        // Perhaps it's an absolute path of some sort... try to resolve those
        // Absolute path? Happens for stack traces in libraries and such
        File realFile  = new File(path);
        if (realFile.isFile()) {
            return FileUtil.toFileObject(FileUtil.normalizeFile(realFile));
        } else {
            LOGGER.warning("Cannot resolve file for \"" + path + "\" path.");
            return null;
        }
    }
}

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
package org.netbeans.modules.extexecution.open;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.extexecution.open.FileOpenHandler;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public class DefaultFileOpenHandler implements FileOpenHandler {

    private static final Logger LOGGER = Logger.getLogger(DefaultFileOpenHandler.class.getName());

    @Override
    public void open(FileObject file, int line) {
        File realFile = FileUtil.toFile(file);
        if (realFile != null) {
            try {
                Desktop.getDesktop().edit(realFile);
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
    }
}

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
package org.netbeans.modules.web.wizards;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;
import org.netbeans.api.project.Project;

/**
 * Generic methods for evaluating the input into the wizards.
 *
 * @author Ana von Klopp
 */
abstract class Evaluator {

    private static final Logger LOG = Logger.getLogger(Evaluator.class.getName());
    private FileType fileType = null;

    Evaluator(FileType fileType) {
        this.fileType = fileType;
    }

    abstract boolean isValid();

    abstract String getTargetPath();

    abstract String getErrorMessage();

    abstract Iterator getPathItems();

    abstract void setInitialFolder(DataFolder df, Project project);

    FileType getFileType() {
        return fileType;
    }

    /**
     * Returns the absolute path to the target class. Used by the
     * wizards to display the result of the selections. 
     */
    String getTargetPath(Iterator pathItems) {
        StringBuffer buffer = new StringBuffer();
        while (pathItems.hasNext()) {
            buffer.append((String) (pathItems.next()));
            if (pathItems.hasNext()) {
                buffer.append(File.separator);
            }
        }
        buffer.append("."); //NOI18N
        buffer.append(fileType.getSuffix());
        return buffer.toString();
    }

    void checkFile(Iterator pathItems, FileObject root) throws IOException {
        LOG.finer("checkFile() " + root); //NOI18N

        String pathItem;
        FileObject fo = root;

        while (pathItems.hasNext()) {
            pathItem = (String) (pathItems.next());
            LOG.finer("path item is " + pathItem); //NOI18N

            // Path item is a directory, check that we can get it
            if (pathItems.hasNext()) {
                LOG.finer("Not the last one"); //NOI18N
                try {
                    fo = fo.getFileObject(pathItem, null);
                } catch (IllegalArgumentException iaex) {
                    throw new IOException(NbBundle.getMessage(Evaluator.class, "MSG_clash_path", pathItem));
                }
                if (fo == null) {
                    return;
                }

                if (!fo.isFolder()) {
                    LOG.finer("Not a folder"); //NOI18N
                    throw new IOException(NbBundle.getMessage(Evaluator.class, "MSG_clash_path", pathItem));
                }
            } else {
                LOG.finer("This is the last one"); //NOI18N
                try {
                    fo = fo.getFileObject(pathItem, fileType.getSuffix());
                } catch (IllegalArgumentException iaex) {
                    throw new IOException(NbBundle.getMessage(Evaluator.class, "MSG_clash_path", pathItem));
                }
                if (fo == null) {
                    return;
                }
                if (fo.isData()) {
                    throw new IOException(NbBundle.getMessage(Evaluator.class, "MSG_file_exists", pathItem));
                }
            }
        }
        LOG.finer("checkFile() passed"); //NOI18N
    }
}


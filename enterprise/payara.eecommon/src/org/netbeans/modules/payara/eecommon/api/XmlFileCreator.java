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

package org.netbeans.modules.payara.eecommon.api;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 * Class that allows proper creation of an XML file via FileSystem.AtomicAction.
 * Otherwise, there is a risk that the wrong data loader will catch the file
 * after it's created, but before the DOCTYPE or schema header is written.
 *
 * @author Peter Williams
 */
public class XmlFileCreator implements FileSystem.AtomicAction {

    private final FileObject source;
    private final FileObject destFolder;
    private final String name;
    private final String ext;
    private FileObject result;

    public XmlFileCreator(final FileObject source, final FileObject destFolder,
            final String name, final String ext) {
        this.source = source;
        this.destFolder = destFolder;
        this.name = name;
        this.ext = ext;
        this.result = null;
    }

    public void run() throws IOException {
        result = FileUtil.copyFile(source, destFolder, name, ext);
    }

    public FileObject getResult() {
        return result;
    }
}

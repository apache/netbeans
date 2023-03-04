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

package org.netbeans.modules.spring.beans.model.impl;

import java.io.File;
import org.netbeans.modules.spring.api.beans.model.Location;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Andrei Badea
 */
public class ConfigFileLocation implements Location {

    private final FileObject fileObject;
    private final int offset;

    public ConfigFileLocation(FileObject fileObject, int offset) {
        this.fileObject = fileObject;
        this.offset = offset;
    }

    public FileObject getFile() {
        return fileObject;
    }

    public int getOffset() {
        return offset;
    }
}

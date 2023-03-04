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

package org.netbeans.modules.java.source.usages.fcs;
import java.io.File;

import java.util.EventObject;

import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileUtil;

/**
 * Event indicating that a file named by a given path was created, deleted, or changed.
 * @author Jesse Glick
 */
public final class FileChangeSupportEvent extends EventObject {

    public static final int EVENT_CREATED = 0;
    public static final int EVENT_DELETED = 1;
    public static final int EVENT_MODIFIED = 2;

    private final int type;
    private final File path;
    
    FileChangeSupportEvent(FileChangeSupport support, int type, File path) {
        super(support);
        this.type = type;
        this.path = path;
    }
    
    public int getType() {
        return type;
    }
    
    public File getPath() {
        return path;
    }
    
    public FileObject getFileObject() {
        return FileUtil.toFileObject(path);
    }
    
    public String toString() {
        return "FCSE[" + "CDM".charAt(type) + ":" + path + "]"; // NOI18N
    }
    
}

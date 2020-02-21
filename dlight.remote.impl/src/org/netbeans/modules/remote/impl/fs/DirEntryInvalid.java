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

package org.netbeans.modules.remote.impl.fs;

import java.util.Date;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.netbeans.modules.remote.impl.RemoteLogger;

/**
 *
 */
public final class DirEntryInvalid extends DirEntry {

    private final String name;

    public DirEntryInvalid(String name) {
        super(name);
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return 0;
    }

    @Override
    public boolean canExecute() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return false;
    }

    @Override
    public boolean canRead() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return false;
    }

    @Override
    public boolean canWrite() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return false;
    }

    @Override
    public Date getLastModified() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return new Date();
    }

    @Override
    public boolean isLink() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return false;
    }

    @Override
    public boolean isDirectory() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return false;
    }

    @Override
    public boolean isPlainFile() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return true;
    }

    @Override
    public boolean isSameLastModified(DirEntry other) {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return false;
    }

    @Override
    public FileType getFileType() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return FileType.Regular;
    }

    @Override
    public String getLinkTarget() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return null;
    }

    @Override
    public String toExternalForm() {
        return name; //TODO: escape '\n'
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public long getDevice() {
        return 0;
    }

    @Override
    public long getINode() {
        return 0;
    }

    @Override
    public String toString() {
        return "DirEntryInvalid {" + name + '}'; //NOI18N
    }
}

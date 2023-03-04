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
package org.netbeans.modules.php.project.connections.transfer;

import java.util.Date;
import org.netbeans.modules.php.project.connections.spi.RemoteFile;

/**
 * Mock implementation of {@link RemoteFile}.
 */
public final class RemoteFileImpl implements RemoteFile {

    private final String name;
    private final String parentDirectory;
    private final boolean file;

    public RemoteFileImpl(String name, String parentDirectory, boolean file) {
        this.name = name;
        this.parentDirectory = parentDirectory;
        this.file = file;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getParentDirectory() {
        return parentDirectory;
    }

    @Override
    public boolean isDirectory() {
        return !file;
    }

    @Override
    public boolean isFile() {
        return file;
    }

    @Override
    public boolean isLink() {
        return false;
    }

    @Override
    public long getSize() {
        return 999;
    }

    @Override
    public long getTimestamp() {
        return new Date().getTime();
    }

}

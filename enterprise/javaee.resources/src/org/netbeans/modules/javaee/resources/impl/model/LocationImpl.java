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
package org.netbeans.modules.javaee.resources.impl.model;

import org.netbeans.modules.javaee.resources.api.model.Location;
import org.openide.filesystems.FileObject;

/**
 * Default location implementation.
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class LocationImpl implements Location {

    private final FileObject file;
    private final int offset;

    public LocationImpl(FileObject file, int offset) {
        this.file = file;
        this.offset = offset;
    }

    public LocationImpl(FileObject file) {
        this(file, -1);
    }

    @Override
    public FileObject getFile() {
        return file;
    }

    @Override
    public int getOffset() {
        return offset;
    }

}

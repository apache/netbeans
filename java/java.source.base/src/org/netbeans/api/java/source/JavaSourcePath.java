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
package org.netbeans.api.java.source;

import org.openide.filesystems.FileObject;

public class JavaSourcePath {

    //XXX: should this be here:
    public static JavaSourcePath forFile(FileObject file) {
        return new JavaSourcePath(file, -1);
    }

    private final FileObject file;
    private final int pos; //TODO: should be something more reliable that just a pos

    JavaSourcePath(FileObject file, int pos) {
        this.file = file;
        this.pos = pos;
    }

    public FileObject getFileObject() {
        return file;
    }

    public int getPos() {
        return pos;
    }
}

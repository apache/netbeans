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

package org.netbeans.modules.versioning.diff;

import java.io.IOException;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class EditorSaveCookie implements SaveCookie {

    private final EditorCookie editorCookie;
    private final String name;

    public EditorSaveCookie(EditorCookie editorCookie, FileObject fileObj) {
        this(editorCookie, getName(fileObj));
    }

    public EditorSaveCookie(EditorCookie editorCookie, String name) {
        super();
        if (editorCookie == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        this.editorCookie = editorCookie;
        this.name = name;
    }

    @Override
    public void save() throws IOException {
        editorCookie.saveDocument();
    }

    private static String getName(FileObject fileObj) {
        return (fileObj != null) ? FileUtil.getFileDisplayName(fileObj) : null;
    }

    @Override
    public String toString() {
        return (name != null) ? name : super.toString();
    }
}

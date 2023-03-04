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
package org.netbeans.modules.css.visual;

import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.lib.api.elements.TreePath;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class HtmlSourceElementHandle {

    private final OpenTag openTag;
    private final Snapshot snapshot;
    private final FileObject file;

    public HtmlSourceElementHandle(OpenTag openTag, Snapshot snapshot, FileObject file) {
        this.openTag = openTag;
        this.snapshot = snapshot;
        this.file = file;
    }

    public OpenTag getOpenTag() {
        return openTag;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public FileObject getFile() {
        return file;
    }

    public boolean isResolved() {
        return openTag != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(HtmlSourceElementHandle.class.getSimpleName());
        sb.append('(');
        sb.append("file=");
        sb.append(getFile().getPath());
        sb.append(", element=");
        sb.append(new TreePath(getOpenTag()).toString());
        sb.append(')');
        return sb.toString();
    }
    
}

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

package org.netbeans.modules.sampler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import org.openide.filesystems.AbstractFileSystem;

/** Filesystem that allows to virtually move some files next to each other.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class SelfSampleVFS extends AbstractFileSystem 
implements AbstractFileSystem.List, AbstractFileSystem.Info, AbstractFileSystem.Attr {
    private final String[] names;
    private final File[] contents;

    SelfSampleVFS(String[] names, File[] contents) {
        this.names = names;
        this.contents = contents;
        this.list = this;
        this.info = this;
        this.attr = this;
    }
    

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public String[] children(String f) {
        return f.equals("") ? names : null;
    }

    private File findFile(String name) {
        for (int i = 0; i < names.length; i++) {
            if (name.equals(names[i])) {
                return contents[i];
            }
        }
        return null;
    }

    @Override
    public Date lastModified(String name) {
        File f = findFile(name);
        return f == null ? null : new Date(f.lastModified());
    }

    @Override
    public boolean folder(String name) {
        return name.equals("");
    }

    @Override
    public boolean readOnly(String name) {
        return true;
    }

    @Override
    public String mimeType(String name) {
        return null;
    }

    @Override
    public long size(String name) {
        File f = findFile(name);
        return f == null ? -1 : f.length();
    }

    @Override
    public InputStream inputStream(String name) throws FileNotFoundException {
        File f = findFile(name);
        if (f == null) {
            throw new FileNotFoundException();
        }
        return new FileInputStream(f);
    }

    @Override
    public OutputStream outputStream(String name) throws IOException {
        throw new IOException();
    }

    @Override
    public void lock(String name) throws IOException {
        throw new IOException();
    }

    @Override
    public void unlock(String name) {
        throw new UnsupportedOperationException(name);
    }

    @Override
    public void markUnimportant(String name) {
        throw new UnsupportedOperationException(name);
    }

    @Override
    public Object readAttribute(String name, String attrName) {
        if ("java.io.File".equals(attrName)) {  // NOI18N
            return findFile(name);
        }
        return null;
    }

    @Override
    public void writeAttribute(String name, String attrName, Object value) throws IOException {
        throw new IOException();
    }

    @Override
    public Enumeration<String> attributes(String name) {
        return Collections.emptyEnumeration();
    }

    @Override
    public void renameAttributes(String oldName, String newName) {
        throw new UnsupportedOperationException(oldName);
    }

    @Override
    public void deleteAttributes(String name) {
        throw new UnsupportedOperationException(name);
    }
}

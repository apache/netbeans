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

package org.netbeans.modules.diff.tree;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * StreamSource used in Diff component based on a FileObject.
 */
public class FileStreamSource extends StreamSource {
    public static StreamSource create(FileObject fo, FileObject basePath) {
        if(fo != null) {
            if(fo.isData()) {
                return new FileStreamSource(fo, basePath);
            } else {
                return DIRECTORY_STREAM_SOURCE;
            }
        } else {
            return EMPTY_STREAM_SOURCE;
        }
    }

    private final FileObject fo;
    private final FileObject baseFo;
    private final Lookup lkp;

    private FileStreamSource(FileObject fo, FileObject baseFo) {
        this.fo = fo;
        this.baseFo = baseFo;
        this.lkp = Lookups.fixed(fo);
    }

    @Override
    public String getName() {
        return this.fo.getPath().substring(this.baseFo.getPath().length() + 1);
    }

    @Override
    public String getTitle() {
        return this.fo.getPath();
    }

    @Override
    public Lookup getLookup() {
        return lkp;
    }

    @Override
    public String getMIMEType() {
        return fo.getMIMEType();
    }

    @Override
    public Reader createReader() throws IOException {
        InputStream is = fo.getInputStream();
        return new InputStreamReader(is, FileEncodingQuery.getEncoding(fo));
    }

    @Override
    public Writer createWriter(Difference[] dfrncs) throws IOException {
        OutputStream os = fo.getOutputStream();
        return new OutputStreamWriter(os, FileEncodingQuery.getEncoding(fo));
    }

    @Override
    public boolean isEditable() {
        return fo.canWrite();
    }

    @Override
    public void close() {
        if(fo != null) {
            try {
                DataObject dObj = DataObject.find(fo);
                if (dObj != null) {
                    EditorCookie ec = dObj.getLookup().lookup(EditorCookie.class);
                    if (ec != null && ec.getOpenedPanes() == null && !ec.isModified()) {
                        ec.close();
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        super.close();
    }

    private static StreamSource EMPTY_STREAM_SOURCE = new DummySource("<MISSING>");
    private static StreamSource DIRECTORY_STREAM_SOURCE = new DummySource("<FOLDER>");


    private static class DummySource extends StreamSource {
        private final String name;

        public DummySource(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getTitle() {
            return name;
        }

        @Override
        public String getMIMEType() {
            return "text/plain";
        }

        @Override
        public Reader createReader() throws IOException {
            return EMPTY_READER;
        }

        @Override
        public Writer createWriter(Difference[] dfrncs) throws IOException {
            return null;
        }

        @Override
        public boolean isEditable() {
            return false;
        }

    };

    private static final Reader EMPTY_READER = new Reader() {

        @Override
        public int read(char[] chars, int i, int i1) throws IOException {
            return -1;
        }

        @Override
        public void close() throws IOException {
        }
    };
}

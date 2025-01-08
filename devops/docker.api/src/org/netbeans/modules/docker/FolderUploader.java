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
package org.netbeans.modules.docker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.netbeans.modules.docker.api.DockerInstance;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class FolderUploader {

    private static final Logger LOGGER = Logger.getLogger(FolderUploader.class.getName());

    // XXX only one upload at a time ?
    private static final RequestProcessor RP = new RequestProcessor(FolderUploader.class);

    private final DockerInstance instance;

    private final OutputStream os;

    public FolderUploader(DockerInstance instance, OutputStream os) {
        this.instance = instance;
        this.os = os;
    }

    public Future<Void> upload(final FileObject folder, final IgnoreFileFilter filter, final Listener listener) {
        return RP.submit(new Callable<Void>() {
            @Override
            public Void call() throws IOException, ArchiveException {
                ChunkedOutputStream cos = new ChunkedOutputStream(new BufferedOutputStream(os));
                try {
                    GZIPOutputStream gzos = new GZIPOutputStream(cos);
                    try {
                        TarArchiveOutputStream aos = new TarArchiveOutputStream(gzos);
                        aos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
                        try {
                            // FIXME exclude dockerignored files
                            FileObject context = folder;
                            for (Enumeration<? extends FileObject> e = context.getChildren(true); e.hasMoreElements();) {
                                FileObject child = e.nextElement();
                                if (child.isFolder()) {
                                    continue;
                                }
                                String path = FileUtil.getRelativePath(context, child);
                                if (filter.accept(path)) {
                                    listener.onUpload(path);
                                    LOGGER.log(Level.FINE, "Uploading {0}", path);
                                    TarArchiveEntry entry = new TarArchiveEntry(new FileObjectAdapter(child),
                                            path);
                                    aos.putArchiveEntry(entry);
                                    try (InputStream is = new BufferedInputStream(child.getInputStream())) {
                                        FileUtil.copy(is, aos);
                                    }
                                    aos.closeArchiveEntry();
                                }
                            }
                        } finally {
                            aos.finish();
                            aos.flush();
                        }
                    } finally {
                        gzos.finish();
                        gzos.flush();
                    }
                } finally {
                    cos.finish();
                    cos.flush();
                }
                return null;
            }
        });
    }

    public static interface Listener {

        void onUpload(String path);

    }

    private static final class FileObjectAdapter extends File {

        private final FileObject fo;

        public FileObjectAdapter(FileObject fo) {
            super(fo.getPath());
            this.fo = fo;
        }

        @Override
        public boolean isDirectory() {
            return fo.isFolder();
        }

        @Override
        public long lastModified() {
            return fo.lastModified().getTime();
        }

        @Override
        public long length() {
            return fo.getSize();
        }
    }
}

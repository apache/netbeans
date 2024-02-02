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
package org.netbeans.modules.java.source.classpath;

import java.net.URL;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SourceForBinaryQueryImplementation.class, position = 100000)
public final class SourceNextToBinaryQueryImpl implements SourceForBinaryQueryImplementation {
    @Override
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        URL file = FileUtil.getArchiveFile(binaryRoot);
        if (file != null) {
            FileObject fo = URLMapper.findFileObject(file);
            if (fo != null) {
                FileObject src = fo.getParent().getFileObject(fo.getName() + "-sources", fo.getExt());
                if (src != null) {
                    return new SourceForBinaryQueryImplementation2.Result() {
                        @Override
                        public boolean preferSources() {
                            return false;
                        }

                        @Override
                        public FileObject[] getRoots() {
                            return new FileObject[]{FileUtil.getArchiveRoot(src)};
                        }

                        @Override
                        public void addChangeListener(ChangeListener l) {
                        }

                        @Override
                        public void removeChangeListener(ChangeListener l) {
                        }
                    };
                }
            }
        }
        return null;
    }
}

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

package org.netbeans.modules.j2ee.core.api.support.java.method;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrei Badea
 */
public class RepositoryImpl extends Repository {

    private XMLFileSystem system;

    public RepositoryImpl() {
        super(new MultiFileSystemImpl());
    }

    public static final class MultiFileSystemImpl extends MultiFileSystem {

        public MultiFileSystemImpl() {
            super(createFileSystems());
        }

        public void reset() {
            setDelegates(createFileSystems());
        }

        private static FileSystem[] createFileSystems() {
            try {
                FileSystem writeFs = FileUtil.createMemoryFileSystem();
                FileSystem utilitiesFs = new XMLFileSystem(RepositoryImpl.class.getClassLoader().getResource("layer.xml"));
                FileSystem j2eeserverFs = new XMLFileSystem(RepositoryImpl.class.getClassLoader().getResource("org/netbeans/modules/j2ee/deployment/impl/layer.xml"));
                FileSystem javaProjectFs = new XMLFileSystem(RepositoryImpl.class.getClassLoader().getResource("org/netbeans/modules/java/project/layer.xml"));
                return new FileSystem[] { writeFs, utilitiesFs, j2eeserverFs, javaProjectFs };
            } catch (SAXException e) {
                AssertionError ae = new AssertionError(e.getMessage());
                ae.initCause(e);
                throw ae;
            }
        }
    }
}

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
package org.netbeans.modules.profiler.categories.j2se;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.xml.sax.SAXException;


/**
 * @author ads
 *
 */
public class RepositoryImpl extends org.openide.filesystems.Repository {


    private static final long serialVersionUID = 4167817456979234805L;

    public RepositoryImpl() {
        super(createDefFs());
    }

    private static FileSystem createDefFs() {
        try
        {
            FileSystem writeFs = FileUtil.createMemoryFileSystem();
            FileSystem profilerLayer = new XMLFileSystem(RepositoryImpl.class.getClassLoader().getResource(
                    "org/netbeans/modules/profiler/j2se/mf-layer.xml"));
            return new MultiFileSystem(new FileSystem[] {writeFs, profilerLayer});
        } catch (SAXException e) {
            AssertionError ae = new AssertionError(e.getMessage());
            ae.initCause(e);
            throw ae;
        }
    }
}

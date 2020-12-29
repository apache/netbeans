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
package org.netbeans.modules.java.mx.project;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = FileOwnerQueryImplementation.class, position = 11)
public final class SuiteFileOwnerQueryImpl implements FileOwnerQueryImplementation {
    @Override
    public Project getOwner(URI url) {
            for (;;) {
                FileObject fo;
                try {
                    fo = URLMapper.findFileObject(url.toURL());
                } catch (MalformedURLException ex) {
                    fo = null;
                }
                if (fo != null) {
                    return getOwner(fo);
                }
                url = url.resolve("..");
            }
    }

    @Override
    public Project getOwner(FileObject file) {
        for (;;) {
            if (file == null) {
                return null;
            }
            if (file.getFileObject(".mxignore") != null) {
                return null;
            }
            if (SuiteFactory.findSuitePy(file) != null) {
                try {
                    return ProjectManager.getDefault().findProject(file);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            file = file.getParent();
        }
    }
}

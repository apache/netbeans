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

package org.netbeans.modules.html.indexing;

import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.html.editor.api.index.HtmlIndex;
import org.netbeans.modules.web.common.api.FileReference;
import org.netbeans.modules.web.common.spi.DependentFileQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=DependentFileQueryImplementation.class)
public class DependentFileQueryImpl implements DependentFileQueryImplementation {

    @Override
    public Dependency isDependent(FileObject master, FileObject dependent) {
        Project p = FileOwnerQuery.getOwner(master);
        if (p == null) {
            return Dependency.UNKNOWN;
        }
        try {
            HtmlIndex.AllDependenciesMaps all = HtmlIndex.get(p).getAllDependencies();
            Collection<FileReference> c = all.getSource2dest().get(master);
            if (c != null) {
                for (FileReference fr : c) {
                    if (dependent.equals(fr.target())) {
                        return Dependency.YES;
                    }
                }
            }
            return Dependency.NO;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

}

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

package org.netbeans.modules.projectapi;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 * @author Tomas Zezula
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.FileEncodingQueryImplementation.class, position=200)
public class ProjectFileEncodingQueryImplementation extends FileEncodingQueryImplementation {

    private static final Logger LOG = Logger.getLogger(ProjectFileEncodingQueryImplementation.class.getName());

    public ProjectFileEncodingQueryImplementation() {}

    public Charset getEncoding(FileObject file) {
        if (isSystemFS(file)) {
            LOG.log(Level.FINER, "{0}: on system filesystem", file);    //NOI18N
            return null;
        }
        Project p = FileOwnerQuery.getOwner(file);
        if (p == null) {
            LOG.log(Level.FINER, "{0}: no owner", file);
            return null;
        }
        FileEncodingQueryImplementation delegate = p.getLookup().lookup(FileEncodingQueryImplementation.class);
        if (delegate == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0}: no FEQI in {1}", new Object[] {file, p});
            }
            return null;
        }
        Charset encoding = delegate.getEncoding(file);
        if (LOG.isLoggable(Level.FINE)) {
           LOG.log(Level.FINE, "{0}: got {1} from {2}", new Object[] {file, encoding, delegate});
        }
        return encoding;
    }

    private static boolean isSystemFS(@NonNull final FileObject file) {
        try {
            return file.getFileSystem().isDefault();
        } catch (IOException ioe) {
            return false;
        }
    }

}

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
package org.netbeans.modules.docker.editor;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service = MIMEResolver.class)
public final class DockerfileResolver extends MIMEResolver {

    public static final String MIME_TYPE = "text/x-dockerfile"; //NOI18N
    private static final String FILE_NAME = "dockerfile";   //NOI18N

    public DockerfileResolver() {
        super(MIME_TYPE);
    }

    @CheckForNull
    @Override
    public String findMIMEType(@NonNull final FileObject fo) {
        final String ext = fo.getExt();
        final String nameWithExt = fo.getNameExt().toLowerCase();
        return FILE_NAME.equalsIgnoreCase(ext) || nameWithExt.startsWith(FILE_NAME)?
            MIME_TYPE :
            null;
    }

}

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
package org.netbeans.modules.project.ui.convertor;

import java.util.Map;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.project.ui.ProjectConvertor;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class ProjectConvertorAcceptor {

    static final String ATTR_PATTERN = "requiredPattern";   //NOI18N
    static final String ATTR_DELEGATE = "delegate";         //NOI18N

    private final Map<String,Object> params;
    private final Pattern requiredPattern;

    public ProjectConvertorAcceptor(@NonNull final Map<String,Object> params) {
        Parameters.notNull("params", params);   //NOI18N
        this.params = params;
        final String pattern = (String) params.get(ATTR_PATTERN);
        Parameters.notNull(ATTR_PATTERN, pattern);  //NOI18N
        requiredPattern = Pattern.compile(pattern);
    }

    @CheckForNull
    ProjectConvertor.Result isProject(@NonNull final FileObject folder) {
        for (FileObject fo : folder.getChildren()) {
            if (requiredPattern.matcher(fo.getNameExt()).matches()) {
                return getProjectConvertor().isProject(folder);
            }
        }
        return null;
    }

    @NonNull
    private ProjectConvertor getProjectConvertor() {
        final Object convertor = params.get(ATTR_DELEGATE);
        if (!(convertor instanceof ProjectConvertor)) {
            throw new IllegalStateException(String.format(
                "Invalid ProjectConvertor:  %s",    //NOI18N
                convertor));
        }
        return (ProjectConvertor) convertor;
    }
}

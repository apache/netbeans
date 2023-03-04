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

package org.netbeans.modules.parsing.impl.event;

import java.util.Objects;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.implspi.SourceControl;
import org.netbeans.modules.parsing.implspi.SourceEnvironment;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class FileChangeSupport extends FileChangeAdapter {

    private final SourceControl sourceControl;

    public FileChangeSupport(@NonNull final SourceControl sourceControl) {
        Parameters.notNull("sourceControl", sourceControl); //NOI18N
        this.sourceControl = sourceControl;
    }

    @Override
    public void fileChanged(final FileEvent fe) {
        // FIXME -- MIME type may changed even though the file only changed content.
        // For example XML files' MIME type depends on their XMLNS declaration
        sourceControl.sourceChanged(false);
        sourceControl.revalidate(SourceEnvironment.getReparseDelay(false));
    }

    @Override
    public void fileRenamed(final FileRenameEvent fe) {
        final String oldExt = fe.getExt();
        final String newExt = fe.getFile().getExt();
        sourceControl.sourceChanged(!Objects.equals(oldExt, newExt));
        sourceControl.revalidate(SourceEnvironment.getReparseDelay(false));
    }
}

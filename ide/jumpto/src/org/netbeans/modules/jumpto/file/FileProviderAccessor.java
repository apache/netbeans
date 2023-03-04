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

package org.netbeans.modules.jumpto.file;

import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.spi.jumpto.file.FileDescriptor;
import org.netbeans.spi.jumpto.file.FileProvider;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public abstract class FileProviderAccessor {

    private static volatile FileProviderAccessor instance;

    public static synchronized FileProviderAccessor getInstance() {
        if (instance == null) {
            try {
                Class.forName(FileProvider.Context.class.getName(), true, FileProviderAccessor.class.getClassLoader());
                assert instance != null;
            } catch (ClassNotFoundException cnf) {
                Exceptions.printStackTrace(cnf);
            }
        }
        assert instance != null;
        return instance;
    }

    public static void setInstance(final FileProviderAccessor theInstance) {
        assert theInstance != null;
        instance = theInstance;
    }

    public abstract FileProvider.Context createContext(
            @NonNull String text,
            @NonNull SearchType searchType,
            int lineNr,
            @NullAllowed Project currentProject);

    public abstract void setRoot(FileProvider.Context ctx, FileObject root);

    public abstract FileProvider.Result createResult(List<? super FileDescriptor> result, String[] message, FileProvider.Context ctx);

    public abstract int getRetry(FileProvider.Result result);

    public abstract void setFromCurrentProject(@NonNull FileDescriptor desc, boolean value);

    public abstract boolean isFromCurrentProject(@NonNull FileDescriptor desc);

    public abstract void setLineNumber(@NonNull FileDescriptor desc, int lineNo);
}

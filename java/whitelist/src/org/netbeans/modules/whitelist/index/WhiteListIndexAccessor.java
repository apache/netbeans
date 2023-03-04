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
package org.netbeans.modules.whitelist.index;

import java.net.URL;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.api.whitelist.index.WhiteListIndex;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public abstract class WhiteListIndexAccessor {

    private static volatile WhiteListIndexAccessor instance;

    @NonNull
    public static synchronized WhiteListIndexAccessor getInstance() {
        if (instance == null) {
            try {
                Class.forName(WhiteListIndex.class.getName(), true, WhiteListIndexAccessor.class.getClassLoader());
                assert instance != null;
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return instance;

    }

    public static void setInstance(@NonNull final WhiteListIndexAccessor _instance) {
        Parameters.notNull("_instance", _instance); //NOI18N
        instance = _instance;
    }

    public abstract void refresh(@NonNull URL root);
    public abstract WhiteListIndex.Problem createProblem(
            @NonNull WhiteListQuery.Result result,
            @NonNull FileObject root,
            @NonNull String key,
            int line);
}

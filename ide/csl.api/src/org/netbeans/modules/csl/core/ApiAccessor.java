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
package org.netbeans.modules.csl.core;

import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.EditList;
import org.openide.util.Parameters;

/**
 *
 * @author Dusan Balek
 */
public abstract class ApiAccessor {
    private static volatile ApiAccessor instance;

    @NonNull
    public static synchronized ApiAccessor getInstance() {
        if (instance == null) {
            try {
                Class.forName(EditList.class.getName(), true, ApiAccessor.class.getClassLoader());
                assert instance != null;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    public static void setInstance(@NonNull final ApiAccessor inst) {
        Parameters.notNull("inst", inst);   //NOI18N
        instance = inst;
    }

    public abstract List<EditList.Edit> getEdits(@NonNull EditList editList);
}

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

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.project.ProjectManagerImplementation;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public abstract class SPIAccessor {

    private static volatile SPIAccessor instance;

    public static synchronized SPIAccessor getInstance() {
        if (instance == null) {
            try {
                Class.forName(
                        ProjectManagerImplementation.ProjectManagerCallBack.class.getName(),
                        true,
                        SPIAccessor.class.getClassLoader());
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }
        assert instance != null;
        return instance;
    }

    public static void setInstance(@NonNull final SPIAccessor _instance) {
        Parameters.notNull("_instance", _instance); //NOI18N
        instance = _instance;
    }

    @NonNull
    public abstract ProjectManagerImplementation.ProjectManagerCallBack createProjectManagerCallBack();
}

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

package org.netbeans.modules.spring.beans;

import java.io.File;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel.DocumentAccess;
import org.netbeans.modules.spring.beans.model.SpringConfigFileModelController.LockedDocument;
import org.netbeans.modules.spring.beans.model.SpringConfigFileModelManager;

/**
 *
 * @author Andrei Badea
 */
public abstract class SpringConfigModelAccessor {

    private static volatile SpringConfigModelAccessor accessor;

    public static void setDefault(SpringConfigModelAccessor accessor) {
        if (SpringConfigModelAccessor.accessor != null) {
            throw new IllegalStateException();
        }
        SpringConfigModelAccessor.accessor = accessor;
    }

    public static SpringConfigModelAccessor getDefault() {
        if (accessor != null) {
            return accessor;
        }
        try {
            Class.forName(SpringConfigModel.class.getName(), true, SpringConfigModel.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
        return accessor;
    }

    public abstract SpringConfigModel createSpringConfigModel(SpringConfigFileModelManager fileModelManager, ConfigFileGroup configFileGroup);
    
    public abstract ConfigFileGroup getConfigFileGroup(SpringConfigModel model);

    public abstract DocumentAccess createDocumentAccess(SpringBeans springBeans, File file, LockedDocument lockedDoc);
}

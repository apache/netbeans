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
package org.netbeans.modules.php.blade.editor.components;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;

/**
 * TO BE CONTINUED
 * 
 * @author bhaidu
 */
public class ComponentModel {

    private static final Map<Integer, ComponentModel> MODEL_INSTANCE = new WeakHashMap<>();

    protected static ComponentModel getModel(FileObject fo, String prefix) {
        ComponentModel selfModel = new ComponentModel();
        Project projectOwner = ProjectConvertors.getNonConvertorOwner(fo);
        if (projectOwner == null) {
            return null;
        }
        int pathHash = projectOwner.getProjectDirectory().toString().hashCode();
        if (ComponentModel.MODEL_INSTANCE.containsKey(pathHash)) {
            selfModel = MODEL_INSTANCE.get(pathHash);

        } else {
            MODEL_INSTANCE.put(pathHash, selfModel);
        }
        return selfModel;
    }
}

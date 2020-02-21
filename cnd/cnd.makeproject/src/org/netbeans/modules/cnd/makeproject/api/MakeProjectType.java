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
package org.netbeans.modules.cnd.makeproject.api;

import org.netbeans.modules.cnd.api.project.NativeProjectType;

/**
 *
 */
public interface MakeProjectType extends NativeProjectType {

    public static final String PROJECT_TYPE = "org-netbeans-modules-cnd-makeproject";//NOI18N
    public static final String TYPE = "org.netbeans.modules.cnd.makeproject"; // NOI18N
    public static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/make-project/1"; // NOI18N
    public static final String PROJECT_CONFIGURATION__NAME_NAME = "name"; // NOI18N
    public static final String PROJECT_CONFIGURATION_NAME = "data"; // NOI18N
    public final static String SOURCE_ROOT_LIST_ELEMENT = "sourceRootList"; // NOI18N

    public String extFolderActionsPath();

    public String folderActionsPath();
    
}

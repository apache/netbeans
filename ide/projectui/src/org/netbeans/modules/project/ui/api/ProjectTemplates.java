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

package org.netbeans.modules.project.ui.api;

import org.netbeans.spi.project.ui.support.CommonProjectActions;

/**
 * Constants not currently documented in {@link CommonProjectActions#newProjectAction}.
 * @since 1.32
 */
public class ProjectTemplates {

    /**
     * {@link String}-valued action property for a project category (subfolder code name) to select.
     * @deprecated use {@link CommonProjectActions.PRESELECT_CATEGORY} instead
     */
    @Deprecated
    public static final String PRESELECT_CATEGORY = CommonProjectActions.PRESELECT_CATEGORY;

    /**
     * {@link String}-valued action property for a project template (code name within a category) to select.
     */
    public static final String PRESELECT_TEMPLATE = "PRESELECT_TEMPLATE";

    private ProjectTemplates() {}

}

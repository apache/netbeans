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
package org.netbeans.spi.project;

import org.netbeans.api.project.Project;

/**
 * Optional ability of a project to determine its immediate parent project, if
 * there is any.
 *
 * @author Laszlo Kishalmi
 * @since 1.79
 */
public interface ParentProjectProvider {

    /**
     * The immediate parent of this project or <code>null</code> if there is no
     * parent known.
     * 
     * @return the immediate parent if exists of <code>null</code>
     */
    Project getPartentProject();

}

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

package org.netbeans.modules.java.api.common.project.ui.customizer;

import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

/**
 * Ability to verify whether the project is currently sharable, and if not to make it so.
 * Will be in project lookup.
 * @since org.netbeans.modules.java.api.common/0 1.8
 */
public interface ProjectSharability {

    /**
     * Checks whether the project is currently sharable.
     * @return true if it is self-contained, false if it has external file references
     * @see AntProjectHelper#isSharableProject
     */
    boolean isSharable();

    /**
     * Offers to make the project sharable.
     * This should be called in EQ and just opens some GUI.
     * The user may or may not proceed.
     * @see SharableLibrariesUtils#showMakeSharableWizard
     */
    void makeSharable();

}

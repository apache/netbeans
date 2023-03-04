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

package org.netbeans.spi.project.support.ant;

import java.io.IOException;

/**
 * Hook run when <code>nbproject/project.xml</code> is saved.
 * An instance should be placed into a project's lookup to register it.
 * @see org.netbeans.api.project.Project#getLookup
 * @author Jesse Glick
 */
public abstract class ProjectXmlSavedHook {

    /**
     * Default constructor for subclasses.
     */
    protected ProjectXmlSavedHook() {}

    /**
     * Called when shared project metadata (<code>project.xml</code>) has been modified.
     * <p>
     * Also called the first the time a project created by {@link ProjectGenerator}
     * is saved.
     * This is called during a project save event and so runs with write access.
     * </p>
     * <p class="nonnormative">
     * Typically the project's <code>build.xml</code> and/or <code>nbproject/build-impl.xml</code>
     * may need to be (re-)generated; see {@link GeneratedFilesHelper} for details.
     * </p>
     * @throws IOException if running the hook failed for some reason
     */
    protected abstract void projectXmlSaved() throws IOException;
    
}

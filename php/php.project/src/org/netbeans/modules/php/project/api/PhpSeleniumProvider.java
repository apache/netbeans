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

package org.netbeans.modules.php.project.api;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;

/**
 * This class can be found in project's lookup and used by Selenium module.
 * @author Tomas Mysik
 * @since 2.9
 */
public interface PhpSeleniumProvider {

    /**
     * Get the root directory of Selenium test files. Return <code>null</code>
     * if such directory is not set.
     * @param showCustomizer <code>true</code> if a dialog which allows to select such directory should be shown
     *                       (suitable for cases when such folder is not set up yet)
     * @return the root directory of Selenium test files or <code>null</code> if such directory is not set
     */
    FileObject getTestDirectory(boolean showCustomizer);

    /**
     * Run all the Selenium tests (similar to Test action on project's node).
     * <p>
     * Does nothing in case that {@link #getSeleniumTestDirectory(boolean)} returns <code>null</code>.
     */
    void runAllTests();
    
    /**
     * Check whether this implementation supports given FileObjects.
     *
     * @param activatedFOs FileObjects to check
     * @return {@code true} if this instance supports given FileObjects, {@code false} otherwise
     */
    boolean isSupportEnabled(FileObject[] activatedFOs);
    
    /**
     * Finds <code>SourceGroup</code>s where a test for the given class
     * can be created (so that it can be found by the projects infrastructure
     * when a test for the class is to be opened or run).
     *
     * @param createdSourceRoots
     * @param  fileObject  <code>FileObject</code> to find target
     *                     <code>SourceGroup</code>(s) for
     * @return  a list of objects - each of them can be either
     *          a <code>SourceGroup</code> for a possible target folder
     *          or simply a <code>FileObject</code> representing a possible
     *          target folder (if <code>SourceGroup</code>) for the folder
     *          was not found);
     *          the returned list may be empty but not <code>null</code>
     */
    List<Object> getTestSourceRoots(Collection<SourceGroup> createdSourceRoots, FileObject refFileObject);
}

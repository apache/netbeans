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

import java.util.EventObject;

/**
 * Event object corresponding to a change made in an Ant project's metadata.
 * The event source is an {@link AntProjectHelper}.
 * @see AntProjectListener
 * @author Jesse Glick
 */
public final class AntProjectEvent extends EventObject {

    private final String path;
    private final boolean expected;

    AntProjectEvent(AntProjectHelper helper, String path, boolean expected) {
        super(helper);
        this.path = path;
        this.expected = expected;
    }
    
    /**
     * Get the associated Ant project helper object.
     * @return the project helper which fired the event
     */
    public AntProjectHelper getHelper() {
        return (AntProjectHelper)getSource();
    }
    
    /**
     * Get the path to the modified (or created or deleted) file.
     * Paths typically used are:
     * <ol>
     * <li>{@link AntProjectHelper#PROJECT_PROPERTIES_PATH}
     * <li>{@link AntProjectHelper#PRIVATE_PROPERTIES_PATH}
     * <li>{@link AntProjectHelper#PROJECT_XML_PATH}
     * <li>{@link AntProjectHelper#PRIVATE_XML_PATH}
     * </ol>
     * However for properties files, other paths may exist if the project
     * uses them for some purpose.
     * @return a project-relative path
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Check whether the change was produced by calling methods on
     * {@link AntProjectHelper} or whether it represents a change
     * detected on disk.
     * @return true if the change was triggered by in-memory modification methods,
     *         false if occurred on disk in the metadata files and is being loaded
     */
    public boolean isExpected() {
        return expected;
    }
    
}

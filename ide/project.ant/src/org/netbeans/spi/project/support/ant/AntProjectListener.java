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

import java.util.EventListener;

/**
 * Listener for changes in Ant project metadata.
 * Most changes are in-memory while the project is still modified, but changes
 * may also be on disk.
 * <p>Event methods are fired with read access to
 * {@link org.netbeans.api.project.ProjectManager#mutex}.
 * @author Jesse Glick
 */
public interface AntProjectListener extends EventListener {

    /**
     * Called when a change was made to an XML project configuration file.
     * @param ev an event with details of the change
     */
    void configurationXmlChanged(AntProjectEvent ev);
    
    /**
     * Called when a change was made to a properties file that might be shared with Ant.
     * <p class="nonnormative">
     * Note: normally you would not use this event to detect property changes.
     * Use the property change listener from {@link PropertyEvaluator} instead to find
     * changes in the interpreted values of Ant properties, possibly coming from multiple
     * properties files.
     * </p>
     * @param ev an event with details of the change
     */
    void propertiesChanged(AntProjectEvent ev);
    
}

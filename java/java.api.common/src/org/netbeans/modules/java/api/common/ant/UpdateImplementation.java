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

package org.netbeans.modules.java.api.common.ant;

import java.io.IOException;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.w3c.dom.Element;

/**
 * Interface that has to be implemented in order to use {@link UpdateHelper}. It represents and does the project update
 * process itself.
 * @author Tomas Mysik
 * @see UpdateHelper
 */
public interface UpdateImplementation {

    /**
     * Return <code>true</code> if the project is of current version.
     * @return <code>true</code> if the project is of current version.
     */
    boolean isCurrent();

    /**
     * Return <code>true</code> if the project can be updated.
     * @return <code>true</code> if the project can be updated.
     */
    boolean canUpdate();

    /**
     * Saving of update. If the project is of current version it should probably do nothing.
     * @param props project properties to be saved, can be <code>null</code>. There's no need to save them because
     *          {@link UpdateHelper} does it.
     * @throws IOException if error occurs during saving.
     */
    void saveUpdate(final EditableProperties props) throws IOException;

    /**
     * Creates probably an in memory update of shared configuration data and return it.
     * @return the configuration data that is available.
     * @see UpdateHelper#getPrimaryConfigurationData(boolean)
     */
    Element getUpdatedSharedConfigurationData();

    /**
     * Creates probably an in memory update of project properties.
     * @return a set of properties.
     * @see UpdateHelper#getProperties(String)
     */
    EditableProperties getUpdatedProjectProperties();
}

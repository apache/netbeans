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

package org.netbeans.modules.web.clientproject.spi.jstesting;

import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.web.clientproject.api.jstesting.Coverage;

/**
 * Interface representing code coverage.
 * @see org.netbeans.modules.web.clientproject.api.util.CoverageProviderImpl
 * @since 1.58
 */
public interface CoverageImplementation {

    /**
     * Property name for changes in enabled state.
     */
    String PROP_ENABLED = "ENABLED"; // NOI18N

    /**
     * Checks whether coverage is enabled or not.
     * @return {@code true} if coverage is enabled, {@code false} otherwise
     */
    boolean isEnabled();

    /**
     * Sets coverage data for individual files.
     * @param files coverage data for individual files
     */
    void setFiles(@NonNull List<Coverage.File> files);

    /**
     * Adds property change listener.
     * @param listener listener to be added, can be {@code null}
     */
    void addPropertyChangeListener(@NullAllowed PropertyChangeListener listener);

    /**
     * Removes property change listener.
     * @param listener listener to be removed, can be {@code null}
     */
    void removePropertyChangeListener(@NullAllowed PropertyChangeListener listener);

}

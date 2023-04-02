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
package org.netbeans.modules.gradle.spi.execute;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager.GradleDistribution;

/**
 * Projects can provide the required Gradle Distribution through this interface,
 * by placing an implementation of it in the project lookup.
 *
 * @since 2.4
 * @author lkishalmi
 */
public interface GradleDistributionProvider {

    /**
     * Shall return the {@link GradleDistribution} used by the project.
     * It may return <code>null</code> if the project does not have specific
     * GradleDistribution requirements. Gradle defaults of the actual project
     * and tooling API would be used in that case (not recommended).
     *
     * @return The {@link GradleDistribution} to use for the project.
     */
    GradleDistribution getGradleDistribution();

    /**
     * Add a {@link ChangeListener} to be notified when the required
     * {@link GradleDistribution} changes for the project;
     *
     * @param l the {@link ChangeListener}
     */
    void addChangeListener(ChangeListener l);

    /**
     * Remove a registered {@link ChangeListener}.
     * @param l the {@link ChangeListener}
     */
    void removeChangeListener(ChangeListener l);

}

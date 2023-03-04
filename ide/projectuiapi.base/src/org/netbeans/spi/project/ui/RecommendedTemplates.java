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

package org.netbeans.spi.project.ui;

/**
 * List of template types supported by a project when making a new file.
 * An instance should be placed in {@link org.netbeans.api.project.Project#getLookup}
 * to affect the recommended template list for that project.
 * <p>
 * For more information about registering templates see overview of
 * <a href="@org-netbeans-modules-projectuiapi@/org/netbeans/spi/project/ui/templates/support/package-summary.html">org.netbeans.spi.project.ui.templates.support</a> package.
 * @author Petr Hrebejk
 */
public interface RecommendedTemplates {

    /**
     * Lists supported template types.
     * @return types of supported templates (should match template file attribute names)
     * @see <code>org.netbeans.api.templates.TemplateRegistration.#category</code>
     */
    public String[] getRecommendedTypes();
    
}

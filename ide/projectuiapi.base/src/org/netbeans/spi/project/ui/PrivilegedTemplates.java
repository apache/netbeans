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

package org.netbeans.spi.project.ui;

/**
 * List of templates which should be in the initial "privileged" list
 * when making a new file.
 * An instance should be placed in {@link org.netbeans.api.project.Project#getLookup}
 * to affect the privileged list for that project.
 * 
 * <p>
 * Since 1.28, the PrivilegedTemplates instance can also reside in active node's lookup
 * and such instance will be used instead of the default one.
 * 
 * <p>
 * For more information about registering templates see overview of
 * <a href="@org-netbeans-modules-projectuiapi@/org/netbeans/spi/project/ui/templates/support/package-summary.html">org.netbeans.spi.project.ui.templates.support</a> package.
 * @see <a href="@org-netbeans-modules-projectuiapi@/org/netbeans/spi/project/ui/support/CommonProjectActions.html">CommonProjectActions</a>
 * @author Petr Hrebejk
 */
public interface PrivilegedTemplates {
    
    /**
     * Lists privileged templates.
     * @return full paths to privileged templates, e.g. <code>Templates/Other/XmlFile.xml</code>
     * @see <code>org.netbeans.api.templates.TemplateRegistration#folder</code>
     * @see <code>org.netbeans.api.templates.TemplateRegistration#content</code>
     * @see <code>org.netbeans.api.templates.TemplateRegistration#id</code>
     */
    public String[] getPrivilegedTemplates();
    
}

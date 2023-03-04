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

package org.apache.tools.ant.module.spi;

import java.io.File;

/**
 * Permits a module to register special additional JARs (or directories)
 * to be added to Ant's primary classpath by default.
 * This should <em>only</em> be used to supply libraries needed by
 * standard "optional" tasks that ship with Ant - e.g. <code>junit.jar</code>
 * as needed by the <code>&lt;junit&gt;</code> task.
 * Register instances to default lookup.
 * <p>
 * Since version <code>org.apache.tools.ant.module/3 3.26</code> there is a
 * way to register a library declaratively. Just put fragment like this into
 * your layer file:
 * <pre>
 * &lt;filesystem&gt;
 *   &lt;folder name="Services"&gt;
 *     &lt;folder name="Hidden"&gt;
 *       &lt;file name="org-your-lib-ant-registration.instance"&gt;
 *         &lt;attr name="instanceCreate" methodvalue="org.apache.tools.ant.module.spi.AutomaticExtraClasspath.url"/&gt;
 *         &lt;attr name="url" urlvalue="nbinst://org.your.module.name/modules/ext/org-your-lib.jar"/&gt;
 *         &lt;attr name="instanceOf" stringvalue="org.apache.tools.ant.module.spi.AutomaticExtraClasspathProvider"/&gt;
 *       &lt;/file&gt;
 *     &lt;/folder&gt;
 *   &lt;/folder&gt;
 * &lt;/filesystem&gt;
 * </pre>
 *
 *
 * @since org.apache.tools.ant.module/3 3.8
 * @author Jesse Glick
 */
public interface AutomaticExtraClasspathProvider {
    
    /**
     * Return a (possibly empty) list of items to add to the
     * automatic classpath used by default when running Ant.
     * Note that the result is not permitted to change between calls
     * in the same VM session.
     * <p>
     * The user may be able to override this path, so there is no
     * firm guarantee that the items will be available when Ant is run.
     * However by default they will be.
     * @return zero or more directories or JARs to add to Ant's startup classpath
     * @see org.openide.modules.InstalledFileLocator
     */
    File[] getClasspathItems();
    
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

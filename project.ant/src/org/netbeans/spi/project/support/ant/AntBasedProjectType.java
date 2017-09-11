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

package org.netbeans.spi.project.support.ant;

import java.io.IOException;
import org.netbeans.api.project.Project;

/**
 * Plugin for an Ant project type.
 * Register one instance to default lookup in order to define an Ant project type.
 * @author Jesse Glick
 */
public interface AntBasedProjectType {

    /**
     * Get a unique type identifier for this kind of project.
     * No two registered {@link AntBasedProjectType} instances may share the same type.
     * The type is stored in <code>nbproject/project.xml</code> in the <code>type</code> element.
     * It is forbidden for the result of this method to change from call to call.
     * @return the project type
     */
    String getType();
    
    /**
     * Create the project object with a support class.
     * Normally the project should retain a reference to the helper object in
     * order to implement various required methods.
     * Do <em>not</em> do any caching here; the infrastructure will call this
     * method only when the project needs to be loaded into memory.
     * @param helper a helper object encapsulating the generic project structure
     * @return a project implementation
     * @throws IOException if there is some problem loading additional data
     */
    Project createProject(AntProjectHelper helper) throws IOException;
    
    /**
     * Get the simple name of the XML element that should be used to store
     * the project's specific configuration data in <code>nbproject/project.xml</code>
     * (inside <code>&lt;configuration&gt;</code>) or <code>nbproject/private/private.xml</code>
     * (inside <code>&lt;project-private&gt;</code>).
     * It is forbidden for the result of this method to change from call to call.
     * @param shared if true, refers to <code>project.xml</code>, else refers to
     *               <code>private.xml</code>
     * @return a simple name; <samp>data</samp> is recommended but not required
     */
    String getPrimaryConfigurationDataElementName(boolean shared);
    
    /**
     * Get the namespace of the XML element that should be used to store
     * the project's specific configuration data in <code>nbproject/project.xml</code>
     * (inside <code>&lt;configuration&gt;</code>) or <code>nbproject/private/private.xml</code>
     * (inside <code>&lt;project-private&gt;</code>).
     * It is forbidden for the result of this method to change from call to call.
     * @param shared if true, refers to <code>project.xml</code>, else refers to
     *               <code>private.xml</code>
     * @return an XML namespace, e.g. <samp>http://www.netbeans.org/ns/j2se-project</samp>
     *         or <samp>http://www.netbeans.org/ns/j2se-project-private</samp>
     */
    String getPrimaryConfigurationDataElementNamespace(boolean shared);

}

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
package org.netbeans.spi.project;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;

/**
 * Project Copy Operation. Allows to gather information necessary for project
 * copy and also provides callbacks to the project type to handle special
 * checkpoints during the copy process.
 *
 * An implementation of this interface may be registered in the project's lookup to support
 * copy operation in the following cases:
 * <ul>
 *     <li>The project type wants to use
 * <a href="@org-netbeans-modules-projectuiapi@/org/netbeans/spi/project/ui/support/DefaultProjectOperations.html"><code>DefaultProjectOperations</code></a>
 *         to perform the copy operation.
 *    </li>
 *    <li>If this project may be part of of a compound project (like EJB project is a part of a J2EE project),
 *        and the compound project wants to copy all the sub-projects.
 *    </li>
 * </ul>
 *
 * The project type is not required to put an implementation of this interface into the project's
 * lookup if the above two cases should not be supported.
 *
 * @author Jan Lahoda
 * @since 1.7
 */
public interface CopyOperationImplementation extends DataFilesProviderImplementation {
    
    /**Pre-copy notification. The exact meaning is left on the project implementors, but
     * typically this means to undeploy the application and remove all artifacts
     * created by the build project.
     *
     * @throws IOException if an I/O operation fails.
     */
    public void notifyCopying() throws IOException;
    
    /**Notification that the copy operation has finished. Is supposed to fix the
     * newly created (copied) project into the correct state (including changing its display name
     * to nueName). Should be called on both original and newly created project (in this order).
     *
     * @param original <code>null</code> when called on the original project, the original project when called on the new project
     * @param originalPath the project folder of the original project (for consistency
     *                     with MoveOperationImplementation.notifyMoved)
     * @param nueName new name for the newly created project.
     *
     * @throws IOException if an I/O operation fails.
     */
    public void notifyCopied(Project original, File originalPath, String nueName)  throws IOException;
    
}

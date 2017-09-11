/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.spi.actions;

import java.util.Set;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Interface that allows to put additional items to project's popup plus to provide specific
 * implementations of ActionProvider actions.
 * Implementations should be registered in default lookup using {@link ServiceProvider},
 * or since 2.50 may also be registered using {@link ProjectServiceProvider} if applicable to just some packagings.
 * 
 * @author  Milos Kleint
 */
public interface MavenActionsProvider {

    
    /**
     * Create an instance of RunConfig configured for execution.
     * @param actionName one of the ActionProvider constants
     * @returns RunConfig or null, if action not supported
     */
    RunConfig createConfigForDefaultAction(String actionName, Project project, Lookup lookup);

    /**
     * get a action to maven mapping configuration for the given action. No context specific value replacements
     * happen.
     * @return
     */
    NetbeansActionMapping getMappingForAction(String actionName, Project project);

    /**
     * return is action is supported or not
     * @param action action name, see ActionProvider for details.
     * @param project project that the action is invoked on.
     * @param lookup context for the action
     * @return
     */
    boolean isActionEnable(String action, Project project, Lookup lookup);

    /**
     * returns a list of supported actions, see ActionProvider.getSupportedActions()
     * @return
     */
    Set<String> getSupportedDefaultActions();
}

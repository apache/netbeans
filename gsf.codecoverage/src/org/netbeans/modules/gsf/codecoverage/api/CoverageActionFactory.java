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
package org.netbeans.modules.gsf.codecoverage.api;

import javax.swing.Action;
import org.netbeans.modules.gsf.codecoverage.CoverageAction;

/**
 * This factory creates actions suitable for insertion into project context menus
 * to decorate a project type with code coverage capability.
 * (Note - you also have to add a {@link CoverageManager} to the project's
 * lookup as well.)
 *
 * @author Tor Norbye
 */
public class CoverageActionFactory {
    /**
     * Create a project sensitive context menu action named "Code Coverage" which
     * will provide actions for enabling/disable code coverage collection, showing
     * a code coverage report, etc.
     *
     * @param configureAction Usually null/empty, but if not null, pass in a new action
     *  which will be added at the top of the code coverage menu for adding support
     *  for code coverage. This typically provides some kind of framework specific
     *  support. For example, in Ruby, if the "rcov" gem isn't installed, code coverage
     *  will be disabled until it is installed, and this action is a "Install RCov"
     *  action which when executed should do whatever it takes to add in support
     *  for code coverage. It is this action's responsibility to call
     *  {@link CoverageManager#setEnabled()} when it is done to indicate that the
     *  actions should be re-enabled if applicable.
     *
     * @param extraActions An optional set of extra actions to add to the menu after
     *   the regular code coverage actions for extra optional coverage related features
     *   provided by your module.
     * @return An action suitable for inclusion in a project context menu
     */
    public static Action createCollectorAction(Action configureAction, Action[] extraActions) {
        //return CoverageAction.get(CoverageAction.class);
        return new CoverageAction(configureAction, extraActions);
    }
}

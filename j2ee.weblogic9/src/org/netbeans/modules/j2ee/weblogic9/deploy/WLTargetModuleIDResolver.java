/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.weblogic9.deploy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver;

/**
 *
 * @author Petr Hejl
 */
public class WLTargetModuleIDResolver extends TargetModuleIDResolver {

    private final WLDeploymentManager dm;

    public WLTargetModuleIDResolver(WLDeploymentManager dm) {
        this.dm = dm;
    }

    @Override
    public TargetModuleID[] lookupTargetModuleID(Map targetModuleInfo, Target[] targetList) {
        String contextRoot = (String) targetModuleInfo.get(KEY_CONTEXT_ROOT);
        if (contextRoot == null) {
            return EMPTY_TMID_ARRAY;
        }
        // WAR modules in EAR contains slash in name
        String noSlashContextRoot = contextRoot;
        if (contextRoot.startsWith("/")) { // NOI18N
            noSlashContextRoot = contextRoot.substring(1);
        }

        ArrayList result = new ArrayList();
        try {
            addCollisions(contextRoot, noSlashContextRoot, result, dm.getAvailableModules(ModuleType.WAR, targetList));
        } catch (Exception ex) {
            Logger.getLogger(WLTargetModuleIDResolver.class.getName()).log(Level.INFO, null, ex);
        }

        return (TargetModuleID[]) result.toArray(new TargetModuleID[result.size()]);
    }

    private void addCollisions(String contextRoot, String noSlashContextRoot, List<TargetModuleID> result, TargetModuleID[] candidates) {
        if (candidates == null) {
            return;
        }
        for (int i = 0; i < candidates.length; i++) {
            TargetModuleID tm = candidates[i];
            if (contextRoot.equals(tm.getModuleID()) || noSlashContextRoot.equals(tm.getModuleID())) {
                TargetModuleID parent = tm.getParentTargetModuleID();
                if (parent != null) {
                    result.add(parent);
                } else {
                    result.add(tm);
                }
            }
        }
    }
}

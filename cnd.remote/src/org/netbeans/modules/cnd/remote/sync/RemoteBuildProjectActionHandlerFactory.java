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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.sync;

import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.PredefinedType;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.Type;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandlerFactory;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * ProjectActionHandlerFactory for remote builds using RFS
 */
@ServiceProvider(service=ProjectActionHandlerFactory.class, position=3000)
public class RemoteBuildProjectActionHandlerFactory implements ProjectActionHandlerFactory {

    private static boolean canHandleType(Type type) {
        if (type instanceof PredefinedType) {
            PredefinedType predefinedType = (PredefinedType) type;
            switch (predefinedType) {
                case PRE_BUILD:
                case BUILD:
                case BUILD_TESTS:
                case CLEAN:
                case COMPILE_SINGLE:
                    return true;
                case RUN:
                case DEBUG:
                case DEBUG_STEPINTO:
                case ATTACH:
                case DEBUG_TEST:
                case DEBUG_STEPINTO_TEST:
                case CHECK_EXECUTABLE:
                case CUSTOM_ACTION:
                case TEST:
                    return false;
                default:
                    AssertionError e = new AssertionError("Unexpected action type " + predefinedType.name()); //NOI18N
                    if (CndUtils.isDebugMode()) {
                        throw e;
                    } else {
                        e.printStackTrace(System.err);
                    }
            }
        }
        return false;
    }

    @Override
    public boolean canHandle(Type type, Lookup context, Configuration configuration) {
        if (canHandleType(type)) {
            if (configuration instanceof MakeConfiguration) {
                MakeConfiguration conf = (MakeConfiguration) configuration;
                if (conf.getDevelopmentHost().getExecutionEnvironment().isRemote()) {
                    return RfsSyncFactory.ENABLE_RFS;
                }
            }
        }
        return false;
    }

    @Override
    public ProjectActionHandler createHandler() {
        return new RemoteBuildProjectActionHandler();
    }

    /* package-local */
    static ProjectActionHandler createDelegateHandler(ProjectActionEvent pae) {
        boolean selfFound = false;
        for (ProjectActionHandlerFactory factory : Lookup.getDefault().lookupAll(ProjectActionHandlerFactory.class)) {
            if (factory instanceof RemoteBuildProjectActionHandlerFactory) {
                selfFound = true;
            } else if (selfFound) {
                if (factory.canHandle(pae)) {
                    return factory.createHandler();
                }
            }
        }
        return null;
    }

    @Override
    public boolean canHandle(ProjectActionEvent pae) {
        return canHandle(pae.getType(), pae.getContext(), pae.getConfiguration());
    }
}

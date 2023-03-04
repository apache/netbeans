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

package org.netbeans.modules.profiler.freeform;

import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;
import java.io.IOException;
import java.util.*;
import org.apache.tools.ant.module.api.support.AntScriptUtils;


/**
 * Miscellaneous utilities.
 * (Ian Formanek: Copied from FreeForm module)
 *
 * @author Jesse Glick
 */
public final class Util {
                                                                                                         // -----
    public static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.profiler.freeform"); // NOI18N

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    private Util() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Returns XML element representing the requested target or null if the target does not exist.
     *
     * @param fo         Ant script which target names should be returned
     * @param targetName the String name of the requested target
     * @return XML element representing the requested target or null if the target does not exist.
     */
    public static Element getAntScriptTarget(final FileObject fo, final String targetName) {
        if (fo == null) {
            throw new IllegalArgumentException("Cannot call Util.getAntScriptTargetNames with null"); // NOI18N
        }

        final AntProjectCookie apc = AntScriptUtils.antProjectCookieFor(fo);

        final Set /*TargetLister.Target*/ allTargets;

        try {
            allTargets = TargetLister.getTargets(apc);
        } catch (IOException e) {
            err.notify(ErrorManager.INFORMATIONAL, e);

            return null;
        }

        final Iterator it = allTargets.iterator();

        while (it.hasNext()) {
            final TargetLister.Target target = (TargetLister.Target) it.next();

            if (target.isOverridden()) {
                // Cannot call it directly.
                continue;
            }

            if (target.isInternal()) {
                // Should not be called from outside.
                continue;
            }

            if (targetName.equals(target.getName())) {
                return target.getElement();
            }
        }

        return null;
    }

    public static List /*TargetLister.Target*/ getAntScriptTargets(final FileObject buildScript) {
        if (buildScript == null) {
            throw new IllegalArgumentException("Cannot call Util.getAntScriptTargetNames with null"); // NOI18N
        }

        final AntProjectCookie apc = AntScriptUtils.antProjectCookieFor(buildScript);

        final Set /*TargetLister.Target*/ allTargets;

        try {
            allTargets = TargetLister.getTargets(apc);
        } catch (IOException e) {
            err.notify(ErrorManager.INFORMATIONAL, e);

            return null;
        }

        final ArrayList targets = new ArrayList();
        final Iterator it = allTargets.iterator();

        while (it.hasNext()) {
            final TargetLister.Target target = (TargetLister.Target) it.next();

            if (target.isOverridden()) {
                // Cannot call it directly.
                continue;
            }

            if (target.isInternal()) {
                // Should not be called from outside.
                continue;
            }

            targets.add(target);
        }

        return targets;
    }

    public static FileObject getProjectBuildScript(final Project project) {
        return org.netbeans.modules.ant.freeform.spi.support.Util.getDefaultAntScript(project);
    }

}

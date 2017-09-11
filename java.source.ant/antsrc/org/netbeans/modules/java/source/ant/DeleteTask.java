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

package org.netbeans.modules.java.source.ant;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.netbeans.modules.java.source.parsing.CachingArchiveClassLoader;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class DeleteTask extends Delete {

    private static final boolean ALL_OS = Boolean.getBoolean("DeleteTask.lock.allOS");  //NOI18N
    private static final Logger LOG = Logger.getLogger(DeleteTask.class.getName());
    private static final String PROP_DIST_DIR = "dist.dir"; //NOI18N

    public DeleteTask() {
    }

    @Override
    public void execute() throws BuildException {
        final boolean needsLock = requiresExclusion();
        final Callable<Void> action = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                DeleteTask.super.execute();
                return null;
            }
        };
        try {
            if (needsLock) {
                CachingArchiveClassLoader.writeAction(action);
            } else {
                action.call();
            }
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private boolean requiresExclusion() {
        final boolean osCond = ALL_OS || Os.isFamily("windows");    //NOI18N
        final boolean distCond = isTarget(PROP_DIST_DIR, file, dir);
        final boolean allCond = osCond && distCond;
        LOG.log(
            Level.FINE,
            "Requires exclusion: {0}, [os: {1}, dist: {2}]",    //NOI18N
            new Object[]{
                allCond,
                osCond,
                distCond
            });
        return allCond;
    }

    private boolean isTarget(
        final String propName,
        final File... targets) {
        final Project p = getProject();
        final String propVal = p.getProperty(propName);
        if (propVal == null) {
            return false;
        }
        final File resolvedFile = p.resolveFile(propVal);
        if (resolvedFile == null) {
            return false;
        }
        final File normalizedResolvedFile = FileUtil.normalizeFile(resolvedFile);
        for (File target : targets) {
            if (target == null) {
                continue;
            }
            final File normalizedTarget = FileUtil.normalizeFile(target);
            if (isParentOf(normalizedTarget, normalizedResolvedFile)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isParentOf(final File folder, File file) {
        while (file != null) {
            if (folder.equals(file)) {
                return true;
            }
            file = file.getParentFile();
        }
        return false;
    }
}

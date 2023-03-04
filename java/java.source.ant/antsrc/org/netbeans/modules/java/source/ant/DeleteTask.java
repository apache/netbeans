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

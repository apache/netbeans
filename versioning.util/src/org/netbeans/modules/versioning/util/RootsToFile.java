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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.versioning.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tomas
 */
public class RootsToFile {
    private final Map<File, File> files = new LinkedHashMap<File, File>() {
        @Override
        protected boolean removeEldestEntry (Map.Entry<File, File> eldest) {
            return size() > 1500;
        }
    };
    private long cachedAccesCount = 0;
    private long accesCount = 0;
    private final int statisticsFrequency;
    private final Callback callback;
    private final Logger log;

    public RootsToFile (Callback callback, Logger log, int statisticsFrequency) {
        this.statisticsFrequency = statisticsFrequency;
        this.callback = callback;
        this.log = log;
    }
    synchronized void put (Collection<File> files, File root) {
        for (File file : files) {
            put(file, root);
        }
    }
    synchronized void put (File file, File root) {
        files.put(file, root);
    }
    synchronized File get (File file) {
        return get(file, false);
    }
    synchronized File get (File file, boolean statistics) {
        File root = files.get(file);
        if(statistics && log.isLoggable(Level.FINEST)) {
           cachedAccesCount += root != null ? 1 : 0;
           accesCount++;
        }
        return root;
    }
    synchronized int size () {
        return files.size();
    }
    synchronized void logStatistics () {
        if(!log.isLoggable(Level.FINEST) ||
           (statisticsFrequency > 0 && (accesCount % statisticsFrequency != 0)))
        {
            return;
        }

        log.finest("Repository roots cache statistics:\n" +                                 // NOI18N
                 "  cached roots size       = " + files.size() + "\n" +                         // NOI18N
                 "  access count            = " + accesCount + "\n" +                           // NOI18N
                 "  cached access count     = " + cachedAccesCount + "\n" +                     // NOI18N
                 "  not cached access count = " + (accesCount - cachedAccesCount) + "\n");      // NOI18N
    }

    public synchronized void clear () {
        files.clear();
        cachedAccesCount = 0;
        accesCount = 0;
    }

    public File getRepositoryRoot (File file) {
        File oFile = file;

        logStatistics();
        File root = get(file, true);
        if(root != null) {
            return root;
        }

        root = callback.getTopmostManagedAncestor(file);
        if(root != null) {
            if(file.isFile()) file = file.getParentFile();
            List<File> folders = new ArrayList<File>();
            for (; file != null && !file.getAbsolutePath().equals(root.getAbsolutePath()) ; file = file.getParentFile()) {
                File knownRoot = get(file);
                if(knownRoot != null) {
                    put(folders, knownRoot);
                    put(oFile, knownRoot);
                    return knownRoot;
                }
                folders.add(file);
                if(callback.repositoryExistsFor(file)) {
                    put(folders, file);
                    put(oFile, file);
                    return file;
                }
            }
            folders.add(root);
            put(folders, root);
            put(oFile, root);
            return root;
        }
        return null;
    }

    public static interface Callback {
        public boolean repositoryExistsFor (File file);
        public File getTopmostManagedAncestor(File file);
    }
}

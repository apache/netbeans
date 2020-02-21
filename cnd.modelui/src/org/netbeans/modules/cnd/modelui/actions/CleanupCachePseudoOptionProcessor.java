/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */

package org.netbeans.modules.cnd.modelui.actions;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.modules.Places;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.sendopts.OptionProcessor.class)
public class CleanupCachePseudoOptionProcessor extends OptionProcessor {
    private static final String[] CACHES = new String[] {
                 "cnd" // NOI18N
                //,"remote-files" // NOI18N
                ,"index" // NOI18N
                //,"svnremotecache" // NOI18N
            };
    private static final String MARKER_FILE = "cnd-cleanup"; // NOI18N
            
    @Override
    protected Set<Option> getOptions() {
        for(String cache:CACHES) {
            cleanupCache(cache);
        }
        return Collections.emptySet();
    }
    
    @Override
    protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
    }

    static void markToCleanup() {
        for(String cache:CACHES) {
            markToCleanup(cache);
        }
    }

    private static void markToCleanup(String cacheName) {
        File cache = Places.getCacheSubdirectory(cacheName);
        if (cache.exists()) {
            File cleanupFile = new File(cache, MARKER_FILE);
            if (!cleanupFile.exists()) {
                try {
                    cleanupFile.createNewFile();
                } catch (IOException x) {
                }
            }
        }
    }
    
    private static void cleanupCache(String cacheName) {
        File cacheDirectory = Places.getCacheDirectory();
        if (cacheDirectory.exists()) {
            File cache = new File(cacheDirectory, cacheName);
            if (cache.exists()) {
                File cleanupFile = new File(cache, MARKER_FILE); 
                if (cleanupFile.exists()) {
                    deleteRecursively(cache);
                }
            }
        }
    }

    public static void deleteRecursively(File file) {
        File [] files = file.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }
        file.delete();
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.JavadocForBinaryQuery.Result;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Able to find Javadoc in the appropriate NbPlatform for the given URL.
 *
 * @author Jesse Glick, Martin Krauskopf
 */
@ServiceProvider(service=JavadocForBinaryQueryImplementation.class)
public final class GlobalJavadocForBinaryImpl implements JavadocForBinaryQueryImplementation {
    
    public @Override JavadocForBinaryQuery.Result findJavadoc(final URL root) {
        try {
            if (root.getProtocol().equals("jar")) { // NOI18N
                return findForBinaryRoot(root);
            } else {
                return findForSourceRoot(root);
            }
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    private Result findForBinaryRoot(final URL binaryRoot) throws MalformedURLException, MalformedURLException {
        URL jar = FileUtil.getArchiveFile(binaryRoot);
        if (!jar.getProtocol().equals("file")) { // NOI18N
            Util.err.log(binaryRoot + " is not an archive file."); // NOI18N
            return null;
        }
        File binaryRootF = Utilities.toFile(URI.create(jar.toExternalForm()));
        // XXX this will only work for modules following regular naming conventions:
        String n = binaryRootF.getName();
        if (!n.endsWith(".jar")) { // NOI18N
            Util.err.log(binaryRootF + " is not a *.jar"); // NOI18N
            return null;
        }
        String cnbdashes = n.substring(0, n.length() - 4);
        NbPlatform supposedPlaf = null;
        for (NbPlatform plaf : NbPlatform.getPlatformsOrNot()) {
            if (binaryRootF.getAbsolutePath().startsWith(plaf.getDestDir().getAbsolutePath() + File.separator)) {
                supposedPlaf = plaf;
                break;
            }
        }
        if (supposedPlaf == null) {
            // try external clusters
            URL[] javadocRoots = ModuleList.getJavadocRootsForExternalModule(binaryRootF);
            if (javadocRoots.length > 0) {
                return findByDashedCNB(cnbdashes, javadocRoots, true);
            }
            Util.err.log(binaryRootF + " does not correspond to a known platform"); // NOI18N
            return null;
        }
        Util.err.log("Platform in " + supposedPlaf.getDestDir() + " claimed to have Javadoc roots "
            + Arrays.asList(supposedPlaf.getJavadocRoots()));
        return findByDashedCNB(cnbdashes, supposedPlaf.getJavadocRoots(), true);
    }

    /**
     * Go through all registered platforms and tries to find Javadoc for the
     * given URL.
     * <p>
     * <em>TODO</em>: ideally should check module, or at least cluster, version.
     */
    private Result findForSourceRoot(final URL root) throws MalformedURLException {
        Project p ;
        try {
            p = FileOwnerQuery.getOwner(root.toURI());
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
        if (p != null) {
            NbModuleProject module = p.getLookup().lookup(NbModuleProject.class);
            if (module != null) {
                String cnb = module.getCodeNameBase();
    //  TODO C.P scan external clusters? Doesn't seem necessary, javadoc is built from source on the fly for clusters with sources
                NbPlatform plaf = module.getPlatform(false);
                if (plaf != null) {
                    Util.err.log("Platform in " + plaf.getDestDir() + " claimed to have Javadoc roots "
                            + Arrays.asList(plaf.getJavadocRoots()));
                    Result r = findByDashedCNB(cnb.replace('.', '-'), plaf.getJavadocRoots(), false);
                    if (r != null) {
                        return r;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Map from Javadoc root URLs to whether it is known to actually exist.
     */
    private static final Map<String,Boolean> knownGoodJavadoc = Collections.synchronizedMap(new HashMap<String,Boolean>());
   
    @SuppressWarnings("DE_MIGHT_IGNORE")
    private Result findByDashedCNB(final String cnbdashes, final URL[] roots, boolean allowRemote) throws MalformedURLException {
        final List<URL> candidates = new ArrayList<URL>();
        for (URL root : roots) {
            // XXX: so should be checked, instead of always adding both?
            // 1. user may insert directly e.g ...nbbuild/build/javadoc/org-openide-actions[.zip]
            candidates.add(root);
            // 2. or whole bunch of javadocs e.g. ...nbbuild/build/javadoc/
            candidates.add(new URL(root, cnbdashes + '/'));
        }
        Iterator<URL> it = candidates.iterator();
        while (it.hasNext()) {
            URL u = it.next();
            if (URLMapper.findFileObject(u) == null) {
                String uS = u.toString();
                Boolean knownGood = knownGoodJavadoc.get(uS);
                if (knownGood == null) {
                    knownGood = false;
                    // Do not check, or cache, non-network URLs.
                    if (allowRemote && uS.startsWith("http")) { // NOI18N
//                        System.err.println("need to check " + uS);
                        try {
                            new URL(u, "package-list").openStream().close();
                            knownGood = true;
                        } catch (IOException x) {/* failed */}
                        knownGoodJavadoc.put(uS, knownGood);
                    }
                }
                if (!knownGood) {
                    Util.err.log("No such Javadoc candidate URL " + u);
                    it.remove();
                }
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return new JavadocForBinaryQuery.Result() {
            public @Override URL[] getRoots() {
                return candidates.toArray(new URL[0]);
            }
            public @Override void addChangeListener(ChangeListener l) {}
            public @Override void removeChangeListener(ChangeListener l) {}
        };
    }

}

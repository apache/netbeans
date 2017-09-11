/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** Keeps the coverage of various packages by existing ProxyClassLoaders.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class ProxyClassPackages {
    private ProxyClassPackages() {
    }
    
    /** A shared map of all packages known by all classloaders. Also covers META-INF based resources.
     * It contains two kinds of keys: dot-separated package names and slash-separated
     * META-INF resource names, e.g. {"org.foobar", "/services/org.foobar.Foo"}
     */
    private static final Map<String, Set<ProxyClassLoader>> packageCoverage = new HashMap<String, Set<ProxyClassLoader>>();

    synchronized static void addCoveredPackages(
        ProxyClassLoader loader, Iterable<String> coveredPackages
    ) {
        for (String pkg : coveredPackages) {
            Set<ProxyClassLoader> delegates = ProxyClassPackages.packageCoverage.get(pkg); 
            if (delegates == null) { 
                delegates = Collections.<ProxyClassLoader>singleton(loader);
                ProxyClassPackages.packageCoverage.put(pkg, delegates); 
            } else if (delegates.size() == 1) {
                delegates = new HashSet<ProxyClassLoader>(delegates);
                ProxyClassPackages.packageCoverage.put(pkg, delegates);
                delegates.add(loader); 
            } else {
                delegates.add(loader);
            }
        }
    }
    
    synchronized static void removeCoveredPakcages(
        ProxyClassLoader loader
    ) {
        for (Iterator<String> it = ProxyClassPackages.packageCoverage.keySet().iterator(); it.hasNext();) {
            String pkg = it.next();
            Set<ProxyClassLoader> set = ProxyClassPackages.packageCoverage.get(pkg);
            if (set.contains(loader) && set.size() == 1) {
                it.remove();
            } else {
                set.remove(loader);
            }
        }
    }

    synchronized static Set<ProxyClassLoader> findCoveredPkg(String pkg) {
        return packageCoverage.get(pkg);
    }
    
}

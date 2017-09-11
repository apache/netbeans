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
package org.netbeans.modules.maven.embedder.impl;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.DefaultRepositoryCache;
import org.eclipse.aether.RepositoryCache;

/**
 *
 * @author mkleint
 */
public class NbRepositoryCache implements RepositoryCache {
    
    private static final Object LOCK = new Object();
    
    //org.eclipse.aether.internal.impl.ObjectPool instances, containing a weakhashmap with weak value references.
    //to be considered harmless..
    private static Object artifacts;
    private static Object dependencies;
    
    //as in DataPool class..
    //TODO mkleint: the constants have wrong values, the caching is not taking effect,
    //interestingly both the Dependency and Artifact instances are bigger in live IDE when correct value is used..
    
    private static final String ARTIFACT_POOL = "org.eclipse.org.eclipse.aether.DataPool$Artifact";
    private static final String DEPENDENCY_POOL = "org.eclipse.org.eclipse.aether.DataPool$Dependency";    

    private final DefaultRepositoryCache superDelegate;
    public NbRepositoryCache() {
        superDelegate = new DefaultRepositoryCache();
    }
    
    @Override
    public Object get(RepositorySystemSession session, Object key) {
        if (ARTIFACT_POOL.equals(key)) {
            synchronized (LOCK) {
                return artifacts;
            }
        }
        if (DEPENDENCY_POOL.equals(key)) {
            synchronized (LOCK) {
                return dependencies;
            }
        }
        return superDelegate.get(session, key);
    }

    @Override
    public void put(RepositorySystemSession session, Object key, Object data) {
        //we just let the pools to get overriden to new value, the worst that can happen is that
        //2 pools will coexist
        if (ARTIFACT_POOL.equals(key)) {
            synchronized (LOCK) {
                artifacts = data;
            }
            return;
        }
        if (DEPENDENCY_POOL.equals(key)) {
            synchronized (LOCK) {
                dependencies = data;
            }
            return;
        }
        superDelegate.put(session, key, data);
    }
    
}

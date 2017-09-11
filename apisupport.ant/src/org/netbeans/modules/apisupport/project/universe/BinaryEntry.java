/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.openide.modules.Dependency;

final class BinaryEntry extends AbstractBinaryEntry {
    private final File nbdestdir;
    
    public BinaryEntry(String cnb, File jar, File[] exts, @NonNull File nbdestdir, File clusterDir,
            String releaseVersion, String specVersion, String[] providedTokens,
            ManifestManager.PackageExport[] publicPackages, String[] friends,
            boolean deprecated, Set<Dependency> moduleDependencies) {
        super(cnb, jar, exts, clusterDir, releaseVersion, specVersion, providedTokens,
                publicPackages, friends, deprecated, moduleDependencies);
        this.nbdestdir = nbdestdir;
        if (nbdestdir == null)
            throw new NullPointerException("nbdestdir must not be null.");    // NOI18N
    }
    
    //private boolean recurring;
    public File getSourceLocation() {
        NbPlatform platform = NbPlatform.getPlatformByDestDir(nbdestdir, null);
            /*
            assert !recurring : jar;
            recurring = true;
            try {
             */
        return platform.getSourceLocationOfModule(getJarLocation());
            /*
            } finally {
                recurring = false;
            }
             */
    }
    
    public String toString() {
        File source = getSourceLocation();
        return "BinaryEntry[" + getJarLocation() + (source != null ? "," + source : "") + "]"; // NOI18N
    }
}

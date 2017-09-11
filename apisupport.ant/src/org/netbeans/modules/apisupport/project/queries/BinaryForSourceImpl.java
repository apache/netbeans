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
package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.BinaryForSourceQuery.Result;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class BinaryForSourceImpl implements BinaryForSourceQueryImplementation {
    
    private final NbModuleProject project;
    private final ConcurrentMap<URI, Result> cache;
    
    public BinaryForSourceImpl(@NonNull final NbModuleProject project) {
        Parameters.notNull("project", project); //NOI18N
        this.project = project;
        this.cache = new ConcurrentHashMap<URI, Result>();
    }

    @Override
    public Result findBinaryRoots(@NonNull final URL sourceRoot) {
        try {
            final URI key = sourceRoot.toURI();
            Result res = cache.get(key);
            if (res == null) {
                URL binRoot = null;
                
                //Try project sources
                final FileObject srcDir = project.getSourceDirectory();                
                if (srcDir != null && srcDir.toURI().equals(key)) {
                    binRoot = Utilities.toURI(project.getClassesDirectory()).toURL();
                }
                
                //Try project generated sources
                if (binRoot == null) {
                    final File genSrcDir = project.getGeneratedClassesDirectory();
                    if (genSrcDir != null && Utilities.toURI(genSrcDir).equals(key)) {
                        binRoot = Utilities.toURI(project.getClassesDirectory()).toURL();
                    }
                }
                
                //Try unit tests
                if (binRoot == null) {
                    for (final String testKind : project.supportedTestTypes()) {
                        final FileObject testSrcDir = project.getTestSourceDirectory(testKind);
                        if (testSrcDir != null && testSrcDir.toURI().equals(key)) {
                            binRoot = Utilities.toURI(project.getTestClassesDirectory(testKind)).toURL();
                            break;
                        }
                        final File testGenSrcDir = project.getTestGeneratedClassesDirectory(testKind);
                        if (testGenSrcDir != null && Utilities.toURI(testGenSrcDir).equals(key)) {
                            binRoot = Utilities.toURI(project.getTestClassesDirectory(testKind)).toURL();
                            break;
                        }
                    }
                }
                
                if (binRoot != null) {
                    res = new ResImpl(binRoot);
                    final Result oldRes = cache.putIfAbsent(key, res);
                    if (oldRes != null) {
                        res = oldRes;
                    }
                }
            }
            return res;
        } catch (MalformedURLException mue) {
            Exceptions.printStackTrace(mue);
        } catch (URISyntaxException use) {
            Exceptions.printStackTrace(use);
        }
        return null;
    }
    
    
    private class ResImpl implements BinaryForSourceQuery.Result {
        
        private final URL binDir;
        
        private ResImpl(
                @NonNull final URL binDir) {
            this.binDir = binDir;
        }

        @Override
        @NonNull
        public URL[] getRoots() {
            return new URL[] {binDir};
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            //Not needed, do not suppose the source root to be changed in nbproject
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            //Not needed, do not suppose the source root to be changed in nbproject
        }
        
    }
    
}

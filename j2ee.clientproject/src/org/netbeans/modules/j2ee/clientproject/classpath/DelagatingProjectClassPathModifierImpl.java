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

package org.netbeans.modules.j2ee.clientproject.classpath;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.clientproject.AppClientCompilationClassPathModifierImpl;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifier;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;

/**
 * In order to handle properly new COMPILE_ONLY classpath this class delegates
 * handling of some classpath types to ClassPathModifier and handling of COMPILE_ONLY
 * is delegated to AppClientCompilationClassPathModifierImpl.
 */
public class DelagatingProjectClassPathModifierImpl extends ProjectClassPathModifierImplementation {

    private final ClassPathModifier cpMod;
    private final AppClientCompilationClassPathModifierImpl compileOnlyClassPathSupport;
    
    public DelagatingProjectClassPathModifierImpl(ClassPathModifier cpMod,
            AppClientCompilationClassPathModifierImpl compileOnlyClassPathSupport) {
        this.compileOnlyClassPathSupport = compileOnlyClassPathSupport;
        this.cpMod = cpMod;
    }

    @Override
    protected SourceGroup[] getExtensibleSourceGroups() {
        return cpMod.getExtensibleSourceGroups();
    }

    @Override
    protected String[] getExtensibleClassPathTypes(SourceGroup sourceGroup) {
        List<String> res = new ArrayList<String>(Arrays.asList(cpMod.getExtensibleClassPathTypes(sourceGroup)));
        res.add(JavaClassPathConstants.COMPILE_ONLY);
        return res.toArray(new String[res.size()]);
    }

    @Override
    protected boolean addLibraries(Library[] libraries, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        if (JavaClassPathConstants.COMPILE_ONLY.equals(type)) {
            return compileOnlyClassPathSupport.addCompileLibraries(libraries);
        } else {
            return cpMod.addLibraries(libraries, sourceGroup, type);
        }
    }

    @Override
    protected boolean removeLibraries(Library[] libraries, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        if (JavaClassPathConstants.COMPILE_ONLY.equals(type)) {
            return compileOnlyClassPathSupport.removeCompileLibraries(libraries);
        } else {
            return cpMod.removeLibraries(libraries, sourceGroup, type);
        }
    }

    @Override
    protected boolean addRoots(URL[] classPathRoots, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        if (JavaClassPathConstants.COMPILE_ONLY.equals(type)) {
            return compileOnlyClassPathSupport.addCompileRoots(classPathRoots);
        } else {
            return cpMod.addRoots(classPathRoots, sourceGroup, type);
        }
    }

    @Override
    protected boolean removeRoots(URL[] classPathRoots, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        if (JavaClassPathConstants.COMPILE_ONLY.equals(type)) {
            return compileOnlyClassPathSupport.removeCompileRoots(classPathRoots);
        } else {
            return cpMod.removeRoots(classPathRoots, sourceGroup, type);
        }
    }

    @Override
    protected boolean addAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        if (JavaClassPathConstants.COMPILE_ONLY.equals(type)) {
            return compileOnlyClassPathSupport.addCompileAntArtifacts(artifacts, artifactElements);
        } else {
            return cpMod.addAntArtifacts(artifacts, artifactElements, sourceGroup, type);
        }
    }

    @Override
    protected boolean removeAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        if (JavaClassPathConstants.COMPILE_ONLY.equals(type)) {
            return compileOnlyClassPathSupport.removeCompileAntArtifacts(artifacts, artifactElements);
        } else {
            return cpMod.removeAntArtifacts(artifacts, artifactElements, sourceGroup, type);
        }
    }

    public ClassPathSupport getClassPathSupport() {
        return cpMod.getClassPathSupport();
    }

    // TODO: remove
    public ClassPathModifier getClassPathModifier() {
        return cpMod;
    }

    
}

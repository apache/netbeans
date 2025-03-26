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

package org.netbeans.modules.web.project.classpath;

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
import org.netbeans.modules.java.api.common.classpath.ClassPathModifier;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;

/**
 * In order to handle properly new COMPILE_ONLY classpath this class delegates
 * handling of some classpath types to ClassPathModifier and handling of COMPILE_ONLY
 * is delegated to WebProjectLibrariesModifierImpl.
 */
public class DelagatingProjectClassPathModifierImpl extends ProjectClassPathModifierImplementation {

    private final ClassPathModifier cpMod;
    private final WebProjectLibrariesModifierImpl compileOnlyClassPathSupport;
    
    public DelagatingProjectClassPathModifierImpl(ClassPathModifier cpMod,
            WebProjectLibrariesModifierImpl compileOnlyClassPathSupport) {
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
        return res.toArray(new String[0]);
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

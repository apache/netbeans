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
package org.netbeans.modules.java.platform.queries;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import static org.netbeans.api.java.platform.JavaPlatform.PROP_SOURCE_FOLDER;
import org.netbeans.api.java.platform.Specification;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Tomas Zezula
 */
class TestJavaPlatform extends JavaPlatform {

    private final String name;
    private final ClassPath boot;
    private volatile ClassPath sources;
    private volatile List<URL> javadoc;

    TestJavaPlatform(final String name, final ClassPath boot) {
        this.name = name;
        this.boot = boot;
        this.sources = ClassPath.EMPTY;
        this.javadoc = Collections.emptyList();
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.<String,String>emptyMap();
    }

    @Override
    public ClassPath getBootstrapLibraries() {
        return boot;
    }

    @Override
    public ClassPath getStandardLibraries() {
        return ClassPath.EMPTY;
    }

    @Override
    public String getVendor() {
        return "Oracle";    //NOI18N
    }

    @Override
    public Specification getSpecification() {
        return new Specification("j2se", new SpecificationVersion("1.5"));
    }

    @Override
    public Collection<FileObject> getInstallFolders() {
        return Collections.<FileObject>emptySet();
    }

    @Override
    public FileObject findTool(String toolName) {
        return null;
    }

    @Override
    public ClassPath getSourceFolders() {
        return sources;
    }

    @Override
    public List<URL> getJavadocFolders() {
        return Collections.unmodifiableList(javadoc);
    }

    void setSources(final ClassPath cp) {
        sources = cp;
        firePropertyChange(PROP_SOURCE_FOLDER, null, null);
    }

    void setJavadoc(final List<URL> javadoc) {
        this.javadoc = javadoc;
        firePropertyChange(PROP_JAVADOC_FOLDER, null, null);
    }

}

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
package org.netbeans.spi.java.platform.support;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.openide.filesystems.FileObject;

/**
 * A {@link JavaPlatform} forwarding calls to a delegate.
 * @since 1.41
 * @author Tomas Zezula
 */
public class ForwardingJavaPlatform extends JavaPlatform {

    protected final JavaPlatform delegate;

    public ForwardingJavaPlatform(@NonNull final JavaPlatform delegate) {
        this.delegate = delegate;
    }


    @Override
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    @Override
    public Map<String, String> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public ClassPath getBootstrapLibraries() {
        return delegate.getBootstrapLibraries();
    }

    @Override
    public ClassPath getStandardLibraries() {
        return delegate.getStandardLibraries();
    }

    @Override
    public String getVendor() {
        return delegate.getVendor();
    }

    @Override
    public Specification getSpecification() {
        return delegate.getSpecification();
    }

    @Override
    public Collection<FileObject> getInstallFolders() {
        return delegate.getInstallFolders();
    }

    @Override
    public FileObject findTool(String toolName) {
        return delegate.findTool(toolName);
    }

    @Override
    public ClassPath getSourceFolders() {
        return delegate.getSourceFolders();
    }

    @Override
    public List<URL> getJavadocFolders() {
        return delegate.getJavadocFolders();
    }

}

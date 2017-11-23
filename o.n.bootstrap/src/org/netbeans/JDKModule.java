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
package org.netbeans;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 *
 * @author lahvac
 */
public class JDKModule extends Module {

    private static final Logger LOG = Logger.getLogger(NetigsoModule.class.getName());

    private final Manifest manifest;
    private final String cnb;

    public JDKModule(ModuleManager mgr, Events ev, Object history, String cnb, String packages, ClassLoader loader) throws InvalidException {
        super(mgr, ev, history, loader, false, false);
        this.manifest = new Manifest();
        this.manifest.getMainAttributes().putValue("OpenIDE-Module", cnb);
        if (packages.isEmpty()) {
            this.manifest.getMainAttributes().putValue("OpenIDE-Module-Public-Packages", "-");
        } else {
            this.manifest.getMainAttributes().putValue("OpenIDE-Module-Public-Packages", Arrays.stream(packages.split(",")).map(p -> p + ".*").collect(Collectors.joining(", ")));
        }
        this.manifest.getMainAttributes().putValue("OpenIDE-Module-Specification-Version", "8");
        this.cnb = cnb;
    }

    @Override
    protected void parseManifest() throws InvalidException {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getJarFile() {
        return null;
    }

    @Override
    public List<File> getAllJars() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setReloadable(boolean r) {
        reloadable = true;
    }

    @Override
    public void reload() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void classLoaderUp(Set<Module> parents) throws IOException {
    }

    @Override
    protected void classLoaderDown() {
    }

    @Override
    public Set<Object> getProblems() {
        return Collections.emptySet();
    }
    
    @Override
    protected void cleanup() {
    }

    @Override
    protected void destroy() {
    }

    @Override
    public boolean isFixed() {
        return false;
    }

    @Override
    public boolean isJDK() {
        return true;
    }

    @Override
    public Manifest getManifest() {
        return manifest;
    }

    @Override
    public Object getLocalizedAttribute(String attr) {
        // TBD;
        return null;
    }

    @Override
    public String toString() {
        return "JDK: " + cnb;
    }

    //TODO: getAttribute

//    private final class DelegateCL extends ProxyClassLoader 
//    implements Util.ModuleProvider {
//        private final Set<String> thisModulePackages;
//        public DelegateCL() {
//            super(new ClassLoader[] {ClassLoader.getSystemClassLoader()}, false, false);
//            thisModulePackages = new HashSet<>(Arrays.asList(packages.split(",")));
//            addCoveredPackages(thisModulePackages);
//        }
//
//        @Override
//        public String toString() {
//            return "JDK module"; // NOI18N
//        }
//
//        @Override
//        public Module getModule() {
//            return JDKModule.this;
//        }
//
//        @Override
//        protected boolean shouldDelegateResource(String pkg, ClassLoader parent) {
//            System.err.println("pkg = " + pkg);
//            return thisModulePackages.contains(pkg) && super.shouldDelegateResource(pkg, parent);
//        }
//        
//    }
    
}

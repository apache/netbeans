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
import java.io.ObjectInput;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;

/** Special module for representing OSGi bundles 
 * @author Jaroslav Tulach
 */
final class NetigsoModule extends Module {
    private static final Logger LOG = Logger.getLogger(NetigsoModule.class.getName());

    private final File jar;
    private final Manifest manifest;
    private int startLevel = -1;
    private InvalidException problem;

    public NetigsoModule(Manifest mani, File jar, ModuleManager mgr, Events ev, Object history, boolean reloadable, boolean autoload, boolean eager) throws IOException {
        super(mgr, ev, history, reloadable, autoload, eager);
        this.jar = jar;
        this.manifest = mani;
    }

    @Override
    ModuleData createData(ObjectInput in, Manifest mf) throws IOException {
        if (in != null) {
            return new ModuleData(in);
        } else {
            return new ModuleData(mf, this);
        }
    }

    @Override
    boolean isNetigsoImpl() {
        return true;
    }

    @Override
    protected void parseManifest() throws InvalidException {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getJarFile() {
        return jar;
    }

    @Override
    public List<File> getAllJars() {
        return Collections.singletonList(jar);
    }

    @Override
    public void setReloadable(boolean r) {
        reloadable = true;
    }

    @Override
    public void reload() throws IOException {
        mgr.netigso().reload(this);
    }

    final void start() throws IOException {
        ProxyClassLoader pcl = (ProxyClassLoader)classloader;
        try {
            Set<String> pkgs = mgr.netigso().createLoader(this, pcl, this.jar);
            pcl.addCoveredPackages(pkgs);
        } catch (IOException ex) {
            classloader = null;
            throw ex;
        }
    }

    @Override
    protected void classLoaderUp(Set<Module> parents) throws IOException {
        NetigsoModule.LOG.log(Level.FINE, "classLoaderUp {0}", getCodeNameBase()); // NOI18N
        assert classloader == null : "already had " + classloader + " for " + this;
        classloader = new DelegateCL();
        mgr.netigsoLoaderUp(this);
    }

    @Override
    protected void classLoaderDown() {
        NetigsoModule.LOG.log(Level.FINE, "classLoaderDown {0}", getCodeNameBase()); // NOI18N
        ProxyClassLoader pcl = (ProxyClassLoader)classloader;
        classloader = null;
        ClassLoader l = pcl.firstParent();
        if (l == null) {
            mgr.netigsoLoaderDown(this);
            return;
        }
        mgr.netigso().stopLoader(this, l);
    }

    @Override
    public ClassLoader getClassLoader() throws IllegalArgumentException {
        if (classloader == null) {
            try {
                classLoaderUp(null);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (classloader == null) {
            throw new IllegalArgumentException("No classloader for " + getCodeNameBase()); // NOI18N
        }
        return classloader;
    }

    @Override
    public Set<Object> getProblems() {
        InvalidException ie = problem;
        return ie == null ? Collections.emptySet() :
            Collections.<Object>singleton(ie);
    }
    
    final void setProblem(InvalidException ie) {
        problem = ie;
    }

    @Override
    public Enumeration<URL> findResources(String resources) {
        return mgr.netigso().findResources(this, resources);
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
    public Manifest getManifest() {
        if (manifest != null) {
            return manifest;
        }
        // XXX #210310: is anyone actually getting here?
        try {
            return getManager().loadManifest(jar);
        } catch (IOException x) {
            Util.err.log(Level.WARNING, "While loading manifest for " + this, x);
            return new Manifest();
        }
    }

    @Override
    public Object getLocalizedAttribute(String attr) {
        // TBD;
        return null;
    }

    @Override
    public String toString() {
        return "Netigso: " + jar;
    }

    @Override
    final int getStartLevelImpl() {
        return startLevel;
    }

    final void setStartLevel(int startLevel) {
        this.startLevel = startLevel;
    }
    
    private final class DelegateCL extends ProxyClassLoader 
    implements Util.ModuleProvider {
        public DelegateCL() {
            super(new ClassLoader[0], false);
        }

        private ProxyClassLoader delegate() {
            ClassLoader l = firstParent();
            assert l != null;
            return (ProxyClassLoader)l;
        }

        @Override
        public URL findResource(String name) {
            try {
                return delegate().findResource(name);
            } catch (IllegalStateException ex) {
                LOG.log(Level.SEVERE, "Can't load " + name, ex);
                return null;
            }
        }

        @Override
        public Enumeration<URL> findResources(String name) throws IOException {
            try {
                return delegate().findResources(name);
            } catch (IllegalStateException ex) {
                throw new IOException("Can't load " + name, ex);
            }
        }

        @Override
        protected Class<?> doLoadClass(String pkg, String name) {
            try {
                return delegate().doLoadClass(pkg, name);
            } catch (IllegalStateException ex) {
                LOG.log(Level.INFO, "Can't load " + name + " in package " + pkg, ex);
                return null;
            }
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            try {
                return delegate().loadClass(name, resolve);
            } catch (IllegalStateException ex) {
                throw new ClassNotFoundException("Can't load " + name, ex);
            }
        }


        @Override
        public String toString() {
            ClassLoader l = firstParent();
            return l == null ? "Netigso[uninitialized]" : "Netigso[" + l.toString() + "]"; // NOI18N
        }

        @Override
        public Module getModule() {
            return NetigsoModule.this;
        }
    }
}

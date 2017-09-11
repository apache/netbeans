/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2011 Sun
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
        protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
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

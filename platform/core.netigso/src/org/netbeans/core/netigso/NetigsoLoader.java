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
package org.netbeans.core.netigso;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.ProxyClassLoader;
import org.openide.modules.ModuleInfo;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;
import org.openide.util.NbCollections;
import org.osgi.framework.Bundle;

final class NetigsoLoader extends ProxyClassLoader {
    private static final Logger LOG = Logger.getLogger(NetigsoLoader.class.getName());
    private Bundle bundle;

    NetigsoLoader(Bundle b, ModuleInfo m, File jar) {
        super(new ClassLoader[0], true);
        this.bundle = b;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public URL findResource(String name) {
        //Netigso.start();
        Bundle b = bundle;
        if (b == null) {
            LOG.log(Level.WARNING, "Trying to load resource before initialization finished {0}", name);
            return null;
        }
        return b.getResource(name);
    }

    @Override
    public Enumeration<URL> findResources(String name) {
        //Netigso.start();
        Bundle b = bundle;
        if (b == null) {
            LOG.log(Level.WARNING, "Trying to load resource before initialization finished {0}", name);
            return Enumerations.empty();
        }
        Enumeration<URL> ret = null;
        try {
            if (b.getState() != Bundle.UNINSTALLED) {
                ret = b.getResources(name);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return ret == null ? Enumerations.<URL>empty() : NbCollections.checkedEnumerationByFilter(ret, URL.class, true);
    }

    @Override
    protected Class<?> doLoadClass(String pkg, String name) {
        Bundle b = bundle;
        if (b == null) {
            LOG.log(Level.WARNING, "Trying to load class before initialization finished {0}", pkg + '.' + name);
            return null;
        }
        try {
            return b.loadClass(name);
        } catch (ClassNotFoundException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "No class found in " + this, ex);
            }
            return null;
        }
    }

    @Override
    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class c = findLoadedClass(name);
        if (c != null) {
            return c;
        }
        Bundle b = bundle;
        if (b == null) {
            LOG.log(Level.WARNING, "Trying to load class before initialization finished {0}", new Object[] { name });
            return null;
        }
        try {
            c = b.loadClass(name);
            if (resolve) {
                resolveClass(c);
            }
            return c;
        } catch (ClassNotFoundException x) {
        }
        return super.loadClass(name, resolve);
    }


    @Override
    public String toString() {
        Bundle b = bundle;
        if (b == null) {
            return "uninitialized";
        }
        return b.getLocation();
    }
}

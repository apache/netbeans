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
package org.netbeans;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class NetigsoLoader extends ClassLoader {
    private final Module mi;

    public NetigsoLoader(Module mi) {
        this.mi = mi;
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        ClassLoader del = getDelegate(10000);
        if (del == null) {
            Util.err.log(Level.WARNING, 
                "Time out waiting to enabled {0}. Cannot load {1}",
                new Object[]{mi.getCodeNameBase(), className}
            );
            throw new ClassNotFoundException(className);
        }
        return del.loadClass(className);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        ClassLoader d = getDelegate();
        if (d instanceof ProxyClassLoader) {
            return ((ProxyClassLoader)d).loadClass(name, resolve);
        } else {
            return d.loadClass(name);
        }
    }

    @Override
    public Enumeration<URL> getResources(String string) throws IOException {
        return getDelegate().getResources(string);
    }

    @Override
    public InputStream getResourceAsStream(String string) {
        return getDelegate().getResourceAsStream(string);
    }

    @Override
    public URL getResource(String string) {
        return getDelegate().getResource(string);
    }

    private ClassLoader getDelegate() {
        return getDelegate(0);
    }
    private ClassLoader getDelegate(long timeout) {
        if (!mi.isEnabled()) {
            Util.err.log(Level.INFO, 
                "OSGi is requesting adhoc start of {0}. This is inefficient. "
              + "It is suggested turn the module on by default", 
                mi.getCodeNameBase()
            );
            Mutex.Privileged p = mi.getManager().mutexPrivileged();
            if (!p.tryWriteAccess(timeout)) {
                return null;
            }
            try {
                mi.getManager().enable(mi, false);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvalidException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                p.exitWriteAccess();
            }
        }
        return mi.getClassLoader();
    }
    
} // end of DelegateLoader

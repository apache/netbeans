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

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * A stream handler factory that delegates to others in lookup.
 */
public class ProxyURLStreamHandlerFactory implements URLStreamHandlerFactory, LookupListener {

    private static final Logger LOG = Logger.getLogger(ProxyURLStreamHandlerFactory.class.getName());
    private static boolean proxyFactoryInitialized;
    private static URLStreamHandler originalJarHandler;

    public static synchronized void register() {
        LOG.log(Level.FINE, "register: {0}", proxyFactoryInitialized); // NOI18N
        LOG.log(Level.FINER, null, new Exception("Initialized by")); // NOI18N
        if (!proxyFactoryInitialized) {
            if (!ProxyURLStreamHandlerFactory.class.getClassLoader().getClass().getName().equals("com.sun.jnlp.JNLPClassLoader")) { // #196970
            try {
                List<Field> candidates = new ArrayList<Field>();
                for (Field f : URL.class.getDeclaredFields()) {
                    if (f.getType() == URLStreamHandler.class) {
                        candidates.add(f);
                    }
                }
                if (candidates.size() != 1) {
                    throw new Exception("No, or multiple, URLStreamHandler-valued fields in URL: " + candidates);
                }
                Field f = candidates.get(0);
                f.setAccessible(true);
                originalJarHandler = (URLStreamHandler) f.get(new URL("jar:file:/sample.jar!/"));
                LOG.log(Level.FINE, "found originalJarHandler: {0}", originalJarHandler);
            } catch (Throwable t) {
                if (originalJarHandler == null) {
                    LOG.log(Level.SEVERE, "No way to find original stream handler for jar protocol", t); // NOI18N
                }
            }
            }
            try {
                URL.setURLStreamHandlerFactory(new ProxyURLStreamHandlerFactory(null));
            } catch (Error e) {
                LOG.log(Level.CONFIG, "Problems registering URLStreamHandlerFactory, trying reflection", e); // NOI18N
                try {
                    URLStreamHandlerFactory prev = null;
                    for (Field f : URL.class.getDeclaredFields()) {
                        LOG.log(Level.FINEST, "Found field {0}", f);
                        if (f.getType() == URLStreamHandlerFactory.class) {
                            LOG.log(Level.FINEST, "Clearing field {0}");
                            f.setAccessible(true);
                            prev = (URLStreamHandlerFactory) f.get(null);
                            LOG.log(Level.CONFIG, "Previous value was {0}", prev);
                            f.set(null, null);
                            LOG.config("Field is supposed to be empty");
                            break;
                        }
                    }
                    if (prev != null && prev.getClass().getName().equals(ProxyURLStreamHandlerFactory.class.getName())) {
                        prev = null;
                    }
                    URL.setURLStreamHandlerFactory(new ProxyURLStreamHandlerFactory(prev));
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, "No way to register URLStreamHandlerFactory; NetBeans is unlikely to work", t); // NOI18N
                }
            }
            proxyFactoryInitialized = true;
        }
    }

    static URLStreamHandler originalJarHandler() {
        return originalJarHandler;
    }

    private final URLStreamHandlerFactory delegate;
    private Lookup.Result<URLStreamHandlerFactory> r;
    private URLStreamHandlerFactory[] handlers;

    private ProxyURLStreamHandlerFactory(URLStreamHandlerFactory delegate) {
        this.delegate = delegate;
        LOG.log(Level.FINE, "new ProxyURLStreamHandlerFactory. delegate={0} originalJarHandler={1}", new Object[]{delegate, originalJarHandler});
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals("jar")) {
            return originalJarHandler != null ? new JarClassLoader.JarURLStreamHandler(originalJarHandler) : null;
        } else if (protocol.equals("file") || protocol.equals("http") || protocol.equals("https") || protocol.equals("resource")) { // NOI18N
            // Well-known handlers in JRE. Do not try to initialize lookup, etc.
            // (delegate already ignores these, but we cannot afford to look for URLSHFs in default lookup either.)
            return null;
        } else {
            if (delegate != null) {
                URLStreamHandler h = delegate.createURLStreamHandler(protocol);
                if (h != null) {
                    return h;
                }
            }
            URLStreamHandlerFactory[] _handlers;
            synchronized (this) {
                if (handlers == null) {
                    r = Lookup.getDefault().lookupResult(URLStreamHandlerFactory.class);
                    r.addLookupListener(this);
                    resultChanged(null);
                }
                _handlers = handlers;
            }
            for (URLStreamHandlerFactory f : _handlers) {
                URLStreamHandler h = f.createURLStreamHandler(protocol);
                if (h != null) {
                    return h;
                }
            }
            return null;
        }
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends URLStreamHandlerFactory> c = r.allInstances();
        synchronized (this) {
            handlers = c.toArray(new URLStreamHandlerFactory[0]);
        }
    }
}

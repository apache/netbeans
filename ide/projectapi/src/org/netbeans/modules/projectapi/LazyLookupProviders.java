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

package org.netbeans.modules.projectapi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Factory methods for lazy {@link LookupProvider} registration.
 */
public class LazyLookupProviders {

    private LazyLookupProviders() {}

    static final Logger LOG = Logger.getLogger(LazyLookupProviders.class.getName());
    private static final Map<Lookup,ThreadLocal<Member>> INSIDE_LOAD = new WeakHashMap<Lookup,ThreadLocal<Member>>();
    private static final Collection<Member> WARNED = Collections.synchronizedSet(new HashSet<Member>());

    /**
     * @see ProjectServiceProvider
     */
    public static LookupProvider forProjectServiceProvider(final Map<String,Object> attrs) throws ClassNotFoundException {
        class Prov implements LookupProvider {
            @Override
            public Lookup createAdditionalLookup(Lookup lkp) {
                LazyLookup result = new LazyLookup(attrs, lkp);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(
                            Level.FINE,
                            "Additional lookup created: {0} service class: {1} for base lookup: {2}", //NOI18N
                            new Object[]{
                            System.identityHashCode(result),
                                attrs.get("class"),
                                System.identityHashCode(lkp)
                            });
                }
                return result;
            }
        }
        return new Prov();
    }

    static void safeToLoad(Lookup lkp) {
        ThreadLocal<Member> memberRef;
        synchronized (LazyLookupProviders.INSIDE_LOAD) {
            memberRef = LazyLookupProviders.INSIDE_LOAD.get(lkp);
        }
        if (memberRef == null) {
            return;
        }
        Member member = memberRef.get();
        if (member != null && LazyLookupProviders.WARNED.add(member)) {
            LazyLookupProviders.LOG.log(Level.WARNING, null, new IllegalStateException("may not call Project.getLookup().lookup(...) inside " + member.getName() + " registered under @ProjectServiceProvider"));
        }
    }
    
    static Object loadPSPInstance(String implName, String methodName, Lookup lkp) throws Exception {
        ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
        }
        Class<?> clazz = loader.loadClass(implName);
        ThreadLocal<Member> member;
        synchronized (INSIDE_LOAD) {
            member = INSIDE_LOAD.get(lkp);
            if (member == null) {
                INSIDE_LOAD.put(lkp, member = new ThreadLocal<Member>());
            }
        }
        if (methodName == null) {
            for (Constructor c : clazz.getConstructors()) {
                Object[] vals = valuesFor(c.getParameterTypes(), lkp);
                if (vals != null) {
                    member.set(c);
                    try {
                        return c.newInstance(vals);
                    } finally {
                        member.remove();
                    }
                }
            }
        } else {
            for (Method m : clazz.getMethods()) {
                if (!m.getName().equals(methodName)) {
                    continue;
                }
                Object[] vals = valuesFor(m.getParameterTypes(), lkp);
                if (vals != null) {
                    member.set(m);
                    try {
                        return m.invoke(null, vals);
                    } finally {
                        member.remove();
                    }
                }
            }
        }
        throw new RuntimeException(implName + "." + methodName); // NOI18N
    }
    private static Object[] valuesFor(Class[] params, Lookup lkp) {
        if (params.length > 2) {
            return null;
        }
        List<Object> values = new ArrayList<Object>();
        for (Class param : params) {
            if (param == Lookup.class) {
                values.add(lkp);
            } else if (param == Project.class) {
                Project project = lkp.lookup(Project.class);
                if (project == null) {
                    throw new IllegalArgumentException("Lookup " + lkp + " did not contain any Project instance");
                }
                values.add(project);
            } else {
                return null;
            }
        }
        return values.toArray();
    }

    /**
     * @see org.netbeans.spi.project.LookupMerger.Registration
     */
    public static MetaLookupMerger forLookupMerger(final Map<String,Object> attrs) throws ClassNotFoundException {
        return new MetaLookupMerger() {
            private final String serviceName = (String) attrs.get("service"); // NOI18N
            private LookupMerger<?> delegate;
            private final ChangeSupport cs = new ChangeSupport(this);
            @Override
            public void probing(Class<?> service) {
                if (delegate == null && service.getName().equals(serviceName)) {
                    try {
                        LookupMerger<?> m = (LookupMerger<?>) attrs.get("lookupMergerInstance"); // NOI18N
                        if (service != m.getMergeableClass()) {
                            throw new ClassCastException(service + " vs. " + m.getMergeableClass()); // NOI18N
                        }
                        delegate = m;
                        cs.fireChange();
                    } catch (Exception x) {
                        Exceptions.printStackTrace(x);
                    }
                }
            }
            @Override
            public LookupMerger merger() {
                return delegate;
            }
            @Override
            public void addChangeListener(ChangeListener listener) {
                cs.addChangeListener(listener);
                assert cs.hasListeners();
            }
            @Override
            public void removeChangeListener(ChangeListener listener) {
                cs.removeChangeListener(listener);
            }
            public @Override String toString() {
                return "MetaLookupMerger[" + serviceName + "]";
            }
        };
    }

}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.projectapi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Template;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Factory methods for lazy {@link LookupProvider} registration.
 */
public class LazyLookupProviders {

    private LazyLookupProviders() {}

    private static final Logger LOG = Logger.getLogger(LazyLookupProviders.class.getName());
    private static final Map<Lookup,ThreadLocal<Member>> INSIDE_LOAD = new WeakHashMap<Lookup,ThreadLocal<Member>>();
    private static final Collection<Member> WARNED = Collections.synchronizedSet(new HashSet<Member>());

    /**
     * @see ProjectServiceProvider
     */
    public static LookupProvider forProjectServiceProvider(final Map<String,Object> attrs) throws ClassNotFoundException {
        class Prov implements LookupProvider {
            @Override
            public Lookup createAdditionalLookup(final Lookup lkp) {
                final Lookup result =  new ProxyLookup() {
                    Collection<String> serviceNames = Arrays.asList(((String) attrs.get("service")).split(",")); // NOI18N
                    final Thread[] LOCK = { null };
                    @Override protected void beforeLookup(Template<?> template) {
                        safeToLoad();
                        Class<?> service = template.getType();
                        synchronized (LOCK) {
                            for (;;) {
                                if (serviceNames == null || !serviceNames.contains(service.getName())) {
                                    return;
                                }
                                if (LOCK[0] == null) {
                                    break;
                                }
                                if (LOCK[0] == Thread.currentThread()) {
                                    return;
                                }
                                try {
                                    LOCK.wait();
                                } catch (InterruptedException ex) {
                                    LOG.log(Level.INFO, null, ex);
                                }
                            }
                            LOCK[0] = Thread.currentThread();
                        }
                        try {
                            Object instance = loadPSPInstance((String) attrs.get("class"), (String) attrs.get("method"), lkp); // NOI18N
                            if (!service.isInstance(instance)) {
                                // JRE #6456938: Class.cast currently throws an exception without details.
                                throw new ClassCastException("Instance of " + instance.getClass() + " unassignable to " + service);
                            }
                            setLookups(Lookups.singleton(instance));
                            synchronized (LOCK) {
                                serviceNames = null;
                            }
                        } catch (Exception x) {
                            Exceptions.attachMessage(x, "while loading from " + attrs);
                            Exceptions.printStackTrace(x);
                        } finally {
                            synchronized (LOCK) {
                                LOCK[0] = null;
                                LOCK.notifyAll();
                            }
                        }
                    }
                    private void safeToLoad() {
                        ThreadLocal<Member> memberRef;
                        synchronized (INSIDE_LOAD) {
                            memberRef = INSIDE_LOAD.get(lkp);
                        }
                        if (memberRef == null) {
                            return;
                        }
                        Member member = memberRef.get();
                        if (member != null && WARNED.add(member)) {
                            LOG.log(Level.WARNING, null, new IllegalStateException("may not call Project.getLookup().lookup(...) inside " + member.getName() + " registered under @ProjectServiceProvider"));
                        }
                    }

                    @Override
                    public String toString() {
                        return Prov.this.toString();
                    }
                };
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(
                        Level.FINE,
                        "Additional lookup created: {0} service class: {1} for base lookup: {2}",   //NOI18N
                        new Object[]{
                            System.identityHashCode(result),
                            attrs.get("class"),
                            System.identityHashCode(lkp)
                        });
                }
                return result;
            }
            
            @Override
            @SuppressWarnings("element-type-mismatch")
            public String toString() {
                return "LazyLookupProviders.LookupProvider[service=" + 
                    attrs.get("service") + 
                    ", class=" + attrs.get("class") + 
                    ", orig=" + attrs.get(FileObject.class) + 
                    "]";
            }
        };
        return new Prov();
    }
    private static Object loadPSPInstance(String implName, String methodName, Lookup lkp) throws Exception {
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

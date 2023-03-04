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
package org.openide.loaders;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.netbeans.modules.openide.loaders.SimpleES;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node.Cookie;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.CloneableEditorSupport.Pane;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class MultiDOEditor implements Callable<Pane>, CookieSet.Factory {
    private CloneableEditorSupport support;
    private static final Method factory;
    static {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        if (l == null) {
            l = Thread.currentThread().getContextClassLoader();
        }
        if (l == null) {
            l = MultiDOEditor.class.getClassLoader();
        }
        Method m = null;
        try {
            Class<?> multiviews = Class.forName("org.netbeans.core.api.multiview.MultiViews", true, l); // NOI18N
            m = multiviews.getMethod("createCloneableMultiView", String.class, Serializable.class); // NOI18N
        } catch (NoSuchMethodException ex) {
            MultiDataObject.LOG.log(Level.WARNING, "Cannot find a method", ex); // NOI18N
        } catch (ClassNotFoundException ex) {
            MultiDataObject.LOG.info("Not using multiviews for MultiDataObject.registerEditor()"); // NOI18N
            MultiDataObject.LOG.log(Level.FINE, "Cannot find a class", ex); // NOI18N
        }
        factory = m;
    }
    private final MultiDataObject outer;
    private final String mimeType;
    private final boolean useMultiview;

    MultiDOEditor(MultiDataObject outer, String mimeType, boolean useMultiview) {
        this.outer = outer;
        this.mimeType = mimeType;
        this.useMultiview = useMultiview;
    }
    
    static boolean isMultiViewAvailable() {
        return factory != null;
    }
    
    static Pane createMultiViewPane(String mimeType, MultiDataObject outer) {
        try {
            return (Pane) factory.invoke(null, mimeType, outer);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Pane call() throws Exception {
        if (factory != null) {
            return createMultiViewPane(mimeType, outer);
        }
        return null;
    }

    @Override
    public <T extends Cookie> T createCookie(Class<T> klass) {
        if (klass.isAssignableFrom(SimpleES.class)) {
            synchronized (this) {
                if (support == null) {
                    support = DataEditorSupport.create(
                        outer, outer.getPrimaryEntry(),
                        outer.getCookieSet(), useMultiview ? this : null
                    );
                }
            }
            return klass.cast(support);
        }
        return null;
    }

    public static void registerEditor(MultiDataObject multi, String mime, boolean useMultiview) {
        MultiDOEditor ed = new MultiDOEditor(multi, mime, useMultiview);
        multi.getCookieSet().add(SimpleES.class, ed);
    }
}

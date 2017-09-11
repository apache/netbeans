/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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

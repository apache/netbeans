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
package org.netbeans.modules.htmlui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.netbeans.spi.htmlui.HTMLViewerSpi;

/** API for controlling HTML like UI from Java language.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Pages {
    private Pages() {
    }

    public static Action openAction(final Map<?,?> map) {
        OpenHtmlAction r = new OpenHtmlAction(map);
        return Actions.alwaysEnabled(
                r,
                (String) map.get("displayName"), // NOI18N
                (String) map.get("iconBase"), // NOI18N
                Boolean.TRUE.equals(map.get("noIconInMenu")) // NOI18N
        );
    }

    static class OpenHtmlAction implements ActionListener {
        private final Map<?,?> map;
        private String methodName;
        private Class<?> clazz;
        private HtmlPair<?, ?> tc;
        private URL pageUrl;
        private List<String> techIds;

        OpenHtmlAction(Map<?, ?> map) {
            this.map = map;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String u = (String) map.get("url");
                String c = (String) map.get("class");
                methodName = (String) map.get("method");

                clazz = HtmlPair.loadClass(c);
                pageUrl = new URL("nbresloc:/" + u);

                ClassLoader loader = findClassLoader();
                HTMLViewerSpi.Context ctx = ContextAccessor.getDefault().newContext(loader, pageUrl, new String[0], getTechIds(), null, null, () -> {
                    Method method = clazz.getMethod(methodName);
                    Object value = method.invoke(null);
                    return new PagesLookup(loader, value);
                }, null);

                tc = HtmlPair.newView(ctx);
                tc.component(Void.class);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

        private ClassLoader findClassLoader() {
            ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
            if (loader == null) {
                loader = clazz.getClassLoader();
            }
            return loader;
        }

        final String[] getTechIds() {
            if (techIds == null) {
                techIds = new ArrayList<>();
                for (int i = 0;; i++) {
                    Object val = map.get("techId." + i);
                    if (val instanceof String) {
                        techIds.add((String) val);
                    } else {
                        break;
                    }
                }
            }
            return techIds.toArray(new String[0]);
        }
    }
}

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
package org.netbeans.modules.htmlui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import org.openide.awt.Actions;

/** API for controlling HTML like UI from Java language.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Pages {
    private Pages() {
    }
    
    public static Action openAction(final Map<?,?> map) {
        R r = new R(map);
        return Actions.alwaysEnabled(
                r,
                (String) map.get("displayName"), // NOI18N
                (String) map.get("iconBase"), // NOI18N
                Boolean.TRUE.equals(map.get("noIconInMenu")) // NOI18N
        );
    }
    static class R implements ActionListener, Runnable {
        private final Map<?,?> map;
        private String m;
        private Class<?> clazz;
        private HtmlComponent tc;
        private URL pageUrl;
        private List<String> techIds;

        public R(Map<?, ?> map) {
            this.map = map;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String u = (String) map.get("url");
                String c = (String) map.get("class");
                m = (String) map.get("method");

                clazz = HtmlComponent.loadClass(c);
                pageUrl = new URL("nbresloc:/" + u);

                tc = new HtmlComponent();
                tc.open();
                tc.requestActive();

                HtmlToolkit.getDefault().execute(this);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public void run() {
            tc.loadFX(pageUrl, clazz, m, getTechIds());
        }

        final Object[] getTechIds() {
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
            return techIds.toArray();
        }
    }
}

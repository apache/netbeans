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

package org.netbeans.modules.debugger.jpda.expr.formatters;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.modules.debugger.jpda.models.JPDAClassTypeImpl;
import org.netbeans.modules.debugger.jpda.util.WeakHashMapActive;
import org.openide.util.WeakListeners;

/**
 *
 * @author Martin Entlicher
 */
public final class Formatters {
    
    public static final String PROP_FORMATTERS = "formatters";  // NOI18N
    
    private static final Formatters INSTANCE = new Formatters();
    
    private VariablesFormatter[] formatters;
    private final Object formattersLock = new Object();
    private Properties jpdaProperties;
    private PropertyChangeListener formattersChangeListener;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private Formatters() {}
    
    public static final Formatters getDefault() {
        return INSTANCE;
    }
    
    public VariablesFormatter[] getFormatters() {
        synchronized (formattersLock) {
            if (formatters == null) {
                if (formattersChangeListener == null) {
                    formattersChangeListener = new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            if ("VariableFormatters".equals(evt.getPropertyName())) {   // NOI18N
                                synchronized (formattersLock) {
                                    formatters = null;
                                }
                                pcs.firePropertyChange(PROP_FORMATTERS, null, null);
                            }
                        }
                    };
                    jpdaProperties = Properties.getDefault().getProperties("debugger.options.JPDA");    // NOI18N
                    jpdaProperties.addPropertyChangeListener(WeakListeners.propertyChange(formattersChangeListener, jpdaProperties));
                }
                formatters = VariablesFormatter.loadFormatters();
            }
            return formatters;
        }
    }
    
    public static VariablesFormatter getFormatterForType(JPDAClassType ct, VariablesFormatter[] formatters) {
        FormatterCache cache = FormatterCache.get(formatters);
        VariablesFormatter typeFormatter = cache.getFormatter(ct);
        if (typeFormatter == FormatterCache.NO_FORMATTER) {
            return null;
        }
        if (typeFormatter != null) {
            return typeFormatter;
        }
        String cname = ct.getName();
        for (VariablesFormatter f: formatters) {
            if (!f.isEnabled()) {
                continue;
            }
            String[] types = f.getClassTypes();
            boolean applies = false;
            for (String type : types) {
                if (type.equals(cname) || (f.isIncludeSubTypes() && isInstanceOf(ct, type))) {
                    applies = true;
                    break;
                }
            }
            if (applies) {
                typeFormatter = f;
                break;
            }
        }
        cache.setFormatter(ct, typeFormatter);
        return typeFormatter;
    }

    private static boolean isInstanceOf(JPDAClassType ct, String className) {
        return ((JPDAClassTypeImpl) ct).isInstanceOf(className);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    private static class FormatterCache {
        
        private static final VariablesFormatter NO_FORMATTER = new VariablesFormatter("");
        
        private static final Map<VariablesFormatter[], FormatterCache> cache = new WeakHashMapActive<>();
        
        private final Map<JPDAClassType, VariablesFormatter> formatters = new WeakHashMapActive<>();
        
        static FormatterCache get(VariablesFormatter[] formatters) {
            FormatterCache fc;
            synchronized (cache) {
                fc = cache.get(formatters);
                if (fc == null) {
                    fc = new FormatterCache();
                    cache.put(formatters, fc);
                }
            }
            return fc;
        }
        
        VariablesFormatter getFormatter(JPDAClassType ct) {
            synchronized (formatters) {
                return formatters.get(ct);
            }
        }
        
        void setFormatter(JPDAClassType ct, VariablesFormatter vf) {
            if (vf == null) {
                vf = NO_FORMATTER;
            }
            synchronized (formatters) {
                formatters.put(ct, vf);
            }
        }
        
    }
}

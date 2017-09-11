/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.uihandler;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import org.netbeans.lib.uihandler.Decorable;
import org.netbeans.lib.uihandler.LogRecords;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach
 */
final class UINode extends AbstractNode implements VisualData, Decorable {
    private static final SimpleFormatter FORMATTER = new SimpleFormatter();
    private LogRecord log;
    private String htmlKey;

    @SuppressWarnings("LeakingThisInConstructor")
    private UINode(LogRecord r, Children ch) {
        super(ch, Lookups.fixed(r));
        log = r;

        LogRecords.decorate(r, this);
        
        /* Not necessary after the UI has changed by http://hg.netbeans.org/core-main/rev/933355e50026
        Sheet.Set s = Sheet.createPropertiesSet();
        s.put(createPropertyDate(this));
        s.put(createPropertyLogger(this));
        s.put(createPropertyMessage(this));
        getSheet().put(s);

        if (r.getParameters() != null && r.getParameters().length > 0) {
            Sheet.Set paramSheet = new Sheet.Set();
            paramSheet.setName("parameters"); // NOI18N
            paramSheet.setDisplayName(NbBundle.getMessage(UINode.class, "MSG_DisplayNameParameters"));
            for (int i = 0; i < r.getParameters().length; i++) {
                paramSheet.put(createProperty(i, getParam(r, i, Object.class)));
            }
            getSheet().put(paramSheet);
        }
        */
    }

    @Override
    public long getMillis() {
        return log.getMillis();
    }
    
    @Override
    public String getLoggerName() {
        return log.getLoggerName();
    }
    
    @Override
    public String getMessage() {
        return FORMATTER.format(log);
    }
    
    @Override
    public String getHtmlDisplayName() {
        if (htmlKey == null) {
            return null;
        } else {
            return NbBundle.getMessage(UINode.class, htmlKey, getDisplayName());
        }
    }
    
    static String getParam(LogRecord r, int index) {
        Object[] arr = r.getParameters();
        if (arr == null || arr.length <= index || !(arr[index] instanceof String)) {
            return "";
        }
        return (String)arr[index];
    }
    
    static Node create(LogRecord r) {
        Children ch;
        if (r.getThrown() != null) {
            ch = new StackTraceChildren(r.getThrown());
        } else if ("UI_ENABLED_MODULES".equals(r.getMessage()) || 
            "UI_DISABLED_MODULES".equals(r.getMessage())) {
            ch = new ModulesChildren(r.getParameters());
        } else {
            ch = Children.LEAF;
        }
        
        
        return new UINode(r, ch);
    }
    
    /* Not necessary after the UI has changed by http://hg.netbeans.org/core-main/rev/933355e50026
     * Leaving it commented out if the properties are going to be displayed somewhere again
    static Node.Property createPropertyDate(final VisualData source) {
        class NP extends PropertySupport.ReadOnly<Date> {
            public NP() {
                super(
                    "date", Date.class, 
                    NbBundle.getMessage(UINode.class, "MSG_DateDisplayName"),
                    NbBundle.getMessage(UINode.class, "MSG_DateShortDescription")
                );
            }

            @Override
            public Date getValue() throws IllegalAccessException, InvocationTargetException {
                return source == null ? null : new Date(source.getMillis());
            }
            
            @Override
            public int hashCode() {
                return getClass().hashCode();
            }
            @Override
            public boolean equals(Object o) {
                return o != null && o.getClass().equals(getClass());
            }
        }
        return new NP();
    }

    static Node.Property createPropertyLogger(final VisualData source) {
        class NP extends PropertySupport.ReadOnly<String> {
            public NP() {
                super(
                    "logger", String.class, 
                    NbBundle.getMessage(UINode.class, "MSG_LoggerDisplayName"),
                    NbBundle.getMessage(UINode.class, "MSG_LoggerShortDescription")
                );
            }

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                if (source == null) {
                    return null;
                }
                String full = source.getLoggerName();
                if (full == null) {
                    return null;
                }
                if (full.startsWith(Installer.UI_LOGGER_NAME)) {
                    if (full.equals(Installer.UI_LOGGER_NAME)) {
                        return "UI General";
                    }
                    
                    return full.substring(Installer.UI_LOGGER_NAME.length());
                }
                return full;
            }
            
            @Override
            public int hashCode() {
                return getClass().hashCode();
            }
            @Override
            public boolean equals(Object o) {
                return o != null && o.getClass().equals(getClass());
            }
        }
        return new NP();
    }
    static Node.Property createPropertyMessage(final VisualData source) {
        class NP extends PropertySupport.ReadOnly<String> {
            public NP() {
                super(
                    "message", String.class, 
                    NbBundle.getMessage(UINode.class, "MSG_MessageDisplayName"),
                    NbBundle.getMessage(UINode.class, "MSG_MessageShortDescription")
                );
            }

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return source == null ? null : source.getMessage();
            }
            
            @Override
            public int hashCode() {
                return getClass().hashCode();
            }
            @Override
            public boolean equals(Object o) {
                return o != null && o.getClass().equals(getClass());
            }
        }
        return new NP();
    }
    private Node.Property<?> createProperty(final int index, final Object object) {
        class NP extends PropertySupport.ReadOnly<String> {
            public NP() {
                super(
                    "param #" + index, String.class, 
                    NbBundle.getMessage(UINode.class, "MSG_ParameterDisplayName", index, object),
                    NbBundle.getMessage(UINode.class, "MSG_ParameterShortDescription", index, object)
                );
            }

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return object == null ? null : object.toString();
            }
            
            private int getIndex() {
                return index;
            }
            
            @Override
            public int hashCode() {
                return getClass().hashCode();
            }
            @Override
            public boolean equals(Object o) {
                if (o == null || !o.getClass().equals(getClass())) {
                    return false;
                }
                NP np = (NP)o;
                return getIndex() == np.getIndex();
            }
        }
        return new NP();
    }
    */

    
    private static final class StackTraceChildren extends Children.Keys<StackTraceElement> {
        private Throwable throwable;
        public StackTraceChildren(Throwable t) {
            throwable = t;
        }
        
        @Override
        protected void addNotify() {
            setKeys(throwable.getStackTrace());
        }
        
        @Override
        protected Node[] createNodes(StackTraceElement key) {
            AbstractNode an = new AbstractNode(Children.LEAF);
            an.setName(key.getClassName() + "." + key.getMethodName());
            an.setDisplayName(NbBundle.getMessage(UINode.class, "MSG_StackTraceElement", 
                new Object[] { 
                    key.getFileName(),
                    key.getClassName(),
                    key.getMethodName(),
                    key.getLineNumber(),
                    afterLastDot(key.getClassName()),
                }
            ));
            an.setIconBaseWithExtension("org/netbeans/modules/uihandler/stackframe.gif"); // NOI18N
            return new Node[] { an };
        }
        
    } // end of StackTraceElement

    
    private static final class ModulesChildren extends Children.Keys<Object> {
        private Object[] modules;
        public ModulesChildren(Object[] m) {
            modules = m;
        }
        
        @Override
        protected void addNotify() {
            setKeys(modules);
        }
        
        @Override
        protected Node[] createNodes(Object key) {
            AbstractNode an = new AbstractNode(Children.LEAF);
            an.setName((String)key);
            an.setIconBaseWithExtension("org/netbeans/lib/uihandler/module.gif"); // NOI18N
            return new Node[] { an };
        }
        
    } // end of StackTraceElement
    
    private static String afterLastDot(String s) {
        int index = s.lastIndexOf('.');
        if (index == -1) {
            return s;
        }
        return s.substring(index + 1);
    }

    private static <T> T getParam(LogRecord r, int index, Class<T> type) {
        if (r == null || r.getParameters() == null || r.getParameters().length <= index) {
            return null;
        }
        Object o = r.getParameters()[index];
        return type.isInstance(o) ? type.cast(o) : null;
    }

}

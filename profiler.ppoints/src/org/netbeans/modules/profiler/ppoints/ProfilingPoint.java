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

package org.netbeans.modules.profiler.ppoints;

import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.TableCellRenderer;
import org.netbeans.lib.profiler.ui.components.HTMLLabel;
import org.openide.util.Lookup;


/**
 * Abstract superclass for all Profiling Points
 *
 * @author Jiri Sedlacek
 */
public abstract class ProfilingPoint {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------
    
    public static class ResultsRenderer extends HTMLLabel implements TableCellRenderer {
        
        private Reference<JComponent> lastTable;
        private Reference<ProfilingPoint> lastProfilingPoint;
        
        {
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder());
            setHorizontalAlignment(SwingConstants.RIGHT);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            ProfilingPoint ppoint = (ProfilingPoint)value;
            if (ppoint == null && table != null) ppoint = (ProfilingPoint)table.getValueAt(row, column);
            if (ppoint == null) {
                setText(""); // NOI18N
                setEnabled(true);
            } else {
                setText("<table cellspacing='0' cellpadding='0'><tr><td height='" + // NOI18N
                        table.getRowHeight() + "' valign='middle'><nobr>&nbsp;" + // NOI18N
                        ppoint.getResultsText() + "&nbsp;</nobr></td></tr></table>"); // NOI18N
                setEnabled(ppoint.isEnabled());
            }
            lastTable = new WeakReference(table);
            lastProfilingPoint = new WeakReference(ppoint);
            return this;
        }

        public void dispatchMouseEvent(MouseEvent e, Rectangle offset) {
            setSize(getPreferredSize());
            
            int w = offset.width - getPreferredSize().width;
            MouseEvent event = new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiers(),
                                              e.getX() - offset.x - w, e.getY() - offset.y, e.getClickCount(),
                                              e.isPopupTrigger(), e.getButton());
            processEvent(event);
        }
        
        public void setCaretPosition(int position) {}
        
        public void moveCaretPosition(int position) {}
        
        public void setCursor(Cursor cursor) {
            super.setCursor(cursor);
            
            JComponent table = lastTable != null ? lastTable.get() : null;
            if (table != null) table.setCursor(cursor);
        }

        protected void showURL(URL url) {
            ProfilingPoint ppoint = lastProfilingPoint != null ? lastProfilingPoint.get() : null;
            if (ppoint != null) ppoint.showResults(url);
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    static final String PROPERTY_NAME = "p_name"; // NOI18N
    static final String PROPERTY_ENABLED = "p_enabled"; // NOI18N
    static final String PROPERTY_PROJECT = "p_project"; // NOI18N
    static final String PROPERTY_RESULTS = "p_results"; // NOI18N
    static final String PROPERTY_CUSTOM = "p_custom"; // NOI18N

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final ProfilingPointFactory factory;
    private Lookup.Provider project; // Project for which the Profiling Point is defined
    private PropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this, true);
    private ResultsRenderer resultsRenderer;
    private String name; // Name of the Profiling Point, must be unique within a project
    private boolean enabled = true; // Defines if the Profiling Point is currently enabled

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    ProfilingPoint(String name, Lookup.Provider project, ProfilingPointFactory factory) {
        this.name = name;
        this.project = project;
        this.factory = factory;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public final ProfilingPointFactory getFactory() {
        return factory;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;
        getChangeSupport().firePropertyChange(PROPERTY_ENABLED, !this.enabled, this.enabled);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setName(String name) {
        if (this.name.equals(name) || (name == null)) {
            return;
        }

        String oldName = this.name;
        this.name = name;
        getChangeSupport().firePropertyChange(PROPERTY_NAME, oldName, this.name);
    }

    public String getName() {
        return name;
    }

    public Lookup.Provider getProject() {
        return project;
    }
    
    public boolean isValid() {
        return true;
    }

    public abstract boolean hasResults();

    public abstract void hideResults();

    public abstract void showResults(URL url);

    public ResultsRenderer getResultsRenderer() {
        if (resultsRenderer == null) {
            resultsRenderer = new ResultsRenderer();
        }

        return resultsRenderer;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    // Opens customizer for the Profiling Point
    public void customize(boolean deleteOnCancel, boolean focusToEditor) {
        final ValidityAwarePanel customizer = getCustomizer();
        if (!ProfilingPointsManager.getDefault().customize(customizer,
            new Runnable() {
                public void run() {
                    setValues(customizer);
                }
            }, focusToEditor) && deleteOnCancel)
            ProfilingPointsManager.getDefault().removeProfilingPoint(this);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public boolean supportsProfilingSettings(ProfilingSettings profilingSettings) {
        final int profilingType = profilingSettings.getProfilingType();
        final ProfilingPointFactory ppFactory = getFactory();

        return 
        // CPU profiling
        (((profilingType == ProfilingSettings.PROFILE_CPU_ENTIRE)
         || (profilingType == ProfilingSettings.PROFILE_CPU_JDBC)
         || (profilingType == ProfilingSettings.PROFILE_CPU_PART)) && ppFactory.supportsCPU())
               || 
        // Memory profiling
        (((profilingType == ProfilingSettings.PROFILE_MEMORY_ALLOCATIONS)
         || (profilingType == ProfilingSettings.PROFILE_MEMORY_LIVENESS)) && ppFactory.supportsMemory())
               || 
        // Monitoring
        ((profilingType == ProfilingSettings.PROFILE_MONITOR) && ppFactory.supportsMonitor());
    }

    public String toString() {
        return getName();
    }

    protected abstract String getResultsText();

    protected abstract void updateCustomizer(ValidityAwarePanel customizer); // Updates customizer according to the values (called for each getCustomizer)

    protected PropertyChangeSupport getChangeSupport() {
        return propertyChangeSupport;
    }

    abstract void setValues(ValidityAwarePanel customizer); // Updates values according to the customizer (called for each getCustomizer)

    abstract void reset();

    ValidityAwarePanel getCustomizer() {
        ValidityAwarePanel customizer = getFactory().getCustomizer(); // Customizer or null if other customizer already showing

        if (customizer != null) {
            updateCustomizer(customizer); // Update data for this customizer
        }

        return customizer;
    }
}

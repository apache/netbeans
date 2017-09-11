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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.ComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.openide.util.WeakListeners;

/**
 * GUI tools for working with the Java platform.
 * @author Jesse Glick
 */
public class JavaPlatformComponentFactory {
    
    private JavaPlatformComponentFactory() {}
    
    public static ComboBoxModel/*<JavaPlatform>*/ javaPlatformListModel() {
        return new Model();
    }
    
    public static ListCellRenderer/*<JavaPlatform>*/ javaPlatformListCellRenderer() {
        return new Renderer();
    }
    
    private static final class Model implements ComboBoxModel/*<JavaPlatform>*/, PropertyChangeListener, Comparator<JavaPlatform> {
        
        private static final Collator COLL = Collator.getInstance();
        private static final JavaPlatformManager mgr = JavaPlatformManager.getDefault();
        private final SortedSet<JavaPlatform> platforms = new TreeSet<JavaPlatform>(this);
        private final List<ListDataListener> listeners = new ArrayList<ListDataListener>();
        private JavaPlatform selected;
        
        public Model() {
            refresh();
            mgr.addPropertyChangeListener(WeakListeners.propertyChange(this, mgr));
        }
        
        private void refresh() {
            platforms.clear();
            for (JavaPlatform plaf : mgr.getInstalledPlatforms()) {
                if (plaf.getSpecification().getName().equals("j2se")) { // NOI18N
                    platforms.add(plaf);
                }
            }
        }

        public int getSize() {
            return platforms.size();
        }

        public Object getElementAt(int index) {
            return new ArrayList<JavaPlatform>(platforms).get(index);
        }

        public void addListDataListener(ListDataListener l) {
            listeners.add(l);
        }

        public void removeListDataListener(ListDataListener l) {
            listeners.remove(l);
        }
        
        public void setSelectedItem(Object sel) {
            if (sel != selected) {
                selected = (JavaPlatform) sel;
                fireChange();
            }
        }

        public Object getSelectedItem() {
            return selected;
        }
        
        private void fireChange() {
            ListDataEvent ev = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0);
            for (ListDataListener l : new ArrayList<ListDataListener>(listeners)) {
                l.contentsChanged(ev);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            refresh();
            fireChange();
        }

        public int compare(JavaPlatform p1, JavaPlatform p2) {
            int res = COLL.compare(p1.getDisplayName(), p2.getDisplayName());
            if (res != 0) {
                return res;
            } else {
                String id1 = ModuleProperties.getPlatformID(p1);
                String id2 = ModuleProperties.getPlatformID(p2);
                if (id1 != null && id2 != null) {
                    return id1.compareTo(id2);
                } else {
                    return System.identityHashCode(p1) - System.identityHashCode(p2);
                }
            }
        }

    }
    
    private static final class Renderer extends JLabel implements ListCellRenderer, UIResource /*<JavaPlatform>*/ {
        
        public Renderer() {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            String name = value != null ? ((JavaPlatform) value).getDisplayName() : null;
            setText(name);
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }

        // #93658: GTK needs name to render cell renderer "natively"
        public @Override String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
        
    }
    
}

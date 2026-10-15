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

package org.netbeans.modules.profiler.v2.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.swing.ProfilerPopup;
import org.netbeans.lib.profiler.ui.swing.SmallButton;
import org.netbeans.lib.profiler.ui.swing.renderer.JavaNameRenderer;
import org.netbeans.lib.profiler.utils.formatting.MethodNameFormatter;
import org.netbeans.lib.profiler.utils.Wildcards;
import org.netbeans.lib.profiler.utils.formatting.DefaultMethodNameFormatter;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.LanguageIcons;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.api.java.SourceMethodInfo;
import org.netbeans.modules.profiler.v2.ProfilerSession;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ClassMethodList_addMethod=Add method",
    "ClassMethodList_removeMethods=Remove selected methods",
    "ClassMethodList_addClass=Add class",
    "ClassMethodList_removeClasses=Remove selected classes",
    "ClassMethodList_selectedMethods=Selected methods:",
    "ClassMethodList_selectedClasses=Selected classes:"
})
public final class ClassMethodList {
    
    public static void showClasses(ProfilerSession session, Set<ClientUtils.SourceCodeSelection> selection, Component invoker) {
        UI.forClasses(session, selection).show(invoker);
    }
    
    public static void showMethods(ProfilerSession session, Set<ClientUtils.SourceCodeSelection> selection, Component invoker) {
        UI.forMethods(session, selection).show(invoker);
    }
    
    
    private ClassMethodList() {}
    
    private static class UI {
        
        private JPanel panel;
        
        static UI forClasses(ProfilerSession session, Set<ClientUtils.SourceCodeSelection> selection) {
            return new UI(session, selection, false);
        }
        
        static UI forMethods(ProfilerSession session, Set<ClientUtils.SourceCodeSelection> selection) {
            return new UI(session, selection, true);
        }
        
        
        void show(Component invoker) {
            int resizeMode = ProfilerPopup.RESIZE_BOTTOM | ProfilerPopup.RESIZE_RIGHT;
            ProfilerPopup.createRelative(invoker, panel, SwingConstants.SOUTH_WEST, resizeMode).show();
        }
        
        
        private UI(final ProfilerSession session, final Set<ClientUtils.SourceCodeSelection> selection, final boolean methods) {
            
            JPanel content = new JPanel(new BorderLayout());
            
            JLabel hint = new JLabel(methods ? Bundle.ClassMethodList_selectedMethods() :
                                                  Bundle.ClassMethodList_selectedClasses(), JLabel.LEADING);
            hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
            content.add(hint, BorderLayout.NORTH);
            
            class XListModel extends AbstractListModel<ClientUtils.SourceCodeSelection> {
                public int getSize() {
                    return selection.size();
                }
                public ClientUtils.SourceCodeSelection getElementAt(int index) {
                    return (ClientUtils.SourceCodeSelection)new ArrayList(selection).get(index);
                }
                public void refresh() {
                    super.fireContentsChanged(this, 0, getSize());
                }
            }
            
            final XListModel xmodel = new XListModel();
            final JList list = new JList(xmodel) {
                public Dimension getPreferredScrollableViewportSize() {
                    Dimension dim = super.getPreferredScrollableViewportSize();
                    dim.width = 420;
                    return dim;
                }
            };
            list.setBackground(UIUtils.getProfilerResultsBackground());
            int format = methods ? DefaultMethodNameFormatter.VERBOSITY_CLASSMETHOD :
                                   DefaultMethodNameFormatter.VERBOSITY_CLASS;
            final MethodNameFormatter formatter = new DefaultMethodNameFormatter(format);
            final JavaNameRenderer renderer = new JavaNameRenderer();
            list.setCellRenderer(new ListCellRenderer() {
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    renderer.setValue(formatter.formatMethodName((ClientUtils.SourceCodeSelection)value).toFormatted(), index);
                    JComponent c = renderer.getComponent();
                    if (isSelected) {
                        c.setForeground(list.getSelectionForeground());
                        c.setBackground(list.getSelectionBackground());
                    } else {
                        c.setForeground(list.getForeground());
                        c.setBackground((index & 0x1) == 0 ? list.getBackground() :
                                         UIUtils.getDarker(list.getBackground()));
                    }
                    return c;
                }
            });
            
            String iconMask = methods ? LanguageIcons.METHOD : LanguageIcons.CLASS;
            Image baseIcon = Icons.getImage(iconMask);
            Image addBadge = Icons.getImage(GeneralIcons.BADGE_ADD);
            Image addImage = ImageUtilities.mergeImages(baseIcon, addBadge, 0, 0);
            final JButton addB = new SmallButton(ImageUtilities.image2Icon(addImage)) {
                protected void fireActionPerformed(ActionEvent e) {
                    Collection<ClientUtils.SourceCodeSelection> sel = null;
                    
                    if (methods) {
                        Collection<SourceMethodInfo> mtd = ClassMethodSelector.selectMethods(session);
                        if (!mtd.isEmpty()) {
                            sel = new HashSet<>();
                            for (SourceMethodInfo smi : mtd) sel.add(
                                    new ClientUtils.SourceCodeSelection(smi.getClassName(),
                                                                        smi.getName(), smi.getSignature()));
                        }
                    } else {
                        Collection<SourceClassInfo> cls = ClassMethodSelector.selectClasses(session);
                        if (!cls.isEmpty()) {
                            sel = new HashSet<>();
                            for (SourceClassInfo sci : cls) sel.add(new ClientUtils.SourceCodeSelection(
                                    sci.getQualifiedName(), Wildcards.ALLWILDCARD, null));
                        }
                    }
                    
                    if (sel != null) {
                        selection.addAll(sel);
                        xmodel.refresh();
                    }
                }   
            };
            addB.setToolTipText(methods ? Bundle.ClassMethodList_addMethod() :
                                          Bundle.ClassMethodList_addClass());
            
            Image removeBadge = Icons.getImage(GeneralIcons.BADGE_REMOVE);
            Image removeImage = ImageUtilities.mergeImages(baseIcon, removeBadge, 0, 0);
            final JButton removeB = new SmallButton(ImageUtilities.image2Icon(removeImage)) {
                protected void fireActionPerformed(ActionEvent e) {
                    selection.removeAll(list.getSelectedValuesList());
                    xmodel.refresh();
                    list.clearSelection();
                    setEnabled(false);
                }
            };
            removeB.setToolTipText(methods ? Bundle.ClassMethodList_removeMethods() :
                                             Bundle.ClassMethodList_removeClasses());
            removeB.setEnabled(false);
            
            
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            list.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting()) return;
                    removeB.setEnabled(list.getSelectedValue() != null);
                }
            });
            
            JScrollPane scroll = new JScrollPane(list);
            hint.setLabelFor(scroll);
            content.add(scroll, BorderLayout.CENTER);
            
            JPanel buttons = new JPanel(null);
            buttons.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
            buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
            buttons.add(addB);
            buttons.add(removeB);
            content.add(buttons, BorderLayout.EAST);
            
            panel = content;            
        }
        
    }
    
}

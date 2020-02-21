/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.debugger.common2.ui.processlist;


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessInfo;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessInfoDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public final class ProcessNode extends AbstractNode {

    private final static Image icon;
    private final ChangeListener changeListener;

    static {
        icon = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }
    private ProcessPanelCustomizer customizer = null;
    private final ProcessFilter filter;

    public ProcessNode(final ProcessInfo pinfo, final Children children,
            //final ProcessActionsSupport.Provider processActionsProvider,
            ProcessPanelCustomizer customizer, ProcessFilter filter) {
        super(children, Lookups.fixed(pinfo));
        this.customizer = customizer;
        this.filter = filter;
        changeListener = new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                fireDisplayNameChange(null, getDisplayName());
            }
        };

        filter.addChangeListener(WeakListeners.change(changeListener, filter));
    }

    @Override
    public Image getIcon(int type) {
        return icon;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getDisplayName() {
        return colorize(filter, customizer.getDisplayName(getInfo()));
    }

    @Override
    public Action[] getActions(boolean context) {
        return null;
                //getActionsProvider().getActions(getPID());
    }

    @Override
    public Action getPreferredAction() {
       // return getActionsProvider().getPreferredAction(getPID());
       return null;
    }

    @Override
    public String toString() {
        return "Node for " + getInfo().toString(); // NOI18N
    }

    @Override
    public PropertySet[] getPropertySets() {
        final ProcessInfo info = getInfo();
        final List<ProcessInfoDescriptor> descriptors = customizer.getValues(info);
        final Property[] properties = new Property<?>[descriptors.size()];

        int idx = 0;

        for (final ProcessInfoDescriptor d : descriptors) {
            properties[idx++] = new ROProperty(info, d, filter);
        }

        return new PropertySet[]{
                    new PropertySet() {

                        @Override
                        public Property<?>[] getProperties() {
                            return properties;
                        }
                    }};
    }

    public ProcessInfo getInfo() {
        return getLookup().lookup(ProcessInfo.class);
    }
//
//    private ProcessActionsSupport.Provider getActionsProvider() {
//        return getLookup().lookup(ProcessActionsSupport.Provider.class);
//    }

    private int getPID() {
        return getInfo().getPID();
    }
    
    private static String toHtml(String plain) {
        plain = plain.replace("&", "&amp;"); // NOI18N
        plain = plain.replace("<", "&lt;"); // NOI18N
        plain = plain.replace(">", "&gt;"); // NOI18N
        plain = plain.replace(" ", "&nbsp;"); // NOI18N
        return plain;
    }

    private static String colorize(ProcessFilter filter, String orig) {
        String filt = filter.get();
        String result;

        int matchPos = orig.indexOf(filt);

        if (matchPos < 0) {
            result = "<html><p>" + toHtml(orig); // NOI18N
        } else {
            result = "<html><p>" + toHtml(orig.substring(0, matchPos)) + "<b>" + toHtml(filt) + "</b>" + toHtml(orig.substring(matchPos + filt.length())); // NOI18N
        }
        return result;
    }

    private final static class ROProperty extends PropertySupport.ReadOnly {

        private final ProcessInfo info;
        private final ProcessInfoDescriptor d;
        private final ProcessFilter filter;

        @SuppressWarnings("unchecked")
        private ROProperty(ProcessInfo info, ProcessInfoDescriptor d, ProcessFilter filter) {
            super(d.id, d.type, d.header, d.shortDescription);
            this.info = info;
            this.d = d;
            this.filter = filter;

            setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (d.id.equals("pid")) { // NOI18N
                return info.getPID();
            }
            return info.get(d.id, d.type);
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new PropertyEditorSupport() {

                @Override
                public boolean isPaintable() {
                    return true;
                }

                @Override
                public void paintValue(Graphics gfx, Rectangle box) {
                    JLabel l = new JLabel(colorize(filter, getAsText()));
                    final Dimension preferredSize = l.getUI().getPreferredSize(l);
                    int w = preferredSize.width;
                    int h = preferredSize.height;

                    l.setSize(w, h);
                    l.addNotify();
                    l.validate();

                    Graphics2D g2d = (Graphics2D) gfx;
                    g2d.clip(box);
                    g2d.translate(4, 0); // ???

                    l.getUI().paint(g2d, l);
                }
            };
        }
    }
}

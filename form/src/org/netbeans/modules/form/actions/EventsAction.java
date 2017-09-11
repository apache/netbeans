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

package org.netbeans.modules.form.actions;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.awt.JMenuPlus;
import org.openide.util.NbBundle;

import org.netbeans.modules.form.*;

/**
 * Action class providing popup menu presenter for events of one component.
 *
 * @author Tomas Pavek
 */

public class EventsAction extends CookieAction {

    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE; // can be invoked on just one node
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] { RADComponentCookie.class };
    }

    @Override
    public String getName() {
        return NbBundle.getBundle(EventsAction.class).getString("ACT_Events"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    /**
     * Returns a JMenuItem that presents this action in a Popup Menu.
     * @return the JMenuItem representation for the action
     */
    @Override
    public JMenuItem getPopupPresenter() {
        JMenu popupMenu = new JMenuPlus(
            NbBundle.getBundle(EventsAction.class).getString("ACT_Events")); // NOI18N
        
        popupMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(popupMenu, EventsAction.class.getName());
        
        popupMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                JMenu menu = (JMenu) e.getSource();
                createEventSubmenu(menu);
            }
            
            @Override
            public void menuDeselected(MenuEvent e) {}
            
            @Override
            public void menuCanceled(MenuEvent e) {}
        });
        return popupMenu;
    }

    private void createEventSubmenu(JMenu menu) {
        if (menu.getMenuComponentCount() > 0)
            menu.removeAll();

        Node[] nodes = getActivatedNodes();
        if (nodes.length == 0)
            return;

        RADComponentCookie radCookie = nodes[0].getCookie(RADComponentCookie.class);
        if (radCookie == null)
            return;

        RADComponent metacomp = radCookie.getRADComponent();
        if (metacomp == null)
            return;

        ResourceBundle bundle = NbBundle.getBundle(EventsAction.class);

        boolean readOnly = metacomp.isReadOnly();
        Event[] events = readOnly ?
                         metacomp.getKnownEvents() : metacomp.getAllEvents();

        java.beans.EventSetDescriptor lastEventSetDesc = null;
        JMenu eventSetMenu = null;
        boolean eventSetHasHandlers = false;

        for (int i=0; i < events.length; i++) {
            Event event = events[i];
            String[] handlers = event.getEventHandlers();
            JMenuItem jmi = null;

            if (handlers.length == 0) {
                if (!readOnly)
                    jmi = new EventMenuItem(
                        MessageFormat.format(
                            bundle.getString("FMT_CTL_EventNoHandlers"), // NOI18N
                            new Object[] { event.getName() }),
                        event,
                        null);
            }
            else if (handlers.length == 1) {
                jmi = new EventMenuItem(
                    MessageFormat.format(
                        bundle.getString("FMT_CTL_EventOneHandler"), // NOI18N
                        new Object[] { event.getName(), handlers[0] }),
                    event,
                    handlers[0]);
            }
            else {
                jmi = new JMenuPlus(MessageFormat.format(
                    bundle.getString("FMT_CTL_EventMultipleHandlers"), // NOI18N
                    new Object[] { event.getName() }));

                for (int j=0; j < handlers.length; j++) {
                    JMenuItem handlerItem = new EventMenuItem(
                        MessageFormat.format(
                            bundle.getString("FMT_CTL_HandlerFromMultiple"), // NOI18N
                            new Object[] { handlers[j] }),
                        event,
                        handlers[j]);

                    handlerItem.addActionListener(getMenuItemListener());

                    HelpCtx.setHelpIDString(handlerItem, EventsAction.class.getName());
                    setBoldFontForMenuText(handlerItem);

                    ((JMenu)jmi).add(handlerItem);
                }
            }

            if (jmi != null) {
                if (event.getEventSetDescriptor() != lastEventSetDesc) {
                    if (eventSetHasHandlers)
                        setBoldFontForMenuText(eventSetMenu);

                    String name = event.getEventSetDescriptor().getName();
                    eventSetMenu = new JMenuPlus(name.substring(0,1).toUpperCase()
                                                 + name.substring(1));
                    HelpCtx.setHelpIDString(eventSetMenu,
                                            EventsAction.class.getName());
                    addSortedMenuItem(menu, eventSetMenu);
                    eventSetHasHandlers = false;
                    lastEventSetDesc = event.getEventSetDescriptor();
                }

                if (!(jmi instanceof JMenu))
                    jmi.addActionListener(getMenuItemListener());

                HelpCtx.setHelpIDString(jmi, EventsAction.class.getName());

                if (handlers.length > 0 && !readOnly) {
                    eventSetHasHandlers = true;
                    setBoldFontForMenuText(jmi);
                }

                addSortedMenuItem(eventSetMenu, jmi);
            }
        }

        if (eventSetHasHandlers)
            setBoldFontForMenuText(eventSetMenu);
    }

    private static void setBoldFontForMenuText(JMenuItem mi) {
        java.awt.Font font = mi.getFont();
        mi.setFont(font.deriveFont(font.getStyle() | java.awt.Font.BOLD));
    }

    private static void addSortedMenuItem(JMenu menu, JMenuItem menuItem) {
        int n = menu.getMenuComponentCount();
        String text = menuItem.getText();
        for (int i=0; i < n; i++) {
            String tx = ((JMenuItem)menu.getMenuComponent(i)).getText();
            if (text.compareTo(tx) < 0) {
                menu.add(menuItem, i);
                return;
            }
        }
        menu.add(menuItem);
    }
	
    private ActionListener getMenuItemListener() {
        if (menuItemListener == null)
            menuItemListener = new EventMenuItemListener();
        return menuItemListener;
    }

    // --------

    private static class EventMenuItem extends JMenuItem {
        private Event event;
        private String handlerName;

        EventMenuItem(String text, Event event, String handlerName) {
            super(text);
            this.event = event;
            this.handlerName = handlerName;
        }

        Event getEvent() {
            return event;
        }

        String getHandlerName() {
            return handlerName;
        }
    }

    private static class EventMenuItemListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            Object source = evt.getSource();
            if (!(source instanceof EventMenuItem))
                    return;

            EventMenuItem mi = (EventMenuItem) source;
            Event event = ((EventMenuItem)source).getEvent();
            Node.Property prop = event.getComponent()
                                           .getPropertyByName(event.getId());
            if (prop != null) {
                String handlerName = mi.getHandlerName();
                event.getComponent().getFormModel().getFormEvents()
                    .attachEvent(event, handlerName, null);

                try { // hack to update the property sheet
                    if (handlerName == null)
                        handlerName = (String) prop.getValue();
                    prop.setValue(handlerName);
                }
                catch (Exception ex) {}
            }
        }
    }

    private ActionListener menuItemListener;
}

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

package org.netbeans.modules.javahelp;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.netbeans.api.javahelp.Help;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.Environment;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/** XML processor for help context links.
 * The associated instance makes it suitable for
 * inclusion in a menu or toolbar.
 * @author Jesse Glick
 */
public final class HelpCtxProcessor implements Environment.Provider {
    
    private static Help findHelp() {
        return Lookup.getDefault().lookup(Help.class);
    }
    
    public @Override Lookup getEnvironment(final DataObject obj) {
        try {
            Class.forName("javax.help.HelpSet");
        } catch (ClassNotFoundException ex) {
            //JavaHelp not available, ignore:
            return Lookup.EMPTY;
        }
        Installer.log.log(Level.FINE, "creating help context presenter from {0}", obj.getPrimaryFile());
        return Lookups.singleton(new InstanceCookie() {
            private Action instance = null;
            public @Override String instanceName() {
                return obj.getName();
            }
            public @Override Class<?> instanceClass() throws IOException, ClassNotFoundException {
                return Action.class;
            }
            public @Override synchronized Object instanceCreate() throws IOException, ClassNotFoundException {
                if (instance != null) {
                    return instance;
                }
                try {
                    Document doc = XMLUtil.parse(new InputSource(obj.getPrimaryFile().toURL().toString()), true, false, XMLUtil.defaultErrorHandler(), EntityCatalog.getDefault());
                    Element el = doc.getDocumentElement();
                    if (!el.getNodeName().equals("helpctx")) { // NOI18N
                        throw new IOException();
                    }
                    instance = new ShortcutAction(obj, el.getAttribute("id"), Boolean.valueOf(el.getAttribute("showmaster")));
                    if (obj.getPrimaryFile().getAttribute("iconBase") != null) { //NOI18N
                        instance.putValue("iconBase", obj.getPrimaryFile().getAttribute("iconBase")); //NOI18N
                    }
                    return instance;
                } catch (IOException x) {
                    throw x;
                } catch (Exception x) {
                    throw new IOException(x);
                }
            }
        });
    }
    
    /** The presenter to be shown in a menu, e.g.
     */
    private static final class ShortcutAction extends AbstractAction implements HelpCtx.Provider, NodeListener, ChangeListener {
        
        private static final RequestProcessor RP =
                new RequestProcessor(ShortcutAction.class);

        /** associated XML file representing it
         */
        private final DataObject obj;
        
        /** the cached help context
         */
        private String helpID;
        
        /** cached flag to show the master help set
         */
        private boolean showmaster;
        
        /** Create a new presenter.
         * @param obj XML file describing it
         */
        public ShortcutAction(DataObject obj, String helpID, boolean showmaster) {
            this.obj = obj;
            this.helpID = helpID;
            this.showmaster = showmaster;
            putValue("noIconInMenu", true); // NOI18N
            Installer.log.log(Level.FINE, "new ShortcutAction: {0} {1} showmaster={2}", new Object[] {obj, helpID, showmaster});
            updateText();
            updateIcon();
            updateEnabled();
            if (obj.isValid()) {
                Node n = obj.getNodeDelegate();
                n.addNodeListener(org.openide.nodes.NodeOp.weakNodeListener (this, n));
            }
            Help h = findHelp();
            if (h != null) {
                h.addChangeListener(WeakListeners.change(this, h));
            }
        }
        
        /** Show the help.
         * @param actionEvent ignored
         */
        public @Override void actionPerformed(ActionEvent actionEvent) {
            Help h = findHelp();
            if (h != null) {
                Installer.log.log(Level.FINE, "ShortcutAction.actionPerformed: {0} showmaster={1}", new Object[] {helpID, showmaster});
                h.showHelp(new HelpCtx(helpID), showmaster);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
        
        /**
         * Help for the shortcut itself is generic.
         * @return a neutral help context - welcome page
         */
        public @Override HelpCtx getHelpCtx() {
            // #23565:
            return new HelpCtx("ide.welcome"); // NOI18N
        }
        
        /** Help sets may have changed.
         * @param changeEvent ignore
         */
        public @Override void stateChanged(ChangeEvent e) {
            updateEnabled();
        }
        
        /** Called when the node delegate changes somehow,
         * @param ev event indicating whether the change
         * was of display name, icon, or other
         */
        public @Override void propertyChange(PropertyChangeEvent ev) {
            String prop = ev.getPropertyName();
            if (!obj.isValid()) {
                return;
            }
            if (prop == null || prop.equals(Node.PROP_NAME) || prop.equals(Node.PROP_DISPLAY_NAME)) {
                updateText();
            }
            if (prop == null || prop.equals(Node.PROP_ICON)) {
                updateIcon();
            }
        }

        /** Update the text of the button according to node's
         * display name. Handle mnemonics sanely.
         */
        private void updateText() {
            String text;
            if (obj.isValid()) {
                text = obj.getNodeDelegate().getDisplayName();
            } else {
                // #16364
                text = "dead"; // NOI18N
            }
            putValue(Action.NAME, text);
        }

        /** Update the icon of the button according to the
         * node delegate.
         */
        private void updateIcon() {
            if (obj.isValid()) {
                Image icon = obj.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
                if (icon != null) {
                    putValue(Action.SMALL_ICON, new ImageIcon(icon));
                }
            }
        }

        private void updateEnabled() {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    Help h = findHelp();
                    final Boolean valid = h == null
                            ? Boolean.FALSE : h.isValidID(helpID, false);
                    if (valid != null) {
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                setEnabled(valid.booleanValue());
                            }
                        });
                    }
                    Installer.log.log(Level.FINE,
                            "enabled: xml={0} id={1} enabled={2}", //NOI18N
                            new Object[]{obj.getPrimaryFile(), helpID, valid});
                }
            });
        }

        public @Override void nodeDestroyed(NodeEvent ev) {
            setEnabled(false);
            updateText();
        }
        
        public @Override void childrenAdded(NodeMemberEvent ev) {}
        public @Override void childrenRemoved(NodeMemberEvent ev) {}
        public @Override void childrenReordered(NodeReorderEvent ev) {}
        
    }
    
}

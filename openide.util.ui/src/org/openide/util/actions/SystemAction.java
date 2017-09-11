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

package org.openide.util.actions;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;

/**
* A base class for user-visible actions.
* <P>
* Implements the Swing {@link Action} interface to enable use
* with the Swing action model.
* <p>An action class is a <em>singleton</em>, i.e. should generally contain no instance state.
* Rather, subclassing and use of abstract protected methods should be used
* to create variants of the action.
* <p>While it is possible to subclass this class directly--for example, if your "action"
* is really a placeholder for a popup menu that shows other actions--most people will
* prefer to use one of the subclasses, which are more convenient.
*
* @author   Ian Formanek, Jaroslav Tulach
*/
public abstract class SystemAction extends SharedClassObject implements Action, HelpCtx.Provider {

    private static final Logger LOG = Logger.getLogger(SystemAction.class.getName());

    /** Name of property indicating whether or not the action is enabled. */
    public static final String PROP_ENABLED = "enabled"; // NOI18N

    /** Name of property for the action's display icon. */
    public static final String PROP_ICON = "icon"; // NOI18N

    /** Name of property for the action's display icon, if textual. */
    private static final String PROP_ICON_TEXTUAL = "iconTextual"; // NOI18N
    private static final Set<String> relativeIconResourceClasses = new HashSet<String>(200);

    // Matches NB 3.4 w/ openide-compat.jar; see #26491
    private static final long serialVersionUID = -8361232596876856810L;

    /** Obtain a singleton instance of the action with a specified class.
    * If there already is a instance then it is returned, otherwise
    * a new one is created.
    *
    * @param actionClass the class of the action to find
    * @return the singleton action instance
    * @exception ClassCastException if the class is not <code>SystemAction</code>
    * @exception IllegalArgumentException if the instance cannot be created
    */
    public static <T extends SystemAction> T get(Class<T> actionClass) {
        return findObject(actionClass, true);
    }

    /** Get a human presentable name of the action.
    * This may be
    * presented as an item in a menu.
    * <p>Using the normal menu presenters, an included ampersand
    * before a letter will be treated as the name of a mnemonic.
    * @return the name of the action
    */
    public abstract String getName();

    /** Get a help context for the action.
    * @return the help context for this action
    */
    public abstract HelpCtx getHelpCtx();

    /** Test whether the action is currently enabled.
    * @return <code>true</code> if so
    */
    public boolean isEnabled() {
        return getProperty(PROP_ENABLED).equals(Boolean.TRUE);
    }

    /** Set whether the action should be enabled.
    * @param value <code>true</code> to enable it
    */
    public void setEnabled(boolean value) {
        putProperty(PROP_ENABLED, value ? Boolean.TRUE : Boolean.FALSE, true);
    }

    /** Set a property in the singleton. This property is common for all instances
    * of the same class.
    *
    * @param name the name of the property
    * @param value the value
    */
    public final void putValue(String name, Object value) {
        putProperty(name, value, true);

        // Could handle putValue (SMALL_ICON, ImageIcon icon) but not
        // really that important.
    }

    /** Get a property in the singleton. Values are shared among all instances of the same class.
    * The special tokens {@link Action#NAME}, {@link Action#SMALL_ICON} and "iconBase" are also recognized
    * and delegated to {@link #getName}, {@link #getIcon} and {@link #iconResource} resp.
    * @param name the name of the property
    * @return the value
    */
    public final Object getValue(String name) {
        if ("iconBase".equals(name)) { // NOI18N

            return iconResource();
        }

        Object val = getProperty(name);

        if (val == null) {
            if (NAME.equals(name)) {
                val = getName();
            } else if (SMALL_ICON.equals(name)) {
                val = getIcon();
            }
        }

        return val;
    }

    /** Actually perform the action.
    * Specified in {@link java.awt.event.ActionListener#actionPerformed}.
    * <p>In some cases, the implementation may have an empty body,
    * if the presenters handle the performing of the action in a different way
    * than by calling this method.
    * <p>Since 4.11, will be performed directly in the event thread.
    * @param ev the event triggering the action
    */
    public abstract void actionPerformed(ActionEvent ev);

    /** Initialize the action.
    * The default implementation just enabled it.
    */
    @Override
    protected void initialize() {
        putProperty(PROP_ENABLED, Boolean.TRUE);

        super.initialize();
    }

    /** Indicate whether action state should be cleared after the last action of this class is deleted.
    * @return <code>false</code> in the default implementation
    */
    @Override
    protected boolean clearSharedData() {
        return false;
    }

    /** Set the action's display icon.
    * @param icon the icon
    */
    public final void setIcon(Icon icon) {
        putProperty(PROP_ICON, icon, true);
        putProperty(PROP_ICON_TEXTUAL, icon);
    }

    /** Get the action's display icon.
    * @return the icon
    * @throws IllegalStateException if an icon could not be created
    */
    public final Icon getIcon() {
        return getIcon(false);
    }

    /** Get the action's display icon, possibly creating a text label.
    * @param createLabel if <code>true</code>, create a textual icon if otherwise there
    * would be none; if <code>false</code>, create a blank icon
    * @return an icon
    * @throws IllegalStateException if an icon could not be created
    */
    public final Icon getIcon(boolean createLabel) {
        synchronized (getLock()) {
            Icon img = (Icon) getProperty(createLabel ? PROP_ICON_TEXTUAL : PROP_ICON);

            if (img == null) {
                // create the icon from the resource
                String resName = iconResource();

                if (resName != null) {
                    if (resName.indexOf('/') == -1) {
                        // Old action that used a relative path to the icon.
                        // (If it used a relative path going down a directory, tough luck.
                        // It was never documented that you could use relative paths.
                        // apisupport templates did it, but they put icons in the same dir.)
                        String clazz = getClass().getName();
                        URL u = getClass().getResource(resName);

                        if (u != null) {
                            img = new ImageIcon(u);

                            if (relativeIconResourceClasses.add(clazz)) {
                                LOG.warning("Deprecated relative path in " + clazz + ".iconResource (cf. #20072)");
                            }
                        } else {
                            LOG.warning("No such icon from " + clazz + ": " + resName);
                        }
                    } else {
                        // Hopefully an absolute path, but again (#26887) might be relative.
                        Image i = ImageUtilities.loadImage(resName, true);

                        if (i != null) {
                            // OK, the normal case.
                            img = new ImageIcon(i);
                        } else {
                            // Check for an old-style relative path.
                            URL u = getClass().getResource(resName);
                            String clazz = getClass().getName();

                            if (u != null) {
                                // OK, but warn.
                                img = new ImageIcon(u);

                                if (relativeIconResourceClasses.add(clazz)) {
                                    LOG.warning("Deprecated relative path in " + clazz + ".iconResource (cf. #26887)");
                                }
                            } else {
                                // Really can't find it.
                                LOG.warning("No such icon from " + clazz + ": " + resName);
                            }
                        }
                    }

                    putProperty(PROP_ICON, img);
                    putProperty(PROP_ICON_TEXTUAL, img);
                }
            }

            if (img == null) {
                if (createLabel) {
                    String text = getName();
                    if (text.endsWith("...")) { // NOI18N
                        text = text.substring(0, text.length() - 3);
                    }
                    text = text.trim();
                    int ampr = text.indexOf('&');
                    if (ampr != -1) {
                        text = new StringBuffer(text).deleteCharAt(ampr).toString();
                    }
                    img = new ComponentIcon(new JLabel(text));
                    putProperty(PROP_ICON_TEXTUAL, img);
                } else {
                    putProperty(PROP_ICON, null);
                }
            }

            return img;
        }
    }

    /** Specify the proper resource name for the action's icon.
    * May be overridden by subclasses; the default is to have no icon.
    * Typically this should be a 16x16 color GIF.
    * Do not use relative paths nor an initial slash.
    * If e.g. myIcon.gif is accompanied with myIcon_pressed.gif, myIcon_disabled.gif
    * and/or myIcon_rollover.gif these images are used to call methods on JButton.setPressedIcon(),
    * JButton.setDisabledIcon() and/or JButton.setRolloverIcon() with appropriate images.
    * Please check <a href="@org-openide-awt@/org/openide/awt/Actions.html#connect(AbstractButton,%20Action)">Actions.connect</a> for
    * additional info how this is achieved (using special "iconBase" key for getValue).
    * As of APIs version 3.24, this path will be used for a localized search automatically.
    * If you do not want an icon, do <em>not</em> override this to return a blank icon. Leave it null,
    * but call <code>putValue("noIconInMenu", Boolean.TRUE)</code> to make sure that no extra space is allotted for an icon in the menu item.
    * @return the resource name for the icon, e.g. <code>com/mycom/mymodule/myIcon.gif</code>; or <code>null</code> to have no icon (make a text label)
    */
    protected String iconResource() {
        return null;
    }

    /** Create the default toolbar representation of an array of actions.
    * Null items in the array will add a separator to the toolbar.
    *
    * @param actions actions to show in the generated toolbar
    * @return a toolbar instance displaying them
    */
    public static JToolBar createToolbarPresenter(SystemAction[] actions) {
        JToolBar p = new JToolBar();
        for (SystemAction action : actions) {
            if (action == null) {
                p.addSeparator();
            } else if (action instanceof Presenter.Toolbar) {
                p.add(((Presenter.Toolbar) action).getToolbarPresenter());
            }
        }
        return p;
    }

    /** Concatenate two arrays of actions.
    * @param actions1 first array of actions to link
    * @param actions2 second array of actions to link
    * @return an array of both sets of actions in the same order
    */
    public static SystemAction[] linkActions(SystemAction[] actions1, SystemAction[] actions2) {
        List<SystemAction> l = new ArrayList<SystemAction>(Arrays.asList(actions1));
        l.addAll(Arrays.asList(actions2));

        return l.toArray(actions1);
    }

    /** Create the default popup menu representation of an array of actions.
    * @param actions actions to show in the generated menu
    * @return a popup menu displaying them
    *
    * @deprecated Use {@link org.openide.util.Utilities#actionsToPopup}
    */
    @Deprecated
    public static JPopupMenu createPopupMenu(SystemAction[] actions) {
        return Utilities.actionsToPopup(actions, Utilities.actionsGlobalContext());
    }

    /** Icon based on a component (such as a text label).
    * Just draws that component as an image.
    */
    private static class ComponentIcon extends ImageIcon {
        private JComponent comp;
        private BufferedImage image;

        /** Create an icon.
        * @param comp a component, which must be unattached to a container
        *             and should not be used for other purposes
        */
        public ComponentIcon(JComponent comp) {
            if (comp.getParent() != null) {
                throw new IllegalArgumentException();
            }

            this.comp = comp;

            Dimension size = comp.getPreferredSize();

            // Careful! If you have e.g. a JLabel with empty text, width = 0 => exceptions.
            // Must make sure it is at least a reasonable size.
            comp.setSize(Math.max(size.width, 16), Math.max(size.height, 16));
        }

        @Override
        protected void loadImage(Image i) {
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            // When enabled, tracks color choices of container:
            comp.setBackground(c.getBackground());
            comp.setForeground(c.getForeground());

            Graphics clip = g.create(x, y, getIconWidth(), getIconHeight());
            comp.paint(clip);
        }

        @Override
        public int getIconWidth() {
            return comp.getWidth();
        }

        @Override
        public int getIconHeight() {
            return comp.getHeight();
        }

        // Needed because this is called directly from e.g.
        // AbstractButton.getDisabledIcon to pass to GrayFilter,
        // rather than going through the Icon interface.
        @Override
        public Image getImage() {
            if (image == null) {
                image = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);

                // [PENDING] this is obviously ugly, but how should we decide what is the
                // default fg for the Main Window toolbar area? Background is irrelevant,
                // since we use alpha channel. But have to guess at the foreground.
                comp.setForeground(Color.black);
                comp.paint(image.getGraphics());
            }

            return image;
        }
    }
}

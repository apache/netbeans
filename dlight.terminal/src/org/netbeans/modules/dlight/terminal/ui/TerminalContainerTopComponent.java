/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.terminal.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.modules.dlight.api.terminal.TerminalSupport;
import org.netbeans.modules.dlight.terminal.action.TerminalAction;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.terminal.api.ui.TerminalContainer;
import org.netbeans.modules.terminal.support.TerminalPinSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.windows.IOContainer;

/**
 * Top component which displays something.
 * @author Vladimir Voskresensky
 */
@ConvertAsProperties(dtd = "-//org.netbeans.modules.dlight.terminal.ui//TerminalContainer//EN",// NOI18N
autostore = false)
public final class TerminalContainerTopComponent extends TopComponent {

    public static final String LOCAL_TERMINAL_PREFIX = "LocalTerminal"; // NOI18N
    public static final String SILENT_MODE_COMMAND = "silent_mode"; // NOI18N
    // private vars.... 
    private static TerminalContainerTopComponent instance;
    /** path to the icon used by the component and its open action */
    private static final String ICON_PATH = "org/netbeans/modules/dlight/terminal/ui/term.png";// NOI18N
    private static final String PREFERRED_ID = "TerminalContainerTopComponent";// NOI18N
    public final static String AUTO_OPEN_LOCAL_PROPERTY = "AutoOpenLocalTerminal"; // NOI18N
    private final TerminalContainer tc;

    public TerminalContainerTopComponent() {
        initComponents();
        initToolbar();
        fillToolBar();
        final String title = NbBundle.getMessage(TerminalContainerTopComponent.class, "CTL_TerminalContainerTopComponent");// NOI18N
        setName(title);
        setToolTipText(NbBundle.getMessage(TerminalContainerTopComponent.class, "HINT_TerminalContainerTopComponent"));// NOI18N
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        // do not use PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, see #187391
//        putClientProperty(TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);
        tc = TerminalContainer.create(TerminalContainerTopComponent.this, title);
        add(tc);
    }

    public IOContainer getIOContainer() {
        return tc.ioContainer();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        actionsBar = new javax.swing.JToolBar();

        setLayout(new java.awt.BorderLayout());

        actionsBar.setFloatable(false);
        actionsBar.setOrientation(1);
        actionsBar.setRollover(true);
        actionsBar.setFocusable(false);
        add(actionsBar, java.awt.BorderLayout.LINE_START);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar actionsBar;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized TerminalContainerTopComponent getDefault() {
        if (instance == null) {
            instance = new TerminalContainerTopComponent();
        }
        return instance;
    }
    private static Action[] actions;

    private synchronized static Action[] getToolbarActions() {
        if (actions == null) {
            List<? extends Action> termActions = Utilities.actionsForPath(TerminalAction.TERMINAL_ACTIONS_PATH);// NOI18N
            actions = termActions.toArray(new Action[termActions.size()]);
        }
        return actions;
    }

    /**
     * Obtain the TerminalContainerTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized TerminalContainerTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(TerminalContainerTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");// NOI18N
            return getDefault();
        }
        if (win instanceof TerminalContainerTopComponent) {
            return (TerminalContainerTopComponent) win;
        }
        Logger.getLogger(TerminalContainerTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID // NOI18N
                + "' ID. That is a potential source of errors and unexpected behavior.");// NOI18N
        return getDefault();
    }

    @Override
    public SubComponent[] getSubComponents() {
        ArrayList<Component> terminalList = new ArrayList<Component>();
        
        terminalList.addAll(tc.getAllTabs());

        if (terminalList.size() <= 1) {
            return super.getSubComponents();
        }

        SubComponent[] subs = new SubComponent[terminalList.size()];

        for (int i = 0; i < terminalList.size(); i++) {
            final Component terminal = terminalList.get(i);
            String title = terminal.getName();

            subs[i] = new SubComponent(
                    title,
                    new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (terminal instanceof JComponent) {
                                tc.ioContainer().select((JComponent) terminal);
                                requestActive();
                            }
                        }

                    },
                    terminal == tc.ioContainer().getSelected());
        }

        return subs;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void requestActive() {
        super.requestActive();
        // redirect focus into current terminal
        tc.requestFocusInWindow();
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        tc.componentActivated();
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        tc.componentDeactivated();
    }

    @Override
    public void componentOpened() {
        TerminalPinSupport support = TerminalPinSupport.getDefault();
        List<TerminalPinSupport.TerminalDetails> readStoredDetails = support.readStoredDetails();
//        support.clear();

        for (TerminalPinSupport.TerminalDetails details : readStoredDetails) {
            TerminalPinSupport.TerminalCreationDetails creationDetails = details.getCreationDetails();
            TerminalPinSupport.TerminalPinningDetails pinningDetails = details.getPinningDetails();

            ExecutionEnvironment env = ExecutionEnvironmentFactory.fromUniqueID(creationDetails.getExecEnv());
            String cwd = pinningDetails.getCwd();
            if (cwd.isEmpty()) {
                /* Will be opened in a default location */
                cwd = null;
            }
            TerminalSupport.restoreTerminal(pinningDetails.getTitle(), env, cwd, creationDetails.isPwdFlag(), creationDetails.getId());
        }

        JComponent selectedTerminal = getIOContainer().getSelected();
        if (selectedTerminal == null && (this.getClientProperty(AUTO_OPEN_LOCAL_PROPERTY) != Boolean.FALSE)) {
            for (Action action : getToolbarActions()) {
                if (action.getValue(Action.NAME).toString().startsWith(LOCAL_TERMINAL_PREFIX)
                        && action.isEnabled()) {
                    action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, SILENT_MODE_COMMAND));
                    break;
                }
            }
        }
    }

    @Override
    public void componentClosed() {
        JComponent selected = getIOContainer().getSelected();
        while (selected != null) {
            getIOContainer().remove(selected);
            selected = getIOContainer().getSelected();
        }
    }
    
    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");// NOI18N
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");// NOI18N
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public void addPanel(JComponent panel) {
    }

    private void fillToolBar() {
        actionsBar.removeAll();

        for (Action action : getToolbarActions()) {
            if (action instanceof Presenter.Toolbar) {
                actionsBar.add(((Presenter.Toolbar) action).getToolbarPresenter());
            }
        }

        actionsBar.revalidate();
        actionsBar.repaint();
    }

    private void initToolbar() {
        Insets ins = actionsBar.getMargin();
        JButton dummy = new JButton();
        dummy.setBorderPainted(false);
        dummy.setOpaque(false);
        dummy.setText(null);
        dummy.setIcon(new Icon() {

            @Override
            public int getIconHeight() {
                return 16;
            }

            @Override
            public int getIconWidth() {
                return 16;
            }

            @SuppressWarnings(value = "empty-statement")
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                ;
            }
        });
        actionsBar.add(dummy);
        Dimension buttonPref = dummy.getPreferredSize();
        Dimension minDim = new Dimension(buttonPref.width + ins.left + ins.right, buttonPref.height + ins.top + ins.bottom);
        actionsBar.setMinimumSize(minDim);
        actionsBar.setPreferredSize(minDim);
        actionsBar.remove(dummy);
        actionsBar.setBorder(new RightBorder());
        actionsBar.setBorderPainted(true);
    }

    private static final class RightBorder implements Border {

        public RightBorder() {
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color old = g.getColor();
            g.setColor(getColor());
            g.drawLine(x + width - 1, y, x + width - 1, y + height);
            g.setColor(old);
        }

        public Color getColor() {
            if (Utilities.isMac()) {
                Color c1 = UIManager.getColor("controlShadow"); // NOI18N
                Color c2 = UIManager.getColor("control"); // NOI18N
                return new Color((c1.getRed() + c2.getRed()) / 2,
                        (c1.getGreen() + c2.getGreen()) / 2,
                        (c1.getBlue() + c2.getBlue()) / 2);
            } else {
                return UIManager.getColor("controlShadow"); // NOI18N
            }
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 2);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }
    }
}

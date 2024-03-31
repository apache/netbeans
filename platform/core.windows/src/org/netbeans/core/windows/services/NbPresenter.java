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

package org.netbeans.core.windows.services;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.FocusManager;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;
import org.netbeans.core.windows.Constants;
import org.openide.DialogDescriptor;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

// XXX Before as org.netbeans.core.NbPresenter

/** Default implementation of Dialog created from NotifyDescriptor.
 *
 * @author Ian Formanek, Jaroslav Tulach
 */
class NbPresenter extends JDialog
implements PropertyChangeListener, WindowListener, Mutex.Action<Void>, Comparator<Object> {

    protected NotifyDescriptor descriptor;

    private final JButton stdYesButton = new JButton(NbBundle.getBundle(NbPresenter.class).getString("YES_OPTION_CAPTION")); // NOI18N
    private final JButton stdNoButton = new JButton(NbBundle.getBundle(NbPresenter.class).getString("NO_OPTION_CAPTION")); // NOI18N
    private final JButton stdOKButton = new JButton(NbBundle.getBundle(NbPresenter.class).getString("OK_OPTION_CAPTION")); // NOI18N
    private final JButton stdCancelButton = new JButton(NbBundle.getBundle(NbPresenter.class).getString("CANCEL_OPTION_CAPTION")); // NOI18N
    private final JButton stdClosedButton = new JButton(NbBundle.getBundle(NbPresenter.class).getString("CLOSED_OPTION_CAPTION")); // NOI18N
    private final JButton stdHelpButton = new JButton();
    private final JButton stdDetailButton = new JButton(NbBundle.getBundle(NbPresenter.class).getString("HELP_OPTION_CAPTION")); // NOI18N
    {
        stdYesButton.setDefaultCapable(true);
        stdOKButton.setDefaultCapable(true);
        stdNoButton.setDefaultCapable(true);
        stdNoButton.putClientProperty( "defaultButton", Boolean.FALSE ); //NOI18N
        stdCancelButton.setDefaultCapable(true);
        stdCancelButton.putClientProperty( "defaultButton", Boolean.FALSE ); //NOI18N
        stdCancelButton.setVerifyInputWhenFocusTarget(false);
        stdClosedButton.setDefaultCapable(true);
        stdClosedButton.putClientProperty( "defaultButton", Boolean.FALSE ); //NOI18N
        stdHelpButton.setDefaultCapable(true);
        stdHelpButton.putClientProperty( "defaultButton", Boolean.FALSE ); //NOI18N
        stdDetailButton.setDefaultCapable(true);
        stdDetailButton.putClientProperty( "defaultButton", Boolean.FALSE ); //NOI18N
        Mnemonics.setLocalizedText (stdHelpButton, NbBundle.getBundle(NbPresenter.class).getString("HELP_OPTION_CAPTION")); // NOI18N

        /** Initilizes accessible contexts */
        initAccessibility();
    }
    private static final String ESCAPE_COMMAND = "Escape"; // NOI18N

    private Component currentMessage;
    private JScrollPane currentScrollPane;
    private boolean leaf = false;
    private JPanel currentButtonsPanel;
    private JLabel notificationLine;
    private Component[] currentPrimaryButtons;
    private Component[] currentSecondaryButtons;

    private static final int MSG_TYPE_ERROR = 1;
    private static final int MSG_TYPE_WARNING = 2;
    private static final int MSG_TYPE_INFO = 3;
    private Color nbErrorForeground;
    private Color nbWarningForeground;
    private Color nbInfoForeground;

    /** useful only for DialogDescriptor */
    private int currentAlign;

    private ButtonListener buttonListener;
    /** Help context to actually associate with the dialog, as it is currently known. */
    private transient HelpCtx currentHelp = null;
    /** Used to prevent updateHelp from calling initializeButtons too many times. */
    private transient boolean haveCalledInitializeButtons = false;

    static final Logger LOG = Logger.getLogger(NbPresenter.class.getName());

    static final long serialVersionUID =-4508637164126678997L;

    private JButton initialDefaultButton;

    /** Creates a new Dialog from specified NotifyDescriptor,
     * with given frame owner.
     * @param d The NotifyDescriptor to create the dialog from
     */
    public NbPresenter(NotifyDescriptor d, Frame owner, boolean modal) {
        super(owner, d.getTitle(), modal); // modal
        initialize(d);
    }

    /** Creates a new Dialog from specified NotifyDescriptor,
     * with given dialog owner.
     * @param d The NotifyDescriptor to create the dialog from
     */
    public NbPresenter(NotifyDescriptor d, Dialog owner, boolean modal) {
        super(owner, d.getTitle(), modal); // modal
        initialize(d);
    }

    boolean isLeaf () {
        return leaf;
    }

    private void initAccessibility(){

        ResourceBundle bundle;
        bundle = NbBundle.getBundle(NbPresenter.class);

        stdYesButton.getAccessibleContext().setAccessibleName(bundle.getString("ACS_YES_OPTION_NAME")); // NOI18N
        stdYesButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_YES_OPTION_DESC")); // NOI18N

        stdNoButton.getAccessibleContext().setAccessibleName(bundle.getString("ACS_NO_OPTION_NAME")); // NOI18N
        stdNoButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_NO_OPTION_DESC")); // NOI18N

        stdOKButton.getAccessibleContext().setAccessibleName(bundle.getString("ACS_OK_OPTION_NAME")); // NOI18N
        stdOKButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_OK_OPTION_DESC")); // NOI18N

        stdCancelButton.getAccessibleContext().setAccessibleName(bundle.getString("ACS_CANCEL_OPTION_NAME")); // NOI18N
        stdCancelButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CANCEL_OPTION_DESC")); // NOI18N

        stdClosedButton.getAccessibleContext().setAccessibleName(bundle.getString("ACS_CLOSED_OPTION_NAME")); // NOI18N
        stdClosedButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CLOSED_OPTION_DESC")); // NOI18N

        stdHelpButton.getAccessibleContext().setAccessibleName(bundle.getString("ACS_HELP_OPTION_NAME")); // NOI18N
        stdHelpButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_HELP_OPTION_DESC")); // NOI18N

        stdDetailButton.getAccessibleContext().setAccessibleName(bundle.getString("ACS_HELP_OPTION_NAME")); // NOI18N
        stdDetailButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_HELP_OPTION_DESC")); // NOI18N
    }

    private void initialize(NotifyDescriptor d) {
        //Optimization related to jdk bug 4393857 - on pre 1.5 jdk's an
        //extra repaint is caused by the search for an opaque component up
        //to the component root. Post 1.5, root pane will automatically be
        //opaque.
        getRootPane().setOpaque(true);

        if (d instanceof WizardDescriptor || d.isNoDefaultClose() ) {
            // #81938: wizard close button shouln't work during finish progress
            setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
        } else {
            // #55273: Dialogs created by DialogDisplayer are not disposed after close
            setDefaultCloseOperation (WindowConstants.DISPOSE_ON_CLOSE);
        }
        if (!Constants.AUTO_FOCUS) {
            setAutoRequestFocus(false);
        }

        descriptor = d;

        buttonListener = new ButtonListener();
        // set leaf by DialogDescriptor, NotifyDescriptor is leaf as default
        leaf = d instanceof DialogDescriptor ? ((DialogDescriptor)d).isLeaf () : true;

        getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ESCAPE_COMMAND);
        getRootPane().getActionMap().put(ESCAPE_COMMAND, new EscapeAction());

        initializePresenter();

        pack();

        initBounds();
    }

    /** Requests focus for <code>currentMessage</code> component.
     * If it is of <code>JComponent</code> type it tries default focus
     * request first. */
    private void requestFocusForMessage() {
        Component comp = currentMessage;

        if(comp == null) {
            return;
        }

        if (/*!Constants.AUTO_FOCUS &&*/ FocusManager.getCurrentManager().getActiveWindow() == null) {
            // Do not steal focus if no Java window have it
            Component defComp = null;
            Container nearestRoot =
                (comp instanceof Container && ((Container) comp).isFocusCycleRoot()) ? (Container) comp : comp.getFocusCycleRootAncestor();
            if (nearestRoot != null) {
                defComp = nearestRoot.getFocusTraversalPolicy().getDefaultComponent(nearestRoot);
            }
            if (defComp != null) {
                defComp.requestFocusInWindow();
            } else {
                comp.requestFocusInWindow();
            }
        } else {
            if (!(comp instanceof JComponent)
                || !((JComponent)comp).requestDefaultFocus()) {

                comp.requestFocus();
            }
        }
    }

    private void initializeMessage() {
        Object newMessage = descriptor.getMessage();
        boolean isDefaultOptionPane = false;
        // replace only if old and new messages are different
        if ((currentMessage == null) || !currentMessage.equals(newMessage)) {
            uninitializeMessage();

            Component toAdd = null;

            if (descriptor.getMessageType() == NotifyDescriptor.PLAIN_MESSAGE &&
                (newMessage instanceof Component)) {
                // if plain message => use directly the component
                currentMessage = (Component)newMessage;
            } else {
                currentMessage = createOptionPane();
                isDefaultOptionPane = true;
            }
            Dimension prefSize = currentMessage.getPreferredSize();
            final Rectangle screenBounds = Utilities.getUsableScreenBounds();

            if (prefSize.width > screenBounds.width - 100
                || prefSize.height > screenBounds.height- 100
                ) {
                currentScrollPane = new JScrollPane() {
                    @Override
                    public Dimension getPreferredSize() {
                        Dimension sz = new Dimension(super.getPreferredSize());
                        if (sz.width > screenBounds.width - 100) {
                            sz.width = screenBounds.width * 3 / 4;
                        }
                        if (sz.height > screenBounds.height - 100)
                            sz.height = screenBounds.height * 3 / 4;
                        return sz;
                    }
                };
                currentScrollPane.setViewportView(currentMessage);
                toAdd = currentScrollPane;
            } else {
                toAdd = currentMessage;
            }

            if (! (descriptor instanceof WizardDescriptor) && descriptor.getNotificationLineSupport () != null) {
                JPanel enlargedToAdd = new JPanel (new BorderLayout ());
                enlargedToAdd.add (toAdd, BorderLayout.CENTER);

                nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
                if (nbErrorForeground == null) {
                    //nbErrorForeground = new Color(89, 79, 191); // RGB suggested by Bruce in #28466
                    nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
                }

                nbWarningForeground = UIManager.getColor("nb.warningForeground"); //NOI18N
                if (nbWarningForeground == null) {
                    nbWarningForeground = new Color(51, 51, 51); // Label.foreground
                }

                nbInfoForeground = UIManager.getColor("nb.warningForeground"); //NOI18N
                if (nbInfoForeground == null) {
                    nbInfoForeground = UIManager.getColor("Label.foreground"); //NOI18N
                }

                notificationLine = new FixedHeightLabel ();
                NotificationLineSupport nls = descriptor.getNotificationLineSupport ();
                if (nls.getInformationMessage () != null) {
                    updateNotificationLine (MSG_TYPE_INFO, nls.getInformationMessage ());
                } else if (nls.getWarningMessage () != null) {
                    updateNotificationLine (MSG_TYPE_WARNING, nls.getWarningMessage ());
                } else if (nls.getErrorMessage () != null) {
                    updateNotificationLine (MSG_TYPE_ERROR, nls.getErrorMessage ());
                }
                JPanel notificationPanel = new JPanel ();
                GroupLayout layout = new GroupLayout(notificationPanel);
                notificationPanel.setLayout(layout);
                layout.setHorizontalGroup(
                    layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(notificationLine)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                layout.setVerticalGroup(
                    layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(notificationLine, FixedHeightLabel.ESTIMATED_HEIGHT, FixedHeightLabel.ESTIMATED_HEIGHT, Short.MAX_VALUE)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                enlargedToAdd.add (notificationPanel, BorderLayout.SOUTH);

                // toAdd is now enlargedToAdd
                toAdd = enlargedToAdd;
            }
            getRootPane().putClientProperty("nb.default.option.pane", isDefaultOptionPane); //NOI18N
            getContentPane ().add (toAdd, BorderLayout.CENTER);
        }
    }

    private static final class FixedHeightLabel extends JLabel {

        private static final int ESTIMATED_HEIGHT = 16;

        public FixedHeightLabel () {
            super ();
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension preferredSize = super.getPreferredSize();
            assert ESTIMATED_HEIGHT == ImageUtilities.loadImage ("org/netbeans/core/windows/resources/warning.png").getHeight (null) : "Use only 16px icon.";
            preferredSize.height = Math.max (ESTIMATED_HEIGHT, preferredSize.height);
            return preferredSize;
        }
    }

    private void uninitializeMessage() {
        if (currentMessage != null) {
            if (currentScrollPane != null) {
                getContentPane().remove(currentScrollPane);
                currentScrollPane = null;
            }
            else {
                getContentPane().remove(currentMessage);
            }
            currentMessage = null;
        }
    }

    private void initializePresenter() {
        if (currentMessage != null)
            return;

        initialDefaultButton = getRootPane().getDefaultButton();
        initializeMessage();

        updateHelp();

        initializeButtons();
        haveCalledInitializeButtons = true;

        descriptor.addPropertyChangeListener(this);
        addWindowListener(this);

        initializeClosingOptions ();
    }

    /** Descriptor can be cached and reused. We need to remove listeners
     *  from descriptor, buttons and disconnect componets from container hierarchy.
     */
    private void uninitializePresenter() {
        descriptor.removePropertyChangeListener(this);
        uninitializeMessage();
        uninitializeButtons();
        uninitializeClosingOptions ();
        initialDefaultButton = null;
    }

    private final HackTypeAhead hack = new HackTypeAhead();
    @Override
    public void addNotify() {
        super.addNotify();
        initializePresenter();

        hack.activate();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        uninitializePresenter();

    }

    /** Creates option pane message.
     */
    private JOptionPane createOptionPane() {
        Object msg = descriptor.getMessage();
        boolean limitLineWidth = true;
        String strMsg = null, strMsgLower;

        if (msg instanceof String) {
            msg = ((String) msg).replace("\t", "    "); // NOI18N
            msg = ((String) msg).replace("\r", ""); // NOI18N
            //If string is html text (contains "<html>" or "<HTML>")
            //we will not override JOptionPane.getMaxCharactersPerLineCount
            //so that html text will be displayed correctly in JOptionPane
            strMsg = (String)msg;
            strMsgLower = strMsg.toLowerCase();
            limitLineWidth = !strMsgLower.startsWith("<html>"); // NOI18N
        }
        if (msg instanceof javax.accessibility.Accessible) {
            strMsg = ((javax.accessibility.Accessible)msg).getAccessibleContext().getAccessibleDescription();
        }

        JOptionPane optionPane;
        if (limitLineWidth) {
            // initialize component (override max char count per line in a message)
            optionPane = new JOptionPane(
                msg,
                descriptor.getMessageType(),
                0, // options type
                null, // icon
                new Object[0], // options
                null // value
            ) {
                @Override
                public int getMaxCharactersPerLineCount() {
                    return 100;
                }
            };
        } else {
            //Do not override JOptionPane.getMaxCharactersPerLineCount for html text
            optionPane = new JOptionPane(
                msg,
                descriptor.getMessageType(),
                0, // options type
                null, // icon
                new Object[0], // options
                null // value
            );
        }

        // javax.swing.plaf.basic.BasicOptionPaneUI uses hardcoded min height of 90,
        // if no other default is set. Min height should be about the size of the
        // used icon, so that dialogs with single-line messages can size themself properly.
        Dimension minSize = UIManager.getDimension("OptionPane.minimumSize");
        if (minSize != null) {
            minSize.setSize(minSize.getWidth(), 38);
        }

        optionPane.setWantsInput(false);
        optionPane.getAccessibleContext().setAccessibleDescription(strMsg);
        if( null != strMsg ) {
            final String clipboardText = strMsg;
            optionPane.addMouseListener( new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    showCopyToClipboardPopupMenu( e );
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    showCopyToClipboardPopupMenu( e );
                }

                private void showCopyToClipboardPopupMenu(MouseEvent e) {
                    if( e.isPopupTrigger() ) {
                        JPopupMenu pm = new JPopupMenu();
                        pm.add(new AbstractAction(NbBundle.getMessage(NbPresenter.class, "Lbl_CopyToClipboard")) { //NOI18N
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
                                c.setContents(new StringSelection(clipboardText), null);
                            }
                        });
                        pm.show(e.getComponent(), e.getX(), e.getY());
                    }
                }

            } );
        }

        // Netbeans-2667 : bug fix contributed by Experian.
        // The goal of this change is to retrieve the border which is supposed to be applied at the OptionPane.border
        // which is displayed around the message area and the button area.
        // As the JOptionPane is now wrapped on a JPanel and the buttons moved on another panel, we need to
        // retrieve the border which is applied on the JOptionPane and apply it at the content pane level.
        // Doing that we reduce inconsistencies between dialogs opened through the Java APIs or through the Netbeans APIs.
        Border optionPaneBorder = optionPane.getBorder();
        if (optionPaneBorder != null && getContentPane() instanceof JComponent) {
            ((JComponent) getContentPane()).setBorder(optionPaneBorder);
            optionPane.setBorder(null);
        }

        return optionPane;
    }

    private void uninitializeButtons() {
        if (currentButtonsPanel != null) {
            if (currentPrimaryButtons != null) {
                for (int i = 0; i < currentPrimaryButtons.length; i++) {
                    modifyListener(currentPrimaryButtons[i], buttonListener, false);
                }
            }
            if (currentSecondaryButtons != null) {
                for (int i = 0; i < currentSecondaryButtons.length; i++) {
                    modifyListener(currentSecondaryButtons[i], buttonListener, false);
                }
            }

            getContentPane().remove(currentButtonsPanel);
            currentButtonsPanel = null;
        }
    }

    private void initializeClosingOptions (boolean init) {
        Object[] options = getClosingOptions ();

        if (options == null) return ;
        for (int i = 0; i < options.length; i++) {
            modifyListener (options[i], buttonListener, init);
        }
    }

    private void initializeClosingOptions () {
        initializeClosingOptions (true);
    }

    private void uninitializeClosingOptions () {
        initializeClosingOptions (false);
    }

    /**
     * On Aqua look and feel, options should be sorted such that the default
     * button is always rightmost, and 'yes' options appear to thr right of
     * 'no' options.
     */
    public int compare (Object a, Object b) {
        boolean isDefaultButton = a.equals(descriptor.getDefaultValue ());
        int result;
        if (a.equals(NotifyDescriptor.OK_OPTION) || a.equals(NotifyDescriptor.YES_OPTION)) {
            result = 1;
        } else {
            result = 0;
        }

        if (isDefaultButton) {
            result++;
        } else if( b.equals(descriptor.getDefaultValue ()) ) {
            result--;
        }
        return result;
    }

    protected final void initializeButtons() {
        // -----------------------------------------------------------------------------
        // If there were any buttons previously, remove them and removeActionListener from them

        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager ().getFocusOwner ();

        boolean dontShowHelp = Constants.DO_NOT_SHOW_HELP_IN_DIALOGS ||
                ( descriptor instanceof WizardDescriptor && ( Boolean.FALSE.equals (((WizardDescriptor)descriptor).getProperty (WizardDescriptor.PROP_HELP_DISPLAYED)) )); // NOI18N
        boolean helpButtonShown =
            stdHelpButton.isShowing() || ( descriptor instanceof WizardDescriptor && !dontShowHelp );


        uninitializeButtons();

        Object[] primaryOptions = descriptor.getOptions();
        Object[] secondaryOptions = descriptor.getAdditionalOptions();
        currentAlign = getOptionsAlign();

        // -----------------------------------------------------------------------------
        // Obtain main (primary) and additional (secondary) buttons

        currentPrimaryButtons = null;
        currentSecondaryButtons = null;

        boolean isAqua = "Aqua".equals (UIManager.getLookAndFeel().getID()) || //NOI18N
                        "true".equalsIgnoreCase (System.getProperty ("xtest.looks_as_mac"));
        if (isAqua) {
            //No mac dialogs with buttons on side
            currentAlign = DialogDescriptor.BOTTOM_ALIGN;
        }

        // explicitly provided options (AKA buttons)
        // JST: The following line causes only problems,
        //      I hope that my change will not cause additional ones ;-)
        //    if (descriptor.getOptionType () == NotifyDescriptor.DEFAULT_OPTION) {
        if (primaryOptions != null) {
            if (isAqua) {
                Arrays.sort(primaryOptions, this);
            }
            currentPrimaryButtons = new Component [primaryOptions.length];
            for (int i = 0; i < primaryOptions.length; i++) {
                if (primaryOptions[i] == NotifyDescriptor.YES_OPTION) {
                    currentPrimaryButtons[i] = stdYesButton;
                } else if (primaryOptions[i] == NotifyDescriptor.NO_OPTION) {
                    currentPrimaryButtons[i] = stdNoButton;
                } else if (primaryOptions[i] == NotifyDescriptor.OK_OPTION) {
                    currentPrimaryButtons[i] = stdOKButton;
                    stdOKButton.setEnabled(descriptor.isValid());
                } else if (primaryOptions[i] == NotifyDescriptor.CANCEL_OPTION) {
                    currentPrimaryButtons[i] = stdCancelButton;
                } else if (primaryOptions[i] == NotifyDescriptor.CLOSED_OPTION) {
                    currentPrimaryButtons[i] = stdClosedButton;
                } else if (primaryOptions[i] instanceof Component) {
                    currentPrimaryButtons[i] = (Component) primaryOptions [i];
                } else if (primaryOptions [i] instanceof Icon) {
                    JButton button = new JButton((Icon)primaryOptions [i]);
                    // ??? Why cannot be default capable ?
                    button.putClientProperty( "defaultButton", Boolean.FALSE ); //NOI18N
                    currentPrimaryButtons[i] = button;
                } else {
                    JButton button = new JButton();
                    Mnemonics.setLocalizedText (button, primaryOptions [i].toString ());
                    button.putClientProperty( "defaultButton", Boolean.valueOf( primaryOptions[i].equals(descriptor.getDefaultValue ()) ) ); //NOI18N
                    currentPrimaryButtons[i] = button;
                }
            }
        } else { // predefined option types
            switch (descriptor.getOptionType()) {
                case NotifyDescriptor.YES_NO_OPTION:
                    if (isAqua) {
                        currentPrimaryButtons = new Component[2];
                        currentPrimaryButtons[0] = stdNoButton;
                        currentPrimaryButtons[1] = stdYesButton;
                    } else {
                        currentPrimaryButtons = new Component[2];
                        currentPrimaryButtons[0] = stdYesButton;
                        currentPrimaryButtons[1] = stdNoButton;
                    }
                    break;
                case NotifyDescriptor.YES_NO_CANCEL_OPTION:
                    currentPrimaryButtons = new Component[3];
                    if (isAqua) {
                        currentPrimaryButtons[0] = stdCancelButton;
                        currentPrimaryButtons[1] = stdNoButton;
                        currentPrimaryButtons[2] = stdYesButton;
                    } else {
                        currentPrimaryButtons[0] = stdYesButton;
                        currentPrimaryButtons[1] = stdNoButton;
                        currentPrimaryButtons[2] = stdCancelButton;
                    }
                    break;
                case NotifyDescriptor.OK_CANCEL_OPTION:
                default:
                    if (isAqua) {
                        currentPrimaryButtons = new Component[2];
                        currentPrimaryButtons[0] = stdCancelButton;
                        currentPrimaryButtons[1] = stdOKButton;
                    } else {
                        currentPrimaryButtons = new Component[2];
                        currentPrimaryButtons[0] = stdOKButton;
                        currentPrimaryButtons[1] = stdCancelButton;
                    }
                    stdOKButton.setEnabled(descriptor.isValid());
                    break;
            }
        }

        if ((secondaryOptions != null) && (secondaryOptions.length != 0)) {
            currentSecondaryButtons = new Component [secondaryOptions.length];
            Arrays.sort (secondaryOptions, this);
            for (int i = 0; i < secondaryOptions.length; i++) {
                if (secondaryOptions[i] == NotifyDescriptor.YES_OPTION) {
                    currentSecondaryButtons[i] = stdYesButton;
                } else if (secondaryOptions[i] == NotifyDescriptor.NO_OPTION) {
                    currentSecondaryButtons[i] = stdNoButton;
                } else if (secondaryOptions[i] == NotifyDescriptor.OK_OPTION) {
                    currentSecondaryButtons[i] = stdOKButton;
                    stdOKButton.setEnabled(descriptor.isValid());
                } else if (secondaryOptions[i] == NotifyDescriptor.CANCEL_OPTION) {
                    currentSecondaryButtons[i] = stdCancelButton;
                } else if (secondaryOptions[i] == NotifyDescriptor.CLOSED_OPTION) {
                    currentSecondaryButtons[i] = stdClosedButton;
                } else if (secondaryOptions[i] instanceof Component) {
                    currentSecondaryButtons[i] = (Component) secondaryOptions [i];
                } else if (secondaryOptions [i] instanceof Icon) {
                    JButton button = new JButton((Icon)secondaryOptions [i]);
                    currentSecondaryButtons[i] = button;
                } else {
                    JButton button = new JButton();
                    Mnemonics.setLocalizedText (button, secondaryOptions [i].toString ());
                    currentSecondaryButtons[i] = button;
                }
            }
        }

        // Automatically add a help button if needed.

        if (!dontShowHelp && (currentHelp != null || helpButtonShown)) {
            if (helpButtonLeft()) {
                if (currentSecondaryButtons == null) currentSecondaryButtons = new Component[] { };
                Component[] cSB2 = new Component[currentSecondaryButtons.length + 1];
                System.arraycopy(currentSecondaryButtons, 0, cSB2, 1, currentSecondaryButtons.length);
                cSB2[0] = stdHelpButton;
                currentSecondaryButtons = cSB2;
            } else {
                if (currentPrimaryButtons == null) currentPrimaryButtons = new Component[] { };
                Component[] cPB2 = new Component[currentPrimaryButtons.length + 1];
                if (isAqua) { //NOI18N
                    //Mac default dlg button should be rightmost, not the help button
                    System.arraycopy(currentPrimaryButtons, 0, cPB2, 1, currentPrimaryButtons.length);
                    cPB2[0] = stdHelpButton;
                } else {
                    System.arraycopy(currentPrimaryButtons, 0, cPB2, 0, currentPrimaryButtons.length);
                    cPB2[currentPrimaryButtons.length] = stdHelpButton;
                }
                currentPrimaryButtons = cPB2;
            }
            stdHelpButton.setEnabled(currentHelp != null);
        }

        // -----------------------------------------------------------------------------
        // Create panels for main (primary) and additional (secondary) buttons and add to content pane

        if (currentAlign == DialogDescriptor.BOTTOM_ALIGN || currentAlign == -1) {

            JPanel panelForPrimary = null;
            JPanel panelForSecondary = null;


            if (currentPrimaryButtons != null) {
                panelForPrimary = new JPanel();

                if (currentAlign == -1) {
                    panelForPrimary.setLayout(new org.openide.awt.EqualFlowLayout());
                } else {
                    panelForPrimary.setLayout(new org.openide.awt.EqualFlowLayout(FlowLayout.RIGHT));
                }
                for (int i = 0; i < currentPrimaryButtons.length; i++) {
                    modifyListener(currentPrimaryButtons[i], buttonListener, true); // add button listener
                    panelForPrimary.add(currentPrimaryButtons[i]);
                }
            }

            if (currentSecondaryButtons != null) {
                panelForSecondary = new JPanel();
                panelForSecondary.setLayout(new org.openide.awt.EqualFlowLayout(FlowLayout.LEFT));
                for (int i = 0; i < currentSecondaryButtons.length; i++) {
                    modifyListener(currentSecondaryButtons[i], buttonListener, true); // add button listener
                    panelForSecondary.add(currentSecondaryButtons[i]);
                }
            }

            // both primary and secondary buttons are used
            if ((panelForPrimary != null) && (panelForSecondary != null)) {
                currentButtonsPanel = new JPanel();
                currentButtonsPanel.setLayout(new BorderLayout());
                currentButtonsPanel.add(panelForPrimary, BorderLayout.EAST);
                currentButtonsPanel.add(panelForSecondary, BorderLayout.WEST);
            } else if (panelForPrimary != null) {
                currentButtonsPanel = panelForPrimary;
            } else {
                currentButtonsPanel = panelForSecondary;
            }

            // add final button panel to the dialog
            if ((currentButtonsPanel != null)&&(currentButtonsPanel.getComponentCount() != 0)) {
                if (currentButtonsPanel.getBorder() == null) {
                    currentButtonsPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 0, 5, 5)));
                }
                getContentPane().add(currentButtonsPanel, BorderLayout.SOUTH);
            }

        } else if (currentAlign == DialogDescriptor.RIGHT_ALIGN) {
            currentButtonsPanel = new JPanel();
            currentButtonsPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0f;
            gbc.insets = new Insets(5, 4, 2, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            if (currentPrimaryButtons != null) {
                for (int i = 0; i < currentPrimaryButtons.length; i++) {
                    modifyListener(currentPrimaryButtons[i], buttonListener, true); // add button listener
                    currentButtonsPanel.add(currentPrimaryButtons[i], gbc);
                }
            }

            GridBagConstraints padding = new GridBagConstraints();
            padding.gridwidth = GridBagConstraints.REMAINDER;
            padding.weightx = 1.0f;
            padding.weighty = 1.0f;
            padding.fill = GridBagConstraints.BOTH;
            currentButtonsPanel.add(new JPanel(), padding);

            gbc.insets = new Insets(2, 4, 5, 5);
            if (currentSecondaryButtons != null) {
                for (int i = 0; i < currentSecondaryButtons.length; i++) {
                    modifyListener(currentSecondaryButtons[i], buttonListener, true); // add button listener
                    currentButtonsPanel.add(currentSecondaryButtons[i], gbc);
                }
            }

            // add final button panel to the dialog
            if (currentButtonsPanel != null) {
                if (currentButtonsPanel.getBorder() == null) {
                    currentButtonsPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
                }
                getContentPane().add(currentButtonsPanel, BorderLayout.EAST);
            }

        }
        updateDefaultButton();


        Component fo = KeyboardFocusManager.getCurrentKeyboardFocusManager ().getFocusOwner ();

        if (fo != focusOwner && focusOwner != null) {
            focusOwner.requestFocus();
        }
    }

    /** @return returns true if the Help button should be at the left side
     */
    private boolean helpButtonLeft() {
        boolean result = false;
        try {
            String resValue = NbBundle.getMessage(NbPresenter.class, "HelpButtonAtTheLeftSide" ); //NOI18N
            result = "true".equalsIgnoreCase(resValue); //NOI18N
        } catch( MissingResourceException e ) {
            //ignore
        }
        return result;
    }

    /** Checks default button and updates it
     */
    private void updateDefaultButton() {
        // bugfix 37083, respects DialogDescriptor's initial value ?
        if (descriptor.getDefaultValue () != null) {
            if (descriptor.getDefaultValue () instanceof JButton) {
                JButton b = (JButton)descriptor.getDefaultValue ();
            if (b.isVisible() && b.isEnabled () && b.isDefaultCapable ()
                    && !Boolean.FALSE.equals(b.getClientProperty("defaultButton")) ) { //NOI18N
                    getRootPane ().setDefaultButton (b);
                    return ;
                }
            } else {
                JButton b = null;
                Collection<Component> currentActive = new HashSet<Component> ();
                if (currentPrimaryButtons != null) {
                    currentActive.addAll (Arrays.asList (currentPrimaryButtons));
                }
                if (currentSecondaryButtons != null) {
                    currentActive.addAll (Arrays.asList (currentSecondaryButtons));
                }
                Arrays.asList (currentPrimaryButtons);
                if (descriptor.getDefaultValue ().equals (NotifyDescriptor.OK_OPTION) && currentActive.contains (stdOKButton)) {
                    b = stdOKButton;
                } else if (descriptor.getDefaultValue ().equals (NotifyDescriptor.YES_OPTION) && currentActive.contains (stdYesButton)) {
                    b = stdYesButton;
                } else if (descriptor.getDefaultValue ().equals (NotifyDescriptor.NO_OPTION)) {
                    b = stdNoButton;
                } else if (descriptor.getDefaultValue ().equals (NotifyDescriptor.CANCEL_OPTION)) {
                    b = stdCancelButton;
                } else if (descriptor.getDefaultValue ().equals (NotifyDescriptor.CLOSED_OPTION)) {
                    b = stdClosedButton;
                }
                if (b != null && b.isVisible() && b.isEnabled ()) {
                    getRootPane ().setDefaultButton (b);
                    return ;
                }
            }
        } else {
            // ??? unset default button if descriptor.getValue() is null
        }
        if (currentPrimaryButtons != null) {
            // finds default button
            for (int i = 0; i < currentPrimaryButtons.length; i++) {
                if (currentPrimaryButtons[i] instanceof JButton) {
                    JButton b = (JButton)currentPrimaryButtons[i];
                    if (b.isVisible() && b.isEnabled() && b.isDefaultCapable()
                            && !Boolean.FALSE.equals(b.getClientProperty("defaultButton"))) { //NOI18N
                        getRootPane().setDefaultButton(b);
                        return;
                    }
                }
            }
        }
        // no default capable button found
        if( null != initialDefaultButton && initialDefaultButton.isEnabled() && initialDefaultButton.isDefaultCapable() )
            getRootPane().setDefaultButton( initialDefaultButton );
        else
            getRootPane().setDefaultButton(null);
    }

    private void updateNotificationLine (int msgType, Object o) {
        String msg = o == null ? null : o.toString ();
        if (msg != null && msg.trim().length() > 0) {
            switch (msgType) {
                case MSG_TYPE_ERROR:
                    prepareMessage(notificationLine, ImageUtilities.loadImageIcon("org/netbeans/core/windows/resources/error.png", false),
                        nbErrorForeground);
                    break;
                case MSG_TYPE_WARNING:
                    prepareMessage(notificationLine, ImageUtilities.loadImageIcon("org/netbeans/core/windows/resources/warning.png", false),
                        nbWarningForeground);
                    break;
                case MSG_TYPE_INFO:
                    prepareMessage(notificationLine, ImageUtilities.loadImageIcon("org/netbeans/core/windows/resources/info.png", false),
                        nbInfoForeground);
                    break;
                default:
            }
            notificationLine.setToolTipText (msg);
        } else {
            prepareMessage(notificationLine, null, null);
            notificationLine.setToolTipText (null);
        }
        notificationLine.setText(msg);
    }

    private void prepareMessage(JLabel label, ImageIcon icon, Color fgColor) {
        label.setIcon(icon);
        label.setForeground(fgColor);
    }

    /** Enables/disables OK button if it is present
     */
    private void updateOKButton(boolean valid) {
        if (currentPrimaryButtons != null) {
            for (int i = 0; i < currentPrimaryButtons.length; i++) {
                if (currentPrimaryButtons[i] instanceof JButton) {
                    JButton b = (JButton)currentPrimaryButtons[i];
                    if ((b == stdOKButton) && b.isVisible()) {
                        b.setEnabled(valid);
                    }
                }
            }
        }
        if (currentSecondaryButtons != null) {
            for (int i = 0; i < currentSecondaryButtons.length; i++) {
                if (currentSecondaryButtons[i] instanceof JButton) {
                    JButton b = (JButton)currentSecondaryButtons[i];
                    if ((b == stdOKButton) && b.isVisible()) {
                        b.setEnabled(valid);
                    }
                }
            }
        }
    }

    private void modifyListener(Object comp, ButtonListener l, boolean add) {
        // on JButtons attach simply by method call
        if (comp instanceof JButton) {
            JButton b = (JButton)comp;
            if (add) {
                List listeners;
                listeners = Arrays.asList (b.getActionListeners ());
                if (!listeners.contains (l)) {
                    b.addActionListener(l);
                }
                listeners = Arrays.asList (b.getComponentListeners ());
                if (!listeners.contains (l)) {
                    b.addComponentListener(l);
                }
                listeners = Arrays.asList (b.getPropertyChangeListeners ());
                if (!listeners.contains (l)) {
                    b.addPropertyChangeListener(l);
                }
            } else {
                b.removeActionListener(l);
                b.removeComponentListener(l);
                b.removePropertyChangeListener(l);
            }
            return;
        } else {
            // we will have to use dynamic method invocation to add the action listener
            // to generic component (and we succeed only if it has the addActionListener method)
            java.lang.reflect.Method m = null;
            try {
                m = comp.getClass().getMethod(add ? "addActionListener" : "removeActionListener", new Class[] { ActionListener.class });// NOI18N
                try {
                    m.setAccessible (true);
                } catch (SecurityException se) {
                    m = null; // no jo, we cannot make accessible
                }
            } catch (NoSuchMethodException e) {
                m = null; // no jo, we cannot attach ActionListener to this Component
            } catch (SecurityException e2) {
                m = null; // no jo, we cannot attach ActionListener to this Component
            }
            if (m != null) {
                try {
                    m.invoke(comp, new Object[] { l });
                } catch (Exception e) {
                    // not succeeded, so give up
                }
            }
        }
    }

    /** Shows the dialog, used in method show so no inner class is needed.
     */
    private void superShow() {
        assert SwingUtilities.isEventDispatchThread () : "Invoked super.show() in AWT event thread."; // NOI18N
        super.show();
    }

    @Override @Deprecated
    public void show() {
        //Bugfix #29993: Call show() asynchronously for non modal dialogs.
        if (isModal()) {
            Mutex.EVENT.readAccess(this);
        } else {
            if (SwingUtilities.isEventDispatchThread()) {
                doShow();
            } else {
                SwingUtilities.invokeLater(new Runnable () {
                    public void run () {
                        doShow();
                    }
                });
            }
        }
    }

    @Override
    public Void run() {
        doShow();
        return null;
    }

    private void doShow () {
        //#206802 - dialog windows are hidden behind full screen window
        Window fullScreenWindow = null;
        if( Utilities.isUnix() ) {
            GraphicsDevice gd = getGraphicsConfiguration().getDevice();
            if( gd.isFullScreenSupported() ) {
                fullScreenWindow = gd.getFullScreenWindow();
                if( null != fullScreenWindow )
                    gd.setFullScreenWindow( null );
            }
        }
        try {
            MenuSelectionManager.defaultManager().clearSelectedPath();
        } catch( NullPointerException npE ) {
            //#216184
            LOG.log( Level.FINE, null, npE );
        }

        superShow();

        if( null != fullScreenWindow ) {
            getGraphicsConfiguration().getDevice().setFullScreenWindow( fullScreenWindow );
        }
    }

    @Override
    public void propertyChange(final java.beans.PropertyChangeEvent evt) {
        if( !SwingUtilities.isEventDispatchThread() ) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    propertyChange(evt);
                }
            });
            return;
        }
        boolean update = false;

        if (DialogDescriptor.PROP_OPTIONS.equals(evt.getPropertyName())) {
            initializeButtons();
            update = true;
        } else if (DialogDescriptor.PROP_OPTION_TYPE.equals(evt.getPropertyName())) {
            initializeButtons();
            update = true;
        } else if (DialogDescriptor.PROP_OPTIONS_ALIGN.equals(evt.getPropertyName())) {
            initializeButtons();
            update = true;
        } else if (DialogDescriptor.PROP_MESSAGE.equals(evt.getPropertyName())) {
            initializeMessage();
            requestFocusForMessage();
            // In case change of help ID on component message:
            updateHelp();
            update = true;
        } else if (DialogDescriptor.PROP_MESSAGE_TYPE.equals(evt.getPropertyName())) {
            initializeMessage();
            requestFocusForMessage();
            update = true;
        } else if (DialogDescriptor.PROP_TITLE.equals(evt.getPropertyName())) {
            setTitle(descriptor.getTitle());
        } else if (DialogDescriptor.PROP_NO_DEFAULT_CLOSE.equals(evt.getPropertyName())) {
            setDefaultCloseOperation( descriptor instanceof WizardDescriptor || descriptor.isNoDefaultClose()
                    ? JDialog.DO_NOTHING_ON_CLOSE : JDialog.DISPOSE_ON_CLOSE );
        } else if (DialogDescriptor.PROP_HELP_CTX.equals(evt.getPropertyName())) {
            // bugfix #40057, restore focus owner after help update
            Component fo = KeyboardFocusManager.getCurrentKeyboardFocusManager ().getFocusOwner ();
            updateHelp();
            // In case buttons have changed: //just buttons!!
            // note, currentButtonsPanel may be null
            if (currentButtonsPanel != null) {
                currentButtonsPanel.revalidate();
            }
            if (currentButtonsPanel != null) {
                currentButtonsPanel.repaint();
            }
            if (fo != null) fo.requestFocus();
        } else if (DialogDescriptor.PROP_VALID.equals(evt.getPropertyName())) {
            updateOKButton(((Boolean)(evt.getNewValue())).booleanValue());
        } else if (NotifyDescriptor.PROP_INFO_NOTIFICATION.equals (evt.getPropertyName ())) {
            // XXX: need set update on true?
            updateNotificationLine (MSG_TYPE_INFO, evt.getNewValue ());
        } else if (NotifyDescriptor.PROP_WARNING_NOTIFICATION.equals (evt.getPropertyName ())) {
            // XXX: need set update on true?
            updateNotificationLine (MSG_TYPE_WARNING, evt.getNewValue ());
        } else if (NotifyDescriptor.PROP_ERROR_NOTIFICATION.equals (evt.getPropertyName ())) {
            // XXX: need set update on true?
            updateNotificationLine (MSG_TYPE_ERROR, evt.getNewValue ());
        }

        if (update) {
            Dimension sz = getSize();
            Dimension prefSize = getPreferredSize();
            if (prefSize.width > sz.width || prefSize.height > sz.height) {
                setSize(Math.max(prefSize.width, sz.width),
                        Math.max(prefSize.height, sz.height));
            }
            invalidate();
            validate();
            repaint();
        }
    }

    private void updateHelp() {
        //System.err.println ("Updating help for NbDialog...");
        HelpCtx help = getHelpCtx();
        // Handle help from the inner component automatically (see docs
        // in DialogDescriptor):
        if (HelpCtx.DEFAULT_HELP.equals(help)) {
            Object msg = descriptor.getMessage();
            if (msg instanceof Component) {
                help = HelpCtx.findHelp((Component) msg);
            }
            if (HelpCtx.DEFAULT_HELP.equals(help)) help = null;
        }
        if (! Utilities.compareObjects(currentHelp, help)) {
            currentHelp = help;
            if (help != null && help.getHelpID() != null) {
                //System.err.println ("New help ID for root pane: " + help.getHelpID ());
                HelpCtx.setHelpIDString(getRootPane(), help.getHelpID());
            }
            // Refresh button list if it had already been created.
            if (haveCalledInitializeButtons) initializeButtons();
        }
    }

    /** Options align.
     */
    protected int getOptionsAlign() {
        return DialogDescriptor.DEFAULT_ALIGN;
    }

    /** Getter for button listener or null
     */
    protected ActionListener getButtonListener() {
        return null;
    }

    /** Closing options.
     */
    protected Object[] getClosingOptions() {
        return null;
    }

    /** Updates help.
     */
    protected HelpCtx getHelpCtx() {
        return null;
    }


    public void windowDeactivated(final java.awt.event.WindowEvent p1) {
    }
    public void windowClosed(final java.awt.event.WindowEvent p1) {
    }
    public void windowDeiconified(final java.awt.event.WindowEvent p1) {
    }
    public void windowOpened(final java.awt.event.WindowEvent p1) {
    }
    public void windowIconified(final java.awt.event.WindowEvent p1) {
    }
    public void windowClosing(final java.awt.event.WindowEvent p1) {
        // #81938: special handling WizardDescriptor to avoid close wizard during instantiate
        if (!descriptor.isNoDefaultClose() ) {
            descriptor.setValue(NotifyDescriptor.CLOSED_OPTION);
            buttonListener.actionPerformed(new ActionEvent(NotifyDescriptor.CLOSED_OPTION, -1, ""));
        }
    }
    public void windowActivated(final java.awt.event.WindowEvent p1) {
    }

    @Deprecated
    public static void addChangeListener(ChangeListener l) {
        // Does nothing
    }
    @Deprecated
    public static void removeChangeListener(ChangeListener l) {
        // Does nothing
    }

    private final class EscapeAction extends AbstractAction {

        public EscapeAction () {
            putValue(Action.ACTION_COMMAND_KEY, ESCAPE_COMMAND);
        }

        public void actionPerformed(ActionEvent e) {
            if( !descriptor.isNoDefaultClose() )
            buttonListener.actionPerformed(e);
        }

    }

    /** Button listener
     */
    private class ButtonListener implements ActionListener, ComponentListener, PropertyChangeListener {
        ButtonListener() {}
        public void actionPerformed(ActionEvent evt) {
            boolean isAqua = "Aqua".equals (UIManager.getLookAndFeel().getID()) || //NOI18N
                            "true".equalsIgnoreCase (System.getProperty ("xtest.looks_as_mac"));

            Object pressedOption = evt.getSource();
            // handle ESCAPE
            if (ESCAPE_COMMAND.equals (evt.getActionCommand ())) {
                MenuElement[] selPath = MenuSelectionManager.defaultManager().getSelectedPath();
                // part of #130919 fix - handle ESC key well in dialogs with menus
                if (selPath == null || selPath.length == 0) {
                    pressedOption = NotifyDescriptor.CLOSED_OPTION;
                } else {
                    MenuSelectionManager.defaultManager().clearSelectedPath();
                    return ;
                }
            } else {
                // handle buttons
                if (evt.getSource() == stdHelpButton) {
                    showHelp(currentHelp);
                    return;
                }

                Object[] options = descriptor.getOptions();
                if (isAqua && options != null) {
                    Arrays.sort (options, NbPresenter.this);
                }

                if (
                options != null &&
                currentPrimaryButtons != null &&
                options.length == (currentPrimaryButtons.length -
                    ((currentHelp != null) ? 1 : 0))
                ) {
                    int offset = currentHelp != null && isAqua ?
                        -1 : 0;
                    for (int i = 0; i < currentPrimaryButtons.length; i++) {
                        if (evt.getSource() == currentPrimaryButtons[i]) {
                            pressedOption = options[i + offset];
                        }
                    }
                }

                options = descriptor.getAdditionalOptions();
                if (isAqua && options != null) {
                    Arrays.sort (options, NbPresenter.this);
                }

                if (
                options != null &&
                currentSecondaryButtons != null &&
                options.length == currentSecondaryButtons.length
                ) {
                    for (int i = 0; i < currentSecondaryButtons.length; i++) {
                        if (evt.getSource() == currentSecondaryButtons[i]) {
                            pressedOption = options[i];
                        }
                    }
                }

                if (evt.getSource() == stdYesButton) {
                    pressedOption = NotifyDescriptor.YES_OPTION;
                } else if (evt.getSource() == stdNoButton) {
                    pressedOption = NotifyDescriptor.NO_OPTION;
                } else if (evt.getSource() == stdCancelButton) {
                    pressedOption = NotifyDescriptor.CANCEL_OPTION;
                } else if (evt.getSource() == stdClosedButton) {
                    pressedOption = NotifyDescriptor.CLOSED_OPTION;
                } else if (evt.getSource() == stdOKButton) {
                    pressedOption = NotifyDescriptor.OK_OPTION;
                }
            }

            descriptor.setValue(pressedOption);

            ActionListener al = getButtonListener();
            if (al != null) {

                if (pressedOption == evt.getSource()) {
                    al.actionPerformed(evt);
                } else {
                    al.actionPerformed(new ActionEvent(
                    pressedOption, evt.getID(), evt.getActionCommand(), evt.getModifiers()
                    ));
                }
            }

            Object[] arr = getClosingOptions();
            if (arr == null || pressedOption == NotifyDescriptor.CLOSED_OPTION) {
                // all options should close
                dispose();
            } else {
                java.util.List l = java.util.Arrays.asList(arr);

                if (l.contains(pressedOption)) {
                    dispose();
                }
            }
        }
        public void componentShown(final java.awt.event.ComponentEvent p1) {
            updateDefaultButton();
        }
        public void componentResized(final java.awt.event.ComponentEvent p1) {
        }

        public void componentHidden(final java.awt.event.ComponentEvent p1) {
            updateDefaultButton();
        }

        public void componentMoved(final java.awt.event.ComponentEvent p1) {
        }

        public void propertyChange(final java.beans.PropertyChangeEvent p1) {
            if ("enabled".equals(p1.getPropertyName())) {
                updateDefaultButton();
            }
        }
    }

    @Override
    public javax.accessibility.AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleNbPresenter();
        }
        return accessibleContext;
    }

    private static String getMessageTypeDescription(int messageType) {
        switch(messageType) {
        case NotifyDescriptor.ERROR_MESSAGE:
            return NbBundle.getBundle(NbPresenter.class).getString("ACSD_ErrorMessage"); // NOI18N
        case NotifyDescriptor.WARNING_MESSAGE:
            return NbBundle.getBundle(NbPresenter.class).getString("ACSD_WarningMessage"); // NOI18N
        case NotifyDescriptor.QUESTION_MESSAGE:
            return NbBundle.getBundle(NbPresenter.class).getString("ACSD_QuestionMessage"); // NOI18N
        case NotifyDescriptor.INFORMATION_MESSAGE:
            return NbBundle.getBundle(NbPresenter.class).getString("ACSD_InformationMessage"); // NOI18N
        case NotifyDescriptor.PLAIN_MESSAGE:
            return NbBundle.getBundle(NbPresenter.class).getString("ACSD_PlainMessage"); // NOI18N
        }
        return ""; // NOI18N
    }

    private class AccessibleNbPresenter extends AccessibleJDialog {
        AccessibleNbPresenter() {}
        @Override
        public String getAccessibleName() {
            if (accessibleName != null) {
                return accessibleName;
            } else {
                if (currentMessage instanceof javax.accessibility.Accessible
                    && currentMessage.getAccessibleContext().getAccessibleName() != null) {
                    return currentMessage.getAccessibleContext().getAccessibleName();
                } else {
                    return super.getAccessibleName();
                }
            }
        }
        @Override
        public String getAccessibleDescription() {
            if (accessibleDescription != null) {
                return accessibleDescription;
            } else {
                if (currentMessage instanceof javax.accessibility.Accessible
                    && currentMessage.getAccessibleContext().getAccessibleDescription() != null) {
                    return java.text.MessageFormat.format(
                        getMessageTypeDescription(descriptor.getMessageType()),
                        new Object[] {
                            currentMessage.getAccessibleContext().getAccessibleDescription()
                        }
                    );
                } else {
                    return super.getAccessibleDescription();
                }
            }
        }
    }

    static Field markers;
    static Method dequeue;
    static {
        if (Boolean.getBoolean("netbeans.hack.50423")) { // NOI18N
            try {
                markers = DefaultKeyboardFocusManager.class.getDeclaredField("typeAheadMarkers"); // NOI18N
                markers.setAccessible(true);
                dequeue = DefaultKeyboardFocusManager.class.getDeclaredMethod("dequeueKeyEvents", new Class[] { Long.TYPE, java.awt.Component.class });
                dequeue.setAccessible(true);
            } catch (Throwable ex) {
                LOG.log(Level.WARNING, "Not activating workaround for #50423", ex); // NOI18N
            }
        }
    }

    private final class HackTypeAhead implements Runnable {
        private RequestProcessor.Task task = RequestProcessor.getDefault().create(this);


        public HackTypeAhead() {
        }

        public void activate() {
            if (markers != null) {
                task.schedule(1000);
            }
        }

        public void run() {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(this);
                return;
            }

            KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            Collection result = null;
            try {
                result = (Collection) markers.get(fm);
            } catch (Exception ex) {
                Logger.getLogger(NbPresenter.class.getName()).log(Level.WARNING, null, ex);
            }

            if (result == null || result.isEmpty()) {
                return;
            }

            LOG.warning("Symptoms of #50423: There is something in type ahead: " + result + " requesting focus change"); // NOI18N
            try {
                dequeue.invoke(fm, new Object[] { Long.valueOf(-1), NbPresenter.this });
            } catch (Exception ex) {
                Logger.getLogger(NbPresenter.class.getName()).log(Level.WARNING, null, ex);
            }
        }
    } // end of HackTypeAhead

    /** Shows a specified HelpCtx in IDE's help window.
    * @param helpCtx thehelp to be shown
    */
    private static void showHelp(HelpCtx helpCtx) {
        if (!helpCtx.display()) {
            Utilities.disabledActionBeep();
        }
    }

    private void initBounds() {
        Window w = findFocusedWindow();
        if( null != w ) {
            //#133235 - dialog windows should be centered on the main app window, not the whole screen
            setLocationRelativeTo( w );
            Rectangle screen = Utilities.getUsableScreenBounds( w.getGraphicsConfiguration() );
            Rectangle bounds = getBounds();
            int dx = bounds.x;
            int dy = bounds.y;
            // bottom
            if (dy + bounds.height > screen.y + screen.height) {
                dy = screen.y + screen.height - bounds.height;
            }
            // top
            if (dy < screen.y) {
                dy = screen.y;
            }
            // right
            if (dx + bounds.width > screen.x + screen.width) {
                dx = screen.x + screen.width - bounds.width;
            }
            // left
            if (dx < screen.x) {
                dx = screen.x;
            }
            setLocation( dx, dy );
        } else {
            //just center the dialog on the screen and let's hope it'll be
            //the correct one in multi-monitor setup
            Dimension size = getSize();
            Rectangle centerBounds = Utilities.findCenterBounds(size);
            if(size.equals(centerBounds.getSize())) {
                setLocation(centerBounds.x, centerBounds.y);
            } else {
                setBounds(centerBounds);
            }
        }
    }

    /**
     * @return Focused and showing Window or null.
     */
    private Window findFocusedWindow() {
        Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
        if( w == null ) {
            // PR#5280
            LOG.fine( () -> "No focused window, find mainWindow" );
            for( Frame f01 : Frame.getFrames() ) {
                if( "NbMainWindow".equals(f01.getName())) { //NOI18N
                    if(f01.getWidth() != 0 || f01.getHeight() != 0) {
                        w = f01;
                    }
                    break;
                }
            }
        }
        while( null != w ) {
            if ((w instanceof Frame || w instanceof Dialog) && w.isShowing()) {
                break;
            }
            w = w.getOwner();
        }
        return w;
    }
}

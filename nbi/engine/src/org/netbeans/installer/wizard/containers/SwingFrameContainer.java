/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.wizard.containers;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiFrame;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiSeparator;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 * This class is a conscrete implementation of the {@link SwingContainer} interface.
 * In this case the container is an {@link NbiFrame}.
 *
 * @author Kirill Sorokin
 * @sicne 1.0
 */
public class SwingFrameContainer extends NbiFrame implements SwingContainer {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * Instance of {@link SwingUi} that is currently shown by the container.
     */
    private SwingUi currentUi;
    
    /**
     * Content pane used by the container.
     */
    private WizardFrameContentPane contentPane;
    
    /**
     * Prefix of the container frame title.
     */
    private String frameTitlePrefix;
    
    /**
     * Pattern which should be used to combine the container frame's title prefix
     * and the title of the current UI.
     */
    private String frameTitlePattern;
    
    /**
     * Creates a new instance of {@link SwingFrameContainer}. The constructor calls
     * the initialization routine of the parent class and searches the system
     * properties for settings which may be releavant to this type of container.
     * Additionally it initializes and lays out the core swing components of the
     * container.
     */
    public SwingFrameContainer() {        
        super();    
        
        frameWidth = UiUtils.getDimension(System.getProperties(),
                WIZARD_FRAME_WIDTH_PROPERTY,
                DEFAULT_WIZARD_FRAME_WIDTH);
        frameMinimumWidth = UiUtils.getDimension(System.getProperties(),
                WIZARD_FRAME_MINIMUM_WIDTH_PROPERTY,
                DEFAULT_WIZARD_FRAME_MINIMUM_WIDTH);
        frameMaximumWidth = UiUtils.getDimension(System.getProperties(),
                WIZARD_FRAME_MAXIMUM_WIDTH_PROPERTY,
                DEFAULT_WIZARD_FRAME_MAXIMUM_WIDTH);
        
        frameHeight = UiUtils.getDimension(System.getProperties(),
                WIZARD_FRAME_HEIGHT_PROPERTY,
                DEFAULT_WIZARD_FRAME_HEIGHT);
        frameMinimumHeight = UiUtils.getDimension(System.getProperties(),
                WIZARD_FRAME_MINIMUM_HEIGHT_PROPERTY,
                DEFAULT_WIZARD_FRAME_MINIMUM_HEIGHT);
        frameMaximumHeight = UiUtils.getDimension(System.getProperties(),
                WIZARD_FRAME_MAXIMUM_HEIGHT_PROPERTY,
                DEFAULT_WIZARD_FRAME_MAXIMUM_HEIGHT);
        
        boolean customIconLoaded = false;
        if (System.getProperty(WIZARD_FRAME_ICON_URI_PROPERTY) != null) {
            final String frameIconUri =
                    System.getProperty(WIZARD_FRAME_ICON_URI_PROPERTY);
            
            try {
                frameIcon = FileProxy.getInstance().getFile(frameIconUri,true);
                customIconLoaded = true;
            } catch (DownloadException e) {
                ErrorManager.notifyWarning(ResourceUtils.getString(
                        SwingFrameContainer.class,
                        RESOURCE_FAILED_TO_DOWNLOAD_WIZARD_ICON,
                        frameIconUri), e);
            }
        }
        
        if (!customIconLoaded) {
            final String frameIconUri = DEFAULT_WIZARD_FRAME_ICON_URI;
            
            try {
                frameIcon = FileProxy.getInstance().getFile(frameIconUri,true);
                customIconLoaded = true;
            } catch (DownloadException e) {
                ErrorManager.notifyWarning(ResourceUtils.getString(
                        SwingFrameContainer.class,
                        RESOURCE_FAILED_TO_DOWNLOAD_WIZARD_ICON,
                        frameIconUri), e);
            }
        }
        
        frameTitlePrefix = DEFAULT_WIZARD_FRAME_TITLE_PREFIX;
        if (System.getProperty(WIZARD_FRAME_TITLE_PREFIX_PROPERTY) != null) {
            frameTitlePrefix =
                    System.getProperty(WIZARD_FRAME_TITLE_PREFIX_PROPERTY);
        }
        
        frameTitlePattern = DEFAULT_WIZARD_FRAME_TITLE_PATTERN;
        if (System.getProperty(WIZARD_FRAME_TITLE_PATTERN_PROPERTY) != null) {
            frameTitlePattern =
                    System.getProperty(WIZARD_FRAME_TITLE_PATTERN_PROPERTY);
        }
        
        initComponents();
    }
    
    /**
     * This method overrides {@link NbiFrame#setVisible()} and at the same time
     * implements {@link WizardContainer#setVisible()}. It is responsible for
     * showing and hiding the wizard container frame.
     *
     * @param visible Whether to show the frame - <code>true</code>, or to hide
     *      it - <code>false</code>.
     */
    @Override
    public void setVisible(final boolean visible) {
        super.setVisible(visible);
        
        if (visible == false) {
            dispose();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateWizardUi(final WizardUi wizardUi) {
        if(wizardUi==null) {
            currentUi = null;
            return;
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        updateWizardUi(wizardUi);
                    }
                });
            } catch (InterruptedException e) {
                LogManager.log("Error during updating wizard UI", e);
            } catch (InvocationTargetException e) {
                LogManager.log("Error during updating wizard UI", e);
            }
            return;
        }
        
        // save the ui reference
        currentUi = wizardUi.getSwingUi(this);
        
        final int currentHeight = getSize().height;             
        
          // update the frame title
        if (currentUi.getTitle() != null) {
            setTitle(StringUtils.format(
                    frameTitlePattern,
                    frameTitlePrefix,
                    currentUi.getTitle()));
        } else {
            setTitle(frameTitlePrefix);
        }
           
        // change the panel
        contentPane.updatePanel(currentUi);
        
        // resize the frame if needed
        final int neededMinimumHeight = 
                this.getLayout().minimumLayoutSize(this).getSize().height;        
                                   
        if(isResizable() && (neededMinimumHeight > currentHeight)) {         
            setPreferredSize(new Dimension(getSize().width, 
                                neededMinimumHeight + EXTRA_SIZE));
            setMinimumSize(new Dimension(getSize().width,
                                neededMinimumHeight + EXTRA_SIZE));
            pack();             
        }                      
        contentPane.repaint();
                             
        // handle the default buttons - Enter
        
        getRootPane().setDefaultButton(currentUi.getDefaultEnterButton());
        
        // handle the default buttons - Escape
        getRootPane().getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                CANCEL_ACTION_NAME);
        getRootPane().getActionMap().put(CANCEL_ACTION_NAME, new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if(currentUi == null) {
                    return;
                }
                final NbiButton button = currentUi.getDefaultEscapeButton();
                if (button != null && button.isEnabled()) {
                    if (button.equals(getHelpButton())) {
                        currentUi.evaluateHelpButtonClick();
                    }
                    if (button.equals(getBackButton())) {
                        currentUi.evaluateBackButtonClick();
                    }
                    if (button.equals(getNextButton())) {
                        currentUi.evaluateNextButtonClick();
                    }
                    if (button.equals(getCancelButton())) {
                        currentUi.evaluateCancelButtonClick();
                    }
                }
            }
        });
        
        // set the default focus for the current page
        if (currentUi.getDefaultFocusOwner() != null) {
            currentUi.getDefaultFocusOwner().requestFocusInWindow();
        }
        
        // a11y
        getAccessibleContext().setAccessibleName(currentUi.getTitle());
        getAccessibleContext().setAccessibleDescription(currentUi.getDescription());
    }
    
    /**
     * {@inheritDoc}
     */
    public NbiButton getHelpButton() {
        return contentPane.getHelpButton();
    }
    
    /**
     * {@inheritDoc}
     */
    public NbiButton getBackButton() {
        return contentPane.getBackButton();
    }
    
    /**
     * {@inheritDoc}
     */
    public NbiButton getNextButton() {
        return contentPane.getNextButton();
    }
    
    /**
     * {@inheritDoc}
     */
    public NbiButton getCancelButton() {
        return contentPane.getCancelButton();
    }

    public void open() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    Thread.currentThread().setUncaughtExceptionHandler(
                            ErrorManager.getExceptionHandler());
                }
            });
        } catch (InvocationTargetException e) {
            ErrorManager.notifyDebug(ResourceUtils.getString(
                    SwingFrameContainer.class,
                    RESOURCE_FAILED_TO_ATTACH_ERROR_HANDLER), e);
        } catch (InterruptedException e) {
            ErrorManager.notifyDebug(ResourceUtils.getString(
                    SwingFrameContainer.class,
                    RESOURCE_FAILED_TO_ATTACH_ERROR_HANDLER), e);
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                setVisible(true);
            }
        });
    }

    public void close() {
        setVisible(false);
    }
    
    // protected ////////////////////////////////////////////////////////////////////
    /**
     * Initializes and lays out the Swing components for the container frame. This
     * method also sets some frame properties which will be required at runtime,
     * such as size, position, etc.
     */    
    private void initComponents() {
        try {
            setDefaultCloseOperation(NbiFrame.DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent event) {
                    cancelContainer();
                }
            });
            // perform some additional intiialization for Mac OS
            initializeMacOS();
        } catch (SecurityException e) {
            // we might fail here with a custom security manager (e.g. the netbeans
            // one); in this case just log the exception and "let it be" (c)
            ErrorManager.notifyDebug(
                    ResourceUtils.getString(
                    SwingFrameContainer.class,
                    RESOURCE_ERROR_SET_CLOSE_OPERATION),
                    e);
        }

        Dimension size = new Dimension(frameWidth, frameHeight);
        setSize(size);
        setPreferredSize(size);
        setMinimumSize(size);
        
        try {
            setIconImage(new ImageIcon(frameIcon.toURI().toURL()).getImage());
        } catch (MalformedURLException e) {
            ErrorManager.notifyWarning(ResourceUtils.getString(
                    SwingFrameContainer.class,
                    RESOURCE_FAILED_TO_SET_FRAME_CONTAINER_ICON), e);
        }
        
        final String resizable = System.getProperty(WIZARD_FRAME_RESIZABLE_PROPERTY);
        if(resizable!=null && (resizable.equals("false") || resizable.equals("FALSE"))) {
            setResizable(false);
        }
                 
        contentPane = new WizardFrameContentPane();
        setContentPane(contentPane);
        
        contentPane.getHelpButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(currentUi!=null) {
                    currentUi.evaluateHelpButtonClick();
                }
            }
        });
        
        contentPane.getBackButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(currentUi!=null) {
                    currentUi.evaluateBackButtonClick();
                }
            }
        });
        
        contentPane.getNextButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(currentUi!=null) {
                    currentUi.evaluateNextButtonClick();
                }
            }
        });
        
        contentPane.getCancelButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(currentUi!=null) {
                    currentUi.evaluateCancelButtonClick();
                }
            }
        });
    }
    
    private void initializeMacOS() {
        if (SystemUtils.isMacOS()) {
            final Application application = Application.getApplication();
            if(application == null) {
                // e.g. running OpenJDK port via X11 on Mac OS X
                return;
            }
            application.removeAboutMenuItem();
            application.removePreferencesMenuItem();
            application.addApplicationListener(new ApplicationAdapter() {

                @Override
                public void handleQuit(ApplicationEvent event) {
                    cancelContainer();
                }
            });
        }
    }
    private void cancelContainer() {
        if (currentUi != null) {
            if (contentPane.getCancelButton().isEnabled()) {
                currentUi.evaluateCancelButtonClick();
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    /**
     * This class is an extension of {@link NbiFrameContentPane} which adds some
     * functionality specific to the wizard container's needs. It is used as the
     * content pane for the wizard frame.
     *
     * @author Kirill Sorokin
     * @since 1.0
     */
    public static class WizardFrameContentPane extends NbiFrameContentPane {
        /**
         * {@link NbiLabel} which would be used to display the
         * {@link WizardComponent}'s title.
         */
        private NbiLabel titleLabel;
        
        /**
         * {@link NbiTextPane} which would be used to display the
         * {@link WizardComponent}'s description.
         */
        private NbiTextPane descriptionPane;
        
        /**
         * Container for the title and description components.
         */
        private NbiPanel titlePanel;
        
        /**
         * Container for the title and description images (left&right).
         */
        private NbiPanel titleDescriptionImageRightPanel;
        private NbiPanel titleDescriptionImageLeftPanel;
        
        /**
         * Separator between the wizard page header (title and description) and the
         * main wizard page contents.
         */
        private NbiSeparator topSeparator;
        
        /**
         * Separator between the wizard page footer (standard wizard container
         * buttons) and the main wizard page contents.
         */
        private NbiSeparator bottomSeparator;
        
        /**
         * The standard <code>Help</code> button.
         */
        private NbiButton helpButton;
        
        /**
         * The standard <code>Back</code> button.
         */
        private NbiButton backButton;
        
        /**
         * The standard <code>Next</code> button.
         */
        private NbiButton nextButton;
        
        /**
         * The standard <code>Cancel</code> button.
         */
        private NbiButton cancelButton;
        
        /**
         * Spacer panel which used to correctly position the standard buttons.
         */
        private NbiPanel spacerPanel;
        
        /**
         * Container for the standard buttons swing components.
         */
        private NbiPanel buttonsPanel;
        
        /**
         * Reference to the {@link SwingUi} being currently displayed.
         */
        private NbiPanel currentPanel;
        
        /**
         * Creates a new instance of {@link WizardFrameContentPane}. The default
         * constructor simply initializes and lays out the swing components
         * required by the content pane.
         */
        public WizardFrameContentPane() {
            initComponents();
        }
        
        /**
         *
         * @param panel
         */
        public void updatePanel(final SwingUi panel) {            
            if (currentPanel != null) {                
                remove(currentPanel);
            }
            currentPanel = panel;
            
            if (panel.getTitle() != null) {
                titleLabel.setText(panel.getTitle());
                descriptionPane.setText(panel.getDescription());
                
                titlePanel.setVisible(true);
                topSeparator.setVisible(true);
                
                currentPanel.setOpaque(false);
            } else {
                titlePanel.setVisible(false);
                topSeparator.setVisible(false);
                
                currentPanel.setOpaque(true);
                currentPanel.setBackground(Color.WHITE);
            }
            
            add(currentPanel, BorderLayout.CENTER);
            
            validate();                         
        }
        
        /**
         * Returns the Swing implementation of the standard <code>Help</code>
         * button. This method is called by the {@link SwingFrameContainer} when it
         * needs to get the handle of the button.
         *
         * @return <code>Help</code> button instance.
         * @see SwingFrameContainer#getHelpButton.
         */
        public NbiButton getHelpButton() {
            return helpButton;
        }
        
        /**
         * Returns the Swing implementation of the standard <code>Back</code>
         * button. This method is called by the {@link SwingFrameContainer} when it
         * needs to get the handle of the button.
         *
         * @return <code>Back</code> button instance.
         * @see SwingFrameContainer#getBackButton.
         */
        public NbiButton getBackButton() {
            return backButton;
        }
        
        /**
         * Returns the Swing implementation of the standard <code>Next</code>
         * button. This method is called by the {@link SwingFrameContainer} when it
         * needs to get the handle of the button.
         *
         * @return <code>Next</code> button instance.
         * @see SwingFrameContainer#getNextButton.
         */
        public NbiButton getNextButton() {
            return nextButton;
        }
        
        /**
         * Returns the Swing implementation of the standard <code>Cancel</code>
         * button. This method is called by the {@link SwingFrameContainer} when it
         * needs to get the handle of the button.
         *
         * @return <code>Cancel</code> button instance.
         * @see SwingFrameContainer#getCancelButton.
         */
        public NbiButton getCancelButton() {
            return cancelButton;
        }
        
        // private //////////////////////////////////////////////////////////////////
        /**
         * Initializes and lays out the swing components required by the content
         * pane.
         */
        private void initComponents() {
            // titleLabel ///////////////////////////////////////////////////////////
            titleLabel = new NbiLabel();           
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
            
            // descriptionPane //////////////////////////////////////////////////////
            descriptionPane = new NbiTextPane();

            NbiPanel titleDescriptionPanel = new NbiPanel();
            titleDescriptionPanel.setLayout(new GridBagLayout());
            titleDescriptionPanel.setOpaque(true);
            
            // titlePanel ///////////////////////////////////////////////////////////
            titlePanel = new NbiPanel();
            
            titlePanel.setLayout(new GridBagLayout());
            titlePanel.setOpaque(true);
            titlePanel.setBackground(Color.WHITE);
            titleDescriptionPanel.setBackground(Color.WHITE);

            final String backgroundImageUri = System.getProperty(WIZARD_FRAME_HEAD_BACKGROUND_IMAGE_URI_PROPERTY);
            if(backgroundImageUri != null) {
                titleDescriptionPanel.setBackgroundImage(backgroundImageUri, NbiPanel.ANCHOR_TOP);
            } else {
                titleDescriptionPanel.setBackground(Color.WHITE);
            }

            final String leftImageUri = System.getProperty(WIZARD_FRAME_HEAD_LEFT_IMAGE_URI_PROPERTY);
            int titlePanelDx = 0;
            if(leftImageUri!=null) {
                titleDescriptionImageLeftPanel  = new NbiPanel();
                titleDescriptionImageLeftPanel .setBackgroundImage(leftImageUri , NbiPanel.ANCHOR_TOP_RIGHT);
                final ImageIcon icon = titleDescriptionImageLeftPanel .getBackgroundImage(NbiPanel.ANCHOR_TOP_RIGHT);
                titleDescriptionImageLeftPanel .setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
                titleDescriptionImageLeftPanel .setPreferredSize(new Dimension(icon.getIconWidth(),icon.getIconHeight()));
                titleDescriptionImageLeftPanel .setMaximumSize(new Dimension(icon.getIconWidth(),icon.getIconHeight()));
                titleDescriptionImageLeftPanel .setMinimumSize(new Dimension(icon.getIconWidth(),0));
                titleDescriptionImageLeftPanel .setSize(new Dimension(icon.getIconWidth(),icon.getIconHeight()));
                titleDescriptionImageLeftPanel .setOpaque(false);
                titlePanel.add(titleDescriptionImageLeftPanel , new GridBagConstraints(
                        titlePanelDx++, 0,                             // x, y
                        1, 2,                             // width, height
                        0.0, 0.0,                         // weight-x, weight-y
                        GridBagConstraints.NORTH,    // anchor
                        GridBagConstraints.BOTH,          // fill
                        new Insets(0, 0, 0, 0),        // padding
                        0, 0));                           // padx, pady - ???
            }
            
            final String rightImageUri = System.getProperty(WIZARD_FRAME_HEAD_RIGHT_IMAGE_URI_PROPERTY);
            if(rightImageUri!=null) {
                titleDescriptionImageRightPanel = new NbiPanel();
                titleDescriptionImageRightPanel.setBackgroundImage(rightImageUri , NbiPanel.ANCHOR_TOP_RIGHT);
                final ImageIcon icon = titleDescriptionImageRightPanel.getBackgroundImage(NbiPanel.ANCHOR_TOP_RIGHT);
                titleDescriptionImageRightPanel.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
                titleDescriptionImageRightPanel.setPreferredSize(new Dimension(icon.getIconWidth(),icon.getIconHeight()));
                titleDescriptionImageRightPanel.setMaximumSize(new Dimension(icon.getIconWidth(),icon.getIconHeight()));
                titleDescriptionImageRightPanel.setMinimumSize(new Dimension(icon.getIconWidth(),0));
                titleDescriptionImageRightPanel.setSize(new Dimension(icon.getIconWidth(),icon.getIconHeight()));
                titleDescriptionImageRightPanel.setOpaque(false);
                titlePanel.add(titleDescriptionImageRightPanel, new GridBagConstraints(
                        titlePanelDx + 1, 0,                             // x, y
                        1, 2,                             // width, height
                        0.0, 0.0,                         // weight-x, weight-y
                        GridBagConstraints.NORTH,    // anchor
                        GridBagConstraints.BOTH,          // fill
                        new Insets(0, 0, 0, 0),        // padding
                        0, 0));                           // padx, pady - ???
            }
            
            // topSeparator /////////////////////////////////////////////////////////
            topSeparator = new NbiSeparator();
            if (SystemUtils.isMacOS()) {
                // JSeparator`s height on Aqua L&F equals to 12px which is too much in SwingFrameContainer
                // thus we descrease it to 7px
                // TODO: possibly move this code to NbiSeparator later
                Dimension d = topSeparator.getPreferredSize();
                if (d != null && d.getHeight() == 12) {
                    d.setSize(d.getWidth(), 7);
                    topSeparator.setPreferredSize(d);
                }
            }

            titleDescriptionPanel.add(titleLabel, new GridBagConstraints(
                    0 , 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            titleDescriptionPanel.add(descriptionPane, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(6, 22, 4, 11),        // padding
                    0, 0));                           // padx, pady - ???

            titlePanel.add(titleDescriptionPanel, new GridBagConstraints(
                    titlePanelDx , 0,                             // x, y
                    1, 2,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),        // padding
                    0, 0));                           // padx, pady - ???
            
            titlePanel.add(topSeparator, new GridBagConstraints(
                    0, 2,                             // x, y
                    2 + titlePanelDx, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            
            // bottomSeparator //////////////////////////////////////////////////////
            bottomSeparator = new NbiSeparator();
            if (SystemUtils.isMacOS()) {
                // JSeparator`s height on Aqua L&F equals to 12px which is too much in SwingFrameContainer
                // thus we descrease it to 7px
                // TODO: possibly move this code to NbiSeparator later
                Dimension d = topSeparator.getPreferredSize();
                if (d != null && d.getHeight() == 12) {
                    d.setSize(d.getWidth(), 7);
                    topSeparator.setPreferredSize(d);
                }
            }
            // helpButton ///////////////////////////////////////////////////////////
            helpButton = new NbiButton();
            
            // backButton ///////////////////////////////////////////////////////////
            backButton = new NbiButton();
            
            // nextButton ///////////////////////////////////////////////////////////
            nextButton = new NbiButton();
            
            // cancelButton /////////////////////////////////////////////////////////
            cancelButton = new NbiButton();
            
            // spacerPanel //////////////////////////////////////////////////////////
            spacerPanel = new NbiPanel();
            
            // buttonsPanel /////////////////////////////////////////////////////////
            buttonsPanel = new NbiPanel();
            
            buttonsPanel.add(bottomSeparator, new GridBagConstraints(
                    0, 0,                             // x, y
                    5, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            buttonsPanel.add(helpButton, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(11, 11, 11, 11),       // padding
                    0, 0));                           // padx, pady - ???
            buttonsPanel.add(spacerPanel, new GridBagConstraints(
                    1, 1,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            buttonsPanel.add(backButton, new GridBagConstraints(
                    2, 1,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(11, 0, 11, 6),         // padding
                    0, 0));                           // padx, pady - ???
            buttonsPanel.add(nextButton, new GridBagConstraints(
                    3, 1,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(11, 0, 11, 11),        // padding
                    0, 0));                           // padx, pady - ???
            buttonsPanel.add(cancelButton, new GridBagConstraints(
                    4, 1,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(11, 0, 11, 11),        // padding
                    0, 0));                           // padx, pady - ???
            
            // currentPanel /////////////////////////////////////////////////////////
            currentPanel = new NbiPanel();
            
            // this /////////////////////////////////////////////////////////////////
            setLayout(new BorderLayout());
            
            add(titlePanel, BorderLayout.PAGE_START);
            add(currentPanel, BorderLayout.CENTER);
            add(buttonsPanel, BorderLayout.PAGE_END);
            
            // debugging plug ///////////////////////////////////////////////////////
            //KeyboardFocusManager.getCurrentKeyboardFocusManager().
            //        addPropertyChangeListener(new PropertyChangeListener() {
            //    public void propertyChange(PropertyChangeEvent event) {
            //        if (event.getPropertyName().equals("focusOwner")) {
            //            if (event.getNewValue() != null) {
            //                System.out.println(event.getNewValue());
            //            }
            //        }
            //    }
            //});
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * Name of the system property which is expected to contain the desired value
     * for the initial width of the wizard frame.
     */
    public static final String WIZARD_FRAME_WIDTH_PROPERTY =
            "nbi.wizard.ui.swing.frame.width"; // NOI18N
    
    /**
     * Name of the system property which is expected to contain the desired value
     * for the minimum width of the wizard frame.
     */
    public static final String WIZARD_FRAME_MINIMUM_WIDTH_PROPERTY =
            "nbi.wizard.ui.swing.frame.minimum.width"; // NOI18N
    
    /**
     * Name of the system property which is expected to contain the desired value
     * for the maximum width of the wizard frame.
     */
    public static final String WIZARD_FRAME_MAXIMUM_WIDTH_PROPERTY =
            "nbi.wizard.ui.swing.frame.maximum.width"; // NOI18N
    
    /**
     * Name of the system property which is expected to contain the desired value
     * for the initial height of the wizard frame.
     */
    public static final String WIZARD_FRAME_HEIGHT_PROPERTY =
            "nbi.wizard.ui.swing.frame.height"; // NOI18N
    
    /**
     * Name of the system property which is expected to contain the desired value
     * for the minimum height of the wizard frame.
     */
    public static final String WIZARD_FRAME_MINIMUM_HEIGHT_PROPERTY =
            "nbi.wizard.ui.swing.frame.minimum.height"; // NOI18N
    
    /**
     * Name of the system property which is expected to contain the desired value
     * for the maximum height of the wizard frame.
     */
    public static final String WIZARD_FRAME_MAXIMUM_HEIGHT_PROPERTY =
            "nbi.wizard.ui.swing.frame.maximum.height"; // NOI18N
    
    /**
     * Name of the system property which is expected to contain the desired value
     * for the URI of the wizard frame icon.
     */
    public static final String WIZARD_FRAME_ICON_URI_PROPERTY =
            "nbi.wizard.ui.swing.frame.icon"; // NOI18N
    
    /**
     * Name of the system property which is expected to contain the desired value
     * for the standard prefix of the wizard frame's title.
     */
    public static final String WIZARD_FRAME_TITLE_PREFIX_PROPERTY =
            "nbi.wizard.ui.swing.frame.title.prefix"; // NOI18N
    
    /**
     * Name of the system property which is expected to contain the desired value
     * for the URI of the wizard frame head background image (right side).
     */
    public static final String WIZARD_FRAME_HEAD_RIGHT_IMAGE_URI_PROPERTY =
            "nbi.wizard.ui.swing.frame.head.right.image"; // NOI18N
    /**
     * Name of the system property which is expected to contain the desired value
     * for the URI of the wizard frame head background image (left side).
     */
    public static final String WIZARD_FRAME_HEAD_LEFT_IMAGE_URI_PROPERTY =
            "nbi.wizard.ui.swing.frame.head.left.image"; // NOI18N

    public static final String WIZARD_FRAME_HEAD_BACKGROUND_IMAGE_URI_PROPERTY =
            "nbi.wizard.ui.swing.frame.head.background.image"; // NOI18N
    
    /**
     * Name of the system property which is expected to contain the desired value
     * for the pattern for merging the standard title prefix with the component's
     * title.
     */
    public static final String WIZARD_FRAME_TITLE_PATTERN_PROPERTY =
            "nbi.wizard.ui.swing.frame.title.pattern"; // NOI18N

     /**
     * Name of the system property which is expected to contain the desired value
     * for the making the wizard window be resizable.
     * <br>If this property is not set at all or set to any string different from
     * "false" and "FALSE" then the wizard is resiazable.
     */
    public static final String WIZARD_FRAME_RESIZABLE_PROPERTY =
            "nbi.wizard.ui.swing.frame.resizable"; // NOI18N
              
    /**
     * Default value for the wizard frame's initial width.
     */
    public static final int DEFAULT_WIZARD_FRAME_WIDTH =
            NbiFrame.DEFAULT_FRAME_WIDTH;
    
    /**
     * Default value for the wizard frame's minimum width.
     */
    public static final int DEFAULT_WIZARD_FRAME_MINIMUM_WIDTH =
            NbiFrame.DEFAULT_FRAME_MINIMUM_WIDTH;
    
    /**
     * Default value for the wizard frame's maximum width.
     */
    public static final int DEFAULT_WIZARD_FRAME_MAXIMUM_WIDTH =
            NbiFrame.DEFAULT_FRAME_MAXIMUM_WIDTH;
    
    /**
     * Default value for the wizard frame's initial height.
     */
    public static final int DEFAULT_WIZARD_FRAME_HEIGHT =
            NbiFrame.DEFAULT_FRAME_HEIGHT;
    
    /**
     * Default value for the wizard frame's minimum height.
     */
    public static final int DEFAULT_WIZARD_FRAME_MINIMUM_HEIGHT =
            NbiFrame.DEFAULT_FRAME_MINIMUM_WIDTH;
    
    /**
     * Default value for the wizard frame's maximum height.
     */
    public static final int DEFAULT_WIZARD_FRAME_MAXIMUM_HEIGHT =
            NbiFrame.DEFAULT_FRAME_MAXIMUM_HEIGHT;
    
    /**
     * Default value for the wizard frame's icon's URI.
     */
    public static final String DEFAULT_WIZARD_FRAME_ICON_URI =
            NbiFrame.DEFAULT_FRAME_ICON_URI;
      
    /**
     * Default value for the wizard frame's standard title prefix.
     */
    public static final String DEFAULT_WIZARD_FRAME_TITLE_PREFIX =
            ResourceUtils.getString(SwingFrameContainer.class,
            "SFC.frame.title.prefix"); // NOI18N
    
    /**
     * Default value for the pattern for merging the standard title prefix with the
     * component's title.
     */
    public static final String DEFAULT_WIZARD_FRAME_TITLE_PATTERN =
            ResourceUtils.getString(SwingFrameContainer.class,
            "SFC.frame.title.pattern"); // NOI18N
    
    // private //////////////////////////////////////////////////////////////////////
    /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_FAILED_TO_DOWNLOAD_WIZARD_ICON =
            "SFC.error.failed.to.download.icon"; // NOI18N
    private static final String RESOURCE_ERROR_SET_CLOSE_OPERATION =
            "SFC.error.close.operation"; //NOI18N
    private static final String RESOURCE_FAILED_TO_SET_FRAME_CONTAINER_ICON =
            "SFC.error.failed.to.set.icon";//NOI18N
    /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_FAILED_TO_ATTACH_ERROR_HANDLER =
            "SFC.error.failed.to.attach.error.handler"; // NOI18N
    
    /**
     * Name of the {@link AbstractAction} which is invoked when the user presses the
     * <code>Escape</code> button.
     */
    private static final String CANCEL_ACTION_NAME =
            "evaluate.cancel"; // NOI18N
    
    private static final int EXTRA_SIZE = 15; 
}

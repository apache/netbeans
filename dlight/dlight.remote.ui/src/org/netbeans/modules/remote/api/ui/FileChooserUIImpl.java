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
package org.netbeans.modules.remote.api.ui;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.AccessControlException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import static javax.swing.JFileChooser.DIRECTORY_CHANGED_PROPERTY;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicFileChooserUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.dlight.libs.common.DLightLibsCommonLogger;
import org.netbeans.modules.remote.api.ui.FileChooserBuilder.JFileChooserEx;
import org.netbeans.modules.remote.ui.support.RemoteLogger;
import org.openide.awt.HtmlRenderer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

// NOTE:
// Dependencies with internal JDK classes below have been commented out
// in order to ensure builds work well with off-the-shelp Java 11 java.awt.desktop module
// import sun.awt.shell.ShellFolder;
// import sun.swing.FilePane;

/**
 * An implementation of a customized filechooser.
 *
 */
final class FileChooserUIImpl extends BasicFileChooserUI{

    static final String USE_SHELL_FOLDER = "FileChooser.useShellFolder";//NOI18N
    static final String NB_USE_SHELL_FOLDER = "nb.FileChooser.useShellFolder";//NOI18N
    static final String START_TIME = "cnd.start.time";//NOI18N


    private static final String DIALOG_IS_CLOSING = "JFileChooserDialogIsClosingProperty";//NOI18N
    private static final Dimension horizontalStrut1 = new Dimension(25, 1);
    private static final Dimension verticalStrut1  = new Dimension(1, 4);
    private static final Dimension verticalStrut2  = new Dimension(1, 6);
    private static final Dimension verticalStrut3  = new Dimension(1, 8);
    private static Dimension PREF_SIZE = new Dimension(425, 245);
    private static Dimension MIN_SIZE = new Dimension(425, 245);
    private static Dimension TREE_PREF_SIZE = new Dimension(380, 230);
    private static final int ACCESSORY_WIDTH = 250;

    private static final Logger LOG = Logger.getLogger(FileChooserUIImpl.class.getName());

    private static final RequestProcessor COMMON_RP = new RequestProcessor("Cnd File Chooser Common Worker", 16); // NOI18N
    private static final RequestProcessor UPDATE_RP = new RequestProcessor("Cnd File Chooser Update Worker"); // NOI18N
    private static final RequestProcessor APPROVE_RP = new RequestProcessor("Cnd File Chooser Update Worker"); // NOI18N

    private static final String TIMEOUT_KEY="nb.fileChooser.timeout"; // NOI18N

    private JPanel centerPanel;

    private JLabel lookInComboBoxLabel;

    private JComboBox directoryComboBox;

    private DirectoryComboBoxModel directoryComboBoxModel;

    private ActionListener directoryComboBoxAction = new DirectoryComboBoxAction();

    private FilterTypeComboBoxModel filterTypeComboBoxModel;

    private JTextField filenameTextField;

    private JComponent placesBar;
    private boolean placesBarFailed = false;

    private JButton approveButton;
    private JButton cancelButton;

    private JPanel buttonPanel;
    private JPanel bottomPanel;

    private JComboBox filterTypeComboBox;

    private int lookInLabelMnemonic = 0;
    private String lookInLabelText = null;
    private String saveInLabelText = null;

    private int fileNameLabelMnemonic = 0;
    private String fileNameLabelText = null;

    private int filesOfTypeLabelMnemonic = 0;
    private String filesOfTypeLabelText = null;

    private String upFolderToolTipText = null;
    private String upFolderAccessibleName = null;

    private String newFolderToolTipText = null;
    private String newFolderAccessibleName = null;

    private String homeFolderTooltipText = null;
    private String homeFolderAccessibleName = null;

    private final NewDirectoryAction newFolderAction = new NewDirectoryAction();

    private BasicFileView fileView = new DirectoryChooserFileView();

    private JTree tree;

    private DirectoryTreeModel model;

    private LoadingTreeModel loadingModel;

    private FileNode newFolderNode;

    private JComponent treeViewPanel;

    //private InputBlocker blocker;

    private JFileChooserEx fileChooser;

    private final AtomicBoolean changeDirectory = new AtomicBoolean(true);

    private boolean showPopupCompletion = false;

    private volatile boolean addNewDirectory = false;

    private JPopupMenu popupMenu;

    private FileCompletionPopup completionPopup;

    private final UpdateWorker updateWorker = new UpdateWorker();

    private volatile ValidationWorkerCheckState currentState = new ValidationWorkerCheckState(Boolean.TRUE,
            new ValidationResult(Boolean.FALSE, null, false, null));//NOI18N

    private boolean useShellFolder = false;

    private JButton upFolderButton;
    private JButton newFolderButton;
    /** can be null */
    private JButton homeButton;
    private JComponent topCombo, topComboWrapper, topToolbar;
    private JPanel slownessPanel;

    private final ListFilesWorker listFilesWorker = new ListFilesWorker();
    private final RequestProcessor.Task listFilesTask = UPDATE_RP.create(listFilesWorker);
    private volatile File curDir;

    private final Action approveSelectionAction;
    private Action changeToParentDirectoryAction;
    private final Action cancelSelectionAction;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    private FileFilter actualFileFilter = null;
    private GlobFilter globFilter = null;

    private final char fileSeparatorChar;

    public FileChooserUIImpl(FileChooserBuilder.JFileChooserEx filechooser) {
        super(filechooser);
        approveSelectionAction = new ApproveSelectionAction();
        changeToParentDirectoryAction = new ChangeToParentDirectoryAction();
        cancelSelectionAction = new CancelSelectionAction();
        fileSeparatorChar = filechooser.getFileSeparatorChar();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        fileChooser = (JFileChooserEx) c;
    }

    @Override
    public void uninstallComponents(JFileChooser fc) {
        fc.removeAll();
        super.uninstallComponents(fc);
    }

    @Override
    public void installComponents(JFileChooser fc) {
        fileChooser = (JFileChooserEx)fc;

        fc.setFocusCycleRoot(true);
        fc.setBorder(new EmptyBorder(4, 10, 10, 10));
        fc.setLayout(new BorderLayout(8, 8));

        updateUseShellFolder();
        createCenterPanel(fc);
        fc.add(centerPanel, BorderLayout.CENTER);

        if (fc.isMultiSelectionEnabled()) {
            setFileName(getStringOfFileNames(fc.getSelectedFiles()));
        } else {
            setFileName(getStringOfFileName(fc.getSelectedFile(), true));
        }

        if(fc.getControlButtonsAreShown()) {
            addControlButtons();
        }

        createPopup();

        initUpdateWorker();
    }

    @Override
    public String getDialogTitle(JFileChooser fc) {
        String title = super.getDialogTitle(fc);
        fc.getAccessibleContext().setAccessibleDescription(title);
        return title;
    }

    private void updateUseShellFolder() {
        // Decide whether to use the ShellFolder class to populate shortcut
        // panel and combobox.

        Boolean prop =
                (Boolean)fileChooser.getClientProperty(USE_SHELL_FOLDER);
        if (prop != null) {
            useShellFolder = prop.booleanValue();
        } else {
            // See if FileSystemView.getRoots() returns the desktop folder,
            // i.e. the normal Windows hierarchy.
            useShellFolder = false;
            File[] roots = fileChooser.getFileSystemView().getRoots();
            if (roots != null && roots.length == 1) {
                File[] cbFolders = getShellFolderRoots();
                if (cbFolders != null && cbFolders.length > 0 && Arrays.asList(cbFolders).contains(roots[0])) {
                    useShellFolder = true;
                }
            }
        }

        if (Utilities.isWindows()) {
            if (useShellFolder) {
                if (placesBar == null) {
                    placesBar = getPlacesBar();
                }
                if (placesBar != null) {
                    fileChooser.add(placesBar, BorderLayout.BEFORE_LINE_BEGINS);
                    if (placesBar instanceof PropertyChangeListener) {
                        fileChooser.addPropertyChangeListener((PropertyChangeListener)placesBar);
                    }
                }
            } else {
                if (placesBar != null) {
                    fileChooser.remove(placesBar);
                    if (placesBar instanceof PropertyChangeListener) {
                        fileChooser.removePropertyChangeListener((PropertyChangeListener)placesBar);
                    }
                    placesBar = null;
                }
            }
        }
    }

    /** Returns instance of WindowsPlacesBar class or null in case of failure
     */
    private JComponent getPlacesBar () {
        if (placesBarFailed) {
            return null;
        }
        try {
            Class<?> clazz = Class.forName("sun.swing.WindowsPlacesBar");//NOI18N
            Class[] params = new Class[] { JFileChooser.class, Boolean.TYPE };
            Constructor<?> constr = clazz.getConstructor(params);
            return (JComponent)constr.newInstance(fileChooser, isXPStyle().booleanValue());
        } catch (Exception exc) {
            // reflection not succesfull, just log the exception and return null
            Logger.getLogger(FileChooserUIImpl.class.getName()).log(
                    Level.FINE, "WindowsPlacesBar class can't be instantiated.", exc);//NOI18N
            placesBarFailed = true;
            return null;
        }
    }

    /** Reflection alternative of
     * sun.awt.shell.ShellFolder.getShellFolder(file)
     */
    private File getShellFolderForFile (File file) {
        try {
            Class<?> clazz = Class.forName("sun.awt.shell.ShellFolder");//NOI18N
            return (File) clazz.getMethod("getShellFolder", File.class).invoke(null, file);//NOI18N
        } catch (Exception exc) {
            // reflection not succesfull, just log the exception and return null
            Logger.getLogger(FileChooserUIImpl.class.getName()).log(
                    Level.FINE, "ShellFolder can't be used.", exc);//NOI18N
            return null;
        }
    }

    /** Reflection alternative of
     * sun.awt.shell.ShellFolder.getShellFolder(dir).getLinkLocation()
     */
    private File getShellFolderForFileLinkLoc (File file) {
        try {
            Class<?> clazz = Class.forName("sun.awt.shell.ShellFolder");//NOI18N
            Object sf = clazz.getMethod("getShellFolder", File.class).invoke(null, file);//NOI18N
            return (File) clazz.getMethod("getLinkLocation").invoke(sf);//NOI18N
        } catch (Exception exc) {
            // reflection not succesfull, just log the exception and return null
            Logger.getLogger(FileChooserUIImpl.class.getName()).log(
                    Level.FINE, "ShellFolder can't be used.", exc);//NOI18N
            return null;
        }

    }

    /** Reflection alternative of
     * sun.awt.shell.ShellFolder.get("fileChooserComboBoxFolders")
     */
    private File[] getShellFolderRoots () {
        try {
            Class<?> clazz = Class.forName("sun.awt.shell.ShellFolder");//NOI18N
            return (File[]) clazz.getMethod("get", String.class).invoke(null, "fileChooserComboBoxFolders");//NOI18N
        } catch (Exception exc) {
            // reflection not succesfull, just log the exception and return null
            Logger.getLogger(FileChooserUIImpl.class.getName()).log(
                    Level.FINE, "ShellFolder can't be used.", exc);//NOI18N
            return null;
        }
    }

    private void createBottomPanel(JFileChooser fc) {
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.PAGE_AXIS));
        labelPanel.add(Box.createRigidArea(verticalStrut1));

        JLabel fnl = new JLabel(fileNameLabelText);
        fnl.setDisplayedMnemonic(fileNameLabelMnemonic);
        fnl.setAlignmentY(0);
        labelPanel.add(fnl);

        labelPanel.add(Box.createRigidArea(new Dimension(1,12)));

        JLabel ftl = new JLabel(filesOfTypeLabelText);
        ftl.setDisplayedMnemonic(filesOfTypeLabelMnemonic);
        labelPanel.add(ftl);

        bottomPanel.add(labelPanel);
        bottomPanel.add(Box.createRigidArea(new Dimension(15, 0)));

        JPanel fileAndFilterPanel = new JPanel();
        fileAndFilterPanel.add(Box.createRigidArea(verticalStrut3));
        fileAndFilterPanel.setLayout(new BoxLayout(fileAndFilterPanel, BoxLayout.Y_AXIS));

        filenameTextField = new JTextField(24) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Short.MAX_VALUE, super.getPreferredSize().height);
            }
        };

        filenameTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateCompletions();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateCompletions();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {}
        });

        filenameTextField.addKeyListener(new TextFieldKeyListener());
        filenameTextField.addKeyListener(new AltUpHandler(filenameTextField));

        fnl.setLabelFor(filenameTextField);
        filenameTextField.addFocusListener(
                new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (!getFileChooser().isMultiSelectionEnabled()) {
                    tree.clearSelection();
                }
            }
        });

        // disable TAB focus transfer, we need it for completion
        Set<AWTKeyStroke> tKeys = filenameTextField.getFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        Set<AWTKeyStroke> newTKeys = new HashSet<AWTKeyStroke>(tKeys);
        newTKeys.remove(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0));
        // #107305: enable at least Ctrl+TAB if we have TAB for completion
        newTKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, KeyEvent.CTRL_DOWN_MASK));
        filenameTextField.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, newTKeys);

        fileAndFilterPanel.add(filenameTextField);
        fileAndFilterPanel.add(Box.createRigidArea(verticalStrut3));

        filterTypeComboBoxModel = createFilterComboBoxModel();
        fc.addPropertyChangeListener(filterTypeComboBoxModel);
        filterTypeComboBox = new JComboBox(filterTypeComboBoxModel);
        ftl.setLabelFor(filterTypeComboBox);
        filterTypeComboBox.setRenderer(createFilterComboBoxRenderer());
        fileAndFilterPanel.add(filterTypeComboBox);

        bottomPanel.add(fileAndFilterPanel);
        bottomPanel.add(Box.createRigidArea(horizontalStrut1));
        createButtonsPanel(fc);
    }

    private void createButtonsPanel(JFileChooser fc) {
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        approveButton = new JButton(getApproveButtonText(fc)) {
            @Override
            public Dimension getMaximumSize() {
                return approveButton.getPreferredSize().width > cancelButton.getPreferredSize().width ?
                    approveButton.getPreferredSize() : cancelButton.getPreferredSize();
            }
        };
        // #107791: No mnemonics desirable on Mac
        if (!Utilities.isMac()) {
            approveButton.setMnemonic(getApproveButtonMnemonic(fc));
        }
        approveButton.addActionListener(getApproveSelectionAction());
        approveButton.setToolTipText(getApproveButtonToolTipText(fc));
        buttonPanel.add(Box.createRigidArea(verticalStrut1));
        buttonPanel.add(approveButton);
        buttonPanel.add(Box.createRigidArea(verticalStrut2));

        cancelButton = new JButton(cancelButtonText) {
            @Override
            public Dimension getMaximumSize() {
                return approveButton.getPreferredSize().width > cancelButton.getPreferredSize().width ?
                    approveButton.getPreferredSize() : cancelButton.getPreferredSize();
            }
        };
        // #107791: No mnemonics desirable on Mac
        if (!Utilities.isMac()) {
            cancelButton.setMnemonic(cancelButtonMnemonic);
        }
        cancelButton.setToolTipText(cancelButtonToolTipText);
        cancelButton.addActionListener(getCancelSelectionAction());
        buttonPanel.add(cancelButton);
    }

    private void createCenterPanel(final JFileChooser fc) {
        centerPanel = new JPanel(new BorderLayout());
        treeViewPanel = createTree();
        treeViewPanel.setPreferredSize(TREE_PREF_SIZE);
        JPanel treePanel = new JPanel();
        treePanel.setLayout(new BorderLayout());
        JComponent accessory = fc.getAccessory();
        topToolbar = createTopToolbar();
        topCombo = createTopCombo(fc);
        topComboWrapper = new JPanel(new BorderLayout());
        topComboWrapper.add(topCombo, BorderLayout.CENTER);
        if (accessory == null) {
            topComboWrapper.add(topToolbar, BorderLayout.EAST);
        }
        treePanel.add(topComboWrapper, BorderLayout.NORTH);
        treePanel.add(treeViewPanel, BorderLayout.CENTER);
        centerPanel.add(treePanel, BorderLayout.CENTER);
        // control width of accessory panel, don't allow to jump (change width)
        JPanel wrapAccessory = new JPanel() {
            private Dimension prefSize = new Dimension(ACCESSORY_WIDTH, 0);
            private Dimension minSize = new Dimension(ACCESSORY_WIDTH, 0);

            @Override
            public Dimension getMinimumSize () {
                if (fc.getAccessory() != null) {
                    minSize.height = getAccessoryPanel().getMinimumSize().height;
                    return minSize;
                }
                return super.getMinimumSize();
            }
            @Override
            public Dimension getPreferredSize () {
                if (fc.getAccessory() != null) {
                    Dimension origPref = getAccessoryPanel().getPreferredSize();
                    LOG.fine("AccessoryWrapper.getPreferredSize: orig pref size: " + origPref);//NOI18N

                    prefSize.height = origPref.height;

                    prefSize.width = Math.max(prefSize.width, origPref.width);
                    int centerW = centerPanel.getWidth();
                    if (centerW != 0 && prefSize.width > centerW / 2) {
                        prefSize.width = centerW / 2;
                    }
                    LOG.fine("AccessoryWrapper.getPreferredSize: resulting pref size: " + prefSize);//NOI18N

                    return prefSize;
                }
                return super.getPreferredSize();
            }
        };
        wrapAccessory.setLayout(new BorderLayout());
        JPanel accessoryPanel = getAccessoryPanel();
        if (accessory != null) {
            accessoryPanel.add(topToolbar, BorderLayout.NORTH);
            accessoryPanel.add(accessory, BorderLayout.CENTER);
        }
        wrapAccessory.add(accessoryPanel, BorderLayout.CENTER);
        centerPanel.add(wrapAccessory, BorderLayout.EAST);
        createBottomPanel(fc);
        centerPanel.add(bottomPanel, BorderLayout.SOUTH);
    }



    private JComponent createTopCombo(JFileChooser fc) {
        JPanel panel = new JPanel();
        if (fc.getAccessory() != null) {
            panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        } else {
            panel.setBorder(BorderFactory.createEmptyBorder(6, 0, 10, 0));
        }
        panel.setLayout(new BorderLayout());

        Box labelBox = Box.createHorizontalBox();

        lookInComboBoxLabel = new JLabel(lookInLabelText);
        lookInComboBoxLabel.setDisplayedMnemonic(lookInLabelMnemonic);
        lookInComboBoxLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        lookInComboBoxLabel.setAlignmentY(JComponent.CENTER_ALIGNMENT);

        labelBox.add(lookInComboBoxLabel);
        labelBox.add(Box.createRigidArea(new Dimension(9,0)));

        // fixed #97525, made the height of the
        // combo box bigger.
        directoryComboBox = new JComboBox() {
            @Override
            public Dimension getMinimumSize() {
                Dimension d = super.getMinimumSize();
                d.width = 60;
                return d;
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                // Must be small enough to not affect total width and height.
                d.height = 24;
                d.width = 150;
                return d;
            }
        };
        directoryComboBox.putClientProperty( "JComboBox.lightweightKeyboardNavigation", "Lightweight" );//NOI18N
        lookInComboBoxLabel.setLabelFor(directoryComboBox);
        directoryComboBoxModel = createDirectoryComboBoxModel(fc);
        directoryComboBox.setModel(directoryComboBoxModel);
        directoryComboBox.addActionListener(directoryComboBoxAction);
        directoryComboBox.setRenderer(createDirectoryComboBoxRenderer(fc));
        directoryComboBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        directoryComboBox.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        directoryComboBox.setMaximumRowCount(8);

        panel.add(labelBox, BorderLayout.WEST);
        panel.add(directoryComboBox, BorderLayout.CENTER);

        return panel;
    }

    private JComponent createTopToolbar() {
        JToolBar topPanel = new JToolBar();
        topPanel.setFloatable(false);

        if (Utilities.isWindows()) {
            topPanel.putClientProperty("JToolBar.isRollover", Boolean.TRUE);//NOI18N
        }

        upFolderButton = new JButton(getChangeToParentDirectoryAction());
        upFolderButton.setText(null);
        // fixed bug #97049
        final boolean isMac = Utilities.isMac();
        Icon upOneLevelIcon = null;
        if (!isMac) {
            upOneLevelIcon = UIManager.getIcon("FileChooser.upFolderIcon");//NOI18N
        }
        // on Mac all icons from UIManager are the same, some default, so load our own.
        // it's also fallback if icon from UIManager not found, may happen
        if (isMac || upOneLevelIcon == null || jdkBug6840086Workaround() ) {
            if (isMac) {
                upOneLevelIcon = ImageUtilities.loadImageIcon("org/netbeans/swing/dirchooser/resources/upFolderIcon_mac.png", false);//NOI18N
            } else {
                upOneLevelIcon = ImageUtilities.loadImageIcon("org/netbeans/swing/dirchooser/resources/upFolderIcon.gif", false);//NOI18N
            }
        }
        upFolderButton.setIcon(upOneLevelIcon);
        upFolderButton.setToolTipText(upFolderToolTipText);
        upFolderButton.getAccessibleContext().setAccessibleName(upFolderAccessibleName);
        upFolderButton.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        upFolderButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);

        if(useShellFolder) {
            upFolderButton.setFocusPainted(false);
        }

        topPanel.add(upFolderButton);
        topPanel.add(Box.createRigidArea(new Dimension(2, 0)));

        // no home on Win platform
        if (!Utilities.isWindows()) {
            homeButton = new JButton(getGoHomeAction());
            Icon homeIcon = null;
            if (!isMac) {
                homeIcon = UIManager.getIcon("FileChooser.homeFolderIcon");//NOI18N
            }
            if (isMac || homeIcon == null) {
                if (isMac) {
                    homeIcon = ImageUtilities.loadImageIcon("org/netbeans/swing/dirchooser/resources/homeIcon_mac.png", false);//NOI18N
                } else {
                    homeIcon = ImageUtilities.loadImageIcon("org/netbeans/swing/dirchooser/resources/homeIcon.gif", false);//NOI18N
                }
            }
            homeButton.setIcon(homeIcon);
            homeButton.setText(null);

            String tooltip = homeButton.getToolTipText();
            if (tooltip == null) {
                tooltip = homeFolderTooltipText;
                if (tooltip == null) {
                    tooltip = NbBundle.getMessage(FileChooserUIImpl.class,
                            "TLTP_HomeFolder");//NOI18N
                }
                homeButton.setToolTipText( tooltip );
            }
            if( null != homeFolderAccessibleName )
                homeButton.getAccessibleContext().setAccessibleName(homeFolderAccessibleName);
            topPanel.add(homeButton);
        }

        newFolderButton = new JButton(newFolderAction);
        newFolderButton.setText(null);
        // fixed bug #97049
        Icon newFoldIcon = null;
        if (!isMac) {
            newFoldIcon = UIManager.getIcon("FileChooser.newFolderIcon");//NOI18N
        }
        // on Mac all icons from UIManager are the same, some default, so load our own.
        // it's also fallback if icon from UIManager not found, may happen
        if (isMac || newFoldIcon == null || jdkBug6840086Workaround()) {
            if (isMac) {
                newFoldIcon = ImageUtilities.loadImageIcon("org/netbeans/swing/dirchooser/resources/newFolderIcon_mac.png", false);//NOI18N
            } else {
                newFoldIcon = ImageUtilities.loadImageIcon("org/netbeans/swing/dirchooser/resources/newFolderIcon.gif", false);//NOI18N
            }
        }
        newFolderButton.setIcon(newFoldIcon);
        newFolderButton.setToolTipText(newFolderToolTipText);
        newFolderButton.getAccessibleContext().setAccessibleName(newFolderAccessibleName);
        newFolderButton.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        newFolderButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);

        if(useShellFolder) {
            newFolderButton.setFocusPainted(false);
        }

        topPanel.add(newFolderButton);
        topPanel.add(Box.createRigidArea(new Dimension(2, 0)));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(4, 9, 8, 0));
        panel.add(topPanel, BorderLayout.CENTER);

        return panel;
    }

    private JComponent createTree() {
        final DirectoryHandler dirHandler = createDirectoryHandler(fileChooser);
        // #106011: don't show "colors, food, sports" sample model after init :-)
        tree = new JTree(new Object[0]) {

            @Override
            protected void processMouseEvent(MouseEvent e) {
                if (this.isEnabled()) {
                    dirHandler.preprocessMouseEvent(e);
                    super.processMouseEvent(e);
                }
            }

            // For speed (#127170):
            @Override
            public boolean isLargeModel() {
                return true;
            }

            // #169303: on GTK or Nimbus L&F the row height can be initially set
            // to 0 which disables the "large model" optimizations (#127170)
            @Override
            public void setRowHeight(int rowHeight) {
                if (rowHeight > 0) {
                    super.setRowHeight(rowHeight);
                }
            }

            // To work with different font sizes (#106223); see: http://www.javalobby.org/java/forums/t19562.html
            private boolean firstPaint = true;
            @Override
            public void setFont(Font f) {
                firstPaint = true;
                super.setFont(f);
            }
            @Override
            public void paint(Graphics g) {
                if (firstPaint) {
                    g.setFont(getFont());
                    setRowHeight(Math.max(/* icon height plus insets? */17, g.getFontMetrics().getHeight()));
                    firstPaint = false;
                    // Setting the fixed height will generate another paint request, no need to complete this one
                    return;
                }
                super.paint(g);
            }
        };

        tree.setFocusable(true);
        tree.setOpaque(true);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setToggleClickCount(0);
        tree.addTreeExpansionListener(new TreeExpansionHandler());
        TreeKeyHandler keyHandler = new TreeKeyHandler();
        tree.addKeyListener(keyHandler);
        tree.addKeyListener(new AltUpHandler(tree));
        tree.addFocusListener(keyHandler);
        tree.addMouseListener(dirHandler);
        tree.addFocusListener(dirHandler);
        tree.addTreeSelectionListener(dirHandler);

        if(fileChooser.isMultiSelectionEnabled()) {
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        } else {
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        }

        TreeCellEditor tce = new DirectoryCellEditor(tree, fileChooser, new JTextField());
        tree.setCellEditor(tce);
        tce.addCellEditorListener(dirHandler);
        tree.setCellRenderer(new DirectoryTreeRenderer());
        JScrollPane scrollBar = new JScrollPane(tree);
        scrollBar.setViewportView(tree);
        tree.setInvokesStopCellEditing(true);

        return scrollBar;
    }

    private boolean jdkBug6840086Workaround() {
        //see issue #167080
        return Utilities.isWindows()
                && "Windows 7".equals(System.getProperty("os.name"))//NOI18N
                && "1.6.0_16".compareTo(System.getProperty("java.version")) >=0;//NOI18N
    }

    private class AltUpHandler extends KeyAdapter {
        
        private final JComponent component;

        public AltUpHandler(JComponent component) {
            this.component = component;
        }
        
        @Override
        public void keyPressed(KeyEvent evt) {
            if(evt.getKeyCode() == KeyEvent.VK_UP && (evt.getModifiers() & KeyEvent.ALT_MASK) == KeyEvent.ALT_MASK) {
                Action action = getChangeToParentDirectoryAction();
                action.actionPerformed(new ActionEvent(evt.getSource(), 0, ""));
                component.requestFocus();
            }
        }
    }

    /**
     * Handles keyboard quick search in tree and delete action.
     */
    class TreeKeyHandler extends KeyAdapter implements FocusListener {

        StringBuffer searchBuf = new StringBuffer();

        java.util.List<TreePath> paths;
        private final Timer resetBufferTimer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                resetBuffer();
            }
        });

        @Override
        public void keyPressed(KeyEvent evt) {
            if(evt.getKeyCode() == KeyEvent.VK_DELETE) {
                deleteAction();
            }

            // F2 as rename shortcut
            if (evt.getKeyCode() == KeyEvent.VK_F2) {
                final Object lastSelectedPathComponent = tree.getLastSelectedPathComponent();
                if (lastSelectedPathComponent instanceof FileNode) {
                    FileNode node = (FileNode)lastSelectedPathComponent;
                    applyEdit(node);
                }
            }

            if (isCharForSearch(evt)) {
                evt.consume();
            } else {
                resetBuffer();
            }

            // #105527: keyboard invocation of tree's popup menu
            if ((evt.getKeyCode() == KeyEvent.VK_F10) && evt.isShiftDown() && !popupMenu.isShowing()) {
                JTree tree = (JTree) evt.getSource();
                int selRow = tree.getLeadSelectionRow();
                if (selRow >= 0) {
                    Rectangle bounds = tree.getRowBounds(selRow);
                    popupMenu.show(tree, bounds.x + bounds.width / 2, bounds.y + bounds.height * 3 / 5);
                    evt.consume();
                }
            }
        }

        @Override
        public void keyTyped(KeyEvent evt) {
            char keyChar = evt.getKeyChar();
            if (isCharForSearch(evt)) {
                if (paths == null) {
                    paths = getVisiblePaths();
                }
                searchBuf.append(keyChar);
                resetBufferTimer.restart();
                TreePath activePath = tree.getSelectionPath();
                for (int i = 0; i < 2; ++i) {
                    String searchedText = searchBuf.toString().toLowerCase();
                    String curFileName = null;
                    if (i == 0 && activePath != null && (curFileName = fileChooser.getName(getLeafFile(activePath))) != null
                            && curFileName.toLowerCase().startsWith(searchedText)) {
                        // keep selection
                        return;
                    }
                    for (TreePath path : paths) {
                        final File file = getLeafFile(path);
                        if (file != null) {
                            curFileName = fileChooser.getName(file);
                            if (curFileName != null && curFileName.toLowerCase().startsWith(searchedText)) {
                                tree.makeVisible(path);
                                tree.scrollPathToVisible(path);
                                tree.setSelectionPath(path);
                                return;
                            }
                        }
                    }
                    searchBuf.delete(0, searchBuf.length() - 1);
                }
            } else {
                resetBuffer();
            }
        }

        @Override
        public void focusGained(FocusEvent e) {
            resetBuffer();
        }

        @Override
        public void focusLost(FocusEvent e) {
            resetBuffer();
        }

        private boolean isCharForSearch (KeyEvent evt) {
            char ch = evt.getKeyChar();
            // refuse backspace key
            if ((int)ch == 8) {
                return false;
            }
            // #110975: refuse modifiers
            if (evt.getModifiers() != 0) {
                return false;
            }
            return (Character.isJavaIdentifierPart(ch) && !Character.isIdentifierIgnorable(ch))
                    || Character.isSpaceChar(ch);
        }

        private void resetBuffer () {
            searchBuf.delete(0, searchBuf.length());
            paths = null;
        }

    }

    private java.util.List<TreePath> getVisiblePaths () {
        int rowCount = tree.getRowCount();
        FileNode node = null;
        java.util.List<TreePath> result = new ArrayList<TreePath>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            result.add(tree.getPathForRow(i));
        }
        return result;
    }

    private void createPopup() {
        popupMenu = new JPopupMenu();
        JMenuItem item1 = new JMenuItem(getBundle().getString("LBL_NewFolder"));//NOI18N
        item1.addActionListener(newFolderAction);

        JMenuItem item2 = new JMenuItem(getBundle().getString("LBL_Rename"));//NOI18N
        item2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Object lastSelectedPathComponent = tree.getLastSelectedPathComponent();
                if (lastSelectedPathComponent instanceof FileNode) {
                    FileNode node = (FileNode)lastSelectedPathComponent;
                    applyEdit(node);
                }
            }
        });

        JMenuItem item3 = new JMenuItem(getBundle().getString("LBL_Delete"));//NOI18N
        item3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteAction();
            }
        });

        popupMenu.add(item1);
        popupMenu.add(item2);
        popupMenu.add(item3);
            }

    // remove multiple directories
    private void deleteAction() {
        // fixed #97079 to be able to delete one or more folders
        final TreePath[] nodePath = tree.getSelectionPaths();

        if(nodePath == null) {
            return;
        }
        String message = "";

        if(nodePath.length == 1) {

            File file = getLeafFile(nodePath[0]);
            // Don't do anything if it's a special file
            if(!canWrite(file)) {
                return;
            }
            message = MessageFormat.format(getBundle().getString("MSG_Delete"), file.getName());//NOI18N
        } else {
            message = MessageFormat.format(getBundle().getString("MSG_Delete_Multiple"), nodePath.length);//NOI18N
        }

        int answer = JOptionPane.showConfirmDialog(fileChooser, message , getBundle().getString("MSG_Confirm"), JOptionPane.YES_NO_OPTION);//NOI18N

        if (answer == JOptionPane.YES_OPTION) {

            COMMON_RP.post(new Runnable() {
                FileNode node;
                ArrayList<File> list = new ArrayList<File>();
                int cannotDelete;
                ArrayList<FileNode> nodes2Remove = new ArrayList<FileNode>(nodePath.length);

                @Override
                public void run() {
                    if (!EventQueue.isDispatchThread()) {
                        // first pass, out of EQ thread, deletes files
                        setCursor(fileChooser, Cursor.WAIT_CURSOR);
                        cannotDelete = 0;
                        for(int i = 0; i < nodePath.length; i++) {
                            final Object lastPathComponent = nodePath[i].getLastPathComponent();
                            if (lastPathComponent instanceof FileNode) {                                
                                FileNode nodeToDelete = (FileNode)lastPathComponent;
                                try {
                                    delete(nodeToDelete.getFile());
                                    nodes2Remove.add(nodeToDelete);
                                } catch (IOException ignore) {
                                    cannotDelete++;

                                    if(canWrite(nodeToDelete.getFile())) {
                                        list.add(nodeToDelete.getFile());
                                    }
                                }
                            }
                        }
                        // send to second pass
                        EventQueue.invokeLater(this);
                    } else {
                        // second pass, in EQ thread
                        for (FileNode curNode : nodes2Remove) {
                            model.removeNodeFromParent(curNode);
                        }

                        setCursor(fileChooser, Cursor.DEFAULT_CURSOR);
                        if(cannotDelete > 0) {
                            String message = "";

                            if(cannotDelete == 1) {
                                message = cannotDelete + " " + getBundle().getString("MSG_Sing_Delete");//NOI18N
                            } else {
                                message = cannotDelete + " " + getBundle().getString("MSG_Plur_Delete");//NOI18N
                            }

                            setSelected(list.toArray(new File[list.size()]));

                            JOptionPane.showConfirmDialog(fileChooser, message , getBundle().getString("MSG_Confirm"), JOptionPane.OK_OPTION);//NOI18N
                        } else {
                            setSelected(new File[] {null});
                            setFileName("");
                        }
                    }
                }
            });
        }
    }

    private void delete(File file) throws IOException {
        FileObject fo = FileUtil.toFileObject(file);
        // Fixing #207116 - NPE when deleting a directory in remote browser.
        // This might be a pseudo-file based on another FileSystem' object (for example, remote file);
        // FileUtil.toFileObject will return correspondent *local* file if local file with such path exists.
        // That's why apart from null check, we check file.equals(FileUtil.toFile(fo),
        // which means that file object really corresponds to the given file.
        if (fo != null && file.equals(FileUtil.toFile(fo))) {
            fo.delete();
        } else {
            if (!file.delete()) {
                throw new IOException();
            }
        }
    }

    private String lastDir;
    private File[] lastChildren;
    private void updateCompletions() {
        if (showPopupCompletion) {
            final String name = normalizeFile(getFileName());
            int slash = name.lastIndexOf(fileSeparatorChar);
            if (slash != -1) {
                String prefix = name.substring(0, slash + 1);
                File[] children;
                synchronized (listFilesWorker) {
                    if (!prefix.equals(lastDir)) {
                        if (completionPopup != null) {
                            completionPopup.setDataList(new Vector<File>(0));
                            completionPopup.detach();
                            completionPopup = null;
                        }
                        listFilesWorker.d = prefix;
                        listFilesTask.schedule(0);
                        return;
                    } else {
                        children = lastChildren;
                    }
                }
                if (children != null) {
                    Vector<File> list = buildList(name, children, 20);
                    if(completionPopup == null) {
                        completionPopup = new FileCompletionPopup(fileChooser, filenameTextField, list);
                    } else if (completionPopup.isShowing() ||
                            (showPopupCompletion && fileChooser.isShowing())) {
                        completionPopup.setDataList(list);
                    }
                    if (fileChooser.isShowing() && !completionPopup.isShowing()) {
                        java.awt.Point los = filenameTextField.getLocation();
                        int popX = los.x;
                        int popY = los.y + filenameTextField.getHeight() - 6;
                        completionPopup.showPopup(filenameTextField, popX, popY);
                    }
                }
            }
        }
    }

    private class ListFilesWorker implements Runnable {
        private String d;
        @Override
        public void run() {
            String path;
            synchronized (this) {
                path = d;
            }
            File dir = getFileChooser().getFileSystemView().createFileObject(path);
            List<File> files = new LinkedList<File>();
            File[] children = dir.listFiles();
            if (children != null) {
                for (File f : children) {
                    if(fileChooser.accept(f)) {
                        if(fileChooser.getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY) {
                            if(f.isDirectory()) {
                                files.add(f);
                            }
                        } else if(fileChooser.getFileSelectionMode() == JFileChooser.FILES_ONLY) {
                            if(f.isFile()) {
                                files.add(f);
                            }
                        } else if(fileChooser.getFileSelectionMode() == JFileChooser.FILES_AND_DIRECTORIES) {
                            files.add(f);
                        }
                    }
                }
            }
            synchronized (this) {
                lastDir = path;
                lastChildren = files.toArray(new File[files.size()]);
            }
            if (lastChildren.length > 0) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateCompletions();
                    }
                });
            }
        }
    }

    public Vector<File> buildList(String text, File[] children, int max) {
        Vector<File> files = new Vector<File>(children.length);
        Arrays.sort(children, FileNode.FILE_NAME_COMPARATOR);

        for (File completion : children) {
            String path = completion.getAbsolutePath();
            if (path.regionMatches(true, 0, text, 0, text.length())) {
                files.add(completion);
            }
            if (files.size() >= max) {
                break;
            }
        }

        return files;
    }


    private String normalizeFile(String text) {
        // See #21690 for background.
        // XXX what are legal chars for var names? bash manual says only:
        // "The braces are required when PARAMETER [...] is followed by a
        // character that is not to be interpreted as part of its name."
        Pattern p = Pattern.compile("(^|[^\\\\])\\$([a-zA-Z_0-9.]+)");//NOI18N
        Matcher m;
        while ((m = p.matcher(text)).find()) {
            // Have an env var to subst...
            // XXX handle ${PATH} too? or don't bother
            String var = System.getenv(m.group(2));
            if (var == null) {
                // Try Java system props too, and fall back to "".
                var = System.getProperty(m.group(2), "");//NOI18N
            }
            // XXX full readline compat would mean vars were also completed with TAB...
            text = text.substring(0, m.end(1)) + var + text.substring(m.end(2));
        }
        if (text.equals("~")) {//NOI18N
            return fileChooser.getHomePath(); //NOI18N
        } else if (text.startsWith("~" + fileSeparatorChar)) {//NOI18N
            return fileChooser.getHomePath() + text.substring(1);//NOI18N
        } else {
            int i = text.lastIndexOf("//");//NOI18N
            if (i != -1) {
                // Treat /home/me//usr/local as /usr/local
                // (so that you can use "//" to start a new path, without selecting & deleting)
                return text.substring(i + 1);
            }
            i = text.lastIndexOf(fileSeparatorChar + "~" + fileSeparatorChar);//NOI18N
            if (i != -1) {
                // Treat /usr/local/~/stuff as /home/me/stuff
                return fileChooser.getHomePath() + text.substring(i + 2);//NOI18N
            }
            return text;
        }
    }

    private static ResourceBundle getBundle() {
        return NbBundle.getBundle(FileChooserUIImpl.class);
    }

    @Override
    public void rescanCurrentDirectory(JFileChooser fc) {
        if (!changeDirectory.get()) {
            return;
        }
        super.rescanCurrentDirectory(fc);
        File oldValue = curDir;
        File dir  = fc.getCurrentDirectory();
        if (oldValue != null) {
            /* Verify the toString of object */
            if (oldValue.equals(dir)) {
                return;
            }
        }
        curDir = dir;
        updateWorker.updateTree(curDir);
        fireDirectoryChanged(null);
    }

    private void initUpdateWorker () {
        updateWorker.attachFileChooser(this);
    }

    private void markStartTime () {
        fileChooser.putClientProperty(START_TIME, Long.valueOf(System.currentTimeMillis()));
    }

    private void checkUpdate() {
        if (Utilities.isWindows() && useShellFolder) {
            Long startTime = (Long) fileChooser.getClientProperty(START_TIME);
            if (startTime == null) {
                return;
            }
            // clean for future marking
            fileChooser.putClientProperty(START_TIME, null);

            long elapsed = System.currentTimeMillis() - startTime.longValue();
            long timeOut = NbPreferences.forModule(FileChooserUIImpl.class).
                    getLong(TIMEOUT_KEY, 10000);
            if (timeOut > 0 && elapsed > timeOut && slownessPanel == null) {
                JLabel slownessNote = new JLabel(
                        NbBundle.getMessage(FileChooserUIImpl.class, "MSG_SlownessNote"));//NOI18N
                slownessNote.setForeground(Color.RED);
                slownessPanel = new JPanel();
                JButton notShow = new JButton(
                        NbBundle.getMessage(FileChooserUIImpl.class, "BTN_NotShow"));//NOI18N
                notShow.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        NbPreferences.forModule(FileChooserUIImpl.class).putLong(TIMEOUT_KEY, 0);
                        centerPanel.remove(slownessPanel);
                        centerPanel.revalidate();
                    }
                });
                JPanel notShowP = new JPanel();
                notShowP.add(notShow);
                slownessPanel.setLayout(new BorderLayout());
                slownessPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                slownessPanel.add(BorderLayout.CENTER, slownessNote);
                slownessPanel.add(BorderLayout.SOUTH, notShowP);
                centerPanel.add(BorderLayout.NORTH, slownessPanel);
                centerPanel.revalidate();
            }
        }
    }

    private Boolean isXPStyle() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Boolean themeActive = (Boolean)toolkit.getDesktopProperty("win.xpstyle.themeActive");//NOI18N
        if(themeActive == null)
            themeActive = Boolean.FALSE;
        if (themeActive.booleanValue() && System.getProperty("swing.noxp") == null) {//NOI18N
            themeActive = Boolean.TRUE;
        }
        return themeActive;
    }

    @Override
    protected void installStrings(JFileChooser fc) {
        super.installStrings(fc);

        Locale l = fc.getLocale();

        lookInLabelMnemonic = UIManager.getInt("FileChooser.lookInLabelMnemonic");//NOI18N
        lookInLabelText = UIManager.getString("FileChooser.lookInLabelText",l);//NOI18N
        saveInLabelText = UIManager.getString("FileChooser.saveInLabelText",l);//NOI18N

        fileNameLabelMnemonic = UIManager.getInt("FileChooser.fileNameLabelMnemonic");//NOI18N
        fileNameLabelText = UIManager.getString("FileChooser.fileNameLabelText",l);//NOI18N

        filesOfTypeLabelMnemonic = UIManager.getInt("FileChooser.filesOfTypeLabelMnemonic");//NOI18N
        filesOfTypeLabelText = UIManager.getString("FileChooser.filesOfTypeLabelText",l);//NOI18N

        upFolderToolTipText =  UIManager.getString("FileChooser.upFolderToolTipText",l);//NOI18N
        if( null == upFolderToolTipText )
            upFolderToolTipText = NbBundle.getMessage(FileChooserUIImpl.class, "TLTP_UpFolder"); //NOI18N
        upFolderAccessibleName = UIManager.getString("FileChooser.upFolderAccessibleName",l);//NOI18N
        if( null == upFolderAccessibleName )
            upFolderAccessibleName = NbBundle.getMessage(FileChooserUIImpl.class, "ACN_UpFolder"); //NOI18N

        newFolderToolTipText = UIManager.getString("FileChooser.newFolderToolTipText",l);//NOI18N
        if( null == newFolderToolTipText )
            newFolderToolTipText = NbBundle.getMessage(FileChooserUIImpl.class, "TLTP_NewFolder"); //NOI18N
        newFolderAccessibleName = NbBundle.getMessage(FileChooserUIImpl.class, "ACN_NewFolder"); //NOI18N

        homeFolderTooltipText = UIManager.getString("FileChooser.homeFolderToolTipText",l);//NOI18N
        if( null == homeFolderTooltipText )
            homeFolderTooltipText = NbBundle.getMessage(FileChooserUIImpl.class, "TLTP_HomeFolder"); //NOI18N
        homeFolderAccessibleName = NbBundle.getMessage(FileChooserUIImpl.class, "ACN_HomeFolder"); //NOI18N

    }


    @Override
    protected void installListeners(final JFileChooser fc) {
        super.installListeners(fc);
        fc.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (DIRECTORY_CHANGED_PROPERTY.equals(evt.getPropertyName()) && fc == fileChooser) {
                    rescanCurrentDirectory(fileChooser);
                } else if (DIALOG_IS_CLOSING.equals(evt.getPropertyName()) && fc == fileChooser) {
                    updateWorker.shutdown();
                }
            }
        });
        ActionMap actionMap = getActionMap();
        SwingUtilities.replaceUIActionMap(fc, actionMap);
    }

    protected ActionMap getActionMap() {
        return createActionMap();
    }

    protected ActionMap createActionMap() {
        AbstractAction escAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getCancelSelectionAction().actionPerformed(e);
            }
            @Override
            public boolean isEnabled(){
                return getFileChooser().isEnabled();
            }
        };
        ActionMap map = new ActionMapUIResource();
        map.put("approveSelection", getApproveSelectionAction());//NOI18N
        map.put("cancelSelection", escAction);//NOI18N
        map.put("Go Up", getChangeToParentDirectoryAction());//NOI18N
        return map;
    }

    @Override
    public Action getNewFolderAction() {
        return newFolderAction;
    }

    @Override
    public void uninstallUI(JComponent c) {
        c.removePropertyChangeListener(filterTypeComboBoxModel);
        cancelButton.removeActionListener(getCancelSelectionAction());
        approveButton.removeActionListener(getApproveSelectionAction());
        filenameTextField.removeActionListener(getApproveSelectionAction());
        super.uninstallUI(c);
    }

    /**
     * Returns the preferred size of the specified
     * <code>JFileChooser</code>.
     * The preferred size is at least as large,
     * in both height and width,
     * as the preferred size recommended
     * by the file chooser's layout manager.
     *
     * @param c  a <code>JFileChooser</code>
     * @return   a <code>Dimension</code> specifying the preferred
     *           width and height of the file chooser
     */
    @Override
    public Dimension getPreferredSize(JComponent c) {
        int prefWidth = PREF_SIZE.width;
        Dimension d = c.getLayout().preferredLayoutSize(c);
        if (d != null) {
            return new Dimension(d.width < prefWidth ? prefWidth : d.width,
                    d.height < PREF_SIZE.height ? PREF_SIZE.height : d.height);
        } else {
            return new Dimension(prefWidth, PREF_SIZE.height);
        }
    }

    /**
     * Returns the minimum size of the <code>JFileChooser</code>.
     *
     * @param c  a <code>JFileChooser</code>
     * @return   a <code>Dimension</code> specifying the minimum
     *           width and height of the file chooser
     */
    @Override
    public Dimension getMinimumSize(JComponent c) {
        return MIN_SIZE;
    }

    /**
     * Returns the maximum size of the <code>JFileChooser</code>.
     *
     * @param c  a <code>JFileChooser</code>
     * @return   a <code>Dimension</code> specifying the maximum
     *           width and height of the file chooser
     */
    @Override
    public Dimension getMaximumSize(JComponent c) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    private String getStringOfFileName(File file, boolean singleSelection) {
        if (file == null) {
            return null;
        } else {
            JFileChooser fc = getFileChooser();
            if (fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled()) {
                if(fc.getFileSystemView().isDrive(file)) {
                    return file.getPath();
                } else {
                    return file.getPath();
                }
            } else if (singleSelection) {
                return file.getName();
            } else {
                return file.getPath();
            }
        }
    }

    private String getStringOfFileNames(File[] files) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; files != null && i < files.length; i++) {
            if (i > 0) {
                buf.append(" ");//NOI18N
            }
            if (files.length > 1) {
                buf.append("\"");//NOI18N
            }
            buf.append(getStringOfFileName(files[i], files.length <= 1));
            if (files.length > 1) {
                buf.append("\"");//NOI18N
            }
        }
        return buf.toString();
    }

    /* The following methods are used by the PropertyChange Listener */

    private void fireSelectedFileChanged(PropertyChangeEvent e) {
        File f = (File) e.getNewValue();
        JFileChooser fc = getFileChooser();
        if (f != null
                && ((fc.isFileSelectionEnabled() && !f.isDirectory())
                || (f.isDirectory() && fc.isDirectorySelectionEnabled()))) {

            setFileName(getStringOfFileName(f, true));
        }
    }

    private void fireSelectedFilesChanged(PropertyChangeEvent e) {
        File[] files = (File[]) e.getNewValue();
        JFileChooser fc = getFileChooser();
        if (files != null
                && files.length > 0
                && (files.length > 1 || fc.isDirectorySelectionEnabled() || (files[0] != null && !files[0].isDirectory()))) {
            setFileName(getStringOfFileNames(files));
        }
    }

    private void fireDirectoryChanged(PropertyChangeEvent e) {
        JFileChooser fc = getFileChooser();
        FileSystemView fsv = fc.getFileSystemView();
        showPopupCompletion = false;
        setFileName("");
        clearIconCache();
        File currentDirectory = fc.getCurrentDirectory();
        if(currentDirectory != null) {
            directoryComboBoxModel.addItem(currentDirectory);
            newFolderAction.enable(currentDirectory);
            getChangeToParentDirectoryAction().setEnabled(!fsv.isRoot(currentDirectory));
            if (e != null) {
                updateWorker.updateTree(currentDirectory);
            }
            if (fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled()) {
                if (fsv.isFileSystem(currentDirectory)) {
                    setFileName(getStringOfFileName(fc.getSelectedFile() == null ? currentDirectory : fc.getSelectedFile(), true));
                } else {
                    setFileName(null);
                }
            }
        }
    }

    private void fireFilterChanged(PropertyChangeEvent e) {
        clearIconCache();
        //re-run updater
        updateWorker.handleValidationParamasChanges();
    }

    private void fireFileSelectionModeChanged(PropertyChangeEvent e) {
        clearIconCache();
        JFileChooser fc = getFileChooser();

        File currentDirectory = fc.getCurrentDirectory();
        if (currentDirectory != null
                && fc.isDirectorySelectionEnabled()
                && !fc.isFileSelectionEnabled()
                && fc.getFileSystemView().isFileSystem(currentDirectory)) {

            setFileName(currentDirectory.getPath());
        } else {
            setFileName(null);
        }
    }

    private void fireMultiSelectionChanged(PropertyChangeEvent e) {
        if (getFileChooser().isMultiSelectionEnabled()) {
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        } else {
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            getFileChooser().setSelectedFiles(null);
        }
    }

    private void fireAccessoryChanged(PropertyChangeEvent e) {
        if(getAccessoryPanel() != null) {
            JComponent oldAcc = (JComponent) e.getOldValue();
            JComponent newAcc = (JComponent) e.getNewValue();
            JComponent accessoryPanel = getAccessoryPanel();
            if(oldAcc != null) {
                accessoryPanel.remove(oldAcc);
            }
            if (oldAcc != null && newAcc == null) {
                accessoryPanel.remove(topToolbar);
                topComboWrapper.add(topToolbar, BorderLayout.EAST);
                topCombo.setBorder(BorderFactory.createEmptyBorder(6, 0, 10, 0));
                topCombo.revalidate();
            }

            if(newAcc != null) {
                getAccessoryPanel().add(newAcc, BorderLayout.CENTER);
            }

            if (oldAcc == null && newAcc != null) {
                topComboWrapper.remove(topToolbar);
                topCombo.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
                accessoryPanel.add(topToolbar, BorderLayout.NORTH);
            }

        }
    }

    private void fireApproveButtonTextChanged(PropertyChangeEvent e) {
        JFileChooser chooser = getFileChooser();
        approveButton.setText(getApproveButtonText(chooser));
        approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
        // #107791: No mnemonics desirable on Mac
        if (!Utilities.isMac()) {
            approveButton.setMnemonic(getApproveButtonMnemonic(chooser));
        }
    }

    private void fireDialogTypeChanged(PropertyChangeEvent e) {
        JFileChooser chooser = getFileChooser();
        approveButton.setText(getApproveButtonText(chooser));
        approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
        // #107791: No mnemonics desirable on Mac
        if (!Utilities.isMac()) {
            approveButton.setMnemonic(getApproveButtonMnemonic(chooser));
        }
        if (chooser.getDialogType() == JFileChooser.SAVE_DIALOG) {
            lookInComboBoxLabel.setText(saveInLabelText);
        } else {
            lookInComboBoxLabel.setText(lookInLabelText);
        }
    }

    private void fireApproveButtonMnemonicChanged(PropertyChangeEvent e) {
        // #107791: No mnemonics desirable on Mac
        if (!Utilities.isMac()) {
            approveButton.setMnemonic(getApproveButtonMnemonic(getFileChooser()));
        }
    }

    private void fireControlButtonsChanged(PropertyChangeEvent e) {
        if(getFileChooser().getControlButtonsAreShown()) {
            addControlButtons();
        } else {
            removeControlButtons();
        }
    }

    /*
     * Listen for filechooser property changes, such as
     * the selected file changing, or the type of the dialog changing.
     */
    @Override
    public PropertyChangeListener createPropertyChangeListener(JFileChooser fc) {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                String s = e.getPropertyName();
                if(s.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                    fireSelectedFileChanged(e);
                } else if (s.equals(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY)) {
                    fireSelectedFilesChanged(e);
                } else if(s.equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY) && changeDirectory.get()) {
                    fireDirectoryChanged(e);
                } else if(s.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
                    fireFilterChanged(e);
                } else if(s.equals(JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY)) {
                    fireFileSelectionModeChanged(e);
                } else if(s.equals(JFileChooser.MULTI_SELECTION_ENABLED_CHANGED_PROPERTY)) {
                    fireMultiSelectionChanged(e);
                } else if(s.equals(JFileChooser.ACCESSORY_CHANGED_PROPERTY)) {
                    fireAccessoryChanged(e);
                } else if (s.equals(JFileChooser.APPROVE_BUTTON_TEXT_CHANGED_PROPERTY) ||
                        s.equals(JFileChooser.APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY)) {
                    fireApproveButtonTextChanged(e);
                } else if(s.equals(JFileChooser.DIALOG_TYPE_CHANGED_PROPERTY)) {
                    fireDialogTypeChanged(e);
                } else if(s.equals(JFileChooser.APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY)) {
                    fireApproveButtonMnemonicChanged(e);
                } else if(s.equals(JFileChooser.CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY)) {
                    fireControlButtonsChanged(e);
                } else if (s.equals(USE_SHELL_FOLDER)) {
                    updateUseShellFolder();
                    fireDirectoryChanged(e);
                } else if (s.equals("componentOrientation")) {//NOI18N
                    ComponentOrientation o = (ComponentOrientation)e.getNewValue();
                    JFileChooser cc = (JFileChooser)e.getSource();
                    if (o != (ComponentOrientation)e.getOldValue()) {
                        cc.applyComponentOrientation(o);
                    }
                } else if (s.equals("ancestor")) {//NOI18N
                    if (e.getOldValue() == null && e.getNewValue() != null) {
                        filenameTextField.selectAll();
                        filenameTextField.requestFocus();
                    }
                }
            }
        };
    }

    protected void removeControlButtons() {
        bottomPanel.remove(buttonPanel);
    }

    protected void addControlButtons() {
        bottomPanel.add(buttonPanel);
    }

    @Override
    public String getFileName() {
        if(filenameTextField != null) {
            return filenameTextField.getText();
        } else {
            return null;
        }
    }

    @Override
    public void setFileName(String filename) {
        if(filenameTextField != null) {
            filenameTextField.setText(filename);
        }
    }

    private DirectoryComboBoxRenderer createDirectoryComboBoxRenderer(JFileChooser fc) {
        return new DirectoryComboBoxRenderer();
    }

    private DirectoryComboBoxModel createDirectoryComboBoxModel(JFileChooser fc) {
        return new DirectoryComboBoxModel();
    }

    private FilterComboBoxRenderer createFilterComboBoxRenderer() {
        return new FilterComboBoxRenderer();
    }

    protected FilterTypeComboBoxModel createFilterComboBoxModel() {
        return new FilterTypeComboBoxModel();
    }

    @Override
    protected JButton getApproveButton(JFileChooser fc) {
        return approveButton;
    }

    @Override
    public FileView getFileView(JFileChooser fc) {

        // fix bug #96957, should use DirectoryChooserFileView
        // only on windows
//        if (Utilities.isWindows()) {
//            if (useShellFolder) {
//                fileView = new DirectoryChooserFileView();
//            }
//        } else {
//            fileView = ;//(BasicFileView) super.getFileView(fileChooser);
//        }
        return fileView;
    }

    private void setSelected(File[] files) {
        changeDirectory.set(false);
        fileChooser.setSelectedFiles(files);
        changeDirectory.set(true);
    }

    private DirectoryHandler createDirectoryHandler(JFileChooser chooser) {
        return new DirectoryHandler(chooser);
    }

    private void addNewDirectory(final TreePath path) {
        COMMON_RP.post(new Runnable() {
            @Override
            public void run() {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Object lastPathComponent = path.getLastPathComponent();
                        if (lastPathComponent instanceof FileNode) {
                            FileNode selectedNode = (FileNode)lastPathComponent;

                            if(selectedNode == null || !canWrite(selectedNode.getFile())) {
                                return;
                            }

                            try {
                                newFolderNode = new FileNode(fileChooser.getFileSystemView().createNewFolder(selectedNode.getFile()));
                                model.insertNodeInto(newFolderNode, selectedNode, selectedNode.getChildCount());
                                applyEdit(newFolderNode);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                });
            }
        });
    }

    private void applyEdit(FileNode node) {
        TreeNode[] nodes = model.getPathToRoot(node);
        TreePath editingPath = new TreePath(nodes);
        tree.setEditable(true);
        tree.makeVisible(editingPath);
        tree.scrollPathToVisible(editingPath);
        tree.setSelectionPath(editingPath);
        tree.startEditingAtPath(editingPath);

        JTextField editField = DirectoryCellEditor.getTextField();
        editField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        editField.setRequestFocusEnabled(true);
        editField.requestFocus();
        editField.setSelectionStart(0);
        editField.setSelectionEnd(editField.getText().length());
    }

    private static boolean canWrite(File f) {
        boolean writeable = false;
        if (f != null) {
            try {
                writeable = f.canWrite();
            } catch (AccessControlException ex) {
                writeable = false;
            }
        }
        return writeable;
    }

    private void expandNode(final JFileChooser fileChooser, final TreePath path) {

        COMMON_RP.post(new Runnable() {
            FileNode node;

            @Override
            public void run() {
                if (!EventQueue.isDispatchThread()) {
                    // first pass, out of EQ thread, loads data
                    markStartTime();
                    setCursor(fileChooser, Cursor.WAIT_CURSOR);
                    final Object lastPathComponent = path.getLastPathComponent();
                    if (lastPathComponent instanceof FileNode) {
                        node = (FileNode) lastPathComponent;
                        node.loadChildren(fileChooser, true);
                        // send to second pass
                        EventQueue.invokeLater(this);
                    }
                } else {
                    // second pass, in EQ thread
                    ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(node);
                  /*
                   * This happens when the add new directory action is called
                   * and the node is not loaded.  Furthermore, it ensures that
                   * adding a new directory execute after the UI has finished
                   * displaying the children of the expanded node.
                   */
                    if(addNewDirectory) {
                        addNewDirectory(path);
                        addNewDirectory = false;
                    }
                    setCursor(fileChooser, Cursor.DEFAULT_CURSOR);
                    checkUpdate();
                }
            }
        });
    }
 	
    private void loadNode(final JFileChooser fileChooser, final FileNode node) {
        COMMON_RP.post(new Runnable() {
            @Override
            public void run() {
                if (!EventQueue.isDispatchThread()) {
                    // first pass, out of EQ thread, loads data
                    markStartTime();
                    setCursor(fileChooser, Cursor.WAIT_CURSOR);
                    node.loadChildren(fileChooser, true);
                    // send to second pass
                    EventQueue.invokeLater(this);
                } else {
                    // second pass, in EQ thread
                    ((DefaultTreeModel) tree.getModel()).reload(node);
//                  ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(node);
                    setCursor(fileChooser, Cursor.DEFAULT_CURSOR);
                    checkUpdate();
                }
            }
        });
    }
 
    private void setCursor (final JComponent comp, final int type) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable () {
                @Override
                public void run () {
                    setCursor(comp, type);
                }
            });
        } else {
            Window window = SwingUtilities.getWindowAncestor(comp);
            if (window != null) {
                Cursor cursor = Cursor.getPredefinedCursor(type);
                window.setCursor(cursor);
                window.setFocusable(true);
            }
            //do not block root pane. at all

//            JRootPane pane = fileChooser.getRootPane();
//            if( null == blocker )
//                blocker = new InputBlocker();
//
//            if(type == Cursor.WAIT_CURSOR) {
//                blocker.block(pane);
//            } else if (type == Cursor.DEFAULT_CURSOR){
//                blocker.unBlock(pane);
//            }
        }
    }

    /*************** HELPER CLASSES ***************/

    private static class IconIndenter implements Icon {
        final static int space = 10;
        Icon icon = null;
        int depth = 0;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (icon == null) {
                return;
            }
            if (c.getComponentOrientation().isLeftToRight()) {
                icon.paintIcon(c, g, x+depth*space, y);
            } else {
                icon.paintIcon(c, g, x, y);
            }
        }

        @Override
        public int getIconWidth() {
            return icon != null ? icon.getIconWidth() + depth*space : 0;
        }

        @Override
        public int getIconHeight() {
            return icon != null ? icon.getIconHeight() : 0;
        }

    }

    private class DirectoryComboBoxRenderer  extends JLabel implements ListCellRenderer, UIResource {
        IconIndenter indenter = new IconIndenter();

        public DirectoryComboBoxRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected,
                boolean cellHasFocus) {
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N

            if (value == null) {
                setText("");
                return this;
            }
            File directory = (File)value;
            setText(getFileChooser().getName(directory));
            Icon icon = getFileChooser().getIcon(directory);
            indenter.icon = icon;
            indenter.depth = directoryComboBoxModel.getDepth(index);
            setIcon(indenter);
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            return this;
        }

        // #89393: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    } // end of DirectoryComboBoxRenderer

    /**
     * Data model for a type-face selection combo-box.
     */
    private class DirectoryComboBoxModel extends AbstractListModel implements ComboBoxModel {
        Vector<File> directories = new Vector<File>();
        int[] depths = null;
        File selectedDirectory = null;
        JFileChooser chooser = getFileChooser();
        FileSystemView fsv = chooser.getFileSystemView();

        public DirectoryComboBoxModel() {
            // Add the current directory to the model, and make it the
            // selectedDirectory
            File dir = getFileChooser().getCurrentDirectory();
            if(dir != null) {
                addItem(dir);
            }
        }

        /**
         * Adds the directory to the model and sets it to be selected,
         * additionally clears out the previous selected directory and
         * the paths leading up to it, if any.
         */
        private void addItem(File directory) {

            if (!SwingUtilities.isEventDispatchThread()) {
                // otherwise we easily get  ArrayIndexOutOfBoundsException -see issue #242883
                DLightLibsCommonLogger.printStackTraceOnce(
                        new AssertionError("fireDirectoryChanged should be called from EDT only!"), //NOI18N
                        Level.SEVERE, true);
            }

            if(directory == null) {
                return;
            }

            directories.clear();

            if(useShellFolder) {
                directories.addAll(Arrays.asList(getShellFolderRoots()));
            } else {
                directories.addAll(Arrays.asList(fileChooser.getFileSystemView().getRoots()));
            }


            // Get the canonical (full) path. This has the side
            // benefit of removing extraneous chars from the path,
            // for example /foo/bar/ becomes /foo/bar

            //as a side effect there will be a problem with symlinks
            //so just use the directory, do not use getCanonicalFile()
            File canonical = directory;
//            try {
//                canonical = directory.getCanonicalFile();
//            } catch (IOException e) {
//                // Maybe drive is not ready. Can't abort here.
//                canonical = directory;
//            }

            // create File instances of each directory leading up to the top
            File sf = useShellFolder? getShellFolderForFile(canonical) : canonical;
            if (sf == null) {
                sf = canonical;
            }
            File f = sf;
            Vector<File> path = new Vector<File>(10);


            /*
             * Fix for IZ#122534 :
             * NullPointerException at
             * org.netbeans.swing.dirchooser.DirectoryChooserUI$DirectoryComboBoxModel.addItem
             *
             */
            while( f!= null) {
                path.addElement(f);
                f = f.getParentFile();
            }

            int pathCount = path.size();
            // Insert chain at appropriate place in vector
            for (int i = 0; i < pathCount; i++) {
                f = path.get(i);
                if (directories.contains(f)) {
                    int topIndex = directories.indexOf(f);
                    for (int j = i-1; j >= 0; j--) {
                        directories.insertElementAt(path.get(j), topIndex+i-j);
                    }
                    break;
                }
            }
            calculateDepths();
            setSelectedItem(sf);
        }

        private void calculateDepths() {
            depths = new int[directories.size()];
            for (int i = 0; i < depths.length; i++) {
                File dir = directories.get(i);
                File parent = dir.getParentFile();
                depths[i] = 0;
                if (parent != null) {
                    for (int j = i-1; j >= 0; j--) {
                        if (parent.equals(directories.get(j))) {
                            depths[i] = depths[j] + 1;
                            break;
                        }
                    }
                }
            }
        }

        public int getDepth(int i) {
            return (depths != null && i >= 0 && i < depths.length) ? depths[i] : 0;
        }

        @Override
        public void setSelectedItem(Object selectedDirectory) {
            this.selectedDirectory = (File)selectedDirectory;
            fireContentsChanged(this, -1, -1);
        }

        @Override
        public Object getSelectedItem() {
            return selectedDirectory;
        }

        @Override
        public int getSize() {
            return directories.size();
        }

        @Override
        public Object getElementAt(int index) {
            return directories.elementAt(index);
        }
    }

    /**
     * Render different type sizes and styles.
     */
    private static class FilterComboBoxRenderer extends JLabel implements ListCellRenderer, UIResource {

        public FilterComboBoxRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list,
                Object value, int index, boolean isSelected,
                boolean cellHasFocus) {

            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N

            if (value != null && value instanceof FileFilter) {
                setText(((FileFilter)value).getDescription());
            }

            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            return this;
        }

        // #89393: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }

    } // end of FilterComboBoxRenderer

    /**
     * Data model for a type-face selection combo-box.
     */
    protected class FilterTypeComboBoxModel extends AbstractListModel implements ComboBoxModel, PropertyChangeListener {
        protected FileFilter[] filters;
        protected FilterTypeComboBoxModel() {
            super();
            filters = getFileChooser().getChoosableFileFilters();
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if(prop == JFileChooser.CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY) {
                filters = (FileFilter[]) e.getNewValue();
                fireContentsChanged(this, -1, -1);
            } else if (prop == JFileChooser.FILE_FILTER_CHANGED_PROPERTY) {
                fireContentsChanged(this, -1, -1);
            }
        }

        @Override
        public void setSelectedItem(Object filter) {
            if(filter != null) {
                getFileChooser().setFileFilter((FileFilter) filter);
                setFileName(null);
                fireContentsChanged(this, -1, -1);
            }
        }

        @Override
        public Object getSelectedItem() {
            // Ensure that the current filter is in the list.
            // NOTE: we shouldnt' have to do this, since JFileChooser adds
            // the filter to the choosable filters list when the filter
            // is set. Lets be paranoid just in case someone overrides
            // setFileFilter in JFileChooser.
            FileFilter currentFilter = getFileChooser().getFileFilter();
            boolean found = false;
            if(currentFilter != null) {
                for(int i=0; i < filters.length; i++) {
                    if(filters[i] == currentFilter) {
                        found = true;
                    }
                }
                if(found == false) {
                    getFileChooser().addChoosableFileFilter(currentFilter);
                }
            }
            return getFileChooser().getFileFilter();
        }

        @Override
        public int getSize() {
            if(filters != null) {
                return filters.length;
            } else {
                return 0;
            }
        }

        @Override
        public Object getElementAt(int index) {
            if(index > getSize() - 1) {
                // This shouldn't happen. Try to recover gracefully.
                return getFileChooser().getFileFilter();
            }
            if(filters != null) {
                return filters[index];
            } else {
                return null;
            }
        }
    }

    /**
     * Gets calls when the ComboBox has changed the selected item.
     */
    private class DirectoryComboBoxAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            File f = (File)directoryComboBox.getSelectedItem();
            getFileChooser().setCurrentDirectory(f);
        }
    }

    private class DirectoryChooserFileView extends BasicFileView implements Runnable{
        private final ScheduledExecutorService executor;
        private ScheduledFuture<?> iconTask;
        private final Object iconTaskLock = new Object();
        static final int LOADING_DELAY = 30;
        private File lookingForIcon;

        private DirectoryChooserFileView() {
            executor = Executors.newScheduledThreadPool(1);
        }

        private void loadIcon(){
            try{
                Thread.sleep(10);
            }catch (InterruptedException ex){
                //return;
            }
            if (Thread.interrupted()) {
                lookingForIcon = null;
                return;
            }
            Icon icon = FileChooserUIImpl.this.fileChooser.getFileSystemView().getSystemIcon(lookingForIcon);
            cacheIcon(lookingForIcon, icon);
            lookingForIcon = null;

        }

        @Override
        public void run() {
            if (SwingUtilities.isEventDispatchThread()) {
                FileChooserUIImpl.this.fileChooser.repaint();
            } else {
                loadIcon();
                SwingUtilities.invokeLater(this);
            }
        }



        @Override
        public Icon getIcon(File f) {

            Icon icon = getCachedIcon(f);
            if (icon != null) {
                return icon;
            }
            synchronized (iconTaskLock) {
                if (iconTask != null) {
                    iconTask.cancel(true);
                }
                if (lookingForIcon == null) {
                    lookingForIcon = f;
                }

                try {
                    iconTask = executor.schedule(this, LOADING_DELAY, TimeUnit.MILLISECONDS);
                } catch (RejectedExecutionException ex) {
                    if (RemoteLogger.getInstance().isLoggable(Level.FINEST)) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            return icon;
//
//            if (f != null) {
//                try {
//                    icon = fileChooser.getFileSystemView().getSystemIcon(f);
//                } catch (NullPointerException exc) {
//                    // workaround for JDK bug 6357445, in IZ: 145832, please remove when fixed
//                    LOG.log(Level.FINE, "JDK bug 6357445 encountered, NPE caught", exc); // NOI18N
//                }
//            }
//
//            if (icon == null) {
//                icon = super.getIcon(f);
//            }
//
//            cacheIcon(f, icon);
//            return icon;
        }
    }

    private class TextFieldKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent evt) {
            showPopupCompletion = true;
            int keyCode = evt.getKeyCode();
            // #105801: completionPopup might not be ready when updateCompletions not called (empty text field)
            if (completionPopup != null && !completionPopup.isVisible()) {
                if (keyCode == KeyEvent.VK_ENTER) {
                    getApproveSelectionAction().actionPerformed(new ActionEvent(evt.getSource(), evt.getID(), "")); //NOI18N
                    evt.consume();
                }
                if ((keyCode == KeyEvent.VK_TAB || keyCode == KeyEvent.VK_DOWN) ||
                    (keyCode == KeyEvent.VK_RIGHT &&
                    (filenameTextField.getCaretPosition() >= (filenameTextField.getDocument().getLength() - 1)))) {
                    updateCompletions();
                }

            }

            if(filenameTextField.isFocusOwner() &&
                    (completionPopup == null || !completionPopup.isVisible()) &&
                    keyCode == KeyEvent.VK_ESCAPE) {
                fileChooser.cancelSelection();
            }
        }
    }

    private class DirectoryHandler extends MouseAdapter
            implements TreeSelectionListener, CellEditorListener, ActionListener,
                        FocusListener, Runnable {
        private JFileChooser fileChooser;
        /** current selection holder */
        private WeakReference<TreePath> curSelPath;
        /** timer for slow click to rename feature */
        private Timer renameTimer;
        /** timer for slow dbl-click */
        private Timer dblClickTimer;
        /** path to rename for slow click to rename feature */
        private TreePath pathToRename;

        public DirectoryHandler(JFileChooser fileChooser) {
            this.fileChooser = fileChooser;
        }

        /************ imple of TreeSelectionListener *******/

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            showPopupCompletion = false;
            FileSystemView fsv = fileChooser.getFileSystemView();
            JTree tree = (JTree) e.getSource();
            TreePath path = tree.getSelectionPath();
            TreePath curSel = e.getNewLeadSelectionPath();
            curSelPath = (curSel != null) ? new WeakReference<TreePath>(curSel) : null;

            if(path != null) {
                if (!(path.getLastPathComponent() instanceof FileNode)) {
                    return;
                }

                FileNode node = (FileNode)path.getLastPathComponent();
                File file = node.getFile();

                if(file != null) {
                    //should I change the current Selection now?
                    setSelected(getSelectedNodes(tree.getSelectionPaths()));
                    newFolderAction.setEnabled(false);

                    if(!node.isLeaf()) {
                        newFolderAction.enable(file);
                        setDirectorySelected(true);
                    }
                }
            }
        }

        private File[] getSelectedNodes(TreePath[] paths) {
            List<File> files = new LinkedList<File>();
            for(int i = 0; i < paths.length; i++) {
                File file = getLeafFile(paths[i]);
                if (file!= null) {
                    if(file.isDirectory()
                            && fileChooser.isTraversable(file)
                            && !fileChooser.getFileSystemView().isFileSystem(file)) {
                        continue;
                    }
                    files.add(file);
                }
            }
            return files.toArray(new File[files.size()]);
        }

        /********* impl of MouseListener ***********/

        @Override
        public void mouseClicked(MouseEvent e) {
            final JTree tree = (JTree) e.getSource();
            Point p = e.getPoint();
            final int x = e.getX();
            final int y = e.getY();
            int row = tree.getRowForLocation(x, y);
            TreePath path = tree.getPathForRow(row);

            if (path != null) {
                final Object lastPathComponent = path.getLastPathComponent();
                if ( !(lastPathComponent instanceof FileNode)){
                    //can be Loading node
                    return;
                }
                FileNode node = (FileNode) lastPathComponent;
                newFolderAction.enable(node.getFile());

                if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
                    handleDblClick(node);
                    return;
                }

                // handles click to rename feature
                if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 1)) {
                    if (pathToRename != null) {
                        if (dblClickTimer != null) {
                            handleDblClick(node);
                            return;
                        }
                        // start slow click rename timer
                        renameTimer = new Timer(800, this);
                        renameTimer.setRepeats(false);
                        renameTimer.start();
                        startDblClickTimer();
                    }
                }

                startDblClickTimer();
                ((DirectoryTreeModel) tree.getModel()).nodeChanged(node);
                if (row == 0) {
                    tree.revalidate();
                    tree.repaint();
                }
            }
        }

        private void handleDblClick (FileNode node) {
            cancelRename();
            cancelDblClick();
            if (node.getFile().isFile() && !node.getFile().getPath().endsWith(".lnk")){//NOI18N
                fileChooser.approveSelection();
            } else {
                changeTreeDirectory(node.getFile());
            }
        }

        private void startDblClickTimer () {
            cancelDblClick();
            dblClickTimer = new Timer(500, this);
            dblClickTimer.setRepeats(false);
            dblClickTimer.start();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            handlePopupMenu(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            handlePopupMenu(e);
        }

        private void handlePopupMenu (MouseEvent e) {
            if (!e.isPopupTrigger()) {
                return;
            }
            final JTree tree = (JTree) e.getSource();
            Point p = e.getPoint();
            int x = e.getX();
            int y = e.getY();
            int row = tree.getRowForLocation(x, y);
            TreePath path = tree.getPathForRow(row);

            if (path != null) {
                final Object lastPathComponent = path.getLastPathComponent();
                if (lastPathComponent instanceof FileNode) {
                    FileNode node = (FileNode) lastPathComponent;
                    ((DirectoryTreeModel) tree.getModel()).nodeChanged(node);
                    if(!fileChooser.getFileSystemView().isFileSystem(node.getFile())) {
                        return;
                    }
                }
                tree.setSelectionPath(path);
                popupMenu.show(tree, x, y);
            }
        }

        private void changeTreeDirectory(File dir) {
            if (fileSeparatorChar == '\\' && dir.getPath().endsWith(".lnk")) {//NOI18N
                File linkLocation = getShellFolderForFileLinkLoc(dir);
                if (linkLocation != null && fileChooser.isTraversable(linkLocation)) {
                    dir = linkLocation;
                } else {
                    return;
                }
            }
            fileChooser.setCurrentDirectory(dir);
        }

        /********** implementation of CellEditorListener ****************/

        /** Refresh filename text field after rename */
        @Override
        public void editingStopped(ChangeEvent e) {
            FileNode node = (FileNode) tree.getLastSelectedPathComponent();
            if (node != null) {
                setFileName(getStringOfFileName(node.getFile(), true));
            }
        }

        @Override
        public void editingCanceled(ChangeEvent e) {
            // no operation
        }

        /********** ActionListener impl, slow-double-click rename ******/

        @Override
        public void actionPerformed(ActionEvent e) {
            if (tree.isFocusOwner() && isSelectionKept(pathToRename)) {
                if (e.getSource() == renameTimer) {
                    final Object lastSelectedPathComponent = tree.getLastSelectedPathComponent();
                    if (lastSelectedPathComponent instanceof FileNode) {
                        FileNode node = (FileNode)lastSelectedPathComponent;
                        if (node != null) {
                            applyEdit(node);
                        }
                    }
                }
            }
            // clear
            if (e.getSource() != dblClickTimer) {
                cancelRename();
            }
            cancelDblClick();
        }

        void preprocessMouseEvent (MouseEvent e) {
            if ((e.getID() != MouseEvent.MOUSE_PRESSED) || (e.getButton() != MouseEvent.BUTTON1)) {
                return;
            }
            TreePath clickedPath = tree.getPathForLocation(e.getX(), e.getY());
            if (clickedPath != null && isSelectionKept(clickedPath)) {
                pathToRename = clickedPath;
            }
        }

        private boolean isSelectionKept (TreePath selPath) {
            if (curSelPath != null) {
                TreePath oldSel = curSelPath.get();
                if (oldSel != null && oldSel.equals(selPath)) {
                    return true;
                }
            }
            return false;
        }

        private void cancelRename () {
            if (renameTimer != null) {
                renameTimer.stop();
                renameTimer = null;
            }
            pathToRename = null;
        }

        private void cancelDblClick () {
            Timer t = dblClickTimer;
            if (t != null) {
                t.stop();
                dblClickTimer = null;
            }
        }

        /******** implementation of focus listener, for slow click rename cancelling ******/

        @Override
        public void focusGained(FocusEvent e) {
            // don't allow to invoke click to rename immediatelly after focus gain
            // what may happen is that tree gains focus by mouse
            // click on selected item - on some platforms selected item
            // is not visible without focus and click to rename will
            // be unwanted and surprising for users

            // see run method
            SwingUtilities.invokeLater(this);
        }

        @Override
        public void run() {
            cancelRename();
        }

        @Override
        public void focusLost(FocusEvent e) {
            cancelRename();
        }

    }

    private class TreeExpansionHandler implements  TreeExpansionListener  {
        @Override
        public void treeExpanded(TreeExpansionEvent evt) {
            TreePath path = evt.getPath();
            final Object lastPathComponent = path.getLastPathComponent();
            if (lastPathComponent instanceof FileNode) {
                FileNode node = (FileNode) lastPathComponent;
                if(!node.isLoaded()) {
                    expandNode(fileChooser, path);
                } else {
                    // fixed #96954, to be able to add a new directory
                    // when the node has been already loaded
                    if(addNewDirectory) {
                        addNewDirectory(path);
                        addNewDirectory = false;
                    }
                    // Fix for IZ#123815 : Cannot refresh the tree content
                    refreshNode( path , node );
                }
            }
        }
        @Override
        public void treeCollapsed(TreeExpansionEvent event) {
        }

    }

    // Fix for IZ#123815 : Cannot refresh the tree content
    private void refreshNode( final TreePath path, final FileNode node ){
        final File folder = node.getFile();

        // Additional fixes for IZ#116859 [60cat] Node update bug in the "open project" panel while deleting directories
        if ( !folder.exists() ){
            TreePath parentPath = path.getParentPath();
            boolean refreshTree = false;

            if(tree.isExpanded(path)) {
                tree.collapsePath(path);
                refreshTree = true;
            }
            model.removeNodeFromParent( node );
            if ( refreshTree ){
                tree.expandPath( parentPath );
            }
            return;
        }

        COMMON_RP.post(new Runnable() {
            private Set<String> realDirs;
            @Override
            public void run() {
                if (!EventQueue.isDispatchThread()) {
                    // first phase
                    realDirs = new HashSet<String>();
                    File[] files = folder.listFiles();
                    files = files == null ? new File[0] : files;
                    for (File file : files) {
                        if ( !file.isDirectory() ){
                            continue;
                        }
                        String name = file.getName();
                        realDirs.add( name );
                    }
                    SwingUtilities.invokeLater(this);
                } else {
                    // second phase, in EQ thread, invoked from first phase
                    int count = node.getChildCount();
                    Map<String,FileNode> currentFiles =
                        new HashMap<String,FileNode>( );
                    for( int i=0; i< count ; i++ ){
                        TreeNode child = node.getChildAt(i);
                        if ( child instanceof FileNode ){
                            File file = ((FileNode)child).getFile();
                            currentFiles.put( file.getName() , (FileNode)child);
                        }
                    }

                    Set<String> realCloned = new HashSet<String>( realDirs );
                    if ( realCloned.removeAll( currentFiles.keySet()) ){
                        // Handle added folders
                        for ( String name : realCloned ){
                            FileNode added = new FileNode( getFileChooser().getFileSystemView().createFileObject( folder, name ) );
                            model.insertNodeInto( added, node, node.getChildCount());
                        }
                    }
                    Set<String> currentNames = new HashSet<String>( currentFiles.keySet());
                    if ( currentNames.removeAll( realDirs )){
                        // Handle deleted folders
                        for ( String name : currentNames ){
                            FileNode removed = currentFiles.get( name );
                            model.removeNodeFromParent( removed );
                        }
                    }
                }
            }
        });
    }


    private class NewDirectoryAction extends AbstractAction {

        private final RequestProcessor.Task enableTask = UPDATE_RP.create(new ActionEnabler());
        private File file;

        @Override
        public void actionPerformed(ActionEvent e) {
            final TreePath path = tree.getSelectionPath();

            if(path == null) {
                final Object root = tree.getModel().getRoot();
                //fixed bz#249623
                if (root instanceof LoadingNode) {
                    //do nothing, just return
                    return;
                }
                // if no nodes are selected, get the root node
                // fixed #96954, to be able to add a new directory
                // in the current directory shown in the tree
                if (root instanceof FileNode) {
                    addNewDirectory(new TreePath(model.getPathToRoot((FileNode)root)));
                }
            }

            if(path != null) {
                if(tree.isExpanded(path)) {
                    addNewDirectory(path);
                } else {
                    addNewDirectory = true;
                    tree.expandPath(path);
                }
            }
        }

        private void enable (File file) {
            setEnabled(false);
            this.file = file;
            enableTask.schedule(0);
        }

        private class ActionEnabler implements Runnable {
            @Override
            public void run () {
                final File f = file;
                if (canWrite(f)) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            if (f == file) {
                                setEnabled(true);
                            }
                        }
                    });
                }
            };
        }
    }

    private class DirectoryTreeRenderer implements TreeCellRenderer {
        HtmlRenderer.Renderer renderer = HtmlRenderer.createRenderer();

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean isSelected,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            Component stringDisplayer = renderer.getTreeCellRendererComponent(tree,
                    value,
                    isSelected,
                    expanded,
                    leaf,
                    row,
                    hasFocus);

            if(value instanceof FileNode) {
                tree.setShowsRootHandles(true);
                FileNode node = (FileNode)value;
                ((JLabel)stringDisplayer).setIcon(getNodeIcon(node));
                ((JLabel)stringDisplayer).setText(getNodeText(node));
            } else if (value instanceof LoadingNode) {
                tree.setShowsRootHandles(true);
                LoadingNode node = (LoadingNode)value;
                ((JLabel)stringDisplayer).setIcon(node.getIcon());
                ((JLabel)stringDisplayer).setText(node.getUserObject() + ""); //NOI18N
            }
                Font f = stringDisplayer.getFont();
                stringDisplayer.setPreferredSize(new Dimension(stringDisplayer.getPreferredSize().width, 30));

            // allow some space around icon of items
            ((JComponent)stringDisplayer).setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
            stringDisplayer.setSize(stringDisplayer.getPreferredSize());

            return stringDisplayer;
        }

        private Icon getNodeIcon(FileNode node) {
            return node.getIcon(fileChooser);
        }

        private String getNodeText (FileNode node) {
            return node.getText(fileChooser);
        }
    }

    private class LoadingTreeModel extends DefaultTreeModel {
        LoadingTreeModel(){
            super(new LoadingNode());
        }

    }


    private static class LoadingNode extends DefaultMutableTreeNode {
        LoadingNode() {
            super();
            final DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode(NbBundle.getMessage(FileChooserUIImpl.class, "LOADING"), false);
            add(defaultMutableTreeNode);
        }

        Icon getIcon() {
            return UIManager.getIcon("FileView.directoryIcon");//NOI18N
        }


    }
    private class DirectoryTreeModel extends DefaultTreeModel {

        public DirectoryTreeModel(TreeNode root) {
            super(root);
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
            boolean refreshTree = false;
            final Object lastPathComponent = path.getLastPathComponent();
            if (lastPathComponent instanceof FileNode) {
                FileNode node = (FileNode)lastPathComponent;
                File f = node.getFile();
                File newFile = getFileChooser().getFileSystemView().createFileObject(f.getParentFile(), (String)newValue);

                if(f.renameTo(newFile)) {
                    // fix bug #97521, #96960
                    if(tree.isExpanded(path)) {
                        tree.collapsePath(path);
                        refreshTree = true;
                    }

                    node.setFile(newFile);
                    node.removeAllChildren();

                    ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(node);
                    if(refreshTree) {
                        tree.expandPath(path);
                    }
                }
            }
        }
    }

    private void updateTree() {
        ValidationResult validationResult = this.currentState.validationResult;
        final TreeNode rootNode = validationResult.node;
        if (validationResult.isValid && rootNode != null) {
            this.curDir = validationResult.currentFile;
            model = new DirectoryTreeModel(rootNode);
            tree.setModel(model);
            tree.repaint();
            checkUpdate();
            if (validationResult.isDirectoryChanged) {
                fireDirectoryChanged(null);
            }
            boolean isLoading = false;
            if (rootNode instanceof FileNode) {
                FileNode fn = (FileNode) rootNode;
                if (!fn.isLoaded()) {
                    isLoading = true;
                    loadNode(fileChooser, fn);
                }
            }
            if (!isLoading) {
                setCursor(fileChooser, Cursor.DEFAULT_CURSOR);
            }
            return;
        }
        if (loadingModel == null) {
            loadingModel = new LoadingTreeModel();
        }
        setCursor(fileChooser, Cursor.WAIT_CURSOR);
        markStartTime();
        tree.setModel(loadingModel);
        tree.repaint();
        checkUpdate();
        if (validationResult.isDirectoryChanged) {
            fireDirectoryChanged(null);
        }

    }

    private static class ValidationResult {

        private final Boolean isValid;
        private final TreeNode node;
        private final boolean isDirectoryChanged;
        private final File currentFile;

        ValidationResult(Boolean isValid, TreeNode rootNode, boolean isDirectoryChanged, File file) {
            this.isValid = isValid;
            this.node = rootNode;
            this.isDirectoryChanged = isDirectoryChanged;
            this.currentFile = file;
        }
    }

    private static final class ValidationWorkerCheckState {
        // null - all is fine
        // TRUE - check in progress
        // FALSE - check failed

        private final Boolean checking;
        private final ValidationResult validationResult;

        private ValidationWorkerCheckState(Boolean checking, ValidationResult validationResult) {
            this.checking = checking;
            this.validationResult = validationResult;
        }
    }

    private static final class ValidationParams {

        private final File file;
        private long eventID;

        ValidationParams(File file) {
            this.file = file;
        }

        void setRequestID(long eventID) {
            this.eventID = eventID;
        }
    }

    private class UpdateWorker implements Runnable {
        private FileChooserUIImpl ui;
        private ValidationParams validationParams;
        private final ScheduledExecutorService executor;
        private ScheduledFuture<?> updateTask;
        private long lastEventID = 0;
        private final Object updateTaskLock = new Object();
        static final int VALIDATION_DELAY = 300;
        private ValidationWorkerCheckState lastCheck = null;

        UpdateWorker() {
            executor = Executors.newScheduledThreadPool(1);
        }


        private void handleValidationParamasChanges() {

            if (validationParams != null) {
                validationParams.setRequestID(++lastEventID);
            }
            ValidationResult validationResult = new ValidationResult(Boolean.FALSE,
                    null, false, null);//NOI18N
            currentState = new ValidationWorkerCheckState(Boolean.TRUE, validationResult);
            ui.updateTree();

            synchronized (updateTaskLock) {
                if (updateTask != null) {
                    updateTask.cancel(true);
                }
                try {
                    updateTask = executor.schedule(this,
                            VALIDATION_DELAY, TimeUnit.MILLISECONDS);
                } catch (RejectedExecutionException ex) {
                    if (RemoteLogger.getInstance().isLoggable(Level.FINEST)) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        public void updateTree(final File file) {
            validationParams = new ValidationParams(file);
            handleValidationParamasChanges();

        }

        public void attachFileChooser(final FileChooserUIImpl ui) {
            this.ui = ui;

        }

        public ValidationResult validate() {
            if (validationParams.eventID < lastEventID || ! fileChooser.isDisplayable()) {
                return new ValidationResult(Boolean.FALSE, null, false, curDir);
            }
            File oldValue = curDir;
            File file = validationParams.file;
            File prev = null;

            if (Thread.interrupted()) {
                return new ValidationResult(Boolean.FALSE, null, false, curDir);
            }

            while (!fileChooser.isTraversable(file) && prev != file) {
                prev = file;
                file = fileChooser.getFileSystemView().getParentDirectory(file);
            }
            if (Thread.interrupted()) {
                return new ValidationResult(Boolean.FALSE, null, false, curDir);
            }

            if (file == null) {
                final Callable<String> currentDirectoryPath = fileChooser.getAndClearDefaultDirectory();
                if (currentDirectoryPath != null) {
                    try{
                        String currentDir = currentDirectoryPath.call();
                        file = (currentDir == null || currentDir.isEmpty()) ?
                                fileChooser.getFileSystemView().getDefaultDirectory() :
                                fileChooser.getFileSystemView().createFileObject(currentDir);
                    } catch(Exception ex) {
                        ex.printStackTrace(System.err);
                        return new ValidationResult(Boolean.FALSE, null, false, curDir);
                    }
                } else {
                    file = oldValue  == null ? fileChooser.getFileSystemView().getDefaultDirectory() : oldValue;
                    //file = fileChooser.getFileSystemView().getDefaultDirectory();
                }
                if (file != null && file.isFile()) {
                    file = file.getParentFile();
                }
            }
            if (file == null) {
                return new ValidationResult(Boolean.FALSE, null, false, null);
            }
            final boolean directoryChanged = !file.equals(oldValue);
            final FileNode node = new FileNode(file);
            //node.loadChildren(fileChooser, true);
            return new ValidationResult(Boolean.TRUE, node, directoryChanged, file);
        }


        @Override
        public void run() {
            if (SwingUtilities.isEventDispatchThread()) {
                ValidationWorkerCheckState curStatus = lastCheck;
                currentState = curStatus;

                ValidationResult validationResult = curStatus == null ? null : curStatus.validationResult;
                if (curStatus == null || curStatus.checking == null) {
                    if (validationResult  == null) {
                        validationResult = new ValidationResult(Boolean.FALSE, null, false, null);
                    } else {
                        validationResult = new ValidationResult(Boolean.TRUE, validationResult.node, validationResult.isDirectoryChanged,  validationResult.currentFile);                    }

                    currentState = new ValidationWorkerCheckState(null, validationResult);
                }
                if (validationResult.isDirectoryChanged) {
                    fileChooser.setCurrentDirectory(validationResult.currentFile);
                }
                //TODO: show error
                ui.updateTree();
            } else {
                //check if we are not cancelled already
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    //log.log(Level.FINEST, "Interrupted (1) check for {0}", path);
                }
                ValidationResult result = validate();
                if (Thread.interrupted()) {
                    return;
                }
                lastCheck = new ValidationWorkerCheckState(result.isValid ? null : Boolean.FALSE, result);
                SwingUtilities.invokeLater(this);

            }
        }


        void cancel() {
            synchronized (updateTaskLock) {
                if (updateTask != null) {
                    updateTask.cancel(true);
                }
            }
        }
        private void restoreCursor () {
            ui.setCursor(fileChooser, Cursor.DEFAULT_CURSOR);
        }

        private void shutdown() {
            synchronized (updateTaskLock) {
                if (updateTask != null) {
                    updateTask.cancel(true);
                }
                executor.shutdown();
            }
        }

    } // end of UpdateWorker

    @Override
    public Action getApproveSelectionAction() {
        return approveSelectionAction;
    }

    @Override
    public Action getCancelSelectionAction() {
        return cancelSelectionAction;
    }

    @Override
    public Action getChangeToParentDirectoryAction() {
        return changeToParentDirectoryAction;
    }

    private class CancelSelectionAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            cancelled.set(true);
            getFileChooser().cancelSelection();
        }
    }

    private class ChangeToParentDirectoryAction extends AbstractAction {
        
        private volatile File parentFile;
        
        protected ChangeToParentDirectoryAction() {
            super("Go Up");
            putValue(Action.ACTION_COMMAND_KEY, "Go Up");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            setCursor(fileChooser, Cursor.WAIT_CURSOR);
            enableAllButCancel(false);
            getApproveSelectionAction();            
            final File curr1 = getFileChooser().getCurrentDirectory();
            COMMON_RP.post(new Runnable() {
                @Override
                public void run() {
                    if (!EventQueue.isDispatchThread()) {
                        File curr2 = getFileChooser().getCurrentDirectory();
                        if (curr2 != null && curr2.equals(curr1)) {
                            parentFile = getFileChooser().getFileSystemView().getParentDirectory(curr2);
                        } else {
                            parentFile = null;
                        }
                        SwingUtilities.invokeLater(this);
                    } else {
                        if(parentFile != null) {
                            getFileChooser().setCurrentDirectory(parentFile);
                        }
                        enableAllButCancel(true);
                        setEnabled(true);
                        setCursor(fileChooser, Cursor.DEFAULT_CURSOR);
                    }
                }
            });            
        }
    }

    private class ApproveSelectionAction extends AbstractAction {

        protected ApproveSelectionAction() {
            super("approveSelection");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            // most code here (and the following "if" is copied from
            // BasicFileChoooserUI.ApproveSelectionAction.actionPerformed
            // and adapted for our case
            if (isDirectorySelected()) {
                // exactly from BasicFileChoooserUI.ApproveSelectionAction.actionPerformed
                File dir = getDirectory();
                if (dir != null) {
                    // Strip trailing ".."
                    // dir = ShellFolder.getNormalizedFile(dir);
                    dir = dir.toPath().normalize().toFile();
                    changeDirectory(dir);
                    return;
                }
            }

            String filename = getFileName();

            if (filename != null) {
                // VK: why isn't it just trim() ? - do we really need leading spaces??
                // Remove whitespaces from end of filename
                int i = filename.length() - 1;
                while (i >=0 && filename.charAt(i) <= ' ') {
                    i--;
                }
                filename = filename.substring(0, i + 1);
            }

            if (filename == null || filename.length() == 0) {
                // no file selected, multiple selection off, therefore cancel the approve action
                resetGlobFilter();
                return;
            }

            // Unix: Resolve '~' to user's home directory
            if (fileChooser.isUnix()) {
                if (filename.startsWith("~/")) { // NOI18N
                    filename = fileChooser.getHomePath() + filename.substring(1);
                } else if (filename.equals("~")) { // NOI18N
                    filename = fileChooser.getHomePath();
                }
            }

            // in the case of single selectiom, use selectedFiles.get(0)
            final List<File> selectedFiles = new ArrayList<>();

            enableAllButCancel(false);
            ApproveSelectionFinisher finisher = new ApproveSelectionFinisher(e, filename, selectedFiles, cancelled);
            ApproveSelectionThreadWorker worker = new ApproveSelectionThreadWorker(
                    e, filename, fileChooser.isMultiSelectionEnabled(),
                    fileChooser.getCurrentDirectory(), fileChooser.getFileSystemView(),
                    selectedFiles, finisher, cancelled);
            APPROVE_RP.post(worker);
        }
    }

    /**
     * To be called OUT of EDT to perform long selection approval tasks.
     * Upon finishing its work, calls ApproveSelectionFinisher in EDT
     */

    private static class ApproveSelectionThreadWorker implements Runnable {

        private final ActionEvent e;
        private final String filename;
        private final boolean multySelection;
        private final File currentDir;
        private final FileSystemView fs;
        private final List<File> selectedFiles;
        private final ApproveSelectionFinisher finisher;
        private final AtomicBoolean cancelled;

        public ApproveSelectionThreadWorker(ActionEvent e, String filename, boolean multySelection, File currentDir, FileSystemView fs, List<File> selectedFiles, ApproveSelectionFinisher finisher, AtomicBoolean cancelled) {
            this.e = e;
            this.filename = filename;
            this.multySelection = multySelection;
            this.currentDir = currentDir;
            this.fs = fs;
            this.selectedFiles = selectedFiles;
            this.finisher = finisher;
            this.cancelled = cancelled;
        }

        @Override
        public void run() {
            try {
                if (multySelection && filename.length() > 1 &&
                        filename.charAt(0) == '"' && filename.charAt(filename.length() - 1) == '"') { // NOI18N

                    // VK: double space between \" breaks this?!
                    String[] files = filename.substring(1, filename.length() - 1).split("\" \""); // NOI18N
                    // Optimize searching files by names in "children" array
                    Arrays.sort(files);

                    File[] children = null;
                    int childIndex = 0;

                    for (String str : files) {
                        if (cancelled.get()) {
                            return;
                        }
                        File file = fs.createFileObject(str);
                        if (!file.isAbsolute()) {
                            if (children == null) {
                                children = fs.getFiles(currentDir, false);
                                Arrays.sort(children);
                            }
                            for (int k = 0; k < children.length; k++) {
                                int l = (childIndex + k) % children.length;
                                if (children[l].getName().equals(str)) {
                                    file = children[l];
                                    childIndex = l + 1;
                                    break;
                                }
                            }
                        }
                        selectedFiles.add(file);
                    }
                } else {
                    File selectedFile = fs.createFileObject(filename);
                    if (!selectedFile.isAbsolute()) {
                        selectedFile = fs.getChild(currentDir, filename);
                    }
                    selectedFiles.add(selectedFile);
                }
                finisher.init();
            } finally {
                SwingUtilities.invokeLater(finisher);
            }
        }
    }

    /**
     * To be called in EDT to complete selection approval
     */
    private class ApproveSelectionFinisher implements Runnable {

        private final String filename;
        private final List<File> selectedFiles;
        private final ActionEvent e;
        private final AtomicBoolean cancelled;
        private volatile boolean isSlectedFileExists;
        private volatile boolean isSlectedFileDir;

        public ApproveSelectionFinisher(ActionEvent e, String filename, List<File> selectedFiles, AtomicBoolean cancelled) {
            this.e = e;
            this.selectedFiles = selectedFiles;
            this.filename = filename;
            this.cancelled = cancelled;
        }
        
        //perform IO out of EDT
        private void init() {
            if (selectedFiles.size() == 1) {
                File selectedFile = selectedFiles.get(0);
                isSlectedFileExists = selectedFile.exists();
                isSlectedFileDir = selectedFile.isDirectory();
            }
        }

        @Override
        public void run() {

            if (cancelled.get()) {
                return;
            }

            enableAllButCancel(true);

            JFileChooser chooser = getFileChooser();
            resetGlobFilter();

            if (selectedFiles.size() == 1) {

                File selectedFile = selectedFiles.get(0);

                // check for wildcard pattern
                FileFilter currentFilter = chooser.getFileFilter();
                if (!isSlectedFileExists && isGlobPattern(filename)) {
                    changeDirectory(selectedFile.getParentFile());
                    if (globFilter == null) {
                        globFilter = new GlobFilter();
                    }
                    try {
                        globFilter.setPattern(selectedFile.getName());
                        if (!(currentFilter instanceof GlobFilter)) {
                            actualFileFilter = currentFilter;
                        }
                        chooser.setFileFilter(null);
                        chooser.setFileFilter(globFilter);
                        return;
                    } catch (PatternSyntaxException pse) {
                        // Not a valid glob pattern. Abandon filter.
                    }
                }

                // Check for directory change action
                boolean isDir = isSlectedFileDir;
                boolean isTrav = chooser.isTraversable(selectedFile);
                boolean isDirSelEnabled = chooser.isDirectorySelectionEnabled();
                boolean isFileSelEnabled = chooser.isFileSelectionEnabled();
                boolean isCtrl = (e != null && (e.getModifiers() &
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0);

                if (isDir && isTrav && (isCtrl || !isDirSelEnabled)) {
                    changeDirectory(selectedFile);
                    return;
                } else if ((isDir || !isFileSelEnabled)
                        && (!isDir || !isDirSelEnabled)
                        && (!isDirSelEnabled || isSlectedFileExists)) {
                    selectedFiles.clear();
                }
            }

            if (!selectedFiles.isEmpty()) {
                if (chooser.isMultiSelectionEnabled()) {
                    final File[] selectedFilesArray = selectedFiles.toArray(new File[selectedFiles.size()]);
                    chooser.setSelectedFiles(selectedFilesArray);
                    // Do it again. This is a fix for bug 4949273 to force the
                    // selected value in case the ListSelectionModel clears it
                    // for non-existing file names.
                    chooser.setSelectedFiles(selectedFilesArray);
                } else {
                    chooser.setSelectedFile(selectedFiles.get(0));
                }
                chooser.approveSelection();
            } else {
                if (chooser.isMultiSelectionEnabled()) {
                    chooser.setSelectedFiles(null);
                } else {
                    chooser.setSelectedFile(null);
                }
                chooser.cancelSelection();
            }
        }
    }

    private void enableAllButCancel(boolean enable) {
        this.upFolderButton.setEnabled(enable);
        this.newFolderButton.setEnabled(enable);
        this.approveButton.setEnabled(enable);
        this.newFolderButton.setEnabled(enable);
        if (homeButton != null) {
            this.homeButton.setEnabled(enable);
        }
        
        this.popupMenu.setEnabled(enable);
        
        this.filenameTextField.setEditable(enable);
        this.filenameTextField.setEnabled(enable);
        this.filterTypeComboBox.setEnabled(enable);
        this.tree.setEnabled(enable);
        this.directoryComboBox.setEnabled(enable);
    }

    private void changeDirectory(File dir) {
        JFileChooser fc = getFileChooser();

        // NOTE: We try to invoke 
        // BasicFileChoooserUI's "private changeDirectory(File)"
        // method, if available, or continue without FilePane enhancements
        // otherwise.
        try {
            Method superChangeDirectory = getClass().getSuperclass().getDeclaredMethod("changeDirectory", new Class[] { File.class} );
            superChangeDirectory.setAccessible(true);
            superChangeDirectory.invoke(this, dir);
            return;
        } catch (Throwable e) {
            LOG.log(Level.WARNING, String.format("Error invoking BasicFileChoooserUI.changeDirectory('%s'): %s:%s",
                    dir.getAbsolutePath(),
                    e.getMessage(),
                    e.getClass().getName()),
                    e);
        }
//        // Traverse shortcuts on Windows
//        if (dir != null && FilePane.usesShellFolder(fc)) {
//            try {
//                ShellFolder shellFolder = ShellFolder.getShellFolder(dir);
//
//                if (shellFolder.isLink()) {
//                    File linkedTo = shellFolder.getLinkLocation();
//
//                    // If linkedTo is null we try to use dir
//                    if (linkedTo != null) {
//                        if (fc.isTraversable(linkedTo)) {
//                            dir = linkedTo;
//                        } else {
//                            return;
//                        }
//                    } else {
//                        dir = shellFolder;
//                    }
//                }
//            } catch (FileNotFoundException ex) {
//                return;
//            }
//        }
        fc.setCurrentDirectory(dir);
        if (fc.getFileSelectionMode() == JFileChooser.FILES_AND_DIRECTORIES &&
            fc.getFileSystemView().isFileSystem(dir)) {

            setFileName(dir.getAbsolutePath());
        }
    }

    private void resetGlobFilter() {
        if (actualFileFilter != null) {
            JFileChooser chooser = getFileChooser();
            FileFilter currentFilter = chooser.getFileFilter();
            if (currentFilter != null && currentFilter.equals(globFilter)) {
                chooser.setFileFilter(actualFileFilter);
                chooser.removeChoosableFileFilter(globFilter);
            }
            actualFileFilter = null;
        }
    }

    private boolean isGlobPattern(String filename) {
        return ((fileSeparatorChar == '\\' && (filename.indexOf('*') >= 0
                                                  || filename.indexOf('?') >= 0))
                || (fileSeparatorChar == '/' && (filename.indexOf('*') >= 0
                                                  || filename.indexOf('?') >= 0
                                                  || filename.indexOf('[') >= 0)));
    }


    private static File getLeafFile(TreePath path) {
        if (path != null) {
            Object lastPathComponent = path.getLastPathComponent();
            if (lastPathComponent instanceof FileNode) {
                return ((FileNode) lastPathComponent).getFile();
            }
        }
        return null;
    }
    
    /* A file filter which accepts file patterns containing
     * the special wildcards *? on Windows and *?[] on Unix.
     */
    class GlobFilter extends FileFilter {
        Pattern pattern;
        String globPattern;

        public void setPattern(String globPattern) {
            char[] gPat = globPattern.toCharArray();
            char[] rPat = new char[gPat.length * 2];
            boolean isWin32 = (fileSeparatorChar == '\\');
            boolean inBrackets = false;
            int j = 0;

            this.globPattern = globPattern;

            if (isWin32) {
                // On windows, a pattern ending with *.* is equal to ending with *
                int len = gPat.length;
                if (globPattern.endsWith("*.*")) { // NOI18N
                    len -= 2;
                }
                for (int i = 0; i < len; i++) {
                    switch(gPat[i]) {
                      case '*':
                        rPat[j++] = '.';
                        rPat[j++] = '*';
                        break;

                      case '?':
                        rPat[j++] = '.';
                        break;

                      case '\\':
                        rPat[j++] = '\\';
                        rPat[j++] = '\\';
                        break;

                      default:
                        if ("+()^$.{}[]".indexOf(gPat[i]) >= 0) { // NOI18N
                            rPat[j++] = '\\';
                        }
                        rPat[j++] = gPat[i];
                        break;
                    }
                }
            } else {
                for (int i = 0; i < gPat.length; i++) {
                    switch(gPat[i]) {
                      case '*':
                        if (!inBrackets) {
                            rPat[j++] = '.';
                        }
                        rPat[j++] = '*';
                        break;

                      case '?':
                        rPat[j++] = inBrackets ? '?' : '.';
                        break;

                      case '[':
                        inBrackets = true;
                        rPat[j++] = gPat[i];

                        if (i < gPat.length - 1) {
                            switch (gPat[i+1]) {
                              case '!':
                              case '^':
                                rPat[j++] = '^';
                                i++;
                                break;

                              case ']':
                                rPat[j++] = gPat[++i];
                                break;
                            }
                        }
                        break;

                      case ']':
                        rPat[j++] = gPat[i];
                        inBrackets = false;
                        break;

                      case '\\':
                        if (i == 0 && gPat.length > 1 && gPat[1] == '~') {
                            rPat[j++] = gPat[++i];
                        } else {
                            rPat[j++] = '\\';
                            if (i < gPat.length - 1 && "*?[]".indexOf(gPat[i+1]) >= 0) { // NOI18N
                                rPat[j++] = gPat[++i];
                            } else {
                                rPat[j++] = '\\';
                            }
                        }
                        break;

                      default:
                        //if ("+()|^$.{}<>".indexOf(gPat[i]) >= 0) {
                        if (!Character.isLetterOrDigit(gPat[i])) {
                            rPat[j++] = '\\';
                        }
                        rPat[j++] = gPat[i];
                        break;
                    }
                }
            }
            this.pattern = Pattern.compile(new String(rPat, 0, j), Pattern.CASE_INSENSITIVE);
        }

        @Override
        public boolean accept(File f) {
            if (f == null) {
                return false;
            }
            if (f.isDirectory()) {
                return true;
            }
            return pattern.matcher(f.getName()).matches();
        }

        @Override
        public String getDescription() {
            return globPattern;
        }
    }

}

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
package org.netbeans.modules.j2ee.persistence.jpqleditor.ui;

import java.awt.CardLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.j2ee.persistence.api.PersistenceEnvironment;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.editor.JPAEditorUtil;
import org.netbeans.modules.j2ee.persistence.jpqleditor.JPQLEditorController;
import org.netbeans.modules.j2ee.persistence.jpqleditor.JPQLExecutor;
import org.netbeans.modules.j2ee.persistence.jpqleditor.JPQLResult;
import org.netbeans.modules.j2ee.persistence.jpqleditor.Utils;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.openide.awt.MouseUtils.PopupMouseAdapter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 * JPQL editor top component.
 */
@MIMEResolver.ExtensionRegistration(
    displayName = "",
extension = "jpql",
mimeType = "text/x-jpql",
position = 1660)
public final class JPQLEditorTopComponent extends TopComponent {

    /**
     * path to the icon used by the component and its open action
     */
    static final String ICON_PATH = "org/netbeans/modules/j2ee/persistence/jpqleditor/ui/resources/queryEditor16X16.png"; //NOI18N
    private static final Logger logger = Logger.getLogger(JPQLEditorTopComponent.class.getName());
    private PUDataObject puObject;
    private HashMap<String, PersistenceUnit> puConfigMap = new HashMap<>();
    private static List<Integer> windowCounts = new ArrayList<>();
    private Integer thisWindowCount = 0;
    private JPQLEditorController controller = null;
    private ProgressHandle ph = null;
    private ProgressHandle ph2 = null;
    private RequestProcessor requestProcessor;
    private RequestProcessor.Task hqlParserTask;
    private boolean isSqlTranslationProcessDone = false;
    private DatabaseConnection dbconn = null;

    private static int getNextWindowCount() {
        int count = 0;
        while (windowCounts.contains(count)) {
            count++;
        }
        windowCounts.add(count);
        return count;
    }

    public static JPQLEditorTopComponent getInstance() {
        return new JPQLEditorTopComponent(null);
    }

    public PUDataObject getDataObject() {
        return puObject;
    }

    public JPQLEditorTopComponent(JPQLEditorController controller) {
        this.controller = controller;
        initComponents();
        
        // configure row height
        // FIXME there should be a common place to do that
        int height = resultsTable.getRowHeight();
        Font cellFont = UIManager.getFont("TextField.font");
        if (cellFont != null) {
            FontMetrics metrics = resultsTable.getFontMetrics(cellFont);
            if (metrics != null) {
                height = metrics.getHeight() + 2;
            }
        }
        resultsTable.setRowHeight(Math.max(resultsTable.getRowHeight(), height));
        
        puComboBox.addActionListener( (ActionEvent e) -> puComboboxActionPerformed() );

        this.thisWindowCount = getNextWindowCount();
        setName(NbBundle.getMessage(JPQLEditorTopComponent.class, "CTL_JPQLEditorTopComponent") + thisWindowCount);
        setToolTipText(NbBundle.getMessage(JPQLEditorTopComponent.class, "HINT_JPQLEditorTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

        sqlToggleButton.setSelected(true);
        jpqlEditor.getDocument().addDocumentListener(new JPQLDocumentListener());
        
        //hack to unlock editor (make modifieble)
        ((NbEditorDocument) jpqlEditor.getDocument()).runAtomic( () -> {} );
        
        jpqlEditor.addMouseListener(new JPQLEditorPopupMouseAdapter());
        showSQL(NbBundle.getMessage(JPQLEditorTopComponent.class, "BuildHint"));
        resultsTable.setDefaultRenderer(Object.class, new ResultTableCellRenderer());
    }

    private class JPQLEditorPopupMouseAdapter extends PopupMouseAdapter {

        private JPopupMenu popupMenu;
        private JMenuItem runJPQLMenuItem;
        private JMenuItem cutMenuItem;
        private JMenuItem copyMenuItem;
        private JMenuItem pasteMenuItem;
        private JMenuItem selectAllMenuItem;
        private final String RUN_JPQL_COMMAND = NbBundle.getMessage(JPQLEditorTopComponent.class, "CTL_RUN_JPQL_COMMAND");
        private final String CUT_COMMAND = NbBundle.getMessage(JPQLEditorTopComponent.class, "CTL_CUT_COMMAND");
        private final String COPY_COMMAND = NbBundle.getMessage(JPQLEditorTopComponent.class, "CTL_COPY_COMMAND");
        private final String PASTE_COMMAND = NbBundle.getMessage(JPQLEditorTopComponent.class, "CTL_PASTE_COMMAND");
        private final String SELECT_ALL_COMMAND = NbBundle.getMessage(JPQLEditorTopComponent.class, "CTL_SELECT_ALL_COMMAND");
        private Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        public JPQLEditorPopupMouseAdapter() {
            super();
            popupMenu = new JPopupMenu();
            ActionListener actionListener = new PopupActionListener();
            runJPQLMenuItem = popupMenu.add(RUN_JPQL_COMMAND);
            runJPQLMenuItem.setMnemonic('Q');
            runJPQLMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK, false));
            runJPQLMenuItem.addActionListener(actionListener);

            popupMenu.addSeparator();

            cutMenuItem = popupMenu.add(CUT_COMMAND);
            cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK, true));
            cutMenuItem.setMnemonic('t');
            cutMenuItem.addActionListener(actionListener);

            copyMenuItem = popupMenu.add(COPY_COMMAND);
            copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, true));
            copyMenuItem.setMnemonic('y');
            copyMenuItem.addActionListener(actionListener);

            pasteMenuItem = popupMenu.add(PASTE_COMMAND);
            pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK, true));
            pasteMenuItem.setMnemonic('P');
            pasteMenuItem.addActionListener(actionListener);

            popupMenu.addSeparator();

            selectAllMenuItem = popupMenu.add(SELECT_ALL_COMMAND);
            selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK, true));
            selectAllMenuItem.setMnemonic('A');
            selectAllMenuItem.addActionListener(actionListener);
        }

        @Override
        protected void showPopup(MouseEvent evt) {
            // Series of checks.. to enable or disable menus.
            if (jpqlEditor.getText().trim().isEmpty()) {
                runJPQLMenuItem.setEnabled(false);
                selectAllMenuItem.setEnabled(false);
            } else {
                runJPQLMenuItem.setEnabled(true);
                selectAllMenuItem.setEnabled(true);
            }
            if (jpqlEditor.getSelectedText() == null || jpqlEditor.getSelectedText().trim().isEmpty()) {
                cutMenuItem.setEnabled(false);
                copyMenuItem.setEnabled(false);
            } else {
                cutMenuItem.setEnabled(true);
                copyMenuItem.setEnabled(true);
            }

            Transferable transferable = (Transferable) systemClipboard.getContents(null);
            if (transferable.getTransferDataFlavors().length == 0) {
                pasteMenuItem.setEnabled(false);
            } else {
                pasteMenuItem.setEnabled(true);
            }


            popupMenu.show(jpqlEditor, evt.getX(), evt.getY());
        }

        private class PopupActionListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals(RUN_JPQL_COMMAND)) {
                    runJPQLButtonActionPerformed(e);
                } else if (e.getActionCommand().equals(SELECT_ALL_COMMAND)) {
                    jpqlEditor.selectAll();
                } else if (e.getActionCommand().equals(CUT_COMMAND)) {
                    StringSelection stringSelection = new StringSelection(jpqlEditor.getSelectedText());
                    systemClipboard.setContents(stringSelection, stringSelection);
                    jpqlEditor.setText(
                            jpqlEditor.getText().substring(0, jpqlEditor.getSelectionStart())
                            + jpqlEditor.getText().substring(jpqlEditor.getSelectionEnd()));

                } else if (e.getActionCommand().equals(COPY_COMMAND)) {
                    StringSelection stringSelection = new StringSelection(jpqlEditor.getSelectedText());
                    systemClipboard.setContents(stringSelection, stringSelection);

                } else if (e.getActionCommand().equals(PASTE_COMMAND)) {
                    Transferable transferable = (Transferable) systemClipboard.getContents(null);
                    String clipboardContents = "";
                    try {
                        if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                            clipboardContents = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                        } else if (transferable.isDataFlavorSupported(DataFlavor.getTextPlainUnicodeFlavor())) {
                            clipboardContents = (String) transferable.getTransferData(DataFlavor.getTextPlainUnicodeFlavor());
                        }
                    } catch (UnsupportedFlavorException ex) {
                        logger.log(Level.INFO, "Unsupported transfer flavor", ex);
                    } catch (IOException ex) {
                        logger.log(Level.INFO, "IOException during paste operation", ex);
                    }
                    if (!clipboardContents.isEmpty()) {
                        if (jpqlEditor.getSelectedText() != null) {
                            jpqlEditor.replaceSelection(clipboardContents);
                        } else {
                            jpqlEditor.setText(
                                    jpqlEditor.getText().substring(0, jpqlEditor.getCaretPosition())
                                    + clipboardContents
                                    + jpqlEditor.getText().substring(jpqlEditor.getCaretPosition()));
                        }
                    }
                }
            }
        }
    }

    public void setFocusToEditor() {
        if(!jpqlEditor.isFocusOwner()) {
            jpqlEditor.requestFocus();
        }
    }

    private class ParseJPQL extends Thread {

        @Override
        public void run() {
            while (!isSqlTranslationProcessDone) {
                String jpql = jpqlEditor.getText().trim();
                if (jpql.isEmpty()) {
                    return;
                }
                if (puComboBox.getSelectedItem() == null) {
                    logger.info("persistence unit selection combo box is empty.");
                    return;
                }
                PersistenceUnit selectedConfigObject = puConfigMap.get(
                        puComboBox.getSelectedItem().toString());

                if (Thread.interrupted() || isSqlTranslationProcessDone) {
                    return;    // Cancel the task
                }
                if (selectedConfigObject != null) {
                    if (Thread.interrupted() || isSqlTranslationProcessDone) {
                        return;    // Cancel the task
                    }
                    ph2 = ProgressHandleFactory.createHandle(
                            NbBundle.getMessage(JPQLEditorTopComponent.class, "progressTaskname"));
                    ph2.start(100);
                    FileObject pXml = puObject.getPrimaryFile();
                    Project project = pXml != null ? FileOwnerQuery.getOwner(pXml) : null;
                    PersistenceEnvironment pe = project != null ? project.getLookup().lookup(PersistenceEnvironment.class) : null;
                    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
                    final List<URL> localResourcesURLList = new ArrayList<>();
                    final HashMap<String, String> props = new HashMap<>();
                    final boolean containerManaged = Util.isSupportedJavaEEVersion(pe.getProject());
                    final Provider provider = ProviderUtil.getProvider(selectedConfigObject.getProvider(), pe.getProject());
                    final List<String> initialProblems = new ArrayList<>();
                    if (containerManaged && provider!=null) {
                        Utils.substitutePersistenceProperties(pe, selectedConfigObject, dbconn, props);
                    }
                    try {
                        initialProblems.addAll(Utils.collectClassPathURLs(pe, selectedConfigObject, dbconn, localResourcesURLList));
                        if(initialProblems.isEmpty()) {
                            ClassLoader customClassLoader = pe.getProjectClassLoader(
                                    localResourcesURLList.toArray(new URL[]{}));
                            Thread.currentThread().setContextClassLoader(customClassLoader);
                            JPQLExecutor queryExecutor = new JPQLExecutor();
                            JPQLResult jpqlResult = new JPQLResult();
                            try {
                                // Parse POJOs from JPQL
                                // Check and if required compile POJO files mentioned in JPQL

                                ph2.progress(50);
                                ph2.setDisplayName(NbBundle.getMessage(JPQLEditorTopComponent.class, "queryParsingPassControlToProvider"));
                                jpqlResult = queryExecutor.execute(jpql, selectedConfigObject, pe, props, provider, 0, ph2, false);
                                ph2.progress(80);
                                ph2.setDisplayName(NbBundle.getMessage(JPQLEditorTopComponent.class, "queryParsingProcessResults"));

                            } catch (Exception|NoClassDefFoundError e) {
                                logger.log(Level.INFO, "Problem in executing JPQL", e);
                                jpqlResult.getExceptions().add(e);
                            }

                            if (Thread.interrupted() || isSqlTranslationProcessDone) {
                                return;    // Cancel the task
                            }
                            if (jpqlResult.getExceptions() != null && !jpqlResult.getExceptions().isEmpty()) {
                                logger.log(Level.INFO, "", jpqlResult.getExceptions());
                                showSQLError("GeneralError", jpqlResult.getQueryProblems());//NOI18N
                            } else {
                                if (jpqlResult.getSqlQuery() == null || jpqlResult.getSqlQuery().length() == 0) {
                                    showSQLError("UnsupportedProvider", jpqlResult.getQueryProblems());//NOI18N
                                } else {
                                    showSQL(jpqlResult.getSqlQuery());
                                }
                            }
                        } else {
                            StringBuilder sb = new StringBuilder();
                            for(String txt:initialProblems){
                                sb.append(txt).append("\n");
                            }
                            showSQLError(null, sb.toString());
                        }

                    } catch (Exception e) {
                        logger.log(Level.INFO, "", e);
                        showSQLError("GeneralError", null);//NOI18N
                    } finally {
                        isSqlTranslationProcessDone = true;
                        ph2.finish();
                        Thread.currentThread().setContextClassLoader(oldClassLoader);
                    }
                }

            }
        }
    }

    private void showSQL(String sql) {
        sqlEditorPane.setText(sql);
        switchToSQLView();
    }

    private void showSQLError(String errorResourceKey, String queryProblems) {
        if (queryProblems != null) {
            sqlEditorPane.setText(queryProblems);
        } else {
            //use default error message
            sqlEditorPane.setText(
                    NbBundle.getMessage(JPQLEditorTopComponent.class, errorResourceKey));
        }
        //
        switchToSQLView();
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        setFocusToEditor();
        requestProcessor = new RequestProcessor("hql-parser", 1, true);
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        requestProcessor.stop();
    }

    private class JPQLDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            process();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            process();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            process();
        }

        private void process() {
            if (hqlParserTask != null && !hqlParserTask.isFinished() && (hqlParserTask.getDelay() != 0)) {
                hqlParserTask.cancel();
            } else if (!requestProcessor.isShutdown()) {
                hqlParserTask = requestProcessor.post(new ParseJPQL(), 2000);
                isSqlTranslationProcessDone = false;
            }
        }
    }

    public void fillPersistenceConfigurations(Node[] activatedNodes) {
        Node node = activatedNodes[0];
        DataObject dO = node.getLookup().lookup(DataObject.class);
        puObject = null;
        if (dO instanceof PUDataObject) {
            puObject = (PUDataObject) dO;
            dO.addPropertyChangeListener( (PropertyChangeEvent evt) -> {
                if (DataObject.PROP_VALID.equals(evt.getPropertyName()) && Boolean.FALSE.equals(evt.getNewValue())) {
                    if (SwingUtilities.isEventDispatchThread()) {
                        close();//need to close if corresponding dataobject was invalidated (deleted)
                    } else {
                        SwingUtilities.invokeLater( () -> close() );
                    }
                }
            });

            Persistence persistence = puObject.getPersistence();
            if (persistence == null) {
                logger.warning("corrupted persistence.xml in enclosing project.");
            } else if (persistence.getPersistenceUnit().length > 0) {
                for (PersistenceUnit unit : persistence.getPersistenceUnit()) {
                    String configName = unit.getName(); //NOI18N
                    puConfigMap.put(configName, unit);
                }
                puComboBox.setModel(new DefaultComboBoxModel(puConfigMap.keySet().toArray()));
                puComboBox.setSelectedIndex(0);
            }
        } else {
            //TODO Don't know whether this case will actually arise..
        }

    }

    /**
     *
     * @param result
     * @param ccl
     */
    public void setResult(JPQLResult result, ClassLoader ccl) {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ccl);
        if (result.getSqlQuery() != null) {
            sqlEditorPane.setText(result.getSqlQuery());
        }
        if (result.getExceptions().isEmpty()) {
            // logger.info(r.getQueryResults().toString());
            switchToResultView();
            StringBuilder strBuffer = new StringBuilder();
            String space = " ", separator = "; "; //NOI18N
            strBuffer.append(result.getUpdateOrDeleteResult());
            strBuffer.append(space);
            strBuffer.append(NbBundle.getMessage(JPQLEditorTopComponent.class, "queryUpdatedOrDeleted"));
            strBuffer.append(separator);

            strBuffer.append(space);
            strBuffer.append(result.getQueryResults().size());
            strBuffer.append(space);
            strBuffer.append(NbBundle.getMessage(JPQLEditorTopComponent.class, "rowsSelected"));

            setStatus(strBuffer.toString());

            TableModel tm = new DefaultTableModel();

            if (!result.getQueryResults().isEmpty()) {
                try {
                    List<ReflectionInfo> info = ReflectionInfo.prepare(result.getQueryResults());
                    tm = new ReflectiveTableModel(info, result.getQueryResults());
                } catch (IntrospectionException ex) {
                    logger.log(Level.WARNING, "Failed to reflect while building table model for JPA display", ex);
                }
            }
            resultsTable.clearSelection();
            resultsTable.setModel(tm);


        } else {
            logger.log(Level.INFO, "JPQL query execution resulted in {0} errors.", result.getExceptions().size());//NOI18N

            switchToErrorView();
            setStatus(NbBundle.getMessage(JPQLEditorTopComponent.class, "queryExecutionError"));
            errorTextArea.setText("");
            for (Throwable t : result.getExceptions()) {
                StringWriter sWriter = new StringWriter();
                PrintWriter pWriter = new PrintWriter(sWriter);
                t.printStackTrace(pWriter);
                errorTextArea.append(
                        removePersistenceModuleCodelines(sWriter.toString()));

            }
            if (result.getQueryProblems() != null) {
                sqlEditorPane.setText(result.getQueryProblems());
            }
        }

        ph.progress(99);
        ph.setDisplayName(NbBundle.getMessage(JPQLEditorTopComponent.class, "queryExecutionDone"));

        runJPQLButton.setEnabled(true);
        ph.finish();
        Thread.currentThread().setContextClassLoader(oldClassLoader);
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private String removePersistenceModuleCodelines(String exceptionTrace) {
        StringTokenizer tokenizer = new StringTokenizer(exceptionTrace, "\n");
        StringBuilder filteredExceptionTrace = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (!token.contains("org.netbeans.modules.j2ee.persistence")) {
                filteredExceptionTrace.append(token).append("\n");
            }
        }
        return filteredExceptionTrace.toString();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolBar = new javax.swing.JToolBar();
        sessionLabel = new javax.swing.JLabel();
        puComboBox = new javax.swing.JComboBox();
        toolbarSeparator = new javax.swing.JToolBar.Separator();
        runJPQLButton = new javax.swing.JButton();
        toolbarSeparator1 = new javax.swing.JToolBar.Separator();
        splitPane = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jpqlEditor = new javax.swing.JEditorPane();
        containerPanel = new javax.swing.JPanel();
        toolBar2 = new javax.swing.JToolBar();
        resultToggleButton = new javax.swing.JToggleButton();
        sqlToggleButton = new javax.swing.JToggleButton();
        spacerPanel1 = new javax.swing.JPanel();
        spacerPanel2 = new javax.swing.JPanel();
        setMaxRowCountPanel = new javax.swing.JPanel();
        setMaxRowCountLabel = new javax.swing.JLabel();
        setMaxRowCountComboBox = new javax.swing.JComboBox();
        executionPanel = new javax.swing.JPanel();
        resultContainerPanel = new javax.swing.JPanel();
        statusPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        resultsOrErrorPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        errorTextArea = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        resultsTable = new javax.swing.JTable() {
            public java.awt.Dimension getPreferredScrollableViewportSize()
            {
                java.awt.Dimension size = super.getPreferredScrollableViewportSize();
                return new java.awt.Dimension(Math.min(getPreferredSize().width, size.width), size.height);
            }
        };
        jScrollPane2 = new javax.swing.JScrollPane();
        sqlEditorPane = new javax.swing.JTextPane();

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(sessionLabel, org.openide.util.NbBundle.getMessage(JPQLEditorTopComponent.class, "JPQLEditorTopComponent.sessionLabel.text")); // NOI18N
        toolBar.add(sessionLabel);

        toolBar.add(puComboBox);
        toolBar.add(toolbarSeparator);

        runJPQLButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/j2ee/persistence/jpqleditor/ui/resources/run_jpql_query_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(runJPQLButton, org.openide.util.NbBundle.getMessage(JPQLEditorTopComponent.class, "JPQLEditorTopComponent.runJPQLButton.text")); // NOI18N
        runJPQLButton.setToolTipText(org.openide.util.NbBundle.getMessage(JPQLEditorTopComponent.class, "runJPQLQueryButtonToolTip")); // NOI18N
        runJPQLButton.setFocusable(false);
        runJPQLButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        runJPQLButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        runJPQLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runJPQLButtonActionPerformed(evt);
            }
        });
        toolBar.add(runJPQLButton);

        toolbarSeparator1.setSeparatorSize(new java.awt.Dimension(300, 10));
        toolBar.add(toolbarSeparator1);

        splitPane.setBorder(null);
        splitPane.setDividerLocation(180);
        splitPane.setDividerSize(7);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jpqlEditor.setContentType("text/x-jpql");
        jScrollPane1.setViewportView(jpqlEditor);

        splitPane.setTopComponent(jScrollPane1);

        toolBar2.setFloatable(false);
        toolBar2.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(resultToggleButton, org.openide.util.NbBundle.getMessage(JPQLEditorTopComponent.class, "JPQLEditorTopComponent.resultToggleButton.text")); // NOI18N
        resultToggleButton.setToolTipText(org.openide.util.NbBundle.getMessage(JPQLEditorTopComponent.class, "showResultTooltipText")); // NOI18N
        resultToggleButton.setFocusable(false);
        resultToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        resultToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        resultToggleButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                resultToggleButtonItemStateChanged(evt);
            }
        });
        toolBar2.add(resultToggleButton);

        org.openide.awt.Mnemonics.setLocalizedText(sqlToggleButton, org.openide.util.NbBundle.getMessage(JPQLEditorTopComponent.class, "JPQLEditorTopComponent.sqlToggleButton.text")); // NOI18N
        sqlToggleButton.setToolTipText(org.openide.util.NbBundle.getMessage(JPQLEditorTopComponent.class, "showSQLTooltipText")); // NOI18N
        sqlToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        sqlToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        sqlToggleButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sqlToggleButtonItemStateChanged(evt);
            }
        });
        toolBar2.add(sqlToggleButton);

        javax.swing.GroupLayout spacerPanel1Layout = new javax.swing.GroupLayout(spacerPanel1);
        spacerPanel1.setLayout(spacerPanel1Layout);
        spacerPanel1Layout.setHorizontalGroup(
            spacerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 224, Short.MAX_VALUE)
        );
        spacerPanel1Layout.setVerticalGroup(
            spacerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 27, Short.MAX_VALUE)
        );

        toolBar2.add(spacerPanel1);

        javax.swing.GroupLayout spacerPanel2Layout = new javax.swing.GroupLayout(spacerPanel2);
        spacerPanel2.setLayout(spacerPanel2Layout);
        spacerPanel2Layout.setHorizontalGroup(
            spacerPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );
        spacerPanel2Layout.setVerticalGroup(
            spacerPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 27, Short.MAX_VALUE)
        );

        toolBar2.add(spacerPanel2);

        org.openide.awt.Mnemonics.setLocalizedText(setMaxRowCountLabel, org.openide.util.NbBundle.getMessage(JPQLEditorTopComponent.class, "JPQLEditorTopComponent.setMaxRowCountLabel.text")); // NOI18N

        setMaxRowCountComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "100", "1000", "10000", "100000" }));
        setMaxRowCountComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(JPQLEditorTopComponent.class, "setMaxRowToolTip")); // NOI18N

        javax.swing.GroupLayout setMaxRowCountPanelLayout = new javax.swing.GroupLayout(setMaxRowCountPanel);
        setMaxRowCountPanel.setLayout(setMaxRowCountPanelLayout);
        setMaxRowCountPanelLayout.setHorizontalGroup(
            setMaxRowCountPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setMaxRowCountPanelLayout.createSequentialGroup()
                .addComponent(setMaxRowCountLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(setMaxRowCountComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        setMaxRowCountPanelLayout.setVerticalGroup(
            setMaxRowCountPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setMaxRowCountPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(setMaxRowCountLabel)
                .addComponent(setMaxRowCountComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        toolBar2.add(setMaxRowCountPanel);

        executionPanel.setLayout(new java.awt.CardLayout());

        resultContainerPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(statusLabel, org.openide.util.NbBundle.getMessage(JPQLEditorTopComponent.class, "JPQLEditorTopComponent.statusLabel.text")); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 607, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(statusPanelLayout.createSequentialGroup()
                    .addGap(0, 303, Short.MAX_VALUE)
                    .addComponent(statusLabel)
                    .addGap(0, 304, Short.MAX_VALUE)))
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(statusPanelLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(statusLabel)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        resultContainerPanel.add(statusPanel, java.awt.BorderLayout.NORTH);

        resultsOrErrorPanel.setLayout(new java.awt.CardLayout());

        errorTextArea.setColumns(20);
        errorTextArea.setEditable(false);
        errorTextArea.setForeground(new java.awt.Color(255, 102, 102));
        errorTextArea.setRows(5);
        jScrollPane4.setViewportView(errorTextArea);

        resultsOrErrorPanel.add(jScrollPane4, "card2");

        resultsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        resultsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane3.setViewportView(resultsTable);

        resultsOrErrorPanel.add(jScrollPane3, "card3");

        resultContainerPanel.add(resultsOrErrorPanel, java.awt.BorderLayout.CENTER);

        executionPanel.add(resultContainerPanel, "card2");

        sqlEditorPane.setEditable(false);
        jScrollPane2.setViewportView(sqlEditorPane);

        executionPanel.add(jScrollPane2, "card1");

        javax.swing.GroupLayout containerPanelLayout = new javax.swing.GroupLayout(containerPanel);
        containerPanel.setLayout(containerPanelLayout);
        containerPanelLayout.setHorizontalGroup(
            containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(executionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        containerPanelLayout.setVerticalGroup(
            containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerPanelLayout.createSequentialGroup()
                .addComponent(toolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(executionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE))
        );

        splitPane.setRightComponent(containerPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(splitPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private int getMaxRowCount() {
        String selectedMaxCount = setMaxRowCountComboBox.getSelectedItem().toString();
        try {
            return Integer.parseInt(selectedMaxCount);
        } catch (NumberFormatException e) {
            logger.warning("Number Format Error during parsing the max. row count");
        }
        return 1000; // Optimum value.
    }

private void resultToggleButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_resultToggleButtonItemStateChanged
    if (resultToggleButton.isSelected()) {//GEN-LAST:event_resultToggleButtonItemStateChanged
            ((CardLayout) (executionPanel.getLayout())).first(executionPanel);
            sqlToggleButton.setSelected(false);
        }
    }

private void sqlToggleButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_sqlToggleButtonItemStateChanged
    if (sqlToggleButton.isSelected()) {//GEN-HEADEREND:event_sqlToggleButtonItemStateChanged
        ((CardLayout) (executionPanel.getLayout())).last(executionPanel);//GEN-LAST:event_sqlToggleButtonItemStateChanged
            resultToggleButton.setSelected(false);
        }
    }

private void runJPQLButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runJPQLButtonActionPerformed
    // Fix - 138856
    if(jpqlEditor.getText().trim().equals("")) {
        switchToResultView();
        setStatus(NbBundle.getMessage(JPQLEditorTopComponent.class, "emptyQuery"));
        return;
    }
    if (puComboBox.getSelectedItem() == null) {
        logger.info("hibernate configuration combo box is empty.");
        switchToResultView();
        setStatus(NbBundle.getMessage(JPQLEditorTopComponent.class, "emptyConfiguration"));
        return;
    }
    runJPQLButton.setEnabled(false);                                            
    try {
        ph = ProgressHandleFactory.createHandle(//GEN-HEADEREND:event_runJPQLButtonActionPerformed
                NbBundle.getMessage(JPQLEditorTopComponent.class, "progressTaskname"));//GEN-LAST:event_runJPQLButtonActionPerformed
            isSqlTranslationProcessDone = true;//will be reparsed in execution thread
            if (hqlParserTask != null && !hqlParserTask.isFinished() && (hqlParserTask.getDelay() != 0)) {
                hqlParserTask.cancel();
            }
            FileObject pXml = puObject.getPrimaryFile();
            Project project = pXml != null ? FileOwnerQuery.getOwner(pXml) : null;
            PersistenceEnvironment pe = project != null ? project.getLookup().lookup(PersistenceEnvironment.class) : null;

            PersistenceUnit pu = (PersistenceUnit) puConfigMap.get(puComboBox.getSelectedItem());
            ph.start(100);
            controller.executeJPQLQuery(jpqlEditor.getText(),
                    pu,
                    pe,
                    getMaxRowCount(),
                    ph);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel containerPanel;
    private javax.swing.JTextArea errorTextArea;
    private javax.swing.JPanel executionPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JEditorPane jpqlEditor;
    private javax.swing.JComboBox puComboBox;
    private javax.swing.JPanel resultContainerPanel;
    private javax.swing.JToggleButton resultToggleButton;
    private javax.swing.JPanel resultsOrErrorPanel;
    private javax.swing.JTable resultsTable;
    private javax.swing.JButton runJPQLButton;
    private javax.swing.JLabel sessionLabel;
    private javax.swing.JComboBox setMaxRowCountComboBox;
    private javax.swing.JLabel setMaxRowCountLabel;
    private javax.swing.JPanel setMaxRowCountPanel;
    private javax.swing.JPanel spacerPanel1;
    private javax.swing.JPanel spacerPanel2;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JTextPane sqlEditorPane;
    private javax.swing.JToggleButton sqlToggleButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JToolBar toolBar2;
    private javax.swing.JToolBar.Separator toolbarSeparator;
    private javax.swing.JToolBar.Separator toolbarSeparator1;
    // End of variables declaration//GEN-END:variables

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    protected void componentClosed() {
        windowCounts.remove(thisWindowCount);
    }

    private void switchToResultView() {
        resultToggleButton.setSelected(true);
        ((CardLayout) resultsOrErrorPanel.getLayout()).last(resultsOrErrorPanel);
    }

    private void switchToSQLView() {
        sqlToggleButton.setSelected(true);
    }

    private void switchToErrorView() {
        resultToggleButton.setSelected(true);
        ((CardLayout) resultsOrErrorPanel.getLayout()).first(resultsOrErrorPanel);
    }

    private void puComboboxActionPerformed() {
        if (puComboBox.getSelectedItem() != null) {
            FileObject pXml = puObject.getPrimaryFile();
            Project project = pXml != null ? FileOwnerQuery.getOwner(pXml) : null;
            PersistenceEnvironment pe = project != null ? project.getLookup().lookup(PersistenceEnvironment.class) : null;

            PersistenceUnit pu = (PersistenceUnit) puConfigMap.get(puComboBox.getSelectedItem());
            dbconn = JPAEditorUtil.findDatabaseConnection(pu, pe.getProject());
            if (dbconn != null) {
                if (dbconn.getJDBCConnection() == null) {
                    Mutex.EVENT.readAccess( (Mutex.Action<DatabaseConnection>) () -> {
                        ConnectionManager.getDefault().showConnectionDialog(dbconn);
                        return dbconn;
                    });
                }
            } else {
                //
            }
        }
    }
}

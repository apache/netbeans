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
package org.netbeans.modules.php.project.connections.sync;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.sync.diff.DiffPanel;
import org.netbeans.modules.php.project.ui.HintArea;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 * Panel for remote synchronization.
 */
@org.netbeans.api.annotations.common.SuppressWarnings("SE_BAD_FIELD_STORE")
public final class SyncPanel extends JPanel implements HelpCtx.Provider {

    private static final long serialVersionUID = 1674646546545121L;

    static final Logger LOGGER = Logger.getLogger(SyncPanel.class.getName());

    @StaticResource
    private static final String DIFF_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/diff.png"; // NOI18N
    @StaticResource
    private static final String RESET_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/reset.png"; // NOI18N
    @StaticResource
    private static final String ERROR_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/error.png"; // NOI18N
    @StaticResource
    private static final String WARNING_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/warning.gif"; // NOI18N
    @StaticResource
    private static final String HORIZONTAL_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/horizontal.png"; // NOI18N
    @StaticResource
    private static final String HEADER_INFO_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/header_info.png"; // NOI18N

    static final RequestProcessor RP = new RequestProcessor("PHP: Sync items validation & information"); // NOI18N

    // @GuardedBy(AWT)
    private static final List<SyncItem.Operation> OPERATIONS = Arrays.asList(
                SyncItem.Operation.NOOP,
                SyncItem.Operation.DOWNLOAD,
                SyncItem.Operation.UPLOAD,
                SyncItem.Operation.DELETE);

    final RemoteClient remoteClient;
    final List<SyncItem> allItems;
    // @GuardedBy(AWT)
    final List<SyncItem> displayedItems;
    // @GuardedBy(AWT)
    final FileTableModel tableModel;
    // @GuardedBy(AWT)
    final JPopupMenu headerPopupMenu = new JPopupMenu();
    // @GuardedBy(AWT)
    final JPopupMenu popupMenu = new JPopupMenu();

    // @GuardedBy(AWT)
    boolean remotePathFirst = true;
    // @GuardedBy(AWT)
    boolean selectionIsAdjusting = false;
    // @GuardedBy(AWT)
    Point popupMenuPoint = new Point(); // XXX is there a better way?
    // @GuardedBy(AWT)
    List<? extends RowSorter.SortKey> sortKeys = Collections.singletonList(new RowSorter.SortKey(1, SortOrder.ASCENDING));

    private final PhpProject project;
    private final String remoteConfigurationName;
    private final String defaultInfoMessage;
    // @GuardedBy(AWT)
    private final List<ViewCheckBox> viewCheckBoxes;
    // @GuardedBy(AWT)
    private final ItemListener viewListener = new ViewListener();

    // @GuardedBy(AWT)
    private PopupMenuListener popupMenuListener;
    // @GuardedBy(AWT)
    private DialogDescriptor descriptor = null;
    // @GuardedBy(AWT)
    private Boolean rememberShowSummary = null;
    // @GuardedBy(AWT)
    private JButton okButton = null;


    SyncPanel(PhpProject project, String remoteConfigurationName, List<SyncItem> items, RemoteClient remoteClient,
            SyncController.SourceFiles sourceFiles) {
        assert SwingUtilities.isEventDispatchThread();
        assert items != null;

        this.project = project;
        this.remoteConfigurationName = remoteConfigurationName;
        this.allItems = new CopyOnWriteArrayList<>(items);
        displayedItems = new ArrayList<>(items);
        this.remoteClient = remoteClient;
        tableModel = new FileTableModel(displayedItems);
        defaultInfoMessage = getDefaultInfoMessage(items, sourceFiles);

        initComponents();
        viewCheckBoxes = getViewCheckBoxes();
        initViewCheckBoxes();
        initViewButtons();
        initTable();
        initOperationButtons();
        initDiffButton();
        initMessages();
        initShowSummaryCheckBox(sourceFiles == SyncController.SourceFiles.PROJECT);
    }

    private JCheckBox createViewCheckBox() {
        ViewCheckBox viewCheckBox = new ViewCheckBox();
        viewCheckBox.setSelected(true);
        viewCheckBox.addItemListener(viewListener);
        return viewCheckBox;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.project.connections.sync.SyncPanel"); // NOI18N
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "# {1} - remote configuration name",
        "SyncPanel.title=Remote Synchronization for {0}: {1}",
        "SyncPanel.button.titleWithMnemonics=S&ynchronize",
        "SyncPanel.collectingInformation=Collecting information, please wait..."
    })
    public boolean open() {
        assert SwingUtilities.isEventDispatchThread();
        okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, Bundle.SyncPanel_button_titleWithMnemonics());
        descriptor = new DialogDescriptor(
                this,
                Bundle.SyncPanel_title(project.getName(), remoteConfigurationName),
                true,
                new Object[] {okButton, DialogDescriptor.CANCEL_OPTION},
                okButton,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        okButton.addActionListener(new OkActionListener(dialog));
        descriptor.setClosingOptions(new Object[] {NotifyDescriptor.CANCEL_OPTION});
        descriptor.setAdditionalOptions(new Object[] {showSummaryCheckBox});
        validateItems();
        updateSyncInfo();
        boolean okPressed;
        try {
            dialog.setVisible(true);
            okPressed = descriptor.getValue() == okButton;
        } finally {
            dialog.dispose();
        }
        return okPressed;
    }

    public List<SyncItem> getItems() {
        assert SwingUtilities.isEventDispatchThread();
        return new ArrayList<>(allItems);
    }

    private List<ViewCheckBox> getViewCheckBoxes() {
        return Arrays.asList(
                (ViewCheckBox) viewNoopCheckBox,
                (ViewCheckBox) viewDownloadCheckBox,
                (ViewCheckBox) viewUploadCheckBox,
                (ViewCheckBox) viewDeleteCheckBox,
                (ViewCheckBox) viewSymlinkCheckBox,
                (ViewCheckBox) viewFileDirCollisionCheckBox,
                (ViewCheckBox) viewFileConflictCheckBox,
                (ViewCheckBox) viewWarningCheckBox,
                (ViewCheckBox) viewErrorCheckBox);
    }

    @NbBundle.Messages({
        "SyncPanel.view.warning=W&arning",
        "SyncPanel.view.error=E&rror"
    })
    private void initViewCheckBoxes() {
        // operations
        initViewCheckBox(viewNoopCheckBox, SyncItem.Operation.NOOP);
        initViewCheckBox(viewDownloadCheckBox, EnumSet.of(SyncItem.Operation.DOWNLOAD, SyncItem.Operation.DOWNLOAD_REVIEW));
        initViewCheckBox(viewUploadCheckBox, EnumSet.of(SyncItem.Operation.UPLOAD, SyncItem.Operation.UPLOAD_REVIEW));
        initViewCheckBox(viewDeleteCheckBox, SyncItem.Operation.DELETE);
        initViewCheckBox(viewSymlinkCheckBox, SyncItem.Operation.SYMLINK);
        initViewCheckBox(viewFileDirCollisionCheckBox, SyncItem.Operation.FILE_DIR_COLLISION);
        initViewCheckBox(viewFileConflictCheckBox, SyncItem.Operation.FILE_CONFLICT);
        // warnings & errors
        initViewCheckBox(viewWarningCheckBox, Bundle.SyncPanel_view_warning());
        initViewCheckBox(viewErrorCheckBox, Bundle.SyncPanel_view_error());
        ((ViewCheckBox) viewWarningCheckBox).setFilter(new SyncItemFilter() {
            @Override
            public boolean accept(SyncItem syncItem) {
                return syncItem.validate().hasWarning();
            }
        });
        ((ViewCheckBox) viewErrorCheckBox).setFilter(new SyncItemFilter() {
            @Override
            public boolean accept(SyncItem syncItem) {
                return syncItem.validate().hasError();
            }
        });
    }

    private void initViewCheckBox(JCheckBox checkBox, SyncItem.Operation operation) {
        initViewCheckBox(checkBox, EnumSet.of(operation));
    }

    private void initViewCheckBox(JCheckBox checkBox, final EnumSet<SyncItem.Operation> operations) {
        SyncItem.Operation operation = operations.iterator().next();
        initViewCheckBox(checkBox, operation.getTitleWithMnemonic());
        ((ViewCheckBox) checkBox).setFilter(new SyncItemFilter() {
            @Override
            public boolean accept(SyncItem syncItem) {
                return operations.contains(syncItem.getOperation());
            }
        });
    }

    private void initViewCheckBox(JCheckBox checkBox, String titleWithMnemonic) {
        Mnemonics.setLocalizedText(checkBox, titleWithMnemonic);
    }

    private void initViewButtons() {
        checkAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setViewCheckBoxesSelected(true);
            }
        });
        uncheckAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setViewCheckBoxesSelected(false);
            }
        });
    }

    private void initTable() {
        assert SwingUtilities.isEventDispatchThread();
        initTableModel();
        initTableSorter();
        initTableRenderers();
        initTableHeader();
        initTableRows();
        initTableColumns();
        initTableSelections();
        initTableActions();
        initTableHeaderPopupMenu();
        initTablePopupMenu();
    }

    private void reinitTable() {
        assert SwingUtilities.isEventDispatchThread();
        initTableSorter();
        initTableHeader();
        initTableRows();
        initTableColumns();
        initTableHeaderPopupMenu();
        initTablePopupMenu();
    }

    private void initTableModel() {
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                validateItems();
                updateSyncInfo();
            }
        });
        itemTable.setModel(tableModel);
    }

    private void initTableSorter() {
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
        itemTable.setRowSorter(sorter);
        // default sort keys
        sorter.setSortKeys(sortKeys);
        // sorting
        sorter.setComparator(0, new SyncItemImageIconComparator());
        sorter.setSortable(2, false);
    }

    private void initTableRenderers() {
        itemTable.setDefaultRenderer(Icon.class, new IconRenderer());
        itemTable.setDefaultRenderer(String.class, new StringRenderer());
        itemTable.setDefaultRenderer(SyncItem.Operation.class, new OperationRenderer());
    }

    @NbBundle.Messages({
        "SyncPanel.table.header.info.toolTip=Click to sort by Information",
        "SyncPanel.table.header.remotePath.toolTip=Click to sort by Remote Path",
        "SyncPanel.table.header.localPath.toolTip=Click to sort by Local Path",
        "SyncPanel.table.header.operation.toolTip=Click to swap Remote Path and Local Path"
    })
    private void initTableHeader() {
        JTableHeader header = itemTable.getTableHeader();
        header.setPreferredSize(new Dimension(itemTable.getColumnModel().getTotalColumnWidth(), Math.max(20, itemTable.getFont().getSize() + 5)));
        header.setReorderingAllowed(false);
        // columns
        TableColumn infoColumn = header.getColumnModel().getColumn(0);
        infoColumn.setHeaderRenderer(new HeaderRenderer(Bundle.SyncPanel_table_header_info_toolTip()));
        infoColumn.setHeaderValue(ImageUtilities.loadImageIcon(HEADER_INFO_ICON_PATH, false));
        TableColumn operationColumn = header.getColumnModel().getColumn(2);
        operationColumn.setHeaderRenderer(new HeaderRenderer(Bundle.SyncPanel_table_header_operation_toolTip()));
        operationColumn.setHeaderValue(ImageUtilities.loadImageIcon(HORIZONTAL_ICON_PATH, false));
        TableColumn remotePathColumn = header.getColumnModel().getColumn(remotePathFirst ? 1 : 3);
        remotePathColumn.setHeaderRenderer(new HeaderRenderer(Bundle.SyncPanel_table_header_remotePath_toolTip()));
        TableColumn localPathColumn = header.getColumnModel().getColumn(remotePathFirst ? 3 : 1);
        localPathColumn.setHeaderRenderer(new HeaderRenderer(Bundle.SyncPanel_table_header_localPath_toolTip()));
        // listener
        itemTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (itemTable.columnAtPoint(e.getPoint()) == 2) {
                    swapPaths();
                }
            }
        });
    }

    private void initTableRows() {
        itemTable.setRowHeight(Math.max(20, itemTable.getFont().getSize() + 5));
    }

    private void initTableColumns() {
        TableColumnModel columnModel = itemTable.getColumnModel();
        columnModel.getColumn(0).setMinWidth(20);
        columnModel.getColumn(0).setMaxWidth(20);
        columnModel.getColumn(0).setResizable(false);
        columnModel.getColumn(1).setPreferredWidth(1000);
        columnModel.getColumn(2).setMinWidth(40);
        columnModel.getColumn(2).setPreferredWidth(40);
        columnModel.getColumn(3).setPreferredWidth(1000);
    }

    private void initTableSelections() {
        itemTable.setColumnSelectionAllowed(false);
        itemTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting()) {
                    return;
                }
                if (selectionIsAdjusting) {
                    return;
                }
                updateSyncInfo();
                setEnabledOperationButtons();
                setEnabledDiffButton();
            }
        });
    }

    private void initTableActions() {
        itemTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }
            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    headerPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        itemTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    // cycle operations?
                    if (itemTable.getSelectedColumn() == 2) {
                        cycleOperations();
                    }
                } else if (e.getClickCount() == 2
                        && isDiffActionPossible(false)) {
                    openDiffPanel();
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }
            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenuPoint.x = e.getX();
                    popupMenuPoint.y = e.getY();
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    @NbBundle.Messages({
        "SyncPanel.popupMenu.swapPaths=Swap Local and Remote Columns",
        "SyncPanel.popupMenu.sort.local.asc=Sort Ascending By Local Path",
        "SyncPanel.popupMenu.sort.remote.asc=Sort Ascending By Remote Path",
        "SyncPanel.popupMenu.sort.local.desc=Sort Descending By Local Path",
        "SyncPanel.popupMenu.sort.remote.desc=Sort Descending By Remote Path",
        "SyncPanel.popupMenu.sort.info=Sort By Info"
    })
    private void initTableHeaderPopupMenu() {
        assert SwingUtilities.isEventDispatchThread();
        // cleanup
        headerPopupMenu.removeAll();
        // swap
        JMenuItem swapMenuItem = new JMenuItem(Bundle.SyncPanel_popupMenu_swapPaths());
        swapMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                swapPaths();
            }
        });
        headerPopupMenu.add(swapMenuItem);
        headerPopupMenu.addSeparator();
        // sort
        JMenuItem sortLocalAscMenuItem = new JMenuItem(Bundle.SyncPanel_popupMenu_sort_local_asc());
        sortLocalAscMenuItem.addActionListener(new SortPopupMenuItemListener(remotePathFirst ? 3 : 1, SortOrder.ASCENDING));
        headerPopupMenu.add(sortLocalAscMenuItem);
        JMenuItem sortRemoteAscMenuItem = new JMenuItem(Bundle.SyncPanel_popupMenu_sort_remote_asc());
        sortRemoteAscMenuItem.addActionListener(new SortPopupMenuItemListener(remotePathFirst ? 1 : 3, SortOrder.ASCENDING));
        headerPopupMenu.add(sortRemoteAscMenuItem);
        JMenuItem sortLocalDescMenuItem = new JMenuItem(Bundle.SyncPanel_popupMenu_sort_local_desc());
        sortLocalDescMenuItem.addActionListener(new SortPopupMenuItemListener(remotePathFirst ? 3 : 1, SortOrder.DESCENDING));
        headerPopupMenu.add(sortLocalDescMenuItem);
        JMenuItem sortRemoteDescMenuItem = new JMenuItem(Bundle.SyncPanel_popupMenu_sort_remote_desc());
        sortRemoteDescMenuItem.addActionListener(new SortPopupMenuItemListener(remotePathFirst ? 1 : 3, SortOrder.DESCENDING));
        headerPopupMenu.add(sortRemoteDescMenuItem);
        JMenuItem sortInfoMenuItem = new JMenuItem(Bundle.SyncPanel_popupMenu_sort_info());
        sortInfoMenuItem.addActionListener(new SortPopupMenuItemListener(0, SortOrder.DESCENDING));
        headerPopupMenu.add(sortInfoMenuItem);
    }

    @NbBundle.Messages({
        "SyncPanel.popupMenu.resetItem=Reset Operation",
        "SyncPanel.popupMenu.disable.download=Disable Downloads",
        "SyncPanel.popupMenu.disable.upload=Disable Uploads",
        "SyncPanel.popupMenu.disable.delete=Disable Deletions",
        "SyncPanel.popupMenu.diffItem=Diff..."
    })
    private void initTablePopupMenu() {
        assert SwingUtilities.isEventDispatchThread();
        // cleanup
        popupMenu.removeAll();
        if (popupMenuListener != null) {
            popupMenu.removePopupMenuListener(popupMenuListener);
        }
        // reset
        final JMenuItem resetMenuItem = new JMenuItem(Bundle.SyncPanel_popupMenu_resetItem(), ImageUtilities.loadImageIcon(RESET_ICON_PATH, false));
        resetMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<SyncItem> selectedItems = getSelectedItems(true);
                for (SyncItem item : selectedItems) {
                    if (item.isOperationChangePossible()) {
                        item.resetOperation();
                    }
                }
                updateDisplayedItems();
                reselectItems(selectedItems);
            }
        });
        popupMenu.add(resetMenuItem);
        popupMenu.addSeparator();
        // set operations
        // - noop
        final JMenuItem operationNoopMenuItem = new JMenuItem(SyncItem.Operation.NOOP.getToolTip(), SyncItem.Operation.NOOP.getIcon(!remotePathFirst));
        operationNoopMenuItem.addActionListener(new PopupMenuItemListener(SyncItem.Operation.NOOP));
        popupMenu.add(operationNoopMenuItem);
        // - noop
        final JMenuItem operationDownloadMenuItem = new JMenuItem(SyncItem.Operation.DOWNLOAD.getToolTip(), SyncItem.Operation.DOWNLOAD.getIcon(!remotePathFirst));
        operationDownloadMenuItem.addActionListener(new PopupMenuItemListener(SyncItem.Operation.DOWNLOAD));
        popupMenu.add(operationDownloadMenuItem);
        // - noop
        final JMenuItem operationUploadMenuItem = new JMenuItem(SyncItem.Operation.UPLOAD.getToolTip(), SyncItem.Operation.UPLOAD.getIcon(!remotePathFirst));
        operationUploadMenuItem.addActionListener(new PopupMenuItemListener(SyncItem.Operation.UPLOAD));
        popupMenu.add(operationUploadMenuItem);
        // - noop
        final JMenuItem operationDeleteMenuItem = new JMenuItem(SyncItem.Operation.DELETE.getToolTip(), SyncItem.Operation.DELETE.getIcon(!remotePathFirst));
        operationDeleteMenuItem.addActionListener(new PopupMenuItemListener(SyncItem.Operation.DELETE));
        popupMenu.add(operationDeleteMenuItem);
        popupMenu.addSeparator();
        // disable operations
        // - downloads
        final JMenuItem disableDownloadsMenuItem = new JMenuItem(Bundle.SyncPanel_popupMenu_disable_download());
        disableDownloadsMenuItem.addActionListener(new PopupMenuItemListener(Arrays.asList(SyncItem.Operation.DOWNLOAD, SyncItem.Operation.DOWNLOAD_REVIEW), SyncItem.Operation.NOOP));
        popupMenu.add(disableDownloadsMenuItem);
        // - uploads
        final JMenuItem disableUploadsMenuItem = new JMenuItem(Bundle.SyncPanel_popupMenu_disable_upload());
        disableUploadsMenuItem.addActionListener(new PopupMenuItemListener(Arrays.asList(SyncItem.Operation.UPLOAD, SyncItem.Operation.UPLOAD_REVIEW), SyncItem.Operation.NOOP));
        popupMenu.add(disableUploadsMenuItem);
        // - deletions
        final JMenuItem disableDeletionsMenuItem = new JMenuItem(Bundle.SyncPanel_popupMenu_disable_delete());
        disableDeletionsMenuItem.addActionListener(new PopupMenuItemListener(SyncItem.Operation.DELETE, SyncItem.Operation.NOOP));
        popupMenu.add(disableDeletionsMenuItem);
        popupMenu.addSeparator();
        // diff
        final JMenuItem diffMenuItem = new JMenuItem(Bundle.SyncPanel_popupMenu_diffItem(), ImageUtilities.loadImageIcon(DIFF_ICON_PATH, false));
        diffMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openDiffPanel();
            }
        });
        popupMenu.add(diffMenuItem);
        // listener
        popupMenuListener = new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                // enable/disable actions
                boolean operationChangesPossible = areOperationChangesPossible(getSelectedItems(true));
                resetMenuItem.setEnabled(operationChangesPossible);
                operationNoopMenuItem.setEnabled(operationChangesPossible);
                operationDownloadMenuItem.setEnabled(operationChangesPossible);
                operationUploadMenuItem.setEnabled(operationChangesPossible);
                operationDeleteMenuItem.setEnabled(operationChangesPossible);
                disableDownloadsMenuItem.setEnabled(operationChangesPossible);
                disableUploadsMenuItem.setEnabled(operationChangesPossible);
                disableDeletionsMenuItem.setEnabled(operationChangesPossible);
                diffMenuItem.setEnabled(isDiffActionPossible(true));
            }
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // noop
            }
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // noop
            }
        };
        popupMenu.addPopupMenuListener(popupMenuListener);
    }

    private void initOperationButtons() {
        // operations
        initOperationButton(noopButton, SyncItem.Operation.NOOP);
        initOperationButton(downloadButton, SyncItem.Operation.DOWNLOAD);
        initOperationButton(uploadButton, SyncItem.Operation.UPLOAD);
        initOperationButton(deleteButton, SyncItem.Operation.DELETE);
        // reset
        initResetButton();
    }

    private void initOperationButton(JButton button, SyncItem.Operation operation) {
        button.setText(null);
        button.setIcon(operation.getIcon(!remotePathFirst));
        button.setToolTipText(operation.getToolTip());
        button.addActionListener(new OperationButtonListener(operation));
    }

    private void reinitOperationButtons() {
        reinitOperationButton(noopButton, SyncItem.Operation.NOOP);
        reinitOperationButton(downloadButton, SyncItem.Operation.DOWNLOAD);
        reinitOperationButton(uploadButton, SyncItem.Operation.UPLOAD);
        reinitOperationButton(deleteButton, SyncItem.Operation.DELETE);
    }

    private void reinitOperationButton(JButton button, SyncItem.Operation operation) {
        button.setIcon(operation.getIcon(!remotePathFirst));
    }

    @NbBundle.Messages("SyncPanel.resetButton.toolTip=Reset to suggested operation (discards Diff changes)")
    private void initResetButton() {
        resetButton.setText(null);
        resetButton.setIcon(ImageUtilities.loadImageIcon(RESET_ICON_PATH, false));
        resetButton.setToolTipText(Bundle.SyncPanel_resetButton_toolTip());
        resetButton.addActionListener(new OperationButtonListener(null));
    }

    @NbBundle.Messages("SyncPanel.diffButton.toolTip=Review differences between remote and local file")
    private void initDiffButton() {
        diffButton.setText(null);
        diffButton.setIcon(ImageUtilities.loadImageIcon(DIFF_ICON_PATH, false));
        diffButton.setToolTipText(Bundle.SyncPanel_diffButton_toolTip());
        diffButton.addActionListener(new DiffActionListener());
    }

    private void initMessages() {
        // sync info
        syncInfoPanel.setBackground(Utils.getHintBackground());
        syncInfoPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, UIManager.getColor("Table.gridColor"))); // NOI18N
        // messages
        messagesTextPane.setText(defaultInfoMessage);
    }

    @NbBundle.Messages({
        "SyncPanel.info.firstRun=<strong>First time for this directory and configuration - more user actions may be needed.</strong><br>",
        "SyncPanel.info.files=Suggested operation for individual files may not be correct (synchronize directory instead).<br>",
        "SyncPanel.info.warning=Review all suggested operations before proceeding. Note that remote timestamps may not be correct.",
    })
    private String getDefaultInfoMessage(List<SyncItem> items, SyncController.SourceFiles sourceFiles) {
        StringBuilder msg = new StringBuilder();
        boolean firstRun = false;
        for (SyncItem item : items) {
            if (!item.hasLastTimestamp()) {
                firstRun = true;
                break;
            }
        }
        if (firstRun) {
            msg.append(Bundle.SyncPanel_info_firstRun());
        }
        if (sourceFiles == SyncController.SourceFiles.INDIVIDUAL_FILES) {
            msg.append(Bundle.SyncPanel_info_files());
        }
        msg.append(Bundle.SyncPanel_info_warning());
        return msg.toString();
    }

    private void initShowSummaryCheckBox(boolean showSummary) {
        if (!showSummary) {
            showSummaryCheckBox.setVisible(false);
            return;
        }
        showSummaryCheckBox.setSelected(PhpOptions.getInstance().getRemoteSyncShowSummary());
        showSummaryCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                rememberShowSummary = e.getStateChange() == ItemEvent.SELECTED;
            }
        });
    }

    void setEnabledOperationButtons() {
        boolean enabled = areOperationChangesPossible(getSelectedItems(false));
        noopButton.setEnabled(enabled);
        downloadButton.setEnabled(enabled);
        uploadButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
    }

    private boolean areOperationChangesPossible(List<SyncItem> selectedItems) {
        assert SwingUtilities.isEventDispatchThread();
        if (selectedItems.isEmpty()) {
            return false;
        }
        for (SyncItem item : selectedItems) {
            if (item.isOperationChangePossible()) {
                return true;
            }
        }
        return false;
    }

    boolean isDiffActionPossible(boolean includingMousePosition) {
        List<SyncItem> selectedItems = getSelectedItems(includingMousePosition);
        if (selectedItems.size() != 1) {
            return false;
        }
        return selectedItems.get(0).isDiffPossible();
    }

    void setEnabledDiffButton() {
        diffButton.setEnabled(isDiffActionPossible(false));
    }

    SyncItem getSelectedItem(boolean includingMousePosition) {
        assert SwingUtilities.isEventDispatchThread();
        List<SyncItem> selectedItems = getSelectedItems(includingMousePosition);
        if (selectedItems.size() == 1) {
            return selectedItems.get(0);
        }
        return null;
    }

    void reselectItem(SyncItem syncItem) {
        assert SwingUtilities.isEventDispatchThread();
        assert itemTable.getSelectedRowCount() <= 1 : "Selected rows in table: " + itemTable.getSelectedRowCount();
        Integer index = itemsToIndex(displayedItems).get(syncItem);
        if (index != null) {
            index = itemTable.convertRowIndexToView(index);
            itemTable.getSelectionModel().setSelectionInterval(index, index);
        }
    }

    List<SyncItem> getSelectedItems(boolean includingMousePosition) {
        assert SwingUtilities.isEventDispatchThread();
        int[] selectedRows = itemTable.getSelectedRows();
        if (selectedRows.length == 0) {
            if (includingMousePosition) {
                return Collections.singletonList(displayedItems.get(itemTable.convertRowIndexToModel(itemTable.rowAtPoint(popupMenuPoint))));
            }
            return Collections.emptyList();
        }
        List<SyncItem> selectedItems = new ArrayList<>(selectedRows.length);
        for (int index : selectedRows) {
            SyncItem syncItem = displayedItems.get(itemTable.convertRowIndexToModel(index));
            selectedItems.add(syncItem);
        }
        return selectedItems;
    }

    void reselectItems(List<SyncItem> selectedItems) {
        assert SwingUtilities.isEventDispatchThread();
        List<Integer> selectedRows = new ArrayList<>(selectedItems.size());
        Map<SyncItem, Integer> itemsToIndex = itemsToIndex(displayedItems);
        if (!itemsToIndex.isEmpty()) {
            for (SyncItem item : selectedItems) {
                Integer index = itemsToIndex.get(item);
                if (index != null) {
                    selectedRows.add(itemTable.convertRowIndexToView(index));
                }
            }
        }
        // #212269 - minimize ui refreshes
        selectionIsAdjusting = true;
        Iterator<Pair<Integer, Integer>> iterator = PhpProjectUtils.getIntervals(selectedRows).iterator();
        while (iterator.hasNext()) {
            Pair<Integer, Integer> pair = iterator.next();
            if (!iterator.hasNext()) {
                selectionIsAdjusting = false;
            }
            itemTable.getSelectionModel().addSelectionInterval(pair.first(), pair.second());
        }
        selectionIsAdjusting = false;
    }

    @NbBundle.Messages({
        "SyncPanel.error.operations=Synchronization not possible. Resolve conflicts first.",
        "SyncPanel.warn.operations=Synchronization possible but warnings should be reviewed first."
    })
    void validateItems() {
        assert SwingUtilities.isEventDispatchThread();
        invalidatePanel();
        RP.post(new Runnable() {
            @Override
            public void run() {
                boolean warn = false;
                for (SyncItem syncItem : allItems) {
                    SyncItem.ValidationResult validationResult = syncItem.validate();
                    if (validationResult.hasError()) {
                        setError(Bundle.SyncPanel_error_operations());
                        return;
                    }
                    if (validationResult.hasWarning()) {
                        warn = true;
                    }
                }
                if (warn) {
                    setWarning(Bundle.SyncPanel_warn_operations());
                } else {
                    clearError();
                }
            }
        });
    }

    void setError(final String error) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                String msg = getImgTag(ERROR_ICON_PATH) + getColoredText(error, UIManager.getColor("nb.errorForeground")) + "<br>" + defaultInfoMessage; // NOI18N
                messagesTextPane.setText(msg);
                descriptor.setValid(false);
                okButton.setEnabled(false);
            }
        });
    }

    void setWarning(final String warning) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                String msg = getImgTag(WARNING_ICON_PATH) + getColoredText(warning, UIManager.getColor("nb.warningForeground")) + "<br>" + defaultInfoMessage; // NOI18N
                messagesTextPane.setText(msg);
                descriptor.setValid(true);
                okButton.setEnabled(true);
            }
        });
    }

    void clearError() {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                messagesTextPane.setText(defaultInfoMessage);
                descriptor.setValid(true);
                okButton.setEnabled(true);
            }
        });
    }

    void invalidatePanel() {
        assert SwingUtilities.isEventDispatchThread();
        messagesTextPane.setText(Bundle.SyncPanel_collectingInformation());
        descriptor.setValid(false);
        okButton.setEnabled(false);
    }

    private String getImgTag(String src) {
        return "<img src=\"" + SyncPanel.class.getClassLoader().getResource(src).toExternalForm() + "\">&nbsp;"; // NOI18N
    }

    private String getColoredText(String text, Color color) {
        String colorText = "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")"; // NOI18N
        return "<span style=\"color: " + colorText + ";\">" + text + "</span>"; // NOI18N
    }

    @NbBundle.Messages({
        "# {0} - synchronization info",
        "SyncPanel.info.prefix.all=All: {0}",
        "# {0} - synchronization info",
        "SyncPanel.info.prefix.selection=Selection: {0}",
        "# {0} - file name",
        "# {1} - message",
        "SyncPanel.info.prefix.error=Error ({0}): {1}",
        "# {0} - file name",
        "# {1} - message",
        "SyncPanel.info.prefix.warning=Warning ({0}): {1}",
        "# {0} - number of files to be downloaded",
        "# {1} - number of files to be uploaded",
        "# {2} - number of files to be deleted",
        "# {3} - number of files without any operation",
        "# {4} - number of files with errors",
        "# {5} - number of files with warnings",
        "SyncPanel.info.status={0} downloads, {1} uploads, {2} deletions, "
            + "{3} no-ops, {4} errors, {5} warnings."
    })
    void updateSyncInfo() {
        assert SwingUtilities.isEventDispatchThread();
        syncInfoLabel.setText(Bundle.SyncPanel_collectingInformation());
        final List<SyncItem> selectedItems = new CopyOnWriteArrayList<>(getSelectedItems(false));
        RP.post(new Runnable() {
            @Override
            public void run() {
                List<SyncItem> selectedItemsCopy = selectedItems;
                if (selectedItemsCopy.size() == 1) {
                    final SyncItem syncItem = selectedItemsCopy.get(0);
                    final SyncItem.ValidationResult result = syncItem.validate();
                    if (result.hasError()) {
                        Mutex.EVENT.readAccess(new Runnable() {
                            @Override
                            public void run() {
                                syncInfoLabel.setForeground(UIManager.getColor("nb.errorForeground")); // NOI18N
                                syncInfoLabel.setText(Bundle.SyncPanel_info_prefix_error(syncItem.getName(), result.getMessage()));
                            }
                        });
                        return;
                    }
                    if (result.hasWarning()) {
                        Mutex.EVENT.readAccess(new Runnable() {
                            @Override
                            public void run() {
                                syncInfoLabel.setForeground(UIManager.getColor("nb.warningForeground")); // NOI18N
                                syncInfoLabel.setText(Bundle.SyncPanel_info_prefix_warning(syncItem.getName(), result.getMessage()));
                            }
                        });
                        return;
                    }
                    selectedItemsCopy.clear();
                }
                // all or selection
                boolean all = false;
                if (selectedItemsCopy.isEmpty()) {
                    all = true;
                    selectedItemsCopy = allItems;
                }
                SyncInfo syncInfo = getSyncInfo(selectedItemsCopy);
                String info = Bundle.SyncPanel_info_status(syncInfo.download, syncInfo.upload, syncInfo.delete, syncInfo.noop, syncInfo.errors, syncInfo.warnings);
                final String msg = all ? Bundle.SyncPanel_info_prefix_all(info) : Bundle.SyncPanel_info_prefix_selection(info);
                Mutex.EVENT.readAccess(new Runnable() {
                    @Override
                    public void run() {
                        syncInfoLabel.setForeground(UIManager.getColor("Label.foreground")); // NOI18N
                        syncInfoLabel.setText(msg);
                    }
                });
            }
        });
    }

    public SyncInfo getSyncInfo(List<SyncItem> items) {
        SyncInfo syncInfo = new SyncInfo();
        for (SyncItem syncItem : items) {
            SyncItem.ValidationResult validationResult = syncItem.validate();
            if (validationResult.hasError()) {
                syncInfo.errors++;
            } else if (validationResult.hasWarning()) {
                syncInfo.warnings++;
            }
            switch (syncItem.getOperation()) {
                case SYMLINK:
                    // noop
                    break;
                case NOOP:
                    syncInfo.noop++;
                    break;
                case DOWNLOAD:
                case DOWNLOAD_REVIEW:
                    syncInfo.download++;
                    break;
                case UPLOAD:
                case UPLOAD_REVIEW:
                    syncInfo.upload++;
                    break;
                case DELETE:
                    syncInfo.delete++;
                    break;
                case FILE_CONFLICT:
                case FILE_DIR_COLLISION:
                    // noop, already counted
                    break;
                default:
                    assert false : "Unknown operation: " + syncItem.getOperation();
            }
        }
        return syncInfo;
    }

    void openDiffPanel() {
        assert SwingUtilities.isEventDispatchThread();

        SyncItem syncItem = getSelectedItem(true);
        if (syncItem == null) {
            // should not happen
            return;
        }
        DiffPanel diffPanel = new DiffPanel(remoteClient, syncItem, ProjectPropertiesSupport.getEncoding(project));
        try {
            if (diffPanel.open()) {
                syncItem.setOperation(SyncItem.Operation.UPLOAD);
                // need to redraw table
                updateDisplayedItems();
                // reselect the row?
                reselectItem(syncItem);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Error while saving document", ex);
            setError(Bundle.SyncPanel_error_documentSave());
        }
    }

    void cycleOperations() {
        SyncItem syncItem = getSelectedItem(false);
        if (syncItem == null) {
            // should not happen
            return;
        }
        int index = OPERATIONS.indexOf(syncItem.getOperation());
        if (index != -1) {
            if (index == OPERATIONS.size() - 1) {
                index = 0;
            } else {
                index++;
            }
            syncItem.setOperation(OPERATIONS.get(index));
            // need to redraw table
            updateDisplayedItems();
            reselectItem(syncItem);
        }
    }

    void setViewCheckBoxesSelected(boolean selected) {
        for (ViewCheckBox checkBox : viewCheckBoxes) {
            checkBox.setSelected(selected);
        }
    }

    /**
     * To preserve correct order and to show items that belong to more
     * view groups (e.g. symlink - warning & symlink).
     */
    void updateDisplayedItems() {
        assert SwingUtilities.isEventDispatchThread();
        displayedItems.clear();
        List<ViewCheckBox> selectedViewCheckBoxes = getSelectedViewCheckBoxes();
        if (!selectedViewCheckBoxes.isEmpty()) {
            // some view button selected
            for (SyncItem syncItem : allItems) {
                for (ViewCheckBox checkBox : selectedViewCheckBoxes) {
                    if (checkBox.getFilter().accept(syncItem)) {
                        displayedItems.add(syncItem);
                        break;
                    }
                }
            }
        }
        tableModel.fireSyncItemsChange();
    }

    void swapPaths() {
        assert SwingUtilities.isEventDispatchThread();
        // swap columns
        remotePathFirst = !remotePathFirst;
        // remember sorting and selected items
        sortKeys = adjustSortKeys(itemTable.getRowSorter().getSortKeys());
        List<SyncItem> selectedItems = getSelectedItems(false);
        // reinit table
        tableModel.fireTableHeaderChanged();
        reinitTable();
        reinitOperationButtons();
        reselectItems(selectedItems);
    }

    /**
     * Adjust possible sort keys to new column order.
     */
    private List<? extends RowSorter.SortKey> adjustSortKeys(List<? extends RowSorter.SortKey> sortKeys) {
        List<RowSorter.SortKey> currentKeys = new ArrayList<>(sortKeys);
        List<RowSorter.SortKey> newKeys = new ArrayList<>(currentKeys.size());
        for (RowSorter.SortKey sortKey : currentKeys) {
            int column = sortKey.getColumn();
            RowSorter.SortKey newSortKey;
            if (column == 1) {
                newSortKey = new RowSorter.SortKey(3, sortKey.getSortOrder());
            } else if (column == 3) {
                newSortKey = new RowSorter.SortKey(1, sortKey.getSortOrder());
            } else {
                newSortKey = sortKey;
            }
            newKeys.add(newSortKey);
        }
        return newKeys;
    }

    private List<ViewCheckBox> getSelectedViewCheckBoxes() {
        List<ViewCheckBox> selected = new ArrayList<>(viewCheckBoxes.size());
        for (ViewCheckBox button : viewCheckBoxes) {
            if (button.isSelected()) {
                selected.add(button);
            }
        }
        return selected;
    }

    private Map<SyncItem, Integer> itemsToIndex(List<SyncItem> items) {
        int i = 0;
        Map<SyncItem, Integer> map = new HashMap<>(items.size() * 2);
        for (SyncItem syncItem : items) {
            map.put(syncItem, i++);
        }
        return map;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        showSummaryCheckBox = new JCheckBox();
        operationsPanel = new JPanel();
        viewDownloadCheckBox = createViewCheckBox();
        viewUploadCheckBox = createViewCheckBox();
        viewNoopCheckBox = createViewCheckBox();
        viewDeleteCheckBox = createViewCheckBox();
        problemsPanel = new JPanel();
        viewWarningCheckBox = createViewCheckBox();
        viewErrorCheckBox = createViewCheckBox();
        viewFileConflictCheckBox = createViewCheckBox();
        viewFileDirCollisionCheckBox = createViewCheckBox();
        viewSymlinkCheckBox = createViewCheckBox();
        spaceHolderPanel = new JPanel();
        uncheckAllButton = new JButton();
        checkAllButton = new JButton();
        itemScrollPane = new JScrollPane();
        itemTable = new JTable();
        syncInfoPanel = new JPanel();
        syncInfoLabel = new JLabel();
        operationButtonsPanel = new JPanel();
        diffButton = new JButton();
        noopButton = new JButton();
        downloadButton = new JButton();
        uploadButton = new JButton();
        deleteButton = new JButton();
        resetButton = new JButton();
        messagesScrollPane = new JScrollPane();
        messagesTextPane = new HintArea();
        Mnemonics.setLocalizedText(showSummaryCheckBox, NbBundle.getMessage(SyncPanel.class, "SyncPanel.showSummaryCheckBox.text")); // NOI18N

        operationsPanel.setBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(SyncPanel.class, "SyncPanel.operationsPanel.title"))); // NOI18N

        GroupLayout operationsPanelLayout = new GroupLayout(operationsPanel);
        operationsPanel.setLayout(operationsPanelLayout);
        operationsPanelLayout.setHorizontalGroup(
            operationsPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(operationsPanelLayout.createSequentialGroup()
                .addContainerGap()

                .addGroup(operationsPanelLayout.createParallelGroup(Alignment.LEADING).addComponent(viewDownloadCheckBox).addComponent(viewUploadCheckBox)).addGap(18, 18, 18).addGroup(operationsPanelLayout.createParallelGroup(Alignment.LEADING).addComponent(viewNoopCheckBox).addComponent(viewDeleteCheckBox)).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        operationsPanelLayout.setVerticalGroup(
            operationsPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(operationsPanelLayout.createSequentialGroup()
                .addContainerGap()

                .addGroup(operationsPanelLayout.createParallelGroup(Alignment.TRAILING).addComponent(viewNoopCheckBox).addComponent(viewDownloadCheckBox)).addPreferredGap(ComponentPlacement.UNRELATED).addGroup(operationsPanelLayout.createParallelGroup(Alignment.TRAILING).addComponent(viewUploadCheckBox).addComponent(viewDeleteCheckBox)).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        problemsPanel.setBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(SyncPanel.class, "SyncPanel.problemsPanel.title"))); // NOI18N

        GroupLayout problemsPanelLayout = new GroupLayout(problemsPanel);
        problemsPanel.setLayout(problemsPanelLayout);
        problemsPanelLayout.setHorizontalGroup(
            problemsPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(problemsPanelLayout.createSequentialGroup()
                .addContainerGap()

                .addGroup(problemsPanelLayout.createParallelGroup(Alignment.LEADING).addComponent(viewWarningCheckBox).addComponent(viewErrorCheckBox)).addGap(18, 18, 18).addGroup(problemsPanelLayout.createParallelGroup(Alignment.LEADING).addComponent(viewFileConflictCheckBox).addComponent(viewFileDirCollisionCheckBox)).addGap(18, 18, 18).addComponent(viewSymlinkCheckBox).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        problemsPanelLayout.setVerticalGroup(
            problemsPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(problemsPanelLayout.createSequentialGroup()
                .addContainerGap()

                .addGroup(problemsPanelLayout.createParallelGroup(Alignment.TRAILING).addComponent(viewSymlinkCheckBox).addComponent(viewFileConflictCheckBox).addComponent(viewWarningCheckBox)).addPreferredGap(ComponentPlacement.RELATED).addGroup(problemsPanelLayout.createParallelGroup(Alignment.TRAILING).addComponent(viewErrorCheckBox).addComponent(viewFileDirCollisionCheckBox)).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        Mnemonics.setLocalizedText(uncheckAllButton, NbBundle.getMessage(SyncPanel.class, "SyncPanel.uncheckAllButton.text")); // NOI18N
        Mnemonics.setLocalizedText(checkAllButton, NbBundle.getMessage(SyncPanel.class, "SyncPanel.checkAllButton.text")); // NOI18N

        GroupLayout spaceHolderPanelLayout = new GroupLayout(spaceHolderPanel);
        spaceHolderPanel.setLayout(spaceHolderPanelLayout);
        spaceHolderPanelLayout.setHorizontalGroup(
            spaceHolderPanelLayout.createParallelGroup(Alignment.LEADING).addComponent(uncheckAllButton).addComponent(checkAllButton, Alignment.TRAILING)
        );

        spaceHolderPanelLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {checkAllButton, uncheckAllButton});

        spaceHolderPanelLayout.setVerticalGroup(
            spaceHolderPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(spaceHolderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkAllButton)

                .addPreferredGap(ComponentPlacement.RELATED).addComponent(uncheckAllButton).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        itemTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        itemScrollPane.setViewportView(itemTable);
        Mnemonics.setLocalizedText(syncInfoLabel, "SYNC INFO LABEL"); // NOI18N

        GroupLayout syncInfoPanelLayout = new GroupLayout(syncInfoPanel);
        syncInfoPanel.setLayout(syncInfoPanelLayout);
        syncInfoPanelLayout.setHorizontalGroup(
            syncInfoPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(syncInfoPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(syncInfoLabel)

                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        syncInfoPanelLayout.setVerticalGroup(
            syncInfoPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(syncInfoPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(syncInfoLabel)
                .addGap(5, 5, 5))
        );

        diffButton.setIcon(new ImageIcon(getClass().getResource("/org/netbeans/modules/php/project/ui/resources/diff.png"))); // NOI18N
        diffButton.setEnabled(false);
        operationButtonsPanel.add(diffButton);

        Mnemonics.setLocalizedText(noopButton, " "); // NOI18N
        noopButton.setEnabled(false);
        operationButtonsPanel.add(noopButton);

        Mnemonics.setLocalizedText(downloadButton, " "); // NOI18N
        downloadButton.setEnabled(false);
        operationButtonsPanel.add(downloadButton);

        Mnemonics.setLocalizedText(uploadButton, " "); // NOI18N
        uploadButton.setEnabled(false);
        operationButtonsPanel.add(uploadButton);

        Mnemonics.setLocalizedText(deleteButton, " "); // NOI18N
        deleteButton.setEnabled(false);
        operationButtonsPanel.add(deleteButton);

        Mnemonics.setLocalizedText(resetButton, " "); // NOI18N
        resetButton.setEnabled(false);
        operationButtonsPanel.add(resetButton);

        messagesScrollPane.setBorder(null);
        messagesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        messagesScrollPane.setViewportView(messagesTextPane);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
                .addContainerGap()

                .addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(itemScrollPane, GroupLayout.DEFAULT_SIZE, 728, Short.MAX_VALUE).addGroup(layout.createSequentialGroup()

                        .addComponent(operationsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(problemsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(ComponentPlacement.RELATED, 0, GroupLayout.PREFERRED_SIZE).addComponent(spaceHolderPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addComponent(messagesScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(operationButtonsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(syncInfoPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
                .addContainerGap()

                .addGroup(layout.createParallelGroup(Alignment.LEADING, false).addComponent(operationsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(problemsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(spaceHolderPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(itemScrollPane, GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE).addGap(0, 0, 0).addComponent(syncInfoPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(operationButtonsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addComponent(messagesScrollPane, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton checkAllButton;
    private JButton deleteButton;
    private JButton diffButton;
    private JButton downloadButton;
    private JScrollPane itemScrollPane;
    private JTable itemTable;
    private JScrollPane messagesScrollPane;
    private JTextPane messagesTextPane;
    private JButton noopButton;
    private JPanel operationButtonsPanel;
    private JPanel operationsPanel;
    private JPanel problemsPanel;
    private JButton resetButton;
    private JCheckBox showSummaryCheckBox;
    private JPanel spaceHolderPanel;
    private JLabel syncInfoLabel;
    private JPanel syncInfoPanel;
    private JButton uncheckAllButton;
    private JButton uploadButton;
    private JCheckBox viewDeleteCheckBox;
    private JCheckBox viewDownloadCheckBox;
    private JCheckBox viewErrorCheckBox;
    private JCheckBox viewFileConflictCheckBox;
    private JCheckBox viewFileDirCollisionCheckBox;
    private JCheckBox viewNoopCheckBox;
    private JCheckBox viewSymlinkCheckBox;
    private JCheckBox viewUploadCheckBox;
    private JCheckBox viewWarningCheckBox;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    @NbBundle.Messages({
        "SyncPanel.table.column.remote.title=Remote Path",
        "SyncPanel.table.column.local.title=Local Path"
    })
    private final class FileTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 16478634354314324L;

        private final List<SyncItem> items;


        public FileTableModel(List<SyncItem> items) {
            assert SwingUtilities.isEventDispatchThread();
            this.items = items;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            assert SwingUtilities.isEventDispatchThread();
            return false;
        }

        @Override
        public int getRowCount() {
            assert SwingUtilities.isEventDispatchThread();
            return items.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            assert SwingUtilities.isEventDispatchThread();
            SyncItem syncItem = items.get(rowIndex);
            if (columnIndex == 0) {
                SyncItem.ValidationResult validationResult = syncItem.validate();
                if (validationResult.hasError()) {
                    return ImageUtilities.loadImageIcon(ERROR_ICON_PATH, false);
                }
                if (validationResult.hasWarning()) {
                    return ImageUtilities.loadImageIcon(WARNING_ICON_PATH, false);
                }
                return null;
            } else if (columnIndex == 1
                    || columnIndex == 3) {
                if (isRemotePathColumn(columnIndex)) {
                    return syncItem.getRemotePath();
                }
                return syncItem.getLocalPath();
            } else if (columnIndex == 2) {
                return syncItem.getOperation();
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        @Override
        public String getColumnName(int columnIndex) {
            assert SwingUtilities.isEventDispatchThread();
            if (columnIndex == 0
                    || columnIndex == 2) {
                return ""; // NOI18N
            } else if (columnIndex == 1
                    || columnIndex == 3) {
                if (isRemotePathColumn(columnIndex)) {
                    return Bundle.SyncPanel_table_column_remote_title();
                }
                return Bundle.SyncPanel_table_column_local_title();
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            assert SwingUtilities.isEventDispatchThread();
            if (columnIndex == 0) {
                return Icon.class;
            } else if (columnIndex == 1
                    || columnIndex == 3) {
                return String.class;
            } else if (columnIndex == 2) {
                return SyncItem.Operation.class;
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        public void fireSyncItemsChange() {
            assert SwingUtilities.isEventDispatchThread();
            fireTableDataChanged();
        }

        public void fireTableHeaderChanged() {
            assert SwingUtilities.isEventDispatchThread();
            fireTableStructureChanged();
        }

        boolean isRemotePathColumn(int columnIndex) {
            assert SwingUtilities.isEventDispatchThread();
            if (remotePathFirst && columnIndex == 1) {
                return true;
            }
            return !remotePathFirst && columnIndex == 3;
        }

    }

    private static final class HeaderRenderer implements TableCellRenderer {

        private static final long serialVersionUID = -6517698451435465L;

        private final String toolTip;


        public HeaderRenderer(String toolTip) {
            this.toolTip = toolTip;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            assert SwingUtilities.isEventDispatchThread();
            JLabel rendererComponent = (JLabel) table.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Icon) {
                Icon icon = (Icon) value;
                rendererComponent.setHorizontalAlignment(SwingConstants.CENTER);
                rendererComponent.setText(null);
                rendererComponent.setIcon(icon);
            }
            rendererComponent.setToolTipText(toolTip);
            return rendererComponent;
        }

    }

    private final class IconRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = -46865321321L;


        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            assert SwingUtilities.isEventDispatchThread();
            Icon icon = (Icon) value;
            JLabel rendererComponent = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            rendererComponent.setHorizontalAlignment(SwingConstants.CENTER);
            rendererComponent.setToolTipText(displayedItems.get(row).validate().getMessage());
            rendererComponent.setText(null);
            rendererComponent.setIcon(icon);
            return rendererComponent;
        }

    }

    private final class StringRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 567654543546954L;


        @NbBundle.Messages({
            "# {0} - file name",
            "SyncPanel.localFile.modified.mark={0}*"
        })
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String text = (String) value;
            JLabel rendererComponent = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (text != null) {
                rendererComponent.setHorizontalAlignment(SwingConstants.LEFT);
                rendererComponent.setToolTipText(text);
                if (!tableModel.isRemotePathColumn(column)) {
                    // local file
                    if (displayedItems.get(row).hasTmpLocalFile()) {
                        text = Bundle.SyncPanel_localFile_modified_mark(text);
                    }
                }
                // add left padding - space just behaves better (on focus, "frame" has the same size as the cell itself)
                //rendererComponent.setBorder(new CompoundBorder(new EmptyBorder(new Insets(0, 2, 0, 0)), rendererComponent.getBorder()));
                text = " " + text; // NOI18N
            }
            rendererComponent.setText(text);
            rendererComponent.setIcon(null);
            return rendererComponent;
        }

    }

    private final class OperationRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = -6786654671313465458L;


        @NbBundle.Messages({
            "# {0} - operation",
            "SyncPanel.operation.tooltip={0} (click to change)"
        })
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel rendererComponent = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            SyncItem.Operation operation = (SyncItem.Operation) value;
            // #218341
            if (operation == null) {
                String expected;
                try {
                    expected = displayedItems.get(itemTable.convertRowIndexToModel(row)).getOperation().toString();
                } catch (Exception ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    expected = "???"; // NOI18N
                }
                LOGGER.log(Level.WARNING, "Unexpected null value for operation (row: {0}, column: {1}, expected: {2})", new Object[] {row, column, expected});
                // fallback to NOOP
                operation = SyncItem.Operation.NOOP;
            }
            rendererComponent.setIcon(operation.getIcon(!remotePathFirst));
            if (OPERATIONS.contains(operation)) {
                rendererComponent.setToolTipText(Bundle.SyncPanel_operation_tooltip(operation.getTitle()));
            } else {
                rendererComponent.setToolTipText(operation.getTitle());
            }
            rendererComponent.setText(null);
            rendererComponent.setHorizontalAlignment(SwingConstants.CENTER);
            return rendererComponent;
        }

    }

    private final class OperationButtonListener implements ActionListener {

        private final SyncItem.Operation operation;


        public OperationButtonListener(SyncItem.Operation operation) {
            this.operation = operation;
        }

        // can be done in background thread if needed
        @Override
        public void actionPerformed(ActionEvent e) {
            assert SwingUtilities.isEventDispatchThread();
            List<SyncItem> selectedItems = getSelectedItems(false);
            for (SyncItem syncItem : selectedItems) {
                if (!syncItem.isOperationChangePossible()) {
                    continue;
                }
                if (operation == null) {
                    syncItem.resetOperation();
                } else {
                    syncItem.setOperation(operation);
                }
            }
            // need to redraw table
            updateDisplayedItems();
            // reselect the rows?
            reselectItems(selectedItems);
        }

    }

    private final class DiffActionListener implements ActionListener {

        @NbBundle.Messages("SyncPanel.error.documentSave=Cannot save file content.")
        @Override
        public void actionPerformed(ActionEvent e) {
            openDiffPanel();
        }

    }

    private final class OkActionListener implements ActionListener {

        private final Dialog dialog;


        public OkActionListener(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (rememberShowSummary != null) {
                PhpOptions.getInstance().setRemoteSyncShowSummary(rememberShowSummary);
            }
            if (!showSummaryCheckBox.isVisible()
                    || !showSummaryCheckBox.isSelected()) {
                closeDialog();
                return;
            }
            SyncInfo syncInfo = getSyncInfo(allItems);
            SummaryPanel panel = new SummaryPanel(
                    syncInfo.upload,
                    syncInfo.download,
                    syncInfo.delete,
                    syncInfo.noop);
            if (panel.open()) {
                closeDialog();
            }
        }

        private void closeDialog() {
            dialog.setVisible(false);
        }

    }

    private static final class ViewCheckBox extends JCheckBox {

        private static final long serialVersionUID = 16576854546544L;

        private SyncItemFilter filter;


        public SyncItemFilter getFilter() {
            return filter;
        }

        public void setFilter(SyncItemFilter filter) {
            this.filter = filter;
        }

    }

    private interface SyncItemFilter {
        boolean accept(SyncItem syncItem);
    }

    private class ViewListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            updateDisplayedItems();
        }

    }

    private class SortPopupMenuItemListener implements ActionListener {

        private final int column;
        private final SortOrder sortOrder;


        public SortPopupMenuItemListener(int column, SortOrder sortOrder) {
            this.column = column;
            this.sortOrder = sortOrder;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RowSorter<? extends TableModel> rowSorter = itemTable.getRowSorter();
            rowSorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(column, sortOrder)));
        }

    }

    private class PopupMenuItemListener implements ActionListener {

        private final Collection<SyncItem.Operation> fromOperations;
        private final SyncItem.Operation toOperation;


        public PopupMenuItemListener(SyncItem.Operation toOperation) {
            this(Collections.<SyncItem.Operation>emptyList(), toOperation);
        }

        public PopupMenuItemListener(SyncItem.Operation fromOperation, SyncItem.Operation toOperation) {
            this(Collections.singleton(fromOperation), toOperation);
        }

        public PopupMenuItemListener(Collection<SyncItem.Operation> fromOperations, SyncItem.Operation toOperation) {
            this.fromOperations = fromOperations;
            this.toOperation = toOperation;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            List<SyncItem> selectedItems = getSelectedItems(true);
            for (SyncItem item : selectedItems) {
                if (!item.isOperationChangePossible()) {
                    continue;
                }
                if (fromOperations.isEmpty()
                        || fromOperations.contains(item.getOperation())) {
                    item.setOperation(toOperation);
                }
            }
            updateDisplayedItems();
            reselectItems(selectedItems);
        }

    }

    public static final class SyncInfo {

        public int download = 0;
        public int upload = 0;
        public int delete = 0;
        public int noop = 0;
        public int errors = 0;
        public int warnings = 0;

    }

    @org.netbeans.api.annotations.common.SuppressWarnings("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE")
    private static final class SyncItemImageIconComparator implements Comparator<ImageIcon> {

        @Override
        public int compare(ImageIcon icon1, ImageIcon icon2) {
            ImageIcon error = ImageUtilities.loadImageIcon(ERROR_ICON_PATH, false);
            boolean isError1 = error.equals(icon1);
            boolean isError2 = error.equals(icon2);
            if (isError1 && isError2) {
                return 0;
            }
            if (isError1) {
                return 1;
            }
            if (isError2) {
                return -1;
            }
            ImageIcon warning = ImageUtilities.loadImageIcon(WARNING_ICON_PATH, false);
            boolean isWarning1 = warning.equals(icon1);
            boolean isWarning2 = warning.equals(icon2);
            if (isWarning1 && isWarning2) {
                return 0;
            }
            if (isWarning1) {
                return 1;
            }
            if (isWarning2) {
                return -1;
            }
            return 0;
        }

    }

}

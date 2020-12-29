package org.netbeans.modules.python.project2.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;
import org.netbeans.modules.python.platform.panels.PythonPlatformPanel;
import org.netbeans.modules.python.project2.PythonProject2;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.HtmlRenderer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class Utils {

    @NbBundle.Messages({"LBL_SelectMainModule=Select Main Module", "LBL_BrowseMainModules=Browse Main Modules"})
    public static String chooseMainModule (PythonProject2 project) {
        final JButton okButton = new JButton (NbBundle.getMessage(Utils.class, "LBL_SelectMainModule"));
        final MainModuleChooser mcc = new MainModuleChooser(project, okButton);
        final Object[] options = new Object[] {okButton, DialogDescriptor.CANCEL_OPTION};
        final DialogDescriptor dd = new DialogDescriptor (mcc, NbBundle.getMessage(Utils.class, "LBL_BrowseMainModules"), true, options,
        okButton,DialogDescriptor.RIGHT_ALIGN,HelpCtx.DEFAULT_HELP,null);
        dd.setClosingOptions(options);
        if (DialogDisplayer.getDefault().notify(dd) == okButton) {
            return mcc.getMainModule();
        }
        return null;
    }

    @NbBundle.Messages({"LBL_SelectPythonPlatform=Select Python Platform", "LBL_BrowsePythonPlatforms=Browse Python Platforms", "LBL_ManagePythonPlatform=Manage Python Platforms"})
    public static String choosePythonPlatform(PythonProject2 project) {
        final JButton okButton = new JButton (NbBundle.getMessage(Utils.class, "LBL_SelectPythonPlatform"));
        final JButton managePythonPlatformButton = new JButton(NbBundle.getMessage(Utils.class, "LBL_ManagePythonPlatform"));
        final PythonPlatformChooser ppc = new PythonPlatformChooser(okButton, managePythonPlatformButton);
        final Object[] options = new Object[] {okButton, managePythonPlatformButton, DialogDescriptor.CANCEL_OPTION};
        final Object[] closingOptions = new Object[] {okButton, DialogDescriptor.CANCEL_OPTION};
        final DialogDescriptor dd = new DialogDescriptor(ppc, NbBundle.getMessage(Utils.class, "LBL_BrowsePythonPlatforms"), true, options,
            okButton,DialogDescriptor.RIGHT_ALIGN, HelpCtx.DEFAULT_HELP, null);
        dd.setClosingOptions(closingOptions);
        if (DialogDisplayer.getDefault().notify(dd) == okButton) {
            return ppc.getPythonPlatform();
        }

        return null;
    }

    public static interface SourceRootsMediator {
        public void setRelatedEditMediator(SourceRootsMediator rem);
    }

    public static ComboBoxModel createPlatformModel () {
        return new PlatformModel ();
    }

    public static ListCellRenderer createPlatformRenderer () {
        return new PlatformRenderer();
    }

    private static class PlatformModel extends DefaultComboBoxModel {

        private final PythonPlatformManager manager;

        public PlatformModel () {
            manager = PythonPlatformManager.getInstance();
            init ();
        }

        private void init () {
            this.removeAllElements();   //init will be used also in case of chnge of installed plaforms
            final List<String> ids = manager.getPlatformList();
            for (String id : ids) {
                PythonPlatform platform = manager.getPlatform(id);
                this.addElement(platform);
            }
        }
    }

    private static class PlatformRenderer implements ListCellRenderer {

        private final ListCellRenderer delegate;

        public PlatformRenderer () {
            delegate = HtmlRenderer.createRenderer();
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String name;
            if (value instanceof PythonPlatform) {
                PythonPlatform key = (PythonPlatform) value;
                name = key.getName();
            }
            else if (value instanceof String) {
                //hndles broken platform for customizer
                name = "<html><font color=\"#A40000\">" //NOI18N
                            + NbBundle.getMessage(
                                    Utils.class, "TXT_BrokenPlatformFmt", (String)value);
            }
            else {
                name = "";
            }
            return delegate.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
        }

    }

    private static class SourceRootsModel extends DefaultTableModel {

        public SourceRootsModel (Object[][] data) {
            super (data,new Object[]{"location","label"});//NOI18N
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return File.class;
                case 1:
                    return String.class;
                default:
                    return super.getColumnClass (columnIndex);
            }
        }
    }

    private static class FileRenderer extends DefaultTableCellRenderer {

        private File projectFolder;

        public FileRenderer (File projectFolder) {
            this.projectFolder = projectFolder;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row, int column) {
            String displayName;
            if (value instanceof File) {
                File root = (File) value;
                String pfPath = projectFolder.getAbsolutePath() + File.separatorChar;
                String srPath = root.getAbsolutePath();
                if (srPath.startsWith(pfPath)) {
                    displayName = srPath.substring(pfPath.length());
                }
                else {
                    displayName = srPath;
                }
            }
            else {
                displayName = null;
            }
            Component c = super.getTableCellRendererComponent(table, displayName, isSelected, hasFocus, row, column);
            if (c instanceof JComponent) {
                ((JComponent) c).setToolTipText (displayName);
            }
            return c;
        }
    }
}

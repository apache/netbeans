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

package org.netbeans.lib.editor.codetemplates.storage.ui;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.netbeans.lib.editor.codetemplates.CodeTemplateHint;
import org.netbeans.lib.editor.codetemplates.CodeTemplateParameterImpl;
import org.netbeans.lib.editor.codetemplates.ParametrizedTextParser;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 * Enables editing code template parameters.
 *
 * @author Arthur Sadykov
 */
class CodeTemplateParametersDialog {

    private static final String X = "code-template-parameters-dialog-x"; //NOI18N
    private static final String Y = "code-template-parameters-dialog-y"; //NOI18N
    private static final String WIDTH = "code-template-parameters-dialog-width"; //NOI18N
    private static final String HEIGHT = "code-template-parameters-dialog-height"; //NOI18N
    private static final int DEFAULT_WIDTH = 750;
    private static final int DEFAULT_HEIGHT = 250;
    private static final String NULL_PARAMETER_NAME = "<null>"; //NOI18N
    private final Dialog dialog;
    private static DialogDescriptor dialogDescriptor;
    private final CodeTemplateParametersPanel templateParametersPanel;
    private static NotificationLineSupport notificationLineSupport;

    CodeTemplateParametersDialog(String language, String parametrizedText) {
        templateParametersPanel = new CodeTemplateParametersPanel(language, parametrizedText);
        dialogDescriptor = new DialogDescriptor(
                templateParametersPanel,
                NbBundle.getMessage(CodeTemplateParametersDialog.class, "CTPD_Edit_Template_Parameters")); //NOI18N
        dialogDescriptor.setValid(templateParametersPanel.isTableDataValid());
        notificationLineSupport = dialogDescriptor.createNotificationLineSupport();
        dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setBounds(getBounds());
        dialog.setVisible(true);
    }

    private Rectangle getBounds() {
        Preferences preferences = NbPreferences.forModule(CodeTemplateParametersDialog.class);
        Frame mainWindow = WindowManager.getDefault().getMainWindow();
        int windowWidth = mainWindow.getWidth();
        int windowHeight = mainWindow.getHeight();
        int dialogWidth = preferences.getInt(WIDTH, DEFAULT_WIDTH);
        int dialogHeight = preferences.getInt(HEIGHT, DEFAULT_HEIGHT);
        int dialogX = preferences.getInt(X, windowWidth / 2 - dialogWidth / 2);
        int dialogY = preferences.getInt(Y, windowHeight / 2 - dialogHeight / 2);
        return new Rectangle(dialogX, dialogY, dialogWidth, dialogHeight);
    }

    boolean isOkButtonPressed() {
        return dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION;
    }

    void close() {
        saveBounds();
        dialog.dispose();
    }

    private void saveBounds() {
        Preferences preferences = NbPreferences.forModule(CodeTemplateParametersDialog.class);
        if (isClipped()) {
            Frame mainWindow = WindowManager.getDefault().getMainWindow();
            int windowWidth = mainWindow.getWidth();
            int windowHeight = mainWindow.getHeight();
            preferences.putInt(X, windowWidth / 2 - dialog.getWidth() / 2);
            preferences.putInt(Y, windowHeight / 2 - dialog.getHeight() / 2);
            preferences.putInt(WIDTH, dialog.getWidth());
            preferences.putInt(HEIGHT, dialog.getHeight());
        } else {
            preferences.putInt(X, dialog.getX());
            preferences.putInt(Y, dialog.getY());
            preferences.putInt(WIDTH, dialog.getWidth());
            preferences.putInt(HEIGHT, dialog.getHeight());
        }
    }

    private boolean isClipped() {
        int dialogArea = dialog.getWidth() * dialog.getHeight();
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = environment.getScreenDevices();
        Rectangle bounds;
        int boundsArea = 0;
        for (GraphicsDevice device : devices) {
            bounds = device.getDefaultConfiguration().getBounds();
            if (bounds.intersects(dialog.getBounds())) {
                bounds = bounds.intersection(dialog.getBounds());
                boundsArea += (bounds.width * bounds.height);
            }
        }
        return boundsArea != dialogArea;
    }

    List<?> getTableData() {
        return templateParametersPanel.getTableData();
    }

    private static class CodeTemplateParametersPanel extends JPanel {

        private final String language;
        private JScrollPane parametersScrollPane;
        private JTable parametersTable;

        private CodeTemplateParametersPanel(String language, String parametrizedText) {
            this.language = language;
            initComponents();
            fillTable(parametrizedText);
        }

        private void initComponents() {
            parametersScrollPane = new JScrollPane();
            parametersTable = new JTable();
            TableModel tableModel = new DefaultTableModel(
                    new String[]{
                        NbBundle.getMessage(CodeTemplateParametersPanel.class, "TC_Name"), //NOI18N
                        NbBundle.getMessage(CodeTemplateParametersPanel.class, "TC_Hint"), //NOI18N
                        NbBundle.getMessage(CodeTemplateParametersPanel.class, "TC_Default_Value"), //NOI18N
                        NbBundle.getMessage(CodeTemplateParametersPanel.class, "TC_Ordering"), //NOI18N
                        NbBundle.getMessage(CodeTemplateParametersPanel.class, "TC_Completion"), //NOI18N
                        NbBundle.getMessage(CodeTemplateParametersPanel.class, "TC_Editable"), //NOI18N
                    }, 0) {
                Class[] types = new Class[]{String.class, String.class, String.class, String.class, Boolean.class,
                    Boolean.class};

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return types[columnIndex];
                }
            };
            parametersTable.setModel(tableModel);
            TableColumn hintColumn = parametersTable.getColumnModel().getColumn(1);
            hintColumn.setCellEditor(new MainHintCellEditor(new JComboBox()));
            TableColumn orderingColumn = parametersTable.getColumnModel().getColumn(3);
            JTextField orderingTextField = new JTextField();
            orderingTextField.setHorizontalAlignment(SwingConstants.RIGHT);
            orderingColumn.setCellEditor(new OrderingHintCellEditor(orderingTextField));
            DefaultTableCellRenderer rightAlignmentRenderer = new DefaultTableCellRenderer();
            rightAlignmentRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
            orderingColumn.setCellRenderer(rightAlignmentRenderer);
            adjustSizeOfColums();
            parametersScrollPane.setViewportView(parametersTable);
            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(parametersScrollPane, GroupLayout.DEFAULT_SIZE, 738, Short.MAX_VALUE)
                                    .addContainerGap())
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(parametersScrollPane, GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
                                    .addContainerGap())
            );
        }

        private void adjustSizeOfColums() {
            TableColumnModel columnModel = parametersTable.getColumnModel();
            columnModel.getColumn(0).setPreferredWidth(75);
            if (!language.equals(CodeTemplateHint.JAVA_LANGUAGE) && !language.equals(CodeTemplateHint.PHP_LANGUAGE)) {
                columnModel.getColumn(1).setMinWidth(0);
                columnModel.getColumn(1).setMaxWidth(0);
            } else {
                columnModel.getColumn(1).setPreferredWidth(150);
            }
            columnModel.getColumn(2).setPreferredWidth(75);
            columnModel.getColumn(3).setPreferredWidth(50);
            columnModel.getColumn(4).setPreferredWidth(50);
            columnModel.getColumn(5).setPreferredWidth(50);
        }

        private void fillTable(String parametrizedText) {
            ParametrizedTextParser parser = new ParametrizedTextParser(null, parametrizedText);
            parser.parse();
            Map<Integer, Object> parametrizedFragmentsByOrdinal = parser.getParametrizedFragmentsByOrdinals();
            int numberOfFragments = parametrizedFragmentsByOrdinal.size();
            for (int ordinal = 1; ordinal < numberOfFragments; ordinal += 2) {
                CodeTemplateParameterImpl paramImpl =
                        (CodeTemplateParameterImpl) parametrizedFragmentsByOrdinal.get(ordinal);
                String paramName = paramImpl.getName();
                if (paramName.equals(NULL_PARAMETER_NAME)
                        || (!paramImpl.isSlave()
                        && (!paramName.equals(CodeTemplateParameter.CURSOR_PARAMETER_NAME)
                        && !paramName.equals(CodeTemplateParameter.SELECTION_PARAMETER_NAME)
                        && !paramName.equals(CodeTemplateParameter.NO_FORMAT_PARAMETER_NAME)
                        && !paramName.equals(CodeTemplateParameter.NO_INDENT_PARAMETER_NAME)))) {
                    Function<String, Boolean> isSupportedHint = hintName -> {
                        return Arrays.stream(CodeTemplateHint.values())
                                .filter(hint -> hint.getLanguages().contains(language)
                                        || hint.getLanguages().contains(CodeTemplateHint.ALL_LANGUAGES))
                                .anyMatch(hint -> hint.getName().equals(hintName));
                    };
                    Function<CodeTemplateParameterImpl, Boolean> checkHints = parameter -> {
                        Set<String> hints = parameter.getHints().keySet();
                        Iterator<String> iterator = hints.iterator();
                        boolean valid = true;
                        while (iterator.hasNext()) {
                            if (!isSupportedHint.apply(iterator.next())) {
                                valid = false;
                                break;
                            }
                        }
                        return valid;
                    };
                    if (checkHints.apply(paramImpl)) {
                        addRowToTable(paramImpl);
                    }
                }
            }
        }

        List<?> getTableData() {
            return ((DefaultTableModel) parametersTable.getModel()).getDataVector();
        }

        private boolean isTableDataValid() {
            Supplier<List<String>> collectHints = () -> {
                List<String> hints = new ArrayList<>();
                TableModel tableModel = parametersTable.getModel();
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    hints.add((String) tableModel.getValueAt(i, 1));
                }
                return Collections.unmodifiableList(hints);
            };
            return HintsValidator.isValid(collectHints.get(), language);
        }

        private void addRowToTable(CodeTemplateParameterImpl paramImpl) {
            DefaultTableModel tableModel = (DefaultTableModel) parametersTable.getModel();
            Map<String, String> hintValuesByNames = paramImpl.getHints();
            Function<Map<String, String>, String> getMainHint = hints -> {
                Iterator<String> iterator = hintValuesByNames.keySet().iterator();
                while (iterator.hasNext()) {
                    String hint = iterator.next();
                    if (!hint.equals(CodeTemplateParameter.DEFAULT_VALUE_HINT_NAME)
                            && !hint.equals(CodeTemplateParameter.EDITABLE_HINT_NAME)
                            && !hint.equals(CodeTemplateParameter.ORDERING_HINT_NAME)
                            && !hint.equals(CodeTemplateParameter.COMPLETION_INVOKE_HINT_NAME)) {
                        return hint;
                    }
                }
                return null;
            };
            Function<Map<String, String>, String> buildMainHint = hints -> {
                String hint = getMainHint.apply(hintValuesByNames);
                if (hint == null) {
                    return ""; //NOI18N
                }
                switch (hint) {
                    case "instanceof": { //NOI18N
                        return "instanceof=\"" + hintValuesByNames.get("instanceof") + "\""; //NOI18N
                    }
                    case "staticImport": { //NOI18N
                        return "staticImport=\"" + hintValuesByNames.get("staticImport") + "\""; //NOI18N
                    }
                    case "type": { //NOI18N
                        return "type=\"" + hintValuesByNames.get("type") + "\""; //NOI18N
                    }
                    case "typeVar": { //NOI18N
                        return "typeVar=\"" + hintValuesByNames.get("typeVar") + "\""; //NOI18N
                    }
                    default: {
                        return hint;
                    }
                }
            };
            String mainHint = buildMainHint.apply(hintValuesByNames);
            String defaultHint = hintValuesByNames.get(CodeTemplateParameter.DEFAULT_VALUE_HINT_NAME);
            String orderingHint = hintValuesByNames.get(CodeTemplateParameter.ORDERING_HINT_NAME);
            int ordering = 0;
            try {
                if (orderingHint != null) {
                    ordering = Integer.parseInt(orderingHint);
                    if (ordering < 0) {
                        orderingHint = null;
                    }
                }
            } catch (NumberFormatException ex) {
                orderingHint = null;
            }
            String completionInvokeHint = hintValuesByNames.get(CodeTemplateParameter.COMPLETION_INVOKE_HINT_NAME);
            tableModel.addRow(new Object[]{
                paramImpl.getName(),
                mainHint,
                defaultHint == null ? "" : defaultHint, //NOI18N
                orderingHint == null ? "" : ordering, //NOI18N
                Boolean.parseBoolean(completionInvokeHint),
                paramImpl.isEditable()});
        }

        private abstract class TableCellEditor extends DefaultCellEditor {

            protected final Border originalBorder;
            protected InputVerifier verifier;
            protected final JTextField textField;
            protected JComboBox<String> comboBox;

            TableCellEditor(JTextField textField) {
                super(textField);
                this.textField = textField;
                this.originalBorder = textField.getBorder();
            }

            TableCellEditor(JComboBox<String> comboBox) {
                super(comboBox);
                this.textField = (JTextField) comboBox.getEditor().getEditorComponent();
                this.comboBox = comboBox;
                this.originalBorder = comboBox.getBorder();
            }

            @Override
            public boolean stopCellEditing() {
                if (!verifier.verify(textField)) {
                    setErrorBorder();
                    highlightErroneousText();
                    textField.requestFocusInWindow();
                    dialogDescriptor.setValid(false);
                    setErrorMessage();
                    return false;
                }
                setOriginalBorder();
                boolean editingStopped = super.stopCellEditing();
                dialogDescriptor.setValid(editingStopped);
                if (editingStopped) {
                    notificationLineSupport.clearMessages();
                }
                return editingStopped;
            }

            abstract void setErrorMessage();

            abstract void highlightErroneousText();

            protected void setErrorBorder() {
                textField.setBorder(new LineBorder(Color.red));
            }

            protected void setOriginalBorder() {
                textField.setBorder(originalBorder);
            }
        }

        private class MainHintCellEditor extends TableCellEditor {

            private String originalHint;

            MainHintCellEditor(JComboBox comboBox) {
                super(comboBox);
                comboBox.setEditable(true);
                Object[] hints = Arrays.stream(CodeTemplateHint.values())
                        .filter(hint -> hint.getLanguages().contains(language))
                        .map(CodeTemplateHint::getName)
                        .sorted()
                        .toArray();
                Supplier<Object[]> updateHints = () -> {
                    for (int i = 0; i < hints.length; i++) {
                        String hint = (String) hints[i];
                        if (hint.equals("instanceof") //NOI18N
                                || hint.equals("staticImport") //NOI18N
                                || hint.equals("type") //NOI18N
                                || hint.equals("typeVar")) { //NOI18N
                            hints[i] = hint + "=\"\""; //NOI18N
                        }
                    }
                    return hints;
                };
                comboBox.setModel(new DefaultComboBoxModel<>(updateHints.get()));
                comboBox.addItemListener(event -> {
                    int selectedIndex = comboBox.getSelectedIndex();
                    if (selectedIndex < 0) {
                        String typedText = ((JTextField) comboBox.getEditor().getEditorComponent()).getText();
                        Matcher matcher = Pattern.compile("^\\s*?([a-zA-Z_]\\w*).*?$").matcher(typedText); //NOI18N
                        if (matcher.matches()) {
                            int i;
                            for (i = 0; i < hints.length; i++) {
                                String hint = (String) hints[i];
                                if (hint.startsWith(matcher.group(1))) {
                                    break;
                                }
                            }
                            if (i != hints.length) {
                                originalHint = (String) hints[i];
                            }
                        }
                    } else {
                        originalHint = (String) hints[selectedIndex];
                    }
                });
                verifier = new MainHintVerifier();
            }

            @Override
            void setErrorMessage() {
                notificationLineSupport.setErrorMessage(getErrorMessage());
            }

            private String getErrorMessage() {
                if (originalHint == null) {
                    return ""; //NOI18N
                }
                if (originalHint.startsWith("instanceof")) { //NOI18N
                    return NbBundle.getMessage(MainHintCellEditor.class, "NLS_Invalid_Instanceof_Hint"); //NOI18N
                } else if (originalHint.startsWith("staticImport")) { //NOI18N
                    return NbBundle.getMessage(MainHintCellEditor.class, "NLS_Invalid_StaticImport_Hint"); //NOI18N
                } else if (originalHint.startsWith("typeVar")) { //NOI18N
                    return NbBundle.getMessage(MainHintCellEditor.class, "NLS_Invalid_TypeVar_Hint"); //NOI18N
                } else if (originalHint.startsWith("type")) { //NOI18N
                    return NbBundle.getMessage(MainHintCellEditor.class, "NLS_Invalid_Type_Hint"); //NOI18N
                } else {
                    return NbBundle.getMessage(MainHintCellEditor.class, "NLS_Invalid_Hint") + originalHint + "."; //NOI18N
                }
            }

            @Override
            void highlightErroneousText() {
                String hint = textField.getText();
                long quoteCount = hint.chars().filter(ch -> ch == '"').count();
                if (quoteCount == 2) {
                    if (hint.startsWith("instanceof") //NOI18N
                            || hint.startsWith("staticImport") //NOI18N
                            || hint.startsWith("typeVar") //NOI18N
                            || hint.startsWith("type")) { //NOI18N
                        int firstQuoteIndex = hint.indexOf('"');
                        int secondQuoteIndex = hint.indexOf('"', firstQuoteIndex + 1);
                        textField.setCaretPosition(firstQuoteIndex + 1);
                        textField.moveCaretPosition(secondQuoteIndex);
                    } else {
                        textField.selectAll();
                    }
                } else {
                    textField.selectAll();
                }
            }

            @Override
            protected void setErrorBorder() {
                comboBox.setBorder(new LineBorder(Color.RED));
            }

            @Override
            protected void setOriginalBorder() {
                comboBox.setBorder(originalBorder);
            }

            private class MainHintVerifier extends InputVerifier {

                @Override
                public boolean verify(JComponent input) {
                    JTextField hintTextField = (JTextField) input;
                    String hint = hintTextField.getText().trim();
                    hintTextField.setText(hint);
                    return HintsValidator.isValid(Collections.singletonList(hint), language);
                }
            }
        }

        private class OrderingHintCellEditor extends TableCellEditor {

            OrderingHintCellEditor(JTextField texField) {
                super(texField);
                verifier = new OrderingHintVerifier();
            }

            @Override
            void setErrorMessage() {
                notificationLineSupport.setErrorMessage(
                        NbBundle.getMessage(CodeTemplateParametersPanel.class, "NLS_Invalid_Ordering_Hint"));//NOI18N
            }

            @Override
            void highlightErroneousText() {
                textField.selectAll();
            }

            private class OrderingHintVerifier extends InputVerifier {

                @Override
                public boolean verify(JComponent input) {
                    JTextField orderingTextField = (JTextField) input;
                    String hint = orderingTextField.getText().trim();
                    orderingTextField.setText(hint);
                    if (hint.isEmpty()) {
                        return true;
                    }
                    try {
                        int ordering = Integer.parseInt(hint);
                        return ordering >= 0;
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                }
            }
        }

        private static class HintsValidator {

            private static boolean isValid(List<String> hints, String language) {
                switch (language) {
                    case CodeTemplateHint.JAVA_LANGUAGE: {
                        return hints.stream().noneMatch(hint ->
                                (!hint.matches("^array$") //NOI18N
                                && !hint.matches("^cast$") //NOI18N
                                && !hint.matches("^currClassFQName$") //NOI18N
                                && !hint.matches("^currClassName$") //NOI18N
                                && !hint.matches("^currMethodName$") //NOI18N
                                && !hint.matches("^currPackageName$") //NOI18N
                                && !hint.matches("^instanceof=\"[a-zA-Z_]\\w*?(\\.[a-zA-Z_]\\w*?)*?\"$") //NOI18N
                                && !hint.matches("^iterable$") //NOI18N
                                && !hint.matches("^iterableElementType$") //NOI18N
                                && !hint.matches("^leftSideType$") //NOI18N
                                && !hint.matches("^named$") //NOI18N
                                && !hint.matches("^newVarName(=[a-zA-Z_]\\w*?)?$") //NOI18N
                                && !hint.matches("^rightSideType$") //NOI18N
                                && !hint.matches("^staticImport=\"[a-zA-Z_]\\w*?(\\.[a-zA-Z_]\\w*?)*?\"$") //NOI18N
                                && !hint.matches("^type=\"[a-zA-Z_]\\w*?(\\.[a-zA-Z_]\\w*?)*?\"$") //NOI18N
                                && !hint.matches("^typeVar=\".*?\"$") //NOI18N
                                && !hint.matches("^uncaughtExceptionCatchStatements$") //NOI18N
                                && !hint.matches("^uncaughtExceptionType$") //NOI18N
                                && !hint.matches("^$"))); //NOI18N
                    }
                    case CodeTemplateHint.PHP_LANGUAGE: {
                        return hints.stream().noneMatch(hint ->
                                (!hint.matches("^allowSurround$") //NOI18N
                                && !hint.matches("^instanceof=\"[a-zA-Z_]\\w*?(\\.[a-zA-Z_]\\w*?)*?\"$") //NOI18N
                                && !hint.matches("^newVarName(=[a-zA-Z_]\\w*?)?$") //NOI18N
                                && !hint.matches("^variableFromNextAssignmentName$") //NOI18N
                                && !hint.matches("^variableFromNextAssignmentType$") //NOI18N
                                && !hint.matches("^variableFromPreviousAssignment$") //NOI18N
                                && !hint.matches("^$"))); //NOI18N
                    }
                    default: {
                        return hints.stream().noneMatch(hint -> !hint.matches("^$")); //NOI18N
                    }
                }
            }
        }
    }
}

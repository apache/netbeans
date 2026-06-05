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

package org.netbeans.modules.texttools;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

@ConvertAsProperties(
    dtd = "-//org/netbeans/modules/texttools//TextTools//EN",
    autostore = false
)
@TopComponent.Description(
    preferredID = "TextToolsTopComponent",
    iconBase="org/netbeans/modules/texttools/accessories-text-editor.png",
    persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED
)
@TopComponent.Registration(mode = "output", openAtStartup = false, position = 3400)
@Messages({
    "CTL_TextToolsAction=Text Tools",
    "CTL_TextToolsTopComponent=Text Tools Window",
    "HINT_TextToolsTopComponent=Base64/URLEncoder/Hex-Encoder"
})
public final class TextToolsTopComponent extends TopComponent {

    private static final Logger LOG = Logger.getLogger(TextToolsTopComponent.class.getName());

    public TextToolsTopComponent() {
        initComponents();
        setName(Bundle.CTL_TextToolsTopComponent());
        setToolTipText(Bundle.HINT_TextToolsTopComponent());
        ((CardLayout) outputPanel.getLayout()).show(outputPanel, "outputScrollPane");
        encodingSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if(value instanceof Charset charset) {
                    return super.getListCellRendererComponent(list, charset.displayName(), index, isSelected, cellHasFocus);
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }
        });
        for(Charset cs: Charset.availableCharsets().values()) {
            encodingSelector.addItem(cs);
        }
        encodingSelector.setSelectedItem(StandardCharsets.UTF_8);
        pipelineSelector.addActionListener((ae) -> pipeChanged());
        Font currentFont = inputTextPane.getFont();
        inputTextPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, currentFont.getSize()));
        outputTextPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, currentFont.getSize()));
        errorTextPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, currentFont.getSize()));
        pipeChanged();
    }

    private void pipeChanged() {
        GridBagConstraints gbc = ((GridBagLayout) this.getLayout()).getConstraints(buttonPanel);
        convertToFile.setVisible(true);
        convertFromFile.setVisible(true);
        convertFromToFile.setVisible(true);
        fileButton.setVisible(true);
        switch (pipelineSelector.getSelectedIndex()) {
            case 0 -> {
                // Base64 encoder
                encodingLabel.setText("Input-Encoding:");
                lineFeedSelector.setVisible(true);
                lineFeedLabel.setVisible(true);
                typeLabel.setVisible(true);
                typeSelector.setVisible(true);
                convertToFile.setVisible(false);
                gbc.gridy = 3;
            }
            case 1 -> {
                // Base64 decoder
                encodingLabel.setText("Output-Encoding:");
                lineFeedSelector.setVisible(false);
                lineFeedLabel.setVisible(false);
                typeLabel.setVisible(true);
                typeSelector.setVisible(true);
                convertFromFile.setVisible(false);
                gbc.gridy = 3;
            }
            case 2 -> {
                // URL encoder
                encodingLabel.setText("Encoding:");
                lineFeedSelector.setVisible(true);
                lineFeedLabel.setVisible(true);
                typeLabel.setVisible(false);
                typeSelector.setVisible(false);
                fileButton.setVisible(false);
                gbc.gridy = 3;
            }
            case 3 -> {
                // URL decoder
                encodingLabel.setText("Encoding:");
                lineFeedSelector.setVisible(false);
                lineFeedLabel.setVisible(false);
                typeLabel.setVisible(false);
                typeSelector.setVisible(false);
                fileButton.setVisible(false);
                gbc.gridy = 2;
            }
            case 4 -> {
                // Hex encoder
                encodingLabel.setText("Encoding:");
                lineFeedSelector.setVisible(true);
                lineFeedLabel.setVisible(true);
                typeLabel.setVisible(false);
                typeSelector.setVisible(false);
                fileButton.setVisible(false);
                gbc.gridy = 3;
            }
            case 5 -> {
                // Hex decoder
                encodingLabel.setText("Encoding:");
                lineFeedSelector.setVisible(false);
                lineFeedLabel.setVisible(false);
                typeLabel.setVisible(false);
                typeSelector.setVisible(false);
                fileButton.setVisible(false);
                gbc.gridy = 2;
            }
        }
        ((GridBagLayout) this.getLayout()).setConstraints(buttonPanel, gbc);
        this.revalidate();
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        fileMenu = new javax.swing.JPopupMenu();
        convertFromFile = new javax.swing.JMenuItem();
        convertToFile = new javax.swing.JMenuItem();
        convertFromToFile = new javax.swing.JMenuItem();
        pipelineLabel = new javax.swing.JLabel();
        pipelineSelector = new javax.swing.JComboBox<>();
        jSplitPane1 = new javax.swing.JSplitPane();
        inputPanel = new javax.swing.JPanel();
        inputScrollPane = new javax.swing.JScrollPane();
        inputTextPane = new javax.swing.JTextArea();
        outputPanel = new javax.swing.JPanel();
        errorScrollPane = new javax.swing.JScrollPane();
        errorTextPane = new javax.swing.JTextArea();
        outputScrollPane = new javax.swing.JScrollPane();
        outputTextPane = new javax.swing.JTextArea();
        encodingLabel = new javax.swing.JLabel();
        encodingSelector = new javax.swing.JComboBox<>();
        typeSelector = new javax.swing.JComboBox<>();
        typeLabel = new javax.swing.JLabel();
        lineFeedLabel = new javax.swing.JLabel();
        lineFeedSelector = new javax.swing.JComboBox<>();
        buttonPanel = new javax.swing.JPanel();
        fileButton = new javax.swing.JButton();
        convertButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(convertFromFile, org.openide.util.NbBundle.getMessage(TextToolsTopComponent.class, "TextToolsTopComponent.convertFromFile.text")); // NOI18N
        convertFromFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                convertFromFileActionPerformed(evt);
            }
        });
        fileMenu.add(convertFromFile);

        org.openide.awt.Mnemonics.setLocalizedText(convertToFile, org.openide.util.NbBundle.getMessage(TextToolsTopComponent.class, "TextToolsTopComponent.convertToFile.text")); // NOI18N
        convertToFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                convertToFileActionPerformed(evt);
            }
        });
        fileMenu.add(convertToFile);

        org.openide.awt.Mnemonics.setLocalizedText(convertFromToFile, org.openide.util.NbBundle.getMessage(TextToolsTopComponent.class, "TextToolsTopComponent.convertFromToFile.text")); // NOI18N
        convertFromToFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                convertFromToFileActionPerformed(evt);
            }
        });
        fileMenu.add(convertFromToFile);

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(pipelineLabel, org.openide.util.NbBundle.getMessage(TextToolsTopComponent.class, "TextToolsTopComponent.pipelineLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(pipelineLabel, gridBagConstraints);

        pipelineSelector.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Base64 Encoder", "Base64 Decoder", "URLEncoder", "URLDecoder", "Hex Encoder", "Hex Decoder" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(pipelineSelector, gridBagConstraints);

        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setOneTouchExpandable(true);
        jSplitPane1.setPreferredSize(new java.awt.Dimension(320, 27));

        inputPanel.setLayout(new java.awt.BorderLayout());

        inputTextPane.setColumns(20);
        inputTextPane.setRows(5);
        inputScrollPane.setViewportView(inputTextPane);

        inputPanel.add(inputScrollPane, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(inputPanel);

        outputPanel.setLayout(new java.awt.CardLayout());

        errorTextPane.setForeground(new java.awt.Color(255, 0, 51));
        errorTextPane.setLineWrap(true);
        errorTextPane.setWrapStyleWord(true);
        errorScrollPane.setViewportView(errorTextPane);

        outputPanel.add(errorScrollPane, "errorScrollPane");

        outputTextPane.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        outputTextPane.setLineWrap(true);
        outputScrollPane.setViewportView(outputTextPane);

        outputPanel.add(outputScrollPane, "outputScrollPane");

        jSplitPane1.setRightComponent(outputPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(jSplitPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(encodingLabel, org.openide.util.NbBundle.getMessage(TextToolsTopComponent.class, "TextToolsTopComponent.encodingLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(encodingLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(encodingSelector, gridBagConstraints);

        typeSelector.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Basic", "URL and Filename safe", "MIME" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(typeSelector, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(typeLabel, org.openide.util.NbBundle.getMessage(TextToolsTopComponent.class, "TextToolsTopComponent.typeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(typeLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(lineFeedLabel, org.openide.util.NbBundle.getMessage(TextToolsTopComponent.class, "TextToolsTopComponent.lineFeedLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        add(lineFeedLabel, gridBagConstraints);

        lineFeedSelector.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Unix, Linux, Android, macOS", "Windows, DOS, OS/2", "Mac OS Classic" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(lineFeedSelector, gridBagConstraints);

        buttonPanel.setOpaque(false);
        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING, 5, 0);
        flowLayout1.setAlignOnBaseline(true);
        buttonPanel.setLayout(flowLayout1);

        org.openide.awt.Mnemonics.setLocalizedText(fileButton, org.openide.util.NbBundle.getMessage(TextToolsTopComponent.class, "TextToolsTopComponent.fileButton.text")); // NOI18N
        fileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(fileButton);

        org.openide.awt.Mnemonics.setLocalizedText(convertButton, org.openide.util.NbBundle.getMessage(TextToolsTopComponent.class, "TextToolsTopComponent.convertButton.text")); // NOI18N
        convertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                convertButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(convertButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void convertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_convertButtonActionPerformed
        errorTextPane.setText("");
        outputTextPane.setText("");
        ((CardLayout) outputPanel.getLayout()).show(outputPanel, "outputScrollPane");
        try {
            Charset cs = (Charset) encodingSelector.getSelectedItem();
            String outputText = "";
            if (pipelineSelector.getSelectedIndex() == 0) { // Base64 Encoder
                String inputText = inputTextPane.getText().replace("\n", getChosenLineFeed());
                switch (typeSelector.getSelectedIndex()) {
                    case 0 -> outputText = Base64.getEncoder().encodeToString(inputText.getBytes(cs));
                    case 1 -> outputText = Base64.getUrlEncoder().encodeToString(inputText.getBytes(cs));
                    case 2 -> outputText = Base64.getMimeEncoder().encodeToString(inputText.getBytes(cs));
                }
            } else if (pipelineSelector.getSelectedIndex() == 1) { // Base64 Decoder
                String inputText = inputTextPane.getText();
                switch (typeSelector.getSelectedIndex()) {
                    case 0 -> outputText = new String(Base64.getDecoder().decode(inputText), cs);
                    case 1 -> outputText = new String(Base64.getUrlDecoder().decode(inputText.getBytes(cs)), cs);
                    case 2 -> outputText = new String(Base64.getMimeDecoder().decode(inputText.getBytes(cs)), cs);
                }
            } else if (pipelineSelector.getSelectedIndex() == 2) { // URL Encoder
                String inputText = inputTextPane.getText().replace("\n", getChosenLineFeed());
                outputText = URLEncoder.encode(inputText, cs.name());
            } else if (pipelineSelector.getSelectedIndex() == 3) { // URL Decoder
                String inputText = inputTextPane.getText();
                outputText = URLDecoder.decode(inputText, cs.name());
            } else if (pipelineSelector.getSelectedIndex() == 4) { // Hex Encoder
                String inputText = inputTextPane.getText().replace("\n", getChosenLineFeed());
                byte[] inputData = inputText.getBytes(cs);
                StringBuilder sb = new StringBuilder(inputData.length * 2);
                for(int i = 0; i < inputData.length; i++) {
                    byte inputByte = inputData[i];
                    sb.append(String.format("%02X", inputByte));
                }
                outputText = sb.toString();
            } else if (pipelineSelector.getSelectedIndex() == 5) { // Hex Decoder
                String inputText = inputTextPane.getText();
                if(inputText.length() % 2 != 0) {
                    throw new IllegalArgumentException("Length of input must even");
                }
                byte[] outputData = new byte[inputText.length()];
                for(int i = 0; i < inputText.length(); i += 2) {
                    outputData[i / 2] = (byte) Integer.parseInt(inputText.substring(i, i + 2), 16);
                }
                outputText = new String(outputData, cs);
            }
            outputTextPane.setText(outputText);
        } catch (UnsupportedEncodingException | RuntimeException ex) {
            errorTextPane.setText(ex.getMessage());
            ((CardLayout) outputPanel.getLayout()).show(outputPanel, "errorScrollPane");
        }
    }//GEN-LAST:event_convertButtonActionPerformed

    private static File lastSelectedDir = null;

    private void convertFromFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_convertFromFileActionPerformed
        outputTextPane.setText("");
        errorTextPane.setText("");
        ((CardLayout) outputPanel.getLayout()).show(outputPanel, "outputScrollPane");
        File targetFile = queryOpenFile();
        if(targetFile != null) {
            try(FileInputStream fos = new FileInputStream(targetFile)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream((int) targetFile.length() * 2);
                OutputStream os;
                switch (typeSelector.getSelectedIndex()) {
                    default:
                    case 0:
                        if(targetFile.length() > 128 * 1024) {
                            JOptionPane.showMessageDialog(this, "File is larger than 128kB - please use the 'From/to file' option");
                            return;
                        }
                        os = Base64.getEncoder().wrap(baos);
                        break;
                    case 1:
                        if(targetFile.length() > 128 * 1024) {
                            JOptionPane.showMessageDialog(this, "File is larger than 128kB - please use the 'From/to file' option");
                            return;
                        }
                        os = Base64.getUrlEncoder().wrap(baos);
                        break;
                    case 2:
                        if(targetFile.length() > 512 * 1024) {
                            JOptionPane.showMessageDialog(this, "File is larger than 128kB - please use the 'From/to file' option");
                            return;
                        }
                        os = Base64.getMimeEncoder().wrap(baos);
                        break;
                }
                byte[] buffer = new byte[1024];
                int read;
                while((read = fos.read(buffer)) >= 0) {
                    os.write(buffer, 0, read);
                }
                outputTextPane.setText(baos.toString(StandardCharsets.ISO_8859_1.name()));
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Failed to write file", ex);
                errorTextPane.setText(ex.getMessage());
                ((CardLayout) outputPanel.getLayout()).show(outputPanel, "errorScrollPane");
            }
        }
    }//GEN-LAST:event_convertFromFileActionPerformed

    private void convertToFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_convertToFileActionPerformed
        outputTextPane.setText("");
        errorTextPane.setText("");
        ((CardLayout) outputPanel.getLayout()).show(outputPanel, "outputScrollPane");
        File targetFile = querySaveFile();
        if(targetFile != null) {
            try(FileOutputStream fos = new FileOutputStream(targetFile)) {
                String inputText = inputTextPane.getText();
                switch (typeSelector.getSelectedIndex()) {
                    case 0 -> fos.write(Base64.getDecoder().decode(inputText));
                    case 1 -> fos.write(Base64.getUrlDecoder().decode(inputText));
                    case 2 -> fos.write(Base64.getMimeDecoder().decode(inputText));
                }
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Failed to write file", ex);
                errorTextPane.setText(ex.getMessage());
                ((CardLayout) outputPanel.getLayout()).show(outputPanel, "errorScrollPane");
            }
        }
    }//GEN-LAST:event_convertToFileActionPerformed

    private File querySaveFile() throws HeadlessException {
        JFileChooser jfc = new JFileChooser();
        jfc.setCurrentDirectory(lastSelectedDir);
        int result = jfc.showSaveDialog(jfc);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (selectedFile.exists()) {
                int confirmResult = JOptionPane.showConfirmDialog(this, "File\n" + selectedFile.getAbsolutePath() + "\nalready exists. Overwrite?", "Overwrite file?", JOptionPane.OK_CANCEL_OPTION);
                if (confirmResult != JOptionPane.OK_OPTION) {
                    return null;
                }
            }
            lastSelectedDir = jfc.getCurrentDirectory();
            return selectedFile;
        }
        return null;
    }

    private File queryOpenFile() throws HeadlessException {
        JFileChooser jfc = new JFileChooser();
        jfc.setCurrentDirectory(lastSelectedDir);
        int result = jfc.showOpenDialog(jfc);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            lastSelectedDir = jfc.getCurrentDirectory();
            if (! selectedFile.exists()) {
                return null;
            }
            return selectedFile;
        }
        return null;
    }

    private void convertFromToFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_convertFromToFileActionPerformed
        outputTextPane.setText("");
        errorTextPane.setText("");
        ((CardLayout) outputPanel.getLayout()).show(outputPanel, "outputScrollPane");
        File sourceFile = queryOpenFile();
        if(sourceFile == null || ! sourceFile.exists()) {
            return;
        }
        File targetFile = querySaveFile();
        if(targetFile == null) {
            return;
        }

        final AtomicBoolean canceled = new AtomicBoolean();

        ProgressHandle handle = ProgressHandle.createHandle("Encoding file", () -> {canceled.set(true); return true;});

        new SwingWorker<Object,long[]>() {
            @Override
            @SuppressWarnings({
                "ConfusingArrayVararg",
                "PrimitiveArrayArgumentToVariableArgMethod"
            })
            protected Object doInBackground() throws Exception {
                long total = sourceFile.length();
                publish(new long[] {total, -1});
                try (FileInputStream fis = new FileInputStream(sourceFile);
                    FileOutputStream fos = new FileOutputStream(targetFile)) {
                    if (pipelineSelector.getSelectedIndex() == 0) {
                        OutputStream os;
                        switch (typeSelector.getSelectedIndex()) {
                            case 0 -> os = Base64.getEncoder().wrap(fos);
                            case 1 -> os = Base64.getUrlEncoder().wrap(fos);
                            case 2 -> os = Base64.getMimeEncoder().wrap(fos);
                            default -> throw new IOException("Invalid conversation: " + typeSelector.getSelectedIndex() + " / " + typeSelector.getSelectedItem());
                        }

                        transfer(fis, os, canceled, (i) -> publish(new long[] {total, i}));
                    } else if (pipelineSelector.getSelectedIndex() == 1) {
                        InputStream is;
                        switch (typeSelector.getSelectedIndex()) {
                            case 0 -> is = Base64.getDecoder().wrap(fis);
                            case 1 -> is = Base64.getUrlDecoder().wrap(fis);
                            case 2 -> is = Base64.getMimeDecoder().wrap(fis);
                            default -> throw new IOException("Invalid conversation: " + typeSelector.getSelectedIndex() + " / " + typeSelector.getSelectedItem());
                        }

                        transfer(is, fos, canceled, (i) -> publish(new long[] {total, i}));
                    }
                }
                return null;
            }

            @Override
            protected void process(List<long[]> list) {
                long[] lastElement = list.get(list.size() - 1);
                int val1;
                int val2;
                if(Long.compare(lastElement[0], Integer.MAX_VALUE) > 0) {
                    val1 = (int) (lastElement[0] / 1000L);
                    val2 = (int) (lastElement[1] / 1000L);
                } else {
                    val1 = (int) lastElement[0];
                    val2 = (int) lastElement[1];
                }
                handle.switchToDeterminate(val1);
                handle.progress(val2);
            }

            @Override
            protected void done() {
                handle.finish();
                try {
                    get();
                } catch (InterruptedException | ExecutionException | RuntimeException ex) {
                    LOG.log(Level.WARNING, "Failed to write file", ex);
                    errorTextPane.setText(ex.getMessage());
                    ((CardLayout) outputPanel.getLayout()).show(outputPanel, "errorScrollPane");
                }
            }

        }.execute();
        handle.start();
        handle.switchToIndeterminate();
    }//GEN-LAST:event_convertFromToFileActionPerformed

    private void transfer(final InputStream fis, final OutputStream os, final AtomicBoolean canceled, final Consumer<Long> progressListener) throws IOException {
        byte[] buffer = new byte[256 * 1024];
        long offset = 0;
        int read;
        while((read = fis.read(buffer)) >= 0) {
            if(canceled.get()) {
                break;
            }
            os.write(buffer, 0, read);
            offset += read;
            if(progressListener != null) {
                progressListener.accept(offset);
            }
        }
    }

    private void fileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileButtonActionPerformed
        fileMenu.show(fileButton, 0, fileButton.getHeight());
    }//GEN-LAST:event_fileButtonActionPerformed

    private String getChosenLineFeed() {
        switch(lineFeedSelector.getSelectedIndex()) {
            default:
            case 0: return "\n";
            case 1: return "\r\n";
            case 2: return "\r";
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton convertButton;
    private javax.swing.JMenuItem convertFromFile;
    private javax.swing.JMenuItem convertFromToFile;
    private javax.swing.JMenuItem convertToFile;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JComboBox<Charset> encodingSelector;
    private javax.swing.JScrollPane errorScrollPane;
    private javax.swing.JTextArea errorTextPane;
    private javax.swing.JButton fileButton;
    private javax.swing.JPopupMenu fileMenu;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JScrollPane inputScrollPane;
    private javax.swing.JTextArea inputTextPane;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel lineFeedLabel;
    private javax.swing.JComboBox<String> lineFeedSelector;
    private javax.swing.JPanel outputPanel;
    private javax.swing.JScrollPane outputScrollPane;
    private javax.swing.JTextArea outputTextPane;
    private javax.swing.JLabel pipelineLabel;
    private javax.swing.JComboBox<String> pipelineSelector;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JComboBox<String> typeSelector;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    void writeProperties(java.util.Properties p) {
        p.setProperty("version", "1.0");
        switch(pipelineSelector.getSelectedIndex()) {
            case 0 -> p.setProperty("pipeline", "base64encoder");
            case 1 -> p.setProperty("pipeline", "base64decoder");
            case 2 -> p.setProperty("pipeline", "urlEncoder");
            case 3 -> p.setProperty("pipeline", "urlDecoder");
            case 4 -> p.setProperty("pipeline", "hexEncoder");
            case 5 -> p.setProperty("pipeline", "hexDecoder");
        }
        p.setProperty("encoding", ((Charset) encodingSelector.getSelectedItem()).name());
        switch(lineFeedSelector.getSelectedIndex()) {
            case 0 -> p.setProperty("linefeed", "unix");
            case 1 -> p.setProperty("linefeed", "windows");
            case 2 -> p.setProperty("linefeed", "mac");
        }
        switch(typeSelector.getSelectedIndex()) {
            case 0 -> p.setProperty("base64Type", "basic");
            case 1 -> p.setProperty("base64Type", "url");
            case 2 -> p.setProperty("base64Type", "mime");
        }
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        if("1.0".equals(version)) {
            switch(p.getProperty("pipeline", "")) {
                case "base64encoder" -> pipelineSelector.setSelectedIndex(0);
                case "base64decoder" -> pipelineSelector.setSelectedIndex(1);
                case "urlEncoder" -> pipelineSelector.setSelectedIndex(2);
                case "urlDecoder" -> pipelineSelector.setSelectedIndex(3);
                case "hexEncoder" -> pipelineSelector.setSelectedIndex(4);
                case "hexDecoder" -> pipelineSelector.setSelectedIndex(5);
                default -> pipelineSelector.setSelectedIndex(0);
            }
            String encoding = p.getProperty("encoding");
            if(encoding != null && Charset.isSupported(encoding)) {
                encodingSelector.setSelectedItem(Charset.forName(encoding));
            }
            switch(p.getProperty("linefeed")) {
                case "unix" -> lineFeedSelector.setSelectedIndex(0);
                case "windows" -> lineFeedSelector.setSelectedIndex(1);
                case "mac" -> lineFeedSelector.setSelectedIndex(2);
                default -> lineFeedSelector.setSelectedIndex(0);
            }
            switch(p.getProperty("base64Type")) {
                case "basic" -> typeSelector.setSelectedIndex(0);
                case "url" -> typeSelector.setSelectedIndex(1);
                case "mime" -> typeSelector.setSelectedIndex(2);
                default -> typeSelector.setSelectedIndex(0);
            }
        }
    }

    @ActionID(category = "Window", id = "org.netbeans.modules.texttools.TextToolsTopComponent.OpenAction")
    @ActionReference(
        path = "Menu/Window/Tools",
        position = 20100,
        separatorBefore = 20000
    )
    @ActionRegistration(
        displayName = "#CTL_TextToolsAction",
        iconBase = "org/netbeans/modules/texttools/accessories-text-editor.png"
    )
    public static class OpenAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ev) {
            TextToolsTopComponent tttc = new TextToolsTopComponent();
            tttc.open();
            Mode output = WindowManager.getDefault().findMode("output");
            if(output != null) {
                output.dockInto(tttc);
            }
            tttc.requestActive();
        }
    }
}

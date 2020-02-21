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
package org.netbeans.modules.cnd.makefile.wizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.ui.CndUIUtilities;
import org.openide.util.NbBundle;

/**
 * Create the third panel in the Makefile wizard.
 */
public class MakefileReviewPanel extends MakefileWizardPanel
        implements FocusListener {

    /** Serial version number */
    static final long serialVersionUID = 6675031915575184904L;

    // the fields in the first panel...
    private JLabel reviewLabel;
    private JTextArea reviewText;
    private JScrollPane reviewSP;
    /** Store the Finish button's label */
    private String finishLabel;
    /** Store the Finish button's mnemonic */
    private int finishMnemonic;
    /** Build the summary text in this StringBuffer */
    private StringBuffer summary;
    private boolean initialized;

    /* Flag to disable text selection when panel is first displayed */
    private boolean enableTextSelection = false;
    ActionListener finishButtonListener = null;

    /**
     * Constructor for the Makefile name panel. Remember, most of the panel is
     * inherited from WizardDescriptor.
     */
    MakefileReviewPanel(MakefileWizard wd) {
        super(wd);
        String subtitle = new String(getString("LBL_MakefileReviewPanel")); // NOI18N
        setSubTitle(subtitle);
        this.getAccessibleContext().setAccessibleDescription(subtitle +
                getString("ACSD_MakefileReview")); // NOI18N
        initialized = false;
    }

    /** Defer widget creation until the panel needs to be displayed */
    private void create() {

        setLayout(new GridBagLayout());

        // Create the review label
        reviewLabel = new JLabel();
        reviewLabel.setText(NbBundle.getMessage(MakefileReviewPanel.class,
                "LBL_Summary")); // NOI18N
        reviewLabel.setDisplayedMnemonic((NbBundle.getMessage(
                MakefileReviewPanel.class,
                "MNEM_Summary")).charAt(0)); // NOI18N

        // Set the GridBagLayout constraints for the label
        GridBagConstraints grid = new GridBagConstraints();
        grid.anchor = GridBagConstraints.NORTHWEST;
        grid.gridx = 0;
        grid.gridy = 1;
        grid.gridheight = 1;
        grid.insets = new java.awt.Insets(0, 0, 5, 0);
        add(reviewLabel, grid);

        // Create the component.
        reviewText = new JTextArea();
        reviewText.setEditable(false);
        reviewText.getCaret().setVisible(false);
        reviewText.getCaret().setSelectionVisible(false);
        reviewText.setBackground(getBackground());
        reviewText.addFocusListener(this);
        reviewLabel.setLabelFor(reviewText);

        reviewSP = new JScrollPane(reviewText);
        reviewSP.getViewport().setBackground(reviewText.getBackground());

        // Set the GridBagLayout constraints.
        grid = new GridBagConstraints();
        grid.gridx = 0;
        grid.gridy = 2;
        grid.weightx = 100.0;
        grid.weighty = 100.0;
        grid.fill = GridBagConstraints.BOTH;
        add(reviewSP, grid);

        if (CndPathUtilities.IfdefDiagnostics) {
            reviewText.addKeyListener(new KeyAdapter() {

                @Override
                public void keyPressed(KeyEvent ev) {

                    int mods = ev.getModifiers();
                    if (ev.isControlDown() && ev.getKeyCode() == KeyEvent.VK_P) {
                        getMakefileData().dump();
                    }
                }
            });
        }
    }

    /** Summarize the information gathered in the MakefileData */
    private String getSummaryString() {
        MakefileData md = getMakefileData();
        String base = md.getBaseDirectory(MakefileData.EXPAND);
        String makefile = md.getMakefileName();
        String dir = getMakefileDirectory(base, makefile);
        String name = getMakefileName(makefile);

        summary = new StringBuffer(1024);
        append("LBL_MakefileSummaryDir", dir); // NOI18N
        append("LBL_MakefileSummaryName", name);	// NOI18N
        append("LBL_BuildInstructions"); // NOI18N
        append("LBL_BuildInstruction1", name, dir); // NOI18N
        append("LBL_BuildInstruction2", name); // NOI18N

        append("LBL_MakefileSpecifics"); // NOI18N
        if (md.getTargetList().size() == 1 || md.getMakefileType() <
                MakefileData.COMPLEX_MAKEFILE_TYPE) {
            TargetData td = md.getTargetList().get(0);

            if (td.isExecutable()) {
                append("LBL_SingleExe", td.getName());			// NOI18N
            } else if (td.isArchive()) {
                append("LBL_SingleArchive", td.getName());		// NOI18N
            } else if (td.isSharedLib()) {
                append("LBL_SingleSharedLib", td.getName());		// NOI18N
            } else if (td.isMakeTarget()) {
                append("LBL_SingleMake", td.getName());			// NOI18N
            } else if (td.isCustomTarget()) {
                append("LBL_SingleCustom", td.getName());		// NOI18N
            } else if (CndPathUtilities.IfdefDiagnostics) {
                System.out.println(
                        "Error: Unknown target type in summary");	// NOI18N
            }
            if (td.getSourcesList() == null) {
                append("LBL_SingleTargetSrcs", Integer.valueOf(0)); // NOI18N
            } else {
                if (td.getSourcesList().length == 1) {
                    append("LBL_SingleTargetSrcs1");			// NOI18N
                } else {
                    append("LBL_SingleTargetSrcs", // NOI18N
                            Integer.valueOf(td.getSourcesList().length));
                }
            }
        } else {
            List<TargetData> tlist = md.getTargetList();

            append("LBL_MultiTarget");					// NOI18N
            for (int i = 0; i < tlist.size(); i++) {
                TargetData td = tlist.get(i);

                int srcCnt;
                if (td.getSourcesList() == null) {
                    srcCnt = 0;
                } else {
                    srcCnt = td.getSourcesList().length;
                }

                if (td.isExecutable()) {
                    if (srcCnt == 1) {
                        append("LBL_MultiExe1", td.getName());		// NOI18N
                    } else {
                        append("LBL_MultiExe", td.getName(), // NOI18N
                                Integer.valueOf(srcCnt));
                    }
                } else if (td.isArchive()) {
                    if (srcCnt == 1) {
                        append("LBL_MultiArchive1", td.getName());	// NOI18N
                    } else {
                        append("LBL_MultiArchive", td.getName(), // NOI18N
                                Integer.valueOf(srcCnt));
                    }
                } else if (td.isSharedLib()) {
                    if (srcCnt == 1) {
                        append("LBL_MultiSharedLib1", td.getName());	// NOI18N
                    } else {
                        append("LBL_MultiSharedLib", td.getName(), // NOI18N
                                Integer.valueOf(srcCnt));
                    }
                } else if (td.isMakeTarget()) {
                    append("LBL_MultiMake", td.getName());		// NOI18N
                } else if (td.isCustomTarget()) {
                    if (srcCnt == 1) {
                        append("LBL_MultiCustom1", td.getName());	// NOI18N
                    } else {
                        append("LBL_MultiCustom", td.getName(), // NOI18N
                                Integer.valueOf(srcCnt));
                    }
                } else if (CndPathUtilities.IfdefDiagnostics) {
                    System.out.println(
                            "Error: Unknown target type in summary");	// NOI18N
                }
            }
        }

        if (isDebug() && !isOptimize()) {
            append("LBL_DebugCompile");					// NOI18N
        } else if (isDebug() && isOptimize()) {
            append("LBL_DebugOptCompile");				// NOI18N
        } else if (isOptimize()) {
            append("LBL_OptimizeCompile");				// NOI18N
        } else if (CndPathUtilities.IfdefDiagnostics) {
            append("LBL_NoOptDebugCompile");				// NOI18N
        }

        validateAllData();
        append("LBL_Finish");						// NOI18N

        return summary.toString();
    }

    private String getMakefileDirectory(String base, String makefile) {

        // First, make sure makefile is an absolute path
        if (makefile.charAt(0) != File.separatorChar) {
            makefile = base + File.separator + makefile;
        }

        // Now get its canonical form
        File file = new File(makefile);
        String path;

        try {
            path = file.getCanonicalPath();
        } catch (IOException ex) {
            return makefile.substring(0, makefile.lastIndexOf(File.separator) - 1);
        }

        return path.substring(0, path.lastIndexOf(File.separator));
    }

    private String getMakefileName(String makefile) {

        int pos = makefile.lastIndexOf(File.separator);
        if (pos >= 0) {
            return makefile.substring(pos + 1);
        } else {
            return makefile;
        }
    }

    /** Shortcut for checking debug flags */
    private boolean isDebug() {
        MakefileData md = getMakefileData();
        CompilerFlags copts = md.getCompilerFlags();

        if (md.getMakefileType() < MakefileData.COMPLEX_MAKEFILE_TYPE) {
            return copts.isSimpleDebug();
        } else if (copts.getOptionSource() == OptionSource.DEVELOPMENT) {
            return true;
        }

        return false;
    }

    /** Shortcut for checking optization */
    private boolean isOptimize() {
        MakefileData md = getMakefileData();
        CompilerFlags copts = md.getCompilerFlags();

        if (md.getMakefileType() < MakefileData.COMPLEX_MAKEFILE_TYPE) {
            return copts.isSimpleOptimize();
        } else if (copts.getOptionSource() == OptionSource.FINAL) {
            return copts.isFinalOptimize();
        } else if (copts.getOptionSource() == OptionSource.DEVELOPMENT) {
            return !copts.isDevelDebug();
        } else {
            return false;
        }
    }

    /**
     *  Validate the MakefileData and all targets. Add any warnings to the
     *  summary in the Warnings area.
     */
    private void validateAllData() {
        ArrayList msgs = MakefileWizard.getMakefileWizard().validateAllData();

        append("LBL_Warnings");						// NOI18N
        if (msgs.size() == 0) {
            append("LBL_NoWarnings");					// NOI18N
        } else {
            for (int i = 0; i < msgs.size(); i++) {
                append(msgs.get(i));
            }
        }
    }

    /** Generate the summary informatin and display it */
    @Override
    public void addNotify() {
        if (!initialized) {
            create();
            initialized = true;
        }

        super.addNotify();
        reviewText.setText(getSummaryString());
        reviewText.setCaretPosition(0);

        MakefileWizard mw = MakefileWizard.getMakefileWizard();
        mw.getNextButton().setEnabled(false);
        mw.setFinishEnabled(true);
        JButton finishButton = mw.getFinishButton();

        finishLabel = finishButton.getText();
        finishMnemonic = finishButton.getMnemonic();
        finishButton.setText(mw.getFinishLabel());
        finishButton.setMnemonic(mw.getFinishMnemonic());
        finishButton.setEnabled(true);
        getRootPane().setDefaultButton(finishButton);
        CndUIUtilities.requestFocus(finishButton);
        enableTextSelection = false;

        if (finishButtonListener == null) {
            finishButtonListener = new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    finishButtonListenerActionPerformed();
                }
            };
        }
        finishButton.addActionListener(finishButtonListener);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();

        JButton finishButton =
                MakefileWizard.getMakefileWizard().getFinishButton();
        finishButton.setText(finishLabel);
        finishButton.setMnemonic(finishMnemonic);
        getRootPane().setDefaultButton(MakefileWizard.getMakefileWizard().getNextButton());

        finishButton.removeActionListener(finishButtonListener);
    }

    public void finishButtonListenerActionPerformed() {
        MakefileWizard mw = MakefileWizard.getMakefileWizard();
        mw.setFinishClosingEnabled(true);
    }

    // The following methods are related to displaying summary information
    /** Append the a single message to the StringBuffer */
    private void append(Object msg) {
        summary.append(msg.toString());
    }

    /** Append the a single message to the StringBuffer */
    private void append(String msg) {
        summary.append(
                NbBundle.getBundle(MakefileReviewPanel.class).getString(msg));
    }

    /** Append the a message with a single arg to the StringBuffer */
    private void append(String msg, Object arg1) {

        if (arg1 instanceof Integer && ((Integer) arg1).intValue() == 1) {
            msg = msg + "1";						// NOI18N
        }

        summary.append(
                NbBundle.getMessage(MakefileReviewPanel.class, msg, arg1));
    }

    /** Append the a message with a single arg to the StringBuffer */
    private void append(String msg, Object arg1, Object arg2) {

        if (arg2 instanceof Integer && ((Integer) arg2).intValue() == 1) {
            msg = msg + "1";						// NOI18N
        }

        summary.append(NbBundle.getMessage(MakefileReviewPanel.class,
                msg, arg1, arg2));
    }

    public void focusGained(FocusEvent evt) {
        // don't select text when panel is first displayed otherwise
        // the text appears to "flash"
	/* This is causing the text to appear selected when gaining focus. Is this intentionally?
         * Removed for now.
         */
        /*
        if(enableTextSelection)
        reviewText.selectAll();
        enableTextSelection = true;
         */
    }

    public void focusLost(FocusEvent evt) {
        /* This is causing the text to appear selected when gaining focus. Is this intentionally?
         * Removed for now.
         */
        /*
        reviewText.setSelectionEnd(0);
         */
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.makefile.wizard;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.openide.text.NbDocument;

/**
 *  Create a panel used for gathering the binary name and output directory for
 *  the simple application cases (all Makefile types other than complex).
 */
public class CustomTargetPanel extends MakefileWizardPanel {

    /** Serial version number */
    static final long serialVersionUID = -5820333613938630399L;
    /** Dependencies for this target */
    private JTextField dependsOn;
    /**
     *  An editor showing both the target name, dependencies, and actions.
     *  The target name and dependencies are in a guarded block. However, the
     *  user may add actions/commands after that.
     */
    private JEditorPane actionText;
    /** The Document behind actionText */
    private StyledDocument actionDoc;
    /** String version of dependsOn */
    private String depends;
    /** Tells if we have created the gui or not */
    private boolean initialized;
    /** Suppress inserting dependencies into actionText if set */
    private boolean inAddNotify = false;
    /** Store the target key */
    private int key;

    /**
     *  Constructor for the custom target panel.
     */
    CustomTargetPanel(MakefileWizard wd) {
        super(wd);
        String subtitle = getString("LBL_CustomTargetPanel"); // NOI18N
        setSubTitle(subtitle);
        this.getAccessibleContext().setAccessibleDescription(subtitle);
        initialized = false;
    }

    /** Defer widget creation until the panel needs to be displayed */
    private void create() {

        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints grid = new GridBagConstraints();

        // Set the GridBagLayout constraints for the first label
        JLabel label = new JLabel(getString("LBL_DependsOn"));		// NOI18N
        label.setDisplayedMnemonic(
                getString("MNEM_DependsOn").charAt(0));		// NOI18N
        grid.anchor = GridBagConstraints.WEST;
        grid.gridx = 0;
        grid.gridy = 0;
        panel.add(label, grid);

        // Now do the textfield
        dependsOn = new JTextField();
        label.setLabelFor(dependsOn);
        grid.gridx = 1;
        grid.gridwidth = GridBagConstraints.REMAINDER;
        grid.weightx = 1.0;
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.anchor = GridBagConstraints.NORTHWEST;
        grid.insets = new Insets(0, 5, 0, 0);
        panel.add(dependsOn, grid);

        // Create the actionText's label
        label = new JLabel(getString("LBL_ActionDisplay"));		// NOI18N
        label.setDisplayedMnemonic(
                getString("MNEM_ActionDisplay").charAt(0));	// NOI18N
        grid.gridx = 0;
        grid.gridy = 1;
        grid.gridwidth = 1;
        grid.weightx = 0.0;
        grid.fill = GridBagConstraints.NONE;
        grid.insets = new Insets(10, 0, 0, 0);
        panel.add(label, grid);
        add(panel, BorderLayout.NORTH);

        // Finally, create the actionText
        actionText = new JEditorPane(MIMENames.MAKEFILE_MIME_TYPE, ""); // NOI18N
        label.setLabelFor(actionText);
        // FIXUP:
        if (!(actionText.getDocument() instanceof StyledDocument)) {
            actionText.setDocument(new DefaultStyledDocument());
        }
        actionDoc = (StyledDocument) actionText.getDocument();
        JScrollPane s = new JScrollPane(actionText);

        s.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        s.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        s.setMinimumSize(s.getPreferredSize());
        add(s, BorderLayout.CENTER);

        dependsOn.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
            }

            public void insertUpdate(DocumentEvent e) {
                insertActionDisplay(e);
            }

            public void removeUpdate(DocumentEvent e) {
                removeActionDisplay(e);
            }
        });
    }

    /** Insert the new dependency characters into actionText */
    private void insertActionDisplay(DocumentEvent e) {

        if (!inAddNotify) {
            TargetData target = getMakefileData().getCurrentTarget();
            int off = e.getOffset();
            String nueDependsOn = dependsOn.getText();
            String nuePart = nueDependsOn.substring(off, off + e.getLength());

            off += target.getName().length() + 2;
            Element elem = actionDoc.getParagraphElement(0);
            NbDocument.unmarkGuarded(actionDoc, elem.getStartOffset(),
                    elem.getEndOffset() - elem.getStartOffset() + 1);
            try {
                actionDoc.insertString(off, nuePart, null);
            } catch (BadLocationException ex) {
                if (CndPathUtilities.IfdefDiagnostics) {
                    System.out.println("BadLocationException: " + // NOI18N
                            ex.getMessage() +
                            "\n\toffset = " + ex.offsetRequested());	// NOI18N
                    ex.printStackTrace();
                }
            }
            depends = nueDependsOn;
            NbDocument.markGuarded(actionDoc, elem.getStartOffset(),
                    target.getName().length() + depends.length() + 3);
        }
    }

    /** Remove characters from actionText */
    private void removeActionDisplay(DocumentEvent e) {

        if (!inAddNotify) {
            TargetData target = getMakefileData().getCurrentTarget();
            int off = target.getName().length() + 2 + e.getOffset();

            Element elem = actionDoc.getParagraphElement(0);
            NbDocument.unmarkGuarded(actionDoc, elem.getStartOffset(),
                    elem.getEndOffset() - elem.getStartOffset() + 1);
            try {
                actionDoc.remove(off, e.getLength());
            } catch (BadLocationException ex) {
                if (CndPathUtilities.IfdefDiagnostics) {
                    System.out.println("BadLocationException[6]: " + // NOI18N
                            ex.getMessage() +
                            "\n\toffset = " + ex.offsetRequested());	// NOI18N
                    ex.printStackTrace();
                }
            }
            depends = dependsOn.getText();
            NbDocument.markGuarded(actionDoc, elem.getStartOffset(),
                    target.getName().length() + depends.length() + 3);
        }
    }

    /** Validate the binary name and output directory */
    @Override
    public void validateData(ArrayList<String> msgs, int key) {
        AbstractDocument doc = (AbstractDocument) actionText.getDocument();
        String action;
        int extraLines = 0;
        int invalidLines = 0;

        try {
            action = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            // Shouldn't happen but this will suppress NPE if it does
            action = "";// NOI18N
            if (CndPathUtilities.IfdefDiagnostics) {
                System.out.println("BadLocationException validating target"); // NOI18N
                e.printStackTrace();
            }
        }

        StringTokenizer st = new StringTokenizer(action, "\n");		// NOI18N
        st.nextToken();			    // skip target line
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            if (line.length() == 0 || extraLines > 0) {
                extraLines++;
            } else if (!line.startsWith("\t")) {			// NOI18N
                invalidLines++;
            }
        }

        if (extraLines > 0) {
            warn(msgs, WARN_EXTRA_LINES_IN_TARGET, String.valueOf(extraLines));
        }
        if (invalidLines > 0) {
            warn(msgs, WARN_INVALID_LINES_IN_TARGET, String.valueOf(invalidLines));
        }
    }

    /** Create and initialize the target */
    @Override
    public void addNotify() {
        TargetData target = getMakefileData().getCurrentTarget();
        int i;

        if (!initialized) {
            create();
            initialized = true;
        }

        key = target.getKey();
        inAddNotify = true;
        depends = target.getDependsOn();
        if (depends == null) {
            depends = "";// NOI18N
        }
        dependsOn.setText(depends);		    // set the textfield
        Element elem = actionDoc.getParagraphElement(0);
        NbDocument.unmarkGuarded(actionDoc, elem.getStartOffset(),
                elem.getEndOffset() - elem.getStartOffset() + 1);
        try {
            actionDoc.remove(0, actionDoc.getLength());
        } catch (BadLocationException e) {
            if (CndPathUtilities.IfdefDiagnostics) {
                System.out.println("BadLocationException[1]: " + // NOI18N
                        e.getMessage() +
                        "\n\toffset = " + e.offsetRequested());		// NOI18N
                e.printStackTrace();
            }
        }

        // Compute the strings for the action are and display them
        StringBuilder depline = new StringBuilder(target.getName());
        if (target.getName().equals("clean")) {				// NOI18N
            // This allows the user to add duplicate clean targets
            depline.append(":: ");					// NOI18N
        } else {
            depline.append(": ");					// NOI18N
        }
        if (depends.length() > 0) {
            depline.append(depends);
        }
        depline.append("\n");						// NOI18N
        try {
            actionDoc.insertString(0, depline.toString(), null);
        } catch (BadLocationException e) {
            if (CndPathUtilities.IfdefDiagnostics) {
                System.out.println("BadLocationException[2]: " + // NOI18N
                        e.getMessage() +
                        "\n\toffset = " + e.offsetRequested());		// NOI18N
                e.printStackTrace();
            }
        }
        NbDocument.markGuarded(actionDoc, 0, depline.length());


        ArrayList list = target.getActions();
        StringBuilder action = new StringBuilder();
        for (i = 0; i < list.size(); i++) {
            String line = list.get(i).toString();
            // XXX - Will the "\t" be needed when we get an indentation engine?
            action.append("\t").append(line).append("\n");		// NOI18N
        }
        if (action.length() == 0) {
            // initial tab (XXX - until indentation engine?)
            action.append("\t");					// NOI18N
        }

        try {
            actionDoc.insertString(depline.length(), action.toString(), null);
        } catch (BadLocationException e) {
            if (CndPathUtilities.IfdefDiagnostics) {
                System.out.println("BadLocationException[3]: " + // NOI18N
                        e.getMessage() +
                        "\n\toffset = " + e.offsetRequested());		// NOI18N
                e.printStackTrace();
            }
        }

        super.addNotify();
        inAddNotify = false;
    }

    /** Get the data from the panel and update the target */
    @Override
    public void removeNotify() {
        super.removeNotify();

        TargetData target = getMakefileData().getTarget(key);
        String action = null;

        target.setDependsOn(depends);
        try {
            action = actionDoc.getText(0, actionDoc.getLength());
        } catch (BadLocationException e) {
            if (CndPathUtilities.IfdefDiagnostics) {
                System.out.println("BadLocationException[4]: " + // NOI18N
                        e.getMessage() +
                        "\n\toffset = " + e.offsetRequested());		// NOI18N
                e.printStackTrace();
            }
        }

        if (action != null) {
            ArrayList<String> list = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(action, "\n");	// NOI18N

            st.nextToken();			    // skip target line
            while (st.hasMoreTokens()) {
                String tmp = st.nextToken().trim();
                if (tmp.length() > 0) {
                    list.add(tmp);
                }
            }
            target.setActions(list);
        }
    }
}


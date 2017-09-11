/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package termtester;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.lib.terminalemulator.Coord;
import org.netbeans.lib.terminalemulator.Term;

/**
 *
 * @author ivan
 */
public class SequenceViewer extends javax.swing.JFrame {

    private final Term completed;
    private final Term unrecognized;

    private boolean filterNumbers = true;

    /**
     * Creates new form SequenceViewer
     */

    public SequenceViewer() {
        initComponents();

        filterNumbersCheckBox.setSelected(filterNumbers);

        completed = new Term();
        completed.setRowsColumns(30, 30);
        completed.setScrollOnOutput(false);
        completed.setHistorySize(1024);

        unrecognized = new Term();
        unrecognized.setRowsColumns(30, 30);
        unrecognized.setScrollOnOutput(false);
        unrecognized.setHistorySize(1024);

        mainPanel.add(completed);
        mainPanel.add(unrecognized);


        pack();
        refresh();
    }

    private void refresh() {
        completed.clearHistoryNoRefresh();
        unrecognized.clearHistoryNoRefresh();

        completed.appendText("Completed\n\r", true);
        unrecognized.appendText("Unrecognized\n\r", true);

        fill(completed, "completed");
        fill(unrecognized, "unrecognized");

        completed.possiblyNormalize(Coord.make(1, 1));
        unrecognized.possiblyNormalize(Coord.make(1, 1));
    }

    private String filterNumber(String line) {
        if (!filterNumbers)
            return line;

        // convert numbers to %d's
        Pattern pattern = Pattern.compile("\\[[0-9]+");
        Matcher matcher = pattern.matcher(line);
        line = matcher.replaceAll("\\[%d");

        pattern = Pattern.compile("\\[\\?[0-9]+");
        matcher = pattern.matcher(line);
        line = matcher.replaceAll("\\[\\?%d");

        pattern = Pattern.compile(";[0-9]+");
        matcher = pattern.matcher(line);
        line = matcher.replaceAll(";%d");

        return line;
    }

    private String filter(String line) {
        // convert escape charactrers to mnemonics
        StringBuilder sb = new StringBuilder();
        for (char c : line.toCharArray())
            sb.append(Util.charToMnemonic(c));

        return sb.toString();
    }

    private void fill(Term term, String title) {
        Set<String> set = new HashSet<String>();

        File file = new File(String.format("/tmp/term-sequences-%s", title));
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SequenceViewer.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        try {
            while (br.ready()) {
                String line = br.readLine();
                if (!line.isEmpty())
                    set.add(filterNumber(line));
            }
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(SequenceViewer.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<String> list = new LinkedList<String>(set);
        Collections.sort(list);

        for (String s : list) {
            term.appendText(filter(s), false);
            term.appendText("\n\r", true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        controlPanel = new javax.swing.JPanel();
        filterNumbersCheckBox = new javax.swing.JCheckBox();
        mainPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        controlPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        filterNumbersCheckBox.setText("Filter Numbers");
        filterNumbersCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterNumbersCheckBoxActionPerformed(evt);
            }
        });
        controlPanel.add(filterNumbersCheckBox);

        getContentPane().add(controlPanel, java.awt.BorderLayout.PAGE_START);

        mainPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void filterNumbersCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterNumbersCheckBoxActionPerformed
        filterNumbers = filterNumbersCheckBox.isSelected();
        refresh();

    }//GEN-LAST:event_filterNumbersCheckBoxActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SequenceViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SequenceViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SequenceViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SequenceViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new SequenceViewer().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel controlPanel;
    private javax.swing.JCheckBox filterNumbersCheckBox;
    private javax.swing.JPanel mainPanel;
    // End of variables declaration//GEN-END:variables
}

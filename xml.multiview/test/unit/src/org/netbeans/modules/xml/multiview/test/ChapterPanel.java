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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xml.multiview.test;

import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.test.bookmodel.Chapter;
import org.netbeans.modules.xml.multiview.Error;

/**
 *
 * @author  mkuchtiak
 */
public class ChapterPanel extends SectionInnerPanel {
    Chapter chapter;
    BookDataObject dObj;
    javax.swing.JTextArea[] paragraphTA;
    /** Creates new form ChapterPanel */
    public ChapterPanel(SectionView view, BookDataObject dObj,  Chapter chapter) {
        super(view);
        this.dObj=dObj;
        this.chapter=chapter;
        initComponents();
        titleTF.setText(chapter.getTitle());
        addValidatee(titleTF);
        summaryTA.setBorder(titleTF.getBorder());
        summaryTA.setText(chapter.getSummary());
        addModifier(summaryTA);
        String[] paragraphs = chapter.getParagraph();
        paragraphTA = new javax.swing.JTextArea[paragraphs.length];
        for (int i=0;i<paragraphs.length;i++) {
            paragraphTA[i] = new javax.swing.JTextArea();
            paragraphTA[i].setText(paragraphs[i]);
            addModifier(paragraphTA[i]);
            paragraphTA[i].setRows(10);
            paragraphsPanel.add(new javax.swing.JScrollPane(paragraphTA[i]),String.valueOf(i+1));
        }
        lengthTF.setText(chapter.getAttributeValue("length"));
        addValidatee(lengthTF);
    }

    public void setValue(javax.swing.JComponent source, Object value) {
        if (source==titleTF) {
            chapter.setTitle((String)value);
        } else if (source==lengthTF) {
            String text = (String)value;
            chapter.setAttributeValue("length",text.length()==0?null:text);
        } else if (source==summaryTA) {
            chapter.setSummary((String)value);
        } else {
            for (int i=0;i<paragraphTA.length;i++) {
                if (source==paragraphTA[i]) {
                    chapter.setParagraph(i, paragraphTA[i].getText());
                    break;
                }
            }
        }
    }
    
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==titleTF) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "title", comp));
                return;
            }
            try {
                Chapter[] chapters = dObj.getBook().getChapter();
                for (int i=0;i<chapters.length;i++) {
                    if (chapter!=chapters[i] && val.equals(chapters[i].getTitle())) {
                        getSectionView().getErrorPanel().setError(new Error(Error.TYPE_FATAL, Error.DUPLICATE_VALUE_MESSAGE, val, comp));
                        return;
                    }
                }
            } catch(java.io.IOException ex){}
            getSectionView().getErrorPanel().clearError();
        }
    }

    public void rollbackValue(javax.swing.text.JTextComponent source) {
        if (titleTF==source) {
            titleTF.setText(chapter.getTitle());
        }
    }
    
    protected void endUIChange() {
        dObj.modelUpdatedFromUI();
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }

    public javax.swing.JComponent getErrorComponent(String errorId) {
        if ("title".equals(errorId)) return titleTF;
        return null;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        titleLabel = new javax.swing.JLabel();
        titleTF = new javax.swing.JTextField();
        summaryLabel = new javax.swing.JLabel();
        summaryTA = new javax.swing.JTextArea();
        paragraphLabel = new javax.swing.JLabel();
        paragraphsPanel = new javax.swing.JTabbedPane();
        filler = new javax.swing.JPanel();
        lengthLabel = new javax.swing.JLabel();
        lengthTF = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        titleLabel.setText("Title:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(titleLabel, gridBagConstraints);

        titleTF.setColumns(40);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(titleTF, gridBagConstraints);

        summaryLabel.setText("Summary:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(summaryLabel, gridBagConstraints);

        summaryTA.setRows(3);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(summaryTA, gridBagConstraints);

        paragraphLabel.setText("Paragraphs:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        add(paragraphLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(paragraphsPanel, gridBagConstraints);

        filler.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(filler, gridBagConstraints);

        lengthLabel.setText("Length:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 5);
        add(lengthLabel, gridBagConstraints);

        lengthTF.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 5);
        add(lengthTF, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel filler;
    private javax.swing.JLabel lengthLabel;
    private javax.swing.JTextField lengthTF;
    private javax.swing.JLabel paragraphLabel;
    private javax.swing.JTabbedPane paragraphsPanel;
    private javax.swing.JLabel summaryLabel;
    private javax.swing.JTextArea summaryTA;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextField titleTF;
    // End of variables declaration//GEN-END:variables
    
}

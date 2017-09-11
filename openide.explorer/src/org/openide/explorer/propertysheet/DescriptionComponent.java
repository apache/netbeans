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
package org.openide.explorer.propertysheet;

import java.awt.BorderLayout;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import org.openide.util.ImageUtilities;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.accessibility.AccessibleRole;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JComponent.AccessibleJComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;
import org.openide.util.NbBundle;


/**
 * A component which can display a description, a title and a button.
 *
 * @author  Tim Boudreau
 */
class DescriptionComponent extends JComponent implements ActionListener, MouseListener, Accessible {
    private static int fontHeight = -1;
    private JEditorPane jep;
    private JLabel lbl;
    private JButton btn;
    private JToolBar toolbar;
    private JScrollPane jsc;

    /** Creates a new instance of SplitLowerComponent */
    public DescriptionComponent() {
        init();
    }

    private void init() {
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        jep = new JEditorPane();
        jep.setOpaque(false);
        jep.setBackground(getBackground());
        jep.setEditable(false);
        jep.setOpaque(false);
        jep.getAccessibleContext().setAccessibleName( NbBundle.getMessage(DescriptionComponent.class, "ACS_Description") );
        jep.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage(DescriptionComponent.class, "ACSD_Description") );
        jep.putClientProperty( JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE );

        HTMLEditorKit htmlKit = new HTMLEditorKit();
        if (htmlKit.getStyleSheet().getStyleSheets() == null) {
            javax.swing.text.html.StyleSheet css = new javax.swing.text.html.StyleSheet();
            java.awt.Font f = new JLabel().getFont();
            css.addRule(new StringBuffer("body { font-size: ").append(f.getSize()) // NOI18N
                        .append("; font-family: ").append(f.getName()).append("; }").toString()); // NOI18N
            css.addStyleSheet(htmlKit.getStyleSheet());
            htmlKit.setStyleSheet(css);
        } else {
            jep.setFont( new JLabel().getFont() );
        }
        jep.setEditorKit( htmlKit );



        //We use a JScrollPane to suppress the changes in layout that will be
        //caused by adding the raw JTextArea directly - JTextAreas can fire
        //preferred size changes from within their paint methods, leading to
        //cyclic revalidation problems
        jsc = new JScrollPane(jep);
        jsc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jsc.setBorder(BorderFactory.createEmptyBorder());
        jsc.setViewportBorder(jsc.getBorder());
        jsc.setOpaque(false);
        jsc.setBackground(getBackground());
        jsc.getViewport().setOpaque(false);

        if (!PropUtils.psNoHelpButton) {
            if( PropUtils.isAqua ) {
                btn = new JButton();
                btn.addActionListener(this);
                btn.putClientProperty("JButton.buttonType", "help");
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
            } else {
                Image help = ImageUtilities.loadImage("org/openide/resources/propertysheet/propertySheetHelp.png", true); //NOI18N

                btn = new JButton(new ImageIcon(help));
                btn.addActionListener(this);

                toolbar = new JToolBar ();
                toolbar.setRollover (true);
                toolbar.setFloatable (false);
                toolbar.setLayout (new BorderLayout (0, 0));
                toolbar.setBorder (BorderFactory.createEmptyBorder());
                toolbar.setBorderPainted(false);
                toolbar.add (btn);
            }
            btn.setFocusable(false);
        }

        lbl = new JLabel("Label"); //NOI18N

        lbl.setFont(new Font(null, Font.BOLD, lbl.getFont().getSize()));

        add(jsc);
        add(lbl);
        if (!PropUtils.psNoHelpButton) {
            add( PropUtils.isAqua ? btn : toolbar);
        }
        jep.addMouseListener(this);
        jsc.addMouseListener(this);
        lbl.addMouseListener(this);
        if (!PropUtils.psNoHelpButton) {
            btn.addMouseListener(this);
        }
        jsc.getViewport().addMouseListener(this);
    }

    @Override
    public void doLayout() {
        Insets ins = getInsets();
        Dimension lbll = lbl.getPreferredSize();
        int height = lbll.height;
        int right = getWidth() - ins.right;
        if (!PropUtils.psNoHelpButton) {
            Dimension bttn = PropUtils.isAqua ? btn.getPreferredSize() : toolbar.getPreferredSize();
            height = Math.max(bttn.height, lbll.height);
            right = getWidth() - (ins.right + bttn.width);
            if( PropUtils.isAqua )
                btn.setBounds(right, ins.top, bttn.width, height);
            else
                toolbar.setBounds(right, ins.top, bttn.width, height);
        }
        lbl.setBounds(ins.left, ins.top, right, height);
        jsc.setBounds(ins.left, height, getWidth() - (ins.left + ins.right), getHeight() - height);
    }

    public void setDescription(String title, String txt) {
        if (title == null) {
            title = "";
        }

        if (txt == null) {
            txt = "";
        }

        lbl.setText(title);

        if (title.equals(txt)) {
            jep.setText("");
        } else {
            jep.setText(txt);
        }
    }

    public void setHelpEnabled(boolean val) {
        if (!PropUtils.psNoHelpButton) {
            btn.setEnabled(val);
        }
    }

    /**
     * Overridden to calculate a font height on the first paint
     */
    @Override
    public void paint(Graphics g) {
        if (fontHeight == -1) {
            fontHeight = g.getFontMetrics(lbl.getFont()).getHeight();
        }

        super.paint(g);
    }

    /** Overridden to ensure the description area doesn't grow too big
     * with large amounts of text */
    @Override
    public Dimension getPreferredSize() {
        Dimension d = new Dimension(super.getPreferredSize());

        if (fontHeight > 0) {
            Insets ins = getInsets();
            d.height = Math.max(50, Math.max(d.height, (4 * fontHeight) + ins.top + ins.bottom + 12));
        } else {
            d.height = Math.min(d.height, 64);
        }

        return d;
    }

    @Override
    public Dimension getMinimumSize() {
        if (fontHeight < 0) {
            return super.getMinimumSize();
        }

        Dimension d = new Dimension(4 * fontHeight, 4 * fontHeight);

        return d;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        PSheet sheet = (PSheet) SwingUtilities.getAncestorOfClass(PSheet.class, this);

        if (sheet != null) {
            sheet.helpRequested();
        }
    }

    private PSheet findSheet() {
        return (PSheet) SwingUtilities.getAncestorOfClass(PSheet.class, this);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    /**
     * Forward events that might invoke the popup menu
     */
    public void mousePressed(MouseEvent e) {
        PSheet sh = findSheet();

        if (sh != null) {
            sh.mousePressed(e);
        }
    }

    /**
     * Forward events that might invoke the popup menu
     */
    public void mouseReleased(MouseEvent e) {
        PSheet sh = findSheet();

        if (sh != null) {
            sh.mousePressed(e);
        }
    }

    @Override
    public AccessibleContext getAccessibleContext() {

        if( null == accessibleContext ) {
            accessibleContext = new AccessibleJComponent() {
                @Override
                public AccessibleRole getAccessibleRole() {
                    return AccessibleRole.SWING_COMPONENT;
                }
            };
        
            accessibleContext.setAccessibleName( NbBundle.getMessage(DescriptionComponent.class, "ACS_Description") );
            accessibleContext.setAccessibleDescription( NbBundle.getMessage(DescriptionComponent.class, "ACSD_Description") );
        }
        
        return accessibleContext;
    }
}

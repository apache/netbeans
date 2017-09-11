/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.versioning.util.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import org.openide.awt.Mnemonics;

/**
 *
 * inspired by org.netbeans.modules.palette.ui.CategoryButton
 * 
 * @author Tomas Stupka
 */
public final class SectionButton extends JCheckBox {

    final boolean isGTK = "GTK".equals( UIManager.getLookAndFeel().getID() );
    final boolean isNimbus = "Nimbus".equals( UIManager.getLookAndFeel().getID() );
    final boolean isAqua = "Aqua".equals( UIManager.getLookAndFeel().getID() );
    private ActionListener al;

    public SectionButton() {        
        this(null);
    }
    
    public SectionButton(ActionListener al) {     

        // force initialization of PropSheet look'n'feel values 
        UIManager.get( "nb.propertysheet" );

        setFont( getFont().deriveFont( Font.BOLD ) );
        setMargin(new Insets(0, 3, 0, 3));
        setFocusPainted( false );

        setHorizontalAlignment( SwingConstants.LEFT );
        setHorizontalTextPosition( SwingConstants.RIGHT );
        setVerticalTextPosition( SwingConstants.CENTER );

        updateProperties();

        if( getBorder() instanceof CompoundBorder ) { // from BasicLookAndFeel
            Dimension pref = getPreferredSize();
            pref.height -= 3;
            setPreferredSize( pref );
        }

        if(al != null) {
            addActionListener(al);
        }
        
        initActions();
    }


    @Override
    public void addActionListener(ActionListener al) {
        this.al = al;
        super.addActionListener(al);
    }
        
    @Override
    public String getUIClassID() {
        return super.getUIClassID();            
    }
    
    private void initActions() {
        InputMap inputMap = getInputMap( WHEN_FOCUSED );
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, 0, false ), "collapse" ); //NOI18N
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, 0, false ), "expand" ); //NOI18N

        ActionMap actionMap = getActionMap();
        actionMap.put( "collapse", new ExpandAction( false ) ); //NOI18N
        actionMap.put( "expand", new ExpandAction( true ) ); //NOI18N
    }

    private void updateProperties() {
        setIcon( UIManager.getIcon(isGTK ? "Tree.gtk_collapsedIcon" : "Tree.collapsedIcon") );
        setSelectedIcon( UIManager.getIcon(isGTK ? "Tree.gtk_expandedIcon" : "Tree.expandedIcon") );
        Mnemonics.setLocalizedText( this, getText() );
        getAccessibleContext().setAccessibleName( getText() );
        getAccessibleContext().setAccessibleDescription( getText() );
        if( isAqua ) {
            setContentAreaFilled(true);
            setOpaque(true);
            setBackground( new Color(0,0,0) );
            setForeground( new Color(255,255,255) );
        }
        if( isNimbus ) {
            setOpaque(true);
            setContentAreaFilled(true);
        }
    }

    private boolean isExpanded() {
        return isSelected();
    }

    private void setExpanded( boolean expand ) {
        setSelected(expand);
        requestFocus ();
    }

    @Override
    public Color getBackground() {
        if( isFocusOwner() ) {
            if( isGTK || isNimbus )
                return UIManager.getColor("Tree.selectionBackground"); //NOI18N
            return UIManager.getColor( "PropSheet.selectedSetBackground" ); //NOI18N
        } else {
            if( isAqua ) {
                Color defBk = UIManager.getColor("NbExplorerView.background");
                if( null == defBk )
                    defBk = Color.gray;
                return new Color( defBk.getRed()-10, defBk.getGreen()-10, defBk.getBlue()-10);
            }
            if( isGTK || isNimbus ) {
                if( getModel().isRollover() )
                    return new Color( UIManager.getColor( "Menu.background" ).getRGB() ).darker(); //NOI18N
                return new Color( UIManager.getColor( "Menu.background" ).getRGB() );//NOI18N
            }
            return UIManager.getColor( "PropSheet.setBackground" ); //NOI18N
        }
    }

    @Override
    public Color getForeground() {
        if( isFocusOwner() ) {
            if( isAqua )
                return UIManager.getColor( "Table.foreground" ); //NOI18N
            else if( isGTK || isNimbus )
                return UIManager.getColor( "Tree.selectionForeground" ); //NOI18N
            return UIManager.getColor( "PropSheet.selectedSetForeground" ); //NOI18N
        } else {
            if( isAqua ) {
                Color res = UIManager.getColor("PropSheet.setForeground"); //NOI18N

                if (res == null) {
                    res = UIManager.getColor("Table.foreground"); //NOI18N

                    if (res == null) {
                        res = UIManager.getColor("textText");

                        if (res == null) {
                            res = Color.BLACK;
                        }
                    }
                }
                return res;
            }
            if( isGTK || isNimbus ) {
                return new Color( UIManager.getColor( "Menu.foreground" ).getRGB() ); //NOI18N
            }
            return super.getForeground();
        }
    }

    private class ExpandAction extends AbstractAction {
        private boolean expand;
        public ExpandAction( boolean expand ) {
            this.expand = expand;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if( expand == isExpanded() ) {
                return;                    
            }
            setExpanded( expand );
            al.actionPerformed(e);
        }
    }
}   

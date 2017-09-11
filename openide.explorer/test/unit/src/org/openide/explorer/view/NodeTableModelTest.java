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

package org.openide.explorer.view;

import java.lang.reflect.InvocationTargetException;
import javax.swing.JCheckBox;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/*
 * Tests for class NodeTableModelTest
 */
public class NodeTableModelTest extends NbTestCase {

    public NodeTableModelTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testMakeAccessibleCheckBox() {
        MyNodeTableModel model = new MyNodeTableModel( 0 );

        MyProperty p;
        JCheckBox checkBox;

        p = new MyProperty();
        p.setDisplayName( "displayName1" );
        p.setShortDescription( "shortDescription1" );
        p.setValue( "ColumnMnemonicCharTTV", "" );
        
        checkBox = new JCheckBox( "displayName" );
        model.makeAccessibleCheckBox( checkBox, p );
        assertEquals( "Invalid accessible name", checkBox.getAccessibleContext().getAccessibleName(), p.getDisplayName() );
        assertEquals( "Invalid accessible description", checkBox.getAccessibleContext().getAccessibleDescription(), p.getShortDescription() );
        assertEquals( "Invalid mnemonic", checkBox.getMnemonic(), 0 );

        
        p = new MyProperty();
        p.setDisplayName( "displayName" );
        p.setShortDescription( "shortDescription2" );
        p.setValue( "ColumnMnemonicCharTTV", "d" );
        
        checkBox = new JCheckBox( "displayName2" );
        model.makeAccessibleCheckBox( checkBox, p );
        assertEquals( "Invalid accessible name", checkBox.getAccessibleContext().getAccessibleName(), p.getDisplayName() );
        assertEquals( "Invalid accessible description", checkBox.getAccessibleContext().getAccessibleDescription(), p.getShortDescription() );
        assertEquals( "Invalid mnemonic", checkBox.getMnemonic(), 'D' );

        
        p = new MyProperty();
        p.setDisplayName( "displayName3" );
        p.setShortDescription( "shortDescription3" );
        p.setValue( "ColumnMnemonicCharTTV", "N" );
        
        checkBox = new JCheckBox( "displayName" );
        model.makeAccessibleCheckBox( checkBox, p );
        assertEquals( "Invalid accessible name", checkBox.getAccessibleContext().getAccessibleName(), p.getDisplayName() );
        assertEquals( "Invalid accessible description", checkBox.getAccessibleContext().getAccessibleDescription(), p.getShortDescription() );
        assertEquals( "Invalid mnemonic", checkBox.getMnemonic(), 'N' );

        
        p = new NullGetValueProperty();
        p.setDisplayName( "displayName4" );
        p.setShortDescription( "shortDescription4" );
        
        checkBox = new JCheckBox( "displayName" );
        model.makeAccessibleCheckBox( checkBox, p );
        assertEquals( "Invalid accessible name", checkBox.getAccessibleContext().getAccessibleName(), p.getDisplayName() );
        assertEquals( "Invalid accessible description", checkBox.getAccessibleContext().getAccessibleDescription(), p.getShortDescription() );
        assertEquals( "Invalid mnemonic", checkBox.getMnemonic(), 0 );
    }
    

    public void testGetDisplayNameWithMnemonic() {
        MyNodeTableModel model = new MyNodeTableModel( 0 );

        MyProperty p;
        JCheckBox checkBox;

        p = new MyProperty();
        p.setDisplayName( "displayName1" );
        p.setShortDescription( "shortDescription1" );
        p.setValue( "ColumnMnemonicCharTTV", "" );

        assertEquals( "Invalid display name:", model.getDisplayNameWithMnemonic(p), p.getDisplayName() );
        
        
        p = new MyProperty();
        p.setDisplayName( "displayName1" );
        p.setShortDescription( "shortDescription1" );
        p.setValue( "ColumnDisplayNameWithMnemonicTTV", "otherDisplayName" );
        p.setValue( "ColumnMnemonicCharTTV", "" );
        checkBox = new JCheckBox( model.getDisplayNameWithMnemonic(p) );
        Mnemonics.setLocalizedText(checkBox, checkBox.getText());
        model.makeAccessibleCheckBox( checkBox, p );

        assertEquals( "Invalid display name:", 
                p.getValue("ColumnDisplayNameWithMnemonicTTV"),
                model.getDisplayNameWithMnemonic(p) );
        assertEquals( "Invalid mnemonic", 0, checkBox.getMnemonic() );
        
        
        p = new MyProperty();
        p.setDisplayName( "displayName1" );
        p.setShortDescription( "shortDescription1" );
        p.setValue( "ColumnDisplayNameWithMnemonicTTV", "otherDisplayName" );
        p.setValue( "ColumnMnemonicCharTTV", "t" );
        checkBox = new JCheckBox( model.getDisplayNameWithMnemonic(p) );
        Mnemonics.setLocalizedText(checkBox, checkBox.getText());
        model.makeAccessibleCheckBox( checkBox, p );

        assertEquals( "Invalid display name:", 
                p.getValue("ColumnDisplayNameWithMnemonicTTV"),
                model.getDisplayNameWithMnemonic(p) );
        assertEquals( "Invalid mnemonic", 'T', checkBox.getMnemonic() );
        
        
        p = new MyProperty();
        p.setDisplayName( "displayName1" );
        p.setShortDescription( "shortDescription1" );
        p.setValue( "ColumnDisplayNameWithMnemonicTTV", "other&DisplayName" );
        p.setValue( "ColumnMnemonicCharTTV", "" );
        checkBox = new JCheckBox( model.getDisplayNameWithMnemonic(p) );
        Mnemonics.setLocalizedText(checkBox, checkBox.getText());
        model.makeAccessibleCheckBox( checkBox, p );

        assertEquals( "Invalid display name:", 
                p.getValue("ColumnDisplayNameWithMnemonicTTV"),
                model.getDisplayNameWithMnemonic(p) );
        if (Utilities.isMac()) {
            assertEquals( "No mnemonic on mac", 0, checkBox.getMnemonic() );
        } else {
            assertEquals( "Invalid mnemonic", 'D', checkBox.getMnemonic() );
        }
    }

    private static class MyNodeTableModel extends NodeTableModel {
        public MyNodeTableModel( int columnCount ) {
            this.allPropertyColumns = new NodeTableModel.ArrayColumn[columnCount];
            for( int i=0; i<allPropertyColumns.length; i++ ) {
                allPropertyColumns[i] = new NodeTableModel.ArrayColumn();
                allPropertyColumns[i].setProperty( new MyProperty() );
            }
        }
        
        Node.Property getProperty( int index ) {
            return allPropertyColumns[index].getProperty();
        }
        
        void setProperty( int index, Node.Property p ) {
            allPropertyColumns[index].setProperty( p );
        }
    }
    
    private static class MyProperty extends Node.Property {
        public MyProperty() {
            super( Object.class );
        }
        
        @Override
        public void setValue(Object val) 
            throws IllegalAccessException, 
                IllegalArgumentException, 
                InvocationTargetException {
        }

        @Override
        public Object getValue() 
            throws IllegalAccessException, 
                InvocationTargetException {
            return null;
        }

        @Override
        public boolean canWrite() {
            return true;
        }

        @Override
        public boolean canRead() {
            return true;
        }
    }
    
    private static class NullGetValueProperty extends MyProperty {
        @Override
        public Object getValue(String attributeName) {
            return null;
        }
    }
}

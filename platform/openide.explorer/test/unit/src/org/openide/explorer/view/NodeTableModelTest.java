/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

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

package org.openide.awt;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import org.netbeans.junit.NbTestCase;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Test use of mnemonics.
 * @author Jesse Glick
 */
public class MnemonicsTest extends NbTestCase {

    public MnemonicsTest(String name) {
        super(name);
    }

    // XXX testFindMnemonicAmpersand

    /** @see #31093 */
    public void testMnemonicAfterParens() throws Exception {
        JButton b = new JButton();
        Mnemonics.setLocalizedText(b, "Execute (&Force Reload)");
        assertEquals("Execute (Force Reload)", b.getText());
        if (Mnemonics.isAquaLF()) {
            assertEquals(0, b.getMnemonic());
            assertEquals(-1, b.getDisplayedMnemonicIndex());
        } else {
            assertEquals(KeyEvent.VK_F, b.getMnemonic());
            assertEquals(9, b.getDisplayedMnemonicIndex());
        }
        assertEquals("Execute (Force Reload)", Actions.cutAmpersand("Execute (&Force Reload)"));
        // XXX test that actual Japanese mnemonics work as expected...
    }
    
    public void testMnemonicHTML() throws Exception {
        JButton b = new JButton();
        Mnemonics.setLocalizedText(b, "<html><b>R&amp;D</b> department");
        assertEquals("<html><b>R&amp;D</b> department", b.getText());
        assertEquals(0, b.getMnemonic());
        assertEquals(-1, b.getDisplayedMnemonicIndex());
        String underStart = Mnemonics.isAquaLF() ? "" : "<u>";
        String underEnd = Mnemonics.isAquaLF() ? "" : "</u>";
        Mnemonics.setLocalizedText(b, "<html><b>R&amp;D</b> departmen&t");
        assertEquals("<html><b>R&amp;D</b> departmen" + underStart + "t" + underEnd, b.getText());
        if (Mnemonics.isAquaLF()) {
            assertEquals(0, b.getMnemonic());
            assertEquals(-1, b.getDisplayedMnemonicIndex());
        } else {
            assertEquals(KeyEvent.VK_T, b.getMnemonic());
        }
        
        Mnemonics.setLocalizedText(b, "<html>Smith &amp; &Wesson");
        assertEquals("<html>Smith &amp; " + underStart + "W" + underEnd + "esson", b.getText());
        if (Mnemonics.isAquaLF()) {
            assertEquals(0, b.getMnemonic());
            assertEquals(-1, b.getDisplayedMnemonicIndex());
        } else {
            assertEquals(KeyEvent.VK_W, b.getMnemonic());
        }
        Mnemonics.setLocalizedText(b, "<html>&Advanced Mode <em>(experimental)</em></html>");
        assertEquals("<html>" + underStart + "A" + underEnd + "dvanced Mode <em>(experimental)</em></html>", b.getText());
        if (Mnemonics.isAquaLF()) {
            assertEquals(0, b.getMnemonic());
            assertEquals(-1, b.getDisplayedMnemonicIndex());
        } else {
            assertEquals(KeyEvent.VK_A, b.getMnemonic());
            assertEquals('A', b.getText().charAt(b.getDisplayedMnemonicIndex()));
        }
    }
    
    public void testSetLocalizedTextWithModel() throws Exception {
        ButtonModel m = new DefaultButtonModel();
        JButton b = new JButton();
        Mnemonics.setLocalizedText(b, "Hello &There");
        assertEquals("Hello There", b.getText());
        if( Mnemonics.isAquaLF() ) {
            assertEquals(0, b.getMnemonic());
            assertEquals(-1, b.getDisplayedMnemonicIndex());
        } else {
            assertEquals('T', b.getMnemonic());
            assertEquals(6, b.getDisplayedMnemonicIndex());
        }
        b.setModel(m);
        assertEquals("Hello There", b.getText());
        if( Mnemonics.isAquaLF() ) {
            assertEquals(0, b.getMnemonic());
            assertEquals(-1, b.getDisplayedMnemonicIndex());
        } else {
            assertEquals('T', b.getMnemonic());
            assertEquals(6, b.getDisplayedMnemonicIndex());
        }
    }

    public void testBug174191_1() {
        final JButton button = new JButton();
        Mnemonics.setLocalizedText(button, "Aaaaaaaaaaaaaaaaaaaarrrrgggghh&h!");
        Mnemonics.setLocalizedText(button, "Help!");
    }

    public void testBug174191_2() {
        final JButton button = new JButton();
        Mnemonics.setLocalizedText(button, "Aaaaaaaaaaaaaaaaaaaarrrrgggghh&h!");
        Mnemonics.setLocalizedText(button, "&Roaarr!");
    }

    public void testAlwaysMnemonics() {
        Locale.setDefault(new Locale("te", "ST"));
        MnemAction mnem = new MnemAction();
        JMenuItem item = new JMenuItem();
        Actions.connect(item, mnem, true);
        assertEquals("Plain text", "Mnem", item.getText());
        if( Mnemonics.isAquaLF() ) {
            assertEquals("No mnenonic on Mac", 0, item.getMnemonic());
        } else {
            assertEquals("Mnenonic is on n", 'N', item.getMnemonic());
        }
    }
    public void testNeverMnemonics() {
        Locale.setDefault(new Locale("te", "NO"));
        MnemAction mnem = new MnemAction();
        JMenuItem item = new JMenuItem();
        Actions.connect(item, mnem, true);
        assertEquals("Plain text", "Mnem", item.getText());
        assertEquals("No mnenonic", 0, item.getMnemonic());
    }
    
    private static class MnemAction extends AbstractAction {

        public MnemAction() {
            putValue(NAME, "M&nem");
        }
            
        @Override
        public void actionPerformed(ActionEvent e) {
        }
        
    }
}

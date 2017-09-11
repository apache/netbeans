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

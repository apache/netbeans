/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uihandler;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.JButton;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Martin Entlicher
 */
public class ButtonsHTMLParserTest extends NbTestCase {
    
    public ButtonsHTMLParserTest(String name) {
        super(name);
    }

    @Test
    public void testParse() throws Exception {
        parsingTest("parsing_with_suffix.html");
    }
    
    @Test
    public void testParseErrorFile() throws Exception {
        parsingTest("parsing_with_errors.html");
    }
    
    private void parsingTest(String file) throws Exception {
        InputStream is = BugTriggersTest.class.getResourceAsStream(file);
        ButtonsHTMLParser bp = new ButtonsHTMLParser(is);
        bp.logger.setLevel(Level.ALL);
        bp.logger.addHandler(new LogHandler());
        bp.parse();
        bp.createButtons();
        assertEquals("Title", "Report Problem", bp.getTitle());
        assertEquals("containsExitButton", true, bp.containsExitButton());
        List<Object> options = bp.getOptions();
        List<Object> additionalOptions = bp.getAdditionalOptions();
        assertEquals("Number of options", 2, options.size());
        assertEquals("Number of additional options", 1, additionalOptions.size());
        JButton b1 = (JButton) options.get(0);
        JButton b2 = (JButton) options.get(1);
        JButton b3 = (JButton) additionalOptions.get(0);
        assertEquals("1st button command name", "submit", b1.getActionCommand());
        assertEquals("1st button text", "Send", b1.getText());
        assertTrue("1st button is default capable", b1.isDefaultCapable());
        assertTrue("1st button is enabled", b1.isEnabled());
        assertEquals("1st button alt", "reportDialog", b1.getClientProperty("alt"));
        assertEquals("1st button now", "&Send", b1.getClientProperty("now"));
        assertEquals("1st button URL", "http://statistics.netbeans.org/analytics/upload.jsp", b1.getClientProperty("url"));
        
        assertEquals("2nd button command name", "exit", b2.getActionCommand());
        assertEquals("2nd button text", "Cancel", b2.getText());
        assertFalse("2nd button is default capable", b2.isDefaultCapable());
        assertTrue("2nd button is enabled", b2.isEnabled());
        assertEquals("2nd button alt", null, b2.getClientProperty("alt"));
        assertEquals("2nd button now", "Cancel", b2.getClientProperty("now"));
        
        assertEquals("3nd button command name", "view-data", b3.getActionCommand());
        assertEquals("3nd button text", "View Data", b3.getText());
        assertFalse("3nd button is default capable", b3.isDefaultCapable());
        assertTrue("3nd button is enabled", b3.isEnabled());
        assertEquals("3nd button alt", "&Hide data", b3.getClientProperty("alt"));
        assertEquals("3nd button now", "&View Data", b3.getClientProperty("now"));
    }
    
    private class LogHandler extends Handler {

        @Override
        public void publish(LogRecord record) {
            System.out.println(MessageFormat.format(record.getMessage(), record.getParameters()));
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
        
    }
}

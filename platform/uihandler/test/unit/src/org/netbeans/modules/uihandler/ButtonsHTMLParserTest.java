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

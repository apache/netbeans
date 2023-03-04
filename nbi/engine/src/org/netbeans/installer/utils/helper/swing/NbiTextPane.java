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

package org.netbeans.installer.utils.helper.swing;

import java.awt.Color;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.helper.Text.ContentType;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiTextPane extends JTextPane {
    public NbiTextPane() {
        super();
        
        setOpaque(false);
        setEditable(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setFocusable(false);
        putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, true);
        
        if (UIManager.getLookAndFeel().getID().equals("Nimbus")) {
            //#134837
            //http://forums.java.net/jive/thread.jspa?messageID=283882
            setBackground(new Color(0, 0, 0, 0));
        }
    }
    
    @Override
    public void setText(String text) {
        if ((text == null) || text.equals("")) {
            if (getContentType().equals("text/plain")) {
                super.setText(" ");
            } else if (getContentType().equals("text/html")) {
                super.setText("&nbsp;");
            }
        } else {
            super.setText(text);
        }
    }
    
    public void setText(Text text) {
        setContentType(text.getContentType());
        setText(text.getText());
    }
    
    public void setText(CharSequence chars) {
        setText(chars.toString());
    }
    
    public void clearText() {
        setText("");
    }
    
    public void setContentType(ContentType contentType) {
        super.setContentType(contentType.toString());
    }
}

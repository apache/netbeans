/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.editor.palette.items;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.java.editor.palette.items.resources.ItemCustomizer;
import org.openide.text.ActiveEditorDrop;

/**
 *
 * @author geertjan
 */
public class Item implements ActiveEditorDrop {
    
    private String comment = "";
    
    public Item() {
    }
    
    private String createBody() {
        String comment = getComment();
        StringBuilder buffer = new StringBuilder();
        buffer.append( "/** " );
        buffer.append( comment );
        buffer.append( " */\n" );
        buffer.append( "public static void main( String[] args ) {\n" );
        buffer.append( "}\n\n" );
        return buffer.toString();
    }
    
    @Override
    public boolean handleTransfer(JTextComponent targetComponent) {
        
        ItemCustomizer c = new ItemCustomizer(this, targetComponent);
        boolean accept = c.showDialog();
        if (accept) {
            String body = createBody();
            try {
                JavaSourceFilePaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }
        return accept;
        
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
}


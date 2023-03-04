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
package org.netbeans.modules.websvc.editor.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;

/**
 *
 * @author mkuchtiak
 */
public abstract class WSCompletionItem  implements CompletionItem {
    
    private static final String COLOR_END = "</font>"; //NOI18N
    private static final String STRIKE = "<s>"; //NOI18N
    private static final String STRIKE_END = "</s>"; //NOI18N
    
    int substitutionOffset;
    
    public static final WSCompletionItem createWsdlFileItem(FileObject wsdlFolder, FileObject wsdlFile, int substitutionOffset) {
        String wsdlPath = FileUtil.getRelativePath(wsdlFolder.getParent().getParent(), wsdlFile);
        // Temporary fix for wsdl files in EJB project
        if (wsdlPath.startsWith("conf/")) wsdlPath = "META-INF/"+wsdlPath.substring(5); //NOI18N
        String displayPath = FileUtil.getRelativePath(wsdlFolder, wsdlFile);
        return new WsdlFileItem(wsdlPath, displayPath, substitutionOffset);
    }
    
    public static final WSCompletionItem createEnumItem(String itemName, String itemType, int substitutionOffset) {
        return new EnumItem(itemName, itemType, substitutionOffset);
    }
    
    public WSCompletionItem(int substitutionOffset) {
        this.substitutionOffset=substitutionOffset;
    }

    public void defaultAction(JTextComponent component) {
        if (component != null) {
            Completion.get().hideDocumentation();
            Completion.get().hideCompletion();
            int caretOffset = component.getSelectionEnd();
            substituteText(component, substitutionOffset, caretOffset - substitutionOffset, null);
        }
    }

    public void processKeyEvent(KeyEvent evt) {
    }

    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
    }

    public CompletionTask createDocumentationTask() {
        return null;
    }

    public CompletionTask createToolTipTask() {
        return null;
    }

    public boolean instantSubstitution(JTextComponent component) {
        return false;
    }

    public int getSortPriority() {
        return 100;
    }

    public abstract CharSequence getSortText();

    public abstract CharSequence getInsertPrefix();
    
    protected abstract String getLeftHtmlText();
    
    protected String getRightHtmlText() {
        return null;
    }
    
    protected ImageIcon getIcon() {
        return null;
    }   
    
    void substituteText(JTextComponent c, int offset, int len, String toAdd) {
        BaseDocument doc = (BaseDocument)c.getDocument();
        String text = getInsertPrefix().toString();
        if (text != null) {
            // Update the text
            doc.atomicLock();
            try {
                String textToReplace = doc.getText(offset, len);
                if (text.equals(textToReplace)) {
                    return;
                }                
                Position position = doc.createPosition(offset);
                doc.remove(offset, len);
                doc.insertString(position.getOffset(), text, null);
            } catch (BadLocationException e) {
                // Can't update
            } finally {
                doc.atomicUnlock();
            }
        }
    }
    
    private static class WsdlFileItem extends WSCompletionItem {
        private static final String FILE_ICON = "org/netbeans/modules/websvc/editor/completion/resources/fileProtocol.gif"; // NOI18N
        private static final String COLOR = "<font color=#005600>"; //NOI18N
        private String leftText;
        String wsdlPath, displayPath;
        private static ImageIcon icon;
        
        private WsdlFileItem(String wsdlPath, String displayPath, int substitutionOffset) {
            super(substitutionOffset);
            this.wsdlPath = wsdlPath;
            this.displayPath = displayPath;
        }
        protected ImageIcon getIcon(){
            if (icon == null) icon = ImageUtilities.loadImageIcon(FILE_ICON, false);
            return icon;      
        }
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(COLOR);
                sb.append(displayPath);
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }
        
        public CharSequence getSortText() {
            return displayPath;
        }
        
        public CharSequence getInsertPrefix() {
            return wsdlPath;
        }
    }
    
    private static class EnumItem extends WSCompletionItem {
        private static final String ENUM_ICON = "org/netbeans/modules/websvc/editor/completion/resources/field_static_16.png"; // NOI18N
        private static final String COLOR = "<font color=#0000b2>"; //NOI18N
        private String leftText;
        private String itemName, itemType;
        private ImageIcon icon;
        
        private EnumItem(String itemName, String itemType, int substitutionOffset) {
            super(substitutionOffset);
            this.itemName=itemName;
            this.itemType=itemType;
        }
        protected ImageIcon getIcon(){
            if (icon == null) icon = ImageUtilities.loadImageIcon(ENUM_ICON, false);
            return icon;
        }
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(COLOR);
                sb.append(itemName);
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }
        
        protected String getRightHtmlText() {
            return itemType;
        }       
        public CharSequence getSortText() {
            return itemName;
        }
        
        public CharSequence getInsertPrefix() {
            return itemName;
        }
        public int getSortPriority() {
            return 10;
        }
    }


}

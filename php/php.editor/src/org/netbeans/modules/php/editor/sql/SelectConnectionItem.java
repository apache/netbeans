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

package org.netbeans.modules.php.editor.sql;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class SelectConnectionItem implements CompletionItem {

    private static final ImageIcon CONNECTION_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/php/editor/resources/connection.gif")); // NOI18N

    private Document doc;
    private DatabaseConnection dbconn;

    public SelectConnectionItem(Document doc) {
        this.doc = doc;
        dbconn = DatabaseConnectionSupport.getDatabaseConnection(doc, true);
    }

    @Override
    public void defaultAction(JTextComponent component) {
        DatabaseConnection newDBConn = DatabaseConnectionSupport.selectDatabaseConnection(Utilities.getDocument(component));
        if (newDBConn != null && newDBConn.getJDBCConnection() != null) {
            Completion.get().showCompletion();
        }
    }

    @Override
    public void processKeyEvent(KeyEvent evt) {
    }

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), null, g, defaultFont);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getImageIcon(), getLeftHtmlText(), null, g, defaultFont, defaultColor, width, height, selected);
    }

    @Override
    public CompletionTask createDocumentationTask() {
        return null;
    }

    @Override
    public CompletionTask createToolTipTask() {
        return null;
    }

    @Override
    public boolean instantSubstitution(JTextComponent component) {
        return false;
    }

    @Override
    public int getSortPriority() {
        return 0;
    }

    @Override
    public CharSequence getSortText() {
        return "";
    }

    @Override
    public CharSequence getInsertPrefix() {
        return "";
    }

    private ImageIcon getImageIcon() {
        return CONNECTION_ICON;
    }

    private String getLeftHtmlText() {
        return NbBundle.getMessage(SelectConnectionItem.class, "SelectDatabaseConnection");
    }
}

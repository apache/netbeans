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
package org.netbeans.modules.spellchecker.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.Locale;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.spellchecker.ComponentPeer;
import org.netbeans.modules.spellchecker.DictionaryImpl;
import org.netbeans.modules.spellchecker.api.LocaleQuery;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Jancura
 */
public class AddToDictionaryCompletionItem implements CompletionItem {

    private String              word;
    private boolean             projects;

    
    /** Creates a new instance of WordCompletionItem */
    public AddToDictionaryCompletionItem (
        String                  word,
        boolean                 projects
    ) {
        this.word = word;
        this.projects = projects;
    }
    
    public void defaultAction (
        final JTextComponent    component
    ) {
        Completion.get ().hideCompletion ();
        Completion.get ().hideDocumentation ();
        Document document = component.getDocument ();
        DataObject dataObject = (DataObject) document.getProperty (Document.StreamDescriptionProperty);
        FileObject fileObject = dataObject.getPrimaryFile ();
        Project project = FileOwnerQuery.getOwner (fileObject);
        Locale locale = LocaleQuery.findLocale(fileObject);
        DictionaryImpl dictionary = projects && project != null ?
            ComponentPeer.getProjectDictionary (project, locale) :
            ComponentPeer.getUsersLocalDictionary (locale);
        dictionary.addEntry (word);
        ComponentPeer componentPeer = (ComponentPeer) component.getClientProperty (ComponentPeer.class);
        componentPeer.reschedule();
    }
    
    public void processKeyEvent (
        KeyEvent                evt
    ) {
    }
    
    public int getPreferredWidth (
        Graphics                g,
        Font                    defaultFont
    ) {
        return CompletionUtilities.getPreferredWidth (getText (), null, g, defaultFont);
    }
    
    public void render (
        Graphics                g,
        Font                    defaultFont,
        Color                   defaultColor,
        Color                   backgroundColor,
        int                     width,
        int                     height,
        boolean                 selected
    ) {
        if (selected) {
            g.setColor (backgroundColor);
            g.fillRect (0, 0, width, height);
            g.setColor (defaultColor);
        }
        CompletionUtilities.renderHtml (
            null, getText (), null, g,
            defaultFont, defaultColor,
            width, height, selected
        );
    }
    
    public CompletionTask createDocumentationTask () {
        return null;
    }
    
    public CompletionTask createToolTipTask () {
        return null;
    }
    
    public boolean instantSubstitution (
        JTextComponent          component
    ) {
        return true;
    }
    
    public int getSortPriority () {
        return 200;
    }
    
    public CharSequence getSortText () {
        return getText();
    }
    
    protected String getText () {
        if (projects)
            return NbBundle.getMessage (AddToDictionaryCompletionItem.class, "CTL_Add_to_projects");
        return NbBundle.getMessage (AddToDictionaryCompletionItem.class, "CTL_Add_to_private");
    }
    
    public CharSequence getInsertPrefix () {
        return "";
    }
}

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

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
package org.netbeans.modules.print.provider;

import java.awt.Component;
import java.awt.Container;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.openide.cookies.EditorCookie;
import org.openide.util.Lookup;

import org.netbeans.modules.print.util.Config;
import static org.netbeans.modules.print.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.04.04
 */
public final class EditorProvider extends ComponentProvider {

    public EditorProvider(EditorCookie editor, Date lastModified) {
        super(null, getName(editor), lastModified);
        myEditor = editor;
    }

    @Override
    protected JComponent getComponent() {
        JTextComponent text = getTextComponent();

        if (text == null) {
            return null;
        }
        if (Config.getDefault().isAsEditor()) {
            return getEditorComponent(text);
        }
        Document document = myEditor.getDocument();

        if (document == null) {
            return null;
        }
        int start;
        int end;

        if (Config.getDefault().isSelection()) {
            start = text.getSelectionStart();
            end = text.getSelectionEnd();
        }
        else {
            start = 0;
            end = document.getLength();
        }
        AttributedCharacterIterator[] iterators = getIterators(document, start, end);
//out();
//out("iterators: " + iterators);
//out();
        if (iterators != null) {
            return new ComponentDocument(iterators);
        }
        try {
            return new ComponentDocument(text.getText(start, end - start));
        }
        catch (BadLocationException e) {
            return null;
        }
    }

    private AttributedCharacterIterator[] getIterators(Document document, int start, int end) {
        ActionListener action = (ActionListener) Lookup.getDefault().lookup(ActionListener.class);
//out();
//out("Action: " + action);
//out();
        if (action == null) {
            return null;
        }
        if ( !action.getClass().getName().contains(".print.")) { // NOI18N
            return null;
        }
        List<Object> source = new ArrayList<Object>();
        source.add(document);
        source.add(Integer.valueOf(start));
        source.add(Integer.valueOf(end));
        ActionEvent event = new ActionEvent(source, 0, null);
        action.actionPerformed(event);
        Object object = event.getSource();

        if ( !(object instanceof List)) {
            return null;
        }
        List list = (List) object;

        if (list.size() != 2*2) {
            return null;
        }
        Object param = list.get(1 + 2);

        if ( !(param instanceof AttributedCharacterIterator[])) {
            return null;
        }
        return (AttributedCharacterIterator[]) param;
    }

    private static String getName(EditorCookie editor) {
        Document document = editor.getDocument();

        if (document == null) {
            return null;
        }
        String title = (String) document.getProperty(Document.TitleProperty);

        if (title == null) {
            return null;
        }
        return title.replace('\\', '/'); // NOI18N
    }

    private JTextComponent getTextComponent() {
        JEditorPane[] panes = myEditor.getOpenedPanes();

        if (panes == null || panes.length == 0) {
            return null;
        }
        return panes[0];
    }

    private JComponent getEditorComponent(JComponent text) {
        if ( !Config.getDefault().isLineNumbers()) {
            return text;
        }
        JComponent lineNumber = getLineNumberComponent(getParent(text));

        if (lineNumber == null) {
            return text;
        }
        List<JComponent> components = new ArrayList<JComponent>();

        components.add(lineNumber);
        components.add(text);

        return new ComponentPanel(components);
    }

    private JComponent getLineNumberComponent(Component component) {
        if (component == null) {
            return null;
        }
//out("  see: " + component.getClass().getName());

        if (component.getClass().getName().equals("org.netbeans.editor.GlyphGutter")) { // NOI18N
            if (component instanceof JComponent) {
                return (JComponent) component;
            }
        }
        if ( !(component instanceof Container)) {
            return null;
        }
        Container container = (Container) component;
        Component[] children = container.getComponents();

        for (Component child : children) {
            JComponent lineNumberComponent = getLineNumberComponent(child);

            if (lineNumberComponent != null) {
                return lineNumberComponent;
            }
        }
        return null;
    }

    private Component getParent(Component component) {
        Component parent = component.getParent();

        if (parent == null) {
            return component;
        }
        return getParent(parent);
    }

    private EditorCookie myEditor;
}

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
package org.netbeans.modules.editor.search;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.EditorUI;
import org.netbeans.spi.editor.SideBarFactory;
import org.netbeans.modules.editor.NbEditorKit;

@MimeRegistration(mimeType = SearchNbEditorKit.SEARCHBAR_MIMETYPE, service = EditorKit.class)
public final class SearchNbEditorKit extends NbEditorKit {

    public static final String REPLACE_ACTION = "replace"; // NOI18N
    public static final String SEARCH_ACTION = "find"; // NOI18N
    public static final String SEARCHBAR_MIMETYPE = "text/x-editor-search"; // NOI18N
    public static final String PROP_SEARCH_CONTAINER = "diff.search.container"; // NOI18N

    @SuppressWarnings("unchecked")
    public static <T> T findComponent(Container container, Class<T> componentClass, int depth) {
        if (depth > 0) {
            for (Component c : container.getComponents()) {
                if (componentClass.isAssignableFrom(c.getClass())) {
                    return (T) c;
                } else if (c instanceof Container) {
                    T target = findComponent((Container) c, componentClass, depth - 1);
                    if (target != null) {
                        return target;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getContentType() {
        return SEARCHBAR_MIMETYPE;
    }

    public static final class Factory implements SideBarFactory {

        @Override
        public JComponent createSideBar(JTextComponent target) {
            SearchJPanel searchJPanel = new SearchJPanel();
            searchJPanel.setLayout(new BoxLayout(searchJPanel, BoxLayout.Y_AXIS));
            return searchJPanel;
        }
    }

    public static class SearchJPanel extends JPanel {
    }
    private static PropertyChangeListener searchAndReplaceBarPersistentListener = null;

    public static synchronized void makeSearchAndReplaceBarPersistent() {
        if (searchAndReplaceBarPersistentListener == null) {
            searchAndReplaceBarPersistentListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(EditorRegistry.FOCUS_GAINED_PROPERTY)
                            && SearchBar.getInstance().getActualTextComponent() != EditorRegistry.lastFocusedComponent()
                            && SearchBar.getInstance().isVisible()) {
                        JTextComponent target = EditorRegistry.lastFocusedComponent();
                        JPanel jp = null;
                        Object clientProperty = target.getClientProperty(SearchNbEditorKit.PROP_SEARCH_CONTAINER);
                        if (clientProperty instanceof JPanel) {
                            jp = (JPanel) clientProperty;
                        } else {
                            EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(target);
                            if (eui != null) {

                                JComponent comp = eui.hasExtComponent() ? eui.getExtComponent() : null;
                                if (comp != null) {
                                    jp = SearchNbEditorKit.findComponent(comp, SearchNbEditorKit.SearchJPanel.class, 5);
                                }
                            }
                        }
                        if (jp != null) {
                            SearchBar searchBarInstance = SearchBar.getInstance(target);
                            ReplaceBar replaceBarInstance = ReplaceBar.getInstance(searchBarInstance);
                            jp.add(searchBarInstance);
                            if (replaceBarInstance.isVisible()) {
                                jp.add(replaceBarInstance);
                            }
                            jp.revalidate();
                            searchBarInstance.checkRegainFocus(evt);
                        }
                    } else if (EditorRegistry.FOCUS_LOST_PROPERTY.equals(evt.getPropertyName())) {
                        SearchBar.getInstance().notifyFocusLost(evt);
                    }
                }
            };
            EditorRegistry.addPropertyChangeListener(searchAndReplaceBarPersistentListener);
        }
    }

    public static void openFindIfNecessary(JTextComponent component, ActionEvent evt) {
        Object findWhat = EditorFindSupport.getInstance().getFindProperty(EditorFindSupport.FIND_WHAT);
        if (!(findWhat instanceof String) || ((String) findWhat).isEmpty()) {

            Action findAction = ((BaseKit) component.getUI().getEditorKit(
                    component)).getActionByName("find");
            if (findAction != null) {
                findAction.actionPerformed(evt);
            }
        }
    }
}

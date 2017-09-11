/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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

    public synchronized static void makeSearchAndReplaceBarPersistent() {
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
        if (findWhat == null || !(findWhat instanceof String) || ((String) findWhat).isEmpty()) {

            Action findAction = ((BaseKit) component.getUI().getEditorKit(
                    component)).getActionByName("find");
            if (findAction != null) {
                findAction.actionPerformed(evt);
            }
        }
    }
}

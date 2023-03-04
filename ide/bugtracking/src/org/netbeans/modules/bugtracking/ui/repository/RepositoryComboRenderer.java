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

package org.netbeans.modules.bugtracking.ui.repository;

import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.api.Repository;
import org.openide.util.NbBundle;

/**
 *
 * @author tomas
 * @author  Marian Petras
 */
public class RepositoryComboRenderer extends DefaultListCellRenderer {
    
    private final String loadingReposText = NbBundle.getMessage(
                            RepositoryComboSupport.class,
                            "RepositoryComboSupport.loadingRepositories");   //NOI18N
    private final String noRepositories = NbBundle.getMessage(
                            RepositoryComboSupport.class,
                            "RepositoryComboSupport.noRepositories");   //NOI18N
    private final String selectRepoText = NbBundle.getMessage(
                            RepositoryComboSupport.class,
                            "RepositoryComboSupport.selectRepository"); //NOI18N

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String text;
        Repository repo = null;
        if (value == null) {
            text = null;        
        } else if (value instanceof String && ((String) value).trim().equals("")) {
            text = (String) value;            
        } else if (value instanceof Repository) {
            repo = (Repository) value;
            text = repo.getDisplayName();
        } else if (value instanceof RepositoryImpl) {
            assert false : "the value provided to the renderer should be a Repository"; // NOI18N
            repo = ((RepositoryImpl) value).getRepository();
            text = repo.getDisplayName();
        } else {
            if (value == RepositoryComboSupport.LOADING_REPOSITORIES) {
                text = loadingReposText;
            } else if (value == RepositoryComboSupport.NO_REPOSITORIES) {
                text = noRepositories;
            } else {
                assert (value == RepositoryComboSupport.SELECT_REPOSITORY) : value;
                text = selectRepoText;
            }
        }
        Component result = super.getListCellRendererComponent(list,
                                                              text,
                                                              index,
                                                              isSelected,
                                                              cellHasFocus);
        if (result instanceof JLabel) {
            JLabel label = (JLabel) result;
            if (repo != null) {
                Image icon = repo.getIcon();
                if(icon instanceof Icon) {
                    label.setIcon((Icon) icon);
                } else if(icon instanceof Image) {
                    label.setIcon(new ImageIcon(icon));
                }
            } else {
                Font font = label.getFont();
                label.setFont(new Font(font.getName(),
                                       font.getStyle() | Font.ITALIC,
                                       font.getSize()));
            }
        }
        return result;
    }
}

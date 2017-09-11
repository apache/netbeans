/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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

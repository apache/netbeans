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
package org.netbeans.modules.cnd.search.impl.spi;

import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.modules.cnd.search.SearchParams;
import org.netbeans.modules.cnd.search.SearchResult;
import org.netbeans.modules.cnd.search.ui.CNDSearchPanel;
import org.netbeans.modules.cnd.search.ui.CNDSearchPanel.ValidationStatus;
import org.netbeans.spi.search.provider.SearchComposition;
import org.netbeans.spi.search.provider.SearchProvider.Presenter;
import org.openide.NotificationLineSupport;
import org.openide.util.HelpCtx;

/**
 *
 */
public final class CNDSearchPresenter extends Presenter {

    private CNDSearchPanel panel = null;

    public CNDSearchPresenter(CNDSearchProvider provider) {
        super(provider, false);
    }

    @Override
    public JComponent getForm() {
        assert SwingUtilities.isEventDispatchThread();
        if (panel == null) {
            panel = new CNDSearchPanel();
            panel.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    fireChange();
                }
            });
        }
        return panel;
    }

    @Override
    public SearchComposition<SearchResult> composeSearch() {
        panel.storeSettings();

        SearchPattern searchPattern = panel.getSearchPattern();
        String textToFind = searchPattern.getSearchExpression();
        String fileName = panel.getFileName();
        String title = textToFind == null || textToFind.isEmpty()
                ? fileName
                : textToFind;

        List<SearchRoot> searchRoots = panel.getSearchInfo().getSearchRoots();

        if (searchRoots.isEmpty()) {
            return null;
        }

        SearchParams params = new SearchParams(searchRoots, fileName, searchPattern);

        return new CNDSearchComposition(title, this, params);
    }

    @Override
    public boolean isUsable(NotificationLineSupport nls) {
        ValidationStatus status = panel.getValidationStatus();

        if (status == ValidationStatus.OK) {
            nls.clearMessages();
            return true;
        }

        nls.setErrorMessage(status.error);
        return false;

    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("FindinProject"); // NOI18N
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

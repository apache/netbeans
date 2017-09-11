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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.search;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.search.ui.UiUtils;
import org.netbeans.spi.search.provider.SearchComposition;
import org.netbeans.spi.search.provider.SearchResultsDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
/**
 *
 * @author kaktus
 */
class ResultViewPanel extends JPanel implements Lookup.Provider {

    @StaticResource
    private static final String STOP_ICON =
            "org/netbeans/modules/search/res/stop.png"; //NOI18N
    @StaticResource
    private static final String INFO_ICON =
            "org/netbeans/modules/search/res/info.png";                 //NOI18N

    private static final String CARD_NAME_RESULTS = "results";          //NOI18N
    private static final String CARD_NAME_INFO = "info";                //NOI18N

    /**
     * tree view for displaying found objects
     */
    private final CardLayout resultViewCards;

    private JPanel resultsPanel;
    private JPanel infoPanel;
    private JPanel infoPanelContent;
    private JButton btnStop = new JButton();
    private SearchTask searchTask;
    private GraphicalSearchListener searchListener = null;
    private final JComponent visualComponent;
    private final Lookup lookup;

    /** */
    private volatile boolean searchInProgress = false;

    private SearchComposition<?> searchComposition;

    public ResultViewPanel(SearchTask searchTask) {
        setLayout(resultViewCards = new CardLayout());
        this.searchComposition = searchTask.getComposition();
        this.searchTask = searchTask;
        SearchResultsDisplayer<?> displayer =
                searchComposition.getSearchResultsDisplayer();
        setName(displayer.getTitle());
        displayer.setInfoNode(this.createListener().getInfoNode());
        resultsPanel = new JPanel();
        resultsPanel.setLayout(
                new BoxLayout(resultsPanel, BoxLayout.PAGE_AXIS));
        SearchResultsDisplayer<?> disp =
                searchComposition.getSearchResultsDisplayer();
        visualComponent = disp.getVisualComponent();
        lookup = (visualComponent instanceof Lookup.Provider)
                ? ((Lookup.Provider) visualComponent).getLookup()
                : Lookup.EMPTY;
        resultsPanel.add(visualComponent);
        add(resultsPanel, CARD_NAME_RESULTS);
        showInfo(UiUtils.getText("TEXT_WAITING_FOR_PREVIOUS"));         //NOI18N
    }

    void componentOpened() {
    }

    final synchronized void showInfo(String title) {
        if (infoPanel == null) {
            infoPanel = new JPanel();
            infoPanel.setLayout(new BorderLayout());
            JScrollPane scrollPane = new JScrollPane();
            infoPanelContent = new JPanel(new FlowLayout(FlowLayout.LEADING));
            infoPanelContent.setBackground(UIManager.getColor("TextField.background")); //NOI18N
            scrollPane.setViewportView(infoPanelContent);
            infoPanel.add(scrollPane, BorderLayout.CENTER);
            add(infoPanel, CARD_NAME_INFO);
            JToolBar toolBar = new JToolBar();
            toolBar.setFloatable(false);
            toolBar.setOrientation(JToolBar.VERTICAL);
            btnStop.setIcon(ImageUtilities.loadImageIcon(STOP_ICON, false));
            btnStop.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    searchCancelled();
                }
            });
            btnStop.setToolTipText(UiUtils.getText("TEXT_BUTTON_STOP"));//NOI18N
            toolBar.add(btnStop);
            infoPanel.add(toolBar, BorderLayout.WEST);
            this.revalidate();
        }
        infoPanelContent.removeAll();
        infoPanelContent.add(new JLabel(ImageUtilities.loadImageIcon(
                INFO_ICON, false)));
        infoPanelContent.add(new JLabel(title));
        infoPanel.validate();
        infoPanel.repaint();
        resultViewCards.show(this, CARD_NAME_INFO);
    }

    synchronized void showResults() {
        resultViewCards.show(this, CARD_NAME_RESULTS);
    }

    public synchronized final GraphicalSearchListener createListener() {
        if (searchListener == null) {
            searchListener = new GraphicalSearchListener(
                    searchComposition, this);
        }
        return searchListener;
    }

    /**
     */
    void searchStarted() {
        searchInProgress = true;
        resultViewCards.show(this, CARD_NAME_RESULTS);
    }

    /**
     */
    void searchFinished() {
        searchInProgress = false;
    }

    /**
     */
    void searchInterrupted() {
        searchFinished();
    }

    /**
     */
    void searchCancelled() {
        Manager.getInstance().stopSearching(searchTask);
        searchTask.cancel();
        showInfo(NbBundle.getMessage(ResultView.class,
                "TEXT_TASK_CANCELLED"));                                //NOI18N
        setBtnStopEnabled(false);
        searchInProgress = false;
        searchComposition.getSearchResultsDisplayer().searchFinished();
    }

    @Override
    public boolean requestFocusInWindow() {
        if (resultsPanel != null && resultsPanel.getComponentCount() > 0) {
            JComponent comp = (JComponent) resultsPanel.getComponent(0);
            if (comp != null) {
                return comp.requestFocusInWindow();
            }
        }
        return super.requestFocusInWindow();
    }

    void setBtnStopEnabled(boolean enabled) {
        btnStop.setEnabled(enabled);
    }

    boolean isSearchInProgress() {
        return searchInProgress;
    }

    SearchComposition<?> getSearchComposition() {
        return this.searchComposition;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public String getToolTipText() {
        return visualComponent.getToolTipText();
    }
}

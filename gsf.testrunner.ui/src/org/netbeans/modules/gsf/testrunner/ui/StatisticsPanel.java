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

package org.netbeans.modules.gsf.testrunner.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.RerunHandler;
import org.netbeans.modules.gsf.testrunner.api.RerunType;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.openide.awt.ToolbarWithOverflow;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Panel containing the toolbar and the tree of test results.
 *
 * @author  Marian Petras
 */
public final class StatisticsPanel extends JPanel {
    
    /** */
    private final ResultPanelTree treePanel;
    /** */
    private JToggleButton btnShowPassed;
    private JToggleButton btnShowFailed;
    private JToggleButton btnShowError;
    private JToggleButton btnShowPassedWithErrors;
    private JToggleButton btnShowIgnored;
    private JToggleButton btnShowSkipped;
    private JToggleButton btnShowAborted;

    private JToggleButton btnAlwaysOpenTRW;
    private JToggleButton btnAlwaysOpenNewTab;

    /**
     * Rerun button for running (all) tests again.
     */
    private JButton rerunButton;
    private JButton rerunFailedButton;

    private JButton nextFailure;

    private JButton previousFailure;
    
    private final ResultDisplayHandler displayHandler;

    private static final String KEY_FILTER_MASK = "filterMask"; // NOI18N
    private int filterMask = NbPreferences.forModule(StatisticsPanel.class).getInt(KEY_FILTER_MASK, Status.PASSED.getBitMask());

    public static final String PROP_ALWAYS_OPEN_TRW = "alwaysOpenTRW"; //NOI18N
    private static final Icon alwaysOpenTRWIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/gsf/testrunner/ui/resources/testResults.png", true);

    public static final String PROP_ALWAYS_OPEN_NEW_TAB = "alwaysOpenNewTab"; //NOI18N
    private static final Icon alwaysOpenNewTabIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/gsf/testrunner/resources/newTab.png", true);

    private static final Icon rerunIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/gsf/testrunner/resources/rerun.png", true);
    private static final Icon rerunFailedIcon = ImageUtilities.image2Icon(ImageUtilities.mergeImages(
                            ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/rerun.png"), //NOI18N
                            ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/error-badge.gif"), //NOI18N
                            8, 8));

    private static final boolean isMacLaf = "Aqua".equals(UIManager.getLookAndFeel().getID());
    private static final Color macBackground = UIManager.getColor("NbExplorerView.background");
    
    /**
     */
    public StatisticsPanel(final ResultDisplayHandler displayHandler) {
        super(new BorderLayout(0, 0));
        this.displayHandler = displayHandler;
        JComponent toolbar = createToolbar();
        treePanel = new ResultPanelTree(displayHandler, this);
        treePanel.setFilterMask(filterMask);

        add(toolbar, BorderLayout.WEST);
        add(treePanel, BorderLayout.CENTER);
        if( isMacLaf ) {
            toolbar.setBackground(macBackground);
            treePanel.setBackground(macBackground);
        }
    }

    public ResultPanelTree getTreePanel() {
        return treePanel;
    }

    public @Override boolean requestFocusInWindow() {
        return treePanel.requestFocusInWindow();
    }

    /**
     */
    private JComponent createToolbar() {
        createShowButtons();
        createNextPrevFailureButtons();
        createRerunButtons();
	createOptionButtons();
        String testingFramework = Manager.getInstance().getTestingFramework();

        JToolBar toolbar = new ToolbarWithOverflow(SwingConstants.VERTICAL);
        toolbar.add(rerunButton);
        toolbar.add(rerunFailedButton);
        toolbar.add(new JToolBar.Separator());
        toolbar.add(btnShowPassed);
        if(testingFramework.equals(Manager.TESTNG_TF)) {
            toolbar.add(btnShowPassedWithErrors);
        }
        toolbar.add(btnShowFailed);
        toolbar.add(btnShowError);
        toolbar.add(btnShowAborted);
//        if(testingFramework.equals(Manager.TESTNG_TF) || testingFramework.equals(Manager.JUNIT_TF)) {
//            toolbar.add(btnShowIgnored);
//        }
        toolbar.add(btnShowSkipped);
	
        toolbar.add(new JToolBar.Separator());
        toolbar.add(previousFailure);
        toolbar.add(nextFailure);
        toolbar.add(new JToolBar.Separator());
        toolbar.add(btnAlwaysOpenTRW);
	toolbar.add(btnAlwaysOpenNewTab);
        
        toolbar.setFocusable(false);
        toolbar.setRollover(true);
        toolbar.setFloatable(false);
        return toolbar;
    }


    @NbBundle.Messages({"btnAlwaysOpenTRW.tooltip=Always open Test Results Window",
	"btnAlwaysOpenTRW.ACSN=Control whether Test Results Window always opens",
	"btnAlwaysOpenNewTab.tooltip=Always open new tab",
	"btnAlwaysOpenNewTab.ACSN=Control whether a new tab always opens"})
    private void createOptionButtons() {
	btnAlwaysOpenTRW = newOptionButton(
		alwaysOpenTRWIcon,
		Bundle.btnAlwaysOpenTRW_tooltip(),
		Bundle.btnAlwaysOpenTRW_ACSN(),
		PROP_ALWAYS_OPEN_TRW);
	btnAlwaysOpenNewTab = newOptionButton(
		alwaysOpenNewTabIcon,
		Bundle.btnAlwaysOpenNewTab_tooltip(),
		Bundle.btnAlwaysOpenNewTab_ACSN(),
		PROP_ALWAYS_OPEN_NEW_TAB);
    }

    private JToggleButton newOptionButton(Icon icon, String tooltip, String accessibleName, final String property) {
	JToggleButton newButton = new JToggleButton(icon);
	newButton.setToolTipText(tooltip);
	newButton.getAccessibleContext().setAccessibleName(accessibleName);
	boolean isSelected = NbPreferences.forModule(StatisticsPanel.class).getBoolean(property, false);
	newButton.setSelected(isSelected);
	newButton.addItemListener(new ItemListener() {
	    @Override
	    public void itemStateChanged(ItemEvent e) {
		boolean selected;
		switch (e.getStateChange()) {
		    case ItemEvent.SELECTED:
			selected = true;
			break;
		    case ItemEvent.DESELECTED:
			selected = false;
			break;
		    default:
			return;
		}
		ResultWindow.getInstance().updateOptionStatus(property, selected);
	    }
	});
	return newButton;
    }

    public void updateOptionStatus(String property, boolean selected) {
	if(property.equals(PROP_ALWAYS_OPEN_TRW)) {
	    btnAlwaysOpenTRW.setSelected(selected);
	} else if (property.equals(PROP_ALWAYS_OPEN_NEW_TAB)) {
	    btnAlwaysOpenNewTab.setSelected(selected);
	}
    }
    
    @NbBundle.Messages({"ACSN_RerunButton=Rerun",
	"MultiviewPanel_rerunButton_tooltip=Rerun",
	"ACSN_RerunFailedButton=Rerun failed",
	"MultiviewPanel_rerunFailedButton_tooltip=Rerun failed"})
    private void createRerunButtons() {
        rerunButton = new JButton(rerunIcon);
        rerunButton.setEnabled(false);
        rerunButton.getAccessibleContext().setAccessibleName(Bundle.ACSN_RerunButton());
        rerunButton.setToolTipText(Bundle.MultiviewPanel_rerunButton_tooltip());

        rerunFailedButton = new JButton(rerunFailedIcon);
        rerunFailedButton.setEnabled(false);
        rerunFailedButton.getAccessibleContext().setAccessibleName(Bundle.ACSN_RerunFailedButton());
        rerunFailedButton.setToolTipText(Bundle.MultiviewPanel_rerunFailedButton_tooltip());

        final RerunHandler rerunHandler = displayHandler.getSession().getRerunHandler();
        if (rerunHandler != null) {
            rerunButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    rerunHandler.rerun();
                }
            });
            rerunFailedButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    rerunHandler.rerun(treePanel.getFailedTests());
                }
            });
            rerunHandler.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    updateButtons();
                }
            });
            updateButtons();
        }
    }

    void updateButtons(){
        RerunHandler rerunHandler = displayHandler.getSession().getRerunHandler();
        if (rerunHandler == null) {
            return;
        }
        rerunButton.setEnabled(displayHandler.sessionFinished &&
                               rerunHandler.enabled(RerunType.ALL));
        rerunFailedButton.setEnabled(displayHandler.sessionFinished && 
                                     rerunHandler.enabled(RerunType.CUSTOM) &&
                                     !treePanel.getFailedTests().isEmpty());
        btnShowPassed.setEnabled(displayHandler.sessionFinished);
        btnShowPassedWithErrors.setEnabled(displayHandler.sessionFinished);
        btnShowFailed.setEnabled(displayHandler.sessionFinished);
        btnShowError.setEnabled(displayHandler.sessionFinished);
        btnShowIgnored.setEnabled(displayHandler.sessionFinished);
        btnShowSkipped.setEnabled(displayHandler.sessionFinished);
        btnShowAborted.setEnabled(displayHandler.sessionFinished);
        nextFailure.setEnabled(displayHandler.sessionFinished);
        previousFailure.setEnabled(displayHandler.sessionFinished);
    }

    @NbBundle.Messages({"StatisticsPanel_btnShowPassed=Show Passed",
	"ACSN_ShowPassedButton=Show results of the passed tests",
        "StatisticsPanel_btnShowPassedWithErrors=Show Passed with Errors",
	"ACSN_ShowPassedWithErrorsButton=Show results of the passed with error tests",
        "StatisticsPanel_btnShowFailed=Show Failed",
	"ACSN_ShowFailedButton=Show results of the failed tests",
        "StatisticsPanel_btnShowError=Show Error",
	"ACSN_ShowErrorButton=Show results of the error tests",
        "StatisticsPanel_btnShowAborted=Show Aborted",
	"ACSN_ShowAbortedButton=Show results of the aborted tests",
        "StatisticsPanel_btnShowIgnored=Show Ignored",
	"ACSN_ShowIgnoredButton=Show results of the ignored tests",
        "StatisticsPanel_btnShowSkipped=Show Skipped",
	"ACSN_ShowSkippedButton=Show results of the skipped tests"})
    private void createShowButtons() {
        btnShowPassed = newShowButton(
               "org/netbeans/modules/gsf/testrunner/resources/ok_16.png",
               Bundle.StatisticsPanel_btnShowPassed(),
               Bundle.ACSN_ShowPassedButton(),
               Status.PASSED);
        btnShowPassedWithErrors = newShowButton(
               "org/netbeans/modules/gsf/testrunner/resources/ok_withErrors_16.png",
               Bundle.StatisticsPanel_btnShowPassedWithErrors(),
               Bundle.ACSN_ShowPassedWithErrorsButton(),
               Status.PASSEDWITHERRORS);
        btnShowFailed = newShowButton(
               "org/netbeans/modules/gsf/testrunner/resources/warning_16.png",
               Bundle.StatisticsPanel_btnShowFailed(),
               Bundle.ACSN_ShowFailedButton(),
               Status.FAILED);
        btnShowError = newShowButton(
               "org/netbeans/modules/gsf/testrunner/resources/error_16.png",
               Bundle.StatisticsPanel_btnShowError(),
               Bundle.ACSN_ShowErrorButton(),
               Status.ERROR);
        btnShowAborted = newShowButton(
               "org/netbeans/modules/gsf/testrunner/resources/aborted.png",
               Bundle.StatisticsPanel_btnShowAborted(),
               Bundle.ACSN_ShowAbortedButton(),
               Status.ABORTED);
        btnShowIgnored = newShowButton(
               "org/netbeans/modules/gsf/testrunner/resources/ignored_16.png",
               Bundle.StatisticsPanel_btnShowIgnored(),
               Bundle.ACSN_ShowIgnoredButton(),
               Status.IGNORED);
        btnShowSkipped = newShowButton(
               "org/netbeans/modules/gsf/testrunner/resources/skipped_16.png",
               Bundle.StatisticsPanel_btnShowSkipped(),
               Bundle.ACSN_ShowSkippedButton(),
               Status.SKIPPED);
    }

    private JToggleButton newShowButton(String iconId,
                                          String tooltip,
                                          String accessibleName,
                                          Status status) {
        JToggleButton btn = new JToggleButton(ImageUtilities.loadImageIcon(iconId, true));
        btn.setToolTipText(tooltip);
        btn.getAccessibleContext().setAccessibleName(accessibleName);
        btn.setSelected((filterMask & status.getBitMask()) == 0);
        btn.addItemListener(new FilterItemListener(status));
        return btn;
    }

    void copyFilterMask(StatisticsPanel sp) {
        filterMask = sp.filterMask;
        updateShowButtons();
    }

    private void updateShowButtons() {
        btnShowPassed.setSelected((filterMask & Status.PASSED.getBitMask()) == 0);
        btnShowPassedWithErrors.setSelected((filterMask & Status.PASSEDWITHERRORS.getBitMask()) == 0);
        btnShowFailed.setSelected((filterMask & Status.FAILED.getBitMask()) == 0);
        btnShowError.setSelected((filterMask & Status.ERROR.getBitMask()) == 0);
        btnShowIgnored.setSelected((filterMask & Status.IGNORED.getBitMask()) == 0);
        btnShowSkipped.setSelected((filterMask & Status.SKIPPED.getBitMask()) == 0);
        btnShowAborted.setSelected((filterMask & Status.ABORTED.getBitMask()) == 0);
    }

    @NbBundle.Messages({"MSG_NextFailure=Next Failure - Ctrl+Period",
	"MSG_PreviousFailure=Previous Failure - Ctrl+Comma"})
    private void createNextPrevFailureButtons() {
        nextFailure = new JButton(ImageUtilities.loadImageIcon("org/netbeans/modules/gsf/testrunner/resources/nextmatch.png", true));
        nextFailure.setToolTipText(Bundle.MSG_NextFailure());
        nextFailure.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectNextFailure();
            }
        });

        previousFailure = new JButton(ImageUtilities.loadImageIcon("org/netbeans/modules/gsf/testrunner/resources/prevmatch.png", true));

        previousFailure.setToolTipText(Bundle.MSG_PreviousFailure());
        previousFailure.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectPreviousFailure();
            }
        });
    }

    void selectPreviousFailure() {
        treePanel.selectPreviousFailure();
    }

    void rerun(boolean rerunFailed) {
	final RerunHandler rerunHandler = displayHandler.getSession().getRerunHandler();
	if (rerunHandler != null) {
	    if (rerunFailed) {
		if (!treePanel.getFailedTests().isEmpty()) {
		    rerunHandler.rerun(treePanel.getFailedTests());
		}
		return;
	    }
	    rerunHandler.rerun();
	}
    }

    void selectNextFailure() {
        treePanel.selectNextFailure();
    }
    
    /**
     */
    void displayReport(final Report report) {
        treePanel.displayReport(report);        
//        btnFilter.setEnabled(
//          treePanel.getSuccessDisplayedLevel() != RootNode.ALL_PASSED_ABSENT);
    }
    
    /**
     */
    void displayReports(final List<Report> reports) {
        if (reports.isEmpty()) {
            return;
        }
        
        treePanel.displayReports(reports);
        
//        btnFilter.setEnabled(
//          treePanel.getSuccessDisplayedLevel() != RootNode.ALL_PASSED_ABSENT);
    }

    /**
     * Displays a message about a running suite.
     *
     * @param  suiteName  name of the running suite,
     *                    or {@code ANONYMOUS_SUITE} for anonymous suites
     * @see  ResultDisplayHandler#ANONYMOUS_SUITE
     */
    void displaySuiteRunning(final String suiteName) {
        treePanel.displaySuiteRunning(suiteName);
    }
    
    /**
     */
    void displayMsg(final String msg) {
        treePanel.displayMsg(msg);
    }

    private class FilterItemListener implements ItemListener {
        private int itemMask;

        public FilterItemListener(Status status) {
            this.itemMask = status.getBitMask();
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            switch(e.getStateChange()) {
                case ItemEvent.SELECTED:
                    filterMask &= ~itemMask;
                    break;
                case ItemEvent.DESELECTED:
                    filterMask |= itemMask;
                    break;
                default:
                    return;
            }
            treePanel.setFilterMask(filterMask);
            NbPreferences.forModule(StatisticsPanel.class).putInt(KEY_FILTER_MASK, filterMask);
        }
    } // FilterItemListener

}

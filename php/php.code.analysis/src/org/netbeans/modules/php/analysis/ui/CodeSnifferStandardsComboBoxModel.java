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
package org.netbeans.modules.php.analysis.ui;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.analysis.commands.CodeSniffer;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.util.StringUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public final class CodeSnifferStandardsComboBoxModel extends AbstractListModel<String> implements ComboBoxModel<String> {

    private static final long serialVersionUID = -35785646574684544L;

    static final Logger LOGGER = Logger.getLogger(CodeSnifferStandardsComboBoxModel.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(CodeSnifferStandardsComboBoxModel.class);

    @NbBundle.Messages("CodeSnifferStandardsComboBoxModel.standards.fetching=<fetching standards...>")
    private static final String FETCHING_STANDARDS = Bundle.CodeSnifferStandardsComboBoxModel_standards_fetching();
    @NbBundle.Messages("CodeSnifferStandardsComboBoxModel.standards.none=<no standards available>")
    private static final String NO_STANDARDS_AVAILABLE = Bundle.CodeSnifferStandardsComboBoxModel_standards_none();


    // @GuardedBy("EDT")
    private final List<String> standards = new ArrayList<>();

    volatile String selectedStandard = null;


    public CodeSnifferStandardsComboBoxModel() {
        assert EventQueue.isDispatchThread();
        setFetchingStandards();
    }

    @Override
    public int getSize() {
        assert EventQueue.isDispatchThread();
        return standards.size();
    }

    @Override
    public String getElementAt(int index) {
        assert EventQueue.isDispatchThread();
        return standards.get(index);
    }

    @Override
    public void setSelectedItem(final Object anItem) {
        if (anItem == null) {
            return;
        }
        // need to do that in the RP since fetch can be running
        RP.post(new Runnable() {
            @Override
            public void run() {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String standard = (String) anItem;
                        if (!standard.equals(selectedStandard)
                                && standards.contains(standard)) {
                            selectedStandard = standard;
                            fireContentsChanged();
                        }
                    }
                });
            }
        });
    }

    /**
     * Use {@link #getSelectedStandard()}.
     */
    @CheckForNull
    @Override
    public String getSelectedItem() {
        return selectedStandard;
    }

    @CheckForNull
    public String getSelectedStandard() {
        if (selectedStandard == NO_STANDARDS_AVAILABLE
                || selectedStandard == FETCHING_STANDARDS ) {
            return null;
        }
        return selectedStandard;
    }

    public void fetchStandards(final JComboBox<String> component) {
        fetchStandards(component, null);
    }

    public void fetchStandards(final JComboBox<String> component, @NullAllowed Runnable postTask) {
        fetchStandards(component, null, postTask);
    }

    public void fetchStandards(final JComboBox<String> component, @NullAllowed final String customCodeSnifferPath, @NullAllowed final Runnable postTask) {
        assert EventQueue.isDispatchThread();
        assert component != null;

        component.setEnabled(false);
        RP.post(new Runnable() {
            @Override
            public void run() {
                List<String> fetchedStandards = null;
                CodeSniffer codeSniffer;
                try {
                    if (StringUtils.hasText(customCodeSnifferPath)) {
                        codeSniffer = CodeSniffer.getCustom(customCodeSnifferPath);
                    } else {
                        codeSniffer = CodeSniffer.getDefault();
                    }
                    fetchedStandards = codeSniffer.getStandards();
                } catch (InvalidPhpExecutableException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
                final List<String> standardsRef = fetchedStandards == null ? null : new ArrayList<>(fetchedStandards);
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        assert EventQueue.isDispatchThread();
                        component.setEnabled(true);
                        if (standardsRef == null) {
                            setNoStandards();
                            component.setPrototypeDisplayValue(NO_STANDARDS_AVAILABLE);
                        } else {
                            // #231832 - add one empty item
                            standardsRef.add(0, ""); // NOI18N
                            setStandards(standardsRef);
                        }
                        if (postTask != null) {
                            postTask.run();
                        }
                    }
                });
            }
        });
    }

    void setFetchingStandards() {
        assert EventQueue.isDispatchThread();
        standards.clear();
        standards.add(FETCHING_STANDARDS);
        selectedStandard = FETCHING_STANDARDS;
        fireContentsChanged();
    }

    void setNoStandards() {
        assert EventQueue.isDispatchThread();
        standards.clear();
        standards.add(NO_STANDARDS_AVAILABLE);
        selectedStandard = NO_STANDARDS_AVAILABLE;
        fireContentsChanged();
    }

    void setStandards(List<String> standards) {
        assert EventQueue.isDispatchThread();
        this.standards.clear();
        this.standards.addAll(standards);
        if (!standards.isEmpty()) {
            selectedStandard = standards.get(0);
        }
        fireContentsChanged();
    }

    void fireContentsChanged() {
        fireContentsChanged(this, -1, -1);
    }

}

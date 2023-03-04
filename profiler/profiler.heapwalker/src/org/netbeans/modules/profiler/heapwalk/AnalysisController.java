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

package org.netbeans.modules.profiler.heapwalk;

import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.modules.profiler.heapwalk.memorylint.MemoryLint;
import org.netbeans.modules.profiler.heapwalk.memorylint.Rule;
import org.netbeans.modules.profiler.heapwalk.model.BrowserUtils;
import org.netbeans.modules.profiler.heapwalk.ui.AnalysisControllerUI;
import org.openide.util.NbBundle;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.BoundedRangeModel;
import javax.swing.JPanel;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.openide.ErrorManager;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "AnalysisController_CannotResolveClassMsg=Cannot resolve class {0}",
    "AnalysisController_CannotResolveInstanceMsg=Cannot resolve instance #{0} of class {1}"
})
public class AnalysisController extends AbstractTopLevelController implements NavigationHistoryManager.NavigationHistoryCapable {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private HeapFragmentWalker heapFragmentWalker;
    private List<Rule> rules = null;
    private MemoryLint runningMemoryLint;
    private boolean analysisRunning = false;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // --- Constructors ----------------------------------------------------------
    public AnalysisController(HeapFragmentWalker heapFragmentWalker) {
        this.heapFragmentWalker = heapFragmentWalker;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean isAnalysisRunning() {
        return analysisRunning;
    }

    // --- NavigationHistoryManager.NavigationHistoryCapable implementation ------
    public NavigationHistoryManager.Configuration getCurrentConfiguration() {
        return new NavigationHistoryManager.Configuration();
    }

    // --- Public interface ------------------------------------------------------
    public HeapFragmentWalker getHeapFragmentWalker() {
        return heapFragmentWalker;
    }

    public List<Rule> getRules() {
        if (rules == null) {
            rules = new ArrayList<Rule>(MemoryLint.createRules());
        }

        return rules;
    }

    public void cancelAnalysis() {
        if (runningMemoryLint != null) {
            runningMemoryLint.interrupt();
            analysisRunning = false;
            runningMemoryLint = null;
        }
    }

    public void configure(NavigationHistoryManager.Configuration configuration) {
        heapFragmentWalker.switchToHistoryAnalysisView();
    }

    public BoundedRangeModel performAnalysis(boolean[] rulesSelection) {
        final List<Rule> selectedRules = new ArrayList<Rule>();
        final List<Rule> allRules = getRules();

        for (int i = 0; i < rulesSelection.length; i++) {
            if (rulesSelection[i]) {
                selectedRules.add(allRules.get(i));
            }
        }

        if (selectedRules.size() > 0) {
            analysisRunning = true;

            final MemoryLint ml = new MemoryLint(heapFragmentWalker.getHeapFragment());
            runningMemoryLint = ml;
            BrowserUtils.performTask(new Runnable() {
                    public void run() {
                        try {
                            ml.process(selectedRules);
                        } catch (Exception e) {
                            ErrorManager.getDefault().log(ErrorManager.ERROR, e.getMessage());
                        }
                        rules = null;
                        analysisRunning = false;
                        runningMemoryLint = null;

                        AnalysisControllerUI ui = (AnalysisControllerUI)getPanel();
                        ui.displayNewRules();
                        if (!ml.isInterruped()) ui.setResult(ml.getResults());
                    }
                });

            return ml.getGlobalProgress();
        } else {
            return null;
        }
    }

    public void showURL(URL url) {
        String urls = url.toString();

        if (urls.startsWith("file://instance/")) { // NOI18N
            urls = urls.substring("file://instance/".length()); // NOI18N

            String[] id = urls.split("/"); // NOI18N
            JavaClass c = heapFragmentWalker.getHeapFragment().getJavaClassByName(id[0]);

            if (c != null) {
                List<Instance> instances = c.getInstances();
                Instance i = null;
                int instanceNumber = Integer.parseInt(id[1]);
                if (instanceNumber <= instances.size()) i = instances.get(instanceNumber - 1);

                if (i != null) {
                    heapFragmentWalker.getClassesController().showInstance(i);
                } else {
                    ProfilerDialogs.displayError(Bundle.AnalysisController_CannotResolveInstanceMsg(id[1], c.getName()));
                }
            } else {
                ProfilerDialogs.displayError(Bundle.AnalysisController_CannotResolveClassMsg(id[0]));
            }
        } else if (urls.startsWith("file://class/")) { // NOI18N
            urls = urls.substring("file://class/".length()); // NOI18N

            JavaClass c = heapFragmentWalker.getHeapFragment().getJavaClassByName(urls);

            if (c != null) {
                heapFragmentWalker.getClassesController().showClass(c);
            } else {
                ProfilerDialogs.displayError(Bundle.AnalysisController_CannotResolveClassMsg(urls));
            }
        }
    }

    protected AbstractButton[] createClientPresenters() {
        return new AbstractButton[0];
    }

    protected AbstractButton createControllerPresenter() {
        return ((AnalysisControllerUI) getPanel()).getPresenter();
    }

    // --- Protected implementation ----------------------------------------------
    protected JPanel createControllerUI() {
        return new AnalysisControllerUI(this);
    }
}

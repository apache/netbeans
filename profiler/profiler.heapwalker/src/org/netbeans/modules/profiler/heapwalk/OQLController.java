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
package org.netbeans.modules.profiler.heapwalk;

import java.awt.Color;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.AbstractButton;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;
import org.netbeans.modules.profiler.heapwalk.memorylint.Utils;
import org.netbeans.modules.profiler.heapwalk.model.BrowserUtils;
import org.netbeans.modules.profiler.heapwalk.ui.OQLControllerUI;
import org.netbeans.modules.profiler.oql.engine.api.OQLEngine;
import org.netbeans.modules.profiler.oql.engine.api.OQLEngine.ObjectVisitor;
import org.netbeans.modules.profiler.oql.engine.api.OQLException;
import org.netbeans.modules.profiler.oql.engine.api.ReferenceChain;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Bachorik
 * @author Jiri Sedlacek
 */
public class OQLController extends AbstractTopLevelController
                implements NavigationHistoryManager.NavigationHistoryCapable {
    
    private static final int RESULTS_LIMIT = Integer.parseInt(System.getProperty("OQLController.limitResults", "100")); // NOI18N
    
    private HeapFragmentWalker heapFragmentWalker;

    private ResultsController resultsController;
    private QueryController queryController;
    private SavedController savedController;

    private final ExecutorService progressUpdater = Executors.newSingleThreadExecutor();

    private final AtomicBoolean analysisRunning = new AtomicBoolean(false);
    private OQLEngine engine = null;


    // --- Constructor ---------------------------------------------------------

    public OQLController(HeapFragmentWalker heapFragmentWalker) {
        this.heapFragmentWalker = heapFragmentWalker;

        if (OQLEngine.isOQLSupported()) {
            engine = new OQLEngine(heapFragmentWalker.getHeapFragment());

            resultsController = new ResultsController(this);
            queryController = new QueryController(this);
            savedController = new SavedController(this);
        }
    }


    // --- Public interface ----------------------------------------------------

    public void executeQuery(String query) {
        executeQueryImpl(query);
    }

    public void cancelQuery() {
        try {
            engine.cancelQuery();
        } catch (OQLException e) {

        }
        finalizeQuery();
    }

    public boolean isQueryRunning() {
        return analysisRunning.get();
    }


    // --- Internal interface --------------------------------------------------
    
    public HeapFragmentWalker getHeapFragmentWalker() {
        return heapFragmentWalker;
    }

    public ResultsController getResultsController() {
        return resultsController;
    }

    public QueryController getQueryController() {
        return queryController;
    }

    public SavedController getSavedController() {
        return savedController;
    }


    // --- AbstractTopLevelController implementation ---------------------------

    protected AbstractButton[] createClientPresenters() {
        return new AbstractButton[] {
            resultsController.getPresenter(),
            queryController.getPresenter(),
            savedController.getPresenter()
        };
    }

    protected AbstractButton createControllerPresenter() {
        return ((OQLControllerUI) getPanel()).getPresenter();
    }

    protected JPanel createControllerUI() {
        return new OQLControllerUI(this);
    }


    // --- NavigationHistoryManager.NavigationHistoryCapable implementation ----

    public NavigationHistoryManager.Configuration getCurrentConfiguration() {
        return new NavigationHistoryManager.Configuration();
    }

    public void configure(NavigationHistoryManager.Configuration configuration) {
        heapFragmentWalker.switchToHistoryOQLView();
    }


    // --- Private implementation ----------------------------------------------

    private void executeQueryImpl(final String oqlQuery) {
        final BoundedRangeModel progressModel = new DefaultBoundedRangeModel(0, 10, 0, 100);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BrowserUtils.performTask(new Runnable() {
                    public void run() {
                        final AtomicInteger counter = new AtomicInteger(RESULTS_LIMIT);
                        progressModel.setMaximum(100);

                        final StringBuilder sb = new StringBuilder();
                        final boolean[] oddRow = new boolean[1];
                        Color oddRowBackground = UIUtils.getDarker(
                                        UIUtils.getProfilerResultsBackground());
                        final String oddRowBackgroundString =
                                "rgb(" + oddRowBackground.getRed() + "," + //NOI18N
                                         oddRowBackground.getGreen() + "," + //NOI18N
                                         oddRowBackground.getBlue() + ")"; //NOI18N

                        sb.append("<table border='0' width='100%'>"); // NOI18N

                        try {
                            analysisRunning.compareAndSet(false, true);
                            queryController.queryStarted(progressModel);
                            progressUpdater.submit(new ProgressUpdater(progressModel));
                            engine.executeQuery(oqlQuery, new ObjectVisitor() {

                                public boolean visit(Object o) {
                                    sb.append(oddRow[0] ?
                                        "<tr><td style='background-color: " + // NOI18N
                                        oddRowBackgroundString + ";'>" : "<tr><td>"); // NOI18N
                                    oddRow[0] = !oddRow[0];
                                    dump(o, sb);
                                    sb.append("</td></tr>"); // NOI18N
                                    return counter.decrementAndGet() == 0 || (!analysisRunning.get() && !engine.isCancelled()); // process all hits while the analysis is running
                                }
                            });

                            if (counter.get() == 0) {
                                sb.append("<tr><td><h4>");  // NOI18N
                                sb.append(NbBundle.getMessage(OQLController.class, "OQL_TOO_MANY_RESULTS_MSG"));      // NOI18N
                                sb.append("</h4></td></tr>");   // NOI18N
                            } else if (counter.get() == RESULTS_LIMIT) {
                                sb.append("<tr><td><h4>"); // NOI18N
                                sb.append(NbBundle.getMessage(OQLController.class, "OQL_NO_RESULTS_MSG")); // NOI18N
                                sb.append("</h4></td></tr>" ); // NOI18N
                            }

                            sb.append("</table>"); // NOI18N

                            resultsController.setResult(sb.toString());
                            finalizeQuery();
                        } catch (OQLException oQLException) {
                            StringBuilder errorMessage = new StringBuilder();
                            errorMessage.append("<h2>").append(NbBundle.getMessage(OQLController.class, "OQL_QUERY_ERROR")).append("</h2>"); // NOI18N
                            errorMessage.append(NbBundle.getMessage(OQLController.class, "OQL_QUERY_PLZ_CHECK")); // NOI18N
                            errorMessage.append("<hr>"); // noi18n
                            errorMessage.append(oQLException.getLocalizedMessage().replace("\n", "<br>").replace("\r", "<br>"));
                            resultsController.setResult(errorMessage.toString());
                            finalizeQuery();
                        }
                    }

                });
            }
        });
    }

    private void finalizeQuery() {
        analysisRunning.compareAndSet(true, false);
        queryController.queryFinished();
    }

    private void dump(Object o, StringBuilder sb) {
        if (o == null) {
            return;
        }
        if (o instanceof Instance) {
            Instance i = (Instance) o;
            sb.append(printInstance(i, heapFragmentWalker.getHeapFragment()));
        } else if (o instanceof JavaClass) {
            JavaClass c = (JavaClass)o;
            sb.append(printClass(c));
        } else if (o instanceof ReferenceChain) {
            ReferenceChain rc = (ReferenceChain) o;
            boolean first = true;
            while (rc != null) {
                if (!first) {
                    sb.append("-&gt;"); // NOI18N
                } else {
                    first = false;
                }
                o = rc.getObj();
                if (o instanceof Instance) {
                    sb.append(printInstance((Instance)o, heapFragmentWalker.getHeapFragment()));
                } else if (o instanceof JavaClass) {
                    sb.append(printClass((JavaClass)o));
                }
                rc = rc.getNext();
            }
        } else if (o instanceof Map) {
            Set<Map.Entry> entries = ((Map)o).<Map.Entry>entrySet();
            sb.append("<span><b>{</b><br/>"); // NOI18N
            boolean first = true;
            for(Map.Entry entry : entries) {
                if (!first) {
                    sb.append(",<br/>"); // NOI18N
                } else {
                    first = false;
                }
                sb.append(entry.getKey().toString().replace("<", "&lt;").replace(">", "&gt;")); // NOI18N
                sb.append(" = "); // NOI18N
                dump(unwrap(entry.getValue()), sb);
            }
            sb.append("<br/><b>}</b></span>"); // NOI18N
        } else if (o instanceof Object[]) {
            sb.append("<span><b>[</b>&nbsp;"); // NOI18N
            boolean first = true;
            for (Object obj1 : (Object[]) o) {
                if (!first) {
                    sb.append(", "); // NOI18N
                } else {
                    first = false;
                }
                dump(unwrap(obj1), sb);
            }
            sb.append("&nbsp;<b>]</b></span>"); // NOI18N
        } else {
            sb.append(o.toString());
        }
    }

    private Object unwrap(Object obj1) {
        Object obj2 = engine.unwrapJavaObject(obj1, true);
        return obj2 != null ? obj2 : obj1;
    }

    private OQLEngine getEngine() {
        return engine;
    }

    private void showURL(URL url) {
        String urls = url.toString();

        if (urls.startsWith("file://instance/")) { // NOI18N
            urls = urls.substring("file://instance/".length()); // NOI18N

            int indexPos = urls.indexOf('#'); // NOI18N
            int pointerPos = urls.indexOf('@'); // NOI18N
            String clzName = null;

            if (indexPos > -1 || pointerPos > -1) {
                clzName = urls.substring(0, Math.max(indexPos, pointerPos));
            }

            Instance i = null;
            String identifier = null;
            if (indexPos > -1) {
                identifier = urls.substring(indexPos + 1);
                JavaClass c = heapFragmentWalker.getHeapFragment().getJavaClassByName(clzName);

                if (c != null) {
                    List<Instance> instances = c.getInstances();
                    int instanceNumber = Integer.parseInt(identifier);
                    if (instanceNumber <= instances.size()) {
                        i = instances.get(instanceNumber - 1);
                    }
                } else {
                    ProfilerDialogs.displayError(Bundle.AnalysisController_CannotResolveClassMsg(clzName));
                }
            } else if (pointerPos > -1) {
                identifier = urls.substring(pointerPos + 1);

                i = heapFragmentWalker.getHeapFragment().getInstanceByID(Long.parseLong(identifier));
            }

            if (i != null) {
                heapFragmentWalker.getClassesController().showInstance(i);
            } else {
                ProfilerDialogs.displayError(Bundle.AnalysisController_CannotResolveInstanceMsg(identifier, clzName));
            }
        } else if (urls.startsWith("file://class/")) { // NOI18N
            urls = urls.substring("file://class/".length()); // NOI18N

            JavaClass c = heapFragmentWalker.getHeapFragment().getJavaClassByID(Long.parseLong(urls));

            if (c != null) {
                heapFragmentWalker.getClassesController().showClass(c);
            } else {
                ProfilerDialogs.displayError(Bundle.AnalysisController_CannotResolveClassMsg(urls));
            }
        }
    }

    private static String printClass(JavaClass cls) {
        if (cls == null) {
            return NbBundle.getMessage(Utils.class, "LBL_UnknownClass"); // NOI18N
        }

        String clsName = cls.getName();
        String fullName = clsName; // NOI18N
        String field = ""; // NOI18N

        // now you can wrap it with a/href to given class
        int dotIdx = clsName.lastIndexOf('.'); // NOI18N
        int colonIdx = clsName.lastIndexOf(':'); // NOI18N

        if (colonIdx == -1) {
            colonIdx = clsName.lastIndexOf(';'); // NOI18N
        }

        if (colonIdx > 0) {
            fullName = clsName.substring(0, colonIdx);
            field = "." + clsName.substring(colonIdx + 1); // NOI18N
        }

//        String dispName = clsName.substring(dotIdx + 1);

        return "<a href='file://class/" + cls.getJavaClassId() + "'>" + fullName + "</a>" + field; // NOI18N
    }

    private static String printInstance(Instance in, Heap heap) {
        String className = in.getJavaClass().getName();
        String details = DetailsUtils.getInstanceString(in, heap);
        

        return "<a href='file://instance/" + className + "@" + in.getInstanceId() + "'>" + className + '#' + in.getInstanceNumber() + "</a>" + // NOI18N
                (details != null ? " - " + htmlize(details) : ""); // NOI18N
//        return "<a href='file://instance/" + className + "/" + in.getInstanceNumber() + "'>" + className + '#' + in.getInstanceNumber() + "</a>"; // NOI18N
//        return in.getJavaClass().getName() + '@' + Long.toHexString(in.getInstanceId()) + '#' + in.getInstanceNumber();
    }

    private static String htmlize(String value) {
            return value.replace(">", "&gt;").replace("<", "&lt;");     // NOI18N
    }


    // --- Controllers ---------------------------------------------------------

    public static class ResultsController extends AbstractController {

        private OQLController oqlController;


        public ResultsController(OQLController oqlController) {
            this.oqlController = oqlController;
        }

        public void setResult(final String result) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ((OQLControllerUI.ResultsUI)getPanel()).setResult(result);
                }
            });
        }

        public void showURL(URL url) {
            oqlController.showURL(url);
        }

        public OQLController getOQLController() {
            return oqlController;
        }

        protected AbstractButton createControllerPresenter() {
            return ((OQLControllerUI.ResultsUI)getPanel()).getPresenter();
        }

        protected JPanel createControllerUI() {
            return new OQLControllerUI.ResultsUI(this);
        }

    }

    
    public static class QueryController extends AbstractController {
        private OQLController oqlController;


        public QueryController(OQLController oqlController) {
            this.oqlController = oqlController;
        }


        public OQLController getOQLController() {
            return oqlController;
        }

        public void setQuery(String query) {
            ((OQLControllerUI.QueryUI)getPanel()).setQuery(query);
        }

        
        private void queryStarted(BoundedRangeModel model) {
            ((OQLControllerUI.QueryUI)getPanel()).queryStarted(model);
        }

        private void queryFinished() {
            ((OQLControllerUI.QueryUI)getPanel()).queryFinished();
        }

        
        protected AbstractButton createControllerPresenter() {
            return ((OQLControllerUI.QueryUI)getPanel()).getPresenter();
        }

        protected JPanel createControllerUI() {
            return new OQLControllerUI.QueryUI(this, oqlController.getEngine());
        }

    }


    public static class SavedController extends AbstractController {

        private OQLController oqlController;


        public SavedController(OQLController oqlController) {
            this.oqlController = oqlController;
        }


        public OQLController getOQLController() {
            return oqlController;
        }

        public void saveQuery(String query) {
            ((OQLControllerUI.SavedUI)getPanel()).saveQuery(query);
        }


        public static void loadData(OQLSupport.OQLTreeModel model) {
            OQLSupport.loadModel(model);
        }

        public static void saveData(OQLSupport.OQLTreeModel model) {
            OQLSupport.saveModel(model);
        }

        
        protected AbstractButton createControllerPresenter() {
            return ((OQLControllerUI.SavedUI)getPanel()).getPresenter();
        }

        protected JPanel createControllerUI() {
            JPanel ui = new OQLControllerUI.SavedUI(this);
            return ui;
        }

    }

    private class ProgressUpdater implements Runnable {

        private final BoundedRangeModel progressModel;

        ProgressUpdater(BoundedRangeModel model) {
            progressModel = model;
        }

        public void run() {
            while (analysisRunning.get()) {
                final int newVal;
                int val = progressModel.getValue() + 10;
                
                if (val > progressModel.getMaximum()) {
                    val = progressModel.getMinimum();
                }
                newVal = val;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        progressModel.setValue(newVal);
                    }
                });
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}

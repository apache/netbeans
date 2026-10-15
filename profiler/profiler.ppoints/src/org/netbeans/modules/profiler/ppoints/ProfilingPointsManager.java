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

package org.netbeans.modules.profiler.ppoints;

import java.util.Arrays;
import org.netbeans.lib.profiler.client.ProfilingPointsProcessor;
import org.netbeans.lib.profiler.client.RuntimeProfilingPoint;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.common.event.ProfilingStateEvent;
import org.netbeans.lib.profiler.common.event.ProfilingStateListener;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;
import org.netbeans.modules.profiler.ppoints.ui.ProfilingPointsWindow;
import org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel;
import org.netbeans.modules.profiler.ppoints.ui.ValidityListener;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.lib.profiler.common.CommonUtils;
import org.netbeans.lib.profiler.TargetAppRunner;
import org.netbeans.lib.profiler.common.event.ProfilingStateAdapter;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.netbeans.modules.profiler.ppoints.ui.ProfilingPointReport;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileChangeListener;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;


/**
 *
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ProfilingPointsManager_AnotherPpEditedMsg=Another Profiling Point is currently being edited!",
    "ProfilingPointsManager_PpCustomizerCaption=Customize Profiling Point",
    "ProfilingPointsManager_CannotStorePpMsg=Cannot store {0} Profiling Points to {1}",
    "ProfilingPointsManager_ShowingFirstNItemsTxt=Showing first {0} items",
    "ProfilingPointsManager_OkButtonText=OK"
})
@ServiceProvider(service=ProfilingPointsProcessor.class)
public final class ProfilingPointsManager extends ProfilingPointsProcessor 
                                          implements ChangeListener,
                                                     PropertyChangeListener {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class ProfilingPointsComparator implements Comparator {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private boolean sortOrder;
        private int sortBy;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public ProfilingPointsComparator(int sortBy, boolean sortOrder) {
            this.sortBy = sortBy;
            this.sortOrder = sortOrder;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public int compare(Object o1, Object o2) {
            ProfilingPoint pp1 = sortOrder ? (ProfilingPoint) o1 : (ProfilingPoint) o2;
            ProfilingPoint pp2 = sortOrder ? (ProfilingPoint) o2 : (ProfilingPoint) o1;

            switch (sortBy) {
//                case CommonConstants.SORTING_COLUMN_DEFAULT:
//                case SORT_BY_PROJECT:
//                    return ProjectUtilities.getDisplayName(pp1.getProject())
//                                       .compareTo(ProjectUtilities.getDisplayName(pp2.getProject()));
                case SORT_BY_SCOPE:

                    int v1 = pp1.getFactory().getScope();
                    int v2 = pp2.getFactory().getScope();

                    return ((v1 < v2) ? (-1) : ((v1 == v2) ? 0 : 1));
                case SORT_BY_NAME:
                    return pp1.getName().compareTo(pp2.getName());
                case SORT_BY_RESULTS:
                    return pp1.getResultsText().compareTo(pp2.getResultsText());
                default:
                    return ProjectUtilities.getDisplayName(pp1.getProject())
                                       .compareTo(ProjectUtilities.getDisplayName(pp2.getProject()));
//                    throw new RuntimeException("Unsupported compare operation for " + o1 + ", " + o2); // NOI18N
            }
        }
    }

    private static class CustomizerButton extends JButton implements ValidityListener {
        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public CustomizerButton() {
            super(Bundle.ProfilingPointsManager_OkButtonText());
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void validityChanged(boolean isValid) {
            setEnabled(isValid);
        }
    }

    private class CustomizerListener extends WindowAdapter {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private Dialog d;
        private DialogDescriptor dd;
        private Runnable updater;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public CustomizerListener(Dialog d, DialogDescriptor dd, Runnable updater) {
            super();
            this.d = d;
            this.dd = dd;
            this.updater = updater;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void windowClosed(WindowEvent e) {
            if (dd.getValue() == getCustomizerButton()) {
                updater.run();
            }

            d.removeWindowListener(this);
            d = null;
            dd = null;
            updater = null;
        }

        public void windowOpened(WindowEvent e) {
            d.requestFocus();
        }
    }

    private static class RuntimeProfilingPointMapper {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private final CodeProfilingPoint owner;
        private final int index;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public RuntimeProfilingPointMapper(CodeProfilingPoint owner, int index) {
            this.owner = owner;
            this.index = index;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public int getIndex() {
            return index;
        }

        public CodeProfilingPoint getOwner() {
            return owner;
        }
    }
    
    private static class FileWatch {
        private int references = 0;
        private LocationFileListener listener;
        
        public FileWatch(LocationFileListener listener) { this.listener = listener; }
        
        public boolean hasReferences() { return references > 0; }
        public LocationFileListener getListener() { return listener; }
        
        public void increaseReferences() { references++; }
        public void decreaseReferences() { references--; }
    }
    
    private class LocationFileListener implements FileChangeListener {
        
        private File file;
        
        public LocationFileListener(File file) { this.file = file; }

        public void fileDeleted(final FileEvent fe) {
            Runnable worker = new Runnable() {
                public void run() { deleteProfilingPointsForFile(file); }
            };
            
            if (SwingUtilities.isEventDispatchThread()) {
                processor().post(worker);
            } else {
                worker.run();
            }
        }

        public void fileRenamed(final FileRenameEvent fe) {
            Runnable worker = new Runnable() {
                public void run() { 
                    FileObject renamedFileO = fe.getFile();
                    File renamedFile = FileUtil.toFile(renamedFileO);
                    if (renamedFile != null && renamedFile.exists() && renamedFile.isFile()) {
                        updateProfilingPointsFile(file, renamedFile);
                    } else {
                        deleteProfilingPointsForFile(file);
                    }
                }
            };
            
            if (SwingUtilities.isEventDispatchThread()) {
                processor().post(worker);
            } else {
                worker.run();
            }
        }
        
        public void fileFolderCreated(FileEvent fe) {}
        public void fileDataCreated(FileEvent fe) {}
        public void fileChanged(FileEvent fe) {}
        public void fileAttributeChanged(FileAttributeEvent fe) {}
        
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------
    
    private static final int MAX_HITS = Integer.getInteger("nbprofiler.ppoints.maxhits", 1000); // NOI18N

    public static final String PROPERTY_PROJECTS_CHANGED = "p_projects_changed"; // NOI18N
    public static final String PROPERTY_PROFILING_POINTS_CHANGED = "p_profiling_points_changed"; // NOI18N
    public static final int SORT_BY_PROJECT = 1;
    public static final int SORT_BY_SCOPE = 2;
    public static final int SORT_BY_NAME = 3;
    public static final int SORT_BY_RESULTS = 4;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private CustomizerButton customizerButton;
    private List<GlobalProfilingPoint> activeGlobalProfilingPoints = new ArrayList<>();
    private Map<Integer, RuntimeProfilingPointMapper> activeCodeProfilingPoints = new HashMap<>();
    private PropertyChangeListener pcl = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(ProfilingPointFactory.AVAILABILITY_PROPERTY)) {
                refreshProfilingPointFactories();
                firePropertyChanged(PROPERTY_PROFILING_POINTS_CHANGED); // notify the profiling points list displayer about the change
            }
        }
    };

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private Set<ProfilingPoint> dirtyProfilingPoints = Collections.synchronizedSet(new HashSet<>());
    private List<ValidityAwarePanel> customizers = new ArrayList<>();
    private final Collection<Lookup.Provider> openedProjects = new ArrayList<>();
    private List<ProfilingPoint> profilingPoints = new ArrayList<>();
    private ProfilingPointFactory[] profilingPointFactories = new ProfilingPointFactory[0];
    private boolean profilingInProgress = false; // collecting data
    private boolean profilingSessionInProgress = false; // session started and not yet finished
    private int nextUniqueRPPIdentificator;
    
    private Map<File, FileWatch> profilingPointsFiles = new HashMap<File, FileWatch>();
    private boolean ignoreStoreProfilingPoints = false;
    
    private boolean processesProfilingPoints;
    private final Object pointsLock = new Object();
    // @GuardedBy pointsLock
    private RuntimeProfilingPoint[] points = null;
    
    private WeakReference<RequestProcessor> _processorRef;
    private final Object _processorLock = new Object();
    
    private static boolean hasInstance;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ProfilingPointsManager() {
        hasInstance = true;
        refreshProfilingPointFactories();
        final ProfilingStateListener listener = new ProfilingStateAdapter() {
            public void profilingStateChanged(final ProfilingStateEvent profilingStateEvent) {
                processor().post(new Runnable() {
                    public void run() {
                        boolean wasProfilingInProgress = profilingInProgress;
                        boolean wasProfilingSessionInProgres = profilingSessionInProgress;

                        synchronized (ProfilingPointsManager.this) {
                            switch (profilingStateEvent.getNewState()) {
                                case Profiler.PROFILING_INACTIVE:
                                case Profiler.PROFILING_STOPPED:
                                    profilingInProgress = false;
                                    profilingSessionInProgress = false;

                                    break;
                                case Profiler.PROFILING_STARTED:
                                case Profiler.PROFILING_IN_TRANSITION:
                                    profilingInProgress = false;
                                    profilingSessionInProgress = true;

                                    break;
                                default:
                                    profilingInProgress = true;
                                    profilingSessionInProgress = true;
                            }
                        }

                        if ((wasProfilingInProgress != profilingInProgress) || (wasProfilingSessionInProgres != profilingSessionInProgress)) {
                            GlobalProfilingPointsProcessor.getDefault().notifyProfilingStateChanged();
                            CommonUtils.runInEventDispatchThread(new Runnable() {
                                public void run() {
                                    if (ProfilingPointsWindow.hasDefault())
                                        ProfilingPointsWindow.getDefault().notifyProfilingStateChanged(); // this needs to be called on EDT
                                    ProfilingPointReport.refreshOpenReports();
                                }
                            });
                        }
                    }
                });
            }
        };
        processor().post(new Runnable() {
                public void run() {
                    ProjectUtilities.addOpenProjectsListener(ProfilingPointsManager.this);
                    processOpenedProjectsChanged(); // will subsequently invoke projectOpened on all open projects
                    Profiler.getDefault().addProfilingStateListener(listener);
                }
            });
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    static boolean hasDefault() {
        return hasInstance;
    }
    
    public static ProfilingPointsManager getDefault() {
        return Lookup.getDefault().lookup(ProfilingPointsManager.class);
    }

    public List<ProfilingPoint> getCompatibleProfilingPoints(Lookup.Provider project, ProfilingSettings profilingSettings, boolean sorted) {
 
        List<ProfilingPoint> projectProfilingPoints = sorted ? getSortedProfilingPoints(project, 1, false)
                                                             : getProfilingPoints(project, ProfilerIDESettings.
                                                               getInstance().getIncludeProfilingPointsDependencies(), false); // TODO: define default sorting (current sorting of Profiling Points window?)
        List<ProfilingPoint> compatibleProfilingPoints = new ArrayList<>();

        for (ProfilingPoint profilingPoint : projectProfilingPoints) {
            if (profilingPoint.supportsProfilingSettings(profilingSettings)) {
                compatibleProfilingPoints.add(profilingPoint);
            }
        }

        return compatibleProfilingPoints;
    }

    // Currently profiling, data are being collected
    public boolean isProfilingInProgress() {
        synchronized (this) {
            return profilingInProgress;
        }
    }

    public ProfilingPointFactory[] getProfilingPointFactories() {
        return profilingPointFactories;
    }

    public List<ProfilingPoint> getProfilingPoints(Lookup.Provider project,
                                                   boolean inclSubprojects,
                                                   boolean inclUnavailable) {
        return getProfilingPoints(ProfilingPoint.class, project,
                                  inclSubprojects, inclUnavailable);
    }

    public <T extends ProfilingPoint> List<T> getProfilingPoints(Class<T> ppClass,
                                                                 Lookup.Provider project,
                                                                 boolean inclSubprojects) {
        return getProfilingPoints(ppClass, project, inclSubprojects, true);
    }

    public <T extends ProfilingPoint> List<T> getProfilingPoints(Class<T> ppClass,
                                                                 Lookup.Provider project,
                                                                 boolean inclSubprojects,
                                                                 boolean inclUnavailable) {
        Set<Lookup.Provider> projects = new HashSet<>();

        if (project == null) {
            synchronized (openedProjects) { projects.addAll(openedProjects); }
        } else {
            projects.add(project);
            if (inclSubprojects) projects.addAll(getOpenSubprojects(project));
        }        

        ArrayList<T> filteredProfilingPoints = new ArrayList<>();
        Iterator<ProfilingPoint> iterator = profilingPoints.iterator();
        
        Set<FileObject> projectsLoc = locations(projects);
        while (iterator.hasNext()) {
            ProfilingPoint profilingPoint = iterator.next();
            ProfilingPointFactory factory = profilingPoint.getFactory();

            // Bugfix #162132, the factory may already be unloaded
            if (factory != null) {
                if (ppClass.isInstance(profilingPoint) && (inclUnavailable || factory.isAvailable())) {
                    Lookup.Provider ppProject = profilingPoint.getProject();
                    if (matchesScope(ppProject, project, projects) ||
                            containsProject(projectsLoc, ppProject))
                        filteredProfilingPoints.add((T) profilingPoint);
                }
            } else {
                // TODO: profiling points without factories should be cleaned up somehow
            }
        }

        return filteredProfilingPoints;
    }

    // Profiling session started and not yet finished
    public boolean isProfilingSessionInProgress() {
        synchronized (this) {
            return profilingSessionInProgress;
        }
    }

    public List<ProfilingPoint> getSortedProfilingPoints(Lookup.Provider project, int sortBy, boolean sortOrder) {
        List<ProfilingPoint> sortedProfilingPoints = getProfilingPoints(project, ProfilerIDESettings.getInstance().
                                                                        getIncludeProfilingPointsDependencies(), false);
        sortedProfilingPoints.sort(new ProfilingPointsComparator(sortBy, sortOrder));

        return sortedProfilingPoints;
    }

    public void addProfilingPoint(ProfilingPoint profilingPoint) {
        addProfilingPoints(new ProfilingPoint[] { profilingPoint });
    }

    public void addProfilingPoints(ProfilingPoint[] profilingPointsArr) {
        addProfilingPoints(profilingPointsArr, false);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    // TODO: should optionally support also subprojects/project references
    public RuntimeProfilingPoint[] createCodeProfilingConfiguration(Lookup.Provider project, ProfilingSettings profilingSettings) {
        
        checkProfilingPoints(); // NOTE: Probably not neccessary but we need to be sure here
        
        nextUniqueRPPIdentificator = 0;

        List<RuntimeProfilingPoint> runtimeProfilingPoints = new ArrayList<>();
        List<ProfilingPoint> compatibleProfilingPoints = getCompatibleProfilingPoints(project, profilingSettings, false);

        for (ProfilingPoint compatibleProfilingPoint : compatibleProfilingPoints) {
            if (compatibleProfilingPoint.isEnabled() && compatibleProfilingPoint instanceof CodeProfilingPoint) {
                CodeProfilingPoint compatibleCodeProfilingPoint = (CodeProfilingPoint) compatibleProfilingPoint;
                RuntimeProfilingPoint[] rpps = compatibleCodeProfilingPoint.createRuntimeProfilingPoints();
                
                if (rpps.length == 0) ErrorManager.getDefault().log(ErrorManager.USER, "Cannot resolve RuntimeProfilingPoint(s) for " + compatibleCodeProfilingPoint.getName() + ", check location"); // NOI18N

                for (int i = 0; i < rpps.length; i++) {
                    runtimeProfilingPoints.add(rpps[i]);
                    activeCodeProfilingPoints.put(rpps[i].getId(),
                                                  new RuntimeProfilingPointMapper(compatibleCodeProfilingPoint, i)); // Note that profiled project may be closed but it's active Profiling Points are still referenced by this map => will be processed
                }
            }
        }

        return runtimeProfilingPoints.toArray(new RuntimeProfilingPoint[0]);
    }

    // TODO: should optionally support also subprojects/project references
    public GlobalProfilingPoint[] createGlobalProfilingConfiguration(Lookup.Provider project, ProfilingSettings profilingSettings) {
        
        checkProfilingPoints(); // NOTE: Probably not neccessary but we need to be sure here
        
        List<ProfilingPoint> compatibleProfilingPoints = getCompatibleProfilingPoints(project, profilingSettings, false);

        for (ProfilingPoint compatibleProfilingPoint : compatibleProfilingPoints) {
            if (compatibleProfilingPoint.isEnabled() && compatibleProfilingPoint instanceof GlobalProfilingPoint) {
                activeGlobalProfilingPoints.add((GlobalProfilingPoint) compatibleProfilingPoint);
            }
        }

        return activeGlobalProfilingPoints.toArray(new GlobalProfilingPoint[0]);
    }

    public synchronized int createUniqueRuntimeProfilingPointIdentificator() {
        return nextUniqueRPPIdentificator++;
    }

    public void firePropertyChanged(String property) {
        propertyChangeSupport.firePropertyChange(property, false, true);
    }

    public void ideClosing() {
        // TODO: dirty profiling points should be persisted on document save!
        processor().post(new Runnable() {
            public void run() {
                storeDirtyProfilingPoints();
            }
        });
    }

    public void profilingPointHit(RuntimeProfilingPoint.HitEvent hitEvent) {
        RuntimeProfilingPointMapper mapper = activeCodeProfilingPoints.get(hitEvent.getId());

        if (mapper != null) {
            mapper.getOwner().hit(hitEvent, mapper.getIndex());
        } else {
            ErrorManager.getDefault().log(ErrorManager.ERROR, "Cannot resolve ProfilingPoint for event: " + hitEvent); // NOI18N
        }
    }

    @Override
    public void init(Object project) {
        reset();
        
        ProfilingSettings ps = Profiler.getDefault().getLastProfilingSettings();
        TargetAppRunner tar = Profiler.getDefault().getTargetAppRunner();
        
        if (ps.useProfilingPoints() && (project != null)) {
            synchronized(pointsLock) {
                points = createCodeProfilingConfiguration((Lookup.Provider)project, ps);
                processesProfilingPoints = points.length > 0;
                tar.getProfilerEngineSettings().setRuntimeProfilingPoints(points);
            }
            //      targetAppRunner.getProfilingSessionStatus().startProfilingPointsActive = profilingSettings.useProfilingPoints();
        } else {
            synchronized(pointsLock) {
                points = new RuntimeProfilingPoint[0];
            }
            processesProfilingPoints = false;
            tar.getProfilerEngineSettings().setRuntimeProfilingPoints(points);
        }

        // TODO: should be moved to openWindowsOnProfilingStart()
        if (processesProfilingPoints) {
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (!ProfilingPointsWindow.getDefault().isOpened()) {
                            ProfilingPointsWindow.getDefault().open();
                            ProfilingPointsWindow.getDefault().requestVisible();
                        }
                    }
                });
        }
    }

    @Override
    public RuntimeProfilingPoint[] getSupportedProfilingPoints() {
        return points != null ? points : new RuntimeProfilingPoint[0];
    }
    
    // Required for JDev implementation
    public void updateLocation(CodeProfilingPoint cpp, int oldLine, int newLine) {
        if (cpp instanceof CodeProfilingPoint.Single) {
            CodeProfilingPoint.Single cpps = (CodeProfilingPoint.Single)cpp;
            CodeProfilingPoint.Location loc = cpps.getLocation();
            if (loc.getLine() == oldLine)
                updateLocation(cpps, newLine, cpps.getAnnotation());
                
        } else if (cpp instanceof CodeProfilingPoint.Paired) {
            CodeProfilingPoint.Paired cppp = (CodeProfilingPoint.Paired)cpp;
            CodeProfilingPoint.Annotation ann = null;
            CodeProfilingPoint.Location loc = cppp.getStartLocation();
            if (loc.getLine() == oldLine) {
                ann = cppp.getStartAnnotation();
            } else {
                loc = cppp.getEndLocation();
                if (loc.getLine() == oldLine)
                    ann = cppp.getEndAnnotation();
            }
            if (ann != null) updateLocation(cppp, newLine, ann);
        }
    }
    
    private void updateLocation(CodeProfilingPoint cpp, int line, CodeProfilingPoint.Annotation cppa) {
        cpp.internalUpdateLocation(cppa, line);
        dirtyProfilingPoints.add(cpp);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof Line && Line.PROP_LINE_NUMBER.equals(evt.getPropertyName())) {
            final Line line = (Line) evt.getSource();
            processor().post(new Runnable() {
                    public void run() {
                        for (ProfilingPoint pp : profilingPoints) {
                            if (pp instanceof CodeProfilingPoint) {
                                CodeProfilingPoint cpp = (CodeProfilingPoint) pp;

                                for (CodeProfilingPoint.Annotation cppa : cpp.getAnnotations()) {
                                    if (line.equals(cppa.getAttachedAnnotatable())) {
                                        cpp.internalUpdateLocation(cppa, line.getLineNumber() + 1); // Line is 0-based, needs to be 1-based for CodeProfilingPoint.Location
                                    }
                                }

                                dirtyProfilingPoints.add(cpp);
                            }
                        }
                    }
                });
        } else if (evt.getSource() instanceof ProfilingPoint) {
            ProfilingPoint profilingPoint = (ProfilingPoint) evt.getSource();
            if (!evt.getPropertyName().equals(ProfilingPoint.PROPERTY_RESULTS))
                storeProfilingPoints(new ProfilingPoint[] { profilingPoint });

            if (isAnnotationChange(evt)) {
                ProfilingPointAnnotator.get().annotationChanged(evt);
            }

            if (isLocationChange(evt)) {
                ProfilingPointAnnotator.get().locationChanged(evt);
                
                CodeProfilingPoint.Location oldLocation = (CodeProfilingPoint.Location)evt.getOldValue();
                if (oldLocation != null && !CodeProfilingPoint.Location.EMPTY.equals(oldLocation))
                    removeFileWatch(new File(oldLocation.getFile()));
                
                CodeProfilingPoint.Location newLocation = (CodeProfilingPoint.Location)evt.getNewValue();
                if (newLocation != null && !CodeProfilingPoint.Location.EMPTY.equals(newLocation))
                    addFileWatch(new File(newLocation.getFile()));
            }
                
            if (isAppearanceChange(evt)) {
                ProfilingPointAnnotator.get().appearanceChanged(evt);
                firePropertyChanged(PROPERTY_PROFILING_POINTS_CHANGED);
            }
//        } else if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
//            processor().post(new Runnable() {
//                    public void run() {
//                        processOpenedProjectsChanged();
//                    }
//                });

            // --- Code for saving dirty profiling points on document save instead of IDE closing ----------------
            //    } else if (DataObject.PROP_MODIFIED.equals(evt.getPropertyName())) {
            //      System.err.println(">>> Changed " + evt.getPropertyName() + " from " + evt.getOldValue() + " to " + evt.getNewValue() + ", origin: "+ evt.getSource());
            // ---------------------------------------------------------------------------------------------------
        }
    }
    
    public void stateChanged(ChangeEvent e) {
        processor().post(new Runnable() {
            public void run() {
                processOpenedProjectsChanged();
            }
        });
    }

    public void removeProfilingPoint(ProfilingPoint profilingPoint) {
        removeProfilingPoints(new ProfilingPoint[] { profilingPoint });
    }

    public synchronized void removeProfilingPoints(ProfilingPoint[] profilingPointsArr) {
        removeProfilingPoints(profilingPointsArr, false);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void reset() {
        // TODO: currently only last used profiling points are reset, check if all profiling points need to be reset
        List<ProfilingPoint> profilingPointsToReset = new ArrayList<>();

        // reset CodeProfilingPoints
        Collection<RuntimeProfilingPointMapper> mappersToReset = activeCodeProfilingPoints.values();

        for (RuntimeProfilingPointMapper mapper : mappersToReset) {
            profilingPointsToReset.add(mapper.getOwner());
        }

        activeCodeProfilingPoints.clear();

        // reset GlobalProfilingPoints
        profilingPointsToReset.addAll(activeGlobalProfilingPoints);
        activeGlobalProfilingPoints.clear();

        for (ProfilingPoint ppoint : profilingPointsToReset) {
            ppoint.reset();
        }

        profilingPointsToReset.clear();
    }

    public void timeAdjust(final int threadId, final long timeDiff0, final long timeDiff1) {
        Iterator<RuntimeProfilingPointMapper> it = activeCodeProfilingPoints.values().iterator();
        Set uniqueSet = new HashSet();

        while (it.hasNext()) {
            CodeProfilingPoint cpp = it.next().getOwner();

            if (cpp instanceof CodeProfilingPoint.Paired) {
                if (uniqueSet.add(cpp)) {
                    ((CodeProfilingPoint.Paired) cpp).timeAdjust(threadId, timeDiff0, timeDiff1);
                }
            }
        }
    }
    
    public boolean belowMaxHits(int hitsCount) {
        return hitsCount < MAX_HITS;
    }
    
    public String getTruncatedResultsText() {
        return "<br>&nbsp;" + Bundle.ProfilingPointsManager_ShowingFirstNItemsTxt(MAX_HITS); // NOI18N
    }

    boolean isAnyCustomizerShowing() {
        return getShowingCustomizer() != null;
    }

    ValidityAwarePanel getShowingCustomizer() {
        Iterator<ValidityAwarePanel> iterator = customizers.iterator();

        while (iterator.hasNext()) {
            ValidityAwarePanel vaPanel = iterator.next();

            if (vaPanel.isShowing()) {
                return vaPanel;
            }
        }

        return null;
    }

    // Returns true if customizer was opened and then submitted by OK button
    boolean customize(final ValidityAwarePanel customizer, Runnable updater, boolean focusToEditor) {
        ValidityAwarePanel showingCustomizer = getShowingCustomizer();

        if (showingCustomizer != null) {
            ProfilerDialogs.displayWarning(
                    Bundle.ProfilingPointsManager_AnotherPpEditedMsg());
            SwingUtilities.getWindowAncestor(showingCustomizer).requestFocus();
            showingCustomizer.requestFocusInWindow();
        } else {
            CustomizerButton cb = getCustomizerButton();
            customizer.addValidityListener(cb);
            cb.setEnabled(customizer.areSettingsValid()); // In fact customizer should be valid but just to be sure...

            JPanel customizerContainer = new JPanel(new BorderLayout());
            JPanel customizerSpacer = new JPanel(new BorderLayout());
            customizerSpacer.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
            customizerSpacer.add(customizer, BorderLayout.CENTER);
            customizerContainer.add(customizerSpacer, BorderLayout.CENTER);
            customizerContainer.add(new JSeparator(), BorderLayout.SOUTH);

            HelpCtx helpCtx = null;

            if (customizer instanceof HelpCtx.Provider) {
                helpCtx = ((HelpCtx.Provider) customizer).getHelpCtx();
            }

            DialogDescriptor dd = new DialogDescriptor(customizerContainer, Bundle.ProfilingPointsManager_PpCustomizerCaption(), false,
                                                       new Object[] { cb, DialogDescriptor.CANCEL_OPTION },
                                                       cb, 0, helpCtx, null);
            final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
            d.addWindowListener(new CustomizerListener(d, dd, updater));
            d.setModal(true);
            // give focus to the initial focus target
            d.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (customizer.getInitialFocusTarget() != null) {
                        customizer.getInitialFocusTarget().requestFocusInWindow();
                    }
                }
            });
            
            if (focusToEditor) {
                Dimension dim = d.getPreferredSize();
                Component masterComponent = WindowManager.getDefault().getRegistry().getActivated();
                if (masterComponent != null) {
                    Rectangle b = masterComponent.getBounds();
                    Point location = new Point((b.x + (b.width / 2)) - (dim.width / 2),
                                               (b.y + (b.height / 2)) - (dim.height / 2));
                    SwingUtilities.convertPointToScreen(location, masterComponent);
                    d.setLocation(location);
                }
            }
            
            d.setVisible(true);
            
            if (dd.getValue() == cb) {
                return true;
            }
        }
        return false;
    }

    void documentOpened(Line.Set lineSet, FileObject fileObject) {
        for (ProfilingPoint profilingPoint : profilingPoints) {
            if (profilingPoint instanceof CodeProfilingPoint) {
                CodeProfilingPoint cpp = (CodeProfilingPoint) profilingPoint;

                for (CodeProfilingPoint.Annotation cppa : cpp.getAnnotations()) {
                    File annotationFile = new File(cpp.getLocation(cppa).getFile());

                    if ((annotationFile == null) || (fileObject == null)) {
                        continue; // see #98535
                    }

                    File adeptFile = FileUtil.toFile(fileObject);

                    if (adeptFile == null) {
                        continue; // see #98535
                    }

                    if (adeptFile.equals(annotationFile)) {
                        deannotateProfilingPoint(cpp);
                        annotateProfilingPoint(cpp);

                        break;
                    }
                }
            }
        }
    }

    ValidityAwarePanel safeGetCustomizer(ValidityAwarePanel customizer) {
        if (!customizers.contains(customizer)) {
            customizers.add(customizer);
        }

        return isAnyCustomizerShowing() ? null : customizer;
    }

    private boolean isAnnotationChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();

        return propertyName.equals(CodeProfilingPoint.PROPERTY_ANNOTATION);
    }
    
    private boolean isLocationChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();

        return propertyName.equals(CodeProfilingPoint.PROPERTY_LOCATION);
    }
    
    private boolean isAppearanceChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();

        return propertyName.equals(ProfilingPoint.PROPERTY_NAME) || propertyName.equals(ProfilingPoint.PROPERTY_ENABLED)
               || propertyName.equals(ProfilingPoint.PROPERTY_PROJECT) || propertyName.equals(ProfilingPoint.PROPERTY_RESULTS);
    }
    
    private static Set<FileObject> locations(Collection<Lookup.Provider> projects) {
        if (projects == null || projects.isEmpty()) return Collections.EMPTY_SET;
        Set<FileObject> locations = new HashSet<>();
        for (Lookup.Provider project : projects)
            locations.add(ProjectUtilities.getProjectDirectory(project));
        return locations;
    }

    private static boolean containsProject(Set<FileObject> locations, Lookup.Provider p) {
        if (p != null && !locations.isEmpty()) {
            FileObject projectDir = ProjectUtilities.getProjectDirectory(p);
            return locations.contains(projectDir);
        }
        return false;
    }

    private Set<Lookup.Provider> getOpenSubprojects(Lookup.Provider project) {
        Set<Lookup.Provider> subprojects = new HashSet<>();
        ProjectUtilities.fetchSubprojects(project, subprojects);

        if (subprojects.isEmpty()) return subprojects;

        Set<Lookup.Provider> openSubprojects = new HashSet<>();
        Set<FileObject> subprojectsLoc = locations(subprojects);
        synchronized(openedProjects) {
            for (Lookup.Provider openProject : openedProjects)
                if (containsProject(subprojectsLoc, openProject))
                    openSubprojects.add(openProject);
        }

        return openSubprojects;
    }
    
    // Returns only valid profiling points (currently all GlobalProfilingPoints and CodeProfilingPoints with all locations pointing to a valid java file)
    private ProfilingPoint[] getValidProfilingPoints(ProfilingPoint[] profilingPointsArr) {
        ArrayList<ProfilingPoint> validProfilingPoints = new ArrayList<ProfilingPoint>();
        for (ProfilingPoint profilingPoint : profilingPointsArr) if(profilingPoint.isValid()) validProfilingPoints.add(profilingPoint);
        return validProfilingPoints.toArray(new ProfilingPoint[0]);
    }
    
    // Returns only invalid profiling points (currently CodeProfilingPoints with any of the locations pointing to an invalid file)
    private ProfilingPoint[] getInvalidProfilingPoints(ProfilingPoint[] profilingPointsArr) {
        ArrayList<ProfilingPoint> invalidProfilingPoints = new ArrayList<ProfilingPoint>();
        for (ProfilingPoint profilingPoint : profilingPointsArr) if(!profilingPoint.isValid()) invalidProfilingPoints.add(profilingPoint);
        return invalidProfilingPoints.toArray(new ProfilingPoint[0]);
    }
    
    // Checks if currently loaded profiling points are valid, invalid profiling points are silently deleted
    private void checkProfilingPoints() {
        ProfilingPoint[] invalidProfilingPoints = getInvalidProfilingPoints(profilingPoints.toArray(new ProfilingPoint[0]));
        if (invalidProfilingPoints.length > 0) removeProfilingPoints(invalidProfilingPoints);
    }
    
    private void addFileWatch(File file) {
        FileObject fileo = null;
        if (file.isFile())
            fileo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (fileo != null) {
            FileWatch fileWatch = profilingPointsFiles.get(file);
            if (fileWatch == null) {
                LocationFileListener listener = new LocationFileListener(file);
                fileWatch = new FileWatch(listener);
                fileo.addFileChangeListener(listener);
                profilingPointsFiles.put(file, fileWatch);
            }
            fileWatch.increaseReferences();
        }
    }
    
    private void removeFileWatch(File file) {
        FileObject fileo = null;
        if (file.isFile())
            fileo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (fileo != null) {
            FileWatch fileWatch = profilingPointsFiles.get(file);
            if (fileWatch != null) {
                fileWatch.decreaseReferences();
                if (!fileWatch.hasReferences()) fileo.removeFileChangeListener(profilingPointsFiles.remove(file).getListener());
            }
        } else {
            profilingPointsFiles.remove(file);
        }
    }
    
    private void addProfilingPointFileWatch(CodeProfilingPoint cpp) {
        CodeProfilingPoint.Annotation[] annotations = cpp.getAnnotations();
        for (CodeProfilingPoint.Annotation annotation : annotations) {
            CodeProfilingPoint.Location location = cpp.getLocation(annotation);
            String filename = location.getFile();
            addFileWatch(new File(filename));
        }
    }
    
    private void removeProfilingPointFileWatch(CodeProfilingPoint cpp) {
        CodeProfilingPoint.Annotation[] annotations = cpp.getAnnotations();
        for (CodeProfilingPoint.Annotation annotation : annotations) {
            CodeProfilingPoint.Location location = cpp.getLocation(annotation);
            String filename = location.getFile();
            removeFileWatch(new File(filename));
        }
    }
    
    private CodeProfilingPoint[] getProfilingPointsForFile(File file) {
        List<CodeProfilingPoint> profilingPointsForFile = new ArrayList<CodeProfilingPoint>();
        
        // TODO: could be optimized to search just within the owner Project
        for (ProfilingPoint profilingPoint : profilingPoints) {
            if (profilingPoint instanceof CodeProfilingPoint) {
                CodeProfilingPoint cpp = (CodeProfilingPoint)profilingPoint;
                for (CodeProfilingPoint.Annotation annotation : cpp.getAnnotations()) {
                    CodeProfilingPoint.Location location = cpp.getLocation(annotation);
                    File ppFile = new File(location.getFile());
                    if (file.equals(ppFile)) {
                        profilingPointsForFile.add(cpp);
                        break;
                    }
                }
            }
        }
        
        return profilingPointsForFile.toArray(new CodeProfilingPoint[0]);
    }
    
    private void deleteProfilingPointsForFile(File file) {
        removeProfilingPoints(getProfilingPointsForFile(file));
    }
    
    private void updateProfilingPointsFile(File oldFile, File newFile) {
        String newFilename = newFile.getAbsolutePath();
        CodeProfilingPoint[] cppa = getProfilingPointsForFile(oldFile);
        
        ignoreStoreProfilingPoints = true;
        
        for (CodeProfilingPoint cpp : cppa) {
            for (CodeProfilingPoint.Annotation annotation : cpp.getAnnotations()) {
                CodeProfilingPoint.Location location = cpp.getLocation(annotation);
                File ppFile = new File(location.getFile());
                if (oldFile.equals(ppFile)) {
                    CodeProfilingPoint.Location newLocation = new CodeProfilingPoint.Location(
                            newFilename, location.getLine(), location.getOffset());
                    cpp.setLocation(annotation, newLocation);
                }
            }
        }
        
        ignoreStoreProfilingPoints = false;
        storeProfilingPoints(cppa);
    }

    private synchronized void addProfilingPoints(ProfilingPoint[] profilingPointsArr, boolean internalChange) {
        
        profilingPointsArr = getValidProfilingPoints(profilingPointsArr);
        
        for (ProfilingPoint profilingPoint : profilingPointsArr) {
            profilingPoints.add(profilingPoint);
            profilingPoint.addPropertyChangeListener(this);

            if (profilingPoint instanceof CodeProfilingPoint) {
                CodeProfilingPoint cpp = (CodeProfilingPoint) profilingPoint;
                annotateProfilingPoint(cpp);
                addProfilingPointFileWatch(cpp);
            }
        }

        if (!internalChange) {
            storeProfilingPoints(profilingPointsArr);
            firePropertyChanged(PROPERTY_PROFILING_POINTS_CHANGED);
        }
    }

    private void annotateProfilingPoint(final CodeProfilingPoint profilingPoint) {
        ProfilingPointAnnotator.get().annotate(profilingPoint);
    }

    private void deannotateProfilingPoint(final CodeProfilingPoint profilingPoint) {
        ProfilingPointAnnotator.get().deannotate(profilingPoint);
    }

    private void loadProfilingPoints(Lookup.Provider project) {
        for (ProfilingPointFactory factory : profilingPointFactories) {
            try {
                ignoreStoreProfilingPoints = true;
                addProfilingPoints(factory.loadProfilingPoints(project), true);
            } catch (Exception e) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, e.getMessage());
            } finally {
                ignoreStoreProfilingPoints = false;
            }
        }
    }
    
    private final List<ProfilingPointScopeProvider> scopeProviders = new ArrayList<>();
    private final List<Lookup.Provider> providedScopes = new ArrayList<>();
    
    private void cacheProvidedScopes() {
        scopeProviders.clear();
        providedScopes.clear();
        
        Collection<? extends ProfilingPointScopeProvider> allScopeProviders =
                Lookup.getDefault().lookupAll(ProfilingPointScopeProvider.class);
        for (ProfilingPointScopeProvider scopeProvider : allScopeProviders) {
            Lookup.Provider providedScope = scopeProvider.getScope();
            if (providedScope != null) {
                scopeProviders.add(scopeProvider);
                providedScopes.add(providedScope);
            }
        }
    }
    
    public List<Lookup.Provider> getProvidedScopes() {
        return new ArrayList(providedScopes);
    }
    
    public boolean isDefaultScope(Lookup.Provider scope) {
        int index = providedScopes.indexOf(scope);
        return index == -1 ? false : scopeProviders.get(index).isDefaultScope();
    }
    
    private boolean matchesScope(Lookup.Provider scope, Lookup.Provider project,
                                     Set<Lookup.Provider> projects) {
        int index = providedScopes.indexOf(scope);
        if (index < 0) return false;
        return scopeProviders.get(index).matchesScope(project, projects);
    }

    private void processOpenedProjectsChanged() {
        Collection<Lookup.Provider> lastOpenedProjects = new ArrayList<>();
        List<Lookup.Provider> lastProvidedScopes = new ArrayList<>();
        synchronized (openedProjects) {
            lastOpenedProjects.addAll(openedProjects);
            openedProjects.clear();

            Lookup.Provider[] openProjects = ProjectUtilities.getOpenedProjects();
            openedProjects.addAll(Arrays.asList(openProjects));
            
            lastProvidedScopes.addAll(providedScopes);
            cacheProvidedScopes();

            Set<FileObject> openedProjectsLoc = locations(openedProjects);
            for (Lookup.Provider project : lastOpenedProjects) {
                if (!containsProject(openedProjectsLoc, project)) {
                    projectClosed(project);
                }
            }
            
            Set<FileObject> providedScopesLoc = locations(providedScopes);
            for (Lookup.Provider scope : lastProvidedScopes) {
                if (!containsProject(providedScopesLoc, scope)) {
                    projectClosed(scope);
                }
            }
            
            Set<FileObject> lastOpenedProjectsLoc = locations(lastOpenedProjects);
            for (Lookup.Provider openProject : openedProjects) {
                if (!containsProject(lastOpenedProjectsLoc, openProject)) {
                    projectOpened(openProject);
                }
            }
            
            Set<FileObject> lastProvidedScopesLoc = locations(lastProvidedScopes);
            for (Lookup.Provider providedScope : providedScopes) {
                if (!containsProject(lastProvidedScopesLoc, providedScope)) {
                    projectOpened(providedScope);
                }
            }
        }

        firePropertyChanged(PROPERTY_PROJECTS_CHANGED);
    }

    private void projectClosed(Lookup.Provider project) {
        unloadProfilingPoints(project);
    }

    private void projectOpened(Lookup.Provider project) {
        loadProfilingPoints(project);
    }

    private void refreshProfilingPointFactories() {
        Collection<?extends ProfilingPointFactory> factories = Lookup.getDefault().lookupAll(ProfilingPointFactory.class);
        Collection<ProfilingPointFactory> cleansedFactories = new ArrayList<ProfilingPointFactory>();

        for (ProfilingPointFactory factory : factories) {
            if (factory.isAvailable()) {
                cleansedFactories.add(factory);
            }

            factory.addPropertyChangeListener(ProfilingPointFactory.AVAILABILITY_PROPERTY,
                                              WeakListeners.propertyChange(pcl, factory));
        }

        profilingPointFactories = cleansedFactories.toArray(new ProfilingPointFactory[0]);
    }

    private synchronized void removeProfilingPoints(ProfilingPoint[] profilingPointsArr, boolean internalChange) {
        for (ProfilingPoint profilingPoint : profilingPointsArr) {
            if (profilingPoint instanceof CodeProfilingPoint) {
                CodeProfilingPoint cpp = (CodeProfilingPoint) profilingPoint;
                removeProfilingPointFileWatch(cpp);
                deannotateProfilingPoint(cpp);
            }

            profilingPoint.removePropertyChangeListener(this);
            profilingPoint.hideResults();
            profilingPoint.reset();
            profilingPoints.remove(profilingPoint);
        }

        if (!internalChange) {
            storeProfilingPoints(profilingPointsArr);
            firePropertyChanged(PROPERTY_PROFILING_POINTS_CHANGED);
        }
    }

    private void storeDirtyProfilingPoints() {
        if (dirtyProfilingPoints.isEmpty()) return;
        ProfilingPoint[] dirtyProfilingPointsArr = dirtyProfilingPoints.toArray(new ProfilingPoint[0]);
        storeProfilingPoints(dirtyProfilingPointsArr);
    }

    private synchronized void storeProfilingPoints(ProfilingPoint[] profilingPointsArr) {
        if (ignoreStoreProfilingPoints) return;
        
        final Set<Lookup.Provider> projects = new HashSet<>();
        final Set<ProfilingPointFactory> factories = new HashSet<>();

        for (ProfilingPoint profilingPoint : profilingPointsArr) {
            projects.add(profilingPoint.getProject());
            factories.add(profilingPoint.getFactory());
            dirtyProfilingPoints.remove(profilingPoint);
        }

        processor().post(new Runnable() {
            public void run() {
                for (ProfilingPointFactory factory : factories) {
                    if (factory == null) continue;

                    for (Lookup.Provider project : projects) {
                        try {
                            factory.saveProfilingPoints(project);
                        } catch (IOException ex) {
                            ProfilerDialogs.displayError(
                                    Bundle.ProfilingPointsManager_CannotStorePpMsg(
                                        factory.getType(), ProjectUtilities.getDisplayName(project)));
                        }
                    }
                }
            }
        });
    }

    private void unloadProfilingPoints(Lookup.Provider project) {
        List<ProfilingPoint> closedProfilingPoints = getProfilingPoints(project, false, true);
        List<ProfilingPoint> dirtyClosedProfilingPoints = new ArrayList<>();

        for (ProfilingPoint profilingPoint : closedProfilingPoints) {
            if (dirtyProfilingPoints.contains(profilingPoint)) {
                dirtyClosedProfilingPoints.add(profilingPoint);
            }
        }

        if (!dirtyClosedProfilingPoints.isEmpty()) {
            storeProfilingPoints(dirtyClosedProfilingPoints.toArray(new ProfilingPoint[0]));
        }

        for (ProfilingPoint closedProfilingPoint : closedProfilingPoints) {
            if (closedProfilingPoint instanceof CodeProfilingPoint) {
                CodeProfilingPoint cpp = (CodeProfilingPoint) closedProfilingPoint;
                removeProfilingPointFileWatch(cpp);
                deannotateProfilingPoint(cpp);
            }

            closedProfilingPoint.hideResults(); // TODO: should stay open if subproject of profiled project
            closedProfilingPoint.reset(); // TODO: should not reset if subproject of profiled project
            profilingPoints.remove(closedProfilingPoint);
        }
    }

    private CustomizerButton getCustomizerButton() {
        if (customizerButton == null) customizerButton = new CustomizerButton();
        return customizerButton;
    }
    
    private RequestProcessor processor() {
        RequestProcessor processor;
        synchronized(_processorLock) {
            processor = _processorRef == null ? null : _processorRef.get();
            if (processor == null) {
                processor = new RequestProcessor("ProfilingPoints RequestProcessor"); // NOI18N
                _processorRef = new WeakReference(processor);
            }
        }
        return processor;
    }

}

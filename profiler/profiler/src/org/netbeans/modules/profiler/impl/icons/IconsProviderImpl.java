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
package org.netbeans.modules.profiler.impl.icons;

import java.util.Map;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.LanguageIcons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.netbeans.modules.profiler.spi.IconsProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@ServiceProvider(service=IconsProvider.class)
public final class IconsProviderImpl extends IconsProvider.Basic {
    
    @Override
    protected final void initStaticImages(Map<String, String> cache) {
        cache.put(GeneralIcons.FILTER, "filter.png");
        cache.put(GeneralIcons.FILTER_HIGHL, "filterHighl.png");
        cache.put(GeneralIcons.SET_FILTER, "setFilter.png");
        cache.put(GeneralIcons.SET_FILTER_HIGHL, "setFilterHighl.png");
        cache.put(GeneralIcons.CLEAR_FILTER, "clearFilter.png");
        cache.put(GeneralIcons.CLEAR_FILTER_HIGHL, "clearFilterHighl.png");
        cache.put(GeneralIcons.CLOSE_PANEL, "closePanel.png");
        cache.put(GeneralIcons.FILTER_CONTAINS, "filterContains.png");
        cache.put(GeneralIcons.FILTER_NOT_CONTAINS, "filterNotContains.png");
        cache.put(GeneralIcons.FILTER_ENDS_WITH, "filterEndsWith.png");
        cache.put(GeneralIcons.FILTER_REG_EXP, "filterRegExp.png");
        cache.put(GeneralIcons.FILTER_STARTS_WITH, "filterStartsWith.png");
        cache.put(GeneralIcons.COLLAPSED_SNIPPET, "collapsedSnippet.png");
        cache.put(GeneralIcons.EXPANDED_SNIPPET, "expandedSnippet.png");
        cache.put(GeneralIcons.HIDE_COLUMN, "hideColumn.png");
        cache.put(GeneralIcons.MAXIMIZE_PANEL, "maximizePanel.png");
        cache.put(GeneralIcons.MINIMIZE_PANEL, "minimizePanel.png");
        cache.put(GeneralIcons.RESTORE_PANEL, "restorePanel.png");
        cache.put(GeneralIcons.SORT_ASCENDING, "sortAsc.png");
        cache.put(GeneralIcons.SORT_DESCENDING, "sortDesc.png");
        cache.put(GeneralIcons.POPUP_ARROW, "popupArrow.png");
        cache.put(GeneralIcons.ZOOM, "zoom.png");
        cache.put(GeneralIcons.ZOOM_IN, "zoomIn.png");
        cache.put(GeneralIcons.ZOOM_OUT, "zoomOut.png");
        cache.put(GeneralIcons.SCALE_TO_FIT, "scaleToFit.png");
        cache.put(GeneralIcons.INFO, "infoIcon.png");
        cache.put(GeneralIcons.FIND_NEXT, "findNext.png");
        cache.put(GeneralIcons.FIND_PREVIOUS, "findPrevious.png");
        cache.put(GeneralIcons.SAVE, "save.png");
        cache.put(GeneralIcons.SAVE_AS, "saveAs.png");
        cache.put(GeneralIcons.SAVE_VIEW, "saveView.png");
        cache.put(GeneralIcons.EXPORT, "saveAs.png");
        cache.put(GeneralIcons.DETACH, "detach.png");
        cache.put(GeneralIcons.START, "start.png");
        cache.put(GeneralIcons.PAUSE, "pause.png");
        cache.put(GeneralIcons.RERUN, "rerun.png");
        cache.put(GeneralIcons.RESUME, "resume.png");
        cache.put(GeneralIcons.STOP, "stop.png");
        cache.put(GeneralIcons.EMPTY, "empty.gif");
        cache.put(GeneralIcons.ERROR, "error.png");
        cache.put(GeneralIcons.FIND, "find.gif");
        cache.put(GeneralIcons.SLAVE_DOWN, "slaveDown.png");
        cache.put(GeneralIcons.SLAVE_UP, "slaveUp.png");
        cache.put(GeneralIcons.UPDATE_AUTO, "autoRefresh.png");
        cache.put(GeneralIcons.PIE, "pie.png");
        cache.put(GeneralIcons.UPDATE_NOW, "updateNow.png");
        cache.put(GeneralIcons.BUTTON_ATTACH, "attachButton.gif");
        cache.put(GeneralIcons.BUTTON_RUN, "runButton.gif");
        cache.put(GeneralIcons.UP, "up.png");
        cache.put(GeneralIcons.DOWN, "down.png");
        cache.put(GeneralIcons.FORWARD, "forward.png");
        cache.put(GeneralIcons.BACK, "back.png");
        cache.put(GeneralIcons.SETTINGS, "settings.png");
        cache.put(GeneralIcons.JAVA_PROCESS, "javaProcess.png");
        cache.put(GeneralIcons.FOLDER, "folder.png");
        cache.put(GeneralIcons.BADGE_ADD, "badgeAdd.png");
        cache.put(GeneralIcons.BADGE_REMOVE, "badgeRemove.png");
        cache.put(GeneralIcons.RENAME, "rename.png");
        cache.put(GeneralIcons.MATCH_CASE, "matchCase.png");
        cache.put(GeneralIcons.ADD, "add.png");
        cache.put(GeneralIcons.EDIT, "edit.png");
        cache.put(GeneralIcons.REMOVE, "remove.png");
        
        cache.put(ProfilerIcons.NODE_FORWARD, "forwardNode.png");
        cache.put(ProfilerIcons.NODE_REVERSE, "reverseNode.png");
        cache.put(ProfilerIcons.NODE_LEAF, "leafNode.png");
        cache.put(ProfilerIcons.SNAPSHOT_MEMORY_32, "memorySnapshot32.png");
        cache.put(ProfilerIcons.THREAD, "thread.png");
        cache.put(ProfilerIcons.ALL_THREADS, "allThreads.png");
        cache.put(ProfilerIcons.SQL_QUERY, "sqlQuery.png");
        cache.put(ProfilerIcons.ATTACH, "attach.png");
        cache.put(ProfilerIcons.ATTACH_24, "attach24.png");
        cache.put(ProfilerIcons.SNAPSHOTS_COMPARE, "compareSnapshots.png");
        cache.put(ProfilerIcons.SNAPSHOT_OPEN, "openSnapshot.png");
        cache.put(ProfilerIcons.SNAPSHOT_TAKE, "takeSnapshot.png");
        cache.put(ProfilerIcons.PROFILE, "profile.png");
        cache.put(ProfilerIcons.PROFILE_24, "profile24.png");
        cache.put(ProfilerIcons.PROFILE_INACTIVE, "profileInactive.png");
        cache.put(ProfilerIcons.PROFILE_RUNNING, "profileRunning.png");
        cache.put(ProfilerIcons.RESET_RESULTS, "resetResults.png");
        cache.put(ProfilerIcons.RUN_GC, "runGC.png");
        cache.put(ProfilerIcons.SNAPSHOT_THREADS, "threadsSnapshot.png");
        cache.put(ProfilerIcons.SNAPSHOT_HEAP, "heapSnapshot.png");
        cache.put(ProfilerIcons.CONTROL_PANEL, "controlPanel.gif");
        cache.put(ProfilerIcons.LIVE_RESULTS, "liveResults.png");
        cache.put(ProfilerIcons.MODIFY_PROFILING, "modifyProfiling.png");
        cache.put(ProfilerIcons.SHOW_GRAPHS, "showGraphs.png");
        cache.put(ProfilerIcons.SNAPSHOT_DO, "snapshotDataObject.png");
        cache.put(ProfilerIcons.SNAPSHOT_DO_32, "snapshotDataObject32.png");
        cache.put(ProfilerIcons.SNAPSHOT_CPU_DO, "snapshotDataObjectCPU.gif");
        cache.put(ProfilerIcons.SNAPSHOT_CPU_DO_32, "snapshotDataObjectCPU32.gif");
        cache.put(ProfilerIcons.SNAPSHOT_MEMORY_DO, "snapshotDataObjectMemory.gif");
        cache.put(ProfilerIcons.SNAPSHOT_MEMORY_DO_32, "snapshotDataObjectMemory32.gif");
        cache.put(ProfilerIcons.SNAPSHOT_FRAGMENT_DO, "snapshotDataObjectFragment.gif");
        cache.put(ProfilerIcons.SNAPSHOT_FRAGMENT_DO_32, "snapshotDataObjectFragment32.gif");
        cache.put(ProfilerIcons.TAKE_SNAPSHOT_CPU_32, "takeSnapshotCPU32.png");
        cache.put(ProfilerIcons.TAKE_SNAPSHOT_FRAGMENT_32, "takeSnapshotFragment32.png");
        cache.put(ProfilerIcons.TAKE_SNAPSHOT_MEMORY_32, "takeSnapshotMem32.png");
        cache.put(ProfilerIcons.TAKE_HEAP_DUMP_32, "takeSnapshotMem32.png");
        cache.put(ProfilerIcons.TAB_BACK_TRACES, "backTracesTab.png");
        cache.put(ProfilerIcons.TAB_CALL_TREE, "callTreeTab.png");
        cache.put(ProfilerIcons.TAB_COMBINED, "combinedTab.png");
        cache.put(ProfilerIcons.TAB_HOTSPOTS, "hotspotsTab.png");
        cache.put(ProfilerIcons.TAB_INFO, "infoTab.png");
        cache.put(ProfilerIcons.TAB_MEMORY_RESULTS, "memoryResultsTab.png");
        cache.put(ProfilerIcons.TAB_STACK_TRACES, "stackTracesTab.png");
        cache.put(ProfilerIcons.TAB_SUBTREE, "subtreeTab.png");
        cache.put(ProfilerIcons.WINDOW_CONTROL_PANEL, "controlPanelWindow.gif");
        cache.put(ProfilerIcons.WINDOW_LIVE_RESULTS, "liveResultsWindow.png");
        cache.put(ProfilerIcons.WINDOW_TELEMETRY, "telemetryWindow.png");
        cache.put(ProfilerIcons.WINDOW_TELEMETRY_OVERVIEW, "telemetryOverviewWindow.png");
        cache.put(ProfilerIcons.WINDOW_THREADS, "threadsWindow.png");
        cache.put(ProfilerIcons.WINDOW_LOCKS, "locksWindow.png");
        cache.put(ProfilerIcons.WINDOW_SQL, "sqlWindow.png");
        cache.put(ProfilerIcons.VIEW_LIVE_RESULTS_CPU_32, "liveResultsCPUView32.png");
        cache.put(ProfilerIcons.VIEW_LIVE_RESULTS_FRAGMENT_32, "liveResultsFragmentView32.png");
        cache.put(ProfilerIcons.VIEW_LIVE_RESULTS_MEMORY_32, "liveResultsMemView32.png");
        cache.put(ProfilerIcons.VIEW_THREADS_32, "threadsView32.png");
        cache.put(ProfilerIcons.VIEW_TELEMETRY_32, "telemetryView32.png");
        cache.put(ProfilerIcons.VIEW_LOCKS_32, "locksView32.png");
        cache.put(ProfilerIcons.CPU, "cpu.png");
        cache.put(ProfilerIcons.CPU_32, "cpu32.png");
        cache.put(ProfilerIcons.FRAGMENT, "fragment.png");
        cache.put(ProfilerIcons.MEMORY, "memory.png");
        cache.put(ProfilerIcons.MEMORY_32, "memory32.png");
        cache.put(ProfilerIcons.HEAP_DUMP, "memoryResultsTab.png");
        cache.put(ProfilerIcons.CUSTOM_32, "custom32.png");
        cache.put(ProfilerIcons.MONITORING, "monitoring.png");
        cache.put(ProfilerIcons.MONITORING_32, "monitoring32.png");
        cache.put(ProfilerIcons.STARTUP_32, "startup32.png");
        cache.put(ProfilerIcons.DELTA_RESULTS, "deltaValues.png");
        
        cache.put(LanguageIcons.CLASS, "class.png");
        cache.put(LanguageIcons.CLASS_ANONYMOUS, "classAnonymous.png");
        cache.put(LanguageIcons.CONSTRUCTOR_PACKAGE, "constructorPackage.png");
        cache.put(LanguageIcons.CONSTRUCTOR_PRIVATE, "constructorPrivate.png");
        cache.put(LanguageIcons.CONSTRUCTOR_PROTECTED, "constructorProtected.png");
        cache.put(LanguageIcons.CONSTRUCTOR_PUBLIC, "constructorPublic.png");
        cache.put(LanguageIcons.CONSTRUCTORS, "constructors.png");
        cache.put(LanguageIcons.INITIALIZER, "initializer.png");
        cache.put(LanguageIcons.INITIALIZER_STATIC, "initializerSt.png");
        cache.put(LanguageIcons.INTERFACE, "interface.png");
        cache.put(LanguageIcons.LIBRARIES, "libraries.png");
        cache.put(LanguageIcons.METHOD_PACKAGE, "methodPackage.png");
        cache.put(LanguageIcons.METHOD_PRIVATE, "methodPrivate.png");
        cache.put(LanguageIcons.METHOD_PROTECTED, "methodProtected.png");
        cache.put(LanguageIcons.METHOD_PUBLIC, "methodPublic.png");
        cache.put(LanguageIcons.METHOD_PACKAGE_STATIC, "methodStPackage.png");
        cache.put(LanguageIcons.METHOD_PRIVATE_STATIC, "methodStPrivate.png");
        cache.put(LanguageIcons.METHOD_PROTECTED_STATIC, "methodStProtected.png");
        cache.put(LanguageIcons.METHOD_PUBLIC_STATIC, "methodStPublic.png");
        cache.put(LanguageIcons.METHOD_INHERITED, "methodInherited.png");
        cache.put(LanguageIcons.METHODS, "methods.png");
        cache.put(LanguageIcons.METHOD, "method.png");
        cache.put(LanguageIcons.PACKAGE, "package.png");
        cache.put(LanguageIcons.VARIABLE_PACKAGE, "variablePackage.png");
        cache.put(LanguageIcons.VARIABLE_PRIVATE, "variablePrivate.gif");
        cache.put(LanguageIcons.VARIABLE_PROTECTED, "variableProtected.png");
        cache.put(LanguageIcons.VARIABLE_PUBLIC, "variablePublic.png");
        cache.put(LanguageIcons.VARIABLE_PACKAGE_STATIC, "variableStPackage.png");
        cache.put(LanguageIcons.VARIABLE_PRIVATE_STATIC, "variableStPrivate.png");
        cache.put(LanguageIcons.VARIABLE_PROTECTED_STATIC, "variableStProtected.png");
        cache.put(LanguageIcons.VARIABLE_PUBLIC_STATIC, "variableStPublic.png");
        cache.put(LanguageIcons.VARIABLES, "variables.png");
        cache.put(LanguageIcons.ARRAY, "array.png");
        cache.put(LanguageIcons.INSTANCE, "instance.png");
        cache.put(LanguageIcons.PRIMITIVE, "primitive.png");
        cache.put(LanguageIcons.JAR, "jar.png");
    }
    
}

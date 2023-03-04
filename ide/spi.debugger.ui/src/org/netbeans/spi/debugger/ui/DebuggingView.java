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

package org.netbeans.spi.debugger.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.debugger.ui.views.debugging.DebuggingViewComponent;
import org.netbeans.modules.debugger.ui.views.debugging.FiltersDescriptor;
import org.netbeans.modules.debugger.ui.views.debugging.FiltersDescriptor.FilterImpl;
import org.netbeans.modules.debugger.ui.views.debugging.FiltersDescriptor.FiltersAccessor;
import static org.netbeans.modules.debugger.ui.views.debugging.FiltersDescriptor.NATURAL_SORT;
import org.openide.windows.TopComponent;

/**
 * Debugging view component provider. The debugging view displays application threads.
 * Implement {@link DVSupport} and register under your session name to use
 * debugging view UI and register appropriate view models for path &lt;session name&gt;/DebuggingView.
 * 
 * @author Martin Entlicher
 * @since 2.43
 */
public final class DebuggingView {
    
    private static final DebuggingView INSTANCE = new DebuggingView();
    
    private Reference<DebuggingViewComponent> dvcRef = new WeakReference<DebuggingViewComponent>(null);
    
    private DebuggingView() {
    }
    
    /**
     * Get the default implementation of debugging view provider.
     * @return the default instance of debugging view provider.
     */
    public static DebuggingView getDefault() {
        return INSTANCE;
    }
    
    private DebuggingViewComponent getDVC() {
        DebuggingViewComponent dvc;
        synchronized (this) {
            dvc = dvcRef.get();
            if (dvc == null) {
                dvc = DebuggingViewComponent.getInstance();
                dvcRef = new WeakReference<DebuggingViewComponent>(dvc);
            }
        }
        return dvc;
    }
    
    /**
     * Get the debugging view top component.
     * @return the {@link TopComponent} of the debugging view.
     */
    public TopComponent getViewTC() {
        return getDVC();
    }
    
    /**
     * Support for debugging view. The component tree is created from view models
     * registered under path &lt;session name&gt;/DebuggingView. But to fully
     * support the debugging view UI, additional information is necessary.
     * Implement this class to provide the additional information.
     * Debugging view is created for the given debugger session only when an
     * implementation of this class is found in the current session engine lookup.
     */
    public abstract static class DVSupport {
        
        /** Property name constant. */
        public static final String          PROP_THREAD_STARTED = "threadStarted";      // NOI18N
        /** Property name constant. */
        public static final String          PROP_THREAD_DIED = "threadDied";            // NOI18N
        /** Property name constant. */
        public static final String          PROP_THREAD_GROUP_ADDED = "threadGroupAdded";   // NOI18N
        /** Property name constant. */
        public static final String          PROP_THREAD_SUSPENDED = "threadSuspended";  // NOI18N
        /** Property name constant. */
        public static final String          PROP_THREAD_RESUMED = "threadResumed";      // NOI18N
        /** Property name constant. */
        public static final String          PROP_CURRENT_THREAD = "currentThread";      // NOI18N

        /**
         * Name of property which is fired when deadlock occurs.
         */
        public static final String          PROP_DEADLOCK = "deadlock";                 // NOI18N
        public static final String          PROP_STATE = "state";                       // NOI18N

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        /**
         * The debugger state.
         */
        public static enum STATE {
            RUNNING,
            DISCONNECTED
        }
        
        static {
            FiltersDescriptor.getInstance().setFiltersAccessor(new FiltersAccessor() {
                @Override
                public List<DVFilter> getFilters(DVSupport dvs) {
                    return dvs.getFilters();
                }
                @Override
                public FilterImpl getImpl(DVFilter filter) {
                    return filter.getImpl();
                }
            });
        }
        
        protected DVSupport() {
        }
    
        /**
         * Get the debugger state.
         * @return current state of debugger
         */
        public abstract STATE getState();
        
        /**
         * Get listing of all threads at this moment.
         * @return list of all threads
         */
        public abstract List<DVThread> getAllThreads();
        
        /**
         * Get a current thread, if any.
         * @return a current thread, or <code>null</code>.
         */
        public abstract DVThread getCurrentThread();
        
        /**
         * Get the display name of the thread. It can contain more information
         * than a thread name, like current session name, etc.
         * @param thread the thread
         * @return the thread display name
         */
        public abstract String getDisplayName(DVThread thread);
        
        /**
         * Get the display name of the frame. It can contain more information
         * than a frame name, like source location, etc.
         * @param frame the frame
         * @return the frame display name
         * @since 2.65
         */
        public String getDisplayName(DVFrame frame) {
            return frame.getName();
        }
        
        /**
         * Get the thread icon.
         * @param thread the thread
         * @return the thread icon
         */
        public abstract Image getIcon(DVThread thread);

        /**
         * Get the session associated with this debugging view.
         * @return 
         */
        public abstract Session getSession();

        /**
         * Resume the application (all it's threads).
         */
        public abstract void resume();

        /**
         * Get the set of detected deadlocks, if any.
         * @return The set of deadlocks, or <code>null</code> when no deadlocks are detected.
         */
        public abstract Set<Deadlock> getDeadlocks();
        
        /**
         * Utility method used by the implementing class to create deadlock description instances.
         * @param threads The threads in deadlock
         * @return Deadlock instance
         */
        protected final Deadlock createDeadlock(Collection<DVThread> threads) {
            return new Deadlock(threads);
        }
        
        /**
         * Get the list of filters applied to debugging view.
         * @return list of filters
         */
        protected abstract List<DVFilter> getFilters();
        
        /**
         * Get actions created from the provided filters.
         * The result can be added to actions provider view model.
         * @return filter actions.
         */
        public final Action[] getFilterActions() {
            return FiltersDescriptor.getInstance().getFilterActions();
        }
        
        protected final void firePropertyChange(PropertyChangeEvent pce) {
            pcs.firePropertyChange(pce);
        }
        
        /**
         * Fire a property change event.
         * @param propertyName the property name
         * @param oldValue old value
         * @param newValue new value
         */
        protected final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
            pcs.firePropertyChange(propertyName, oldValue, newValue);
        }
        
        /**
         * Add a property change listener.
         * @param pcl the property change listener
         */
        public final void addPropertyChangeListener(PropertyChangeListener pcl) {
            pcs.addPropertyChangeListener(pcl);
        }
        
        /**
         * Remove a property change listener.
         * @param pcl the property change listener
         */
        public final void removePropertyChangeListener(PropertyChangeListener pcl) {
            pcs.removePropertyChangeListener(pcl);
        }
        
        /**
         * Declarative registration of a DVSupport implementation.
         * By marking the implementation class with this annotation,
         * you automatically register that implementation for use by the debugging view.
         * The class must be public and have a public constructor which takes
         * no arguments or takes {@link ContextProvider} as an argument.
         *
         * @author Martin Entlicher
         */
        @Retention(RetentionPolicy.SOURCE)
        @Target({ElementType.TYPE})
        public @interface Registration {
            /**
             * The path to register this implementation in.
             * Usually the session ID.
             */
            String path();
            
            /**
             * An optional position in which to register this service relative to others.
             * Lower-numbered services are returned in the lookup result first.
             * Services with no specified position are returned last.
             */
            int position() default Integer.MAX_VALUE;

        }

    }
    
    /**
     * Representation of a thread in debugging view.
     * Nodes representing a thread in debugging view model should implement this
     * interface.
     */
    public static interface DVThread {
        
        /**
         * Property name fired when a thread gets suspended.
         */
        public static final String PROP_SUSPENDED = "suspended";            // NOI18N
        /**
         * Property name fired when list of locker threads change.
         */
        public static final String PROP_LOCKER_THREADS = "lockerThreads";   // NOI18N
        /**
         * Property name fired when current breakpoint is changed.
         */
        public static final String PROP_BREAKPOINT = "currentBreakpoint";   // NOI18N
        
        /**
         * Get the name of the thread.
         * @return the name of the thread
         */
        public String getName();
        
        /**
         * Test if this thread is currently suspended by debugger.
         * @return <code>true</code> when the thread is suspended, <code>false</code> otherwise.
         */
        public boolean isSuspended();
        
        /**
         * Resume this thread.
         */
        public void resume();

        /**
         * Suspend this thread.
         */
        public void suspend();
        
        /**
         * Make this thread current. Code evaluation and stepping should be performed
         * in the current thread.
         */
        public void makeCurrent();

        /**
         * Get frame count of this thread. The frame count is provided when the thread
         * is suspended only.
         * @return the frame count, <code>0</code> when the thread is running.
         * @since 2.65
         */
        public default int getFrameCount() {
            return 0;
        }

        /**
         * Get the stack frames of this thread. Stack frames are provided when the thread
         * is suspended only.
         *
         * @return a list of stack frames, it's empty when the thread is running.
         * @since 2.65
         */
        public default List<DVFrame> getFrames() {
            return Collections.emptyList();
        }

        /**
         * Get the stack frames of this thread. Stack frames are provided when the thread
         * is suspended only.
         *
         * @param from a from index, inclusive
         * @param to a to index, exclusive
         * @return a list of stack frames, it's empty when the thread is running.
         * @since 2.65
         */
        public default List<DVFrame> getFrames(int from, int to) {
            return Collections.emptyList();
        }

        /**
         * Get the debugging view support that provides this thread.
         * @return the debugging view support
         */
        public DVSupport getDVSupport();

        /**
         * Lists threads that hold monitors that this thread is waiting on.
         * @return list of locker threads
         */
        public List<DVThread> getLockerThreads();
        
        /**
         * Resume any suspended threads that block execution of this thread by holding monitors.
         */
        public void resumeBlockingThreads();

        /**
         * Get current breakpoint, if any. This is a breakpoint that this thread is suspended on.
         * @return the current breakpoint or <code>null</code>
         */
        public Breakpoint getCurrentBreakpoint();
        
        /**
         * Test if this thread is performing a step operation right now.
         * @return <code>true</code> if this thread is in a step, <code>false</code> otherwise.
         */
        public boolean isInStep();

        /**
         * Add a property change listener.
         * @param pcl the property change listener
         */
        public void addPropertyChangeListener(PropertyChangeListener pcl);

        /**
         * Remove a property change listener.
         * @param pcl the property change listener
         */
        public void removePropertyChangeListener(PropertyChangeListener pcl);

    }
    
    /**
     * Representation of a thread group in debugging view.
     * Nodes representing a thread group in debugging view model should implement
     * this interface.
     */
    public static interface DVThreadGroup {
        
        /**
         * Get the name of the thread group.
         * @return the name of the thread group
         */
        public String getName();
        
        /**
         * Get the parent thread group, if exists.
         * @return the parent thread group or <code>null</code> if this thread group has no parent (root thread group).
         */
        public DVThreadGroup getParentThreadGroup();
        
        /**
         * Get this thread group's threads.
         * @return threads from this thread group
         */
        public DVThread[] getThreads();
        
        /**
         * Get this thread group's thread groups.
         * @return thread groups from this thread group
         */
        public DVThreadGroup[] getThreadGroups();
    }
    
    /**
     * Representation of a stack frame in debugging view.
     * Nodes representing a stack frame in debugging view model should implement this
     * interface.
     * @since 2.65
     */
    public static interface DVFrame {

        /**
         * Get the name of the frame. Usually the frame's class + method name, or function name.
         * @return the name of the frame to be displayed in the debugging view.
         * @since 2.65
         */
        String getName();

        /**
         * Get the thread of this frame.
         * @since 2.65
         */
        DVThread getThread();

        /**
         * Make this frame current. Code evaluation and stepping should be performed
         * in the current frame.
         * @since 2.65
         */
        void makeCurrent();

        /**
         * Get URI of the source file associated with this frame, if any.
         * @return a source URI, or <code>null</code> if the file is unknown.
         * @since 2.65
         */
        URI getSourceURI();

        /**
         * Get the source MIME type, if known.
         * @return the source MIME type, or <code>null</code> if the source, or
         * its MIME type is unknown.
         * @since 2.67
         */
        default String getSourceMimeType() {
            return null;
        }

        /**
         * Line location of the frame in the source code at {@link #getSourceURI()}.
         *
         * @return the line number, or <code>-1</code> if the line is unknown
         * @since 2.65
         */
        int getLine();

        /**
         * Column location of the frame in the source code at {@link #getSourceURI()}.
         *
         * @return the column number, or <code>-1</code> if the column is unknown
         * @since 2.65
         */
        int getColumn();

        /**
         * Pop all frames up to and including this frame off the stack.
         *
         * @throws UnsupportedOperationException thrown when popping of stack frames is not supported.
         * @throws PopException when the pop frame operation fails.
         * @since 2.70
         */
        default void popOff() throws UnsupportedOperationException, PopException {
            throw new UnsupportedOperationException("The frame pop is not supported.");
        }
    }

    /**
     * Thrown when {@link DVFrame#popOff()} operation fails. The message of this
     * exception describes the failure.
     *
     * @since 2.70
     */
    public static final class PopException extends Exception {

        /**
         * Creates a new PopException with a description message.
         * @param message the description
         *
         * @since 2.70
         */
        public PopException(String message) {
            super(message);
        }
    }

    /**
     * Representation of a deadlock - one set of mutually deadlocked threads.
     */
    public static final class Deadlock {

        private final Collection<DVThread> threads;
        
        private Deadlock(Collection<DVThread> threads) {
            this.threads = threads;
        }
        
        /**
         * Get the threads in deadlock.
         * @return The threads in deadlock.
         */
        public Collection<DVThread> getThreads() {
            return threads;
        }
    }
    
    /**
     * Boolean state filter that is applied to the debugging view.
     * It's icon is made visible in the debugging view bottom panel.
     */
    public static final class DVFilter {

        /**
         * Pre-defined default filters enumeration.
         */
        public static enum DefaultFilter {
            sortAlphabetic,
            sortSuspend,
            sortNatural,
            showQualifiedNames,
            showMonitors,
            showSystemThreads,
            showSuspendTable,
            showThreadGroups,
            showSuspendedThreadsOnly,
        }
        
        private static Reference<Group> sortGroupRef = new WeakReference<Group>(null);
        
        /**
         * Get an instance of a default filter.
         * @param filter the default filter kind
         * @return filter implementation
         */
        public static DVFilter getDefault(DefaultFilter filter) {
            FilterImpl fimpl = FilterImpl.createDefault(filter);
            Group g;
            switch (filter) {
                case sortAlphabetic:
                case sortNatural:
                case sortSuspend:
                    g = getGroupFor(filter, fimpl); //sortGroup;
                    break;
                default:
                    g = null;
            }
            fimpl.setGroup(g);
            return new DVFilter(fimpl, g);
        }
        
        private static Group getGroupFor(DefaultFilter filter, FilterImpl fimpl) {
            Group group = sortGroupRef.get();
            if (group == null) {
                group = new Group();
                sortGroupRef = new WeakReference<Group>(group);
            } else {
                for (DVFilter df : group.getItems()) {
                    if (df.getImpl().getName().equals(fimpl.getName())) {
                        // The group already contains this item. We need to create a new group...
                        group = new Group();
                        sortGroupRef = new WeakReference<Group>(group);
                    }
                }
            }
            return group;
        }
        
        /**
         * Create a custom filter.
         * @param name name of the filter
         * @param displayName display name of the filter (visible in an action menu)
         * @param tooltip tool-tip of the filter
         * @param selectedIcon icon of the filter
         * @param valuePrefs preferences which are asked for the filter value
         * @param valuePrefKey key that is used to retrieve the filter value from preferences
         * @param isSelectedDefault whether the filter should be selected by default when preferences do not contain the value
         * @param group a filter group, can be <code>null</code>
         * @return implementation of the filter
         */
        public static DVFilter create(String name, String displayName,
                                      String tooltip, Icon selectedIcon,
                                      Preferences valuePrefs, String valuePrefKey,
                                      boolean isSelectedDefault, Group group) {
            FilterImpl fimpl = new FilterImpl(name, displayName, tooltip, selectedIcon,
                                              valuePrefs, valuePrefKey, isSelectedDefault);
            fimpl.setGroup(group);
            return new DVFilter(fimpl, group);
        }
        
        private final FilterImpl fimpl;
        private final Group group;
        
        DVFilter (FilterImpl fimpl, Group group) {
            this.fimpl = fimpl;
            this.group = group;
            if (group != null) {
                group.add(this);
            }
        }

        private FilterImpl getImpl() {
            return fimpl;
        }
        
        /**
         * Get the filter group.
         * @return the filter group, or <code>null</code> when the filter has no group
         */
        public Group getGroup() {
            return group;
        }
        
        /**
         * Get the filter name.
         * @return the filter name
         */
        public String getName() {
            return fimpl.getName();
        }
        
        /**
         * Get the filter display name.
         * @return the filter display name
         */
        public String getDisplayName() {
            return fimpl.getDisplayName();
        }
        
        /**
         * Get the filter tooltip.
         * @return the filter tooltip
         */
        public String getTooltip() {
            return fimpl.getTooltip();
        }

        /**
         * Get the filter icon.
         * @return the filter icon
         */
        public Icon getIcon() {
            return fimpl.getIcon();
        }

        /**
         * Test if the filter is selected.
         * @return whether the filter is selected right now
         */
        public boolean isSelected() {
            return fimpl.isSelected();
        }
        
        /**
         * Set the filter as selected/unselected.
         * @param state whether to select the filter
         */
        public void setSelected(boolean state) {
            
            if (!state && group != null) {
                // unselecting a grouped item
                boolean isSomeSelected = false;
                for (DVFilter dvf : group.getItems()) {
                    if (dvf.getImpl() != fimpl) {
                        if (dvf.isSelected()) {
                            isSomeSelected = true;
                            break;
                        }
                    }
                }
                if (!isSomeSelected) {
                    // We're trying to unselect the only selected item in the group
                    if (NATURAL_SORT.equals(fimpl.getName())) {
                        // Ignore unselect
                        fimpl.setSelected(true);
                        fimpl.assureButtonSelected(true);
                        return ;
                    }
                    // Else force to select the natural sort
                    for (DVFilter dvf : group.getItems()) {
                        if (NATURAL_SORT.equals(dvf.getName())) {
                            dvf.getImpl().setSelected(true);
                        }
                    }
                }
            }
            
            fimpl.setSelected(state);
        }
        
        /**
         * Get the filter preferences.
         * @return the preferences of this filter
         */
        public Preferences getPreferences() {
            return fimpl.getPreferences();
        }
        
        /**
         * Get the preference key.
         * @return key that is used to retrieve the filter value from preferences
         */
        public String getPrefKey() {
            return fimpl.getPrefKey();
        }
        
        /**
         * The filter group.
         */
        public static final class Group {
            
            private final List<DVFilter> items = new LinkedList<DVFilter>();
            
            /**
             * Create a new empty group.
             */
            public Group() {
            }
            
            void add(DVFilter filter) {
                items.add(filter);
            }
            
            /**
             * Get list of filters in this group.
             * @return list of filters
             */
            public List<DVFilter> getItems() {
                return items;
            }
        }
        
    }
    
}

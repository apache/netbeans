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

package org.netbeans.modules.team.ide.spi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

/**
 * Provides access to various IDE services so that the expected consumers (bugtracking and team modules) 
 * are able to independently access different IDE infrastructures (like e.g. NetBeans or JDev). 
 * 
 * @author Tomas Stupka
 */
public interface IDEServices {
    
    /**
     * Determines whether the functionality to open a document for a resource (file)
     * is available. <br>
     * 
     * @return <code>true</code> if available, otherwise <code>false</code>
     */
    public boolean providesOpenDocument();
    
    /**
     * Opens a document representing the given resource. 
     * <b>Note</b> that the given path doesn't necessarily have to be a fully qualified path, but 
     * might be in a shorter form as given by e.g. an stacktrace - org/netbeans/modules/bugzilla/Bugzilla.java

     * @param resourcePath
     * @param offset 
     */
    public void openDocument(String resourcePath, int offset);
    
    /**
     * Determines whether the functionality to jump to a resource is available. 
     * 
     * @return <code>true</code> if available, otherwise <code>false</code>
     */
    public boolean providesJumpTo();
    
    /**
     * 
     * Opens a search/find resource UI prefilled with the given resource. 
     * <br>
     * <b>Note</b> that the given resource doesn't necessarily have to be a be a fully qualified path, but 
     * might be just an arbitrary string potentially identifying e.g. a java type.
     * 
     * @param resource
     * @param title 
     */
    public void jumpTo(String resource, String title);

    /**
     * Determines whether the functionality to download a plugin is available 
     * 
     * @return <code>true</code> if available, otherwise <code>false</code>
     */
    public boolean providesPluginUpdate();
    
    /**
     * Returns a Plugin with the given code name base in case there is none installed, 
     * or that the currently installed version is lesser than the installed.
     * 
     * @param cnb - the plugins code name base
     * @param pluginName the plugins name - e.g. Bugzilla or Jira
     * @return plugin or null if not available
     */
    public Plugin getPluginUpdates(String cnb, String pluginName);
    
    /**
     * Determines whether the plugin with the given code name base is already 
     * installed or not.
     * 
     * @param cnb - the code name base
     * @return <code>true<code> in case the plugin is already installed, otherwise <code>false</code>
     */
    public boolean isPluginInstalled(String cnb);
    
    /**
     * Determines whether patch relevant functionality is available.
     * 
     * @return <code>true</code> if available, otherwise <code>false</code>
     */
    public boolean providesPatchUtils();

    /**
     * Applies the given patch file.
     * 
     * @param patchFile the patch files
     */
    public void applyPatch(File patchFile);

    /**
     * Determines whether the given file is in a recognized patch format.
     * 
     * @param patchFile
     * @return true in case the file is a patch, otherwise false
     * @throws IOException in case something is wrong with the file
     */
    public boolean isPatch(File patchFile) throws IOException;

    /**
     * Determines whether the functionality to open the History for a resource (file)
     * is available.
     * 
     * @return <code>true</code> if available, otherwise <code>false</code>
     */
    public boolean providesOpenHistory();

    /**
     * Meant to open a VCS history view where:
     * - it is possible to traverse the given resource history entries 
     * - a diff view is provided, showing the selected revision compared against 
     * it's parent and positioned on the given line.
     *
     * @param resourcePath resourcePath representing a versioned file (not a folder). 
     * <b>Note</b> that the given path doesn't necessarily have to be the full path, but 
     * might be a shorter form as given by e.g. an stacktrace - org/netbeans/modules/bugzilla/Bugzilla.java
     * @param line requested line number to lock on
     * @return true if parameters are valid, the file is versioned and the history view was opened, 
     * otherwise false.
     */
    public boolean openHistory(String resourcePath, int line);
    
    /**
     * Creates an animated busy icon (used e.g. in ProgressLabel) to be shown in
     * UI (like the treelist nodes) that perform some operation (e.g. searching).
     * May return null.
     * 
     * @return <code>BusyIcon</code> implementation of an animated busy icon, or
     *         <code>null</code> if no specific implementation is available
     */
    public BusyIcon createBusyIcon();

    /**
     * Determines whether the capability of opening a directory in a file browse
     * UI (e.g. Favorites window in NetBeans) is available.
     * @return <code>true</code> if can open a directory in a Favorites UI, otherwise <code>false</code>
     */
    public boolean canOpenInFavorites();

    /**
     * Opens given directory in file browser (Favorites).
     * @param workingDir 
     */
    public void openInFavorites(File workingDir);

    /**
     * Determines whether it is possible to shutdown the IDE or not.
     * 
     * @param restart whether it is possible to shutdown with a subsequent restart or without it.
     * @return <code>true</code> if it is possible to shutdown the IDE, otherwise <code>false</code>. 
     */
    public boolean providesShutdown(boolean restart);

    /**
     * Shuts down the IDE. 
     * 
     * @param restart <code>true</code> if the IDE is supposed to restart after the shutdown.
     */
    public void shutdown(boolean restart);
        
    /**
     * Determines whether it is possible to open some text fragment in the StackAnalyzer.
     * 
     * @return <code>true</code> a stack analyzer view is available, otherwise <code>false</code>. 
     */
    public boolean providesOpenInStackAnalyzer();

    /**
     * Opens some text in the StackAnalyzer. Reads the lines from 
     * the supplied reader and fills them into the stack analyzer view.
     * @param r
     */
    public void openInStackAnalyzer(BufferedReader r);
    
    /**
     * Creates a date picker component that can be used in task editor UI.
     * May return null.
     * 
     * @return <code>DatePickerComponent</code> implementation of a date picker, or
     *         <code>null</code> if no specific implementation is available
     */
    public DatePickerComponent createDatePicker ();

    /**
     * Provides access to a downloadable plugin - e.g. from the NetBeans UC
     */
    public interface Plugin {
        /**
         * Returns the plugins description
         * @return the plugins description
         */
        String getDescription();
        
        /**
         * Install or Update the plugin. 
         * @return <code>true</code> in case it was possible to install the plugin, otherwise <code>false</code> 
         */
        boolean installOrUpdate();
    }

    /**
     * Defines interface for an animated busy icon (used e.g. in ProgressLabel).
     * Implementation can use a specific library, e.g. SwingX.
     */
    public interface BusyIcon extends Icon {
        /**
         * Called by timer (run by ProgressLabel) for next animation step.
         */
        public void tick();
    }
    
    /**
     * Date picker component used in task editor and tasks view
     * Implementation can use a specific library, e.g. SwingX.
     */
    public interface DatePickerComponent {
        
        /**
         * Returns the component that can be added into a component hierarchy
         * @return UI component
         */
        public JComponent getComponent ();
        
        /**
         * Sets date for this component.
         * @param date date to set
         */
        public void setDate (Date date);
        
        /**
         * Returns date selected by this component
         * @return selected date
         */
        public Date getDate ();
        
        /**
         * Adds a listener that will be notified when the selected date changes.
         * @param listener listener to add
         */
        public void addChangeListener (ChangeListener listener);
        
        /**
         * Removes a listener, it will be no longer notified about changes.
         * @param listener listener to remove
         */
        public void removeChangeListener (ChangeListener listener);
        
        /**
         * Specifies if it is allowed to call {@link #openDaySelector()} and the
         * implementation allows selecting a date from a tool able traversing
         * months and selecting dates from a table. Some implementations may not
         * allow that (such as dummy implementations built on top of a
         * {@link JTextComponent}s.
         *
         * @return <code>true</code> if the implementation allows selecting date
         * from a popup, <code>false</code> otherwise.
         */
        public boolean allowsOpeningDaySelector ();
        
        /**
         * Opens a dialog, window or popup and lets user select a date in a
         * smarter way than enter the date manually by keyboard. The date may be
         * preselected with a preceding call to
         * {@link #setDate(java.util.Date)}. Call {@link #getDate()} to get the
         * date user selected.
         *
         *
         * An implementation could open a month view and let user traverse
         * months and select a day from a table.
         *
         * @return <code>true</code> if user selected the date or
         * <code>false</code> if the process was interrupted (user may have
         * canceled it).
         */
        public boolean openDaySelector ();
        
    }
}

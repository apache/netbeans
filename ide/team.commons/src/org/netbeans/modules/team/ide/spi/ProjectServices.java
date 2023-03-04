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

import java.io.File;
import java.net.URL;
import java.util.concurrent.Callable;
import org.openide.filesystems.FileObject;

/**
 *
 * Provides access to various Project relevant services so that the expected consumers (bugtracking and team modules) 
 * are able to independently access different IDE Project infrastructures (like e.g. NetBeans or JDev). 
 * 
 * @author Tomas Stupka
 */
public interface ProjectServices {
    
    /**
     * 
     * Postpones the execution of the given operation in case projects opening is currently running. 
     * 
     * @param <T>
     * @param operation
     * @return
     * @throws Exception 
     */
    public <T> T runAfterProjectOpenFinished(final Callable<T> operation) throws Exception;
   
    /**
     * 
     * Return the currently open projects
     * 
     * @return the currently open projects
     */
    public FileObject[] getOpenProjectsDirectories();
    
    /**
     * 
     * Returns the main project or null if none
     * 
     * @return main project
     */
    public FileObject getMainProjectDirectory();
    
    /** 
     * 
     * Determines the directory of the given files owner - typically a project
     * 
     * @param fileObject
     * @return owners directory or null if not available
     */
    public FileObject getFileOwnerDirectory(FileObject fileObject);
    
    /**
     * 
     * Determines the FileObject-s representing the current selection in the IDE.
     * 
     * @return 
     */
    public FileObject[] getCurrentSelection();
    
    /**
     * 
     * Opens project of given URL in the IDE.
     * 
     * @param url URL representing the project
     * @return true if the project opened (false e.g. if not found)
     */
    public boolean openProject(URL url);

    /**
     * 
     * Lets the user open a project from within given working directory.
     * 
     * @param workingDir 
     */
    public void openOtherProject(File workingDir);

    /**
     * Lets the user choose a project on disk (the same way it would be chosen
     * to open).
     * @param workingDir Context where to open the chooser at, can be null
     * @return File[] representing the directories of selected projects
     */
    public File[] chooseProjects(File workingDir);

    /**
     * This method is used to close projects currently opened in the IDE and
     * reopen them from a different locations. Used when projects from different
     * places were copied under the same root to be added to version control.
     * @param oldLocations Directories of projects to be closed
     * @param newLocations Directories of the corresponding projects to be opened
     */
    public void reopenProjectsFromNewLocation(File[] oldLocations, File[] newLocations);

    /**
     * Lets the user create a new local project in given location.
     * @param workingDir
     */
    public void createNewProject(File workingDir);

    /**
     * Creates IDEProject representation for given URL.
     * 
     * @param url URL representing the project
     * @return IDEProject for given project URL, or null if the project does not exist
     */
    public IDEProject getIDEProject(URL url);

    /**
     * 
     * Provides information about all projects currently opened in the IDE
     * 
     * (i.e. shown in a list of opened projects).
     * @return IDEProject array representing all opened user projects
     */
    public IDEProject[] getOpenProjects();

    /**
     * 
     * Adds a listener to be informed about opening new projects. The listener
     * should be held weakly in the implementation.
     * 
     * @param listener 
     */
    public void addProjectOpenListener(IDEProject.OpenListener listener);

    /**
     * 
     * Removes listener on project opening.
     * 
     * @param listener 
     */
    public void removeProjectOpenListener(IDEProject.OpenListener listener);

}

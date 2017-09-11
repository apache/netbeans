/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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

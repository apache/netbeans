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
package org.netbeans.modules.selenium2.spi;

import java.util.Collection;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Theofanis Oikonomou
 */
public abstract class Selenium2SupportImpl {
    
    /**
     * Check whether given project instance is supported by this implementation
     *
     * @param p project to check
     * @return true if this instance supports given project
     */
    public abstract boolean isSupportActive(Project p);

    /**
     * Configure project owning given FileObject
     *
     * @param targetFolder FileObject for which the project should be configured
     */
    public abstract void configureProject(FileObject targetFolder);
    
    /**
     * Creates a target chooser panel.
     *
     * @param wiz wizard's descriptor
     * @return a wizard panel prompting the user to choose a name and location
     */
    public abstract WizardDescriptor.Panel createTargetChooserPanel(WizardDescriptor wiz);
    
    /**
     * Finds the template type that is to be loaded while creating a new test.
     *
     * @return bundle key identifying the template type
     */
    public abstract String getTemplateID();
    
    /**
     * Check whether this implementation supports given FileObjects.
     *
     * @param activatedFOs FileObjects to check
     * @return {@code true} if this instance supports given FileObjects, {@code false} otherwise
     */
    public abstract boolean isSupportEnabled(FileObject[] activatedFOs);

    /**
     * Finds <code>SourceGroup</code>s where a test for the given class
     * can be created (so that it can be found by the projects infrastructure
     * when a test for the class is to be opened or run).
     *
     * @param createdSourceRoots
     * @param  fileObject  <code>FileObject</code> to find target
     *                     <code>SourceGroup</code>(s) for
     * @return  an array of objects - each of them can be either
     *          a <code>SourceGroup</code> for a possible target folder
     *          or simply a <code>FileObject</code> representing a possible
     *          target folder (if <code>SourceGroup</code>) for the folder
     *          was not found);
     *          the returned array may be empty but not <code>null</code>
     */
    public abstract List<Object> getTestSourceRoots(Collection<SourceGroup> createdSourceRoots, FileObject fileObject);

    /**
     * Finds <code>SourceGroup</code>s where a test for the given class
     * can be created (so that it can be found by the projects infrastructure
     * when a test for the class is to be opened or run).
     *
     * @param fo <code>FileObject</code> to find Source and Test filenames for
     * @param isTestNG {@code true} if user wants to create TestNG test, {@code false} otherwise
     * @param isSelenium {@code true} if user wants to create Selenium test, {@code false} otherwise
     * @return  an array of Strings - the first one being the source class name
     *          and the second being the test class name.
     *          the returned array may be empty but not <code>null</code>
     */
    public abstract String[] getSourceAndTestClassNames(FileObject fo, boolean isTestNG, boolean isSelenium);
    
    /**
     * Runs tests based on user selection.
     *
     * @param activatedFOs <code>FileObject</code>s to run tests for
     * @param isSelenium {@code true} if Selenium tests are to be run, {@code false} otherwise
     */
    public abstract void runTests(FileObject[] activatedFOs, boolean isSelenium);
    
}

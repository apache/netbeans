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
package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.test.subversion.operators.actions.ImportAction;

/**
 * Class implementing all necessary methods for handling "ImportWizardOperator" NbDialog.
 * 
 * 
 * @author peter
 * @version 1.0
 */
public class ImportWizardOperator extends WizardOperator {

    /**
     * Creates new ImportWizardOperator that can handle it.
     */
    public ImportWizardOperator() {
        super("Import");
    }

    /** Invokes new wizard and returns instance of ImportWizardOperator.
     * @return instance of ImportWizardOperator
     */
    public static ImportWizardOperator invoke() {
        new ImportAction().perform();
        return new ImportWizardOperator();
    }
    
    /** Invokes new wizard on given node and returns instance of ImportWizardOperator.
     * @param node node on which to invoke wizard
     * @return instance of ImportWizardOperator
     */
    public static ImportWizardOperator invoke(Node node) {
        new ImportAction().perform(node);
        return new ImportWizardOperator();
    }
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /**
     * Goes through the wizard and fill supplied parameter.
     * @param cvsRoot CVS root.
     * @param password password - can be null
     * @param folderToImport local folder to import
     * @param importMessage import message
     * @param repositoryFolder repository folder
     */
    public void doImport(String repositoryURL, String password, String importMessage, String repositoryFolder) {
        if(repositoryURL == null) {
            throw new JemmyException("CVS root must not be null."); // NOI18N
        }
        if(importMessage == null) {
            throw new JemmyException("Import message must not be null."); // NOI18N
        }
        if(repositoryFolder == null) {
            throw new JemmyException("Repository Folder must not be null."); // NOI18N
        }
        ImportWizardOperator.invoke();
        RepositoryStepOperator rso = new RepositoryStepOperator();
        if(password != null) {
            rso.setPassword(password);
        }
        rso.setRepositoryURL(repositoryURL);
        rso.next();
        FolderToImportStepOperator folderToImportOper = new FolderToImportStepOperator();
        folderToImportOper.setImportMessage(importMessage);
        folderToImportOper.setRepositoryFolder(repositoryFolder);
        folderToImportOper.finish();
    }
}


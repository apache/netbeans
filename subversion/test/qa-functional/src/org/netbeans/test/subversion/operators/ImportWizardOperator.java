/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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


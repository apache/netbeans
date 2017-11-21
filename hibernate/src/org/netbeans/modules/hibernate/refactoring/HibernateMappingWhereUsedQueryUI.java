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
package org.netbeans.modules.hibernate.refactoring;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Dongmei Cao
 */
public class HibernateMappingWhereUsedQueryUI implements RefactoringUI {
    private WhereUsedQuery query = null;
    private FileObject fileObject;

    public HibernateMappingWhereUsedQueryUI(FileObject fileObject) {
        this.query = new WhereUsedQuery(Lookups.singleton(fileObject));
        this.fileObject = fileObject;
    }
    

    public boolean isQuery() {
        return true;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        return null;
    }

    public org.netbeans.modules.refactoring.api.Problem setParameters() {
            return null;
    }
    
    
    
    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
        
            return null;
    }

    public org.netbeans.modules.refactoring.api.AbstractRefactoring getRefactoring() {
        return query;
    }

    public String getDescription() {
        return NbBundle.getMessage(HibernateMappingWhereUsedQueryUI.class, "DSC_WhereUsed", fileObject.getNameExt());
    }
   
    
    public boolean hasParameters() {
        return false;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(HibernateMappingWhereUsedQueryUI.class);
    }

    public String getName() {
        return fileObject.getName();
    }
    
}

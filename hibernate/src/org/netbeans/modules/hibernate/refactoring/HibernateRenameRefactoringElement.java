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

import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.NbBundle;

/**
 * A refactoring element for renaming Java class or mapping file names
 * 
 * @author Dongmei Cao
 */
public class HibernateRenameRefactoringElement extends HibernateRefactoringElement {

    private String newName;

    public HibernateRenameRefactoringElement(FileObject fo, String oldName, String newName, PositionBounds position, String displayText) {
        this(fo, oldName, oldName, newName, position, displayText);
    }
    
    public HibernateRenameRefactoringElement(FileObject fo, String oldName, String matching, String newName, PositionBounds position, String displayText) {
        super(fo, oldName, matching, position, displayText);
        this.newName = newName;
    }

    @Override
    public String getDisplayText() {
        return NbBundle.getMessage(HibernateRenameRefactoringElement.class, "CHANGE", origName, newName);
    }

    @Override
    public void performChange() {
        // Do nothing here. The changes are performed in RenameTransaction
    }
    
    /*
     * Return String representation of whole file after refactoring
     * */
    @Override
    public String getNewFileContent() {
        // TODO: implment this method so that the user can preview the diffs
        return null;
    }
}

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
package org.netbeans.modules.web.refactoring.rename;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.refactoring.RefactoringUtil;

/**
 * Handles rename refactoring in tld files.
 *
 * @author Erno Mononen
 */
public class TldRename extends BaseTldRename{
    
    private final String clazz;
    private final RenameRefactoring rename;
    
    public TldRename(String clazz, RenameRefactoring rename, WebModule webModule) {
        super(webModule);
        this.rename = rename;
        this.clazz = clazz;
    }
    
    protected List<RenameItem> getAffectedClasses() {
        String newName = RefactoringUtil.renameClass(clazz, rename.getNewName());
        return Collections.<RenameItem>singletonList(new RenameItem(newName, clazz));
    }
    
    protected AbstractRefactoring getRefactoring() {
        return rename;
    }
    
}

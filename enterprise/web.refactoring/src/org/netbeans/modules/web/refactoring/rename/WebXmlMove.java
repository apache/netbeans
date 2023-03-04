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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.web.refactoring.RefactoringUtil;
import org.openide.filesystems.FileObject;

/**
 * Handles moving of a class.
 *
 * @author Erno Mononen
 */
public class WebXmlMove extends BaseWebXmlRename{
    
    private final MoveRefactoring move;
    private final List<String> classes;
    
    public WebXmlMove(FileObject webDD, MoveRefactoring move) {
        super(webDD);
        this.move = move;
        this.classes = RefactoringUtil.getRefactoredClasses(move);
    }
    
    protected AbstractRefactoring getRefactoring() {
        return move;
    }
    
    protected List<RenameItem> getRenameItems() {
        String pkg = RefactoringUtil.getPackageName(move.getTarget().lookup(URL.class));
        List<RenameItem> result = new ArrayList<RenameItem>();
        for (String clazz : classes) {
            String newName = pkg + "." + JavaIdentifiers.unqualify(clazz);
            result.add(new RenameItem(newName, clazz));
        }
        return result;
    }
}

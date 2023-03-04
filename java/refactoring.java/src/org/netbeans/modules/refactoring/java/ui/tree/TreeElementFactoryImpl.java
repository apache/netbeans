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

package org.netbeans.modules.refactoring.java.ui.tree;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation.class, position=100)
public class TreeElementFactoryImpl implements TreeElementFactoryImplementation {

    private final Map<Object, TreeElement> map = new WeakHashMap<>();
    
    @Override
    public TreeElement getTreeElement(Object o) {
        TreeElement result;
        if (o instanceof SourceGroup) {
            result = map.get(((SourceGroup) o).getRootFolder());
        } else {
            result = map.get(o);
        }
        if (result != null) {
            return result;
        }
        if (o instanceof FileObject) {
            FileObject fo = (FileObject) o;
            if (fo.isFolder()) {
                SourceGroup sg = FolderTreeElement.getSourceGroup(fo);
                if (sg != null && fo.equals(sg.getRootFolder())) {
                    result = new SourceGroupTreeElement(sg);
                } else {
                    result = new FolderTreeElement(fo);
                }
            } else {
                result = new FileTreeElement(fo);
            }
        } else if (o instanceof SourceGroup) {
            result = new SourceGroupTreeElement((SourceGroup) o);
        } else if (o instanceof ElementGrip) {
            result = new ElementGripTreeElement((ElementGrip) o);
        } else if (o instanceof Project) {
            result = new ProjectTreeElement((Project) o);
        } else if (o instanceof RefactoringElement) {
            RefactoringElement refactoringElement = (RefactoringElement) o;
            ElementGrip grip = (refactoringElement).getLookup().lookup(ElementGrip.class);
            if (grip != null) {
                result = new RefactoringTreeElement(refactoringElement);
            }
        } else if (o instanceof JavaPlatform) {
            result = new JavaPlatformTreeElement((JavaPlatform) o);
        }
        if (result != null) {
            if (o instanceof SourceGroup) {
                map.put(((SourceGroup) o).getRootFolder(), result);
            } else {
                map.put(o, result);
            }
        }
        return result;
    }

    @Override
    public void cleanUp() {
        map.clear();
        ElementGripFactory.getDefault().cleanUp();
    }
}

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

package org.netbeans.modules.lsp.client.bindings.refactoring.tree;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation;
import org.openide.filesystems.FileObject;

/**Copied from refactoring.java, and simplified.
 *
 * @author Jan Becicka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation.class)
public class TreeElementFactoryImpl implements TreeElementFactoryImplementation {

    private final Map<Object, TreeElement> map = new WeakHashMap<>();

    @Override
    public TreeElement getTreeElement(Object o) {
        TreeElement result;
        result = map.get(o);
        if (result != null) {
            return result;
        }
        if (o instanceof FileObject) {
            result = new FileTreeElement((FileObject) o);
        } else if (o instanceof Project) {
            result = new ProjectTreeElement((Project) o);
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
    }
}

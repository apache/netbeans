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

package org.netbeans.modules.maven.refactoring;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=TreeElementFactoryImplementation.class, position=189)
public class MavenTreeElementFactory implements TreeElementFactoryImplementation {

    private final Map<Object,TreeElement> created = new WeakHashMap<Object,TreeElement>();

    @Override public TreeElement getTreeElement(Object o) {
        TreeElement old = created.get(o);
        if (old == null) {
            created.put(o, old = doGetTreeElement(o));
        }
        return old;
    }

    @Override public void cleanUp() {
        created.clear();
    }

    private TreeElement doGetTreeElement(Object o) {
        if (o instanceof NBVersionInfo) {
            return new ArtifactTreeElement((NBVersionInfo) o);
        } else if (o instanceof RepositoryInfo) {
            return new RepositoryTreeElement((RepositoryInfo) o);
        } else if (o instanceof RefactoringElement) {
            RefactoringElement el = (RefactoringElement) o;
            ReferringClass ref = el.getLookup().lookup(ReferringClass.class);
            if (ref != null) {
                return new ReferringClassTreeElement(ref, el);
            }
        }
        return null;
    }

}

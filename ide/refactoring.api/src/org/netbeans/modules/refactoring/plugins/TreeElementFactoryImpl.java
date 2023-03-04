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

package org.netbeans.modules.refactoring.plugins;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation;


/**
 *
 * @author Jan Becicka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation.class)
public class TreeElementFactoryImpl implements TreeElementFactoryImplementation {

    private Map<Object, TreeElement> map = new WeakHashMap<Object, TreeElement>();

    @Override
    public TreeElement getTreeElement(Object o) {
        TreeElement result = map.get(o);
        if (result!= null)
            return result;
        if (o instanceof RefactoringElement) {
            result = new RefactoringTreeElement((RefactoringElement) o);
        }
        if (result != null) {
            map.put(o, result);
        }
        return result;
    }

    @Override
    public void cleanUp() {
        map.clear();
    }
}

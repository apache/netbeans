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

package org.netbeans.modules.cnd.highlight.error.includes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;


/**
 *
 */
public class ErrorFilesModel implements ListModel{
    private final List<String> names = new ArrayList<>();
    private final List<CsmOffsetable> failedPreproDirectiveList = new ArrayList<>();
    public ErrorFilesModel(List<CsmOffsetable> errors){
        Map<String, CsmOffsetable> tree = new TreeMap<>();
        for (Iterator<CsmOffsetable> it = errors.iterator(); it.hasNext(); ){
            CsmOffsetable error = it.next();
            String name = error.getContainingFile().getAbsolutePath().toString();
            tree.put(name, error);
        }
        for (Iterator<Entry<String, CsmOffsetable>> it = tree.entrySet().iterator(); it.hasNext(); ){
            Entry<String, CsmOffsetable> entry = it.next();
            names.add(entry.getKey());
            failedPreproDirectiveList.add(entry.getValue());
        }
    }
    @Override
    public int getSize() {
        return names.size();
    }

    @Override
    public Object getElementAt(int index) {
        return names.get(index);
    }

    public CsmOffsetable getFailedDirective(int index){
        return failedPreproDirectiveList.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
    }
}

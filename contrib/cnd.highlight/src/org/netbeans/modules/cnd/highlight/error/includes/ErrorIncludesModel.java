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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;


/**
 *
 */
public class ErrorIncludesModel implements ListModel {
    private final List<String> names = new ArrayList<>();
    private final List<List<CsmOffsetable>> includeList = new ArrayList<>();
    private final int errorFiles;
    private final int errorIncludes;
    public ErrorIncludesModel(List<CsmInclude> includes, List<CsmErrorDirective> errors){
        errorIncludes = includes.size();
        Map<String, List<CsmOffsetable>> tree = new TreeMap<>();
        Set<CsmFile> files = new HashSet<>();
        for (Iterator<CsmInclude> it = includes.iterator(); it.hasNext(); ){
            CsmInclude incl = it.next();
            files.add(incl.getContainingFile());
            String name;
            if (incl.isSystem()){
                name = "<"+incl.getIncludeName()+">"; // NOI18N
            } else {
                name = "\""+incl.getIncludeName()+"\""; // NOI18N
            }
            List<CsmOffsetable> list = tree.get(name);
            if (list == null){
                list = new ArrayList<>();
                tree.put(name,list);
            }
            list.add(incl);
        }
        for (CsmErrorDirective error : errors) {
            files.add(error.getContainingFile());
            String name = error.getText().toString();
            List<CsmOffsetable> list = tree.get(name);
            if (list == null) {
                list = new ArrayList<>();
                tree.put(name, list);
            }
            list.add(error);            
        }
        for (Iterator<Entry<String, List<CsmOffsetable>>> it = tree.entrySet().iterator(); it.hasNext(); ){
            Entry<String, List<CsmOffsetable>> entry = it.next();
            names.add(entry.getKey());
            includeList.add(entry.getValue());
        }
        errorFiles = files.size();
    }
    
    public int getFailedIncludesSize(){
        return errorIncludes;
    }

    public int getFailedFilesSize(){
        return errorFiles;
    }
    
    @Override
    public int getSize() {
        return names.size();
    }
    
    @Override
    public Object getElementAt(int index) {
        return names.get(index);
    }
    
    public List<CsmOffsetable> getElementList(int index){
        return includeList.get(index);
    }
    
    @Override
    public void addListDataListener(ListDataListener l) {
    }
    
    @Override
    public void removeListDataListener(ListDataListener l) {
    }
}

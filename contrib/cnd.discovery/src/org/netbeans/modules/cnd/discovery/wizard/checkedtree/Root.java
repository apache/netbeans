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

package org.netbeans.modules.cnd.discovery.wizard.checkedtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public class Root implements AbstractRoot {
    private final Map<String,AbstractRoot> children = new HashMap<>();
    private final String name;
    private final String folder;
    private final FileSystem fileStystem;
    private List<String> files;
    
    public Root(String name, String folder, FileSystem fileStystem){
        this.name = name;
        this.folder = folder;
        this.fileStystem = fileStystem;
    }
    
    @Override
    public Collection<AbstractRoot> getChildren(){
        return children.values();
    }
    
    @Override
    public String getName(){
        return name;
    }

    @Override
    public String getFolder(){
        return CndFileUtils.normalizeAbsolutePath(fileStystem, folder);
    }
    
    private Root getChild(String child){
        return (Root)children.get(child);
    }
    
    @Override
    public List<String> getFiles(){
        return files;
    }
    
    public void setFiles(List<String> files){
        if (this.files == null) {
            this.files = files;
        }
    }
    
    public Root addChild(String child){
        Root current = this;
        StringTokenizer st = new StringTokenizer(child,"/\\"); // NOI18N
        StringBuilder path = new StringBuilder();
        while(st.hasMoreTokens()){
            String segment = st.nextToken();
            if (path.length()>0){
                path.append('/');
            } else {
                if(!(segment.length()>1 && segment.charAt(1)==':')){
                    path.append('/');
                }
            }
            path.append(segment);
            if (st.hasMoreTokens()) {
                Root found = current.getChild(segment);
                if (found == null) {
                    found = new Root(segment, path.toString(), fileStystem);
                    current.children.put(segment, found);
                }
                current = found;
            } else {
                List<String> fileList = current.getFiles();
                if (fileList == null){
                    fileList = new ArrayList<>();
                    current.setFiles(fileList);
                }
                fileList.add(child);
            }
        }
        return current;
    }
}

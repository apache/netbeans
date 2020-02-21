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
import java.util.List;
import java.util.Set;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public class UnusedFactory {
    
    private UnusedFactory() {
    }

    public static AbstractRoot createRoot(Set<String> set, FileSystem fileStystem){
        AbstractRoot root = makeRoot(set, fileStystem);
        consolidateRoot(root);
        return root;
    }
    
    private static List<String> consolidateRoot(AbstractRoot root){
        List<String> files = root.getFiles();
        for(AbstractRoot current : root.getChildren()){
            List<String> fp = consolidateRoot(current);
            if (files == null) {
                files = new ArrayList<>();
                ((Root)root).setFiles(files);
            }
        }
        return files;
    }
    
    private static AbstractRoot makeRoot(Set<String> set, FileSystem fileStystem){
        AbstractRoot root = new Root("", "", fileStystem); //NOI18N
        for(String path : set){
            ((Root)root).addChild(path);
        }
        while(root.getFiles()== null){
            Collection<AbstractRoot> children = root.getChildren();
            if (children.size()==1){
                AbstractRoot child = children.iterator().next();
                root = child;
                continue;
            }
            break;
        }
        return root;
    }
    
}

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

package org.netbeans.modules.java.source.nbjavac.indexing;

import com.sun.tools.javac.api.ClassNamesForFileOraculum;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.tools.JavaFileObject;

/**
 *
 * @author Jan Lahoda
 */
public class ClassNamesForFileOraculumImpl implements ClassNamesForFileOraculum {

    private final Map<JavaFileObject, List<String>> misplacedSource2FQNs;

    public ClassNamesForFileOraculumImpl(Map<JavaFileObject, List<String>> misplacedSource2FQNs) {
        this.misplacedSource2FQNs = misplacedSource2FQNs;
    }
    
    public String[] divineClassName(JavaFileObject jfo) {
        if (misplacedSource2FQNs.isEmpty()) {
            return null;
        }
        
        List<String> result = misplacedSource2FQNs.get(jfo);
        
        if (result != null) {
            return result.toArray(new String[result.size()]);
        }
        
        return null;
    }

    public JavaFileObject[] divineSources(String fqn) {
        if (fqn == null || fqn.length() == 0 || misplacedSource2FQNs.isEmpty()) {
            return null;
        }

        fqn += "."; //fqn should always be a package name

        List<JavaFileObject> jfos = new LinkedList<JavaFileObject>();
        for (Map.Entry<JavaFileObject, List<String>> entry : misplacedSource2FQNs.entrySet()) {
            for (String s : entry.getValue()) {
                if (s.startsWith(fqn)) {
                    if (s.indexOf('.', fqn.length()) == -1) {
                        jfos.add(entry.getKey());
                        break;
                    }
                }
            }
        }
        
        return jfos.size() > 0 ? jfos.toArray(new JavaFileObject[jfos.size()]) : null;
    }
}

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
package org.netbeans.test.web.core.syntax;

import java.util.Map;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public class TestClassPathProvider implements ClassPathProvider {
    
    public TestClassPathProvider(Map<String, ClassPath> map) {
        this.myMap = map;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.java.classpath.ClassPathProvider#findClassPath(org.openide.filesystems.FileObject, java.lang.String)
     */
    public ClassPath findClassPath( FileObject file, String type ) {
        if (myMap != null) {
            return myMap.get(type);
        } else {
            return null;
        }
    }
    
    private Map<String, ClassPath> myMap;


}

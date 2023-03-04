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
package org.netbeans.modules.java.source;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Zezula
 */
public final class JavaFileFilterQuery {    
    
    private static JavaFileFilterImplementation unitTestFilter;

    private static Reference<FileObject> key;
    private static Reference<JavaFileFilterImplementation> value;
        
    private JavaFileFilterQuery() {
    }
    
    public static JavaFileFilterImplementation getFilter (FileObject fo) {
        assert fo != null;
        if (unitTestFilter != null) {
            return unitTestFilter;
        }
        synchronized (JavaFileFilterImplementation.class) {
            final FileObject _key = key == null ? null : key.get();
            final JavaFileFilterImplementation _value = value == null ? null : value.get();
            if (_key != null && _key.equals(fo) && _value != null) {
                return _value;
            }
        }
        Project p = FileOwnerQuery.getOwner(fo);
        if (p != null) {
            JavaFileFilterImplementation impl = p.getLookup().lookup(JavaFileFilterImplementation.class);
            if (impl != null) {
                synchronized (JavaFileFilterImplementation.class) {
                    key = new WeakReference<FileObject>(fo);
                    value = new WeakReference<JavaFileFilterImplementation>(impl);
                }
                return impl;
            }
        }
        return null;
    }
    
    
    static void setTestFileFilter(JavaFileFilterImplementation testFilter) {
        unitTestFilter = testFilter;
    }
    
}

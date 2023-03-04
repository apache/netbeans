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
package org.netbeans.modules.openide.loaders;

import java.io.IOException;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.FolderInstance;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;

/**
 * API trampoline to access package private methods in DataObject class.
 * 
 * @since 6.3
 * @author S. Aubrecht
 */
public abstract class DataObjectAccessor {
    
    public static DataObjectAccessor DEFAULT;
    
    static {
        // invokes static initializer of Item.class
        // that will assign value to the DEFAULT field above
        Class c = DataObject.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }
//        assert DEFAULT != null : "The DEFAULT field must be initialized";
    }
    
    public abstract DataObject copyRename( DataObject dob, DataFolder f, String name, String ext ) throws IOException;
    
    public abstract CookieSet getCookieSet( MultiDataObject dob );

    public abstract boolean isInstancesThread();

    public abstract void precreateInstances(FolderInstance fi);
    
    public abstract String getOrigName();
}

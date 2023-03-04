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
package org.netbeans.modules.languages.features;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.editor.fold.FoldType;


class Folds {
    
    private static Map<String,WeakReference<FoldType>> nameToFoldType = new HashMap<String,WeakReference<FoldType>>();
    
    static synchronized FoldType getFoldType (String name) {
        if (name == null) return null;
        WeakReference<FoldType> wr = nameToFoldType.get (name);
        FoldType ft = wr == null ? null : wr.get ();
        if (ft == null) {
            ft = new FoldType (name);
            nameToFoldType.put (name, new WeakReference<FoldType> (ft));
        }
        return ft;
    }
}


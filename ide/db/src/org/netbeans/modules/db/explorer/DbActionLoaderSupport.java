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

package org.netbeans.modules.db.explorer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import org.openide.util.Lookup;

/**
 * Supporting methods to work with the registered implementation of ActionLoader
 * 
 * @author David Van Couvering
 */
public class DbActionLoaderSupport {

    private DbActionLoaderSupport() {
    }

    public static List<Action> getAllActions() {
        List<Action> actions = new ArrayList<Action>();
        Collection loaders = Lookup.getDefault().lookupAll(DbActionLoader.class);
        for (Iterator<DbActionLoader> i = loaders.iterator(); i.hasNext();) {
            actions.addAll(i.next().getAllActions());
        }
        
        return actions;
    }

}

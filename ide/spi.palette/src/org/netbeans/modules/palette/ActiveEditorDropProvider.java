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

package org.netbeans.modules.palette;

import org.openide.ErrorManager;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Libor Kotouc
 */
class ActiveEditorDropProvider implements InstanceContent.Convertor<String,ActiveEditorDrop> {

    private static ActiveEditorDropProvider instance = new ActiveEditorDropProvider();

    /** Creates a new instance of ActiveEditorDropProvider */
    private ActiveEditorDropProvider() {
    }

    static ActiveEditorDropProvider getInstance() {
        return instance;
    }
    
    public Class<? extends ActiveEditorDrop> type(String obj) {
        //able to convert String instances only
        return ActiveEditorDrop.class;
    }

    public String id(String obj) {
        return obj;
    }

    public String displayName(String obj) {
        return obj;
    }

    public ActiveEditorDrop convert(String obj) {
        return getActiveEditorDrop(obj);
    }
    
    private ActiveEditorDrop getActiveEditorDrop(String instanceName) {

        ActiveEditorDrop drop = null;

        if (instanceName != null && instanceName.trim().length() > 0) {//we should try to instantiate item drop
            try {
                ClassLoader loader = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
                if (loader == null)
                    loader = getClass ().getClassLoader ();
                Class instanceClass = loader.loadClass (instanceName);
                drop = (ActiveEditorDrop)instanceClass.getDeclaredConstructor().newInstance();
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }

        return drop;
    }
    
}

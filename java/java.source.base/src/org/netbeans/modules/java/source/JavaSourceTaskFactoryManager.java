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

import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Lahoda
 */
public final class JavaSourceTaskFactoryManager {
    
    private static JavaSourceTaskFactoryManager INSTANCE;
    
    public static synchronized void register() {
        INSTANCE = new JavaSourceTaskFactoryManager();
    }
    
    private Lookup.Result<JavaSourceTaskFactory> factories;
    
    /** Creates a new instance of JavaSourceTaskFactoryManager */
    private JavaSourceTaskFactoryManager() {
        final RequestProcessor.Task updateTask = new RequestProcessor("JavaSourceTaskFactoryManager Worker", 1).create(new Runnable() {
            public void run() {
                update();
            }
        });
        
        factories = Lookup.getDefault().lookupResult(JavaSourceTaskFactory.class);
        factories.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent ev) {
                updateTask.schedule(0);
            }
        });
        
        update();
    }
    
    private void update() {
        for (JavaSourceTaskFactory f : factories.allInstances()) {
            ACCESSOR.fireChangeEvent(f);
        }
    }
    
    public static interface Accessor {
        public abstract void fireChangeEvent(JavaSourceTaskFactory f);
    }
    
    public static Accessor ACCESSOR;
    
}

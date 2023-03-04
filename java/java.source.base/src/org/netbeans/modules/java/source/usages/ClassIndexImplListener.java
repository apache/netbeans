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
package org.netbeans.modules.java.source.usages;

import java.util.EventListener;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;

/**
 *
 * @author Tomas Zezula
 */
public interface ClassIndexImplListener extends EventListener {

    /**
     * Called when the new declared types are added
     * into the {@link ClassIndexImpl}
     * @param event specifying the added types
     */
    public void typesAdded (ClassIndexImplEvent event);
    
    /**
     * Called when declared types are removed
     * from the {@link ClassIndexImpl}
     * @param event specifying the removed types
     */
    public void typesRemoved (ClassIndexImplEvent event);
        
    /**
     * Called when some declared types are changed.
     * @param event specifying the changed types
     */
    public void typesChanged (ClassIndexImplEvent event);

}

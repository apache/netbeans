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

package org.netbeans.modules.payara.common.nodes;

import java.util.HashMap;
import org.netbeans.modules.payara.common.utils.Util;
import org.netbeans.modules.payara.spi.Decorator;
import org.netbeans.modules.payara.spi.DecoratorFactory;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Peter Williams
 */
public final class DecoratorManager {

    private static volatile java.util.Map<String, Decorator> decoratorMap;
    
    private DecoratorManager() {
    }
    
    private static synchronized void initDecorators() {
        if(decoratorMap != null) {
            return;
        }
        
        // Find all decorator support, categorize by type.
        decoratorMap = new HashMap<String, Decorator>();
        for (DecoratorFactory decoratorFactory : 
                Lookups.forPath(Util.PF_LOOKUP_PATH).lookupAll(DecoratorFactory.class)) {
            java.util.Map<String, Decorator> map = decoratorFactory.getAllDecorators();
            decoratorMap.putAll(map);
        }
    }

    
    public static Decorator findDecorator(String type, Decorator defaultDecorator,boolean enabled) {
        if(decoratorMap == null) {
            initDecorators();
        }

        if (!enabled) {
            type = Decorator.DISABLED+type;
        }
        Decorator d = decoratorMap.get(type);
        return d != null ? d : defaultDecorator;
    }
    
}

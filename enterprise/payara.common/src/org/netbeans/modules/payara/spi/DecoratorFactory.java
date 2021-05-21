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

package org.netbeans.modules.payara.spi;

import java.util.Map;

/**
 *
 * @author Peter Williams
 */
public interface DecoratorFactory {
    
    /**
     * Indicates whether this decorator supports the specified type.  Type is
     * currently one of the strings returned by list-applications for segregating
     * applications.
     * 
     * @param type String indicating what type the item to be decorated is.
     * @return true if this decorator should be used with that type, false otherwise.
     */
    public boolean isTypeSupported(String type);
    
    /**
     * Obtain a decorator object for the specified type.
     * 
     * @param type String indicating what type the item to be decorated is.
     * @return decorator instance
     */
    public Decorator getDecorator(String type);

    /**
     * Returns a map of all decorators supported by this factory, indexed by type.
     * 
     * @return map of supported decorators
     */
    public Map<String, Decorator> getAllDecorators();
    
}

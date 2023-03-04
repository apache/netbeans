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

package org.netbeans.spi.project.support.ant;

import java.util.Map;
import javax.swing.event.ChangeListener;

/**
 * Provides a set of Ant property definitions that might be evaluated in
 * some context.
 * <p>
 * This interface defines no independent thread safety, but in typical usage
 * it will be used with the project manager mutex. Changes should be fired
 * synchronously.
 * @author Jesse Glick
 */
public interface PropertyProvider {

    /**
     * Get all defined properties.
     * The values might contain Ant-style property references.
     * @return all properties defined in this block
     */
    Map<String,String> getProperties();
    
    /**
     * Add a change listener.
     * When the set of available properties, or some of the values, change,
     * this listener should be notified.
     * @param l a listener to add
     */
    void addChangeListener(ChangeListener l);
    
    /**
     * Remove a change listener.
     * @param l a listener to remove
     */
    void removeChangeListener(ChangeListener l);
    
}

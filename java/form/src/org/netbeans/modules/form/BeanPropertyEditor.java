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

package org.netbeans.modules.form;

import org.openide.nodes.Node;

/**
 * This interface can be implemented by property editors to express that the
 * current property value in the property editor is a
 * bean with its properties and to make these properties accessible.
 *
 * @author Tomas Stupka
 *
 */
public interface BeanPropertyEditor {

    /**
     * @return true if the current value is a bean not directly
     *         suported by the editor
     */
    public boolean valueIsBeanProperty();

    /**
    * Called to initialize the editor with a specified type. If succesfull,
    * the value should be available via the getValue method.     
    * An Exception should be thrown when the value cannot be set.
     
    * @param type class type to initialize the editor with
    * @exception Exception thrown when the value cannot be set
    */
    public void intializeFromType(Class type) throws Exception;
    
    /**
     * @return properties from the current value
     */
    public Node.Property[] getProperties();
    
}

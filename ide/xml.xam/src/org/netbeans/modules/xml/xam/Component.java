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

package org.netbeans.modules.xml.xam;

import java.util.Collection;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 * A component in model.
 *
 */
public interface Component<C extends Component> {
    
    /**
     * @return parent component.
     */
    C getParent();
    
    /**
     * @return the unmodifiable list of child components.
     */
    List<C> getChildren();

    /**
     * @param type Interested children type to return.
     * @return unmodifiable list of directly contained component of specified type.
     */
    <T extends C> List<T> getChildren(Class<T> type);
    
    /**
     * @param types Interested children type to return.
     * @return unmodifiable list of directly contained component of specified types.
     */
    List<C> getChildren(Collection<Class<? extends C>> types);
    
    /**
     * @return the model where this element is being used or null if not
     * currently part of a model. 
     */
    @CheckForNull Model getModel();

    /**
     * Returns a copy of this component for adding into the given parent component.
     */
    Component copy(C parent);
    
    /**
     * Returns true if given component can be added as this component child.
     */
    boolean canPaste(Component child);

}

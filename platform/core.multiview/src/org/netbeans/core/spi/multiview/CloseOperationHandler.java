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

package org.netbeans.core.spi.multiview;


/**
 * Handles closing of the MultiView component globally. Each opened {@link org.netbeans.core.spi.multiview.MultiViewElement}
 * creates a {@link org.netbeans.core.spi.multiview.CloseOperationState} instance to notify the environment of it's internal state.
 *
 * @author  Milos Kleint
 */
public interface CloseOperationHandler {

    /**
     * Perform the closeOperation on the opened elements in the multiview topcomponent.
     * Can resolve by itself just based on the states of the elements or ask the user for
     * the decision.
     * @param elements {@link org.netbeans.core.spi.multiview.CloseOperationState} instances of {@link org.netbeans.core.spi.multiview.MultiViewElement}s that cannot be
     * closed and require resolution.
     * @return true if component can be close, false if it shall remain opened.
     */
    boolean resolveCloseOperation(CloseOperationState[] elements);
    
}

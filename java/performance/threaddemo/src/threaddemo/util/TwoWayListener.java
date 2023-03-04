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

package threaddemo.util;

import java.util.EventListener;

/**
 * Listener for changes in the status of a two-way support.
 * @author Jesse Glick
 * @see TwoWaySupport
 */
public interface TwoWayListener<DM, UMD, DMD> extends EventListener {

    /**
     * Called when a new derived value has been produced.
     * May have been a result of a synchronous or asynchronous derivation.
     * @param evt the associated event with more information
     */
    void derived(TwoWayEvent.Derived<DM, UMD, DMD> evt);

    /**
     * Called when a derived model has been invalidated.
     * @param evt the associated event with more information
     */
    void invalidated(TwoWayEvent.Invalidated<DM, UMD, DMD> evt);
    
    /**
     * Called when the derived model was changed and the underlying model recreated.
     * @param evt the associated event with more information
     */
    void recreated(TwoWayEvent.Recreated<DM, UMD, DMD> evt);
    
    /**
     * Called when changes in the underlying model were clobbered by changes to
     * the derived model.
     * @param evt the associated event with more information
     */
    void clobbered(TwoWayEvent.Clobbered<DM, UMD, DMD> evt);
    
    /**
     * Called when the reference to the derived model was garbage collected.
     * @param evt the associated event
     */
    void forgotten(TwoWayEvent.Forgotten<DM, UMD, DMD> evt);
    
    /**
     * Called when an attempted derivation failed with an exception.
     * The underlying model is thus considered to be in an inconsistent state.
     * @param evt the associated event with more information
     */
    void broken(TwoWayEvent.Broken<DM, UMD, DMD> evt);
    
}

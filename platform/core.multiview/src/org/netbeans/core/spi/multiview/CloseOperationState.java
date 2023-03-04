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

import javax.swing.Action;


/**
 * instances of this class describe the MultiViewElement's state when the component is
 * about to be closed. (See {@link org.netbeans.core.spi.multiview.MultiViewElement#canCloseElement})
 * New instances are created by {@link org.netbeans.core.spi.multiview.MultiViewFactory#createUnsafeCloseState} factory method.
 */
public final class CloseOperationState {

    /**
     * Singleton instance of a close operation state, to be used whenever {@link org.netbeans.core.spi.multiview.MultiViewElement} is in consistent state
     * and can be safely closed.
     */
    public static final CloseOperationState STATE_OK = MultiViewFactory.createSafeCloseState();
    
    private boolean canClose;
    private String id;
    private Action proceedAction;
    private Action discardAction;
    
    
    CloseOperationState(boolean close, String warningId, Action proceed, Action discard) {
        canClose = close;
        proceedAction = proceed;
        discardAction = discard;
        id = warningId;
    }
    
    /**
     * The return value denotes wheather the {@link org.netbeans.core.spi.multiview.MultiViewElement} can be closed or not.
     * @return can close element or not
     */
    
    public boolean canClose() {
        return canClose;
    }
    
    /**
     * A preferably unique id of the reason why the element cannot be closed.
     * {@link org.netbeans.core.spi.multiview.CloseOperationHandler} implementation can use it when deciding about UI shown or action taken.
     */
    
    public String getCloseWarningID() {
        return id;
    }
    
    /**
     * Action is used when user wants to complete the closing of the component without loosing changed data.
     * Used by {@link org.netbeans.core.spi.multiview.CloseOperationHandler}.
     * @return action which will be triggered when user confirms changes
     */
    public Action getProceedAction() {
        return proceedAction;
    }

    /**
     * Action is used when user wants to complete the closing of the component and discard any changes.
     * Used by {@link org.netbeans.core.spi.multiview.CloseOperationHandler}.
     * @return action which will be triggered when user discards changes
     */
    
    public Action getDiscardAction() {
        return discardAction;
    }
    
    
}


/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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


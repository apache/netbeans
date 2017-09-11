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

package org.netbeans.core.windows.view.ui.slides;

import java.awt.Component;
import java.awt.Rectangle;

/**
 * Factory for possible types of sliding operations with asociated effect.
 *
 * Operations are designed to be sent to winsys to be runned.
 *
 * @author Dafe Simonek
 */
public final class SlideOperationFactory {

    private static final SlidingFx slideInFx = new ScaleFx(0.1f, 0.9f, true);
    private static final SlidingFx slideOutFx = new ScaleFx(0.9f, 0.1f, false);
    private static final SlidingFx slideIntoEdgeFx = new ScaleFx(0.9f, 0.1f, false);
    private static final SlidingFx slideIntoDesktopFx = new ScaleFx(1.0f, 1.0f, true);

    /** true when slide effects should be applied, false otherwise */
    static final boolean EFFECTS_ENABLED = Boolean.getBoolean("nb.winsys.sliding.effects"); //NOI18N
    
    private SlideOperationFactory() {
        // no need to instantiate
    }
    
    public static SlideOperation createSlideIn(Component component, 
        int orientation, boolean useEffect, boolean requestActivation) {
            
        SlideOperation result = new SlideOperationImpl(SlideOperation.SLIDE_IN, 
                component, orientation, useEffect && EFFECTS_ENABLED ? slideInFx : null,
                requestActivation);
                
        return result;
    }

    public static SlideOperation createSlideOut(Component component, 
        int orientation, boolean useEffect, boolean requestActivation) {
            
        SlideOperation result = new SlideOperationImpl(SlideOperation.SLIDE_OUT, 
                component, orientation, useEffect && EFFECTS_ENABLED ? slideOutFx : null,
                requestActivation);
                
        return result;
    }
    
    public static SlideOperation createSlideIntoEdge(Component component, 
        String side, boolean useEffect) {
            
        SlideOperation result = new SlideOperationImpl(SlideOperation.SLIDE_INTO_EDGE,
                component, side, useEffect && EFFECTS_ENABLED ? slideIntoEdgeFx : null, false);
                
        return result;
    }
    
    public static SlideOperation createSlideIntoDesktop(Component component, 
        int orientation, boolean useEffect) {
            
        SlideOperation result = new SlideOperationImpl(SlideOperation.SLIDE_INTO_DESKTOP,
                component, orientation, useEffect && EFFECTS_ENABLED ? slideIntoDesktopFx : null, false);
                
        return result;
    }
    
    public static SlideOperation createSlideResize(Component component, int orientation) {
        SlideOperation result = new SlideOperationImpl(SlideOperation.SLIDE_RESIZE,
                component, orientation, null, false);
                
        return result;
    }
    
    public static SlideOperation createSlideResize(Component component, String side) {
        return createSlideResize( component, SlideOperationImpl.side2Orientation( side ) );
    }
}

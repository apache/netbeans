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


package org.netbeans.core.windows.view;


import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.model.ModelElement;
import org.openide.windows.TopComponent;

import java.awt.*;
import org.netbeans.core.windows.view.dnd.TopComponentDraggable;


/**
 * Class which handles controller requests.
 *
 * @author  Peter Zavadsky
 */
public interface ControllerHandler {

    public void userActivatedMode(ModeImpl mode);

    public void userActivatedModeWindow(ModeImpl mode);

    public void userActivatedEditorWindow();
    
    public void userActivatedTopComponent(ModeImpl mode, TopComponent selected);
    
    public void userResizedMainWindow(Rectangle bounds);
    
    public void userResizedEditorArea(Rectangle bounds);
    
    public void userResizedModeBounds(ModeImpl mode, Rectangle bounds);
    
    public void userChangedFrameStateMainWindow(int frameState);
    
    public void userChangedFrameStateEditorArea(int frameState);
    
    public void userChangedFrameStateMode(ModeImpl mode, int frameState);
    
    public void userChangedSplit( ModelElement[] snapshots, double[] splitWeights );
    
    public void userClosedTopComponent(ModeImpl mode, TopComponent tc);
    
    public void userClosedMode(ModeImpl mode);
    
    // Helpers>>
    public void userResizedMainWindowBoundsSeparatedHelp(Rectangle bounds);
    
    public void userResizedEditorAreaBoundsHelp(Rectangle bounds);
    
    public void userResizedModeBoundsSeparatedHelp(ModeImpl mode, Rectangle bounds);
    // Helpers<<
    
    // DnD>>
    public void userDroppedTopComponents(ModeImpl mode, TopComponentDraggable draggable);
    
    public void userDroppedTopComponents(ModeImpl mode, TopComponentDraggable draggable, int index);
    
    public void userDroppedTopComponents(ModeImpl mode, TopComponentDraggable draggable, String side);
    
    public void userDroppedTopComponentsIntoEmptyEditor(TopComponentDraggable draggable);
    
    public void userDroppedTopComponentsAround(TopComponentDraggable draggable, String side);
    
    public void userDroppedTopComponentsAroundEditor(TopComponentDraggable draggable, String side);
    
    public void userDroppedTopComponentsIntoFreeArea(TopComponentDraggable draggable, Rectangle bounds);
    // DnD<<

    // undock/dock
    public void userUndockedTopComponent(TopComponent tc, ModeImpl mode);

    public void userDockedTopComponent(TopComponent tc, ModeImpl mode);

    // Sliding>>
    public void userEnabledAutoHide(TopComponent tc, ModeImpl source, String target);
    
    public void userDisabledAutoHide(TopComponent tc, ModeImpl source);
    
    public void userResizedSlidingMode(ModeImpl mode, Rectangle rect);
    // Sliding<<
    
}


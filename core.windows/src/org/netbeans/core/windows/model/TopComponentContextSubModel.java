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


package org.netbeans.core.windows.model;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.SplitConstraint;
import org.openide.windows.TopComponent;

/**
 * Model which stores context of TopComponents in one mode. Context consists
 * of mode and constraints info of previous container TopComponent was part of.
 *
 * This sub model is not thread safe. It is supposed to be just part of DefaultModeModel
 * which is responsible for the synch.
 *
 * @author  Dafe Simonek
 */
final class TopComponentContextSubModel {
    
    private static final class Context {
        // XXX we should use weak reference for holding mode, to let it vanish
        ModeImpl mode;
        //tab index
        int index = -1;
        SplitConstraint[] constraints;
    } // end of Context

    /** Mapping <TopComponentID, Context> between top component and context holding
     its previous location */
    private final Map<String, Context> tcID2Contexts = new HashMap<String, Context> (10);
    
    public TopComponentContextSubModel() {
    }

    public void setTopComponentPreviousConstraints(String tcID, SplitConstraint[] constraints) {
        Context context = tcID2Contexts.get(tcID);
        if (context == null) {
            context = new Context();
            tcID2Contexts.put(tcID, context);
        }
        context.constraints = constraints;
    }
    
    public void setTopComponentPreviousMode(String tcID, ModeImpl mode, int index) {
        Context context = tcID2Contexts.get(tcID);
        if (context == null) {
            context = new Context();
            tcID2Contexts.put(tcID, context);
        }
        context.mode = mode;
        context.index = index;
    }
    
    public SplitConstraint[] getTopComponentPreviousConstraints(String tcID) {
        Context context = tcID2Contexts.get(tcID);
        return context == null ? null : context.constraints;
    }
    
    public ModeImpl getTopComponentPreviousMode(String tcID) {
        Context context = tcID2Contexts.get(tcID);
        return context == null ? null : context.mode;
    }
    
    public int getTopComponentPreviousIndex(String tcID) {
        Context context = tcID2Contexts.get(tcID);
        return context == null ? -1 : context.index;
    }
}

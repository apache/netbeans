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
package org.netbeans.api.debugger.jpda;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.sun.jdi.request.StepRequest;

/**
 * Represents one JPDA step.
 *
 * @author Roman Ondruska
 */
public abstract class JPDAStep {
    private int size;
    private int depth;
    private boolean hidden;
    /** Associated JPDA debugger */
    protected JPDADebugger debugger;
    private PropertyChangeSupport pcs;

    /** Step into any newly pushed frames */
    public static final int STEP_INTO   =   StepRequest.STEP_INTO;
    /** Step over any newly pushed frames */
    public static final int STEP_OVER   =   StepRequest.STEP_OVER;
    /** Step out of the current frame */
    public static final int STEP_OUT    =   StepRequest.STEP_OUT;
    /** Step to the next location on a different line */
    public static final int STEP_LINE   =   StepRequest.STEP_LINE;
    /** Step to the next available operation */
    public static final int STEP_OPERATION = 10;
    /** Step to the next available location */
    public static final int STEP_MIN    =   StepRequest.STEP_MIN;
    /** Property fired when the step is executed */
    public static final String PROP_STATE_EXEC = "exec";
    
    /** Constructs a JPDAStep for given {@link org.netbeans.api.debugger.jpda.JPDADebugger}, 
     *  size {@link #STEP_LINE}, {@link #STEP_MIN} 
     *  and depth {@link #STEP_OUT}, {@link #STEP_INTO}.
     * 
     *  @param debugger an associated JPDADebuger
     *  @param size step size
     *  @param depth step depth
     */
    public JPDAStep(JPDADebugger debugger, int size, int depth) {
        this.size = size;
        this.depth = depth;
        this.debugger = debugger;
        this.hidden = false;
        pcs = new PropertyChangeSupport(this);
    }
   
    /** Sets the hidden property. */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
    
    /**
     * Returns hidden property of the step.
     *
     * @return hidden property 
     */
    public boolean getHidden() {
        return hidden;
    }
    
    /** Sets size of the step.
     *  @param size step size
     */
    public void setSize(int size) {
        this.size = size;
    }
    
    /**
     * Returns size of the step.
     *
     * @return step size
     */
    public int getSize() {
        return size;
    }
    
    /** Sets depth of the step.
     *  @param depth step depth
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }
    
    /**
     * Returns depth of the step.
     *
     * @return step depth
     */
    public int getDepth() {
        return depth;
    }
    
    /** Adds the step request to the associated
     *  {@link org.netbeans.api.debugger.jpda.JPDADebugger}.
     *  Method is not synchronized.
     *
     *  @param tr associated thread
     */
    public abstract void addStep(JPDAThread tr);
    
    /**
    * Adds property change listener.
    *
    * @param l new listener.
    */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
    * Removes property change listener.
    *
    * @param l removed listener.
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }

    /**
    * Adds property change listener.
    *
    * @param l new listener.
    */
    public void addPropertyChangeListener (String propertyName, PropertyChangeListener l) {
        pcs.addPropertyChangeListener (propertyName, l);
    }

    /**
    * Removes property change listener.
    *
    * @param l removed listener.
    */
    public void removePropertyChangeListener (String propertyName, PropertyChangeListener l) {
        pcs.removePropertyChangeListener (propertyName, l);
    }
    
    /**
    * Fires property change.
    */
    protected void firePropertyChange (String name, Object o, Object n) {
        pcs.firePropertyChange (name, o, n);
    }
    
}

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
package org.netbeans.api.debugger.jpda;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;

import com.sun.jdi.request.StepRequest;

import org.netbeans.api.debugger.Properties;

/**
 * Represents one JPDA step.
 *
 * @author Roman Ondruska
 */
public abstract class JPDAStep {
    private int size;
    private int depth;
    private boolean hidden;
    private String[] classFilters;
    private boolean stepThroughFilters;
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
        Properties p = Properties.getDefault().getProperties("debugger.options.JPDA"); // NOI18N
        boolean useStepFilters = p.getBoolean("UseStepFilters", true);
        this.stepThroughFilters = useStepFilters && p.getBoolean("StepThroughFilters", false);
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
    
    /**
     * Add additional class exclusion filters to this step.
     * The provided list of class filters is combined with the current
     * {@link SmartSteppingFilter#getExclusionPatterns()} just for this step.
     *
     * @param classFilters A list of class filters
     * @since 3.19
     */
    public void addSteppingFilters(String... classFilters) {
        if (this.classFilters == null) {
            this.classFilters = classFilters;
        } else {
            int cfl = this.classFilters.length;
            this.classFilters = Arrays.copyOf(this.classFilters, cfl + classFilters.length);
            System.arraycopy(classFilters, 0, this.classFilters, cfl, classFilters.length);
        }
    }
    
    /**
     * Get the additional exclusion filters of this step.
     *
     * @return A list of exclusion patterns, or <code>null</code> when no additional
     * filters are provided
     * @since 3.19
     */
    public String[] getSteppingFilters() {
        return classFilters;
    }
    
    /**
     * Set whether this step is stepping through exclusion filters, or not.
     * The default value is taken from the option property.
     *
     * @param stepThroughFilters <code>true</code> to step through the filters,
     *        <code>false</code> otherwise
     * @since 3.19
     */
    public void setStepThroughFilters(boolean stepThroughFilters) {
        this.stepThroughFilters = stepThroughFilters;
    }
    
    /**
     * Test whether this step is stepping through the exclusion filters.
     *
     * @return <code>true</code> to step through the filters,
     *         <code>false</code> otherwise
     * @since 3.19
     */
    public boolean isStepThroughFilters() {
        return stepThroughFilters;
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

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

package org.netbeans.spi.debugger.jpda;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.Objects;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.modules.debugger.jpda.apiregistry.DebuggerProcessor;
import org.netbeans.spi.debugger.ContextAwareService;
import org.netbeans.spi.debugger.ContextAwareSupport;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * Listens on stepping engine and defines classes / places the debugger can
 * stop in.
 *
 * @author   Jan Jancura
 */
public abstract class SmartSteppingCallback {


    /**
     * Defines default set of smart stepping filters. Method is called when
     * a new JPDA debugger session is created.
     *
     * @param f a filter to be initialized
     */
    public abstract void initFilter (SmartSteppingFilter f);
    
    /**
     * This method is called during stepping through debugged application.
     * The execution is stopped when all registered <code>SmartSteppingCallback</code>s
     * returns true.
     * <p>
     * The {@link SmartSteppingFilter} instance is unique per step. Any changes to it applies
     * to one ongoing step only. Use {@link JPDADebugger#getSmartSteppingFilter()} to make
     * persistent changes.
     *
     * @param thread contains all available information about current position
     *        in debugged application
     * @param f a filter, unique to the ongoing step
     * @return true if execution should be stopped on the current position
     */
    public abstract boolean stopHere (ContextProvider lookupProvider, JPDAThread thread, SmartSteppingFilter f);

    /**
     * This is an enhanced version of former {@link #stopHere(org.netbeans.spi.debugger.ContextProvider, org.netbeans.api.debugger.jpda.JPDAThread, org.netbeans.api.debugger.jpda.SmartSteppingFilter)}
     * method, which is called during stepping through debugged application.
     * The <code>frame</code> argument allows this method to be called for specific stack frames,
     * to check if the execution could be suspended there. This is valuable e.g. for step out.
     * When a top frame is provided, this method can suggest a specific step to continue with,
     * in case it's not possible to stop at the given location.
     * <p>
     * The {@link SmartSteppingFilter} instance is unique per step. Any changes to it applies
     * to one ongoing step only. Register a {@link SmartSteppingFilter} implementation
     * to make persistent changes.
     * 
     * The default implementation calls {@link #stopHere(org.netbeans.spi.debugger.ContextProvider, org.netbeans.api.debugger.jpda.JPDAThread, org.netbeans.api.debugger.jpda.SmartSteppingFilter)}
     * when called with a top frame and throws an {@link UnsupportedOperationException} otherwise.
     * 
     * @param lookupProvider The debugger services lookup
     * @param frame The frame in question
     * @param f a filter, unique to the ongoing step
     * @return whether the debugger can stop at this location, or whether it should continue with a step.
     * @since 3.5
     */
    public StopOrStep stopAt(ContextProvider lookupProvider, CallStackFrame frame, SmartSteppingFilter f) {
        int depth = frame.getFrameDepth();
        if (depth > 0) {
            throw new UnsupportedOperationException("Not supporting frames with depth > 0");
        }
        boolean stop = stopHere(lookupProvider, frame.getThread(), f);
        return stop ? StopOrStep.stop() : StopOrStep.skip();
    }
    
    /**
     * Information about a possibility to stop at the given location,
     * or suggestion to perform a step.
     * Used by {@link #stopAt(org.netbeans.spi.debugger.ContextProvider, org.netbeans.api.debugger.jpda.CallStackFrame, org.netbeans.api.debugger.jpda.SmartSteppingFilter)}
     * @since 3.5
     */
    public static final class StopOrStep {
        
        private static final StopOrStep STOP = new StopOrStep(true, 0, 0);
        private static final StopOrStep SKIP = new StopOrStep(false, 0, 0);
        
        private final boolean stop;
        private final int stepSize;
        private final int stepDepth;
        
        /**
         * Express the possibility to stop.
         * @return information about a possibility to stop at the given location.
         */
        public static StopOrStep stop() {
            return STOP;
        }
        
        /**
         * Express the necessity to skip the given location,
         * using whatever the default debugger action is at the moment.
         * @return information about the necessity to skip the given location.
         */
        public static StopOrStep skip() {
            return SKIP;
        }
        
        /**
         * Express the necessity to perform a step at the given location.
         * @param stepSize the step size,
         *  one of {@link #JPDAStep.STEP_LINE} or {@link #JPDAStep.STEP_MIN},
         *  or <code>0</code> for the default size.
         * @param stepDepth the step depth,
         *  one of {@link #JPDAStep.STEP_INTO}, {@link #JPDAStep.STEP_OVER},
         *  {@link #JPDAStep.STEP_OUT}, or <code>0</code> for the default depth.
         * @return the step information instance.
         * throws {@link IllegalArgumentException} when the size or depth is wrong.
         */
        public static StopOrStep step(int stepSize, int stepDepth) {
            return new StopOrStep(false, stepSize, stepDepth);
        }
        
        private StopOrStep(boolean stop, int stepSize, int stepDepth) {
            this.stop = stop;
            switch (stepSize) {
                case 0:
                case JPDAStep.STEP_MIN:
                case JPDAStep.STEP_LINE:
                    // O.K.
                    break;
                default:
                    throw new IllegalArgumentException("Wrong step size: "+stepSize);
            }
            switch (stepDepth) {
                case 0:
                case JPDAStep.STEP_INTO:
                case JPDAStep.STEP_OVER:
                case JPDAStep.STEP_OUT:
                    // O.K.
                    break;
                default:
                    throw new IllegalArgumentException("Wrong step depth: "+stepDepth);
            }
            this.stepSize = stepSize;
            this.stepDepth = stepDepth;
        }
        
        /**
         * Whether this is a possibility to stop.
         * If yes, values of step size and step depth are irrelevant.
         * @return <code>true</code> if it's possible to stop, <code>false</code> otherwise.
         */
        public boolean isStop() {
            return stop;
        }
        
        /**
         * Get the step size.
         * @return One of {@link #JPDAStep.STEP_LINE} or {@link #JPDAStep.STEP_MIN},
         *  or <code>0</code> for the default size.
         */
        public int getStepSize() {
            return stepSize;
        }
        
        /**
         * Get the step depth.
         * @return One of {@link #JPDAStep.STEP_INTO}, {@link #JPDAStep.STEP_OVER},
         *  {@link #JPDAStep.STEP_OUT}, or <code>0</code> for the default depth.
         */
        public int getStepDepth() {
            return stepDepth;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StopOrStep)) {
                return false;
            }
            StopOrStep ss = (StopOrStep) obj;
            return stop == ss.stop &&
                   stepSize == ss.stepSize &&
                   stepDepth == ss.stepDepth;
        }

        @Override
        public int hashCode() {
            return Objects.hash(stop, stepSize, stepDepth);
        }

        @Override
        public String toString() {
            return "StopOrStep["+stop+","+stepSize+","+stepDepth+"]";
        }
        
    }
    
    /**
     * Declarative registration of a SmartSteppingCallback implementation.
     * By marking the implementation class with this annotation,
     * you automatically register that implementation for use by debugger.
     * The class must be public and have a public constructor which takes
     * no arguments or takes {@link ContextProvider} as an argument.
     *
     * @author Martin Entlicher
     * @since 2.19
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE})
    public @interface Registration {
        /**
         * An optional path to register this implementation in.
         */
        String path() default "";

    }

    static class ContextAware extends SmartSteppingCallback implements ContextAwareService<SmartSteppingCallback> {

        private String serviceName;

        private ContextAware(String serviceName) {
            this.serviceName = serviceName;
        }

        public SmartSteppingCallback forContext(ContextProvider context) {
            return (SmartSteppingCallback) ContextAwareSupport.createInstance(serviceName, context);
        }

        @Override
        public void initFilter(SmartSteppingFilter f) {
            assert false;
        }

        @Override
        public boolean stopHere(ContextProvider lookupProvider, JPDAThread thread, SmartSteppingFilter f) {
            assert false;
            throw new UnsupportedOperationException("Not supported.");
        }
        
        /**
         * Creates instance of <code>ContextAwareService</code> based on layer.xml
         * attribute values
         *
         * @param attrs attributes loaded from layer.xml
         * @return new <code>ContextAwareService</code> instance
         */
        static ContextAwareService createService(Map attrs) throws ClassNotFoundException {
            String serviceName = (String) attrs.get(DebuggerProcessor.SERVICE_NAME);
            return new ContextAware(serviceName);
        }

    }

}


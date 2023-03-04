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
package org.netbeans.modules.debugger.jpda.actions;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.spi.debugger.jpda.SmartSteppingCallback;


/**
 * Loads all different SmartSteppingListeners and delegates to them.
 *
 * @author  Jan Jancura
 */
public final class CompoundSmartSteppingListener extends SmartSteppingCallback {

    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.step"); // NOI18N

    private List<? extends SmartSteppingCallback> smartSteppings;
    private final ContextProvider lookupProvider;
    
    public CompoundSmartSteppingListener (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        SmartSteppingFilter smartSteppingFilter = lookupProvider.lookupFirst(null, SmartSteppingFilter.class);
        initFilter (smartSteppingFilter);
    }
    
    @Override
    public void initFilter (SmartSteppingFilter filter) {
        // init list of smart stepping listeners
        smartSteppings = lookupProvider.lookup(null, SmartSteppingCallback.class);
        for (SmartSteppingCallback ss : smartSteppings) {
            ss.initFilter(filter);
        }
    }
    
    /**
     * Asks all SmartSteppingListener listeners if execution should stop on the
     * current place represented by JPDAThread.
     */
    @Override
    public boolean stopHere (
        ContextProvider lookupProvider, 
        JPDAThread t, 
        SmartSteppingFilter smartSteppingFilter
    ) {
        if (logger.isLoggable(Level.FINE))
            logger.fine("\nSS  CompoundSmartSteppingListener.stopHere? : " +
                t.getClassName () + '.' +
                t.getMethodName () + ':' +
                t.getLineNumber (null)
            );
        
        boolean stop = true;
        for (SmartSteppingCallback ss : smartSteppings) {
            boolean sh = ss.stopHere (lookupProvider, t, smartSteppingFilter);
            stop = stop && sh;
            if (logger.isLoggable(Level.FINE))
                logger.fine("SS    " + ss.getClass () + " = " + sh);
        }
        return stop;
    }

    @Override
    public StopOrStep stopAt(ContextProvider lookupProvider,
                                CallStackFrame frame,
                                SmartSteppingFilter f) {
        if (logger.isLoggable(Level.FINE))
            logger.fine("\nSS  CompoundSmartSteppingListener.canStopAt? : " +
                frame.getClassName () + '.' +
                frame.getMethodName () + ':' +
                frame.getLineNumber (null)
            );
        StopOrStep ss = null;
        for (SmartSteppingCallback ssc : smartSteppings) {
            StopOrStep s = ssc.stopAt(lookupProvider, frame, f);
            if (ss == null) {
                ss = s;
            } else {
                if (!ss.equals(s)) {
                    boolean stop = ss.isStop() && s.isStop();
                    int ssi = ss.getStepSize();
                    int ssd = ss.getStepDepth();
                    int si = s.getStepSize();
                    int sd = s.getStepDepth();
                    int stepSize;
                    int stepDepth;
                    if (ssi == 0) {
                        stepSize = si;
                    } else if (si == 0) {
                        stepSize = ssi;
                    } else {
                        stepSize = Math.max(ssi, si); // The size is negative, the greater is the shorter
                    }
                    if (ssd == 0) {
                        stepDepth = sd;
                    } else if (sd == 0) {
                        stepDepth = ssd;
                    } else {
                        stepDepth = Math.min(ssd, sd); // The depth is positive, the smaller is hit sooner
                    }
                    if (stop || stepSize == 0 && stepDepth == 0) {
                        ss = (stop) ? StopOrStep.stop() : StopOrStep.skip();
                    } else {
                        ss = StopOrStep.step(stepSize, stepDepth);
                    }
                }
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("SS    " + ssc.getClass () + " = " + s);
            }
        }
        if (ss == null) {
            ss = StopOrStep.stop();
        }
        logger.log(Level.FINE, "SS  stop or step: {0}", ss);
        return ss;
    }
    
}


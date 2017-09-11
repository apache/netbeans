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


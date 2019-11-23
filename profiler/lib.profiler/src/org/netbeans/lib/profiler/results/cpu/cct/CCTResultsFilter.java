/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.profiler.results.cpu.cct;

import java.util.Collection;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.MarkedCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.ThreadCPUCCTNode;
import org.netbeans.lib.profiler.marker.Mark;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.profiler.results.RuntimeCCTNodeProcessor;


/**
 *
 * @author Jaroslav Bachorik
 */
public final class CCTResultsFilter extends RuntimeCCTNodeProcessor.PluginAdapter {
    //~ Inner Interfaces ---------------------------------------------------------------------------------------------------------

    public static interface Evaluator {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        boolean evaluate(Mark mark);
    }
    
    public static interface EvaluatorProvider {
        Set/*<Evaluator>*/ getEvaluators();
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final Logger LOGGER = Logger.getLogger(CCTResultsFilter.class.getName());

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Set<Evaluator> evaluators = null;
    private Set evaluatorProviders = new HashSet();

    private Stack passFlagStack;
    private boolean passingFilter;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of CategoryFilter */
    public CCTResultsFilter() {
        evaluators = new HashSet<>();
        passFlagStack = new Stack();
        doReset();
    }

    public void setEvaluators(Collection evaluatorProviders) {
        this.evaluatorProviders.clear();
        this.evaluatorProviders.addAll(evaluatorProviders);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    public synchronized boolean passesFilter() {
        return passingFilter;
    }

    @Override
    public void onStart() {
        evaluators.clear();
        
        for(Iterator iter = evaluatorProviders.iterator();iter.hasNext();) {
            evaluators.addAll(((EvaluatorProvider)iter.next()).getEvaluators());
        }
    }

    @Override
    public void onStop() {
        evaluators.clear();
    }

    public void reset() {
        doReset();
    }

    @Override
    public void onNode(ThreadCPUCCTNode node) {
        LOGGER.finest("visiting thread node");
        passFlagStack.push(Boolean.valueOf(passingFilter));
        passingFilter = true;

        for (Iterator iter = evaluators.iterator(); iter.hasNext();) {
            Evaluator evaluator = (Evaluator) iter.next();
            passingFilter = passingFilter && evaluator.evaluate(Mark.DEFAULT);
        }

        LOGGER.log(Level.FINEST, "Evaluator result: {0}", passingFilter);
    }
    
    @Override
    public void onNode(MarkedCPUCCTNode node) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Entering a node marked {0}", node.getMark().getId()); // NOI18N
        }

        passFlagStack.push(Boolean.valueOf(passingFilter));
        passingFilter = true;

        for (Iterator iter = evaluators.iterator(); iter.hasNext();) {
            Evaluator evaluator = (Evaluator) iter.next();
            passingFilter = passingFilter && evaluator.evaluate(node.getMark());
        }
    }
    
    @Override
    public void onBackout(ThreadCPUCCTNode node) {
        if (!passFlagStack.isEmpty()) {
            passingFilter = ((Boolean) passFlagStack.pop()).booleanValue();
        }
    }
    
    public void onBackout(MarkedCPUCCTNode node) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Leaving a node marked {0}", node.getMark().getId()); // NOI18N
        }

        if (!passFlagStack.isEmpty()) {
            passingFilter = ((Boolean) passFlagStack.pop()).booleanValue();
        }
    }    

    private void doReset() {
        passingFilter = false;
        passFlagStack.clear();
    }
}

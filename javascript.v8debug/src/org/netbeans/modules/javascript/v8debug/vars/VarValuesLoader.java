/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.v8debug.vars;

import java.awt.EventQueue;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.commands.Lookup;
import org.netbeans.lib.v8debug.vars.V8Value;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
public class VarValuesLoader {
    
    private static final Logger LOG = Logger.getLogger(VarValuesLoader.class.getName());
    
    private static final int THROUGHPUT = 3;
    private static final RequestProcessor RP = new RequestProcessor(VarValuesLoader.class.getName(), THROUGHPUT);
    
    private final V8Debugger dbg;
    //private final Queue<Variable> varQueue = new ArrayDeque<>();
    private final Set<Variable> varQueue = new LinkedHashSet<>();
    private final Queue<Loader> loadTasks = new ArrayDeque<>(THROUGHPUT);
    
    public VarValuesLoader(V8Debugger dbg) {
        this.dbg = dbg;
        for (int i = 0; i < THROUGHPUT; i++) {
            loadTasks.offer(new Loader());
        }
    }
    
    public V8Value getValue(Variable var) throws EvaluationError {
        assert !EventQueue.isDispatchThread();
        synchronized (varQueue) {
            V8Value value;
            value = var.getValue();
            if (value == null || var.hasIncompleteValue()) {
                //LOG.fine("Queueing variable '"+var.getName()+"' "+var+" for evaluation.");
                varQueue.add(var);
                Loader loader = loadTasks.poll();
                if (loader != null) {
                    RP.post(loader);
                }
                do {
                    try {
                        //LOG.fine("  waiting for var value loader...");
                        varQueue.wait();
                    } catch (InterruptedException ex) {
                        throw new EvaluationError(ex.getLocalizedMessage());
                    }
                    value = var.getValue();
                    //LOG.fine("Variable '"+var.getName()+"' "+var+" value = "+value);
                } while (value == null);
            }
            return value;
        }
    }

    public void updateValue(Variable var, V8Value evalVal) {
        var.setValue(evalVal);
    }
    
    private final class Loader implements Runnable {

        @Override
        public void run() {
            final Map<Long, Variable> varsByRef = new LinkedHashMap<>();
            synchronized (varQueue) {
                for (Variable var : varQueue) {
                    varsByRef.put(var.getRef(), var);
                }
                varQueue.clear();
            }
            //LOG.fine("Loader vars = "+varsByRef);
            if (varsByRef.isEmpty()) {
                return ;
            }
            long[] handles = new long[varsByRef.size()];
            int i = 0;
            for (long h : varsByRef.keySet()) {
                handles[i++] = h;
            }
            //LOG.fine("Loader var handles = "+Arrays.toString(handles));
            final V8Response[] responseRef = new V8Response[] { null };
            final boolean[] responsed = new boolean[] { false };
            V8Request lookupCommandRequest = dbg.sendCommandRequest(V8Command.Lookup, new Lookup.Arguments(handles, Boolean.FALSE), new V8Debugger.CommandResponseCallback() {
                @Override
                public void notifyResponse(V8Request request, V8Response response) {
                    synchronized (responseRef) {
                        responseRef[0] = response;
                        responsed[0] = true;
                        responseRef.notifyAll();
                    }
                }
            });
            //LOG.fine("Lookup command request: "+lookupCommandRequest+", waiting for the response...");
            V8Response response;
            synchronized (responseRef) {
                if (lookupCommandRequest != null && !responsed[0]) {
                    try {
                        responseRef.wait();
                    } catch (InterruptedException iex) {}
                }
                response = responseRef[0];
            }
            //LOG.fine("Lookup response = "+response);
            if (response == null || !response.isSuccess()) {
                String err = (response != null) ? response.getErrorMessage() : "interrupted";
                //LOG.fine("Var lookup error: "+err);
                synchronized (varQueue) {
                    for (Variable var : varsByRef.values()) {
                        var.setValueLoadError(err);
                        //LOG.fine("Setting error to var '"+var.getName()+"' "+var+" : "+err);
                    }
                    varQueue.notifyAll();
                    if (!varQueue.isEmpty()) {
                        RP.post(this);
                    } else {
                        loadTasks.offer(this);
                    }
                }
            } else {
                Lookup.ResponseBody lrb = (Lookup.ResponseBody) response.getBody();
                Map<Long, V8Value> valuesByHandle = lrb.getValuesByHandle();
                //LOG.fine("Lookup values by handle: "+valuesByHandle);
                synchronized (varQueue) {
                    for (Long ref : valuesByHandle.keySet()) {
                        Variable var = varsByRef.get(ref);
                        if (var != null) {
                            var.setValue(valuesByHandle.get(ref));
                            //LOG.fine("Setting value to var '"+var.getName()+"' "+var+" : "+valuesByHandle.get(ref));
                        }
                    }
                    varQueue.notifyAll();
                    if (!varQueue.isEmpty()) {
                        RP.post(this);
                    } else {
                        loadTasks.offer(this);
                    }
                }
            }
        }
        
    }
    
}

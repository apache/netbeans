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
                    for (Map.Entry<Long, V8Value> entry : valuesByHandle.entrySet()) {
                        Variable var = varsByRef.get(entry.getKey());
                        if (var != null) {
                            var.setValue(entry.getValue());
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

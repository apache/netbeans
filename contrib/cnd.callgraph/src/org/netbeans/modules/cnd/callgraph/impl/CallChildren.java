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

package org.netbeans.modules.cnd.callgraph.impl;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.callgraph.api.Call;
import org.netbeans.modules.cnd.callgraph.api.Function;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class CallChildren extends Children.Keys<Call> {
    private Call call;
    private Function function;
    private final CallGraphState model;
    private Node parent;
    private volatile boolean isInited = false;
    private final boolean isCalls;
    private static final RequestProcessor RP = new RequestProcessor(CallChildren.class.getName(), 1);

    public CallChildren(Call call, CallGraphState model, boolean isCalls) {
        this.call = call;
        this.model = model;
        this.isCalls = isCalls;
    }

    public CallChildren(Function function, CallGraphState model, boolean isCalls) {
        this.function = function;
        this.model = model;
        this.isCalls = isCalls;
    }
 
    public void dispose(){
        if (isInited) {
            isInited = false;
            setKeys(new Call[0]);
        }
    }
    
    /*package-local*/ void setParent(Node parent){
        this.parent = parent;
    }
    
    private synchronized void resetKeys(){
        List<Call> set;
        if (isCalls) {
            if (call != null) {
                set = model.getCallees(call.getCallee());
                model.setCalleesExpanded(call.getCallee(), true);
            } else {
                set = model.getCallees(function);
                model.setCalleesExpanded(function, true);
            }
        } else {
            if (call != null) {
                set = model.getCallers(call.getCaller());
                model.setCallersExpanded(call.getCaller(), true);
            } else {
                set = model.getCallers(function);
                model.setCallersExpanded(function, true);
            }
        }
        if (set != null && set.size() > 0) {
            Collections.<Call>sort(set);
            setKeys(set);
            return;
        }
        setKeys(new Call[0]);
    }
    
    @Override
    protected Node[] createNodes(Call call) {
        if (call instanceof LoadingNode) {
            return new Node[]{(Node)call};
        }
        Node node = new CallNode(call, model, isCalls);
        return new Node[]{node};
    }

    /*package-local*/ boolean isRecusion(){
        Function fun;
        if (isCalls) {
            if (call != null) {
                fun = call.getCallee();
            } else {
                fun = function;
            }
        } else {
            if (call != null) {
                fun = call.getCaller();
            } else {
                fun = function;
            }
        }
        Node p = parent;
        while (p != null) {
            if (p instanceof CallNode) {
                Function f;
                if (isCalls) {
                    f = ((CallNode)p).getCall().getCaller();
                } else {
                    f = ((CallNode)p).getCall().getCallee();
                }
                if (fun.equals(f)){
                    return true;
                }
            } else if (p instanceof FunctionRootNode){
                return fun.equals( ((FunctionRootNode)p).getFunction() );
            }
            p = p.getParentNode();
        }
        return false;
    }
    
    /*package*/ void init() {
        //invoke in sync
        if (!isInited)  {
            isInited = true;
            if (isRecusion()) {
                setKeys(new Call[0]);
            } else {
                resetKeys();
            }            
        }
    }

    @Override
    protected void addNotify() {
        if (!isInited) {
            isInited = true;
            if (isRecusion()) {
                setKeys(new Call[0]);
            } else {
                setKeys(new Call[]{new LoadingNode()});
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        resetKeys();
                    }
                });
            }
        }
        super.addNotify();
    }

    @Override
    protected void removeNotify() {
        super.removeNotify();
        dispose();
    }
}

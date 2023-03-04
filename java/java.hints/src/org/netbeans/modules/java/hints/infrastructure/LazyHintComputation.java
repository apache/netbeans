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

package org.netbeans.modules.java.hints.infrastructure;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
public class LazyHintComputation implements CancellableTask<CompilationInfo> {
    
    private FileObject file;
    
    /** Creates a new instance of LazyHintComputation */
    public LazyHintComputation(FileObject file) {
        this.file = file;
    }

    public synchronized void cancel() {
        cancelled.set(true);
        if (delegate != null) {
            delegate.cancel();
        }
    }

    private synchronized void setDelegate(CreatorBasedLazyFixList delegate) {
        this.delegate = delegate;
    }
    
    private AtomicBoolean cancelled = new AtomicBoolean();
    private CreatorBasedLazyFixList delegate;
    private boolean isCancelled() {
        return cancelled.get();
    }
    
    private void resume() {
        cancelled.set(false);
    }
    
    public void run(CompilationInfo info) {
        resume();
        
        boolean cancelled = false;
        
        List<CreatorBasedLazyFixList> toCompute = new LinkedList<CreatorBasedLazyFixList>();
        
        try {
            toCompute.addAll(LazyHintComputationFactory.getAndClearToCompute(file));
            
            if (isCancelled()) {
                cancelled = true;
                return;
            }
            
            while (!toCompute.isEmpty()) {
                if (isCancelled()) {
                    cancelled = true;
                    return;
                }
                
                CreatorBasedLazyFixList l = toCompute.remove(0);
                
                setDelegate(l);
                l.compute(info, this.cancelled);
                setDelegate(null);
                
                if (isCancelled()) {
                    toCompute.add(0, l);
                    cancelled = true;
                    return;
                }
            }
        } finally {
            if (cancelled && !toCompute.isEmpty()) {
                for (CreatorBasedLazyFixList l : toCompute) {
                    LazyHintComputationFactory.addToCompute(file, l);
                }
            }
        }
    }
    
}

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
package org.netbeans.modules.java.editor.semantic;

import com.sun.source.tree.Tree;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.java.source.support.CancellableTreeScanner;

/**
 *
 * @author Jan Lahoda
 */
public abstract class ScanningCancellableTask<T> implements CancellableTask<T> {

    protected AtomicBoolean canceled = new AtomicBoolean();

    /** Creates a new instance of ScanningCancellableTask */
    protected ScanningCancellableTask() {
    }

    public final synchronized void cancel() {
        canceled.set(true);
        
        if (pathScanner != null) {
            pathScanner.cancel();
        }
        if (scanner != null) {
            scanner.cancel();
        }
    }

    public abstract void run(T parameter) throws Exception;
    
    protected final synchronized boolean isCancelled() {
        return canceled.get();
    }
    
    protected final synchronized void resume() {
        canceled.set(false);
    }
    
    private CancellableTreePathScanner pathScanner;
    private CancellableTreeScanner     scanner;
    
    protected <R, P> R scan(CancellableTreePathScanner<R, P> scanner, Tree toScan, P p) {
        if (isCancelled())
            return null;
        
        try {
            synchronized (this) {
                this.pathScanner = scanner;
            }
            
            if (isCancelled())
                return null;
            
            return scanner.scan(toScan, p);
        } finally {
            synchronized (this) {
                this.pathScanner = null;
            }
        }
    }

    protected <R, P> R scan(CancellableTreeScanner<R, P> scanner, Tree toScan, P p) {
        if (isCancelled())
            return null;
        
        try {
            synchronized (this) {
                this.scanner = scanner;
            }
            
            if (isCancelled())
                return null;
            
            return scanner.scan(toScan, p);
        } finally {
            synchronized (this) {
                this.scanner = null;
            }
        }
    }
    
}

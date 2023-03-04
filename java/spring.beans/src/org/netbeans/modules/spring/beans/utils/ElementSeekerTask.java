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
package org.netbeans.modules.spring.beans.utils;

import java.io.IOException;
import java.security.Policy;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 * Abstract class which eases and generalize writing of {@link Runnable}, {@link CancellableTask} classes.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public abstract class ElementSeekerTask implements Runnable, CancellableTask<CompilationController> {

    protected final AtomicBoolean elementFound = new AtomicBoolean(false);
    protected final JavaSource javaSource;

    public ElementSeekerTask(JavaSource javaSource) {
        Parameters.notNull("javaSource", javaSource);
        this.javaSource = javaSource;
    }

    @Override
    public void run() {
        runAsUserTask();
    }

    @Override
    public void cancel() {
    }

    public boolean wasElementFound() {
        return elementFound.get();
    }

    public void runAsUserTask() {
        try {
            javaSource.runUserActionTask(this, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}

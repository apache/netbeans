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
package org.netbeans.modules.java.disco;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.SwingWorker;
import org.checkerframework.checker.guieffect.qual.UIEffect;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SwingWorker2 {

    static class Builder<T> {

        private final @NonNull
        Callable<T> task;
        private @MonotonicNonNull
        Consumer<T> consumer;
        private @Nullable
        Consumer<Exception> errors;

        @SuppressWarnings("initialization")
        public Builder(@NonNull Callable<T> task) {
            this.task = task;
        }

        public Builder<T> then(@NonNull Consumer<T> consumer) {
            this.consumer = consumer;
            return this;
        }

        public Builder<T> handle(@Nullable Consumer<Exception> errors) {
            this.errors = errors;
            return this;
        }

        @UIEffect
        public void execute() {
            new SwingWorker<T, Void>() {
                @Override
                protected T doInBackground() throws Exception {
                    return task.call();
                }

                @Override
                protected void done() {
                    try {
                        consumer.accept(get());
                    } catch (InterruptedException | ExecutionException ex) {
                        if (errors != null)
                            errors.accept(ex);
                    }
                }
            }.execute();
        }

    }

    public static <T> Builder<T> submit(Callable<T> bg) {
        return new Builder(bg);
    }

    @UIEffect
    public static <T> void post(Callable<T> bg, Consumer<T> success, Consumer<Exception> fail) {
        submit(bg).then(success).handle(fail).execute();
    }

}

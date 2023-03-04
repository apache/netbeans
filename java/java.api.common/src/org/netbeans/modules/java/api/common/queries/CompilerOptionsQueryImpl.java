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
package org.netbeans.modules.java.api.common.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
final class CompilerOptionsQueryImpl implements CompilerOptionsQueryImplementation {

    private final PropertyEvaluator eval;
    private final String additionalCompilerOptionsProperty;
    private final AtomicReference<Result> result;

    CompilerOptionsQueryImpl(
            @NonNull final PropertyEvaluator eval,
            @NonNull final String additionalCompilerOptionsProperty) {
        Parameters.notNull("eval", eval);   //NOI18N
        Parameters.notNull("additionalCompilerOptionsProperty", additionalCompilerOptionsProperty); //NOI18N
        this.eval = eval;
        this.additionalCompilerOptionsProperty = additionalCompilerOptionsProperty;
        this.result = new AtomicReference<>();
    }

    @Override
    @CheckForNull
    public Result getOptions(FileObject file) {
        Result res = result.get();
        if (res == null) {
            res = new ResultImpl(eval, additionalCompilerOptionsProperty);
            if (!result.compareAndSet(null, res)) {
                res = result.get();
            }
            assert res != null;
        }
        return res;
    }

    private static final class ResultImpl extends Result implements PropertyChangeListener {

        private final PropertyEvaluator eval;
        private final String additionalCompilerOptionsProperty;
        private final ChangeSupport listeners;
        private volatile List<String> cache;

        ResultImpl(
                @NonNull final PropertyEvaluator eval,
                @NonNull final String additionalCompilerOptionsProperty) {
            this.eval = eval;
            this.additionalCompilerOptionsProperty = additionalCompilerOptionsProperty;
            this.listeners = new ChangeSupport(this);
            this.eval.addPropertyChangeListener(this);
        }

        @Override
        @NonNull
        public List<? extends String> getArguments() {
            List<String> res = cache;
            if (res == null) {
                final String additionalCompilerOptions = eval.getProperty(additionalCompilerOptionsProperty);
                res = additionalCompilerOptions == null || additionalCompilerOptions.isEmpty() ?
                        Collections.emptyList() :
                        parseLine(additionalCompilerOptions);
                cache = res;
            }
            return res;
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            listeners.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            listeners.removeChangeListener(listener);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (propName == null || additionalCompilerOptionsProperty.equals(propName)) {
                cache = null;
                listeners.fireChange();
            }
        }

    }

}

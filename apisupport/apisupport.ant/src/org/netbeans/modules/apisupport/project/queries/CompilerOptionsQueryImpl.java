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
package org.netbeans.modules.apisupport.project.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SingleModuleProperties;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

public class CompilerOptionsQueryImpl implements CompilerOptionsQueryImplementation {

    private final NbModuleProject project;
    private final AtomicReference<Result> cachedResult = new AtomicReference<>();

    public CompilerOptionsQueryImpl(NbModuleProject project) {
        this.project = project;
    }

    @Override
    public Result getOptions(FileObject file) {
        Result result = cachedResult.get();

        if (result == null) {
            result = cachedResult.updateAndGet(existing -> existing != null ? existing
                                                                            : new ResultImpl(project));
        }

        return result;
    }

    private static class ResultImpl extends Result implements PropertyChangeListener {

        private static final Pattern SPACE_SPLIT = Pattern.compile(" +");
        private final PropertyEvaluator evaluator;
        private final ChangeSupport cs = new ChangeSupport(this);
        private List<? extends String> cachedArguments;

        public ResultImpl(NbModuleProject project) {
            this.evaluator = project.evaluator();
            this.evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, this.evaluator));
        }


        @Override
        public synchronized List<? extends String> getArguments() {
            List<? extends String> result = cachedArguments;

            if (result == null) {
                Map<String, String> properties = evaluator.getProperties();
                String compilerArgs = properties.getOrDefault(SingleModuleProperties.JAVAC_COMPILERARGS, "") +
                        " " +
                        properties.getOrDefault(SingleModuleProperties.JAVAC_COMPILERARGS_INTERNAL_EXTRA, "");

                compilerArgs = compilerArgs.trim();

                if (compilerArgs.isEmpty()) {
                    result = Collections.emptyList();
                } else {
                    result = Collections.unmodifiableList(Arrays.asList(SPACE_SPLIT.split(compilerArgs)));
                }

                cachedArguments = result;
            }

            return result;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            cs.removeChangeListener(listener);
        }

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            synchronized (this) {
                cachedArguments = null;
            }
            cs.fireChange();
        }
    }

}

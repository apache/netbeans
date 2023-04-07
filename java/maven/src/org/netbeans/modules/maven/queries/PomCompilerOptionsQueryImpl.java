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
package org.netbeans.modules.maven.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Parameters;

/**
 * Implementation of the {@link CompilerOptionsQueryImplementation} to provide pom.xml declared compiler arguments.
 * @author Tomas Stupka
 */
public final class PomCompilerOptionsQueryImpl implements CompilerOptionsQueryImplementation {

    private final AtomicReference<ResultImpl> result;
    private final NbMavenProjectImpl proj;

    public PomCompilerOptionsQueryImpl(
            NbMavenProjectImpl proj) {
        Parameters.notNull("proj", proj);   // NOI18N
        this.proj = proj;
        this.result = new AtomicReference<>();
    }

    @CheckForNull
    @Override
    public Result getOptions(@NonNull final FileObject file) {
        ResultImpl res = result.get();
        if (res == null) {
            res = new ResultImpl(proj);
            if (!result.compareAndSet(null, res)) {
                res = result.get();
            }
            assert res != null;
        }
        return res;
    }

    private static final class ResultImpl extends Result implements PropertyChangeListener {
        private static final List<String> EMPTY = Collections.EMPTY_LIST;
        private static final String ARG_PARAMETERS = "-parameters"; // NOI18N
        private final ChangeSupport cs;
        //@GuardedBy("this")
        private List<String> cache;

        //@GuardedBy("this")
        private final NbMavenProjectImpl proj;

        ResultImpl(NbMavenProjectImpl proj) {
            this.proj = proj;
            proj.getProjectWatcher().addPropertyChangeListener(this);
            this.cs = new ChangeSupport(this);
        }

        @Override
        public List<? extends String> getArguments() {
            List<String> args;
            synchronized (this) {
                args = cache;
            }
            if (args == null) {
                args = createArguments();
                synchronized (this) {
                    if (cache == null) {
                        cache = args;
                    } else {
                        args = cache;
                    }
                }
            }
            return args;
        }

        private List<String> createArguments() {
            String[] compilerArgs = PluginPropertyUtils.getPluginPropertyList(proj, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, "compilerArgs", "arg", null); // NOI18N
            if (compilerArgs != null) {
                List<String> args = new ArrayList<>();
                for (String compilerArg : compilerArgs) {
                    if ((compilerArg != null) && (!compilerArg.isEmpty())) {
                        args.add(compilerArg);
                    }
                }
                return args;
            }
            return EMPTY;
        }
        
        @Override
        public void addChangeListener(@NonNull final ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener listener) {
            cs.removeChangeListener(listener);
        }

        @Override
        public void propertyChange(@NonNull final PropertyChangeEvent evt) {
            if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                reset();
            }
        }

        private void reset() {
            synchronized (this) {
                cache = null;
            }
            cs.fireChange();
        }

    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
                for (String compilerArg : compilerArgs) {
                    if(compilerArg != null && ARG_PARAMETERS.equals(compilerArg.trim())) {
                        return Arrays.asList(ARG_PARAMETERS);
                    }
                }
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

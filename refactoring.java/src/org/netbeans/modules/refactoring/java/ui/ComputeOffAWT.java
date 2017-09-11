/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.refactoring.java.ui;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class ComputeOffAWT {

    public static <T> T computeOffAWT(Worker<T> w, String featureName, final JavaSource source, Phase phase) {
        AtomicBoolean cancel = new AtomicBoolean();
        Compute<T> c = new Compute(cancel, source, phase, w);

        ProgressUtils.runOffEventDispatchThread(c, featureName, cancel, false);

        return c.result;
    }

    private static final class Compute<T> implements Runnable, Task<CompilationController> {

        private final AtomicBoolean cancel;
        private final JavaSource source;
        private final Phase phase;
        private final Worker<T> worker;
        private       T result;

        public Compute(AtomicBoolean cancel, JavaSource source, Phase phase, Worker<T> worker) {
            this.cancel = cancel;
            this.source = source;
            this.phase = phase;
            this.worker = worker;
        }

        public void run() {
            try {
                source.runUserActionTask(this, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                result = null;
            }
        }

        public void run(CompilationController parameter) throws Exception {
            if (cancel.get()) return ;

            parameter.toPhase(phase);

            if (cancel.get()) return ;

            T t = worker.process(parameter);

            if (cancel.get()) return ;

            result = t;
        }
        
    }
    
    public static interface Worker<T> {
        T process(CompilationInfo info);
    }

    private ComputeOffAWT() {
    }
}

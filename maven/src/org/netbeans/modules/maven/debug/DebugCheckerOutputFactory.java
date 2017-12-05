/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2017 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.debug;

import java.awt.Color;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.output.ContextOutputProcessorFactory;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputProcessorFactory;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service=OutputProcessorFactory.class)
public class DebugCheckerOutputFactory implements ContextOutputProcessorFactory {

    @Override
    public Set<? extends OutputProcessor> createProcessorsSet(Project project, RunConfig config) {
        String trigger = config.getProperties().get(Constants.ACTION_PROPERTY_JPDAATTACH_TRIGGER);
        if (trigger == null) {
            return Collections.emptySet();
        }
        return Collections.singleton(new JPDATrigger(trigger, config));
    }

    @Override
    public Set<? extends OutputProcessor> createProcessorsSet(Project project) {
        return Collections.emptySet();
    }

    private static class JPDATrigger implements OutputProcessor {

        private String trigger;
        private final RunConfig cfg;

        public JPDATrigger(String trigger, RunConfig cfg) {
            this.trigger = trigger;
            this.cfg = cfg;
        }

        @Override
        public String[] getRegisteredOutputSequences() {
            return new String[] { "project-execute" };
        }

        @Override
        public void processLine(String line, OutputVisitor visitor) {
            System.err.println("processLine " + line);
            if (trigger != null && line.contains(trigger)) {
                trigger = null;
                try {
                    DebuggerChecker.connect(cfg);
                } catch (DebuggerStartException ex) {
                    visitor.setLine(visitor.getLine() + " cannot connect: " + ex.getMessage());
                    visitor.setColor(Color.red);
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        @Override
        public void sequenceStart(String sequenceId, OutputVisitor visitor) {
        }

        @Override
        public void sequenceEnd(String sequenceId, OutputVisitor visitor) {
        }

        @Override
        public void sequenceFail(String sequenceId, OutputVisitor visitor) {
        }
    }
    
}

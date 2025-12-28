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

package org.netbeans.modules.gradle.execute;

import org.netbeans.modules.gradle.api.execute.RunConfig;
import org.netbeans.modules.gradle.api.output.OutputDisplayer;
import org.netbeans.modules.gradle.api.output.OutputProcessor;
import org.netbeans.modules.gradle.api.output.OutputProcessorFactory;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.output.GradleProcessorFactory;
import org.openide.util.Lookup;
import org.openide.windows.IOColorPrint;
import org.openide.windows.IOColors;
import org.openide.windows.IOColors.OutputType;
import org.openide.windows.InputOutput;

import static org.openide.windows.IOColors.OutputType.*;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradlePlainEscapeProcessor implements EscapeProcessor {

    final InputOutput io;
    final OutputDisplayer output;
    IOColors.OutputType outType;
    StringBuilder line = new StringBuilder(80);

    final Set<OutputProcessor> processors = new LinkedHashSet<>();

    public GradlePlainEscapeProcessor(InputOutput io, RunConfig cfg, boolean error) {
        this.io = io;
        output = new IODisplayer();
        if (cfg != null) {
            Project prj = cfg.getProject();
            if (prj != null) {
                processorsFromLookup(prj.getLookup(), cfg);
            }
        }
        outType = error ? ERROR : OUTPUT;
        processors.add(GradleProcessorFactory.URL_PROCESSOR);
        processors.add(GradleProcessorFactory.TASK_LINE_PROCESSOR);
        processors.add(GradleProcessorFactory.STATIC_STRING_PROCESSOR);
    }

    public GradlePlainEscapeProcessor(InputOutput io, boolean error) {
        this(io, null, error);
    }

    private void processorsFromLookup(Lookup lookup, RunConfig cfg) {
        Collection<? extends OutputProcessorFactory> factories
                = lookup.lookupAll(OutputProcessorFactory.class);
        for (OutputProcessorFactory factory : factories) {
            processors.addAll(factory.createOutputProcessors(cfg));
        }
    }

    @Override
    public void processCommand(String sequence, char command, int... a) {
    }

    @Override
    public void processText(String text, boolean forceOutput) {
        line.append(text);
        boolean eol = text.endsWith("\n");
        if (eol) {
            line.setLength(line.length() -1);
        }
        if (eol) {
            String out = line.toString();
            // Filter out through output processors
            boolean processed = false;
            Iterator<? extends OutputProcessor> it = processors.iterator();
            while (!processed && it.hasNext()) {
                processed = it.next().processLine(output, out);
            }
            if (!processed) {
                output.print(out, null, outType);
            }
            output.print("\n");
            line.setLength(0);
        } else if (forceOutput && line.length() > 0) {
            output.print(line.toString());
            line.setLength(0);
        }
    }

    private class IODisplayer extends OutputDisplayer {

        @Override
        protected void doPrint(CharSequence text, final Runnable action, OutputType type) {
            OutputListener l = null;
            if (action != null) {
                l = new OutputListener() {

                    @Override
                    public void outputLineCleared(OutputEvent ev) {
                    }

                    @Override
                    public void outputLineSelected(OutputEvent ev) {
                    }

                    @Override
                    public void outputLineAction(OutputEvent ev) {
                        action.run();
                    }
                };
            }
            if (type == null) {
                type = (action == null) ? outType : HYPERLINK;
            }
            try {
                IOColorPrint.print(io, text, l, false, IOColors.getColor(io, type));
            } catch (IOException ex) {
                //TODO: Shall we log this?
            }
        }

    }
    
}

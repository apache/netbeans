/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.progress.ProgressHandle;
import static org.openide.windows.IOColors.OutputType.*;
import org.openide.windows.InputOutput;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradleColorEscapeProcessor extends GradlePlainEscapeProcessor {

    private static final Pattern PROGRESS_PATTERN = Pattern.compile("> Building (\\d+)%(.*)");
    
    final ProgressHandle handle;
    
    StringBuilder statusLine = new StringBuilder(120);
    boolean append = true;
    boolean hasProgress;
    
    public GradleColorEscapeProcessor(InputOutput io, ProgressHandle handle, RunConfig cfg) {
        super(io, cfg, false);
        this.handle = handle;
    }
    
    @Override
    public void processCommand(String sequence, char command, int... a) {
        if (a.length > 0) {
            int param = a[0];
            switch (command) {
                case 'm':
                    switch (param) {
                        case  1: 
                            outType = LOG_DEBUG;
                            break;
                        case 22: 
                            outType = OUTPUT;
                            break;
                        case 31:
                            outType = LOG_FAILURE;
                            break;
                        case 33:
                            outType = LOG_WARNING;
                            break;
                        case 39:
                            outType = OUTPUT;
                            break;
                    }
                    break;
                case 'D':
                    if (statusLine != null) {
                        statusLine.delete(statusLine.length() - param, statusLine.length());
                    }
                    break;
                case 'A':
                    append = true;
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void processText(String text) {
        if (outType == LOG_DEBUG) {
            if (!text.startsWith("--")) {
                statusLine.append(text);
                processProgress();
            } else {
                outType = OUTPUT;
            }
        }
        if (outType != LOG_DEBUG) {
            String out = text;
            if (!append) {
                io.getOut().println();
            }
            append = !text.endsWith("\n");
            if (!append) {
                out = text.substring(0, text.length() - 1);
            }
            
            super.processText(out);
        }
    }

    private void processProgress() {
        Matcher matcher = PROGRESS_PATTERN.matcher(statusLine);
        if (matcher.matches()) {
            if (!hasProgress) {
                handle.switchToDeterminate(100);
                hasProgress = true;
            }
            String percent = matcher.group(1);
            String status = matcher.group(2);
            try {
                handle.progress(status, Integer.parseInt(percent));
            } catch (NumberFormatException ex) {
                //Unlikely to happen.
            }
        } else {
            handle.progress(statusLine.toString());
        }
    }
    
}

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

package org.netbeans.modules.gradle.java.output;

import org.netbeans.modules.gradle.api.execute.RunConfig;
import org.netbeans.modules.gradle.api.output.OutputDisplayer;
import org.netbeans.modules.gradle.api.output.OutputListeners;
import org.netbeans.modules.gradle.api.output.OutputProcessor;
import org.netbeans.modules.gradle.api.output.OutputProcessorFactory;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.Utilities;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GatlingReportProcessorFactory implements OutputProcessorFactory {

    static final OutputProcessor GATLING_OUTPUT_PROCESSOR = new GatlingOutputProcessor();
    
    @Override
    public Set<? extends OutputProcessor> createOutputProcessors(RunConfig cfg) {
        return Collections.singleton(GATLING_OUTPUT_PROCESSOR);
    }
    
    private static class GatlingOutputProcessor implements OutputProcessor {

        private static final Pattern GATLING_REPORT = Pattern.compile("(Please open the following file: )((.+)index.html)"); //NOI18N
        
        @Override
        public boolean processLine(OutputDisplayer out, String line) {
            Matcher matcher = GATLING_REPORT.matcher(line);
            if (matcher.matches()) {
                String txt = matcher.group(1);
                String lnk = matcher.group(2);
                out.print(txt);
                File report = new File(lnk);
                if (report.isFile()) {
                    try {
                        out.print(lnk, OutputListeners.openURL(Utilities.toURI(report).toURL()));
                    } catch (MalformedURLException ex) {
                    }
                } else {
                    out.print(lnk);
                }
                return true;
            }
            return false;
        }
    }
    
}

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
package org.netbeans.modules.findbugs.installer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class FakeAnalyzer implements Analyzer {

    @Override
    public Iterable<? extends ErrorDescription> analyze() {
        return Collections.emptyList();
    }

    @Override
    public boolean cancel() {
        return true;
    }

    @ServiceProvider(service=AnalyzerFactory.class)
    public static final class FakeAnalyzerFactory extends AnalyzerFactory {

        @Messages("DN_FindBugs=FindBugs")
        public FakeAnalyzerFactory() {
            super("findbugs", Bundle.DN_FindBugs(), (String) null);
        }

        @Override
        public Iterable<? extends WarningDescription> getWarnings() {
            return Collections.emptyList();
        }

        @Messages("DN_FindBugsIntegration=FindBugs Integration")
        @Override
        public Collection<? extends MissingPlugin> requiredPlugins(Context context) {
            return Arrays.asList(new MissingPlugin("org.netbeans.modules.findbugs", Bundle.DN_FindBugsIntegration()));
        }

        @Override
        public CustomizerProvider<?, ?> getCustomizerProvider() {
            return null;
        }

        @Override
        public Analyzer createAnalyzer(Context context) {
            return new FakeAnalyzer();
        }

    }

}

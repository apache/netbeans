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
package org.netbeans.modules.cnd.model.jclank.bridge.trace;

import java.io.PrintWriter;
import java.util.Set;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.debug.CndDiagnosticProvider;
import org.netbeans.modules.cnd.model.jclank.bridge.impl.CsmJClankSerivicesImpl;
import static org.netbeans.modules.cnd.model.jclank.bridge.trace.Bundle.*;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
public abstract class JClankPreprocessFile extends JClankDiagnosticAbstractProvider {

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 108)
    public static final class JClankOnlyExpandTokensWOStatistics extends JClankPreprocessFile {

        public JClankOnlyExpandTokensWOStatistics() {
            super(false, false);
        }

        @NbBundle.Messages({"JClankOnlyExpandTokensWOStatistics.displayName=Expand Only"})
        @Override
        public String getDisplayName() {
            return JClankOnlyExpandTokensWOStatistics_displayName();
        }
    }

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 106)
    public static final class JClankOnlyExpandTokensWithStatistics extends JClankPreprocessFile {

        public JClankOnlyExpandTokensWithStatistics() {
            super(false, true);
        }

        @NbBundle.Messages({"JClankOnlyExpandTokensWithStatistics.displayName=Expand Only + Statistics"})
        @Override
        public String getDisplayName() {
            return JClankOnlyExpandTokensWithStatistics_displayName();
        }
    }

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 102)
    public static final class JClankPrintTokensWithStatistics extends JClankPreprocessFile {

        public JClankPrintTokensWithStatistics() {
            super(true, true);
        }

        @NbBundle.Messages({"JClankPrintTokensWithStatistics.displayName=Print Expanded + Statistics"})
        @Override
        public String getDisplayName() {
            return JClankPrintTokensWithStatistics_displayName();
        }
    }

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 104)
    public static final class JClankPrintTokensWOStatistics extends JClankPreprocessFile {

        public JClankPrintTokensWOStatistics() {
            super(true, false);
        }

        @NbBundle.Messages({"JClankPrintTokensWOStatistics.displayName=Print Expanded"})
        @Override
        public String getDisplayName() {
            return JClankPrintTokensWOStatistics_displayName();
        }
    }

    final boolean printTokens;
    final boolean printStatistics;

    public JClankPreprocessFile(boolean printTokens, boolean printStatistics) {
        this.printTokens = printTokens;
        this.printStatistics = printStatistics;
    }

    @Override
    protected void doNativeFileItemDiagnostic(Set<NativeFileItem> nfis, PrintWriter printOut) {
        printOut.printf("====%s\n", getDisplayName()); // NOI18N
        long totalTime = 0;
        int numFiles = 0;
        for (NativeFileItem nfi : nfis) {
            try {
                printOut.printf("dumpFileTokens %s...%n", nfi.getAbsolutePath()); // NOI18N
                long time = System.currentTimeMillis();
                CsmJClankSerivicesImpl.dumpPreprocessed(nfi, printOut, null, printTokens, printStatistics);
                numFiles++;
                time = System.currentTimeMillis() - time;
                totalTime += time;
                printOut.printf("dumpFileTokens %s took %,dms %n", nfi.getAbsolutePath(), time); // NOI18N
            } catch (Throwable e) {
                new Exception(nfi.getAbsolutePath(), e).printStackTrace(printOut);
            }
        }
        printOut.printf("====%s for %d files took %,dms\n", getDisplayName(), numFiles, totalTime); // NOI18N
    }

}

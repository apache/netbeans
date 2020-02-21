/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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

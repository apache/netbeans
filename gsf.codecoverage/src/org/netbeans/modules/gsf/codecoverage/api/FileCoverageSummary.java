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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.gsf.codecoverage.api;

import org.openide.filesystems.FileObject;

/**
 *
 * @author Tor Norbye
 */
public class FileCoverageSummary implements Comparable<FileCoverageSummary> {
    private final FileObject file;
    private final String displayName;
    private final int lineCount;
    private final int executedLineCount;
    private final int inferredCount;
    private final int partialCount;
    //private final int executableLines;

    /**
     * Create a code coverage summary for a file.
     *
     * @param file The file we collected data from
     * @param displayName A display name for the file (often the path itself)
     * @param lineCount The total number of lines in the file
     * @param executedLineCount The total number of lines that were executed (including inferred and partial)
     * @param inferredCount The lines not recorded but inferred to be executed (such as comments and whitespace
     *   between executed statements) Return 0 for "unknown/not recorded".
     * @param partialCount The lines that were partially executed. Return 0 for "unknown/not recorded".
     */
    public FileCoverageSummary(FileObject file, String displayName, int lineCount, int executedLineCount,
            int inferredCount, int partialCount) {
        this.file = file;
        this.displayName = displayName;
        this.lineCount = lineCount;
        this.executedLineCount = executedLineCount;
        this.inferredCount = inferredCount;
        this.partialCount = partialCount;
    }

    public float getCoveragePercentage() {
        if (lineCount == 0) {
            //return 100.0f;
            return 0f;
        } else {
            return (100.0f*executedLineCount)/lineCount;
        }
    }

    public int compareTo(FileCoverageSummary other) {
        float cov = getCoveragePercentage();
        float otherCov = other.getCoveragePercentage();
        if (cov == otherCov) {
            return 0;
        } else return cov < otherCov ? -1 : 1;
    }

    public FileObject getFile() {
        return file;
    }

    public int getExecutedLineCount() {
        return executedLineCount;
    }

    public int getLineCount() {
        return lineCount;
    }

    public int getInferredCount() {
        return inferredCount;
    }

    public int getPartialCount() {
        return partialCount;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName + ": " + getCoveragePercentage() + "%";
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.clientproject.api.jstesting;

import org.netbeans.modules.gsf.codecoverage.api.CoverageType;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

final class FileCoverageDetailsImpl implements FileCoverageDetails {

    private final FileObject fo;
    private final Coverage.File file;
    private final long generated;


    FileCoverageDetailsImpl(FileObject fo, Coverage.File file) {
        assert fo != null;
        assert file != null;

        this.fo = fo;
        this.file = file;
        this.generated = FileUtil.toFile(fo).lastModified();
    }

    @Override
    public FileObject getFile() {
        return fo;
    }

    @Override
    public int getLineCount() {
        return file.getMetrics().getLineCount();
    }

    @Override
    public boolean hasHitCounts() {
        return true;
    }

    @Override
    public long lastUpdated() {
        return generated;
    }

    @Override
    public FileCoverageSummary getSummary() {
        return CoverageProviderImpl.getFileCoverageSummary(file);
    }

    @Override
    public CoverageType getType(int lineNo) {
        lineNo++;
        // XXX when to return CoverageType.INFERRED?
        // XXX optimize - hold lines in hash map
        for (Coverage.Line line : file.getLines()) {
            if (line.getNumber() == lineNo) {
                if (line.getHitCount() > 0) {
                    return CoverageType.COVERED;
                } else {
                    return CoverageType.NOT_COVERED;
                }
            }
        }
        return CoverageType.UNKNOWN;
    }

    @Override
    public int getHitCount(int lineNo) {
        lineNo++;
        for (Coverage.Line line : file.getLines()) {
            if (line.getNumber() == lineNo) {
                return line.getHitCount();
            }
        }
        return 0;
    }

}

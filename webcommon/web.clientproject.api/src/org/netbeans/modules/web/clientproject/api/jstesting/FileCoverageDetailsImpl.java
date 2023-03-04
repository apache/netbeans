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

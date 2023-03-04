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
package org.netbeans.modules.php.editor.verification;

import org.netbeans.modules.csl.api.Error.Badging;
import org.netbeans.modules.csl.api.Severity;
import org.openide.filesystems.FileObject;

/**
 * Class encapsulating errors caused by verification package.
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class VerificationError implements Badging {
    private final FileObject fileObject;
    private final int startOffset;
    private final int endOffset;

    public VerificationError(FileObject fileObject, int startOffset, int endOffset) {
        this.fileObject = fileObject;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    @Override
    public boolean showExplorerBadge() {
        return true;
    }

    @Override
    public FileObject getFile() {
        return fileObject;
    }

    @Override
    public int getStartPosition() {
        return startOffset;
    }

    @Override
    public int getEndPosition() {
        return endOffset;
    }

    @Override
    public boolean isLineError() {
        return true;
    }

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public Object[] getParameters() {
        return new Object[]{};
    }

}

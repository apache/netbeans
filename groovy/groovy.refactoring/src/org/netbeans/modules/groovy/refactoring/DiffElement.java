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

package org.netbeans.modules.groovy.refactoring;

import java.io.IOException;
import java.lang.ref.WeakReference;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @todo Copied from php module. Should be a part of either CSL or better
 * the refactoring API.
 *
 * @author Jan Becicka
 */
public class DiffElement extends SimpleRefactoringElementImplementation {

    private final Difference diff;
    private final PositionBounds bounds;
    private final FileObject parentFile;
    private final ModificationResult modification;
    private final String displayText;
    private WeakReference<String> newFileContent;


    private DiffElement(Difference diff, PositionBounds bounds, FileObject parentFile, ModificationResult modification) {
        this.diff = diff;
        this.bounds = bounds;
        this.parentFile = parentFile;
        this.modification = modification;
        this.displayText = diff.getDescription();
    }

    public static DiffElement create(Difference diff, FileObject fileObject, ModificationResult modification) {
        PositionRef start = diff.getStartPosition();
        PositionRef end = diff.getEndPosition();
        PositionBounds bounds = new PositionBounds(start, end);

        return new DiffElement(diff, bounds, fileObject, modification);
    }

    @Override
    public String getDisplayText() {
        return displayText;
    }

    @Override
    public Lookup getLookup() {
        return Lookups.fixed(diff);
    }

    @Override
    public FileObject getParentFile() {
        return parentFile;
    }

    @Override
    public void setEnabled(boolean enabled) {
        diff.exclude(!enabled);
        newFileContent = null;
        super.setEnabled(enabled);
    }

    @Override
    public PositionBounds getPosition() {
        return bounds;
    }

    @Override
    public String getText() {
        return displayText;
    }

    @Override
    public void performChange() {
        final String oldFileName = diff.getOldText();
        final String newFileName = diff.getNewText();

        if (parentFile.getName().equals(oldFileName)) {
            try {
                FileLock fileLock = parentFile.lock();
                parentFile.rename(fileLock, newFileName, "groovy"); // NOI18N
                fileLock.releaseLock();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    protected String getNewFileContent() {
        String result;
        if (newFileContent != null) {
            result = newFileContent.get();
            if (result != null) {
                return result;
            }
        }
        try {
            result = modification.getResultingSource(parentFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        newFileContent = new WeakReference<String>(result);
        return result;
    }
}

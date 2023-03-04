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
package org.netbeans.modules.refactoring.php.rename;

import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference;
import org.netbeans.modules.refactoring.php.DiffElement;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class RenameDiffElement extends DiffElement {
    private final String newFileName;
    private final FileRenamer fileRenamer;

    public static RenameDiffElement create(
            ModificationResult.Difference diff,
            FileObject fileObject,
            ModificationResult modification,
            FileRenamer fileRenamer) {
        return new RenameDiffElement(diff, new PositionBounds(diff.getStartPosition(), diff.getEndPosition()), fileObject, modification, fileRenamer);
    }

    public RenameDiffElement(Difference diff, PositionBounds bounds, FileObject parentFile, ModificationResult modification, FileRenamer fileRenamer) {
        super(diff, bounds, parentFile, modification);
        this.newFileName = diff.getNewText();
        this.fileRenamer = fileRenamer;
    }

    @Override
    public void performChange() {
        fileRenamer.rename(newFileName);
    }

}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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

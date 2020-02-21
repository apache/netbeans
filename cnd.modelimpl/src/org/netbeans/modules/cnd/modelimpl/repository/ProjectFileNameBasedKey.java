/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.CharSequences;

/**
 * A common ancestor for keys 
 * that are based on (project, file) pair
 */

/*package*/ abstract class ProjectFileNameBasedKey extends ProjectNameBasedKey {

    protected static final CharSequence NO_FILE = CharSequences.create("<No File Name>"); // NOI18N

    protected final int fileNameIndex;
    
    protected ProjectFileNameBasedKey(int unitID, int fileID) {
        super(unitID);
        this.fileNameIndex = fileID;
    }
    
    protected ProjectFileNameBasedKey(FileImpl file) {
        this(getFileUnitId(file), getFileNameId(file));
    }

    protected ProjectFileNameBasedKey(KeyDataPresentation presentation) {
        super(presentation);
        fileNameIndex = presentation.getFilePresentation();
    }

    static int getFileUnitId(FileImpl file) {
        // judging by #208877 null might occur here, although it's definitely wrong
        if (file == null) {
            CndUtils.assertUnconditional("Null file"); //NOI18N
            return -1;
        } else {
            return file.getUnitId();
        }
    }

    private static int getFileNameId(FileImpl file) {
        // extra check for #208877
        if (file == null) {
            CndUtils.assertUnconditional("Null file"); //NOI18N
            return -1;
        } else {
            return file.getFileId();
        }
    }

    @Override
    public void write(RepositoryDataOutput aStream) throws IOException {
        super.write(aStream);
        aStream.writeInt(fileNameIndex);
    }

    protected ProjectFileNameBasedKey(RepositoryDataInput aStream) throws IOException {
        super(aStream);
        this.fileNameIndex = aStream.readInt();
    }

    @Override
    public int hashCode() {
        return hashCode(getUnitId());
    }
    

    @Override
    public int hashCode(int unitID) {
        return 17*fileNameIndex + super.hashCode(unitID);
    }

    @Override
    public boolean equals(int thisUnitID, Key object, int objectUnitID) {
        if (!super.equals(thisUnitID, object, objectUnitID)) {
             return false;
         }
         ProjectFileNameBasedKey other = (ProjectFileNameBasedKey) object;
         return this.fileNameIndex == other.fileNameIndex;

    }

    /*package-local*/int getProjectFileIndex(){
        return fileNameIndex;
    }

    protected CharSequence getFileName() {
        return KeyUtilities.getFileNameById(getUnitId(), this.fileNameIndex);
    }

    /** A special safe method, mainly for toString / tracing */
    protected CharSequence getFileNameSafe() {
        return KeyUtilities.getFileNameByIdSafe(getUnitId(), this.fileNameIndex);
    }

    @Override
    public int getDepth() {
        assert super.getDepth() == 0;
        return 1;
    }

    @Override
    public CharSequence getAt(int level) {
        assert super.getDepth() == 0 && level < getDepth();
        return getFileName();
    }

    @Override
    public final int getFilePresentation() {
        return fileNameIndex;
    }
}

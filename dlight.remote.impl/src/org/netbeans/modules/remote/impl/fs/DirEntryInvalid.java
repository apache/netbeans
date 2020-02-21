/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs;

import java.util.Date;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.netbeans.modules.remote.impl.RemoteLogger;

/**
 *
 */
public final class DirEntryInvalid extends DirEntry {

    private final String name;

    public DirEntryInvalid(String name) {
        super(name);
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return 0;
    }

    @Override
    public boolean canExecute() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return false;
    }

    @Override
    public boolean canRead() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return false;
    }

    @Override
    public boolean canWrite() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return false;
    }

    @Override
    public Date getLastModified() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return new Date();
    }

    @Override
    public boolean isLink() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return false;
    }

    @Override
    public boolean isDirectory() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return false;
    }

    @Override
    public boolean isPlainFile() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return true;
    }

    @Override
    public boolean isSameLastModified(DirEntry other) {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return false;
    }

    @Override
    public FileType getFileType() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return FileType.Regular;
    }

    @Override
    public String getLinkTarget() {
        RemoteLogger.assertTrueInConsole(false, "unsupported operation for " + name); //NOI18N
        return null;
    }

    @Override
    public String toExternalForm() {
        return name; //TODO: escape '\n'
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public long getDevice() {
        return 0;
    }

    @Override
    public long getINode() {
        return 0;
    }

    @Override
    public String toString() {
        return "DirEntryInvalid {" + name + '}'; //NOI18N
    }
}

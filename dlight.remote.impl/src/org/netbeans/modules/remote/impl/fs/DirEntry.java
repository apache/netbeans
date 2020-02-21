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

/**
 *
 */
public abstract class DirEntry {

    private String cache;

    public DirEntry(String cache) {
        this.cache = cache;
    }
    
    public abstract String getName();

    public abstract long getSize();
    
    public abstract boolean canExecute();
    public abstract boolean canRead();
    public abstract boolean canWrite();

    /** Device no (stat.st_dev field). Zero value means that it is unknown */
    public abstract long getDevice();
    
    /** Inode (stat.st_ino field). Zero value means that it is unknown */
    public abstract long getINode();

    public abstract Date getLastModified();

    public abstract boolean isLink();
    public abstract boolean isDirectory();
    public abstract boolean isPlainFile();

    public abstract FileType getFileType();
    
    public boolean isSameLastModified(DirEntry other) {
        return getLastModified().equals(other.getLastModified());
    }
    
    public boolean hasINode() {
        return getINode() != 0;
    }

    public boolean isSameINode(DirEntry other) {
        return other.getDevice() == getDevice() && other.getINode() == this.getINode();
    }

    public boolean isSameType(DirEntry other) {
        return isLink() == other.isLink() && isDirectory() == other.isDirectory() && isPlainFile() == other.isPlainFile();
    }

    public boolean isSameAccess(DirEntry other) {
        if (other == null) {
            return false;
        } else {
            return this.canRead() == other.canRead()
                    && this.canWrite() == other.canWrite()
                    && this.canExecute() == other.canExecute();
        }
    }    
    
    public abstract String getLinkTarget();

    public final String getCache() {
        return cache;
    }
    
    public final void setCache(String cache) {
        this.cache = cache;
    }

    public abstract String toExternalForm();
    
    public abstract boolean isValid();    
}

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

package org.openide.text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import org.openide.filesystems.AbstractFileSystem;
import org.openide.filesystems.DefaultAttributes;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class TestFileSystem extends AbstractFileSystem 
implements AbstractFileSystem.Info, AbstractFileSystem.List, AbstractFileSystem.Change {
    String dir = "dir";

    @SuppressWarnings(value = "LeakingThisInConstructor")
    public TestFileSystem() {
        this.info = this;
        this.list = this;
        this.change = this;
        this.attr = new DefaultAttributes(info, change, list);
    }

    @Override
    public String getDisplayName() {
        return "Test FS";
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public Date lastModified(String name) {
        return new Date(5000L);
    }

    @Override
    public boolean folder(String name) {
        return dir.equals(name);
    }

    @Override
    public boolean readOnly(String name) {
        return false;
    }

    @Override
    public String mimeType(String name) {
        return "text/plain";
    }

    @Override
    public long size(String name) {
        return 0;
    }

    @Override
    public InputStream inputStream(String name) throws FileNotFoundException {
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public OutputStream outputStream(String name) throws IOException {
        return new ByteArrayOutputStream();
    }

    @Override
    public void lock(String name) throws IOException {
    }

    @Override
    public void unlock(String name) {
    }

    @Override
    public void markUnimportant(String name) {
    }

    @Override
    public String[] children(String f) {
        if ("".equals(f)) {
            return new String[]{dir};
        } else {
            return new String[]{"x.txt"};
        }
    }

    @Override
    public void createFolder(String name) throws IOException {
        throw new IOException();
    }

    @Override
    public void createData(String name) throws IOException {
        throw new IOException();
    }

    @Override
    public void rename(String oldName, String newName) throws IOException {
    }

    @Override
    public void delete(String name) throws IOException {
        throw new IOException();
    }

}

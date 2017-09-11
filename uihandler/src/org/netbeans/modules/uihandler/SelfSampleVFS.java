/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uihandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import org.openide.filesystems.AbstractFileSystem;
import org.openide.util.Enumerations;

/** Filesystem that allows to virtually move some files next to each other.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class SelfSampleVFS extends AbstractFileSystem 
implements AbstractFileSystem.List, AbstractFileSystem.Info, AbstractFileSystem.Attr {
    private final String[] names;
    private final File[] contents;

    @SuppressWarnings("LeakingThisInConstructor")
    SelfSampleVFS(String[] names, File[] contents) {
        this.names = names;
        this.contents = contents;
        this.list = this;
        this.info = this;
        this.attr = this;
    }
    

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public String[] children(String f) {
        return f.equals("") ? names : null;
    }

    private File findFile(String name) {
        for (int i = 0; i < names.length; i++) {
            if (name.equals(names[i])) {
                return contents[i];
            }
        }
        return null;
    }

    @Override
    public Date lastModified(String name) {
        File f = findFile(name);
        return f == null ? null : new Date(f.lastModified());
    }

    @Override
    public boolean folder(String name) {
        return name.equals("");
    }

    @Override
    public boolean readOnly(String name) {
        return true;
    }

    @Override
    public String mimeType(String name) {
        return null;
    }

    @Override
    public long size(String name) {
        File f = findFile(name);
        return f == null ? -1 : f.length();
    }

    @Override
    public InputStream inputStream(String name) throws FileNotFoundException {
        File f = findFile(name);
        if (f == null) {
            throw new FileNotFoundException();
        }
        return new FileInputStream(f);
    }

    @Override
    public OutputStream outputStream(String name) throws IOException {
        throw new IOException();
    }

    @Override
    public void lock(String name) throws IOException {
        throw new IOException();
    }

    @Override
    public void unlock(String name) {
        throw new UnsupportedOperationException(name);
    }

    @Override
    public void markUnimportant(String name) {
        throw new UnsupportedOperationException(name);
    }

    @Override
    public Object readAttribute(String name, String attrName) {
        return null;
    }

    @Override
    public void writeAttribute(String name, String attrName, Object value) throws IOException {
        throw new IOException();
    }

    @Override
    public Enumeration<String> attributes(String name) {
        return Enumerations.empty();
    }

    @Override
    public void renameAttributes(String oldName, String newName) {
        throw new UnsupportedOperationException(oldName);
    }

    @Override
    public void deleteAttributes(String name) {
        throw new UnsupportedOperationException(name);
    }
}

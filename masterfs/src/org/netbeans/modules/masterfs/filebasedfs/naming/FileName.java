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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.masterfs.filebasedfs.naming;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.util.CharSequences;

/**
 * @author Radek Matous
 */
public class FileName implements FileNaming {
    private final CharSequence name;
    private final FileNaming parent;
    private final Integer id;
    private CharSequence currentName;

    protected FileName(final FileNaming parent, final File file, Integer theKey) {
        this.parent = parent;
        this.name = CharSequences.create(parseName(parent, file));
        this.id = theKey == null ? NamingFactory.createID(file) : theKey;
        this.currentName = name;
        boolean debug = false;
        assert debug = true;
        if (debug) {
            this.currentName = new Creation(name);
        }
    }

    private static String parseName(final FileNaming parent, final File file) {
        return parent == null ? file.getPath() : file.getName();
    }

    @Override
    public FileNaming rename(String name, ProvidedExtensions.IOHandler handler) throws IOException {
        boolean success = false;
        final File f = getFile();

        if (FileChangedManager.getInstance().exists(f)) {
            File newFile = new File(f.getParentFile(), name);
            if (handler != null) {
                handler.handle();
                success = true;
            } else {
                success = f.renameTo(newFile);
            }
            if (success) {
                FolderName.freeCaches();
                return NamingFactory.fromFile(getParent(), newFile, true);
            }
        }
        return this;
    }

    public @Override final boolean isRoot() {
        return (getParent() == null);
    }


    public @Override File getFile() {
        final FileNaming myParent = this.getParent();
        return (myParent != null) ? new File(myParent.getFile(), getName()) : new File(getName());
    }


    public @Override final String getName() {
        return currentName.toString();
    }

    public @Override FileNaming getParent() {
        return parent;
    }

    public final @Override Integer getId() {
        return id;
    }

    public final @Override boolean equals(final Object obj) {
        if (obj instanceof FileName ) {
            FileName fn = (FileName)obj;
            if (obj.hashCode() != hashCode()) {
                return false;
            }
            if (!name.equals(fn.name)) {
                return false;
            }
            if (parent == null || fn.parent == null) {
                return parent == null && fn.parent == null;
            }
            return parent.equals(fn.parent);
        }
        return (obj instanceof FileNaming && obj.hashCode() == hashCode());
    }


    public final @Override String toString() {
        return getFile().getAbsolutePath();
    }

    public final @Override int hashCode() {
        return id.intValue();
    }

    public @Override boolean isFile() {
        return true;
    }

    public @Override boolean isDirectory() {
        return !isFile();
    }

    void updateCase(String name) {
        assert String.CASE_INSENSITIVE_ORDER.compare(name, this.name.toString()) == 0: "Only case can be changed. Was: " + this.name + " name: " + name;
        final CharSequence value = CharSequences.create(name);
        if (this.currentName instanceof Creation) {
            ((Creation)this.currentName).delegate = value;
        } else {
            this.currentName = value;
        }
    }

    public void dumpCreation(StringBuilder sb) {
        if (this.currentName instanceof Creation) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ((Creation)this.currentName).printStackTrace(pw);
            pw.close();
            sb.append(sw.toString());
        }
    }

    final void recordCleanup(String msg) {
        Throwable ex = null;
        if (this.currentName instanceof Creation) {
            ex = ((Creation)this.currentName);
        }
        if (ex != null) {
            while (ex.getCause() != null) {
                ex = ex.getCause();
            }
            ex.initCause(new Exception("Reference cleanup: " + msg)); // NOI18N
        }
    }
    
    private static final class Creation extends Exception 
    implements CharSequence {
        CharSequence delegate;

        private Creation(CharSequence name) {
            delegate = name;
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return delegate.equals(obj);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return delegate.subSequence(start, end);
        }

        @Override
        public int length() {
            return delegate.length();
        }

        @Override
        public char charAt(int index) {
            return delegate.charAt(index);
        }
    } // end of Creation
}

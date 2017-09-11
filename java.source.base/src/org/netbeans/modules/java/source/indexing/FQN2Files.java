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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.indexing;

import com.sun.tools.javac.api.DuplicateClassChecker;
import com.sun.tools.javac.code.Symbol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementScanner9;
import javax.tools.JavaFileObject;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
public final class FQN2Files implements DuplicateClassChecker {

    private static final Logger LOG = Logger.getLogger(FQN2Files.class.getName());
    private static final String FQN2FILES_FILE = "fqn2files.properties"; //NOI18N

    public static FQN2Files forRoot(final URL root) throws IOException {
        return new FQN2Files(root);
    }
    private final File propFile;
    private final Properties props = new Properties();

    private FQN2Files(final URL root) throws IOException {
        this.propFile = new File(JavaIndex.getIndex(root), FQN2FILES_FILE);
        this.propFile.createNewFile();
        load();
    }


    public void set(final Iterable<? extends Element> topLevelElements, final URL file) {
        new ElementScanner9() {
            @Override
            public Object visitType(TypeElement e, Object p) {
                String fqn = e instanceof Symbol.TypeSymbol
                        ? ((Symbol.TypeSymbol)e).flatName().toString()
                        : e.getQualifiedName().toString();
                String value = props.getProperty(fqn);
                if (value == null) {
                    props.setProperty(fqn, file.toExternalForm());
                }
                return super.visitType(e, p);
            }
            @Override
            public Object visitModule(ModuleElement e, Object p) {
                if (e instanceof Symbol.ModuleSymbol) {
                    final String fqn = ((Symbol.ModuleSymbol)e).module_info.flatName().toString();
                    String value = props.getProperty(fqn);
                    if (value == null) {
                        props.setProperty(fqn, file.toExternalForm());
                    }
                }
                return null;
            }
            @Override
            public Object visitPackage(PackageElement e, Object p) {
                if (e instanceof Symbol.PackageSymbol) {
                    final String fqn = ((Symbol.PackageSymbol)e).package_info.flatName().toString();
                    String value = props.getProperty(fqn);
                    if (value == null) {
                        props.setProperty(fqn, file.toExternalForm());
                    }
                }
                return null;
            }            
        }.scan(topLevelElements, null);
    }

    public boolean remove(final String fqn, final URL file) {
        String value = props.getProperty(fqn);
        if (value != null && value.equals(file.toExternalForm())) {
            props.remove(fqn);
            return true;
        }
        return false;
    }

    public void store() throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(propFile));
        try {
            props.store(out, ""); //NOI18N
        } finally {
            out.close();
        }
    }

    private void load() throws IOException {
        final InputStream in = new BufferedInputStream(new FileInputStream(propFile), 65536);
        try {
            props.load(in);
        } catch (IllegalArgumentException iae) {
            props.clear();
            LOG.log(Level.WARNING, "Broken {0}, ignoring.", propFile.getAbsolutePath());    //NOI18N
        } finally {
            in.close();
        }
    }

    @Override
    public boolean check(final Name fqn, final JavaFileObject jfo) {
        String value = props.getProperty(fqn.toString());
        try {
            return value != null && !value.equals(jfo.toUri().toURL().toExternalForm());
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    public boolean check(final String fqn, final URL url) {
        String value = props.getProperty(fqn);
        return value != null && !value.equals(url.toExternalForm());
    }
}

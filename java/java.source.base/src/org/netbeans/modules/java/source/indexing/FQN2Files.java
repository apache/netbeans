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
package org.netbeans.modules.java.source.indexing;

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
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner9;
import javax.tools.JavaFileObject;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
public final class FQN2Files {

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
                return scan(e.getEnclosedElements(), p);
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
            @Override
            public Object visitRecordComponent(RecordComponentElement e, Object p) {
                return visitVariable((VariableElement) e, p);
            }
            @Override
            public Object scan(Element e, Object p) {
                return super.scan(e, p);
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

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.lib.nbjavac.services;

import com.sun.tools.javac.code.ClassFinder.BadClassFile;
import java.util.Set;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.jvm.ClassFile;
import com.sun.tools.javac.jvm.ClassFile.Version;
import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.resources.CompilerProperties.Warnings;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.ForwardingJavaFileObject;
import javax.tools.JavaFileObject;

/**
 *
 * @author lahvac
 */
public class NBClassReader extends ClassReader {

    private static final Logger LOG = Logger.getLogger(NBClassReader.class.getName());

    public static void preRegister(Context context) {
        context.put(classReaderKey, new Context.Factory<ClassReader>() {
            @Override
            public ClassReader make(Context c) {
                return new NBClassReader(c);
            }
        });
    }

    private final Names names;
    private final NBNames nbNames;
    private final Log log;

    public NBClassReader(Context context) {
        super(context);

        names = Names.instance(context);
        nbNames = NBNames.instance(context);
        log = Log.instance(context);

        NBAttributeReader[] readers = {
            new NBAttributeReader(nbNames._org_netbeans_EnclosingMethod, Version.V45_3, CLASS_OR_MEMBER_ATTRIBUTE) {
                public void read(Symbol sym, int attrLen) {
                    int newbp = bp + attrLen;
                    readEnclosingMethodAttr(sym);
                    bp = newbp;
                }
            },
            new NBAttributeReader(nbNames._org_netbeans_SourceLevelAnnotations, Version.V49, CLASS_OR_MEMBER_ATTRIBUTE) {
                protected void read(Symbol sym, int attrLen) {
                    attachAnnotations(sym);
                }

            },
            new NBAttributeReader(nbNames._org_netbeans_SourceLevelParameterAnnotations, Version.V49, CLASS_OR_MEMBER_ATTRIBUTE) {
                protected void read(Symbol sym, int attrLen) {
                    attachParameterAnnotations(sym);
                }

            },
            new NBAttributeReader(nbNames._org_netbeans_SourceLevelTypeAnnotations, Version.V52, CLASS_OR_MEMBER_ATTRIBUTE) {
                protected void read(Symbol sym, int attrLen) {
                    attachTypeAnnotations(sym);
                }

            },
        };

        for (NBAttributeReader r: readers)
            attributeReaders.put(r.getName(), r);
    }

    @Override
    public void readClassFile(ClassSymbol c) {
        try {
            super.readClassFile(c);
        } catch (BadClassFile cf) {
            if ("compiler.misc.bad.class.file.header".equals(cf.getDiagnostic().getCode())) {
                JavaFileObject origFile = c.classfile;
                try (InputStream in = origFile.openInputStream()) {
                    byte[] data = readFile(in);
                    if (data.length > 8) {
                        int major = (Byte.toUnsignedInt(data[6]) << 8) + Byte.toUnsignedInt(data[7]);
                        int maxMajor = ClassFile.Version.MAX().major;
                        if (maxMajor < major) {
                            if (log.currentSourceFile() != null) {
                                log.warning(0, Warnings.BigMajorVersion(origFile, major, maxMajor));
                            }
                            data[6] = (byte) (maxMajor >> 8);
                            data[7] = (byte) (maxMajor & 0xFF);
                            byte[] dataFin = data;
                            c.classfile = new ForwardingJavaFileObject(origFile) {
                                @Override
                                public InputStream openInputStream() throws IOException {
                                    return new ByteArrayInputStream(dataFin);
                                }
                            };
                            super.readClassFile(c);
                            return ;
                        }
                    }
                } catch (IOException ex) {
                    LOG.log(Level.FINE, null, ex);
                } finally {
                    c.classfile = origFile;
                }
            }
            throw cf;
        }
    }

    static byte[] readFile(final InputStream in) throws IOException {
        byte[] data = new byte[Math.max(in.available(), 256)];
        int off = 0;
        int read;
        while ((read = in.read(data, off, data.length - off)) != (-1)) {
            off += read;
            if (data.length == off) {
                data = Arrays.copyOf(data, 2 * (data.length + in.available()));
            }
        }
        return Arrays.copyOf(data, off);
    }

    private void attachAnnotations(Symbol sym) {
        try {
            Method m = ClassReader.class.getDeclaredMethod("attachAnnotations", Symbol.class);
            m.setAccessible(true);
            m.invoke(this, sym);
        } catch (NoSuchMethodException | SecurityException |
                 IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void attachParameterAnnotations(Symbol sym) {
        try {
            Method m = ClassReader.class.getDeclaredMethod("readParameterAnnotations", Symbol.class);
            m.setAccessible(true);
            m.invoke(this, sym);
        } catch (NoSuchMethodException | SecurityException |
                 IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void attachTypeAnnotations(Symbol sym) {
        try {
            Method m = ClassReader.class.getDeclaredMethod("attachTypeAnnotations", Symbol.class);
            m.setAccessible(true);
            m.invoke(this, sym);
        } catch (NoSuchMethodException | SecurityException |
                 IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private abstract class NBAttributeReader extends AttributeReader {

        private NBAttributeReader(Name name, Version version, Set<AttributeKind> kinds) {
            super(name, version, kinds);
        }
        
        private Name getName() {
            return name;
        }
    }

}

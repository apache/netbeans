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

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Attribute.Compound;
import com.sun.tools.javac.code.Attribute.RetentionPolicy;
import com.sun.tools.javac.code.Attribute.TypeCompound;
import com.sun.tools.javac.code.Kinds.Kind;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.jvm.ClassWriter;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lahvac
 */
public class NBClassWriter extends ClassWriter {

    private static final Logger LOG = Logger.getLogger(NBClassWriter.class.getName());

    public static void preRegister(Context context) {
        context.put(classWriterKey, new Context.Factory<ClassWriter>() {
            public ClassWriter make(Context c) {
                return new NBClassWriter(c);
            }
        });
    }

    private final NBNames nbNames;
    private final Types types;

    protected NBClassWriter(Context context) {
        super(context);
        nbNames = NBNames.instance(context);
        types = Types.instance(context);
    }

    @Override
    protected int writeExtraAttributes(Symbol sym) {
        int attrs = super.writeExtraAttributes(sym);
        if (sym.kind == Kind.MTH) {
            attrs += writeExtraParameterAttributes((MethodSymbol) sym);
        }
        attrs += writeExtraJavaAnnotations(sym.getRawAttributes());
        attrs += writeExtraTypeAnnotations(sym.getRawTypeAttributes());
        return attrs;
    }


    protected int writeExtraParameterAttributes(MethodSymbol m) {
        boolean hasSourceLevel = false;
        if (m.params != null) for (VarSymbol s : m.params) {
            for (Attribute.Compound a : s.getRawAttributes()) {
                if (types.getRetention(a) == RetentionPolicy.SOURCE) {
                    hasSourceLevel = true;
                    break;
                }
            }
        }
        int attrCount = 0;
        if (hasSourceLevel) {
            int attrIndex = writeAttr(nbNames._org_netbeans_SourceLevelParameterAnnotations);
            databuf.appendByte(m.params.length());
            for (VarSymbol s : m.params) {
                ListBuffer<Attribute.Compound> buf = new ListBuffer<Attribute.Compound>();
                for (Attribute.Compound a : s.getRawAttributes())
                    if (types.getRetention(a) == RetentionPolicy.SOURCE)
                        buf.append(a);
                databuf.appendChar(buf.length());
                for (Attribute.Compound a : buf)
                    writeCompoundAttribute(a);
            }
            endAttr(attrIndex);
            attrCount++;
        }
        return attrCount;
    }

    protected int writeExtraJavaAnnotations(List<Attribute.Compound> attrs) {
        ListBuffer<Attribute.Compound> sourceLevel = new ListBuffer<Attribute.Compound>();
        for (Attribute.Compound a : attrs) {
            if (types.getRetention(a) == RetentionPolicy.SOURCE) {
                sourceLevel.append(a);
            }
        }
        int attrCount = 0;
        if (sourceLevel.nonEmpty()) {
            int attrIndex = writeAttr(nbNames._org_netbeans_SourceLevelAnnotations);
            databuf.appendChar(sourceLevel.length());
            for (Attribute.Compound a : sourceLevel)
                writeCompoundAttribute(a);
            endAttr(attrIndex);
            attrCount++;
        }
        return attrCount;
    }

    protected int writeExtraTypeAnnotations(List<TypeCompound> attrs) {
        ListBuffer<Attribute.TypeCompound> sourceLevel = new ListBuffer<Attribute.TypeCompound>();
        for (Attribute.TypeCompound tc : attrs) {
            if (tc.hasUnknownPosition()) {
                boolean fixed = tc.tryFixPosition();

                // Could we fix it?
                if (!fixed) {
                    // This happens for nested types like @A Outer. @B Inner.
                    // For method parameters we get the annotation twice! Once with
                    // a valid position, once unknown.
                    // TODO: find a cleaner solution.
                    continue;
                }
            }

            if (tc.position.type.isLocal())
                continue;
            if (!tc.position.emitToClassfile()) {
                continue;
            }
            if (types.getRetention(tc) == RetentionPolicy.SOURCE) {
                sourceLevel.append(tc);
            }
        }
        int attrCount = 0;
        if (sourceLevel.nonEmpty()) {
            int attrIndex = writeAttr(nbNames._org_netbeans_SourceLevelTypeAnnotations);
            databuf.appendChar(sourceLevel.length());
            for (Attribute.TypeCompound p : sourceLevel)
                writeTypeAnnotation(p);
            endAttr(attrIndex);
            attrCount++;
        }
        return attrCount;
    }

    private void writeCompoundAttribute(Compound a) {
        try {
            Method m = ClassWriter.class.getDeclaredMethod("writeCompoundAttribute", Compound.class);
            m.setAccessible(true);
            m.invoke(this, a);
        } catch (NoSuchMethodException | SecurityException |
                 IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void writeTypeAnnotation(TypeCompound p) {
        try {
            Method m = ClassWriter.class.getDeclaredMethod("writeTypeAnnotation", TypeCompound.class);
            m.setAccessible(true);
            m.invoke(this, p);
        } catch (NoSuchMethodException | SecurityException |
                 IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}

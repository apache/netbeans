/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.lib.nbjavac.services;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Attribute.RetentionPolicy;
import com.sun.tools.javac.code.Attribute.TypeCompound;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.jvm.ClassWriter;
import com.sun.tools.javac.jvm.Target;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import java.util.Collection;

/**
 *
 * @author lahvac
 */
public class NBClassWriter extends ClassWriter {

    public static void preRegister(Context context) {
        context.put(classWriterKey, new Context.Factory<ClassWriter>() {
            public ClassWriter make(Context c) {
                return new NBClassWriter(c);
            }
        });
    }

    private final NBNames nbNames;
    private final NBMessager nbMessager;
    private final Types types;

    protected NBClassWriter(Context context) {
        super(context);
        nbNames = NBNames.instance(context);
        nbMessager = NBMessager.instance(context);
        types = Types.instance(context);
    }
    
    @Override
    protected int writeExtraClassAttributes(ClassSymbol c) {
        if (c.sourcefile != null) {
            final Collection<? extends ClassSymbol> nip = nbMessager.removeNotInProfile(c.sourcefile.toUri());
            if (nip != null) {
                for (ClassSymbol s : nip) {
                    pool.put(s.type);
                }
            }
        }
        return 0;
    }

    @Override
    protected int writeExtraMemberAttributes(Symbol sym) {
        int attrCount = 0;
        if (sym.externalType(types).isErroneous()) {
            int rsIdx = writeAttr(nbNames._org_netbeans_TypeSignature);
            try {
                preserveErrors = true;
                databuf.appendChar(pool.put(typeSig(sym.type)));
            } finally {
                preserveErrors = false;
            }
            endAttr(rsIdx);
            attrCount++;
        }
        return attrCount;
    }

    @Override
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
        if (m.code == null && m.params != null && m.params.nonEmpty()) {
            int attrIndex = writeAttr(nbNames._org_netbeans_ParameterNames);
            for (VarSymbol s : m.params)
                databuf.appendChar(pool.put(s.name));
            endAttr(attrIndex);
            attrCount++;
        }
        return attrCount;
    }

    @Override
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

    @Override
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
}

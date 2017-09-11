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

import java.util.Set;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.jvm.ClassFile.Version;
import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

/**
 *
 * @author lahvac
 */
public class NBClassReader extends ClassReader {
    
    public static void preRegister(Context context) {
        context.put(classReaderKey, new Context.Factory<ClassReader>() {
            public ClassReader make(Context c) {
                return new NBClassReader(c);
            }
        });
    }

    private final Names names;
    private final NBNames nbNames;

    public NBClassReader(Context context) {
        super(context);

        names = Names.instance(context);
        nbNames = NBNames.instance(context);

        NBAttributeReader[] readers = {
            new NBAttributeReader(nbNames._org_netbeans_EnclosingMethod, Version.V45_3, CLASS_OR_MEMBER_ATTRIBUTE) {
                public void read(Symbol sym, int attrLen) {
                    int newbp = bp + attrLen;
                    readEnclosingMethodAttr(sym);
                    bp = newbp;
                }
            },
            new NBAttributeReader(nbNames._org_netbeans_TypeSignature, Version.V49, CLASS_OR_MEMBER_ATTRIBUTE) {
                protected void read(Symbol sym, int attrLen) {
                    sym.type = readType(nextChar());
                }
            },
            new NBAttributeReader(nbNames._org_netbeans_ParameterNames, Version.V49, CLASS_OR_MEMBER_ATTRIBUTE) {
                protected void read(Symbol sym, int attrLen) {
                    int newbp = bp + attrLen;
                    List<Name> parameterNames = List.nil();
                    int numParams = 0;
                    if (sym.type != null) {
                        List<Type> parameterTypes = sym.type.getParameterTypes();
                        if (parameterTypes != null)
                            numParams = parameterTypes.length();
                    }
                    for (int i = 0; i < numParams; i++) {
                        if (bp < newbp - 1)
                            parameterNames = parameterNames.prepend(readName(nextChar()));
                    }
                    parameterNames = parameterNames.reverse();
                    while(parameterNames.length() < numParams)
                        parameterNames = parameterNames.prepend(names.empty);
                    ((MethodSymbol)sym).savedParameterNames = parameterNames;
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
    
    private abstract class NBAttributeReader extends AttributeReader {

        private NBAttributeReader(Name name, Version version, Set<AttributeKind> kinds) {
            super(name, version, kinds);
        }
        
        private Name getName() {
            return name;
        }
    }

}

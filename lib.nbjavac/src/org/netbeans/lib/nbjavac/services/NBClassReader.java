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

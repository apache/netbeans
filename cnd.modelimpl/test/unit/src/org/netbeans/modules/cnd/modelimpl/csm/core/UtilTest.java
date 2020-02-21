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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.modelimpl.repository.KeyObjectFactory;
import org.netbeans.modules.cnd.modelimpl.repository.KeyPresentationFactoryImpl;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.test.ModelBasedTestCase;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.openide.util.CharSequences;

/**
 *
 */
public class UtilTest extends ModelBasedTestCase {

    public UtilTest(String testName) {
        super(testName);
    }

    @Test
    public void testConsistency() throws Exception {
        Set<Character> set = new HashSet<>();
        for(CsmDeclaration.Kind kind : CsmDeclaration.Kind.values()) {
            char csmDeclarationKindkey = Utils.getCsmDeclarationKindkey(kind);
            if (set.contains(csmDeclarationKindkey)) {
                assert false : "Duplicated key "+csmDeclarationKindkey+" for "+kind;
            }
            set.add(csmDeclarationKindkey);
            char charAt = csmDeclarationKindkey;
            assert Utils.getCsmDeclarationKind(charAt) == kind : "Undefined kind for char "+csmDeclarationKindkey;
            Key key = presentationFactory((short)charAt);
            assert key != null;
            assert KeyUtilities.getKeyChar(key) == charAt;
        }
        for(final CsmVisibility kind : CsmVisibility.values()) {
            char csmInheritanceKindKey = Utils.getCsmInheritanceKindKey(new CsmInheritance() {

                @Override
                public CsmClassifier getClassifier() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public CsmVisibility getVisibility() {
                    return kind;
                }

                @Override
                public boolean isVirtual() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public CsmType getAncestorType() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public CsmFile getContainingFile() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public int getStartOffset() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public int getEndOffset() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Position getStartPosition() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Position getEndPosition() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public CharSequence getText() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public CsmScope getScope() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
            if (set.contains(csmInheritanceKindKey)) {
                assert false : "Duplicated key "+csmInheritanceKindKey+" for "+kind;
            }
            set.add(csmInheritanceKindKey);
            char charAt = csmInheritanceKindKey;
            assert Utils.getCsmVisibility(charAt) == kind : "Undefined kind for char "+csmInheritanceKindKey;
            Key key = presentationFactory2((short)charAt);
            assert key != null;
            assert KeyUtilities.getKeyChar(key) == charAt;
        }
        char key = Utils.getCsmIncludeKindKey();
        assert !set.contains(key) : "Duplicated key "+key;
        set.add(key);
        Key aKey = presentationFactory3((short)key);
        assert aKey != null;
        assert KeyUtilities.getKeyChar(aKey) == key;
        
        key = Utils.getCsmParamListKindKey();
        assert !set.contains(key) : "Duplicated key "+key;
        set.add(key);
        aKey = presentationFactory4((short)key);
        assert aKey != null;
        assert KeyUtilities.getKeyChar(aKey) == key;

        key = Utils.getCsmInstantiationKindKey();
        assert !set.contains(key) : "Duplicated key "+key;
        set.add(key);
        aKey = presentationFactory5((short)key);
        assert aKey != null;
        assert KeyUtilities.getKeyChar(aKey) == key;
        assertNoExceptions();
    }

    private Key presentationFactory(final short kind) {
        KeyDataPresentation presentation = new KeyDataPresentationImpl() {

            @Override
            public short getHandler() {
                CsmDeclaration.Kind csmDeclarationKind = Utils.getCsmDeclarationKind((char) kind);
                switch (csmDeclarationKind) {
                    case BUILT_IN:
                        return KeyObjectFactory.KEY_BUILT_IN_KEY;
                    case CLASS:
                        return KeyObjectFactory.KEY_CLASS_KEY;
                    case UNION:
                        return KeyObjectFactory.KEY_UNION_KEY;
                    case STRUCT:
                        return KeyObjectFactory.KEY_STRUCT_KEY;
                    case ENUM:
                        return KeyObjectFactory.KEY_ENUM_KEY;
                    case ENUMERATOR:
                        return KeyObjectFactory.KEY_ENUMERATOR_KEY;
                    case MACRO:
                        return KeyObjectFactory.KEY_MACRO_KEY;
                    case VARIABLE:
                        return KeyObjectFactory.KEY_VARIABLE_KEY;
                    case VARIABLE_DEFINITION:
                        return KeyObjectFactory.KEY_VARIABLE_DEFINITION_KEY;
                    case FUNCTION:
                        return KeyObjectFactory.KEY_FUNCTION_KEY;
                    case FUNCTION_DEFINITION:
                        return KeyObjectFactory.KEY_FUNCTION_DEFINITION_KEY;
                    case FUNCTION_INSTANTIATION:
                        return KeyObjectFactory.KEY_FUNCTION_INSTANTIATION_KEY;
                    case FUNCTION_LAMBDA:
                        return KeyObjectFactory.KEY_FUNCTION_LAMBDA_KEY;
                    case TEMPLATE_SPECIALIZATION:
                        return KeyObjectFactory.KEY_TEMPLATE_SPECIALIZATION_KEY;
                    case TYPEDEF:
                        return KeyObjectFactory.KEY_TYPEDEF_KEY;
                    case TYPEALIAS:
                        return KeyObjectFactory.KEY_TYPEALIAS_KEY;
                    case ASM:
                        return KeyObjectFactory.KEY_ASM_KEY;
                    case TEMPLATE_DECLARATION:
                        return KeyObjectFactory.KEY_TEMPLATE_DECLARATION_KEY;
                    case NAMESPACE_DEFINITION:
                        return KeyObjectFactory.KEY_NAMESPACE_DEFINITION_KEY;
                    case TEMPLATE_PARAMETER:
                        return KeyObjectFactory.KEY_TEMPLATE_PARAMETER_KEY;
                    case NAMESPACE_ALIAS:
                        return KeyObjectFactory.KEY_NAMESPACE_ALIAS_KEY;
                    case USING_DIRECTIVE:
                        return KeyObjectFactory.KEY_USING_DIRECTIVE_KEY;
                    case USING_DECLARATION:
                        return KeyObjectFactory.KEY_USING_DECLARATION_KEY;
                    case CLASS_FORWARD_DECLARATION:
                        return KeyObjectFactory.KEY_CLASS_FORWARD_DECLARATION_KEY;
                    case ENUM_FORWARD_DECLARATION:
                        return KeyObjectFactory.KEY_ENUM_FORWARD_DECLARATION_KEY;
                    case CLASS_FRIEND_DECLARATION:
                        return KeyObjectFactory.KEY_CLASS_FRIEND_DECLARATION_KEY;
                    case FUNCTION_FRIEND:
                        return KeyObjectFactory.KEY_FUNCTION_FRIEND_KEY;
                    case FUNCTION_FRIEND_DEFINITION:
                        return KeyObjectFactory.KEY_FUNCTION_FRIEND_DEFINITION_KEY;
                    case FUNCTION_TYPE:
                        return KeyObjectFactory.KEY_FUNCTION_TYPE_KEY;
                }
                throw new IllegalArgumentException(""+(char) kind);
            }
        };
        KeyPresentationFactoryImpl impl = new KeyPresentationFactoryImpl();
        return impl.create(presentation);
    }
    
    private Key presentationFactory2(final short kind) {
        KeyDataPresentation presentation = new KeyDataPresentationImpl() {

            @Override
            public short getHandler() {
                CsmVisibility csmVisibility = Utils.getCsmVisibility((char) kind);
                switch (csmVisibility) {
                    case NONE:
                        return KeyObjectFactory.KEY_INHERITANCE_NONE_KEY;
                    case PRIVATE:
                        return KeyObjectFactory.KEY_INHERITANCE_PRIVATE_KEY;
                    case PROTECTED:
                        return KeyObjectFactory.KEY_INHERITANCE_PROTECTED_KEY;
                    case PUBLIC:
                        return KeyObjectFactory.KEY_INHERITANCE_PUBLIC_KEY;
                }
                throw new IllegalArgumentException(""+(char) kind);
            }
        };
        KeyPresentationFactoryImpl impl = new KeyPresentationFactoryImpl();
        return impl.create(presentation);
    }

    private Key presentationFactory3(final short kind) {
        KeyDataPresentation presentation = new KeyDataPresentationImpl() {

            @Override
            public short getHandler() {
                assert kind == Utils.getCsmIncludeKindKey();
                return KeyObjectFactory.KEY_INCLUDE_KEY;
            }
            
        };
        KeyPresentationFactoryImpl impl = new KeyPresentationFactoryImpl();
        return impl.create(presentation);
    }

    private Key presentationFactory4(final short kind) {
        KeyDataPresentation presentation = new KeyDataPresentationImpl() {

            @Override
            public short getHandler() {
                assert kind == Utils.getCsmParamListKindKey();
                return KeyObjectFactory.KEY_PARAM_LIST_KEY;
            }
            
        };
        KeyPresentationFactoryImpl impl = new KeyPresentationFactoryImpl();
        return impl.create(presentation);
    }

    private Key presentationFactory5(final short kind) {
        KeyDataPresentation presentation = new KeyDataPresentationImpl() {

            @Override
            public short getHandler() {
                assert kind == Utils.getCsmInstantiationKindKey();
                return KeyObjectFactory.KEY_INSTANTIATION_KEY;
            }
            
        };
        KeyPresentationFactoryImpl impl = new KeyPresentationFactoryImpl();
        return impl.create(presentation);
    }

    private abstract class KeyDataPresentationImpl implements KeyDataPresentation {

        @Override
        public int getUnitPresentation() {
            return 10000 + 1;
        }

        @Override
        public CharSequence getNamePresentation() {
            return CharSequences.empty();
        }

        @Override
        public int getFilePresentation() {
            return 0;
        }

        @Override
        public int getStartPresentation() {
            return 0;
        }

        @Override
        public int getEndPresentation() {
            return 0;
        }
    }
}

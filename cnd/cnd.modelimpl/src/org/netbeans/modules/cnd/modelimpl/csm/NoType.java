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

package org.netbeans.modules.cnd.modelimpl.csm;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.*;

/**
 * Used as return type for constructor and destructor
 * 
 * TODO: replace implementations of all methods with throwing UnsupportedOperationException?
 */
public class NoType implements CsmType {

    private static final NoType instance = new NoType();

//    private Position position = new Position() {
//
//        public int getOffset() {
//            return 0;
//        }
//
//        public int getLine() {
//            return 0;
//        }
//
//        public int getColumn() {
//            return 0;
//        }
//    };
    
    /** prevents external creation */
    private NoType() {
    }
    
    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public boolean isRValueReference() {
        return false;
    }
    
    @Override
    public boolean isPointer() {
        return false;
    }
    
    @Override
    public boolean isConst() {
        return false;
    }

    @Override
    public boolean isVolatile() {
        return false;
    }

    @Override
    public CharSequence getText() {
        return "";
    }
    
    @Override
    public CharSequence getCanonicalText() {
	return "";
    }
    
    @Override
    public Position getStartPosition() {
        return null;
    }

    @Override
    public int getStartOffset() {
        return 0;
    }

    @Override
    public int getPointerDepth() {
        return 0;
    }

    @Override
    public Position getEndPosition() {
        return null;
    }

    @Override
    public int getEndOffset() {
        return 0;
    }

    @Override
    public CsmFile getContainingFile() {
        return null;
    }

    @Override
    public CsmClassifier getClassifier() {
        return null;
    }

    @Override
    public List<CsmSpecializationParameter> getInstantiationParams() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasInstantiationParams() {
        return false;
    }

    @Override
    public boolean isInstantiation() {
        return false;
    }

    @Override
    public boolean isPackExpansion() {
        return false;
    }

    @Override
    public boolean isTemplateBased() {
        return false;
    }

    @Override
    public CharSequence getClassifierText() {
        return "";
    }

    @Override
    public int getArrayDepth() {
        return 0;
    }
    
    public static NoType instance() {
        return instance;
    }

    @Override
    public boolean isBuiltInBased(boolean resolveTypeChain) {
        return false;
    }
}

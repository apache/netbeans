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
package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;

/**
 * A key for CsmInclude objects (file and offset -based)
 */

/*package*/ abstract class InheritanceKey extends OffsetableKey {

    
    /*package*/ static InheritanceKey createInheritanceKey(CsmInheritance obj) {
        switch(obj.getVisibility()) {
            case PUBLIC:
                return new PUBLIC(obj);
            case PRIVATE:
                return new PRIVATE(obj);
            case PROTECTED:
                return new PROTECTED(obj);
            case NONE:
                return new NONE(obj);
        }
        throw new IllegalArgumentException();
    }

    private InheritanceKey(CsmInheritance obj) {
        super(obj, obj.getAncestorType().getClassifierText()); // NOI18N
    }

    private InheritanceKey(RepositoryDataInput aStream) throws IOException {
        super(aStream);
    }

    private InheritanceKey(KeyDataPresentation presentation) {
        super(presentation);
    }

    @Override
    public PersistentFactory getPersistentFactory() {
        return CsmObjectFactory.instance();
    }

    @Override
    public String toString() {
        String retValue;

        retValue = "InhKey: " + super.toString(); // NOI18N
        return retValue;
    }

    @Override
    public int getSecondaryDepth() {
        return super.getSecondaryDepth() + 1;
    }

    @Override
    public int getSecondaryAt(int level) {
        if (level == 0) {
            return getHandler();
        } else {
            return super.getSecondaryAt(level - 1);
        }
    }
    
    static final class PUBLIC extends InheritanceKey {
        PUBLIC(CsmInheritance obj) {super(obj);}
        PUBLIC(KeyDataPresentation presentation) {super(presentation);}
        PUBLIC(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmInheritanceKindKey(CsmVisibility.PUBLIC);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_INHERITANCE_PUBLIC_KEY;}
    }
    static final class PRIVATE extends InheritanceKey {
        PRIVATE(CsmInheritance obj) {super(obj);}
        PRIVATE(KeyDataPresentation presentation) {super(presentation);}
        PRIVATE(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmInheritanceKindKey(CsmVisibility.PRIVATE);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_INHERITANCE_PRIVATE_KEY;}
    }
    static final class PROTECTED extends InheritanceKey {
        PROTECTED(CsmInheritance obj) {super(obj);}
        PROTECTED(KeyDataPresentation presentation) {super(presentation);}
        PROTECTED(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmInheritanceKindKey(CsmVisibility.PROTECTED);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_INHERITANCE_PROTECTED_KEY;}
    }
    static final class NONE extends InheritanceKey {
        NONE(CsmInheritance obj) {super(obj);}
        NONE(KeyDataPresentation presentation) {super(presentation);}
        NONE(RepositoryDataInput aStream) throws IOException {super(aStream);}
        @Override char getKind() {return Utils.getCsmInheritanceKindKey(CsmVisibility.NONE);}
        @Override public short getHandler() {return KeyObjectFactory.KEY_INHERITANCE_NONE_KEY;}
    }
}

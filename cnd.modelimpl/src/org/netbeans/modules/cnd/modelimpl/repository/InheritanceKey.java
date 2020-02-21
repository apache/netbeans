/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

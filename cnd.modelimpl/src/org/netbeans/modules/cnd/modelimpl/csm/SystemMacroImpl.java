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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMacroParameter;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.CsmParameterList;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.Unresolved;
import org.netbeans.modules.cnd.modelimpl.parser.clank.MacroReference;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDProviderIml;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.openide.util.CharSequences;
import org.netbeans.modules.cnd.utils.cache.TextCache;

/**
 * Implementation of system macros and user-defined (in project properties) ones.
 *
 */
public final class SystemMacroImpl implements CsmMacro, CsmIdentifiable {
    
    private final CharSequence macroName;
    private final CharSequence macroBody;
    private final Kind macroKind;
    private final List<CharSequence> params;
    private final CsmFile containingFile;
    private final CsmUID<CsmMacro> uid;

    private SystemMacroImpl(CharSequence macroName, CharSequence macroBody, List<CharSequence> macroParams, CsmFile containingFile, Kind macroKind) {
        this.macroName = NameCache.getManager().getString(macroName);
        this.macroBody = TextCache.getManager().getString(macroBody);
        this.macroKind = macroKind;
        if (macroParams != null) {
            this.params = Collections.unmodifiableList(macroParams);
        } else {
            this.params = null;
        }
        this.containingFile = containingFile;
        if (APTTraceFlags.USE_CLANK) {
            this.uid = new BuiltInMacroUID(UIDs.get(containingFile), macroName);
        } else {
            assert containingFile instanceof Unresolved.UnresolvedFile;
            this.uid = UIDProviderIml.createSelfUID((CsmMacro)this);
        }
    }

    public static SystemMacroImpl create(CharSequence macroName, CharSequence macroBody, List<CharSequence> macroParams, CsmFile containingFile, Kind macroKind) {
        return new SystemMacroImpl(macroName, macroBody, macroParams, containingFile, macroKind);
    }
    
    @Override
    public List<CharSequence> getParameters() {
        return params;
    }

    @Override
    public CharSequence getBody() {
        return macroBody;
    }

    @Override
    public Kind getKind() {
        return macroKind;
    }

    @Override
    public CharSequence getName() {
        return macroName;
    }

    @Override
    public CsmFile getContainingFile() {
        return containingFile;
    }

    @Override
    public int getStartOffset() {
        return 0;
    }

    @Override
    public int getEndOffset() {
        return 0;
    }

    @Override
    public Position getStartPosition() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public Position getEndPosition() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public CharSequence getText() {
        return "#define " + macroName + " " + macroBody; // NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        boolean retValue;
        if (obj == null || !(obj instanceof SystemMacroImpl)) {
            retValue = false;
        } else {
            SystemMacroImpl other = (SystemMacroImpl)obj;
            retValue = CharSequences.comparator().compare(getName(), other.getName()) == 0;
        }
        return retValue;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.macroName != null ? this.macroName.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder retValue = new StringBuilder();
        retValue.append("#define '"); // NOI18N
        retValue.append(getName());
        if (getParameters() != null) {
            retValue.append("["); // NOI18N
            for (Iterator<CharSequence> it = getParameters().iterator(); it.hasNext();) {
                CharSequence param = it.next();
                retValue.append(param);
                if (it.hasNext()) {
                    retValue.append(", "); // NOI18N
                }                
            }
            retValue.append("]"); // NOI18N
        }
        if (getBody().length() > 0) {
            retValue.append("'='"); // NOI18N
            retValue.append(getBody());
        }
        retValue.append("' [").append(macroKind == Kind.USER_SPECIFIED ? "user defined" : "system").append("]"); // NOI18N
        return retValue.toString();
    }

    @Override
    public CsmParameterList<CsmMacroParameter> getParameterList() {
        return null;
    }

    @Override
    public CsmUID<?> getUID() {
        return uid;
    }

    private static class FactoryBasedUID<I,T> implements CsmUID<T>, SelfPersistent {
        private final CsmUID<I> instance;
        private final CharSequence argument;

        protected FactoryBasedUID(CsmUID<I> instance, CharSequence argument) {
            this.instance = instance;
            this.argument = argument;
        }

        @Override
        public T getObject() {
            I object = instance.getObject();
            if (object == null) {
                // could be request from delayed clients like Sema HL over deleted project
                return null;
            }
            FileImpl file = (FileImpl)object;
            CharSequence body = MacroReference.findBody(file, argument);
            if (body == null) {
                // macros is not defined in item properties.
                body = "";
            }
            SystemMacroImpl res = SystemMacroImpl.create(argument, body, null, file, MacroReference.findType(file, argument));
            return (T)res;
        }

        @Override
        public void write(RepositoryDataOutput aStream) throws IOException {
            UIDObjectFactory.getDefaultFactory().writeUID(instance, aStream);
            aStream.writeCharSequenceUTF(argument);
        }

        public FactoryBasedUID(RepositoryDataInput aStream) throws IOException {
            instance = UIDObjectFactory.getDefaultFactory().readUID(aStream);
            argument = aStream.readCharSequenceUTF();
        }
    }

    public static final class BuiltInMacroUID extends FactoryBasedUID<CsmFile, CsmMacro> {

        public BuiltInMacroUID(CsmUID<CsmFile> instance, CharSequence argument) {
            super(instance, argument);
        }

        public BuiltInMacroUID(RepositoryDataInput aStream) throws IOException {
            super(aStream);
        }
    }
}

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

package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMacroParameter;
import org.netbeans.modules.cnd.api.model.CsmParameterList;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableIdentifiableBase;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.DefaultCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.openide.util.CharSequences;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Implements CsmMacro
 * represents file defined macros:
 * #define SUM(a, b) ((a)+(b))
 * #define MACRO VALUE
 * #define MACRO
 *
 */
public final class MacroImpl extends OffsetableIdentifiableBase<CsmMacro> implements CsmMacro {
    
    /** name of macros, i.e. SUM or MACRO */
    private final CharSequence name;
    
    /** 
     * body of macros, 
     * i.e. ((a)+(b)) or VALUE, or empty string
     */
    private final CharSequence body;
    
    /** 
     * flag to distinguish system and other types of macros 
     * now we support only macros in file => all macros are not system
     */
    private final Kind kind;
    
    /** 
     * immutable list of parameters, 
     * i.e. [a, b] or null if macros without parameters
     */
    private final List<CharSequence> params;
    
    
    private MacroImpl(CharSequence macroName, List<CharSequence> macroParams, CharSequence macroBody, CsmFile containingFile, int startOffset, int endOffset, Kind kind) {
        super(containingFile, startOffset, endOffset);
        macroName = macroName == null ? CharSequences.empty() : macroName;
        assert(macroBody != null);
        this.name = NameCache.getManager().getString(macroName);
        this.kind = kind;
        this.body = DefaultCache.getManager().getString(macroBody);
        if (macroParams != null) {
            this.params = Collections.unmodifiableList(macroParams);
        } else {
            this.params = null;
        }
    }

    public static CsmMacro create(CharSequence macroName, List<CharSequence> macroParams, CharSequence macroBody, CsmFile containingFile, int startOffset, int endOffset, Kind kind) {
        return new MacroImpl(macroName, macroParams, macroBody, containingFile, startOffset, endOffset, kind);
    }
    
    @Override
    public List<CharSequence> getParameters() {
        return params;
    }
    
    @Override
    public CharSequence getBody() {
        return body;
    }
    
    @Override
    public Kind getKind() {
        return kind;
    }
    
    @Override
    public CharSequence getName() {
        return name;
    }

    public @Override String toString() {
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
        retValue.append("' ["); // NOI18N
        retValue.append(getStartPosition()).append("-").append(getEndPosition()); // NOI18N
        retValue.append("]"); // NOI18N
        return retValue.toString();
    }   
    
    public @Override boolean equals(Object obj) {
        boolean retValue;
        if (obj == null || !(obj instanceof MacroImpl)) {
            retValue = false;
        } else {
            MacroImpl other = (MacroImpl)obj;
            retValue = MacroImpl.equals(this, other);
        }
        return retValue;
    }
    
    private static boolean equals(MacroImpl one, MacroImpl other) {
        // compare only name and start offset
        return (one.getStartOffset() == other.getStartOffset()) && 
                (CharSequences.comparator().compare(one.getName(), other.getName()) == 0);
    }
    
    public @Override int hashCode() {
        int retValue = 17;
        retValue = 31*retValue + getStartOffset();
        retValue = 31*retValue + getName().hashCode();
        return retValue;
    }    

    public @Override void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);
        assert this.body != null;
        PersistentUtils.writeUTF(body, output);
        output.writeByte((byte)this.kind.ordinal());
        CharSequence[] out = this.params == null?null:this.params.toArray(new CharSequence[params.size()]);
        PersistentUtils.writeStrings(out, output);
    }

    public MacroImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.name != null;
        this.body = PersistentUtils.readUTF(input, DefaultCache.getManager());
        assert this.body != null;
        this.kind = Kind.values()[input.readByte()];
        CharSequence[] out = PersistentUtils.readStrings(input, NameCache.getManager());
        this.params = out == null ? null : Collections.unmodifiableList(Arrays.asList(out));
    }


    @Override
    protected CsmUID<CsmMacro> createUID() {
        return UIDUtilities.createMacroUID(this);
    }

    @Override
    public CsmParameterList<CsmMacroParameter> getParameterList() {
        throw new UnsupportedOperationException("Not supported yet."); //NOI18N
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacroParameter;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmParameterList;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * implementation of offsetable object to represent functions' parameters
 */
public class ParameterListImpl<T, K extends CsmNamedElement> extends OffsetableBase implements CsmParameterList<K> {

    private final Collection<CsmParameter> parameters;

    protected ParameterListImpl(CsmFile file, int start, int end, Collection<CsmParameter> parameters) {
        super(file, start, end);
        if (parameters == null || parameters.isEmpty()) {
            this.parameters = null;
        } else {
            this.parameters = parameters;
        }
    }
    
    @Override
    public final Collection<K> getParameters() {
        return _getParameters();
    }

    public final boolean isEmpty() {
        return parameters == null || parameters.isEmpty();
    }

    public final int getNrParameters() {
        return parameters == null ? 0 : parameters.size();
    }
    
    @Override
    public void dispose() {
        super.dispose();
    }

    private Collection<K> _getParameters() {
        if (this.parameters == null) {
            return Collections.<K>emptyList();
        } else {
            @SuppressWarnings("unchecked")
            Collection<K> collection = (Collection<K>) parameters;
            return Collections.unmodifiableCollection(collection);
        }
    }

    @Override
    public String toString() {
        return "ParamList " + super.toString(); // NOI18N
    }

//    @Override
//    public boolean equals(Object obj) {
//        boolean retValue;
//        if (obj == null || (obj.getClass() != this.getClass())) {
//            retValue = false;
//        } else {
//            ParameterListImpl other = (ParameterListImpl) obj;
//            retValue = ParameterListImpl.equals(this, other);
//        }
//        return retValue;
//    }
//    @Override
//    public int hashCode() {
//        int hash = super.hashCode();
//        hash = 47 * hash + (this.parameters != null ? this.parameters.hashCode() : 0);
//        return hash;
//    }
//
//    private static final boolean equals(ParameterListImpl one, ParameterListImpl other) {
//        // compare only name and start offset
//        return (one.getStartOffset() == other.getStartOffset()) &&
//                (one.getEndOffset() == other.getEndOffset()) &&
//                (one.getContainingFile().equals(other.getContainingFile()));
//    }
    ////////////////////////////////////////////////////////////////////////////
    // persistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeParameters(parameters, output);
    }
    
    public ParameterListImpl(RepositoryDataInput input, CsmScope scope) throws IOException {
        super(input);
        parameters = PersistentUtils.readParameters(input, scope);
    }

    ////////////////////////////////////////////////////////////////////////////
    // help factory methods
    //public static <T, K extends CsmNamedElement> ParameterListImpl<T, K> create(CsmFile file, int start, int end, Collection<K> parameters) {
    //    return new ParameterListImpl<T, K>(file, start, end, parameters);
    //}

    public static ParameterListImpl<CsmParameterList<CsmMacroParameter>, CsmMacroParameter> create(CsmFile file, APTMacro macro) {
        return create(file, macro.getName(), macro.getParams());
    }

    public static ParameterListImpl<CsmParameterList<CsmMacroParameter>, CsmMacroParameter> create(CsmFile file, APTDefine define) {
        return create(file, define.getName(), define.getParams());
    }

    private static ParameterListImpl<CsmParameterList<CsmMacroParameter>, CsmMacroParameter> create(CsmFile file, APTToken name, Collection<APTToken> params) {
        return null;
    }
}

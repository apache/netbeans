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
package org.netbeans.modules.cnd.apt.impl.support;

import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenAbstact;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;

/**
 * wrapper for APT nodes corresponding to preprocessor directives
 */
public final class APTPreprocessorToken extends APTTokenAbstact {
    private final APT ppNode;
    private final APTToken ppNodeToken;
    private final Map<APT, Map<Object, Object>> props;
    private final ResolvedPath resolvedPath;
    private final Boolean enterInclude;
    private static final Map<APT, Map<Object, Object>> EMPTY = Collections.emptyMap();
    private final PPIncludeHandler.IncludeState inclState;

    /**
     * 
     * @param ppNode
     * @param inclState resolved path include state
     * @param resolvedPath
     * @param props 
     */
    public APTPreprocessorToken(APT ppNode, Boolean enterInclude, PPIncludeHandler.IncludeState inclState, ResolvedPath resolvedPath, final Map<APT, Map<Object, Object>> props) {
        assert ppNode != null;
        this.ppNode = ppNode;
        this.enterInclude = enterInclude;
        this.inclState = inclState;
        this.resolvedPath = resolvedPath;
        this.ppNodeToken = ppNode.getToken();
        this.props = props == null ? EMPTY : props;
    }
    
    @Override
    public Object getProperty(Object key) {
        if (key == ResolvedPath.class) {
            return resolvedPath;
        } else if (key == Boolean.class) {
            return enterInclude;
        } else if (key == PPIncludeHandler.IncludeState.class) {
            return inclState;
        } else if (key == APT.class) {
            return ppNode;
        }
        Map<Object, Object> nodeProps = props.get(ppNode);
        return nodeProps == null ? null : nodeProps.get(key);
    }

    @Override
    public int getType() {
        return ppNodeToken.getType();
    }

    @Override
    public int getLine() {
        return ppNodeToken.getLine();
    }

    @Override
    public String getFilename() {
        return ppNodeToken.getFilename();
    }

    @Override
    public int getColumn() {
        return ppNodeToken.getColumn();
    }

    @Override
    public int getOffset() {
        return ppNodeToken.getOffset();
    }

    @Override
    public int getEndOffset() {
        return ppNode.getEndOffset();
    }

    @Override
    public CharSequence getTextID() {
        return ppNodeToken.getTextID();
    }

    @Override
    public String toString() {
        String suffix = super.toString();
        return (enterInclude ? "Before " : "After ") + inclState + " " + resolvedPath + " " + suffix; // NOI18N
    }        
}

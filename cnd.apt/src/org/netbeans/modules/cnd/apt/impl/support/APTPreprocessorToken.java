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

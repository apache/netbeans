/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.hints.spiimpl;

import com.sun.source.util.TreePath;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.spi.java.hints.HintContext;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public abstract class SPIAccessor {

    private static volatile SPIAccessor accessor;

    public static synchronized SPIAccessor getINSTANCE() {
        if (accessor == null) {
            try {
                Class.forName(HintContext.class.getName(), true, HintContext.class.getClassLoader());
                assert accessor != null;
            } catch (ClassNotFoundException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return accessor;
    }

    public static void setINSTANCE(SPIAccessor instance) {
        assert instance != null;
        accessor = instance;
    }

    public abstract HintContext createHintContext(CompilationInfo info, HintsSettings settings, HintMetadata metadata, TreePath path, Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> multiVariables, Map<String, String> variableNames, Map<String, TypeMirror> constraints, Collection<? super MessageImpl> problems, boolean bulkMode, AtomicBoolean cancel, int caret);
    public abstract HintContext createHintContext(CompilationInfo info, HintsSettings settings, HintMetadata metadata, TreePath path, Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> multiVariables, Map<String, String> variableNames);
    public abstract HintMetadata getHintMetadata(HintContext ctx);
    public abstract HintsSettings getHintSettings(HintContext ctx);

}

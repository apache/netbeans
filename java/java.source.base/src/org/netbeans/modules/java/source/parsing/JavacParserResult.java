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

package org.netbeans.modules.java.source.parsing;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.source.CompilationInfoAccessor;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public class JavacParserResult extends Parser.Result {
    
    private final CompilationInfo info;

    public JavacParserResult (final CompilationInfo info) {
        super (
            JavaSourceAccessor.getINSTANCE ().getCompilationInfoImpl (info).getSnapshot ()
        );
        Parameters.notNull("info", info);   //NOI18N
        this.info = info;
    }
        
    private boolean supports (Class<? extends CompilationInfo> clazz) {
        assert clazz != null;
        return clazz.isInstance(info);
    }
    
    public <T extends CompilationInfo> T get (final Class<T> clazz) {
        if (supports(clazz)) {
            return clazz.cast(info);
        }
        return null;
    }

    @Override
    public void invalidate() {
        JavaSourceAccessor.getINSTANCE().invalidate (info);
    }

    @Override
    public boolean processingFinished() {
        return !CompilationInfoAccessor.getInstance().getCompilationInfoImpl(info).isIncomplete();
    }
}

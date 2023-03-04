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
package org.netbeans.modules.jshell.editor;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.fold.FoldTypeProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@MimeRegistration(mimeType = "text/x-repl", service = FoldTypeProvider.class, position = 200)
public final class ConsoleFoldsProvider implements FoldTypeProvider {
    private static final Collection<FoldType>   types = new ArrayList<FoldType>(5);
    
    @NbBundle.Messages("FoldType_InitialInfo=Greeting Message")
    public static final FoldType    INITIAL_INFO = FoldType.INITIAL_COMMENT.derive(
            FoldType.INITIAL_COMMENT.code(), 
            Bundle.FoldType_InitialInfo(), 
            FoldType.INITIAL_COMMENT.getTemplate());
    
    @NbBundle.Messages("FoldType_ClasspathInfo=Classpath Info")
    public static final FoldType    CLASSPATH_INFO = FoldType.INITIAL_COMMENT.derive("classpath-info", 
            Bundle.FoldType_ClasspathInfo(), new
            FoldTemplate(0, 0, "{ ... }"));
    
    @NbBundle.Messages("FoldType_CommandOutput=Command output")
    public static final FoldType    OUTPUT  = FoldType.create("command-output", Bundle.FoldType_CommandOutput(),
                                    new FoldTemplate(0, 0, "..."));
    
    @NbBundle.Messages("FoldType_Message=Message")
    public static final FoldType    MESSAGE  = FoldType.create("message", Bundle.FoldType_Message(),
                                    new FoldTemplate(0, 0, "[...]"));

    static {
        types.add(INITIAL_INFO);
        types.add(CLASSPATH_INFO);
        types.add(OUTPUT);
        types.add(MESSAGE);
    }
    
    @Override
    public Collection getValues(Class type) {
        return types;
    }

    @Override
    public boolean inheritable() {
        return false;
    }
}

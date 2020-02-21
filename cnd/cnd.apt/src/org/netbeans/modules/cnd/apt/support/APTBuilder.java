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

package org.netbeans.modules.cnd.apt.support;

import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.impl.structure.APTBuilderImpl;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.filesystems.FileSystem;

/**
 * builds APT from TokenStream and APTLight from APT
 */
public final class APTBuilder {
    /** Creates a new instance of APTBuilder */
    private APTBuilder() {
    }

    public static APTFile buildAPT(FileSystem fileSystem, CharSequence path, TokenStream ts, APTFile.Kind aptKind) {
        CndUtils.assertTrueInConsole(!APTTraceFlags.USE_CLANK, "Not For Clank Mode");
        return new APTBuilderImpl().buildAPT(fileSystem, path, ts, aptKind);
    }
    
    public static APT buildAPTLight(APT apt) {
        CndUtils.assertTrueInConsole(!APTTraceFlags.USE_CLANK, "Not For Clank Mode");
        return APTBuilderImpl.buildAPTLight(apt);
    }
}

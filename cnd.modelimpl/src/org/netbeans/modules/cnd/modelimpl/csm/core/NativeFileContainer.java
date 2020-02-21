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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.project.NativeFileItem;

/**
 *
 */
public class NativeFileContainer {
    private final Map<CsmUID<CsmFile>, NativeFileItem> myFiles = new ConcurrentHashMap<>();

    /*package-local*/ NativeFileContainer(){
    }

    public final NativeFileItem getNativeFileItem(CsmUID<CsmFile> file) {
	return myFiles.get(file);
    }
    
    /*package-local*/ final void putNativeFileItem(CsmUID<CsmFile> file, NativeFileItem nativeFileItem) {
        assert nativeFileItem != null;
	myFiles.put(file, nativeFileItem);
    }
    
    /*package-local*/ final NativeFileItem removeNativeFileItem(CsmUID<CsmFile> file) {
	return myFiles.remove(file);
    }

    /*package-local*/ final void clear() {
	myFiles.clear();
    }
}

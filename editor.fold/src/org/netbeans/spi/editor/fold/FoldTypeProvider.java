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
package org.netbeans.spi.editor.fold;

import java.util.Collection;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * Provider of FoldType constants for the MimeType. 
 * The Provider should enumerate FoldTypes that apply to the given MIME type.
 * There can be multiple providers for a MIME type - some advanced constructions in
 * the language can be recognized / folded by extension modules. Consider Java vs.
 * Bean Patterns, or XML vs. Spring bean config.
 * <p/>
 * FoldTypes will be collected and some pieces of UI can present the folds, such 
 * as Auto-folding options.
 * <p/>
 * The Provider may specify inheritable=true; in that case the contributed FoldTypes
 * will become available for more specific MIME types, too. For example, if a FoldTypeProvider
 * for text/xml registers FoldTypes TAG and COMMENT with inheritable=true,
 * those FoldTypes will be listed also for text/x-ant+xml. This feature allows 
 * to "inject" Fold types and FoldManager on general MIME type ("") for all 
 * types of files.
 * 
 * @author sdedic
 * @since 1.35
 */
@MimeLocation(subfolderName = "FoldManager")
public interface FoldTypeProvider {
    /**
     * Enumerates values for the given type.
     * @return FoldType values.
     */
    public Collection getValues(Class type);
    
    /**
     * Determines whether the folds propagate to child mime types(paths).
     * If the method returns true, then more specific MIME types will also
     * list FoldTypes returned by this Provider. 
     * 
     * @return whether the provided FoldTypes should be inherited (true).
     */
    public boolean inheritable();
    
}

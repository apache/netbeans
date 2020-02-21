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

package org.netbeans.modules.cnd.modelimpl.syntaxerr.spi;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.openide.util.Lookup;

/**
 * An abstrasct class (a usual NB parttern - 
 * hybrid of an interface and a factory)
 * for filtering ANTLR recognition exceptions.
 * 
 * Implementation can
 * - filter out some particular sort of errors
 * - convert messages / info into human understandable format
 * 
 */
public abstract class ParserErrorFilter {

    /** A class that just joins all available filters to a single one */
    private static class JointFilter extends ParserErrorFilter {
        
        private final Lookup.Result<ParserErrorFilter> res;

        public JointFilter() {
            res = Lookup.getDefault().lookupResult(ParserErrorFilter.class);
        }
        
        
        @Override
        public void filter(Collection<CsmParserProvider.ParserError> parserErrors, Collection<CsmErrorInfo> result, 
                ReadOnlyTokenBuffer tokenBuffer, CsmFile file) {
            for( ParserErrorFilter filter : res.allInstances() ) {
                filter.filter(parserErrors, result, tokenBuffer, file);
            }
        }
    }
    
    private static final ParserErrorFilter DEFAULT = new JointFilter();  
    
    public static ParserErrorFilter getDefault() {
        return DEFAULT;
    }

    /**
     * Filters the collection of exceptions returned by ANTLR parser,
     * converts some (or all) of them to CsmErrorInfo
     * 
     * 
     * @param parserErrors the collection of exceptions returned by ANTLR parser. 
     * Feel free to remove some elements if the filter knows they are induced errors
     * and you don't want anyone to process them
     * 
     * @param result a collection to add resulting CsmErrorInfos to
     */
    abstract public void filter(
            Collection<CsmParserProvider.ParserError> parserErrors, 
            Collection<CsmErrorInfo> result,
            ReadOnlyTokenBuffer tokenBuffer,
            CsmFile file);
}

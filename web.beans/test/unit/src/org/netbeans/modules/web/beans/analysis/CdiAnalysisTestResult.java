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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.web.beans.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;


/**
 * @author ads
 *
 */
public class CdiAnalysisTestResult extends CdiAnalysisResult 
    implements TestProblems
{
    
    public CdiAnalysisTestResult( CompilationInfo info ) {
        super(info, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.CdiAnalysisResult#addError(javax.lang.model.element.Element, java.lang.String)
     */
    @Override
    public void addError( Element subject, String message ) {
        /* TODO: method signature needs to be changed to get a key 
         * (id of message ) of bundle instead of  localized message
         */ 
        myErrors.put( subject , message );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.CdiAnalysisResult#addNotification(org.netbeans.spi.editor.hints.Severity, javax.lang.model.element.Element, java.lang.String)
     */
    @Override
    public void addNotification( Severity severity, Element element,
            String message )
    {
        if ( severity == Severity.WARNING ){
            myWarnings.put( element , message );
        }
        else {
            assert false;
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.CdiAnalysisResult#getProblems()
     */
    @Override
    public List<ErrorDescription> getProblems() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.CdiAnalysisResult#requireCdiEnabled(javax.lang.model.element.Element)
     */
    @Override
    public void requireCdiEnabled( Element element ) {
    }
    
    @Override
    public Map<Element,String> getErrors(){
        return myErrors;
    }

    @Override
    public Map<Element,String> getWarings(){
        return myWarnings;
    }
    
    private Map<Element,String> myErrors = new HashMap<Element, String>();
    private Map<Element,String> myWarnings = new HashMap<Element, String>();
}

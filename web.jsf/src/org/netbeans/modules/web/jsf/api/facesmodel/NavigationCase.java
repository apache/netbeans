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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.web.jsf.api.facesmodel;

import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The "navigation-case" element describes a particular
 * combination of conditions that must match for this case to
 * be executed, and the view id of the component tree that
 * should be selected next.
 * @author Petr Pisl, ads
 */
public interface NavigationCase extends JSFConfigComponent, DescriptionGroup, 
    IdentifiableElement 
{
    
    String FROM_ACTION = JSFConfigQNames.FROM_ACTION.getLocalName();
    String FROM_OUTCOME = JSFConfigQNames.FROM_OUTCOME.getLocalName();
    String TO_VIEW_ID = JSFConfigQNames.TO_VIEW_ID.getLocalName();
    String REDIRECT = JSFConfigQNames.REDIRECT.getLocalName();
    
    String IF = JSFConfigQNames.IF.getLocalName();
    
    
    // TODO : Incorrect signature. FromAction should be separate element. 
    // It has additional attribute.
    public String getFromAction();
    public void setFromAction(String fromAction);
    
    public String getFromOutcome();
    public void setFromOutcome(String fromOutcome);
    
    /**
     * This method along with getter should not be used for JSF 2.0 spec.
     * Redirect has number of subelements and attributes.
     * Accessor methods to Redirect should be used instead.
     */
    public void setRedirected(boolean redirect);
    /**
     * This method along with setter should not be used for JSF 2.0 spec.
     * Redirect has number of subelements and attributes.
     * Accessor methods to Redirect should be used instead.
     */
    public boolean isRedirected();
    
    public String getToViewId();
    public void setToViewId(String toViewId);
    
    If getIf();
    void setIf( If iff );
    
    Redirect getRedirect();
    void setRedirect(Redirect redirect);
}

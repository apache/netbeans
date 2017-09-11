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

package org.netbeans.api.xml.cookies;

import org.openide.nodes.Node;

/**
 * Validate XML document entity (in polyform manner).
 * <p>Implemenmtation must follow XML 1.0 specification for processors
 * conformance. It is allowed to extend the contract to support domain
 * specifics semantics checks (i.e. XML Schema). It must not change UI state.
 * <p>
 * It should be gracefully served by all data objects and explorer nodes
 * representing validateable XML resources.
 * <p>
 * <h3>Use Cases</h3>
 * <ul>
 * <li><b>Provider</b> needs to define domain specifics semantics checks such as
 *     XML Schema based validation, Ant script dependency cycles checking, etc.
 * <li><b>Client</b> needs to check XML document entities transparently without
 *     appriory knowledge how to perform it for specifics XML document type.
 * </ul>
 *
 * @author      Petr Kuzel
 * @see         CheckXMLCookie
 * @see         <a href="http://www.w3.org/TR/REC-xml#proc-types">XML 1.0</a>     
 */
public interface ValidateXMLCookie extends Node.Cookie {

    /**
     * Validate XML document entity.
     * @param observer Optional listener (<code>null</code> allowed)
     *               giving judgement details via {@link XMLProcessorDetail}s.
     * @return true if validity check passes.
     */
    boolean validateXML(CookieObserver observer);

}

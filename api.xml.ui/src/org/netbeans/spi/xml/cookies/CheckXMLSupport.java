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
package org.netbeans.spi.xml.cookies;

import org.xml.sax.InputSource;

import org.netbeans.api.xml.cookies.*;

/**
 * <code>CheckXMLCookie</code> implementation support simplifing cookie providers
 * based on <code>InputSource</code>s representing XML documents and entities.
 * <p>
 * <b>Primary use case</b> in a DataObject subclass (which primary file is XML):
 * <pre>
 *   CookieSet cookies = getCookieSet();
 *   InputSource in = DataObjectAdapters.inputSource(this);
 *   CheckXMLSupport cookieImpl = new CheckXMLSupport(in);
 *   cookies.add(cookieImpl);
 * </pre>
 * <p>
 * <b>Secondary use case:</b> Subclasses can customize the class by customization
 * protected methods. The customized subclass can be used according to
 * primary use case.
 *
 * @author Petr Kuzel
 */
public class CheckXMLSupport extends SharedXMLSupport implements CheckXMLCookie {
        
    /**
     * General parsed entity strategy. This strategy works well only for
     * standalone (no entity reference) external entities.
     */
    public static final int CHECK_ENTITY_MODE = 1;
    
    /**
     * Parameter parsed entity strategy. This strategy is suitable for standalone
     * (no undeclared parameter entity reference) external DTDs.
     */
    public static final int CHECK_PARAMETER_ENTITY_MODE = 2;
    
    /**
     * XML document entity strategy. It is ordinary XML document processing mode.
     */
    public static final int DOCUMENT_MODE = 3;
    
    /** 
     * Create new CheckXMLSupport for given data object using DOCUMENT_MODE strategy.
     * @param inputSource Supported data object.
     */    
    public CheckXMLSupport(InputSource inputSource) {
        super(inputSource);
    }
    
    /** 
     * Create new CheckXMLSupport for given data object.
     * @param inputSource Supported data object.
     * @param strategy One of <code>*_MODE</code> constants.
     */    
    public CheckXMLSupport(InputSource inputSource, int strategy) {
        super(inputSource, strategy);
    }

    // inherit JavaDoc
    public boolean checkXML(CookieObserver l) {
        return super.checkXML(l);
    }
}


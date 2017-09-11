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

package org.netbeans.spi.project.ui;

/**
 * List of templates which should be in the initial "privileged" list
 * when making a new file.
 * An instance should be placed in {@link org.netbeans.api.project.Project#getLookup}
 * to affect the privileged list for that project.
 * 
 * <p>
 * Since 1.28, the PrivilegedTemplates instance can also reside in active node's lookup
 * and such instance will be used instead of the default one.
 * 
 * <p>
 * For more information about registering templates see overview of
 * {@link org.netbeans.spi.project.ui.templates.support} package.
 * @see org.netbeans.spi.project.ui.support.CommonProjectActions
 * @author Petr Hrebejk
 */
public interface PrivilegedTemplates {
    
    /**
     * Lists privileged templates.
     * @return full paths to privileged templates, e.g. <samp>Templates/Other/XmlFile.xml</samp>
     * @see <code>org.netbeans.api.templates.TemplateRegistration#folder</code>
     * @see <code>org.netbeans.api.templates.TemplateRegistration#content</code>
     * @see <code>org.netbeans.api.templates.TemplateRegistration#id</code>
     */
    public String[] getPrivilegedTemplates();
    
}

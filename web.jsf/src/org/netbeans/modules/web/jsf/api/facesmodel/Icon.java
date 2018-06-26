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
 * The icon type contains small-icon and large-icon elements
 * that specify the file names for small and large GIF, JPEG,
 * or PNG icon images used to represent the parent element in a
 * GUI tool.
 *
 * The xml:lang attribute defines the language that the
 * icon file names are provided in. Its value is "en" (English)
 * by default
 *
 * @author Petr Pisl
 */

public interface Icon extends LangAttribute {
    
    public static final String SMALL_ICON = JSFConfigQNames.SMALL_ICON.getLocalName();
    public static final String LARGE_ICON = JSFConfigQNames.LARGE_ICON.getLocalName();
    /**
     * The small-icon element contains the name of a file
     * containing a small (16 x 16) icon image. The file
     * name is a relative path within the Deployment
     * Component's Deployment File.
     *
     * The image may be in the GIF, JPEG, or PNG format.
     * The icon can be used by tools.
     * @return the path to the small icon
     */
    public String getSmallIcon();
    
    /**
     * The small-icon element contains the name of a file
     * containing a small (16 x 16) icon image. The file
     * name is a relative path within the Deployment
     * Component's Deployment File.
     *
     * The image may be in the GIF, JPEG, or PNG format.
     * The icon can be used by tools.
     * @param smallIcon the file name
     */
    public void setSmallIcon(String smallIcon);
    
    /**
     * The large-icon element contains the name of a file
     * containing a large
     * (32 x 32) icon image. The file name is a relative
     * path within the Deployment Component's Deployment
     * File.
     *
     * The image may be in the GIF, JPEG, or PNG format.
     * The icon can be used by tools.
     * @return the path to the large icon
     */
    public String getLargeIcon();
    
    /**
     * The large-icon element contains the name of a file
     * containing a large
     * (32 x 32) icon image. The file name is a relative
     * path within the Deployment Component's Deployment
     * File.
     *
     * The image may be in the GIF, JPEG, or PNG format.
     * The icon can be used by tools.
     * @param largeIcon the path to the large icon
     */
    public void setLargeIcon(String largeIcon);
    
}

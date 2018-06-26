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
package org.netbeans.modules.j2ee.sun.dd.api;

import java.io.IOException;
import org.openide.filesystems.FileObject;

/**
 * Interface representing the root of interfaces bean tree structure.
 *
 *
 */
public interface RootInterface extends CommonDDBean {    
    
    public static final String PROPERTY_STATUS = "dd_status";
    public static final String PROPERTY_VERSION = "dd_version";
    public static final int STATE_INVALID_PARSABLE = 1;
    public static final int STATE_INVALID_UNPARSABLE = 2;
    public static final int STATE_VALID = 0;
    
 
    /** 
     * Changes current DOCTYPE to match version specified.
     * Warning: Only the upgrade from lower to higher version is supported.
     * 
     * @param version 
     */
    public void setVersion(java.math.BigDecimal version);
    
    /** 
     * Version property as defined by the DOCTYPE, if known.
     * 
     * @return current version
     */
    public java.math.BigDecimal getVersion();
    
    /** 
     * Current parsing status
     * 
     * @return status value
     */
    public int getStatus();

    /**
     * Confirms that the DD passed in is the proxied DD owned by this interface
     */
    public boolean isEventSource(RootInterface rootDD);
    
    /** 
     * Writes the deployment descriptor data from deployment descriptor bean graph to file object.<br>
     * This is more convenient method than {@link org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean#write} method.<br>
     * The locking problems are solved for the user in this method.
     *
     * @param fo FileObject for where to write the content of deployment descriptor 
     *   holding in bean tree structure
     * @throws java.io.IOException 
     */
    public void write(FileObject fo) throws IOException;
    
}

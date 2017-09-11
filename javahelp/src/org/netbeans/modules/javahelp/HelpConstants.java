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

package org.netbeans.modules.javahelp;




/** Useful constants pertaining to the help system.
* @author Jesse Glick
*/
public interface HelpConstants {

    /** public ID for standard helpset DTD
     */
    String PUBLIC_ID_HELPSET = "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN"; // NOI18N
    /** public ID for NetBeans reference to a helpset
     */
    String PUBLIC_ID_HELPSETREF = "-//NetBeans//DTD JavaHelp Help Set Reference 1.0//EN"; // NOI18N
    /** public URL for NetBeans reference to a helpset
     */
    String PUBLIC_URL_HELPSETREF = "http://www.netbeans.org/dtds/helpsetref-1_0.dtd"; // NOI18N
    /** public ID for a help context link
     */    
    String PUBLIC_ID_HELPCTX = "-//NetBeans//DTD Help Context 1.0//EN"; // NOI18N
    /** public URL for a help context link DTD
     */    
    String PUBLIC_URL_HELPCTX = "http://www.netbeans.org/dtds/helpcontext-1_0.dtd"; // NOI18N
    /** "context" for merge attribute on helpsets
     */    
    String HELPSET_MERGE_CONTEXT = "OpenIDE"; // NOI18N
    /** attribute (type Boolean) on helpsets indicating
     * whether they should be merged into the master or
     * not; by default, true
     */    
    String HELPSET_MERGE_ATTR = "mergeIntoMaster"; // NOI18N
    /** A helpID present only in the master help set;
     *however, when displayed by {@link #showHelp} as the helpID in a context,
     *the master help set (with merged-in children) will be shown instead,
     *with no change made to the content pane.
     *Also, this is the help ID mapped to the "default" page in the master help viewer.
     */
    String MASTER_ID = "org.netbeans.api.javahelp.MASTER_ID"; // NOI18N

}

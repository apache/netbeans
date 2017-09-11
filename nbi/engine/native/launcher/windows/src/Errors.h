/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

#ifndef _Errors_H
#define	_Errors_H

#ifdef	__cplusplus
extern "C" {
#endif

    
#define ERROR_OK                            0
#define ERROR_INTEGRITY                     1000
#define ERROR_FREESPACE                     1001
#define ERROR_INPUTOUPUT                    1002
#define ERROR_JVM_UNCOMPATIBLE              1003
#define ERROR_JVM_NOT_FOUND                 1004
#define ERROR_ON_EXECUTE_PROCESS            1005
#define ERROR_PROCESS_TIMEOUT               1006
#define ERROR_USER_TERMINATED               1007
#define EXTERNAL_RESOURCE_MISSING           1008
#define ERROR_BUNDLED_JVM_EXTRACTION        1009
#define ERROR_BUNDLED_JVM_VERIFICATION      1010
    
#define EXIT_CODE_EVENTS_INITIALIZATION_ERROR 1022
#define EXIT_CODE_GUI_INITIALIZATION_ERROR  1023
#define EXIT_CODE_STUB                      1024
#define EXIT_CODE_SYSTEM_ERROR              1025


#ifdef	__cplusplus
}
#endif

#endif	/* _Errors_H */


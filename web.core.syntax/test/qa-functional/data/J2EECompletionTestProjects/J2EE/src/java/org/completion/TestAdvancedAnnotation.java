/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.completion;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.persistence.FieldResult;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Temporal;
public class TestAdvancedAnnotation{
    public @interface RequestForEnhancement {
        int    id();
        String synopsis();
        String engineer() default "[unassigned]";
        String date() default "[unimplemented]";
    }
    
/** Instant substitution in case of one annotation name. */
/**CC
@|
Basic
@Basic
*/

/** Instant substitution in case of one annotation attribute. */
/**CC
@FieldResult(|
String name
@FieldResult(name=
*/
    
/** Instant substitution in case of one annotation value. */
/**
@Enumerated(v|
EnumType value = javax.persistence.EnumType.ORDINAL
@Enumerated(value=
 */

/** Completion of annotation value in case of just one attribute. */
/**CC
@Temporal(|
TemporalType value
@Temporal(value=
*/

/** Completion of Java class for resultClass attribute. */
/**CC
@NamedNativeQuery(resultClass=java.util.C|
Calendar
@NamedNativeQuery(resultClass=java.util.Calendar
*/

/** Completion of Boolean value. */
/**CC
@Column(unique=|
false
@Column(unique=false
*/

/** Completion user defined annotation. */
/**CC
@|
RequestForEnhancement
@RequestForEnhancement
*/
    
/**CC
@RequestForEnhancement(|)
int id
@RequestForEnhancement(id=)
*/
    
}

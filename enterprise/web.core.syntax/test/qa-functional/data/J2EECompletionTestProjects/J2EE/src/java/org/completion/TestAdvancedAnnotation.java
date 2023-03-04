/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

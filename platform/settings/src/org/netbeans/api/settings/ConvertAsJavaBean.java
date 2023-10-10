/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.api.settings;

import java.beans.PropertyChangeListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Specifies the kind of persistence to use of the annotated class.
 * Uses {@link XMLDecoder} and {@link XMLEncoder} to store and read
 * values of the class (and by default also its subclasses).
 * <p>
 * The format uses getters and setters of the bean and usually needs
 * default constructor:
 * <pre>
 * <code>@</code>ConvertAsJavaBean
 * <font class="type">public class</font> YourObject {
 *   <font class="type">public</font> YourObject() {}
 *   <font class="type">public</font> <font class="type">String</font> <font class="function-name">getName</font>();
 *   <font class="type">public void</font> <font class="function-name">setName</font>(<font class="type">String</font> <font class="variable-name">name</font>);
 * }
 * </pre>
 * If the bean supports {@link PropertyChangeListener} notifications and
 * contains <code>addPropertyChangeListener</code> method, the system
 * starts to listen on existing objects and in case a property change
 * is delivered, the new state of the object is persisted again.
 *
 * @author Jaroslav Tulach &lt;jtulach@netbeans.org&gt;
 * @since 1.20
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface ConvertAsJavaBean {
    /** Shall subclasses of this class be also converted as JavaBeans? */
    boolean subclasses() default true;
}

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

package org.openide.awt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.openide.filesystems.FileUtil;

/** Identifies one registered action. The action ought to be placed into 
 * a category folder and use additional id identification. 
 * In terms of {@link FileUtil#getConfigFile(java.lang.String)
 * layer based definition},
 * the action ought to be placed in 
 * <code>"Actions/" + category() + "/" + id().replace('.','-') + ".instance"</code>
 * path.
 *
 * @author Jaroslav Tulach &lt;jtulach@netbeans.org&gt;
 * @since 7.26
 * @see ActionRegistration
 * @see Actions#forID
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface ActionID {
    /** Identifies category of an action.
     * @return string representing programmatic name of the category
     */
    String category();
    /** The unique ID (inside a category) of the action. Should follow
     * Java naming conventions and somehow include package name prefix. Like
     * <code>org.myproject.myproduct.MyAction</code>.
     * 
     * @return java identifiers separated with '.'
     */
    String id();
}

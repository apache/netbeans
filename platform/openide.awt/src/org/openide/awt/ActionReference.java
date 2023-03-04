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

package org.openide.awt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/** Registers an action under associated identifier specified by separate
 * {@link ActionID} annotation on the same element. Usually it is used together
 * with {@link ActionRegistration}. You can place your action reference into
 * any path, here are few typical locations:
 * <ul>
 * <li>menu - <code>path="Menu/File"</code>, <code>path="Menu/Edit"</code>, etc.</li>
 * <li>toolbar - <code>path="Toolbars/Edit"</code> and other peer directories</li>
 * <li>shortcuts - <code>path="Shortcuts" name="C-F2 D-A"</code>, see {@link Utilities#stringToKeys(java.lang.String)}
 *     and {@link Utilities#stringToKey(java.lang.String)} for description of valid reference names</li>
 * <li>context menus - <code>path="Loaders/text/xml"</code>, and other mime types</li>
 * </ul>
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @since 7.27
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface ActionReference {
    /** Into which location one wants to place the reference?
     * Translates to {@link FileUtil#getConfigFile(java.lang.String)}.
     */
    String path();
    
    /** Position in the location.
     */
    int position() default Integer.MAX_VALUE;
    
    /** Identification of the action this reference shall point to.
     * Usually this is specified as {@link ActionID} peer annotation, but
     * in case one was to create references to actions defined by someone else,
     * one can specify the id() here.
     */
    ActionID id() default @ActionID(id="",category="");
    
    /** One can specify name of the reference. This is not necessary,
     * then it is deduced from associated {@link ActionID}.
     */
    String name() default "";
    
    /** Shall a separator be placed before the action?
     * @return position that is lower than {@link #position()}
     */
    int separatorBefore() default Integer.MAX_VALUE;
    
    /** Shall a separator be placed after the action?
     * @return position that is higher than {@link #position()}
     */
    int separatorAfter() default Integer.MAX_VALUE;
}

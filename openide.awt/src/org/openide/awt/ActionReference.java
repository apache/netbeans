/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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

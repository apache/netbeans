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

import java.awt.event.ActionListener;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.swing.Action;
import javax.swing.ActionMap;
import org.openide.util.ContextAwareAction;

/** Registers an action under associated identifier specified by separate
 * {@link ActionID} annotation on the same element. Here is few usage examples:
 * <ul>
 *   <li>{@linkplain Actions#alwaysEnabled(java.awt.event.ActionListener, java.lang.String, java.lang.String, boolean) always enabled action}</li>
 *   <li>{@linkplain Actions#callback(java.lang.String, javax.swing.Action, boolean, java.lang.String, java.lang.String, boolean) callback action}</li>
 *   <li>{@linkplain Actions#context(java.lang.Class, boolean, boolean, org.openide.util.ContextAwareAction, java.lang.String, java.lang.String, java.lang.String, boolean)  context aware action} </li>
 * </ul>
 * 
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @since 7.26
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface ActionRegistration {
    /** Display name. Usually prefixed with '#' to reference value from a 
     * <code>Bundle.properties</code> file in the same package.
     * @return display name for the action
     */
    String displayName();
    
    /** 
     * Provides the JMenuItem text if one wants to use other than 
     * the name of the action returned by {@link #displayName()}.
     * 
     * @return display name for the action
     * 
     * @see Actions#connect(javax.swing.JMenuItem, javax.swing.Action, boolean) 
     * @since 7.35
     */    
    String menuText() default "";
    
    /** 
     * Provides the JMenuItem popup text if one wants to use other   
     * than the name of the action returned by {@link #displayName()}.
     * 
     * @return display name for the action in a popup menu
     * 
     * @see Actions#connect(javax.swing.JMenuItem, javax.swing.Action, boolean) 
     * @since 7.35
     */
    String popupText() default "";
    
    /** Path to image representing the action's icon.
     * @return "org/myproject/mypkg/Icon.png"
     */
    String iconBase() default "";
    /** Shall the action's icon be visible in menu?
     * @return true or false
     */
    boolean iconInMenu() default true;
    /** Shall this action be associated with a particular key in an
     * {@link ActionMap}? E.g. behave like {@link Actions#callback(java.lang.String, javax.swing.Action, boolean, java.lang.String, java.lang.String, boolean)} one?
     * @return the value of the key to seek in currently selected {@link ActionMap}
     */
    String key() default "";
    /** Shall the action be performed outside of AWT thread.
     * @return false, if the action shall run synchronously
     */
    boolean asynchronous() default false;
    /** Shall the action work on last selection when it was enabled?
     */
    boolean surviveFocusChange() default false;

    /**
     * Whether a lazy factory registration from {@link Actions} should be used.
     * <p>Most actions can be registered using a lazy factory (see class Javadoc for list),
     * which permits the application to avoid loading the action class unless and until it
     * is actually run. Before then, queries on the name, icon, etc. are serviced using
     * static metadata, which minimizes startup overhead.
     * <p>In limited cases, using this sort of lazy delegate is impossible or undesirable:
     * the action needs to export some special key from {@link Action#getValue};
     * it presents multiple menu items according to dynamic conditions; or for UI reasons
     * it is preferred to visually disable the action under certain conditions (rather than
     * leaving it enabled and showing a message if selected under those conditions).
     * <p>For these special cases,
     * you may specify {@code lazy=false} to force the action registration to use the
     * traditional direct registration of an instance of the action, without any lazy factory.
     * That allows the action to supply arbitrary customized behavior even before it is ever run,
     * at the expense of overhead when the registration is first encountered (such as when the
     * main menu bar is populated, or a context menu of a certain kind is first shown).
     * <p>It is an error to specify {@code lazy=false} on an element which is not assignable
     * to {@link Action} (e.g. an {@link ActionListener}, or a {@code String} field);
     * or which requires a context parameter in its constructor (or factory method).
     * <p>For compatibility, registrations which do not <em>explicitly</em> specify this
     * attribute but which are on elements assignable to any of the following types
     * will be registered as if {@code lazy=false}, despite the default value of the attribute
     * (but a warning will be issued and the attribute should be made explicit):
     * <ul>
     * <li>{@link org.openide.util.actions.Presenter.Menu}
     * <li>{@link org.openide.util.actions.Presenter.Toolbar}
     * <li>{@link org.openide.util.actions.Presenter.Popup}
     * <li>{@link ContextAwareAction}
     * <li>{@link DynamicMenuContent}
     * </ul>
     * @since 7.41
     */
    boolean lazy() default true;

}

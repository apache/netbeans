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
package org.openide.explorer.propertysheet;

import java.beans.PropertyChangeListener;

import java.lang.reflect.InvocationTargetException;


/**
 * A model defining the behavior of a property.  This model is used to allow
 * components such as PropertyPanel to be used with arbitrary JavaBeans, without
 * requiring them to be instances of Node.Property.
 * <p>
 * <b>Note:</b>While not yet deprecated, this class will soon be deprecated.  The
 * only functionality it offers that is distinct from Node.Property and/or
 * PropertySupport.Reflection is the ability to listen to the model for
 * changes, rather than listening to the bean it is a property of.
 * <p>
 * Users of PropertyPanel are encouraged instead to use
 * its Node.Property constructor unless you are absolutely sure that
 * the property you want to display can be changed by circumstances
 * beyond your control <strong>while it is on screen</strong> (this is
 * usually a bug not a feature).
 *
 * @see DefaultPropertyModel
 * @author Jaroslav Tulach, Petr Hamernik
 */
public interface PropertyModel {
    /** Name of the 'value' property. */
    public static final String PROP_VALUE = "value"; // NOI18N

    /**
     * Getter for current value of a property.
     * @return the value
     * @throws InvocationTargetException if, for example, the getter method
     * cannot be accessed
     */
    public Object getValue() throws InvocationTargetException;

    /** Setter for a value of a property.
    * @param v the value
    * @exception InvocationTargetException if, for example, the setter cannot
    * be accessed
    */
    public void setValue(Object v) throws InvocationTargetException;

    /**
     * The class of the property.
     * @return A class object
     */
    public Class getPropertyType();

    /**
     * The class of the property editor or <CODE>null</CODE>
     * if default property editor should be used.
     * @return the class of PropertyEditor that should be used to edit this
     * PropertyModel
     */
    public Class getPropertyEditorClass();

    /** Add listener to change of the value.
    */
    public void addPropertyChangeListener(PropertyChangeListener l);

    /** Remove listener to change of the value.
    */
    public void removePropertyChangeListener(PropertyChangeListener l);
}

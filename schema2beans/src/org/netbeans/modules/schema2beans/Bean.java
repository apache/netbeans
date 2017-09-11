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

package org.netbeans.modules.schema2beans;

import java.beans.*;

/**
 * @author cliffwd
 *
 * All generated beans that use the runtime will implement this interface.
 * It allows for some navigation and reflection.
 */
public interface Bean {
    public void addPropertyChangeListener(PropertyChangeListener l);
    public void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * @return a representation of the property.  This method does not return
     * null.  If there is no object available for the specified
     * property name, an exception is thrown.
     */
    public BeanProp beanProp(String name);

    /**
     * @return the schema name of this bean as define by this bean's parent.
     */
    public String dtdName();
    public boolean isRoot();
    public Bean _getParent();
    public Bean _getRoot();

    /**
     *	Return the bean name of this graph node.
     */
    public String name();
    public boolean hasName(String name);
    public int indexToId(String name, int index);
    public int idToIndex(String name, int id);
    public Bean propertyById(String name, int id);
    public Object getValueById(String name, int id);
    public void setValue(String name, Object value);
    public void setValue(String name, int index, Object value);
    public void setValueById(String name, int id, Object value);
    public int removeValue(String name, Object value);
    public int addValue(String name, Object value);
    public Object getValue(String name);
    public Object getValue(String name, int index);
    public Object[] getValues(String name);
    public BaseProperty getProperty();
    public BaseProperty getProperty(String propName);
    public BaseProperty[] listProperties();

    /**
     * Find all child beans and put them into the give beans List.
     */
    public void childBeans(boolean recursive, java.util.List beans);
}

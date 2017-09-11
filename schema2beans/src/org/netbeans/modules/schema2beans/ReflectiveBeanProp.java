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

import java.lang.reflect.*;

/**
 * A BeanProp that works using reflection.
 */
public class ReflectiveBeanProp extends BeanProp {
    private Method writer;
    private Method arrayWriter;
    private Method reader;
    private Method arrayReader;
    private Method adder;
    private Method remover;

    public ReflectiveBeanProp(BaseBean bean, String dtdName, String beanName,
                              int type, Class propClass,
                              Method writer, Method arrayWriter, Method reader, Method arrayReader, Method adder, Method remover) {
        super(bean, dtdName, beanName, type, propClass, -1);
        bindings = null;
        attributes = null;
        this.writer = writer;
        this.arrayWriter = arrayWriter;
        this.reader = reader;
        this.arrayReader = arrayReader;
        this.adder = adder;
        this.remover = remover;
    }

    public ReflectiveBeanProp(BaseBean bean, String dtdName, String beanName,
                              int type, Class propClass, boolean isRoot,
                              Method writer, Method arrayWriter, Method reader, Method arrayReader, Method adder, Method remover) {
        super(bean, dtdName, beanName, type, propClass, isRoot, -1);
        bindings = null;
        attributes = null;
        this.writer = writer;
        this.arrayWriter = arrayWriter;
        this.reader = reader;
        this.arrayReader = arrayReader;
        this.adder = adder;
        this.remover = remover;
    }

    DOMBinding getBinding(int index) {
        throw new IllegalStateException();
    }
    
    protected int bindingsSize() {
        Object[] arr = getObjectArray(0);
        if (arr == null)
            return 0;
        return arr.length;
    }

    public Object getValue(int index) {
        try {
            if (isIndexed())
                return reader.invoke(bean, new Object[] {new Integer(index)});
            else
                return reader.invoke(bean, new Object[] {});
        } catch (java.lang.IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getValueById(int id) {
        return getObjectArray(0)[idToIndex(id)];
    }

    public int indexToId(int index) {
        return index;
    }

    public int idToIndex(int id) {
        return id;
    }

    protected Object[] getObjectArray(int extraElements) {
        if (arrayReader == null)
            return new Object[] {};
        try {
            return (Object[]) arrayReader.invoke(bean, new Object[] {});
        } catch (java.lang.IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setValue(Object[] value) {
        try {
            arrayWriter.invoke(bean, new Object[] {value});
        } catch (java.lang.IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public int removeValue(Object value) {
        try {
            return ((Integer) remover.invoke(bean, new Object[] {value})).intValue();
        } catch (java.lang.IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeValue(int index) {
        Object[] arr = getObjectArray(0);
        if (arr == null)
            return;
        Object[] newArr = (Object[]) Array.newInstance(arr.getClass().getComponentType(),
                                            arr.length-1);
        System.arraycopy(arr, 0, newArr, 0, index);
        System.arraycopy(arr, index+1, newArr, index, arr.length - index - 1);
        setValue(newArr);
    }

    protected int setElement(int index, Object value, boolean add) {
        try {
            if (add)
                index = ((Integer) adder.invoke(bean, new Object[] {value})).intValue();
            else if (isIndexed())
                writer.invoke(bean, new Object[] {new Integer(index), value});
            else
                writer.invoke(bean, new Object[] {value});
            return index;
        } catch (java.lang.IllegalAccessException e) {
            throw new RuntimeException("setElement: "+getBeanName(), e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException("setElement: "+getBeanName(), e);
        }
    }

    public void setAttributeValue(int index, String name, String value) {
        throw new IllegalStateException();
    }

    public String getAttributeValue(int index, String name) {
        throw new IllegalStateException();
    }

    public DOMBinding registerDomNode(org.w3c.dom.Node node, DOMBinding binding,
				      BaseBean bean) throws Schema2BeansException {
        throw new IllegalStateException();
    }
    
    public String getFullName() {
        return getFullName(0);
    }

    public String getFullName(int index) {
        if (reader == null)
            return bean.nameSelf();
        else
            return bean.nameSelf()+"/"+bean.nameChild(getValue(index), false, false);
    }

    void buildPathName(DOMBinding binding, StringBuffer str) {
        throw new IllegalStateException();
    }
    
    String buildFullName(int index, String attr) {
        throw new IllegalStateException();
    }
}

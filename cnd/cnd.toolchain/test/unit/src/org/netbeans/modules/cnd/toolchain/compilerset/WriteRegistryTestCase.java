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

package org.netbeans.modules.cnd.toolchain.compilerset;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ToolchainDescriptor;
import org.openide.util.NbPreferences;


/**
 *
 */
public class WriteRegistryTestCase extends NbTestCase {
    private static final boolean TRACE = false;
    
    public WriteRegistryTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.toolchain.personality.create_shadow","true");
        NbPreferences.forModule(ToolchainManagerImpl.class).putBoolean(ToolchainManagerImpl.SHADOW_KEY, false);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testWrtiteDescriptor() throws Exception {
        List<ToolchainDescriptor> original = ToolchainManagerImpl.getImpl().getAllToolchains();
        ToolchainManagerImpl.getImpl().reinitToolchainManager();
        List<ToolchainDescriptor> restored = ToolchainManagerImpl.getImpl().getAllToolchains();
        for(int i = 0; i < original.size(); i++) {
            if (TRACE) {
                System.out.println("Tool collection "+original.get(i));
            }
           assertTrue("Tool chain "+original.get(i)+" not equals "+restored.get(i), deepObjectComparing(0, original.get(i),restored.get(i),null));
        }
    }

    private String wrap(int level) {
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < level; i++) {
            buf.append('\t');
        }
        return buf.toString();
    }
    
    private boolean deepObjectComparing(int level, Object original, Object restored, Field container){
        if (TRACE) {
            System.out.println(wrap(level)+original);
        }
        if (!original.getClass().equals(restored.getClass())){
            System.out.println("Class "+original.getClass()+" not equals "+restored.getClass());
            return false;
        }
        if (original instanceof String) {
            if (!original.equals(restored)){
                System.out.println("String fields "+container.getName()+" in class "+container.getDeclaringClass()+" not equal: "+original+" != "+restored);
                return false;
            }
            return true;
        } else if (original instanceof Boolean) {
            if (!original.equals(restored)){
                System.out.println("Boolean fields "+container.getName()+" in class "+container.getDeclaringClass()+" not equal: "+original+" != "+restored);
                return false;
            }
            return true;
        } else if (original instanceof Integer) {
            if (!original.equals(restored)){
                System.out.println("Integer fields "+container.getName()+" in class "+container.getDeclaringClass()+" not equal: "+original+" != "+restored);
                return false;
            }
            return true;
        } else if (original instanceof String[]) {
            if (!Arrays.equals((String[])original, (String[])restored)){
                System.out.println("String[] fields "+container.getName()+" in class "+container.getDeclaringClass()+" not equal:\n\t"+
                        Arrays.toString((String[])original)+"\n\t"+Arrays.toString((String[])restored));
                return false;
            }
            return true;
        }
        List<Field> list = new ArrayList<Field>();
        list.addAll(Arrays.asList(original.getClass().getDeclaredFields()));
        Class<? extends Object> superclass = original.getClass().getSuperclass();
        if (superclass != null && !"java.lang.Object".equals(superclass.getName())) {
            list.addAll(Arrays.asList(superclass.getDeclaredFields()));
        }
        for (Field field : list){
            try {
                if ((field.getModifiers() & (Modifier.PROTECTED | Modifier.PRIVATE | Modifier.STATIC)) != 0) {
                    continue;
                }
                Object o1 = field.get(original);
                Object o2 = field.get(restored);
                if (o1 instanceof ToolchainManagerImpl.Compiler){
                     if (!((ToolchainManagerImpl.Compiler)o1).isValid() && !((ToolchainManagerImpl.Compiler)o1).isValid()){
                         continue;
                     }
                }
                if (o1 == null && o2 == null){
                    // both objects have null references
                    continue;
                } else if (o1 != null && o2 == null) {
                    System.out.println("Fields "+field.getName()+" in class "+original.getClass()+" not equal: o1 != null && o2 == null o1 = " + o1);
                    return false;
                } else if (o1 == null && o2 != null) {
                    System.out.println("Fields "+field.getName()+" in class "+original.getClass()+" not equal: o1 == null && o2 != null o2 = " + o2);
                    return false;
                }
                if (TRACE) {
                    System.out.println(wrap(level)+o1);
                }
                if (o1 instanceof String) {
                    if (!o1.equals(o2)){
                        System.out.println("String fields "+field.getName()+" in class "+original.getClass()+" not equal: "+o1+" != "+o2);
                        return false;
                    }
                } else if (o1 instanceof Boolean) {
                    if (!o1.equals(o2)){
                        System.out.println("Boolean fields "+field.getName()+" in class "+original.getClass()+" not equal: "+o1+" != "+o2);
                        return false;
                    }
                } else if (o1 instanceof Integer) {
                    if (!o1.equals(o2)){
                        System.out.println("Integer fields "+field.getName()+" in class "+original.getClass()+" not equal: "+o1+" != "+o2);
                        return false;
                    }
                } else if (o1 instanceof String[]) {
                    if (!Arrays.equals((String[])o1, (String[])o2)){
                        System.out.println("String[] fields "+field.getName()+" in class "+original.getClass()+" not equal:\n\t"+
                                Arrays.toString((String[])o1)+"\n\t"+Arrays.toString((String[])o2));
                        return false;
                    }
                } else if (o1 instanceof Map) {
                    Map m1 = (Map)o1;
                    Map m2 = (Map)o2;
                    if (!m1.equals(m2)){
                        System.out.println("Map fields "+field.getName()+" in class "+original.getClass()+" not equal:\n\t"+
                                m1+"\n\t"+m2);
                        return false;
                    }
                } else if (o1 instanceof Collection) {
                    Iterator i1 = ((Collection)o1).iterator();
                    Iterator i2 = ((Collection)o2).iterator();
                    while(i1.hasNext() && i2.hasNext()) {
                        if (!deepObjectComparing(level + 1, i1.next(), i2.next(), field)){
                            return false;
                        }
                    }
                    if (i1.hasNext() || i2.hasNext()){
                        System.out.println("Collection fields "+field.getName()+" in class "+original.getClass()+" not equal:\n\t"+
                                o1+"\n\t"+o2);
                        return false;
                    }
                } else {
                    if (!deepObjectComparing(level + 1, o1, o2, field)){
                        return false;
                    }
                }
            } catch (IllegalAccessException ex) {
                ex.printStackTrace(System.err);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace(System.err);
                return false;
            }
        }
        return true;
    }

}

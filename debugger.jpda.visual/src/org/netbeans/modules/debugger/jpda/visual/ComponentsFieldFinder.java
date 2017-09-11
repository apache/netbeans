/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;

/**
 * Find fields in which individual components are declared.
 * 
 * @author Martin Entlicher
 */
public class ComponentsFieldFinder {
    
    static void findFieldsForComponents(JavaComponentInfo ci) {
        // Components by UID:
        int n = countComponents(ci);
        long[] uids = new long[n];
        JavaComponentInfo[] components = new JavaComponentInfo[n];
        fillComponents(ci, components, 0);
        Arrays.sort(components, new JavaComponentInfoUIDComparator());
        for (int i = 0; i < n; i++) {
            uids[i] = components[i].uid;
        }
        for (JavaComponentInfo jci : components) {
            if (jci.isCustomType() && jci.getSubComponents().length > 0) {
                ObjectReference c = jci.getComponent();
                try {
                    Map<Field, Value> fieldValues = ObjectReferenceWrapper.getValues(c, ReferenceTypeWrapper.fields(ObjectReferenceWrapper.referenceType(c)));
                    for (Map.Entry<Field, Value> fv : fieldValues.entrySet()) {
                        Value value = fv.getValue();
                        if (value instanceof ObjectReference) {
                            long uid = ObjectReferenceWrapper.uniqueID((ObjectReference) value);
                            int index = Arrays.binarySearch(uids, uid);
                            if (index >= 0) {
                                components[index].setFieldInfo(new JavaComponentInfo.FieldInfo(fv.getKey(), jci));
                            }
                        }
                    }
                } catch (ClassNotPreparedExceptionWrapper cnpex) {
                } catch (InternalExceptionWrapper iex) {
                } catch (ObjectCollectedExceptionWrapper ocex) {
                } catch (VMDisconnectedExceptionWrapper vmdex) {
                    return ;
                }
            }
        }
    }
    
    private static int countComponents(JavaComponentInfo ci) {
        int n = 1;
        for (JavaComponentInfo jci : ci.getSubComponents()) {
            n += countComponents(jci);
        }
        return n;
    }

    private static int fillComponents(JavaComponentInfo ci, JavaComponentInfo[] components, int pos) {
        components[pos] = ci;
        pos++;
        for (JavaComponentInfo jci : ci.getSubComponents()) {
            pos = fillComponents(jci, components, pos);
        }
        return pos;
    }

    private static class JavaComponentInfoUIDComparator implements Comparator<JavaComponentInfo> {

        public JavaComponentInfoUIDComparator() {
        }

        @Override
        public int compare(JavaComponentInfo o1, JavaComponentInfo o2) {
            if (o1.uid == o2.uid) {
                return 0;
            } else if (o1.uid < o2.uid) {
                return -1;
            } else {
                return +1;
            }
        }
    }
}

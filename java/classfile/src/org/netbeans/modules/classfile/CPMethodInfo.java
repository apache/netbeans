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
/*
 * Contributor(s): Thomas Ball
 */

package org.netbeans.modules.classfile;


/**
 * A class representing the CONSTANT_Methodref constant pool type.
 *
 * @author Thomas Ball
 */
public class CPMethodInfo extends CPFieldMethodInfo {
    CPMethodInfo(ConstantPool pool,int iClass,int iNameAndType) {
        super(pool, iClass, iNameAndType);
    }

    public final String getMethodName() {
	return getFieldName();
    }

    /**
     * Get method name and signature, such as "void setBar(Bar)".
     * @return method name and signature
     */
    public final String getFullMethodName() {
        return getFullMethodName(getMethodName(), getDescriptor());
    }
    
    static String getFullMethodName(String name, String signature) {
        StringBuffer sb = new StringBuffer();
        int index = signature.indexOf(')');
        String params = signature.substring(1, index);
        
        if (!"<init>".equals(name) && !"<clinit>".equals(name)) {
            String ret = signature.substring(index + 1);
            ret = CPFieldMethodInfo.getSignature(ret, false);
            if (ret.length() > 0) {
                sb.append(ret);
                sb.append(' ');
            }
        }
        sb.append(name);
        sb.append('(');
        index = 0;
        int paramsLength = params.length();
        while (index < paramsLength) {
            StringBuffer p = new StringBuffer();
            char ch = params.charAt(index++);
            while (ch == '[') {
                p.append(ch);
                ch = params.charAt(index++);
            }
            p.append(ch);
            if (ch == 'L')
                do {
                    ch = params.charAt(index++);
                    p.append(ch);
                } while (ch != ';');
            sb.append(CPFieldMethodInfo.getSignature(p.toString(), false));
            if (index < paramsLength)
                sb.append(',');
        }
        sb.append(')');
        return sb.toString();
    }

    @Override
    public int getTag() {
	return ConstantPool.CONSTANT_MethodRef;
    }
}

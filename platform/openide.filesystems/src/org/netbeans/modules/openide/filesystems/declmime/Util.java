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

package org.netbeans.modules.openide.filesystems.declmime;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 * @author  Petr Kuzel
 * @version
 */
class Util {
    /** Forbid creating new Util */
    private Util() {
    }

    static String[] addString(String[] array, String val) {
        if (array == null) {
            return new String[] {val};
        } else {
            String[] n = new String[array.length + 1];
            System.arraycopy(array, 0, n, 0, array.length);
            n[array.length] = val;
            return n;
        }
    }

    static int indexOf(Object[] where, Object what) {                    
        if (where == null) return -1;
        for (int i = 0; i<where.length; i++) {
            if (where[i].equals(what)) return i;
        }        
        return -1;
    }

    static int indexOf(String[] where, String what, boolean caseInsensitiv) {                  
        boolean isEqual;        
        
        for (int i = 0; where != null && i < where.length; i++) {            
            if (caseInsensitiv)
                isEqual = where[i].equalsIgnoreCase (what);
            else  
                isEqual = where[i].equals(what);
            
            if (isEqual)  return i;
        }                
        return -1;
    }
        
    static boolean contains(Object[] where, Object what) {
        return indexOf(where, what) != -1;
    }
    
    static boolean contains(String[] where, String what, boolean caseInsensitiv) {                    
        return indexOf(where, what, caseInsensitiv) != -1;
    }    
    
    static void writeUTF(DataOutput os, String s) throws IOException {
        if (s == null) {
            s = "\u0000";
        }
        os.writeUTF(s);
    }
    
    static String readUTF(DataInput in) throws IOException {
        String s = in.readUTF();
        if ("\u0000".equals(s)) {
            return null;
        }
        return s;
    }

    static void writeStrings(DataOutput out, String[] arr) throws IOException {
        if (arr == null) {
            out.writeInt(-1);
            return;
        }
        out.writeInt(arr.length);
        for (String m : arr) {
            writeUTF(out, m);
        }
    }

    static String[] readStrings(DataInput in) throws IOException {
        final int len = in.readInt();
        if (len == -1) {
            return null;
        }
        final String[] arr = new String[len];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = readUTF(in);
        }
        return arr;
    }

    static void writeBytes(DataOutput out, byte[] arr) throws IOException {
        if (arr == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(arr.length);
            out.write(arr);
        }
    }
    
    static byte[] readBytes(DataInput is) throws IOException {
        int len = is.readInt();
        if (len == -1) {
            return null;
        } else {
            byte[] arr = new byte[len];
            is.readFully(arr);
            return arr;
        }
    }
}

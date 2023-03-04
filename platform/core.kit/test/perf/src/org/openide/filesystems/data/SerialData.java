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
package org.openide.filesystems.data;

import java.util.ArrayList;
import java.io.*;

/**
 * Serves for generating
*/
public class SerialData extends Object implements Serializable {

    private static String serialData;

    private int data1;
    private String data2;
    private Object data3;
    private ArrayList data4;

    /** Creates new SerialData */
    public SerialData() {
        data1 = 0;
        data2 = "data2";
        data3 = new Integer(5);
        data4 = new ArrayList();
        data4.add(data3);
    }

    public static String getSerialDataString() throws Exception {
        if (serialData == null) {
            serialData = createSerialDataString();
        }
        
        return serialData;
    }
    
    private static String createSerialDataString() throws Exception {
        ByteArrayOutputStream barros = new ByteArrayOutputStream();
        ObjectOutputStream obos = new ObjectOutputStream(barros);
        
        obos.writeObject(new SerialData());
        obos.close();
        
        return bytes2String(barros.toByteArray());
    }

    /**
    * @param args the command line arguments
    */
    /*
    public static void main(String args[]) throws Exception {
        ByteArrayOutputStream barros = new ByteArrayOutputStream();
        ObjectOutputStream obos = new ObjectOutputStream(barros);
        
        obos.writeObject(new SerialData());
        obos.close();
        
        byte[] bytes = barros.toByteArray();
        System.out.println(bytes2String(bytes));
    }*/
    
    private static String bytes2String (byte[] bytes) {
        StringBuffer buffer = new StringBuffer(2 * bytes.length);
        
        for (int i = 0; i < bytes.length; i++) {
            addByte(bytes[i], buffer);
        }
        
        return buffer.toString();
    }
    
    private static void addByte(int b, StringBuffer buffer) {
        if (b < 0) {
            b += 256;
        }
        
        int rest = b % 16;
        b = b / 16;
        buffer.append(toChar(b));
        buffer.append(toChar(rest));
    }
    
    private static char toChar(int b) {
        if (b > 9) {
            return (char) ('a' + b - 10);
        } else {
            return (char) ('0' + b);
        }
    }
}

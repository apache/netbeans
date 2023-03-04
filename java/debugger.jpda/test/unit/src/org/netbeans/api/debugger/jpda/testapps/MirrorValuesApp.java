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
package org.netbeans.api.debugger.jpda.testapps;

import java.awt.Color;
import java.awt.Point;
import java.beans.FeatureDescriptor;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import sun.reflect.ReflectionFactory;

/**
 * A test application for mirror values.
 * 
 * @author Martin Entlicher
 */
public class MirrorValuesApp {
    
    public static void main(String[] args) {
        MirrorValuesApp mva = new MirrorValuesApp();
        mva.mirrors();
    }

    private void mirrors() {
        boolean boo = true;
        byte b = 5;
        char c = 'c';
        short s = 512;
        int i = 10000;
        long l = Long.MAX_VALUE;
        float f = 12.12f;
        double d = 1e150;
        
        int[] iarr = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        double[][] darr = new double[][] { { 0.1, 0.2, 0.3 }, { 1.1, 1.2, 1.3 }, { 2.1, 2.2, 2.3 } };
        
        String str = "A String";
        Integer integer = new Integer(Integer.MIN_VALUE);
        
        Date date = new Date(1000000000l);
        date.toString(); // This is necessary to initialize the Date.cdate field and Gregorian$Date class.
        Color color = Color.RED;
        Point point = new Point(10, 10);
        File file = new File("/tmp/Foo.txt");
        URL url = createURL("http://netbeans.org");
        URL url2 = createPristineURL();
        
        Color[][] colors = new Color[][] { { Color.WHITE, Color.BLACK }, { Color.YELLOW, Color.GRAY } };
        
        List selfReferencedList = createSelfReferencedList();
        EventObject event = createObjectCircle();
        
        System.currentTimeMillis();             // LBREAKPOINT
        
        boolean newValues = (boo == false) && b == (byte) 255 && c == 'Z' &&
                             s == (short) -1024 && i == -1 && l == 123456789101112l &&
                             f == 2e-2f && d == -3e250 &&
                             Arrays.equals(iarr, new int[] { 9, 7, 5, 3, 1 }) &&
                             Arrays.deepEquals(darr, new double[][] { { 1e100, 2e200 }, { 1.1e100, 2.1e200 }, { 1.2e100, 2.2e200 }, { 1.3e100, 2.3e200 } }) &&
                             str.equals("An alternate sTRING") &&
                             integer.equals(-1048576) &&
                             date.equals(new Date(3333333333l)) &&
                             color.equals(Color.GREEN) &&
                             point.equals(new Point(-100, -100)) &&
                             file.equals(new File("/tmp/Test.java")) &&
                             url.equals(createURL("http://debugger.netbeans.org")) &&
                             url2 == null;
        
        System.currentTimeMillis();             // LBREAKPOINT
    }

    private static List createSelfReferencedList() {
        List list = new LinkedList();
        list.add(list);
        return list;
    }
    
    private static EventObject createObjectCircle() {
        FeatureDescriptor fd = new FeatureDescriptor();
        EventObject eo = new EventObject(fd);
        fd.setValue("event", eo);
        return eo;
    }
    
    private static URL createURL(String urlStr) {
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            url = null;
        }
        return url;
    }
    
    private static URL createPristineURL() {
        // Creates a pristine, incomplete URL object, that gets created when we
        // step into new URL(), for instance.
        ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
        try {
            Constructor constructor = rf.newConstructorForSerialization(URL.class, Object.class.getDeclaredConstructor());
            Object newInstance = constructor.newInstance();
            return (URL) newInstance;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}

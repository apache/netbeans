/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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

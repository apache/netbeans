/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.util.system;

import junit.framework.*;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.system.WindowsNativeUtils;
import org.netbeans.installer.utils.system.windows.*;

/**
 *
 * @author dlm198383
 */
public class WindowsRegistryTest extends TestCase {
    private static WindowsRegistry registry = null;
    private SystemUtils su = null;
    private String sep = "\\";
    private int HKLM = WindowsRegistry.HKEY_LOCAL_MACHINE;
    private int HKCU = WindowsRegistry.HKEY_CURRENT_USER;
    private int HKCC = WindowsRegistry.HKEY_CURRENT_CONFIG;
    private int HKCR = WindowsRegistry.HKEY_CLASSES_ROOT;
    private int HKU = WindowsRegistry.HKEY_USERS;
    private String sw = "Software";
    String subkey = "WindowsRegistryTest";
    String valueName = "Name";
    String stringValue = "Value";
    
    public WindowsRegistryTest(String testName) {
        super(testName);
    }
    /*
    protected void setUp() throws Exception {
        init();
    }
    
    protected void tearDown() throws Exception {
    }
    
    private void init() {
        if(su==null) {
            su = SystemUtils.getInstance();
            if(su instanceof WindowsNativeUtils) {
                registry = ((WindowsNativeUtils)su).getWindowsRegistry();
            }
            
        }
    }
    public void testInitialize() {
        init();
    }
    
    
    public void testNullKeysAndValues() {
        if(!registry.isKeyExists(HKCU, "")) {
            fail();
            return;
        }
        if(registry.isValueExists(HKCU, null, null)) {
            fail();
            return;
        }
        if(!registry.isKeyEmpty(HKCU, null)) {
            fail();
            return;
        }
        if(registry.getStringValue(HKCU, null,null)!=null) {
            fail();
            return;
        }
        if(registry.getBinaryValue(HKCU, null,null)!=null) {
            fail();
            return;
        }
        if(registry.getMultiStringValue(HKCU, null,null)!=null) {
            fail();
            return;
        }
        if(registry.get32BitValue(HKCU, null,null)!=-1) {
            fail();
            return;
        }
        if(registry.isKeyExists(HKCU,subkey)) {
            if(!registry.deleteKey(HKCU,subkey)) {
                fail();
            }
        }
        if(!registry.createKey(HKCU,null,subkey)) {
            fail();
        }
        if(registry.deleteKey(HKCU,subkey,null)) {
            fail();
        }
        if(registry.createKey(HKCU,null,subkey)) {
            fail();
        }
        if(registry.deleteKey(HKCU,null,null)) {
            fail();
        }
        if(registry.createKey(HKCU,null,subkey)) {
            fail();
        }
        if(registry.deleteKey(HKCU,null)) {
            fail();
        }
        if(!registry.deleteKey(HKCU,null,subkey)) {
            fail();
        }
        return;
    }
    
    public void testIsKeyExists() {
     
        String keys  = "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";
        if(registry.isKeyExists(HKCU, keys)) {
            return;
        }
        fail();
    }
     
     
    public void testIsValueExists() {
        if(!registry.isValueExists(HKCU,
                "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings",
                "User Agent")) {
            fail();
        }
     
    }
     
    public void testIsKeyEmpty() {
        if(!registry.isKeyEmpty(
                HKCU,
                "Software\\Microsoft\\Windows")) {
            fail();
        }
    }
     
     
    public void testAddRemoveStringValue() {
     
        if(registry.isKeyExists(HKCU,sw + sep + subkey)) {
            if(!registry.deleteKey(HKCU,sw,subkey)) {
                fail("Can`t delete key");
            }
        }
     
        if(!registry.createKey(HKCU,sw,subkey)) {
            fail("Can`t create key");
        }
     
     
        if(registry.isValueExists(HKCU,sw + sep + subkey,valueName)) {
            if(!registry.deleteValue(HKCU,sw + sep + subkey, valueName)) {
                fail("Can`t delete value");
            }
        }
     
        if(!registry.setStringValue(HKCU,sw + sep + subkey, valueName, stringValue, false)) {
            fail("Can`t set string value");
        }
     
        String getV = registry.getStringValue(HKCU,sw + sep + subkey, valueName, false);
     
        if(getV == null) {
            fail("getStringValue return null");
        }
     
        if(!getV.equals(stringValue)) {
            fail("\n\ngetStringValue returned \"" + getV + "\"!=\""+stringValue + "\"\n");
        }
     
        if(!registry.setStringValue(HKCU,sw + sep + subkey, valueName, stringValue, false)) {
            fail("Can`t set string value");
        }
     
        String env = "USERPROFILE";
        String value = System.getenv(env);
        if(value==null) {
            env = "TEMP";
            value = System.getenv(env);
        }
        if(value==null) {
            env = "SYSTEMROOT";
            value = System.getenv(env);
        }
        if(value!=null) {
            if(!registry.setStringValue(HKCU,sw + sep + subkey, valueName, "%" + env +"%", true)) {
                fail("Can`t set string value");
            }
            getV = registry.getStringValue(HKCU,sw + sep + subkey, valueName, true);
     
            if(getV == null) {
                fail("getStringValue return null");
            }
     
            if(!getV.equals(value)) {
                fail("\n\ngetStringValue returned \"" + getV + "\"!=\""+value + "\"\n");
            }
        }
     
        if(!registry.deleteValue(HKCU,sw + sep + subkey, valueName)) {
            fail();
        }
        if(!registry.deleteKey(HKCU,sw,subkey)) {
                fail("Can`t delete key");
            }
    }
     
    public void testAddRemoveKey() {
     
        if(registry.isKeyExists(HKCU,sw + sep + subkey)) {
            if(!registry.deleteKey(HKCU,sw, subkey)) {
                fail("Can`t delete key");
            }
        }
     
        if(!registry.createKey(HKCU,sw,subkey)) {
            fail("Can`t create key");
        }
     
        if(!registry.deleteKey(HKCU,sw, subkey)) {
            fail("Can`t delete key");
        }
    }
    public void testAddRemoveBinaryValue() {
        if(registry.isKeyExists(HKCU,sw + sep + subkey)) {
            if(!registry.deleteKey(HKCU,sw,subkey)) {
                fail("Can`t delete key");
            }
        }
     
        if(!registry.createKey(HKCU,sw,subkey)) {
            fail("Can`t create key");
        }
     
     
        if(registry.isValueExists(HKCU,sw + sep + subkey,valueName)) {
            if(!registry.deleteValue(HKCU,sw + sep + subkey, valueName)) {
                fail("Can`t delete value");
            }
        }
        byte [] data = new byte [] {0,Byte.MIN_VALUE,Byte.MIN_VALUE/2, Byte.MAX_VALUE, Byte.MAX_VALUE/2};
     
        if(!registry.setBinaryValue(HKCU,sw + sep + subkey, valueName, data)) {
            fail("Can`t set string value");
        }
        byte [] getV = registry.getBinaryValue(HKCU,sw + sep + subkey, valueName);
        if(getV == null) {
            fail("getBinaryValue return null");
        }
     
        if(getV.length!=data.length) {
            fail("getBinaryValue return unexpected length of data");
        }
        for(int i=0;i<getV.length;i++) {
            if(getV[i]!=data[i]) {
                fail("\n\ngetBinaryValue returned \"" + getV[i] +
                        "\"!=\"" + data[i] + "\"\n");
            }
        }
     
        byte [] data2 = new byte [] {Byte.MIN_VALUE,Byte.MAX_VALUE,0};
     
        if(!registry.setBinaryValue(HKCU,sw + sep + subkey, valueName, data2)) {
            fail("Can`t set string value");
        }
     
        getV = registry.getBinaryValue(HKCU,sw + sep + subkey, valueName);
     
        if(getV == null) {
            fail("getBinaryValue return null");
        }
     
        if(getV.length!=data2.length) {
            fail("getBinaryValue return unexpected length of data");
        }
        for(int i=0;i<getV.length;i++) {
            if(getV[i]!=data2[i]) {
                fail("\n\ngetBinaryValue returned \"" + getV[i] +
                        "\"!=\"" + data2[i] + "\"\n");
            }
        }
     
        if(!registry.deleteValue(HKCU,sw + sep + subkey, valueName)) {
            fail();
        }
        if(!registry.deleteKey(HKCU,sw,subkey)) {
                fail("Can`t delete key");
            }
    }
    public void testAddRemoveMultiStringValue() {
        if(registry.isKeyExists(HKCU,sw + sep + subkey)) {
            if(!registry.deleteKey(HKCU,sw,subkey)) {
                fail("Can`t delete key");
            }
        }
     
        if(!registry.createKey(HKCU,sw,subkey)) {
            fail("Can`t create key");
        }
     
     
        if(registry.isValueExists(HKCU,sw + sep + subkey,valueName)) {
            if(!registry.deleteValue(HKCU,sw + sep + subkey, valueName)) {
                fail("Can`t delete value");
            }
        }
        String [] data = new String [] {"String1","String2","String3","String4"};
     
        if(!registry.setMultiStringValue(HKCU,sw + sep + subkey, valueName, data)) {
            fail("Can`t set multi string value");
        }
        String [] getV = registry.getMultiStringValue(HKCU,sw + sep + subkey, valueName);
        if(getV == null) {
            fail("getMultiStringValue return null");
        }
     
        if(getV.length!=data.length) {
            fail("getMultiStringValue return unexpected length of data");
        }
        for(int i=0;i<getV.length;i++) {
            if(!getV[i].equals(data[i])) {
                fail("\n\ngetBinaryValue returned \"" + getV[i] +
                        "\"!=\"" + data[i] + "\"\n");
            }
        }
     
        String [] data2 = new String [] {"String1","String2","String3","String4","String5"};
     
        if(!registry.setMultiStringValue(HKCU,sw + sep + subkey, valueName, data2)) {
            fail("Can`t set string value");
        }
     
        getV = registry.getMultiStringValue(HKCU,sw + sep + subkey, valueName);
     
        if(getV == null) {
            fail("getBinaryValue return null");
        }
     
        if(getV.length!=data2.length) {
            fail("getMultiStringValue return unexpected length of data");
        }
        for(int i=0;i<getV.length;i++) {
            if(!getV[i].equals(data2[i])) {
                fail("\n\ngetMultiStringValue returned \"" + getV[i] +
                        "\"!=\"" + data2[i] + "\"\n");
            }
        }
     
        if(!registry.deleteValue(HKCU,sw + sep + subkey, valueName)) {
            fail();
        }
        if(!registry.deleteKey(HKCU,sw,subkey)) {
                fail("Can`t delete key");
            }
    }
    public void testAddRemove32BitValue() {
        if(registry.isKeyExists(HKCU,sw + sep + subkey)) {
            if(!registry.deleteKey(HKCU,sw,subkey)) {
                fail("Can`t delete key");
            }
        }
     
        if(!registry.createKey(HKCU,sw,subkey)) {
            fail("Can`t create key");
        }
     
     
        if(registry.isValueExists(HKCU,sw + sep + subkey,valueName)) {
            if(!registry.deleteValue(HKCU,sw + sep + subkey, valueName)) {
                fail("Can`t delete value");
            }
        }
        int data = Integer.MAX_VALUE;
     
        if(!registry.set32BitValue(HKCU,sw + sep + subkey, valueName, data)) {
            fail("Can`t set integer value");
        }
        int getV = registry.get32BitValue(HKCU,sw + sep + subkey, valueName);
        if(getV == -1) {
            fail("get32BitValue return null");
        }
     
        if(getV!=data) {
            fail("\n\nget32BitValue returned \"" + getV +
                    "\"!=\"" + data + "\"\n");
        }
     
        int data2 = Integer.MIN_VALUE;
     
        if(!registry.set32BitValue(HKCU,sw + sep + subkey, valueName, data2)) {
            fail("Can`t set integer value");
        }
        getV = registry.get32BitValue(HKCU,sw + sep + subkey, valueName);
        if(getV == -1) {
            fail("get32BitValue return null");
        }
     
        if(getV!=data2) {
            fail("\n\nget32BitValue returned \"" + getV +
                    "\"!=\"" + data2 + "\"\n");
        }
     
        if(!registry.deleteValue(HKCU,sw + sep + subkey, valueName)) {
            fail();
        }
        if(!registry.deleteKey(HKCU,sw,subkey)) {
                fail("Can`t delete key");
            }
    }
    public void testGetValueNames () {
        if(registry.isKeyExists(HKCU,sw + sep + subkey)) {
            if(!registry.deleteKey(HKCU,sw,subkey)) {
                fail("Can`t delete key");
            }
        }
     
        if(!registry.createKey(HKCU,sw,subkey)) {
            fail("Can`t create key");
        }
     
        String [] names = { "Super Duper Name",
        "\\u1047\\u1085\\u1072\\u1081\\u32\\u1080\\u32\\u1083" + //NOI18N
                "\\u1102\\u1073\\u1080\\u32\\u1074\\u1077\\u1083" + //NOI18N
                "\\u1080\\u1082\\u1080\\u1081\\u32\\u1080\\u32\\u1084" + //NOI18N
                "\\u1086\\u1075\\u1091\\u1095\\u1080\\u1081\\u46\\u46\\u46"}; //NOI18N
        for(int i=0;i<names.length;i++)  {
            if(!registry.setStringValue(HKCU,sw + sep + subkey, names[i],"Value")) {
                fail("Can`t set string value with name: " + names[i]);
            }
        }
        String [] namesV = registry.getValueNames(HKCU,sw + sep + subkey);
        if(namesV == null) {
            fail("getValueNames returned unexpected null");
        }
        if(namesV.length != names.length) {
            fail("getValueNames returned unexpected length");
        }
        for(int i=0;i<names.length;i++) {
            if(!names[i].equals(namesV[i])) {
                fail("getValueNames returned unexpected name. \nWas: " + namesV[i] +
                        "\nExpected: " + names[i]);
            }
        }
     
        if(!registry.deleteKey(HKCU,sw,subkey)) {
                fail("Can`t delete key");
            }
    }*/
    public void testNone() {
        
    }
}

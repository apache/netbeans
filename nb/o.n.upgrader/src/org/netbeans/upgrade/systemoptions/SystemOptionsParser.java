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

package org.netbeans.upgrade.systemoptions;

import java.io.*;
import java.util.Iterator;
import java.util.Set;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Radek Matous
 */
public class SystemOptionsParser  {
    static final String EXPECTED_INSTANCE = "org.openide.options.SystemOption";//NOI18N
    
    private String systemOptionInstanceName;
    private boolean types;
    
    private SystemOptionsParser(final String systemOptionInstanceName, final boolean types) {
        this.systemOptionInstanceName = systemOptionInstanceName;
        this.types = types;
    }
    
    public static DefaultResult parse(FileObject settingsFo, boolean types) throws IOException, ClassNotFoundException {
        SettingsRecognizer instance = getRecognizer(settingsFo);
        
        try (InputStream is = instance.getSerializedInstance()) {
            SerParser sp = new SerParser(is);
            SerParser.Stream s = sp.parse();
            SystemOptionsParser rImpl = new SystemOptionsParser(instance.instanceName(), types);
            DefaultResult ret = (DefaultResult)rImpl.processContent(s.contents.iterator(), false);
            ret.setModuleName(instance.getCodeNameBase().replace('.','/'));
            return ret;
        }
    }
    
    private Result processContent(final Iterator<Object> it, final boolean reachedWriteReplace) {
        for (; it.hasNext();) {
            Object elem = it.next();
            if (!reachedWriteReplace && elem instanceof SerParser.ObjectWrapper) {
                SerParser.ObjectWrapper ow = (SerParser.ObjectWrapper)elem;
                String name = ow.classdesc.name;
                if (name.endsWith("org.openide.util.SharedClassObject$WriteReplace;")) {//NOI18N
                    return processContent(ow.data.iterator(), true);
                }
            } else if (reachedWriteReplace && elem instanceof SerParser.NameValue ) {
                SerParser.NameValue nv = (SerParser.NameValue)elem;
                if (systemOptionInstanceName.equals(nv.value)) {
                        Result result = ContentProcessor.parseContent(systemOptionInstanceName, types, it);
                    return result;
                }
            }
        }
        return null;
    }            
            
    private static SettingsRecognizer getRecognizer(final FileObject settingsFo) throws IOException {
        SettingsRecognizer recognizer = new SettingsRecognizer(false, settingsFo);
        recognizer.parse();
        
        Set instances = recognizer.getInstanceOf();
        String iName = recognizer.instanceName();
        if (!instances.contains(EXPECTED_INSTANCE)) {
            throw new IOException(iName);
        }
        return recognizer;
    }
}





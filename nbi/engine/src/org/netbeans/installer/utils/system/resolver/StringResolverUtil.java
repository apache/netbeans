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

package org.netbeans.installer.utils.system.resolver;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dmitry Lipin
 */
public class StringResolverUtil {
    
    private static List <StringResolver> list;
    
    public static final String resolve(String string, ClassLoader loader) {
        String parsed = string;
        if(parsed == null) {
            return null;
        }
        if(list==null) {
            list = createStringResolvers();
        }
        for(StringResolver resolver : list) {
            parsed = resolver.resolve(parsed, loader);
            if(parsed == null) {
                break;
            }
        }
        return parsed;
    }
    private static List <StringResolver> createStringResolvers() {
        List <StringResolver> srlist = new ArrayList<StringResolver> ();
        srlist.add(new NameResolver());
        srlist.add(new BundlePropertyResolver());
        srlist.add(new FieldResolver());
        srlist.add(new MethodResolver());
        srlist.add(new ResourceResolver());
        srlist.add(new SystemPropertyResolver());
        srlist.add(new EnvironmentVariableResolver());
        return srlist;
    }
}

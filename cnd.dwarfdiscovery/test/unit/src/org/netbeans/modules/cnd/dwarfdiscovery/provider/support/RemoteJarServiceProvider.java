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

package org.netbeans.modules.cnd.dwarfdiscovery.provider.support;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.cnd.dwarfdump.Offset2LineService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;

/**
 *
 */
public class RemoteJarServiceProvider {
    private static final Class<?> service = Offset2LineService.class;
    private static final String localAbsPath;
    static {
        localAbsPath = findPaths();
    }

    private static String findPaths() {
        try {
            return service.getProtectionDomain().getCodeSource().getLocation().getPath();
        } catch (Throwable thr) {
            Exceptions.printStackTrace(thr);
            return null;
        }
    }

    public static ProcessUtils.ExitStatus getJavaProcess(String java, Class<?> clazz, ExecutionEnvironment env, String[] arguments) throws IOException{
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
        npb.setCharset(Charset.forName("UTF-8")); // NOI18N
        npb.setExecutable(java); //NOI18N
        List<String> args = new ArrayList<String>();
        args.add("-cp"); //NOI18N
        args.add(localAbsPath); //NOI18N
        args.add(clazz.getName());
        args.addAll(Arrays.asList(arguments));
        npb.setArguments(args.toArray(new String[args.size()]));
        return ProcessUtils.execute(npb);
    }
}

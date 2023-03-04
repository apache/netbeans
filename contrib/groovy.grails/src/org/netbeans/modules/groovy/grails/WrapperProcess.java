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

package org.netbeans.modules.groovy.grails;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import org.netbeans.api.extexecution.ExternalProcessSupport;

/**
 *
 * @author mkleint
 */
public class WrapperProcess extends Process {

    public static final String KEY_UUID = "NB_EXEC_GRAILS_PROCESS_UUID"; //NOI18N

    private final String uuid;

    private final Process del;

    public WrapperProcess(Process delegate, String uuid) {
        this.del = delegate;
        this.uuid = uuid;
    }

    @Override
    public OutputStream getOutputStream() {
        return del.getOutputStream();
    }

    @Override
    public InputStream getInputStream() {
        return del.getInputStream();
    }

    @Override
    public InputStream getErrorStream() {
        return del.getErrorStream();
    }

    @Override
    public int waitFor() throws InterruptedException {
        return del.waitFor();
    }

    @Override
    public int exitValue() {
        return del.exitValue();
    }

    @Override
    public void destroy() {
        ExternalProcessSupport.destroy(del, Collections.singletonMap(KEY_UUID, uuid));
    }

}

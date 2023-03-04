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

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import java.io.OutputStream;
import org.netbeans.modules.j2ee.deployment.common.api.ValidationException;
import org.openide.filesystems.FileObject;

/**
 * Verifier service to be implmeneted by Server Integration Plugin.
 * Instance of this service needs to be declared in plugin module layer.xml.
 *
 * @author nn136682
 */
public abstract class VerifierSupport {

    /**
     * Whether the verifier support this module type; default to supports all types.
     */
    // FIXME use J2eeModule.Type
    public boolean supportsModuleType(Object moduleType) {
        return true;
    }
    
    /**
     * Verify the provided target J2EE module or application, including both
     * standard J2EE and platform specific deployment info.  The provided 
     * service could include invoking its own specific UI displaying of verification
     * result. In this case, the service could have limited or no output to logger stream.
     *
     * @param target The an archive, directory or file to verify.
     * @param logger Log stream to write verification output to.
     * @exception ValidationException if the target fails the validation.
     */
    public abstract void verify(FileObject target, OutputStream logger) throws ValidationException;
    
}

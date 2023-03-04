/**
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

package org.netbeans.installer.infra.autoupdate;

import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationException.ERROR_TYPE;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.spi.autoupdate.CustomUninstaller;

/**
 *
 */
public class NbiCustomUninstaller implements CustomUninstaller {
    private Product product;
    
    public NbiCustomUninstaller(
            final Product product) {
        this.product = product;
    }

    public boolean uninstall(
            final String name, 
            final String version, 
            final ProgressHandle progressHandle) throws OperationException {
        final Progress uninstallProgress = 
                new Progress(new ProgressHandleAdapter(progressHandle));
        
        try {
            product.uninstall(uninstallProgress);
        } catch (UninstallationException e) {
            throw new OperationException(ERROR_TYPE.UNINSTALL, e);
        }
        
        return true;
    }

}

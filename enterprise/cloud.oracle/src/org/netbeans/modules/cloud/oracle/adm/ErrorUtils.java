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
package org.netbeans.modules.cloud.oracle.adm;

import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.responses.BmcResponse;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */

@NbBundle.Messages({
    "# {0} - error code number",
    "MSG_Error_Code=Error Code {0} - ",
    "MSG_Error_400=Bad request",
    "MSG_Error_401=Unauthorized",
    "MSG_Error_404=Not Found",
    "MSG_Error_412=Precondition failed",
    "MSG_Error_429=Too Many Requests",
    "MSG_Error_500=Internal Server Error",
})
class ErrorUtils {
    
    private static String getErrorDescription(int code) {
       switch (code) {
            case 400: return Bundle.MSG_Error_400();
            case 401: return Bundle.MSG_Error_401();
            case 404: return Bundle.MSG_Error_404();
            case 412: return Bundle.MSG_Error_412();
            case 429: return Bundle.MSG_Error_429();
            case 500: return Bundle.MSG_Error_429();
            default: return "Unknown";
        } 
    }
    
    public static void processError(AuditException exc, String errorMessage) {
        StringBuilder sb = new StringBuilder(errorMessage);
        sb.append('\n').append(Bundle.MSG_Error_Code(exc.getStatusCode()));
        sb.append(getErrorDescription(exc.getStatusCode()));
        sb.append('\n').append(exc.getMessage());
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(sb.toString()));
    }
    
    public static void processError(BmcResponse reqest, String errorMessage) {
        StringBuilder sb = new StringBuilder(errorMessage);
        sb.append('\n').append(Bundle.MSG_Error_Code(reqest.get__httpStatusCode__()));
        sb.append(getErrorDescription(reqest.get__httpStatusCode__()));
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(sb.toString()));
    }
    
   public static void processError(BmcException exc, String errorMessage) {
       StringBuilder sb = new StringBuilder(errorMessage);
        sb.append('\n').append(Bundle.MSG_Error_Code(exc.getStatusCode()));
        sb.append(getErrorDescription(exc.getStatusCode()));
        sb.append('\n').append(exc.getMessage());
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(sb.toString()));
   }
}

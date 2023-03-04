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

#ifndef _Errors_H
#define	_Errors_H

#ifdef	__cplusplus
extern "C" {
#endif

    
#define ERROR_OK                            0
#define ERROR_INTEGRITY                     1000
#define ERROR_FREESPACE                     1001
#define ERROR_INPUTOUPUT                    1002
#define ERROR_JVM_UNCOMPATIBLE              1003
#define ERROR_JVM_NOT_FOUND                 1004
#define ERROR_ON_EXECUTE_PROCESS            1005
#define ERROR_PROCESS_TIMEOUT               1006
#define ERROR_USER_TERMINATED               1007
#define EXTERNAL_RESOURCE_MISSING           1008
#define ERROR_BUNDLED_JVM_EXTRACTION        1009
#define ERROR_BUNDLED_JVM_VERIFICATION      1010
    
#define EXIT_CODE_EVENTS_INITIALIZATION_ERROR 1022
#define EXIT_CODE_GUI_INITIALIZATION_ERROR  1023
#define EXIT_CODE_STUB                      1024
#define EXIT_CODE_SYSTEM_ERROR              1025


#ifdef	__cplusplus
}
#endif

#endif	/* _Errors_H */


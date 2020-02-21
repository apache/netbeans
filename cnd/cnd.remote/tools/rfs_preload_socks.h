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

#ifndef _RFS_PRELOAD_SOCKS_H
#define	_RFS_PRELOAD_SOCKS_H

/**
 * as well as open syscall, returns
 * -1 if an error occurred, or
 * non-negative integer in the case of success
 */
int get_socket(int create);

void release_socket();

#if TRACE
//void trace_sd(const char* text);
#define trace_sd(...)
#else
#define trace_sd(...)
#endif

#endif	/* _RFS_PRELOAD_SOCKS_H */


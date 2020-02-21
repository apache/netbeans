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

#define string_in_macro_params_XMST(OBJ) CONST_CAST(char *,OBJ)
#define DDD_NAME     "DDD"
#define DDD_VERSION  "3.3.11"
#define DDD_HOST   "x86_64-unknown-linux-gnu"

void string_in_macro_params_foo() {
    char * s = string_in_macro_params_XMST(
		    "GNU " DDD_NAME " " DDD_VERSION " (" DDD_HOST "), "
		    "by Dorothea L\374tkehaus and Andreas Zeller.\n"
		    "Copyright \251 1995-1999 "
		    "Technische Universit\344t Braunschweig, Germany.\n"
		    "Copyright \251 1999-2001 "
		    "Universit\344t Passau, Germany.\n"
		    "Copyright \251 2001 "
		    "Universit\344t des Saarlandes, Germany.\n"
		    "Copyright \251 2001-2004 "
		    "Free Software Foundation, Inc.\n");
}
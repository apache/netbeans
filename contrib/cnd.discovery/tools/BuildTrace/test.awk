# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

BEGIN {
    R["called:"]=0; 
    R["gcc"]=0; 
    R["BuildTrace"]=0; 
    R["--version"]=0; 
    S["exit"]=0
}
/called:/{ R["called:"]++; next }
/BuildTrace/{ R["BuildTrace"]++; next }
/gcc/{ R["gcc"]++; next }
/--version/{ R["--version"]++; next }
NF == 0 { next }
{ print "Unexpected output " $0 }
END {
    for (r in R) { 
        if (R[r] != 3) { 
            print "Expected 3 for #" r " but " R[r] " found"
            S["exit"]=1
        } 
    } 
    exit S["exit"]
}

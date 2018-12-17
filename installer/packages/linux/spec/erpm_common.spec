#
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
#
# This should be the same as the Solaris package version. #
%define global_product_version 4.1

# This is an RPM-specific thing.  The RPM manual says that this is the
# package's version (rather than the package's content's version).  So
# this should be 1 unless a package is redelivered with the same
# Version, in which case the Release should be incremented each time
# the package is redelivered.  I.e. if the Version of the RPM's is the
# same for EA and FCS, then the Release should be incremented for FCS.
%define global_product_release 1

%define _prefix /usr/lib

Version: %{global_product_version}
Release: %{global_product_release}
Group: Applications
Copyright: commercial
Vendor: Sun Microsystems, Inc.
URL: http://www.sun.com/
Prefix: %_prefix
AutoReqProv: no

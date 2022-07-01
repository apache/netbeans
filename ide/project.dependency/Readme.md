<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->

# Project Dependencies

This module is highly experimental; provides an abstract API to query project dependncies
that shall be implemented by
- gradle
- maven
- ant/nbm support
At this moment, just Maven implements it and I need to validate the API design by adding
the other implementations before the API is going to be published officially. In the
meantime, implementation dependencies are required to access / implement the API.

The code will **eventually merge** into `project.api` module and this experimental one
will be deleted.


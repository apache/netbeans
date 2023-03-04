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

package org.netbeans.modules.bugzilla.query;

/**
 *
 * @author tomas
 */
public interface QueryConstants {
    String REPO_NAME = "Beautiful";
    String QUERY_NAME = "Hilarious";
    String PARAMETERS_FORMAT =
        "&short_desc_type=allwordssubstr&short_desc={0}" +
        "&long_desc_type=substring&long_desc=&bug_file_loc_type=allwordssubstr" +
        "&bug_file_loc=&status_whiteboard_type=allwordssubstr&status_whiteboard=" +
        "&keywords_type=allwords&keywords=&deadlinefrom=&deadlineto=&bug_status=NEW" +
        "&bug_status=ASSIGNED&bug_status=REOPENED&emailassigned_to1=1&emailtype1=substring" +
        "&email1=&emailassigned_to2=1&emailreporter2=1&emailqa_contact2=1&emailcc2=1" +
        "&emailtype2=substring&email2=&bugidtype=include&bug_id=&votes=&chfieldfrom=" +
        "&chfieldto=Now&chfieldvalue=&cmdtype=doit&order=Reuse+same+sort+as+last+time" + "" +
        "&field0-0-0=noop&type0-0-0=noop&value0-0-0=";
}

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

#include <stdio.h>

void go();

int main(int argc, char**argv) {
    go();
    return 0;
}

void go() {
    int i;

    char *prompt = "\nType a string (or 'q' when done) >>>\n";
    for (i = 0; i < 25; i++) {
	char line[132];
	if (feof(stdin))
	    break;
	printf(prompt);
	fflush(stdout);
	scanf("%s", line);
	printf("Read: %s\n", line);
	if (line[0] == 'q' || line[0] == 'Q')
	    break;
    }
}

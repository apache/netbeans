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
 /*
 * Author: Tomas Holy
 */

#ifndef _CMDARGS_H
#define	_CMDARGS_H

class CmdArgs {
public:

    CmdArgs(int _count) {
        used = 0;
        size = _count;
        args = new char*[size];
        memset(args, 0, size * sizeof (char*));
    }

    ~CmdArgs() {
        if (args) {
            for (int i = 0; i < size; i++) {
                delete[] args[i];
            }
            delete[] args;
        }
    }

    void add(const char *arg) {
        if (used + 1 > size) {
            int newSize = size + size / 2 + 1;
            char **newArgs = new char*[newSize];
            memcpy(newArgs, args, size * sizeof (char*));
            memset(newArgs + size, 0, (newSize - size) * sizeof (char*));
            delete[] args;
            args = newArgs;
            size = newSize;
        }
        args[used] = new char[strlen(arg) + 1];
        strcpy(args[used++], arg);
    }

    void addCmdLine(const char *cmdLine) {
        char arg[1024] = "";
        bool inQuotes = false;
        bool inText = false;
        int i = 0;
        int j = 0;
        char c;
        while (c = cmdLine[i]) {
            if (inQuotes) {
                if (c == '\"') {
                    inQuotes = false;
                } else {
                    arg[j++] = c;
                }
            } else {
                switch (c) {
                    case '\"':
                        inQuotes = true;
                        inText = true;
                        break;
                    case ' ':
                    case '\t':
                    case '\n':
                    case '\r':
                        if (inText) {
                            arg[j] = '\0';
                            add(arg);
                            j = 0;
                        }
                        inText = false;
                        break;
                    default:
                        inText = true;
                        arg[j++] = c;
                        break;
                }
            }
            i++;
        }
        if (j > 0) {
            arg[j] = '\0';
            add(arg);
        }
    }

    int getCount() {
        return used;
    }

    char **getArgs() {
        return args;
    }

private:
    int used;
    int size;
    char **args;
};

#endif	/* _CMDARGS_H */


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

#ifndef _NBEXECLOADER_H
#define	_NBEXECLOADER_H

#include "utilsfuncs.h"

#define HELP_MSG \
"\
  --console suppress    supppress console output\n\
  --console new         open new console for output\n\
\n"

class NBExecLoader {
    typedef int (*StartPlatform)(int argc, char *argv[], const char *help);

public:
    NBExecLoader()
        : hLib(0) {
    }
    ~NBExecLoader() {
        if (hLib) {
            FreeLibrary(hLib);
        }
    }
    int start(const char *path, int argc, char *argv[]) {
        if (!hLib) {
            hLib = LoadLibrary(path);
            if (!hLib) {
                logErr(true, true, "Cannot load \"%s\".", path);
                return -1;
            }
        }

        StartPlatform startPlatform = (StartPlatform) GetProcAddress(hLib, "startPlatform");
        if (!startPlatform) {
            logErr(true, true, "Cannot start platform, failed to find startPlatform() in %s", path);
            return -1;
        }
        logMsg("Starting platform...\n");
        return startPlatform(argc, argv, HELP_MSG);
    }

private:
    HMODULE hLib;
};

#endif	/* _NBEXECLOADER_H */


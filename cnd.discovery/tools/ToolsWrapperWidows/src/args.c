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
#include <string.h>
#include <stdlib.h>
#define MY_MAX_PATH (1024)
#ifdef MINGW
#include <process.h>
#else
#include <spawn.h>
#include <sys/wait.h>
#endif
#include <unistd.h>
#include <limits.h>
#ifdef WINDOWS
#define PATH_SEPARATOR ";"
#define FILE_SEPARATOR_CHAR '\\'
#define FILE_SEPARATOR_STRING "\\"
#else
#define PATH_SEPARATOR ":"
#define FILE_SEPARATOR_CHAR '/'
#define FILE_SEPARATOR_STRING "/"
#endif
#define MAGIC "echo magic"
extern char **environ;
#define COPY(x) x x x x x x x x x x
char *real_binary = COPY(COPY(MAGIC));

void prependPath(char* path) {
    char** e = environ;
    while (e) {
        char *buf = malloc(strlen(*e) + 1);
        strcpy(buf, *e);
        char* eq = strchr(buf, '=');
        if (eq != NULL) {
            *eq = 0;
            if (strcasecmp("PATH", buf) == 0) {
                char* old_path = getenv(buf);
                char *tool_path = malloc(strlen(path) + 1);
                strcpy(tool_path, path);
                char* key;
                key = strrchr(tool_path, FILE_SEPARATOR_CHAR);
                if (key != NULL) {
                    *key = 0;
                }
                char *new_path = malloc(strlen(tool_path) + 1 + strlen(old_path) + 1);
#ifdef WINDOWS
                sprintf(new_path, "%s;%s", tool_path, old_path); 
#else
                sprintf(new_path, "%s:%s", tool_path, old_path); 
#endif
#ifdef MINGW
                char *path_macro = malloc(strlen(buf) + 1 + strlen(new_path) + 1);
                sprintf(path_macro, "%s=%s", buf, new_path); 
                putenv(path_macro);
                free(path_macro);
#else
                setenv(buf, new_path, 1);
#endif
                free(new_path);
                free(tool_path);
                free(buf);
                break;
            }
        }
        free(buf);
        e++;
    }
}

int main(int argc, char**argv) {
    char *pattern = MAGIC;
    char *place = real_binary;
    int changed = 0;
    while(*pattern) {
        if (*place != *pattern) {
            changed = 1;
            break;
        }
        pattern++;
        place++;
    }
    if (!changed) {
        printf("Real compiler is not set\n");
        return -1;
    }

    argv[0] = real_binary;
    char* log = getenv("__CND_BUILD_LOG__");
    if (log != NULL) {
        FILE* flog = fopen(log, "a");
        if (flog != NULL) {
            fprintf(flog, "called: %s\n", real_binary);
            char *buf = malloc(MY_MAX_PATH + 1);
            getcwd(buf, MY_MAX_PATH);
            fprintf(flog, "\t%s\n", buf);
            char** par = (char**) argv;
            for (; *par != 0; par++) {
                fprintf(flog, "\t%s\n", *par);
            }
            fprintf(flog, "\n");
            fflush(flog);
            fclose(flog);
        }
    }
    prependPath(real_binary);
#ifdef MINGW
    // It seems MinGW merge arguments in command line
    // Wrap each argument in quote.
    char** par = (char**) argv;
    int len = 0;
    for (; *par != 0; par++) {
        len += 3;
        char *i = *par;
        while(*i) {
            if (*i == '"') {
                len++;
            }
            len++;
            i++;
        }
    }
    char *newpar = malloc(len);
    char *line = newpar;
    par = (char**) argv;
    for (; *par != 0; par++) {
        *newpar++ = '"';
        char *i = *par;
        while(*i) {
            if (*i == '"') {
                *newpar++ = '\\';
            }
            *newpar++ = *i++;
        }
        *newpar++ = '"';
        *newpar++ = ' ';
    }
    *--newpar = 0;
    argv[0] = line;
    argv[1] = 0;
    
    return spawnv(P_WAIT, real_binary, argv);
#else
    pid_t pid;
    int status;
    status = posix_spawn(&pid, tool, NULL, NULL, argv, environ);
    if (status == 0) {
        if (waitpid(pid, &status, 0) != -1) {
            return status;
        } else {
            return -1;
        }
    } else {
        return status;
    }
#endif
}

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

#include <signal.h>
#include <sys/types.h>
#include "killall.h"
#include "pfind.h"

const char* progname;

#ifndef SOLARIS 

typedef struct signame {
    const char *sigstr;
    const int signum;
} signame_t;

static signame_t signames[] = {
#ifdef SIGABRT
    {"ABRT", SIGABRT},
#endif
#ifdef SIGALRM
    {"ALRM", SIGALRM},
#endif
#ifdef SIGBUS
    {"BUS", SIGBUS},
#endif
#ifdef SIGCANCEL
    {"CANCEL", SIGCANCEL},
#endif
#ifdef SIGCHLD
    {"CHLD", SIGCHLD},
#endif
#ifdef SIGCLD
    {"CLD", SIGCLD},
#endif
#ifdef SIGCONT
    {"CONT", SIGCONT},
#endif
#ifdef SIGEMT
    {"EMT", SIGEMT},
#endif
#ifdef SIGFPE
    {"FPE", SIGFPE},
#endif
#ifdef SIGFREEZE
    {"FREEZE", SIGFREEZE},
#endif
#ifdef SIGHUP
    {"HUP", SIGHUP},
#endif
#ifdef SIGILL
    {"ILL", SIGILL},
#endif
#ifdef SIGINFO
    {"INFO", SIGINFO},
#endif
#ifdef SIGINT
    {"INT", SIGINT},
#endif
#ifdef SIGIO
    {"IO", SIGIO},
#endif
#ifdef SIGIOT
    {"IOT", SIGIOT},
#endif
#ifdef SIGJVM1
    {"JVM1", SIGJVM1},
#endif
#ifdef SIGJVM2
    {"JVM2", SIGJVM2},
#endif
#ifdef SIGKILL
    {"KILL", SIGKILL},
#endif
#ifdef SIGLOST
    {"LOST", SIGLOST},
#endif
#ifdef SIGLWP
    {"LWP", SIGLWP},
#endif
#ifdef SIGPIPE
    {"PIPE", SIGPIPE},
#endif
#ifdef SIGPOLL
    {"POLL", SIGPOLL},
#endif
#ifdef SIGPROF
    {"PROF", SIGPROF},
#endif
#ifdef SIGPWR
    {"PWR", SIGPWR},
#endif
#ifdef SIGQUIT
    {"QUIT", SIGQUIT},
#endif
#ifdef SIGSEGV
    {"SEGV", SIGSEGV},
#endif
#ifdef SIGSTKFLT
    {"STKFLT", SIGSTKFLT},
#endif
#ifdef SIGSTOP
    {"STOP", SIGSTOP},
#endif
#ifdef SIGSYS
    {"SYS", SIGSYS},
#endif
#ifdef SIGTERM
    {"TERM", SIGTERM},
#endif
#ifdef SIGTHAW
    {"THAW", SIGTHAW},
#endif
#ifdef SIGTRAP
    {"TRAP", SIGTRAP},
#endif
#ifdef SIGTSTP
    {"TSTP", SIGTSTP},
#endif
#ifdef SIGTTIN
    {"TTIN", SIGTTIN},
#endif
#ifdef SIGTTOU
    {"TTOU", SIGTTOU},
#endif
#ifdef SIGURG
    {"URG", SIGURG},
#endif
#ifdef SIGUSR1
    {"USR1", SIGUSR1},
#endif
#ifdef SIGUSR2
    {"USR2", SIGUSR2},
#endif
#ifdef SIGVTALRM
    {"VTALRM", SIGVTALRM},
#endif
#ifdef SIGWAITING
    {"WAITING", SIGWAITING},
#endif
#ifdef SIGWINCH
    {"WINCH", SIGWINCH},
#endif
#ifdef SIGXCPU
    {"XCPU", SIGXCPU},
#endif
#ifdef SIGXFSZ
    {"XFSZ", SIGXFSZ},
#endif
#ifdef SIGXRES
    {"XRES", SIGXRES},
#endif
};

#define SIGCNT  (sizeof (signames) / sizeof (struct signame))

int str2sig(const char *name, int *sig_ret) {
    signame_t* sp;
    for (sp = signames; sp < &signames[SIGCNT]; sp++) {
        if (strcmp(sp->sigstr, name) == 0) {
            *sig_ret = sp->signum;
            return 0;
        }
    }
    return -1;
}
#endif // not defined SOLARIS

static int sendsignal(sigscope_t scope, int id, int sig) {
    switch (scope) {
        case S_PID:
            return kill((pid_t) id, sig);
        case S_PGID:
            return killpg((pid_t) id, sig);
#ifdef SOLARIS
        case S_SID:
            return sigsend(P_SID, (id_t) id, sig);
#endif
            // not supported on other systems?
            return -1;
    }
}

static int signal_by_env(int sig, const char* magicenv, int nosig) {
    pid_t* pids = pfind(magicenv);

    if (pids != NULL) {
        pid_t* p;
        for (p = pids; *p != 0; p++) {
            if (nosig) {
                printf("%d\n", (int) *p);
            } else {
                sendsignal(S_PID, (int) (*p), sig);
            }
        }
        free(pids);
    }

    return 0;
}

int main(int argc, char** argv) {
    options_t params;
    int nopt;

    // Get program name - this is used in error.c, for example
    progname = basename(argv[0]);

    // Parse options
    nopt = readopts(argc, argv, &params);
    argv += nopt;
    argc -= nopt;

    if (argc == 0) {
        err_quit("\n\nusage: %s -p|-g|-s signal_name id\n"
                "\t-p\t\tsend signal signal_name to a process with the specified id\n"
                "\t-g\t\tsend signal signal_name to all processes with the specified process group ID\n"
                "\t-s\t\tsend signal signal_name to all processes with the specified session ID\n"
                "\nusage: %s -q signal_name pid value\n"
                "\t-q\t\tsignal process with the given signal and integer value attached.\n"
                "\nusage: %s [-n] -e signal_name env\n"
                "\t-e\t\tfind all processes that have env entry in their environment and send the signal to them\n",
                progname, progname, progname);
    }

    if (params.scope == P_ENV) {
        return signal_by_env(params.sig, argv[0], params.nosignal);
    }

    params.id = atoi(argv[0]);

    if (params.id <= 0) {
        err_quit("Wrong ID: %s", argv[0]);
    }

    if (params.scope == P_QUEUE) {
        if (argc < 2) {
            err_quit("value is expected for sigqueue");
        }
#ifndef MACOSX
        union sigval value;
        value.sival_int = atoi(argv[1]);
        return sigqueue(params.id, params.sig, value);
#else
        // unsupported on Mac?
        return sendsignal(S_PID, params.id, params.sig);
#endif        
    }

    return sendsignal(params.scope, params.id, params.sig);
}

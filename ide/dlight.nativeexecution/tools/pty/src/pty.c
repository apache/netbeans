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

#include "pty.h"

#define NEED_CHILD_INFO_BEFORE_REAP 0

const char* progname;
static int got_sigchld = 0;
static int allocated_pty_fd = -1;
static struct tms start_tms;
static struct tms end_tms;
static clock_t start = 0;
static clock_t end;

// Local functions declarations 

#if NEED_CHILD_INFO_BEFORE_REAP
static void sigchld(int sig, siginfo_t* info, void* uap);
#endif //NEED_CHILD_INFO_BEFORE_REAP

static void sigcont(int sig);
static void modify_term(int slavefd, int echooff, int fixerase);
static void do_report(const char* fname, int status, struct rusage* rusage /*prusage_t* usage,*/ /*psinfo_t* psinfo,*/);

int main(int argc, char** argv) {
    int nopt;
    options_t params;

    // Get program name - this is used in error.c, for example
    progname = basename(argv[0]);

    // Parse options
    nopt = readopts(argc, argv, &params);

    // If dumpenv was requested just do it and we are done
    if (nopt == argc && params.envfile != NULL) {
        return dumpenv(params.envfile);
    }

    pid_t pid;

    // Bootstrap env
    if (params.envfile != NULL) {
        pid_t p = fork();

        if (p < 0) {
            err_sys("fork error");
        }

        if (p == 0) {
            char** env = readenv(params.envfile);
            execve(argv[0], argv, env);
            err_sys("execve failed");
        }

        int status, w;
        w = waitpid(p, &status, WUNTRACED | WCONTINUED);

        if (w != -1 && WIFEXITED(status)) {
            exit(WEXITSTATUS(status));
        }

        exit(EXIT_FAILURE);
    }

    argv += nopt;
    argc -= nopt;
    /* now argv points to the executable */

    if (argc == 0) {
        //  -e          turned echoing off
        //  -w          wait until signaled (SIGCONT) before executing a process
        //  -p          define pts_name to use instead of opening a new one
        //  --env       passes additional environment variable to a program
        //              in NAME=VALUE form. For multiple variables multiple
        //              --env options should be used.
        //  --no-pty    do not allocate pseudo-terminal - just execute a program
        //              if --no-pty is used, -p/-w has no effect
        //  --readenv   start a new process in an environment. recorded in a
        //              specified file.
        //  --dumpenv   record current environment in a specified file
        //  --report    record process' and exit status information into 
        //              specified file

        err_quit("\n\n"
                "usage: %s [-e] [--no-pty] [-w] [-p pts_name] [--set-erase-key]\n"
                "\t\t[--readenv env_file] [[--env NAME=VALUE] ...] [--dir dir]\n"
                "\t\t[--redirect-error] [--report report_file] program [ arg ... ]\n\n"
                "\t-e\t\t turn echoing off\n"
                "\t-w\t\t wait SIGCONT after reporting PID/TTY/.. but before executing the program\n"
                "\t-p\t\t define pts_name to attach process's I/O instead of opening a new one\n"
                "\t--readenv\t read environment to start process in from a file\n"
                "\t--env\t\t pass (additional) environment variable to the process\n"
                "\t--dir\t\t change working directory for starting process\n"
                "\t--redirect-error redirect stderror to stdout for starting process (makes sense if --no-pty only)\n"
                "\t--report\t record process' and exit status information into specified file\n\n"
                "usage: %s --dumpenv env_file\n"
                "\t--dumpenv\t dump environment to a file\n"
                , progname, progname);
        exit(-1);
    }

    // Set SIGCONT handler for both parent and child
    // This is to avoid a race condition
    signal(SIGCONT, sigcont);

    if (params.redirect_error) {
        close(STDERR_FILENO);
        dup2(STDOUT_FILENO, STDERR_FILENO);
    }

    if (params.nopty == 0) {
        if (params.pty != NULL) {
            pid = pty_fork1(params.pty);
        } else {
            pid = pty_fork(&allocated_pty_fd);
        }
    } else {
        pid = fork();
        // We call setsid for a child to make it a session leader -
        // this allows us later send signals to the whole session
        // But we don't want to do this in a single case - when --no-pty is 
        // used but we do have terminal (because in this case the connection 
        // with existent terminal will be lost and it will not be a controlling 
        // terminal for the process anymore.
        if (!isatty(STDOUT_FILENO)) {
            setsid();
        }
    }

    if (pid < 0) {
        err_sys("fork error");
    }

    if (pid == 0) {
        /* child */

        /*
         * get a name of a terminal we have
         */
        if (isatty(STDOUT_FILENO)) {
            params.pty = ttyname(STDOUT_FILENO);
        }

        // MAGIC variable to be used to find all children of this process
        char magic[20];
        snprintf(magic, 20, "NBMAGIC=%ld", (long) getppid());

        printf("PID=%d\n", getpid());
        printf("TTY=%s\n", params.pty == NULL ? "null" : params.pty);
        printf("PSID=%d\n", getsid(0));
        if (params.reportfile != NULL) {
            printf("REPORT=%s.%ld\n", params.reportfile, (long) getppid());
        }
        printf("%s\n", magic);
        printf("\n"); // empty line means that we printed everything we needed
        fflush(stdout);

        modify_term(STDIN_FILENO, params.noecho, params.set_erase_key);

        // Set passed environment variables
        for (int i = 0; i < params.envnum; i++) {
            putenv(params.envvars[i]);
        }

        // The last variable we set is NBMAGIC
        putenv(magic);

        if (params.waitSignal) {
            /*
             * Waiting for a SIGCONT, blocking (queuing) all other signals.
             * (but let SIGINT to pass...)
             */
            sigset_t sigset;
            sigfillset(&sigset);
            sigdelset(&sigset, SIGCONT);
            sigdelset(&sigset, SIGINT);
            sigsuspend(&sigset);
        } else {
            // Reset SIGCONT handler to default
            signal(SIGCONT, SIG_DFL);
        }

        // Just before doing execvp - notify the parent process so that
        // it can take a start time snapshot
        // Parent doesn't explicitly wait for this signal, but guarantees that
        // it is not blocked
        kill(getppid(), SIGCONT);


        if (params.wdir != NULL) {
            if (chdir(params.wdir) == -1) {
                err_sys2(errno == ENOENT ? 127 : 1, "failed to change directory: %s", params.wdir);
            }
        }

        // setvbuf is not retained across an exec, (as buffering is a property 
        // of a FILE, not fd - so need to do un-buffering in the execd process.
        // Use LD_PRELOAD for this..
        // This is controlled by java code for now, at least

        if (execvp(argv[0], argv) < 0) {
            err_sys2(errno == ENOENT ? 127 : 1, "failed to start %s", argv[0]);
        }

    }

    /* parent */

    /*
     * We will ignore SIGINT and SIGQUIT signals generated by a terminal to
     * have a chance to get user's program exit status...
     */
    signal(SIGINT, SIG_IGN);
    signal(SIGQUIT, SIG_IGN);

    if (params.envvars != NULL) {
        free(params.envvars);
    }

#if NEED_CHILD_INFO_BEFORE_REAP
    /*
     * We want to get a statistics of the child we just forked.
     * This statistics will be collected differently on different systems, 
     * but this should be done at the very end of the process execution - 
     * (just after it is done, but before reaping it's exit status).
     * The approach is to set a signal handler for SIGCHLD and wait for it.
     * SIGCHLD is send when child has finished it's execution (!!! actually, 
     * when it changes it's state - so it is send when it goes to 
     * foreground/background or when it is stoped/continued !!!)
     * Once signal is received - will read process's /proc and will get/reap it's 
     * status with waitpid.
     */

    struct sigaction sig_action;
    sig_action.sa_sigaction = sigchld;
    sigemptyset(&sig_action.sa_mask);
    sig_action.sa_flags = SA_SIGINFO | SA_RESETHAND | SA_NODEFER | SA_NOCLDSTOP;
    sigaction(SIGCHLD, &sig_action, NULL);
#endif // NEED_CHILD_INFO_BEFORE_REAP

    /*
     * If we have a newly allocated terminal - start a loop that re-directs 
     * process's I/O to a terminal.
     */
    if (allocated_pty_fd > 0) {
        // At least on Windows, when gdb is started through this pty process
        // and calling process is killed (i.e. stdin gets broken)
        // and even when we close master_fd, gdb continues to work... 
        // ??? will kill the process (gdb) in this case...
        int loop_result = loop(allocated_pty_fd); /* copies stdin -> ptym, ptym -> stdout */

        if (loop_result != 0) {
            int attempt = 2;
            while (attempt-- >= 0 && kill(pid, 0) == 0) {
                kill(pid, SIGTERM);
                sleep(1);
            }

            if (kill(pid, 0) == 0) {
                kill(pid, SIGKILL);
            }
        }
    }

#if NEED_CHILD_INFO_BEFORE_REAP

    /*
     * There is a race here !!
     * I bet it could be that got_sigchld is 0, but signal is already send
     * ?? What is we send SIGKILL to the child (the code above)? - seems that in 
     * this case signal is sent as well...
     * 
     * Will hung on a SIGCHLD if signal was not already received only
     */
    while (!got_sigchld) {
        sigset_t sigset;
        sigfillset(&sigset);
        sigdelset(&sigset, SIGCHLD);
        // Do not block SIGCONT as this is a way for child to notify the parent 
        // about user process starting moment
        sigdelset(&sigset, SIGCONT);
        sigsuspend(&sigset);
    }

    //prusage_t usage;
    //psinfo_t psinfo;

    if (params.reportfile != NULL) {
        //        int pfd;
        //        char fname[PATH_MAX];
        //        int r = getrusage(RUSAGE_CHILDREN, &rusage);
        //        printf("r==%d\n", r);

        //        snprintf(fname, PATH_MAX, "/proc/%ld/usage", (long) pid);
        //        pfd = open(fname, O_RDONLY);
        //        read(pfd, &usage, sizeof (prusage_t));
        //        close(pfd);
        //        snprintf(fname, PATH_MAX, "/proc/%ld/psinfo", (long)pid);
        //        pfd = open(fname, O_RDONLY);
        //        read(pfd, &psinfo, sizeof (psinfo_t));
        //        close(pfd);
    }
#endif //NEED_CHILD_INFO_BEFORE_REAP

    // Reap the process once it's /proc is read...
    int status, w;
    while ((w = wait(&status)) != pid);

#if !NEED_CHILD_INFO_BEFORE_REAP 
    end = times(&end_tms);
#endif 

    struct rusage rusage;
    struct rusage* prusage = &rusage;

    if (params.reportfile) {
        if (getrusage(RUSAGE_CHILDREN, prusage) == -1) {
            prusage = NULL;
        }
    } else {
        prusage = NULL;
    }

    if (allocated_pty_fd > 0) {
        tcdrain(allocated_pty_fd);
    }

    if (w != -1) {
        if (params.reportfile != NULL) {
            int sz = strlen(params.reportfile) + 10;
            char reportfile[sz];
            snprintf(reportfile, sz, "%s.%ld", params.reportfile, (long) getpid());
            do_report(reportfile, status, prusage /*&usage, &psinfo,*/);
        }
        if (WIFEXITED(status)) {
            exit(WEXITSTATUS(status));
        }
    }

    exit(EXIT_FAILURE);
}

void do_report(const char* fname, int status, struct rusage* rusage /*prusage_t* usage, *psinfo_t* psinfo,*/) {
    int fd = open(fname, O_TRUNC | O_CREAT | O_WRONLY, S_IRUSR | S_IWUSR);

    if (fd > 0) {
        char info[100];

        sprintf(info, "RC: %d\n", WIFEXITED(status) ? WEXITSTATUS(status) : 1);
        writen(fd, info, strlen(info));
        sprintf(info, "SIG: %s\n", WIFSIGNALED(status) ? strsignal(WTERMSIG(status)) : "-");
        writen(fd, info, strlen(info));
        sprintf(info, "CORE: %d\n", WCOREDUMP(status) ? 1 : 0);
        writen(fd, info, strlen(info));

        double tps = (double) sysconf(_SC_CLK_TCK);
        sprintf(info, "RTIME: %ld\n", start == 0 ? 0 : (long) ((1000 * (end - start)) / tps));
        writen(fd, info, strlen(info));
        if (end_tms.tms_cutime > 0) {
            sprintf(info, "UTIME: %ld\n", (long) ((1000 * (end_tms.tms_cutime - start_tms.tms_cutime)) / tps));
            writen(fd, info, strlen(info));
            sprintf(info, "STIME: %ld\n", (long) ((1000 * (end_tms.tms_cstime - start_tms.tms_cstime)) / tps));
            writen(fd, info, strlen(info));
        }

        if (rusage != NULL) {
            if (end_tms.tms_cutime == 0) {
                sprintf(info, "UTIME: %ld\n", (long) (rusage->ru_utime.tv_sec + rusage->ru_utime.tv_usec / 1000000.0));
                writen(fd, info, strlen(info));
                sprintf(info, "STIME: %ld\n", (long) (rusage->ru_stime.tv_sec + rusage->ru_stime.tv_usec / 1000000.0));
                writen(fd, info, strlen(info));
            }

            sprintf(info, "MAXRSS: %ld\n", rusage->ru_maxrss);
            writen(fd, info, strlen(info));
        }

        //        if (psinfo != NULL) {
        //            sprintf(info, "RSS_KB: %lu\n", psinfo == NULL ? 0 : psinfo->pr_rssize);
        //            writen(fd, info, strlen(info));
        //        }

        close(fd);
    } else {
        warn_sys("Failed to write status to %s", fname);
    }
}

/**
 * turn echo off (for slave pty)
 * set erase to ^H
 */
static void modify_term(int fd, int echooff, int fixerase) {
    if (echooff == 0 && fixerase == 0) {
        return;
    }

    struct termios stermios;

    if (tcgetattr(fd, &stermios) < 0) {
        err_sys("tcgetattr error");
    }

    if (echooff) {
        stermios.c_lflag &= ~(ECHO | ECHOE | ECHOK | ECHONL);

        /*
         * Also turn off NL to CR/NL mapping on output.
         */

        stermios.c_oflag &= ~(ONLCR);
    }

    if (fixerase) {
        stermios.c_cc[VERASE] = (cc_t) '\b';
    }

    if (tcsetattr(fd, TCSANOW, &stermios) < 0) {
        err_sys("tcsetattr error");
    }
}

static void sigcont(int sig) {
    start = times(&start_tms);
}

#if NEED_CHILD_INFO_BEFORE_REAP

static void sigchld(int sig, siginfo_t* info, void* uap) {
    end = times(&end_tms);
    got_sigchld = 1;
}
#endif //NEED_CHILD_INFO_BEFORE_REAP

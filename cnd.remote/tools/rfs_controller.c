/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <utime.h>

#include <memory.h>
#include <ctype.h>

#include <netinet/in.h>
#include <arpa/inet.h>
#include <alloca.h>
#include <sys/time.h>

#include "rfs_protocol.h"
#include "rfs_util.h"
#include "rfs_filedata.h"

static int emulate = false;

enum  {
    VERSION_1 = '3',
    VERSION_2 = '5'
} protocol_version = 0;

static struct timeval fs_skew;

typedef struct connection_data {
    int sd;
    struct sockaddr_in pin;
} connection_data;

static const char LC_PROTOCOL_REQUEST = 'r';
static const char LC_PROTOCOL_WRITTEN = 'w';
static const char LC_PROTOCOL_PING    = 'p';

static pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;

static bool mutex_lock(pthread_mutex_t* mutex) {
    if (pthread_mutex_lock(mutex) == 0) {
        return true;
    } else {
        report_error("Error locking mutex: %s\n", strerror(errno));
        return false;
    }
}

static bool mutex_unlock(pthread_mutex_t* mutex) {
    if (pthread_mutex_unlock(mutex) == 0) {
        return true;
    } else {
        report_error("Error unlocking mutex: %s\n", strerror(errno));
        return false;
    }
}

static void serve_connection_impl(connection_data *conn_data) {
    trace("New connection from  %s:%d sd=%d\n", inet_ntoa(conn_data->pin.sin_addr), ntohs(conn_data->pin.sin_port), conn_data->sd);

    const int maxsize = PATH_MAX + 32;
    char buffer[maxsize];
    struct package *pkg = (struct package *) &buffer;

    int first = true;
    char requestor_id[32] = "-1";

    while (1) {
        trace("Waiting for a data to arrive from %s, sd=%d...\n", requestor_id, conn_data->sd);
        errno = 0;
        enum sr_result recv_res = pkg_recv(conn_data->sd, pkg, maxsize);
        if (recv_res == sr_reset) {
            trace("Connection sd=%d reset by peer => normal termination\n", conn_data->sd);
            break;
        } else if (recv_res == sr_failure) {
            if (errno != 0) {
                perror("error getting message");
            }
            break;
        }

        trace("Request (%s): %s sd=%d\n", pkg_kind_to_string(pkg->kind), pkg->data, conn_data->sd);
        if (first ? (pkg->kind != pkg_handshake) : (pkg->kind != pkg_request && pkg->kind != pkg_written)) {
            fprintf(stderr, "protocol error: unexpected %s from %s sd=%d\n", pkg_kind_to_string(pkg->kind), requestor_id, conn_data->sd);
            break;
        }

        if (first) {
            first = false;
            strncpy(requestor_id, pkg->data, sizeof requestor_id);
            continue;
        }

        const char* filename = pkg->data;
        file_data *fd = find_file_data(filename);

        if (pkg->kind == pkg_written) {
            // NB 1: process that wrote a file does NOT wait any response
            // NB 2: MODIFIED status is final => no need to sync here
            if (fd == NULL) {
                trace("File %s is unknown - nothing to uncontrol\n", filename);
            } else if (fd->state == MODIFIED) {
                trace("File %s already reported as modified\n", filename);
            } else {
                fd->state = MODIFIED;
                trace("File %s sending uncontrol request to LC\n", filename);
                // TODO: this is a very primitive sync!
                mutex_lock(&mutex);
                fprintf(stdout, "%c %s\n", LC_PROTOCOL_WRITTEN, filename);
                fflush(stdout);
                mutex_unlock(&mutex);
            }
        } else { // pkg->kind == pkg_request
            char response[64];
            response[1] = 0;
            if (fd != NULL) {
                mutex_lock(&fd->mutex); //NB: never return unless unlocked !!!
                switch (fd->state) {
                    case TOUCHED:
                        trace("File %s state %c - requesting LC\n", filename, (char) fd->state);
                        /* TODO: this is a very primitive sync!  */
                        mutex_lock(&mutex);

                        fprintf(stdout, "%c %s\n", LC_PROTOCOL_REQUEST, filename);
                        fflush(stdout);

                        if (emulate) {
                            response[0] = response_ok;
                        } else
                        if (!fgets(response, sizeof response, stdin)) {
                            trace("Input stream closed. Exiting.\n");
                            break;
                        }
                        fd->state = (response[0] == response_ok) ? COPIED : ERROR;
                        mutex_unlock(&mutex);
                        trace("File %s state %c - got from LC %s, replying %s\n", filename, (char) fd->state, response, response);
                        break;
                    case COPIED:    // fall through
                    case UNCONTROLLED:
                    case MODIFIED:
                    case INEXISTENT:
                        response[0] = response_ok;
                        trace("File %s state %c - uncontrolled/modified/copied/inexistent, replying %s\n", filename, (char) fd->state,  response);
                        break;
                    case ERROR:
                        response[0] = response_failure;
                        trace("File %s state %c - old error, replying %s\n", filename, (char) fd->state, response);
                        break;
                    case INITIAL:   // fall through
                    case DIRECTORY: // fall through
                    case PENDING:   // fall through
                    default:
                        response[0] = response_failure;
                        trace("File %s state %c (0x%x)- unexpected state, replying %s\n", filename, (char) fd->state, (char) fd->state, response);
                        break;
                }
                mutex_unlock(&fd->mutex);
            } else {
                response[0] = response_ok;
                trace("File %s: state n/a, replying: %s\n", filename, response);
            }

            response[1] = 0;
            enum sr_result send_res = pkg_send(conn_data->sd, pkg_reply, response);
            if (send_res == sr_failure) {
                perror("send");
            } else if (send_res == sr_reset) {
                perror("send");
            } else { // success
                trace("reply for %s sent to %s sd=%d\n", filename, requestor_id, conn_data->sd);
            }
        }
    }
    close(conn_data->sd);
    trace("Connection to %s:%d (%s) closed sd=%d\n", inet_ntoa(conn_data->pin.sin_addr), ntohs(conn_data->pin.sin_port), requestor_id, conn_data->sd);
}

static void serve_connection(void* data) {
    serve_connection_impl((connection_data*) data);
    free(data);
}

static int _mkdir(const char *dir, int mask) {
    char tmp[1024];
    char *p = 0;
    size_t len;

    snprintf(tmp, sizeof (tmp), "%s", dir);
    len = strlen(tmp);
    if (tmp[len - 1] == '/') {
        tmp[len - 1] = 0;
    }
    for (p = tmp + 1; *p; p++) {
        if (*p == '/') {
            *p = 0;
            int rc = mkdir(tmp, mask);
            if (rc != 0) {
                // TODO: report errors
                trace("\t\terror creating dir %s: rc=%d\n", tmp, rc);
            }
            *p = '/';
        }
    }
    int rc = mkdir(tmp, mask);
    if (rc != 0) {
        // TODO: report errors
        trace("\t\terror creating dir %s: rc=%d\n", tmp, rc);
    }
    return rc;
}

static int create_dir(const char* path) {
    trace("\tcreating dir %s\n", path);
    int rc = _mkdir(path, 0700); // TODO: error processing
    if (rc != 0) {
        // TODO: report errors
        trace("\t\terror creating dir %s: rc=%d\n", path, rc);
    }
    return true; // TODO: check
}

static int create_lnk(const char* path, const char* lnk_src) {
    trace("\tcreating a symlink %s -> %s\n", path, lnk_src);
    int rc = symlink(lnk_src, path);
    if (rc != 0) {
        // TODO: report errors? then check existence first
        trace("\t\terror creating a symlink %s -> %s\n", path, lnk_src);
    }
    return true; // TODO: check
}

static int create_file(const char* path, int size, const struct timeval *file_time) {
    trace("\tcreating file %s %d\n", path, size);
    int fd = open(path, O_WRONLY | O_CREAT | O_TRUNC, 0700);
    if (fd > 0) { // // TODO: error processing
        if (size > 0) {
            lseek(fd, size-1, SEEK_SET);
            char space = '\n';
            int written = write(fd, &space, 1);
            if (written != 1) {
                report_error("Error writing %s: %d bytes written\n", path, written);
                return false;
            }
        }
        if (close(fd) != 0) {
            report_error("error closing %s (fd=%d)\n", path, fd);
            return false;
        }
        struct utimbuf tm;
        tm.actime = file_time->tv_sec;
        tm.modtime = file_time->tv_sec;
        if (utime(path, &tm) != 0) {
            report_error("Error setting modification time for %s: %s\n", path, strerror(errno));
        }

    } else {
        report_error("Error opening %s: %s\n", path, strerror(errno));
        return false;
    }
    return true;
}

static int touch_file(const char* path,  int size, const struct timeval *file_time) {
    if (create_file(path, size, file_time)) {
        return true;
    } else {
        report_error("can not create proxy file %s: %s\n", path, strerror(errno));
        return false;
    }
}

static int file_exists(const char* path, int size) {
    struct stat stat_buf;
    if (stat(path, &stat_buf) == -1) {
        if (errno != ENOENT) {
            report_error("Can't check file %s: %s\n", path, strerror(errno));
        }
        return false;
    } else {
        return true;
    }
}

static enum file_state char_to_state(char c) {
    switch (c) {
        case INITIAL:
        case TOUCHED:
        case COPIED:
        case ERROR:
        case UNCONTROLLED:
        case MODIFIED:
        case DIRECTORY:
        case LINK:
        case INEXISTENT:
            return c;
        default:
            return -1;
    }
}

/**
 * Converts a string that represents a non negative number with trailing space char
 * to long.
 * Checks whether the number is followed by the space char; if it is not, returns -1.
 * Stores the pointer to the character AFTER this space into *next_char
 */
static long string2num(const char *buffer, const char *buf_limit, const char** next_char) {
    *next_char = buffer;
    while (*next_char < buf_limit && isdigit(**next_char)) {
        (*next_char)++;
    }
    // we should necessarily stay on ' '
    if (**next_char == ' ') {
        (*next_char)++; // skip space after the number
    } else {
        return -1;
    }
    return atol(buffer);
}


static bool scan_line(const char* buffer, int bufsize, enum file_state *state, int *file_size,
                    struct timeval *file_time, const char **path) {
    *state = char_to_state(*buffer);
    if (*state == -1) {
        return false;
    }

    file_time->tv_sec = 0;
    file_time->tv_usec = 0;

    if (*state == DIRECTORY || *state == LINK) { // directory
        // format is as in printf("D %s", path)
        *path = buffer + 2;
        *file_size = 0;
        return true;
    } else {
        // VERSION_1: format is as in printf("%c %d %s", kind, length, path)
        const char* filename;
        *file_size = string2num(buffer+2, buffer+bufsize, &filename);
        if (*file_size < 0) {
            return false;
        }
        // VERSION_2: format is as in printf("%c %d %d %d %s", kind, length, seconds, milliseconds, path)
        // so the beginning is the same, and two long values are inserted before path
        if (protocol_version == VERSION_2) {
            file_time->tv_sec = string2num(filename, buffer+bufsize, &filename);
            if (file_time->tv_sec < 0) {
                return false;
            }
            file_time->tv_usec = string2num(filename, buffer+bufsize, &filename);
            if (file_time->tv_sec < 0) {
                return false;
            }
        }
        *path = filename;
        return true;
    }
}

typedef struct file_elem {
    struct file_elem* next;
    enum file_state state;
    char* real_path;
    char filename[]; // have to be the last field
} file_elem;

/**
 * adds info about new file to the tail of the list
 */
static file_elem* add_file_to_list(file_elem* tail, const char* filename, enum file_state state, const char* real_path) {
    int namelen = strlen(filename);
    int realpath_len = strlen(real_path);
    int size = sizeof(file_elem) + namelen + realpath_len + 2;
    file_elem *fe = (file_elem*) malloc_wrapper(size);
    fe->next = NULL;
    strcpy(fe->filename, filename);
    fe->state = state;
    fe->real_path = fe->filename + namelen + 1;
    strcpy(fe->real_path, real_path);
    if (tail != NULL) {
        tail->next = fe;
    }
    trace("\t\tadd_file_to_list %c %s -> %s\n", fe->state, fe->filename, fe->real_path);
    return fe;
}

static void free_file_list(file_elem* list) {
    while (list != NULL) {
        file_elem* next = list->next;
        free(list);
        list = next;
    }
}

static void calc_fs_skew(struct timeval *skew) {

    skew->tv_sec = 0;
    skew->tv_usec = 0;
    
    char path[PATH_MAX+1];
    getcwd(path, sizeof path);
    strncat(path, "/tmpXXXXXX", sizeof path);

    int fd;

    fd = mkstemp(path);

    struct timeval curr_time;
    gettimeofday(&curr_time, 0);

    if (fd < 0) {
        perror("rfs_controller: mktemp failed: ");
        return;
    }
    struct stat st;
    if (stat(path, &st) != 0) {
        perror("rfs_controller: stat failed: ");
        return;
    } 
    
    unlink(path);
    
    skew->tv_sec = st.st_mtim.tv_sec - curr_time.tv_sec;
    skew->tv_usec = st.st_mtim.tv_nsec/1000000 - curr_time.tv_usec/1000;
}

static int calc_time_skew() {
    int bufsize = 256;
    char buffer[bufsize];
    if(fgets(buffer, bufsize, stdin)) {
        if (strncmp("SKEW_COUNT=", buffer, 11) != 0) {
            report_error("protocol error: expected SKEW_COUNT=<number>: %s\n", buffer);
            return false;
        }
        int cnt;
        sscanf(buffer + 11, "%d", &cnt);
        if (cnt < 0) {
            report_error("protocol error: unexpected skew pass count: %d\n", cnt);
            return false;
        }
        trace("Going to get skew. cnt=%d\n", cnt);
        int i;        
        for (i = 0; i < cnt; i++) {
            if(fgets(buffer, bufsize, stdin)) {
                if (strncmp("SKEW ", buffer, 5) != 0) {
                    report_error("protocol error: expected SKEW :%s\n", buffer);
                    return false;
                }
                struct timeval tm;
                gettimeofday(&tm, 0);
                long millis = tm.tv_sec * 1000 + tm.tv_usec / 1000;
                fprintf(stdout, "%ld\n", millis);
                fflush(stdout);
            } else {
                report_error("protocol error on pass %d during skew processing: unexpected EOF\n", i);
                return false;
            }
        }
        if(fgets(buffer, bufsize, stdin)) {
            if (strncmp("SKEW_END", buffer, 8) != 0) {
                report_error("protocol error: expected SKEW_END :%s\n", buffer);
                return false;
            }
        }

        trace("Calculating FS skew\n");
        calc_fs_skew(&fs_skew);
        trace("FS skew is %ld s %ld mks\n", fs_skew.tv_sec, fs_skew.tv_usec);
        
        fprintf(stdout, "FS_SKEW %ld\n", fs_skew.tv_sec*1000 + fs_skew.tv_usec/1000);
        fflush(stdout);

        return true;
    } else {
        report_error("protocol error during skew processing: unexpected EOF\n");
        return false;
    }
}

static int init() {
    trace("Initialization. Sending supported versions: %c %c\n", VERSION_1, VERSION_2);
    fprintf(stdout, "CONTROLLER VERSION 1.2.61  (%s %s)\n", __DATE__, __TIME__);
    fprintf(stdout, "VERSIONS %c %c\n", VERSION_1, VERSION_2);
    fflush(stdout);
    int bufsize = 256;
    char buffer[bufsize];
    if(fgets(buffer, bufsize, stdin)) {
        if ( strncmp("VERSION=", buffer, 8) != 0 ) {
            report_error("protocol error: first line shoud start with VERSION= but was: %s\n", buffer);
            return false;
        }
        protocol_version = buffer[8];
        if (protocol_version != VERSION_1 && protocol_version != VERSION_2) {
            report_error("protocol error: unexpected version: %s\n", buffer);
            return false;
        }
    } else {
        report_error("protocol error during initialization: unexpected EOF\n");
        return false;
    }
    return true;
}

/**
 * Reads the list of files from the host IDE runs on,
 * creates files, fills internal file table
 */
static int init_files() {
    trace("Files list initialization\n");
    int bufsize = PATH_MAX + 32;
    char buffer[bufsize];
    int success = false;
    file_elem* list = NULL;
    file_elem* tail = NULL;
    start_adding_file_data();
    while (1) {
        if( !fgets(buffer, bufsize, stdin)) {
            report_error("protocol error while reading file info: unexpected EOF\n");
            return false;
        }
        if (buffer[0] == '\n') {
            success = true;
            break;
        }
        trace("\tFile init: %s", buffer); // no trailing LF since it's in the buffer
        // remove trailing LF
        char* lf = strchr(buffer, '\n');
        if (lf) {
            *lf = 0;
        }
        if (strchr(buffer, '\r')) {
            report_error("protocol error: unexpected CR: %s\n", buffer);
            return false;
        }

        enum file_state state;
        int file_size;
        char *path;
        struct timeval file_time;
        file_time.tv_sec = file_time.tv_usec = 0;

        if (!scan_line(buffer, bufsize, &state, &file_size, &file_time, (const char**) &path)) {
            report_error("protocol error: %s\n", buffer);
            break;
        }
        trace("\t\tpath=%s size=%d state=%c (0x%x) time=%d.%d\n", path, file_size, (char) state, (char) state, file_time.tv_sec, file_time.tv_usec);

        if (state == -1) {
            report_error("protocol error: %s\n", buffer);
            break;
        } else if (state == DIRECTORY) { // directory
            create_dir(path);
        } else if (state == LINK) { // symbolic link
            char lnk_src[bufsize]; // it is followed by a line that contains the link source
            if( !fgets(lnk_src, sizeof lnk_src, stdin)) {
                report_error("protocol error while reading link info: unexpected EOF\n");
                return false;
            }
            char* lf = strchr(lnk_src, '\n');
            if (lf) {
                *lf = 0;
            }
            if (strchr(buffer, '\r')) {
                report_error("protocol error: unexpected CR: %s\n", buffer);
                return false;
            }
            create_lnk(path, lnk_src);
        } else { // plain file
            int touch = false;
            if (state == INITIAL) {
                touch = true;
            } else if (state == COPIED || state == TOUCHED) {
                touch = !file_exists(path, file_size);
            } else if (state == UNCONTROLLED || state == INEXISTENT) {
                // nothing
            } else {
                report_error("protocol error: %s\n", buffer);
            }

            enum file_state new_state = state;
            if (touch) {
                if (touch_file(path, file_size, &file_time)) {
                    new_state = TOUCHED;
                } else {
                    new_state = ERROR;
                }
            }

            if (*path == '/') {
                char real_path [PATH_MAX + 1];
                if (state == UNCONTROLLED || state == INEXISTENT) {
                    char *dir = path;
                    char *file = path;
                    // find trailing zero
                    while (*file) {
                        file++;
                    }
                    // we'll find '/' for sure - at least the starting one
                    while(*file != '/') {
                        file--;
                    }
                    if (file == path) { // the file resides in root directory
                        strcpy(real_path, path);
                    } else {
                        // NB: we modify the path! but we'll restore later
                        char *pfile_start = file; // save file start char
                        char file_start = *file;  // save file start char
                        *file = 0; // replace the '/' that separates file from dir by zero
                        file++;
                        if (!realpath(dir, real_path)) {
                            report_unresolved_path(dir);
                            break;
                        }
                        char *p = real_path;
                        while (*p) {
                            p++;
                        }
                        *(p++) = '/';
                        strncpy(p, file, sizeof real_path - (p - real_path));
                        *pfile_start = file_start; // restore file start char
                    }
                } else {
                    if (!realpath(path, real_path)) {
                       report_unresolved_path(path);
                       break;
                    }
                }
                trace("\t\tadding %s with state '%c' (0x%x) -> %s\n", path, (char) new_state, (char) new_state, real_path);
                add_file_data(real_path, new_state);
                // send real path to local controller
                tail = add_file_to_list(tail, path, new_state, real_path);
                //trace("\t\tadded to list %s with state '%c' (0x%x) -> %s\n", tail->filename, tail->state, tail->state, tail->real_path);
                if (list == NULL) {
                    list = tail;
                }
            } else {
                report_error("protocol error: %s is not absoulte\n", path);
                break;
            }
        }
    }
    stop_adding_file_data();
    trace("Files list initialization done\n");
    if (success) {
        // send info about touched files which were passed as copied files
        tail = list;
        while (tail != NULL) {
            fprintf(stdout, "*%c%s\n%s\n", tail->state, tail->filename, tail->real_path);
            fflush(stdout);
            tail = tail->next;
        }
        free_file_list(list);
        // empty line as indication of finished files list
        fprintf(stdout, "\n");
        fflush(stdout);
    }
    return success;
}

static const char* exit_flag_file = NULL;

/**
 * From time to time prints to stdout.
 * This guarantees that, as soon as as ssh connection breaks, program will get SIGPIPE and terminate
 */
static void check_stdout_pipe(void* data) {
    do {
        mutex_lock(&mutex);
        fprintf(stdout, "%c\n", LC_PROTOCOL_PING);
        fflush(stdout);
        // no response needed
        // char response[64];
        // fgets(response, sizeof response, stdin);
        mutex_unlock(&mutex);
        sleep(20);
        if (exit_flag_file) {
            struct stat stat_buf;
            if (lstat(exit_flag_file, &stat_buf) == 0) {
                exit(0);
            }
        }

    } while (1);
}

int main(int argc, char* argv[]) {
    exit_flag_file = getenv("RFS_CONTROLLER_EXIT_FLAG_FILE");
    init_trace_flag("RFS_CONTROLLER_TRACE");
    trace_startup("RFS_C", "RFS_CONTROLLER_LOG", argv[0]);
    int port = default_controller_port;
    if (argc > 1) {
        port = atoi(argv[1]);
    }
    // auto mode for test purposes
    if (argc > 2 && strcmp(argv[2], "emulate") == 0) {
        emulate = true;
    }
    int sd = socket(AF_INET, SOCK_STREAM, 0);
    if (sd == -1) {
        perror("Socket");
        exit(1);
    }

    struct sockaddr_in sin;
    memset(&sin, 0, sizeof (sin));
    sin.sin_family = AF_INET;
    sin.sin_addr.s_addr = INADDR_ANY;
    sin.sin_port = htons(port);

    if (bind(sd, (struct sockaddr *) & sin, sizeof (sin)) == -1) {
        if (errno != EADDRINUSE) {
            perror("Error opening port: ");
            exit(2);
        }
        trace("Searching for available port...\n", port);
        int bind_rc;
        do {
            sin.sin_port = htons(++port);
            trace("\t%d...\n", port);
            bind_rc = bind(sd, (struct sockaddr *) &sin, sizeof (sin));
        } while (bind_rc == -1 && port < 99999);
        if (bind_rc == -1) {
            perror("port");
            exit(4);
        }
    };

    /* show that we are willing to listen */
    if (listen(sd, 5) == -1) {
        perror("listen");
        exit(1);
    }

    if (!init()) {
        report_error("Initialization error\n");
        exit(8);
    }
    if (!calc_time_skew()) {
        report_error("Initialization error\n");
        exit(8);
    }
    if (!init_files()) {
        report_error("Error when initializing files\n");
        exit(8);
    }

    // print port later, when we're done with initializing files
    fprintf(stdout, "PORT %d\n", port);
    fflush(stdout);

    pthread_t ping_pong_thread;
    pthread_create(&ping_pong_thread, NULL /*&attr*/, (void *(*) (void *)) check_stdout_pipe, NULL);
    pthread_detach(ping_pong_thread);

    while (1) {
        /* wait for a client to talk to us */
        connection_data* conn_data = (connection_data*) malloc_wrapper(sizeof (connection_data));
        socklen_t addrlen = sizeof (conn_data->pin);
        if ((conn_data->sd = accept(sd, (struct sockaddr *) & conn_data->pin, &addrlen)) == -1) {
            perror("accept");
            exit(1);
        }
        pthread_t thread;
        pthread_create(&thread, NULL /*&attr*/, (void *(*) (void *)) serve_connection, conn_data);
        pthread_detach(thread);
    }
    // the code below is unreachable, so I commented it out
    // TODO: (?) more accurate shutdon?
    // close(sd);
    // trace_shutdown();
}

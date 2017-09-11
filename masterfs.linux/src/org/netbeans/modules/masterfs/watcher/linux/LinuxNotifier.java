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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.masterfs.watcher.linux;

import org.netbeans.modules.masterfs.providers.Notifier;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * A {@link Notifier} implementation based on Linux inotify mechanism.
 *
 * @author nenik
 */
@ServiceProvider(service=Notifier.class, position=500)
public final class LinuxNotifier extends Notifier<LinuxNotifier.LKey> {
    private static final Logger LOG = Logger.getLogger(LinuxNotifier.class.getName());

    private static interface InotifyImpl extends Library {
	public int inotify_init();
	public int inotify_init1(int flags);
	public int close(int fd);
    public int read(int fd, ByteBuffer buff, int count);
	public int inotify_add_watch(int fd, String pathname, int mask);
	public int inotify_rm_watch(int fd, int wd);

        public static final int O_CLOEXEC = 02000000; //0x80000

        // Masks
        public static final int IN_ACCESS = 0x0001;      /* File was accessed */
        public static final int IN_MODIFY = 0x0002;      /* File was modified */
        public static final int IN_ATTRIB = 0x0004;      /* Metadata changed */
        public static final int IN_CLOSE_WRITE =   0x0008;      /* Writtable file was closed */
        public static final int IN_CLOSE_NOWRITE = 0x0010;      /* Unwrittable file closed */
        public static final int IN_OPEN =          0x0020;      /* File was opened */
        public static final int IN_MOVED_FROM =   0x0040;      /* File was moved from X */
        public static final int IN_MOVED_TO =     0x0080;      /* File was moved to Y */
        public static final int IN_CREATE =        0x0100;      /* Subfile was created */
        public static final int IN_DELETE =        0x0200;      /* Subfile was deleted */
        public static final int IN_DELETE_SELF =    0x0400;      /* Self was deleted */
        public static final int IN_MOVE_SELF =     0x0800;      /* Self was moved */

        // additional event masks
        public static final int IN_UNMOUNT =      0x2000;      /* Backing fs was unmounted */
        public static final int IN_Q_OVERFLOW =    0x4000;      /* Event queued overflowed */
        public static final int IN_IGNORED =       0x8000;      /* File was ignored */

    }

    final InotifyImpl IMPL;
    int fd;
    private ByteBuffer buff = ByteBuffer.allocateDirect(4096);

    // An array would serve nearly as well
    private Map<Integer, LKey> map = new HashMap<Integer, LKey>();

    public LinuxNotifier() {
        IMPL = (InotifyImpl) Native.loadLibrary("c", InotifyImpl.class);
    }

    private String getString(int maxLen) {
        if (maxLen < 1) return null; // no name field
        int stop = maxLen - 1;
        byte[] temp = new byte[maxLen];
        buff.get(temp);
        while (temp[stop] == 0) stop--;
        return new String(temp, 0, stop+1);
    }

    @Override public String nextEvent() throws IOException {
        String path;
        while (true) {
            path = nextEventPath();
            if (path == null || !path.isEmpty()) {
                return path;
            }
        }
    }

    /**
     * @return Path for the next event, null in case of queue overflow, or empty
     * string if the event should be ignored.
     */
    public String nextEventPath() throws IOException {
        /* inotify event structure layout:
         *   int      wd;    // Watch descriptor
         *   uint32_t mask;  // Mask of events
         *   uint32_t cookie;// Unique cookie associating related events (for rename(2))
         *   uint32_t len;   // Size of name field
         *   char     name[];// Optional null-terminated name
         */
        while (buff.remaining() < 16 || buff.remaining() < 16 + buff.getInt(buff.position() + 12)) {
            buff.compact();
            int len = IMPL.read(fd, buff, buff.remaining());

            if (len <= 0) {
                // lazily get a thread local errno
                int errno = NativeLibrary.getInstance("c").getFunction("errno").getInt(0);
                if (errno == 4) { // EINTR
                    buff.flip();
                    continue; // restart the I/O 
                } else {
                    throw new IOException("error reading from inotify: " + errno);
                }
            }
            buff.position(buff.position() + len);
            buff.flip();
        }

        // now we have enough data in the buffer
        int wd = buff.getInt();
        int mask = buff.getInt();
        int cookie = buff.getInt();
        int len = buff.getInt();
        String name = getString(len); // ignore

        if ((mask & InotifyImpl.IN_IGNORED) == InotifyImpl.IN_IGNORED) {
            return ""; // #235632                                       //NOI18N
        }
        LKey key = map.get(wd);
        if (key == null) { /* wd == -1 -> Queue overflow */
            return null;
        }
        
        return key.path;
    }


    static class LKey {
        int id;
        String path;

        public LKey(int id, String path) {
            this.id = id;
            this.path = path;
        }

        @Override
        public String toString() {
            return "LKey[" + id + " - '" + path + "']";
        }
    }

    @Override
    protected void start() throws IOException {
        buff.position(buff.capacity()); // make the buffer empty
        buff.order(ByteOrder.nativeOrder());
        fd = IMPL.inotify_init1(InotifyImpl.O_CLOEXEC);
        if (fd < 0) {
            LOG.log(
                    Level.INFO, "Linux kernel {0} returned {1} from inotify_init1",
                    new Object[]{System.getProperty("os.version"), fd});
            fd = IMPL.inotify_init();
            LOG.log(Level.INFO, "Trying inotify_init: {0}", fd);
        }
        if (fd < 0) {
            throw new IOException("inotify_init failed: " + fd);
        }
    }

    @Override public LKey addWatch(String path) throws IOException {
        // what if the file doesn't exist?
        int id = IMPL.inotify_add_watch(fd, path,
                    InotifyImpl.IN_CREATE | InotifyImpl.IN_MOVED_TO |
                    InotifyImpl.IN_DELETE | InotifyImpl.IN_MOVED_FROM |
                    InotifyImpl.IN_MODIFY | InotifyImpl.IN_ATTRIB);
        //XXX handle error return value (-1)
        LOG.log(Level.FINEST, "addWatch{0} res: {1}", new Object[]{path, id});
        if (id <= 0) {
            // 28 == EINOSPC
            int errno = NativeLibrary.getInstance("c").getFunction("errno").getInt(0); // NOI18N
            throw new IOException("addWatch on " + path + " errno: " + errno); // NOI18N
        }

        LKey newKey = map.get(id);
        if (newKey == null) {
            newKey = new LKey(id, path);
            map.put(id, newKey);
        }
        return newKey;
    }

    @Override public void removeWatch(LKey lkey) {
        map.remove(lkey.id);
        IMPL.inotify_rm_watch(fd, lkey.id);
    }
}

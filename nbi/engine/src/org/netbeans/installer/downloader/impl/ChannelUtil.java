/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.downloader.impl;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.helper.MutualHashMap;
import org.netbeans.installer.utils.helper.MutualMap;

/**
 * @author Danila_Dugurov
 */
public class ChannelUtil {
  
  /////////////////////////////////////////////////////////////////////////////////
  // Constants
  public static final int BUFFER_SIZE = 64 * 1024;
  
  /////////////////////////////////////////////////////////////////////////////////
  // Static
  private static final Map<FileChannel, Integer> channel2ClientsCount = new HashMap<FileChannel, Integer>();
  private static final MutualMap<File, FileChannel> file2Channel = new MutualHashMap<File, FileChannel>();
  
  //so synchronization on file save me from closing channel when
  //concurrently another thread try to get fragment.
  //No synchronization in methods signiture becouse
  //I don't want to block threads wich deal with another resourses(files)
  
  public static OutputStream channelFragmentAsStream(final File file, final SectionImpl pumpSection) throws FileNotFoundException {
    if (file == null || pumpSection == null) throw new IllegalArgumentException();
    synchronized (file) {
      if (!file.exists()) throw new FileNotFoundException();
      FileChannel channel;
      if (!file2Channel.containsKey(file)) {
        channel = new RandomAccessFile(file, "rw").getChannel();
        file2Channel.put(file, channel);
        channel2ClientsCount.put(channel, 1);
      } else {
        channel = file2Channel.get(file);
        int count = channel2ClientsCount.get(channel);
        channel2ClientsCount.put(channel, ++count);
      }
    }
    return new OutputStream() {
      final FileChannel channel = file2Channel.get(file);
      final SectionImpl section = pumpSection;
      long position = pumpSection.offset();
      long barier = section.length() > 0 ? section.offset() + section.length() : Long.MAX_VALUE;
      
      ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
      
      public synchronized void write(int b) throws IOException {
        if (position + buffer.position() >= barier) return;
        if (buffer.remaining() == 0) flush();
        buffer.put((byte) b);
      }
      
      public synchronized void write(byte[] b, int off, int len) throws IOException {
        if (b == null) throw new NullPointerException();
        if (off < 0 || off > b.length || (off + len > b.length)) throw new IndexOutOfBoundsException();
        while (len > 0) {
          int length = len <= buffer.remaining() ? len: buffer.remaining();
          final long remaining = barier - position - buffer.position();
          if (remaining == 0) break;
          length = length <= remaining  ? length: (int)remaining;
          buffer.put(b, off, length);
          if (buffer.remaining() == 0) flush();
          len -= length;
          off += length;
        }
      }
      
      // close may be invoked asynchroniously so synchronized modifer really need
      public synchronized void flush() throws IOException {
        final int written = this.channel.write((ByteBuffer) buffer.flip(), position);
        position += written;
        if (written > 0) section.shiftOffset(written);
        buffer.rewind();
      }
      
      //on close() thread release channel in any case of exceptions
      public void close() throws IOException {
        try {
          if (!channel.isOpen()) return;
          flush();
        } finally {
          releaseFile(channel);
        }
      }
    };
  }
  
  private static void releaseFile(final FileChannel channel) {
    final File file = file2Channel.reversedGet(channel);
    if (file == null) return;
    synchronized (file) {
      Integer count = channel2ClientsCount.get(channel);
      if (count == null) return;//already removed
      if (count > 1) {
        channel2ClientsCount.put(channel, --count);
      } else {
        channel2ClientsCount.remove(channel);
        file2Channel.reversedRemove(channel);
        try {
          channel.close();
        } catch (IOException ex) {
          LogManager.log("can't close channel", ex);
        }
      }
    }
  }
}

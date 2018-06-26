/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.netserver.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.netbeans.modules.netserver.SocketFramework;


/**
 * @author ads
 *
 */
abstract class AbstractWSHandler75<T extends SocketFramework> extends AbstractWSHandler<T> 
    implements WebSocketChanelHandler 
{
    AbstractWSHandler75( T framework ){
        super(framework);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.WebSocketChanelHandler#read(java.nio.ByteBuffer)
     */
    @Override
    public void read( ByteBuffer byteBuffer ) throws IOException {
        SocketChannel socketChannel = (SocketChannel) getKey().channel();
        byte[] bytes = new byte[Utils.BYTES];
        List<List<Byte>> messages = new LinkedList<List<Byte>>();
        List<Byte> message = new LinkedList<Byte>();
        boolean newMessage = false;
        while (!isStopped()) {
            byteBuffer.clear();
            if (socketChannel.read(byteBuffer) == -1) {
                close();
                return;
            }
            byteBuffer.flip();
            byteBuffer.get(bytes, 0, byteBuffer.limit() );
            int start =0;
            if (bytes[0] == 0 && !newMessage) {
                start =1;
                newMessage = true;
                if (!message.isEmpty()) {
                    messages.add(new ArrayList<Byte>(message));
                }
                message.clear();
            }
            for (int i = start; i < byteBuffer.limit(); i++) {
                if (bytes[i] == (byte) 255) {
                    messages.add(new ArrayList<Byte>(message));
                    message.clear();
                    newMessage = false;
                }
                else {
                    message.add(bytes[i]);
                }
            }
            if (message.isEmpty()) {
                break;
            }
        }
        if ( isStopped() ){
            close();
            return ;
        }
        for (List<Byte> list : messages) {
            bytes = new byte[list.size()];
            int i = 0;
            for (Byte byt : list) {
                bytes[i] = byt;
                i++;
            }
            readDelegate(bytes);
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.WebSocketChanelHandler#createTextFrame(java.lang.String)
     */
    @Override
    public byte[] createTextFrame( String message ) {
        byte[] data = message.getBytes( Charset.forName( Utils.UTF_8));
        byte[] result = new byte[ data.length +2 ];
        result[0] = 0;
        result[ data.length +1 ]=(byte)255;
        System.arraycopy(data, 0, result, 1, data.length);
        return result;
    }
    
    protected byte[] readRequestContent(  int size ) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        SocketChannel socketChannel = (SocketChannel) getKey().channel();
        try {
            while ( buffer.hasRemaining() && !isStopped()){
                if ( socketChannel.read( buffer ) == -1){
                    close();
                }
            }
            if ( isStopped() ){
                close();
                return null;
            }
            byte[] bytes = new byte[buffer.capacity()];
            buffer.flip();
            buffer.get( bytes );
            return bytes;
        }
        catch( IOException e ){
            close();
        }
        return null;
    }
    
    protected abstract void readDelegate( byte[] bytes );
    
}

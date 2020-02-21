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

package org.netbeans.modules.cnd.debugger.common2.utils;

import java.io.InputStream;
import java.io.IOException;

public class NotifyingInputSteam extends InputStream {

    private final InputStream delegate;

    private Listener listener;
    private boolean armed;

    public static interface Listener {
	public void activity ();
    }

    public synchronized void setListener (Listener l) {
	listener = l;
    }

    public synchronized void arm() {
	armed = true;
    }

    public void fireListener() {
	Listener l = null;

	synchronized(this) {
	    if (armed) {
		l = listener;
		armed = false;
	    }
	}
	if (l != null)
	    l.activity();
    }

    public NotifyingInputSteam(InputStream delegate) {
	this.delegate = delegate;
    }

    @Override
    public int read() throws IOException {
	int c;
	c = delegate.read();
	fireListener();
	return c;
    }

    @Override
    public int 	available() throws IOException {
	return delegate.available();
    }

    @Override
    public  void close() throws IOException  {
	delegate.close();
    }

    @Override
    public void mark(int readlimit) {
	delegate.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
	return delegate.markSupported();
    }

    @Override
    public int 	read(byte[] b) throws IOException {
	int c;
	c = delegate.read(b);
	fireListener();
	return c;
    }

    @Override
    public int 	read(byte[] b, int off, int len) throws IOException {
	int c;
	c = delegate.read(b, off, len);
	fireListener();
	return c;
    }

    @Override
    public void	reset() throws IOException {
	delegate.reset();
    }

    @Override
    public long skip(long n)  throws IOException {
	return delegate.skip(n);
    }

}



Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

* dlight.nativeexecution tests

dlight.nativeexecution module includes support for remote connection testing.

In order to test remote connections you have to set up two files, used in NativeExecutionTestSupport.java:

** cndtestrc file

This file specifies the remote platforms available. See test/unit/data/cndtestrc for an example.

Can be set using the system property

-Dcnd.remote.rcfile=test/unit/data/cndtestrc

If not set then $HOME/.cndtestrc is used.

** testuserinfo

This file specifies the user/password/host used in each platform. See test/unit/data/testuserinfo for an example.

Can be set using the system property

-Dcnd.remote.testuserinfo=test/unit/data/testuserinfo

Or the CND_REMOTE_TESTUSERINFO environment variable.

If not set then $HOME/.testuserinfo is used.

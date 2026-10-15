Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements; and to You under the Apache License, Version 2.0.

Updating the Typescript Language Server
=======================================

1. Update the versions in `bundles/package/package.json`
2. Run the `prepare` programm according to the documentation in
   `bundles/prepare/README`
3. Clean the `external` folder from all binaries and license files and the
   `binaries-list` file
4. Check the generated license files in `bundles/bundles` and copy the contents
   of that folder to the `external` folder
5. Rebuild the `typescript.editor` module and test it
6. Upload the files to netbeans.osuosl.org

For review it is advised to make use of the option to use arbitrary URLs in the
`binary-list` file. For example, if the list looks like this:

```
713D7D3E9651707669DE7452B0262D85DDC8344F typescript-5.5.4.zip
6895F0456E4B0FE3D857AA7D3F399932A026D060 typescript-language-server-4.3.3.zip
```

For review the two files `typescript-language-server-4.3.3.zip` and
`typescript-5.5.4.zip` can be placed on `my-webspace.org`. The `binary-list`
would then read:

```
713D7D3E9651707669DE7452B0262D85DDC8344F https://my-webspace.org/typescript-5.5.4.zip typescript-5.5.4.zip
6895F0456E4B0FE3D857AA7D3F399932A026D060 https://my-webspace.org/typescript-language-server-4.3.3.zip typescript-language-server-4.3.3.zip
```

Step 6 can then be executed once review is done.
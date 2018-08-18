package org.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class HelloWorldAgent implements ClassFileTransformer {
    public static void agentmain(String args, Instrumentation inst) {
        inst.getClass(); // null check
        inst.addTransformer(new HelloWorldAgent());
    }

    @Override
    public byte[] transform(
        ClassLoader loader, String className, Class<?> classBeingRedefined,
        ProtectionDomain protectionDomain, byte[] arr
    ) throws IllegalClassFormatException {
        byte[] ret = arr;
        for (int i = 0; i < arr.length - 4; i++) {
            if (arr[i] == 'H' && arr[i + 1] == 'e' && arr[i + 2] == 'l' && 
                arr[i + 3] == 'o'
            ) {
                ret = ret.clone();
                ret[i] = 'A';
                ret[i + 1] = 'h';
                ret[i + 2] = 'o';
                ret[i + 3] = 'j';
            }
        }
        return ret;
    }
}
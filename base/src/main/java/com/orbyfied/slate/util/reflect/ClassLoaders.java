package com.orbyfied.slate.util.reflect;

import lombok.SneakyThrows;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;

public class ClassLoaders {

    static final VarHandle ClassLoader_parent;
    static final MethodHandle ClassLoader_findLoadedClass;
    static final MethodHandle ClassLoader_findLoadedClass_SCL;

    static {
        try {
            ClassLoader_parent = UnsafeUtil.getInternalLookup()
                    .findVarHandle(ClassLoader.class, "parent", ClassLoader.class);
            ClassLoader_findLoadedClass = UnsafeUtil.getInternalLookup()
                    .findVirtual(ClassLoader.class, "findLoadedClass", MethodType.methodType(Class.class, String.class));
            ClassLoader_findLoadedClass_SCL = ClassLoader_findLoadedClass.bindTo(ClassLoader.getSystemClassLoader());
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    @SneakyThrows
    public static Class<?> findLoadedClass(ClassLoader cl, String name) {
        return (Class<?>) ClassLoader_findLoadedClass.invoke(cl, name);
    }

    @SneakyThrows
    public static ClassLoader getParent(ClassLoader cl) {
        return (ClassLoader) ClassLoader_parent.get(cl);
    }

    @SneakyThrows
    public static Class<?> findLoadedClassTopLevel(String name) {
        return (Class<?>) ClassLoader_findLoadedClass_SCL.invokeWithArguments(name);
    }

}

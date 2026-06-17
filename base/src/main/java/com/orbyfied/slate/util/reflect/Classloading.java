package com.orbyfied.slate.util.reflect;

import com.orbyfied.slate.util.exception.Throwables;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities related to class loading.
 */
public class Classloading {

    private static Class<?> C_URLClassPath;
    private static MethodHandle M_URLClassPath_addURL;
    private static MethodHandle GET_URLClassLoader_ucp;
    private static final Map<Class<?>, MethodHandle> ucpGetterCache = new HashMap<>();
    private static MethodHandle M_URLClassLoader_addURL;

    static {
        try {
            C_URLClassPath = Class.forName("jdk.internal.loader.URLClassPath");
            M_URLClassPath_addURL = UnsafeUtil.getInternalLookup()
                    .findVirtual(C_URLClassPath, "addURL", MethodType.methodType(void.class, URL.class));

            GET_URLClassLoader_ucp = UnsafeUtil.getInternalLookup()
                    .findGetter(URLClassLoader.class, "ucp", C_URLClassPath);
        } catch (ClassNotFoundException e) {
            try {
                // URLClassPath doesnt exist, all class loaders
                // are instances of URLClassLoader so use legacy
                // reflection tactics to add URLs
                M_URLClassLoader_addURL = UnsafeUtil.getInternalLookup()
                        .findVirtual(URLClassLoader.class, "addURL", MethodType.methodType(void.class, URL.class));
            } catch (Throwable t) {
                Throwables.sneakyThrow(t);
            }
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    /**
     * Get the {@code jdk.internal.loader.URLClassPath} object for the given class loader.
     * This only works on Java versions which actually have this class.
     *
     * @param loader The loader.
     * @return The
     */
    public static Object getURLClassPath(ClassLoader loader) {
        try {
            if (loader instanceof URLClassLoader) {
                return GET_URLClassLoader_ucp.invoke(loader);
            }

            Class<?> loaderClass = loader.getClass();
            MethodHandle ucpGetter = ucpGetterCache.get(loaderClass);
            if (ucpGetter == null) {
                Field theField = null;
                for (Field f : ReflectUtil.findAllFields(loaderClass)) {
                    if (C_URLClassPath.isAssignableFrom(f.getType())) {
                        theField = f;
                        break;
                    }
                }

                if (theField == null) {
                    throw new NoSuchFieldException("No URLClassPath field in loader class `" + loaderClass + "`");
                }

                UnsafeUtil.forcePublic(theField);
                theField.setAccessible(true);

                ucpGetter = UnsafeUtil.getInternalLookup().unreflectGetter(theField);
                ucpGetterCache.put(loaderClass, ucpGetter);
            }

            return ucpGetter.invoke(loader);
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
            throw new AssertionError();
        }
    }

    /**
     * Add the given URLs to be loaded by the given class loader.
     *
     * @param loader The class loader.
     * @param urls The URLs.
     */
    public static void addURLs(ClassLoader loader, URL... urls) {
        try {
            if (C_URLClassPath == null) {
                // legacy URLClassLoader reflection
                assert loader instanceof URLClassLoader;
                for (URL url : urls) {
                    M_URLClassLoader_addURL.invoke(loader, url);
                }

                return;
            }

            Object ucp = getURLClassPath(loader);
            for (URL url : urls) {
                M_URLClassPath_addURL.invoke(ucp, url);
            }
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
            throw new AssertionError();
        }
    }

}

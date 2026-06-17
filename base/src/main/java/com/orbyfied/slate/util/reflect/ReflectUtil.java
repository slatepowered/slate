package com.orbyfied.slate.util.reflect;

import com.orbyfied.slate.util.exception.Throwables;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A utility class for transforming and loading classes.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ReflectUtil {
    private ReflectUtil() { }

    static final Map<String, Class<?>> forNameCache = new HashMap<>();

    // The sun.misc.Unsafe instance
    static final Unsafe UNSAFE;

    /* Method handles for cracking  */
    static final MethodHandle SETTER_Field_modifiers;
    static final MethodHandle ClassLoader_findLoadedClass;
    static final MethodHandle ClassLoader_addClass;
    static final MethodHandles.Lookup INTERNAL_LOOKUP;

    static {
        try {
            // get using reflection
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            UNSAFE = (Unsafe) field.get(null);
        } catch (Exception e) {
            // rethrow error
            throw new ExceptionInInitializerError(e);
        }

        try {
            {
                // get lookup
                Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
                MethodHandles.publicLookup();
                INTERNAL_LOOKUP = (MethodHandles.Lookup)
                        UNSAFE.getObject(
                                UNSAFE.staticFieldBase(field),
                                UNSAFE.staticFieldOffset(field)
                        );
            }

            SETTER_Field_modifiers = INTERNAL_LOOKUP.findSetter(Field.class, "modifiers", Integer.TYPE);
            ClassLoader_findLoadedClass = INTERNAL_LOOKUP.findVirtual(ClassLoader.class, "findLoadedClass", MethodType.methodType(Class.class, String.class));
            ClassLoader_addClass = INTERNAL_LOOKUP.findVirtual(ClassLoader.class, "addClass", MethodType.methodType(void.class, Class.class));
        } catch (Throwable t) {
            // throw exception in init
            throw new ExceptionInInitializerError(t);
        }
    }

    public static MethodHandles.Lookup getInternalLookup() {
        return INTERNAL_LOOKUP;
    }

    public static Unsafe getUnsafe() {
        return UNSAFE;
    }

    /**
     * Invokes the default (non-proxy) method, equivalent of
     * {@link InvocationHandler#invokeDefault(Object, Method, Object...)}
     * in Java 16.
     *
     * @param on The object to invoke it on.
     * @param method The method to invoke.
     * @param args The arguments.
     * @return The return value.
     */
    public static Object invokeDefault(Object on, Method method, Object[] args) {
        try {
            MethodHandle handle = UnsafeUtil.findInvokeSpecial(method);
            return handle.invoke(on, args);
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
            throw new AssertionError();
        }
    }

    /**
     * Find all fields, including private ones, for the given class.
     *
     * @param klass The class.
     * @return The list of fields.
     */
    public static List<Field> findAllFields(Class<?> klass) {
        List<Field> fields = new ArrayList<>();
        for (; klass != null; klass = klass.getSuperclass()) {
            fields.addAll(Arrays.asList(klass.getDeclaredFields()));
        }

        return fields;
    }

    /**
     * Tries to get the class for the given type, stripping any
     * generic or other data on the type.
     *
     * @param type The type.
     * @return The class representing the type.
     */
    public static Class<?> getClassForType(Type type) {
        if (type == null) {
            return null;
        }

        if (type instanceof Class) {
            return (Class<?>) type;
        }

        if (type instanceof ParameterizedType) {
            return getClassForType(((ParameterizedType)type).getRawType());
        }

        if (type instanceof AnnotatedType) {
            return getClassForType(((AnnotatedType)type).getType());
        }

        if (type instanceof WildcardType) {
            return Object.class;
        }

        throw new IllegalArgumentException("No support to get the base class from Type object of type: " + type.getClass().getSimpleName());
    }

    /**
     * Get the loaded class by the given name.
     *
     * @param name The class name.
     * @throws IllegalArgumentException If no class by that name exists.
     * @return The class.
     */
    public static Class<?> getClass(String name) {
        Class<?> klass = forNameCache.get(name);
        if (klass != null)
            return klass;

        try {
            forNameCache.put(name, klass = Class.forName(name));
            return klass;
        } catch (Exception e) {
            throw new IllegalArgumentException("No class by name '" + name + "'", e);
        }
    }

    /**
     * Get the loaded class by the given name.
     *
     * @param name The class name.
     * @param loader The loader to load the class with.
     * @throws IllegalArgumentException If no class by that name exists.
     * @return The class.
     */
    public static Class<?> getClass(String name, ClassLoader loader) {
        Class<?> klass = forNameCache.get(name);
        if (klass != null)
            return klass;

        try {
            forNameCache.put(name, klass = Class.forName(name, true, loader));
            return klass;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while finding class by name '" + name + "'", e);
        }
    }

    /**
     * Set the modifiers of the given field.
     *
     * @param f The field.
     * @param mods The modifiers.
     */
    public static void setModifiers(Field f, int mods) {
        try {
            SETTER_Field_modifiers.invoke(f, mods);
        } catch (Throwable t) {
            throw new IllegalStateException("Failed to set modifiers of " + f, t);
        }
    }

    /**
     * Find the loaded class lowest in the chain of class loaders.
     *
     * @param loader The loader.
     * @param name The class name.
     * @return The loaded class or null if unloaded.
     */
    public static Class<?> findLoadedClassInParents(ClassLoader loader, String name) {
        try {
            while (loader != null) {
                Class<?> klass = (Class<?>) ClassLoader_findLoadedClass.invoke(loader, name);
                if (klass != null) {
                    return klass;
                }

                loader = loader.getParent();
            }

            return null;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Try to find a loaded class by the given name in the given loader.
     *
     * @param loader The loader.
     * @param name The class name.
     * @return The loaded class or null if absent.
     */
    public static Class<?> findLoadedClass(ClassLoader loader, String name) {
        try {
            return (Class<?>) ClassLoader_findLoadedClass.invoke(loader, name);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /** Black hole for class objects */
    public static boolean ensureLoaded(Class<?> klass) {
        if (klass == null) return false;
        return klass.hashCode() != 0;
    }

    /**
     * Finds the root class loader from the given class loader.
     *
     * @param loader The start loader.
     * @return The root class loader.
     */
    public static ClassLoader rootClassLoader(ClassLoader loader) {
        while (loader.getParent() != null) {
            loader = loader.getParent();
        }

        return loader;
    }

    @SuppressWarnings("unchecked")
    public static <T, T2> T2[] arrayCast(T[] arr, Class<T2> t2Class) {
        T2[] arr2 = (T2[]) Array.newInstance(t2Class, arr.length);
        for (int i = 0, n = arr.length; i < n; i++)
            arr2[i] = (T2) arr[i];
        return arr2;
    }

    /**
     *
     * @param aClass The first class.
     * @param bClass The second class.
     * @return 0 if they're the same, otherwise the distance between the two.
     */
    public static int findSuperclassSeparation(Class<?> aClass, Class<?> bClass) {
        if (aClass == bClass)
            return 0;
        if (aClass.isAssignableFrom(bClass)) {
            // swap aClass and bClass so bClass is the superclass and aClass
            // is the subclass
            Class<?> t = aClass;
            aClass = bClass;
            bClass = t;
        }

        // find separation
        Class<?> superCl = aClass.getSuperclass();
        if (superCl.isAssignableFrom(bClass)) {
            return findSuperclassSeparation(superCl, bClass) + 1;
        }

        if (bClass.isInterface()) {
            for (Class<?> itf : aClass.getInterfaces()) {
                if (itf.isAssignableFrom(bClass)) {
                    return findSuperclassSeparation(itf, bClass) + 1;
                }
            }
        }

        return -1;
    }

    /**
     * Preload all declared inner classes matching the given predicate recursively.
     */
    public static void preloadInnerClassesRecursive(Class<?> base, Predicate<Class<?>> predicate) {
        for (Class<?> klass : base.getDeclaredClasses()) {
            if (predicate.test(klass)) {
                preloadInnerClassesRecursive(klass, predicate);
            }
        }
    }

    /**
     * Preload all declared inner classes recursively.
     */
    public static void preloadInnerClassesRecursive(Class<?> base) {
        preloadInnerClassesRecursive(base, k -> true);
    }

    public record ParentsOfInterest(Class<?> klass, Class<?>[] klasses) {
        public void forEach(Consumer<Class<?>> consumer) {
            consumer.accept(klass);
            for (Class<?> kl : klasses) {
                consumer.accept(kl);
            }
        }
    }

    static final WeakHashMap<Class<?>, ParentsOfInterest> PARENTS_OF_INTEREST_CACHE = new WeakHashMap<>();

    public static ParentsOfInterest parentsOfInterest(Class<?> klass) {
        return PARENTS_OF_INTEREST_CACHE.computeIfAbsent(klass, __ -> {
            List<Class<?>> list = new ArrayList<>();
            for (Class kl = klass; kl != Object.class; kl = kl.getSuperclass()) {
                list.add(kl);
                for (Class itf : kl.getInterfaces()) {
                    list.add(itf);
                }
            }

            return new ParentsOfInterest(klass, list.toArray(new Class[0]));
        });
    }

    public static void registerByAllSuperclassesAndInterfaces(Map map, Object v) {
        parentsOfInterest(v.getClass()).forEach(k -> map.put(k, v));
    }

    public static void removeByAllSuperclassesAndInterfaces(Map map, Object v) {
        parentsOfInterest(v.getClass()).forEach(k -> map.remove(k, v));
    }

}

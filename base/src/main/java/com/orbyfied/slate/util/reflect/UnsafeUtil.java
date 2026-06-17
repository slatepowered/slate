package com.orbyfied.slate.util.reflect;

import com.orbyfied.slate.util.exception.Throwables;
import com.orbyfied.slate.util.functional.ThrowingRunnable;
import com.orbyfied.slate.util.functional.ThrowingSupplier;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utilities for working with {@link sun.misc.Unsafe} and other unsafe reflection.
 */
@SuppressWarnings("unchecked")
public class UnsafeUtil {

    // the unsafe instance
    static final sun.misc.Unsafe UNSAFE;

    static {
        sun.misc.Unsafe unsafe = null; // temp var

        try {
            // get using reflection
            Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (sun.misc.Unsafe) field.get(null);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }

        // set unsafe
        UNSAFE = unsafe;
    }

    /**
     * Get the instance of {@link sun.misc.Unsafe}.
     *
     * @return The instance.
     */
    public static sun.misc.Unsafe getUnsafe() {
        return UNSAFE;
    }

    private static boolean areModulesSupported; // Whether modules are supported in this Java version
    private static Method M_Module_implAddOpens;

    private static final MethodHandle SET_Method_modifiers;
    private static final MethodHandle SET_Field_modifiers;

    /**
     * The cached special invocation handles.
     */
    private static final Map<Method, MethodHandle> cachedSpecialHandles = new HashMap<>();

    /**
     * The internal method handle lookup with all permissions.
     */
    private static final MethodHandles.Lookup INTERNAL_LOOKUP;

    /**
     * Get the internal method handle lookup with all permissions.
     *
     * @return The internal lookup.
     */
    public static MethodHandles.Lookup getInternalLookup() {
        return INTERNAL_LOOKUP;
    }

    static {
        try {
            // get lookup
            Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            MethodHandles.publicLookup();
            INTERNAL_LOOKUP = (MethodHandles.Lookup)
                    UNSAFE.getObject(
                            UNSAFE.staticFieldBase(field),
                            UNSAFE.staticFieldOffset(field)
                    );

            SET_Method_modifiers = INTERNAL_LOOKUP.findSetter(Method.class, "modifiers", Integer.TYPE);
            SET_Field_modifiers = INTERNAL_LOOKUP.findSetter(Field.class, "modifiers", Integer.TYPE);
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }

        try {
            // de-encapsulate if Modules exist
            Class<?> CLASS_Module = Class.forName("java.lang.Module");
            areModulesSupported = true;

            M_Module_implAddOpens = CLASS_Module.getDeclaredMethod("implAddOpens", String.class);
            forcePublic(M_Module_implAddOpens);
            M_Module_implAddOpens.setAccessible(true);

            final Class<?> C_ModuleLayer = Class.forName("java.lang.ModuleLayer");
            final Method M_ModuleLayer_boot = C_ModuleLayer.getDeclaredMethod("boot");
            final Method M_ModuleLayer_modules = C_ModuleLayer.getDeclaredMethod("modules");
            final Method M_Module_getPackages = CLASS_Module.getDeclaredMethod("getPackages");

            // open every module, including unnamed ones, to every other module
            Set<Object> modules = (Set<Object>) M_ModuleLayer_modules.invoke(M_ModuleLayer_boot.invoke(null));
            for (Object moduleToOpen : modules) {
                Set<String> packages = (Set<String>) M_Module_getPackages.invoke(moduleToOpen);
                for (String pk : packages) {
                    M_Module_implAddOpens.invoke(moduleToOpen, pk);
                }
            }
        } catch (ClassNotFoundException ignored) {
            // no de-encapsulation required
            M_Module_implAddOpens = null;
            areModulesSupported = false;
        } catch (Throwable t) {
            throw new RuntimeException("Failed to de-encapsulate", t);
        }
    }

    // execute 'safely' (no errors in signature required)
    protected static void doSafe(ThrowingRunnable runnable) {
        runnable.run();
    }

    // execute 'safely' (no errors in signature required)
    protected static <T> T doSafe(ThrowingSupplier<T> runnable) {
        return runnable.get();
    }

    /**
     * Forcefully allow access to the given
     * implMethod by setting it's access modifier
     * to public.
     *
     * @param method The implMethod.
     * @return The implMethod.
     */
    public static Method forcePublic(Method method) {
        // get modifiers
        int mods = method.getModifiers();

        // negate every other access modifier
        mods &= ~Modifier.PRIVATE;
        mods &= ~Modifier.PROTECTED;

        // set public modifier
        mods |= Modifier.PUBLIC;

        // set modifiers and return
        setModifiers(method, mods);
        return method;
    }

    /**
     * Forcefully allow access to the given
     * field by setting it's access modifier
     * to public.
     *
     * @param field The field.
     * @return The field.
     */
    public static Field forcePublic(Field field) {
        // get modifiers
        int mods = field.getModifiers();

        // negate every other access modifier
        mods &= ~Modifier.PRIVATE;
        mods &= ~Modifier.PROTECTED;

        // set public modifier
        mods |= Modifier.PUBLIC;

        // set modifiers and return
        setModifiers(field, mods);
        return field;
    }

    /**
     * Set the modifiers on the given implMethod
     * to your liking.
     *
     * @param method The implMethod.
     * @param modifiers The modifiers to set.
     */
    public static void setModifiers(Method method, int modifiers) {
        doSafe(() -> {
            SET_Method_modifiers.invokeExact(method, modifiers);
        });
    }

    /**
     * Set the modifiers on the given field
     * to your liking.
     *
     * @param field The field.
     * @param modifiers The modifiers to set.
     */
    public static void setModifiers(Field field, int modifiers) {
        doSafe(() -> {
            SET_Field_modifiers.invokeExact(field, modifiers);
        });
    }

    /**
     * Find a method handle for the given reflection method
     * which specially invokes it.
     *
     * @param method The method.
     * @return The invokespecial method handle.
     */
    public static MethodHandle findInvokeSpecial(Method method) {
        MethodHandle handle = cachedSpecialHandles.get(method);
        if (handle == null) {
            try {
                handle = INTERNAL_LOOKUP.unreflectSpecial(method, method.getDeclaringClass());
            } catch (Exception e) {
                Throwables.sneakyThrow(e);
                throw new AssertionError();
            }
        }

        return handle;
    }

}

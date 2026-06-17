package com.orbyfied.slate.util.reflect;

import java.lang.reflect.Type;

public interface FieldAccessor {

    byte PT_REFERENCE = 0;
    byte PT_LONG = 1;
    byte PT_INT = 2;
    byte PT_DOUBLE = 3;
    byte PT_FLOAT = 4;
    byte PT_SHORT = 5;
    byte PT_BYTE = 6;
    byte PT_BOOLEAN = 7;
    byte PT_CHAR = 8;

    static byte getPrimitiveType(Class<?> kl) {
        if (!kl.isPrimitive()) return 0;
        if (kl == Long.TYPE) return 1;
        if (kl == Integer.TYPE) return 2;
        if (kl == Double.TYPE) return 3;
        if (kl == Float.TYPE) return 4;
        if (kl == Byte.TYPE) return 6;
        if (kl == Short.TYPE) return 5;
        if (kl == Boolean.TYPE) return 7;
        if (kl == Character.TYPE) return 8;
        throw new IllegalArgumentException("Weird primitive type " + kl);
    }

    Class<?> declaringClass();
    String name();
    Type genericType();
    Class<?> valueType();
    byte primitiveType();

    Object getAsObject(Object instance);
    void setFromObject(Object instance, Object value);

}

package com.orbyfied.slate.util.reflect;

import com.orbyfied.slate.util.reflect.UnsafeUtil;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public record UnsafeFieldAccessor(
        Class<?> declaringClass,
        String name,
        Type genericType,
        Class<?> valueType,
        byte primitiveType,
        long offset
) implements FieldAccessor {

    static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();

    public static UnsafeFieldAccessor forField(Field field) {
        try {
            return new UnsafeFieldAccessor(
                    field.getDeclaringClass(),
                    field.getName(),
                    field.getGenericType(),
                    field.getType(),
                    FieldAccessor.getPrimitiveType(field.getType()),
                    UNSAFE.objectFieldOffset(field)
            );
        } catch (Exception ex) {
            throw new IllegalArgumentException("An exception occurred while trying to create UnsafeFieldAccessor for " + field, ex);
        }
    }

    @Override
    public Object getAsObject(Object instance) {
        return switch (this.primitiveType) {
            case 0 -> UNSAFE.getObject(instance, this.offset);
            case 1 -> UNSAFE.getLong(instance, this.offset);
            case 2 -> UNSAFE.getInt(instance, this.offset);
            case 3 -> UNSAFE.getDouble(instance, this.offset);
            case 4 -> UNSAFE.getFloat(instance, this.offset);
            case 5 -> UNSAFE.getShort(instance, this.offset);
            case 6 -> UNSAFE.getByte(instance, this.offset);
            case 7 -> UNSAFE.getBoolean(instance, this.offset);
            case 8 -> UNSAFE.getChar(instance, this.offset);
            default ->
                    throw new IllegalArgumentException("Goofy primitive type idk how to get that [byte primitiveType = " + this.primitiveType + "]");
        };
    }

    @Override
    public void setFromObject(Object instance, Object value) {
        switch (this.primitiveType) {
            case 0: UNSAFE.putObject(instance, this.offset, value); break;
            case 1: UNSAFE.putLong(instance, this.offset, value != null ? (Long)value : 0L); break;
            case 2: UNSAFE.putInt(instance, this.offset, value != null ? (Integer)value : 0); break;
            case 3: UNSAFE.putDouble(instance, this.offset, value != null ? (Double)value : (double)0.0F); break;
            case 4: UNSAFE.putFloat(instance, this.offset, value != null ? (Float)value : 0.0F); break;
            case 5: UNSAFE.putShort(instance, this.offset, value != null ? (Short)value : 0); break;
            case 6: UNSAFE.putByte(instance, this.offset, value != null ? (Byte)value : 0); break;
            case 7: UNSAFE.putBoolean(instance, this.offset, value != null ? (Boolean)value : false); break;
            case 8: UNSAFE.putChar(instance, this.offset, value != null ? (Character)value : '\u0000'); break;
            default:
                throw new IllegalArgumentException("Goofy primitive type idk how to set that [byte primitiveType = " + this.primitiveType + "]");
        }
    }

    @Override
    public String toString() {
        return "UnsafeFieldAccessor(" + declaringClass.getName() + "::" + name + ")";
    }
}

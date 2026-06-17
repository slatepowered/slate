package com.orbyfied.slate.util.reflect;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

public class Types {

    public static ParameterizedType parameterized(Type owner, Type... parameters) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return parameters;
            }

            @Override
            public Type getRawType() {
                return owner;
            }

            @Override
            public Type getOwnerType() {
                return owner;
            }
        };
    }

    public static Class<?> classOf(Type type) {
        if (type == null) {
            return null;
        } else if (type instanceof Class) {
            return (Class)type;
        } else if (type instanceof ParameterizedType) {
            return classOf(((ParameterizedType)type).getRawType());
        } else if (type instanceof AnnotatedType) {
            return classOf(((AnnotatedType)type).getType());
        } else if (type instanceof WildcardType) {
            return Object.class;
        } else {
            throw new IllegalArgumentException("No support to get the base class from Type object of type: " + type.getClass().getSimpleName());
        }
    }

}

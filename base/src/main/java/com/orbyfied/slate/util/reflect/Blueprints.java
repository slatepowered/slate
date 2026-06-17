package com.orbyfied.slate.util.reflect;

import lombok.Builder;
import lombok.Getter;
import com.orbyfied.slate.util.data.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Blueprints {

    @Builder(toBuilder = true, builderClassName = "Builder")
    @Getter
    public static final class BaseMemberOptions {
        final boolean inheritMembers;
    }

    public static <A extends Annotation> Map<String, Pair<FieldAccessor, A>> compileAnnotatedInstanceFields(Class<?> klass, Class<A> aClass, BaseMemberOptions options) {
        try {
            // collect all fields
            List<Field> fields = new ArrayList<>(List.of(klass.getDeclaredFields()));
            if (options.inheritMembers) {
                for (Class<?> kl = klass.getSuperclass(); kl != Object.class && kl != null; kl = kl.getSuperclass()) {
                    fields.addAll(List.of(kl.getDeclaredFields()));
                }
            }

            // filter and compile field accessors
            Map<String, Pair<FieldAccessor, A>> map = new LinkedHashMap<>();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                A annotation = field.getAnnotation(aClass);
                if (annotation == null) continue;

                var fieldAccessor = UnsafeFieldAccessor.forField(field);
                map.put(fieldAccessor.name(), Pair.of(fieldAccessor, annotation));
            }

            return map;
        } catch (Exception ex) {
            throw new IllegalStateException("An exception occurred while compiling annotated fields for " + klass, ex);
        }
    }

}

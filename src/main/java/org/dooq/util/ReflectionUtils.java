package org.dooq.util;

import org.dooq.api.ColumnAlias;
import org.dooq.api.PartitionKey;
import org.dooq.api.SortKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author alex
 */
public class ReflectionUtils {

    public static Map<String, Field> mapFields(@NotNull Class<?> clazz) {

        final boolean notARecord = clazz.isRecord();

        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> {

                    if (Modifier.isStatic(field.getModifiers())) {
                        return false;
                    }

                    if (Modifier.isTransient(field.getModifiers())) {
                        return false;
                    }

                    if (Modifier.isNative(field.getModifiers())) {
                        return false;
                    }

                    if (notARecord) {
                        return !Modifier.isFinal(field.getModifiers());
                    }

                    return true;
                })
                .collect(Collectors.toMap(ReflectionUtils::getColumnName, c -> c));
    }

    public static String getColumnName(@NotNull Field field) {

        if (field.isAnnotationPresent(ColumnAlias.class)) {
            return field.getAnnotation(ColumnAlias.class).value();
        }

        if (field.isAnnotationPresent(PartitionKey.class)) {
            return field.getAnnotation(PartitionKey.class).alias();
        }

        if (field.isAnnotationPresent(SortKey.class)) {
            return field.getAnnotation(SortKey.class).alias();
        }

        return field.getName();
    }

    public static boolean hasDiamondInterface(@NotNull Field field) {
        return field.getGenericType().getTypeName().contains("<");
    }

    public static @Nullable Class<?> getGenericType(@NotNull Field field) {

        var s = field.getGenericType()
                .getTypeName();

        if (!s.contains("<")) return null;

        var clazz = s.substring(s.lastIndexOf("<") + 1, s.indexOf(">"));

        if (clazz.contains(",")) {
            clazz = clazz.substring(clazz.indexOf(",") + 1)
                    .trim();
        }

        if (clazz.contains("? extends ")) {
            clazz = clazz.substring("? extends ".length())
                    .trim();
        }

        try {
            return Class.forName(clazz, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public static @Nullable Class<?> getGenericType(@NotNull Type type) {

        var s = type.getTypeName();

        if (!s.contains("<")) return null;

        var clazz = s.substring(s.lastIndexOf("<") + 1, s.indexOf(">"));

        if (clazz.contains(",")) {
            clazz = clazz.substring(clazz.indexOf(",") + 1)
                    .trim();
        }

        if (clazz.contains("? extends ")) {
            clazz = clazz.substring("? extends ".length())
                    .trim();
        }

        try {
            return Class.forName(clazz, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }


    public static boolean hasInterface(@NotNull Class<?> target, @NotNull Class<?> inter) {

        if (!inter.isInterface()) {
            throw new IllegalArgumentException(inter + " is not a interface");
        }

        if (target == inter) {
            throw new IllegalStateException(inter + " is the same as " + target);
        }

        return Arrays.asList(target.getInterfaces()).contains(inter);
    }

    @SuppressWarnings("unchecked")
    public static <K> @NotNull K newInstance(@NotNull Class<K> type) {

        try {
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                if (constructor.getParameterCount() == 0) {
                    return (K) constructor.newInstance();
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }

        throw new IllegalArgumentException("No default constructor found for " + type);
    }
}

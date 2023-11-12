package org.dooq.core;

import org.dooq.api.Column;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.beans.Transient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

public class AttributeWriter {

    @Contract(pure = true)
    public static @NotNull Map<String, AttributeValue> parseMap(@NotNull Map<String, Object> map) {
        Map<String, AttributeValue> resultMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            resultMap.put(entry.getKey(), parse(entry.getValue()));
        }

        return resultMap;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static AttributeValue parse(Object value) {

        if (value == null) return nil();

        if (value instanceof AttributeValue) {
            return (AttributeValue) value;
        }

        if (value instanceof String) {
            return string((String) value);
        }

        if (value instanceof Number) {
            return number((Number) value);
        }

        if (value instanceof Boolean bool) {
            return bool(bool);
        }

        if (value instanceof Set<?> set) {

            if (set.isEmpty()) return nil();

            var first = set.toArray()[0];

            if (first instanceof String) {
                return stringSet((Set<String>) set);
            }

            if (first instanceof Number) {
                return numberSet((Set<Number>) set);
            }

            throw new IllegalStateException("Not implemented set of type: " + first.getClass());

        }

        if (value instanceof Collection<?>) {
            return collection((Collection<?>) value);
        }

        if (value instanceof Map) {
            return map((Map) value);
        }

        if (value instanceof LocalDate || value instanceof LocalDateTime) {
            return string(value.toString());
        }

        if (value.getClass().getPackageName().startsWith("java")) {
            throw new IllegalStateException("Not implemented: " + value.getClass());
        }

        /*
        Serialize beans as map
         */
        try {
            return parseAsMap(value);
        } catch (IllegalAccessException e) {
            Logger.getLogger(AttributeWriter.class.getName())
                    .log(Level.SEVERE, null, e);
        }

        throw new IllegalStateException("Not implemented: " + value.getClass());
    }

    public static AttributeValue parseAsMap(@NotNull Object obj) throws IllegalAccessException {

        Map<String, AttributeValue> parsedMap = new HashMap<>();

        var fieldMap = Arrays.stream(obj.getClass().getDeclaredFields())
                .filter(a -> !Modifier.isTransient(a.getModifiers()) && !a.isAnnotationPresent(Transient.class))
                .collect(Collectors.toMap(Field::getName, b -> b));

        for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {

            entry.getValue().setAccessible(true);

            var fieldValue = entry.getValue()
                    .get(obj);

            parsedMap.put(entry.getKey(), parse(fieldValue));
        }

        return AttributeValue.builder()
                .m(parsedMap)
                .build();
    }

    public static @NotNull @Unmodifiable Map<String, AttributeValue> parsing(@NotNull Column<?, ?> column, Object value) {

        Objects.requireNonNull(value, "Value can't be null");

        return Map.of(column.name(), parse(value));
    }

    public static @NotNull @Unmodifiable Map<String, AttributeValue> parsing(String column, Object value) {

        Objects.requireNonNull(value, "Value can't be null");

        return Map.of(column, parse(value));
    }

    public static @NotNull @Unmodifiable Map<String, AttributeValue> string(@NotNull Column<?, ?> column, String number) {
        return Map.of(column.name(), string(number));
    }

    public static AttributeValue string(String value) {
        return AttributeValue.builder()
                .s(value)
                .build();
    }

    public static @NotNull @Unmodifiable Map<String, AttributeValue> number(@NotNull Column<?, ?> column, Number number) {
        return Map.of(column.name(), number(number));
    }

    public static AttributeValue number(Number number) {
        return AttributeValue.builder()
                .n(number + "")
                .build();
    }

    public static AttributeValue bool(Boolean bool) {
        return AttributeValue.builder()
                .bool(bool)
                .build();
    }

    public static AttributeValue nil() {
        return AttributeValue.builder()
                .nul(true)
                .build();
    }

    public static AttributeValue stringSet(@NotNull Set<String> set) {
        return AttributeValue
                .builder()
                .ss(set)
                .build();
    }

    public static AttributeValue numberSet(@NotNull Set<Number> set) {

        var parsed = set.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toList());

        return AttributeValue
                .builder()
                .ns(parsed)
                .build();
    }

    public static AttributeValue collection(@NotNull Collection<?> list) {

        var parsedList = list.stream()
                .map(AttributeWriter::parse)
                .collect(Collectors.toList());

        return AttributeValue.builder()
                .l(parsedList)
                .build();
    }

    public static AttributeValue map(@NotNull Map<String, ?> map) {

        Map<String, AttributeValue> parsedMap = new HashMap<>();

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            parsedMap.put(entry.getKey(), parse(entry.getValue()));
        }

        return AttributeValue.builder()
                .m(parsedMap)
                .build();
    }

    public static AttributeValue compressedList(@NotNull Collection<String> collection) throws IOException {
        var compressedList = new ArrayList<SdkBytes>();

        for (String s : collection) {
            compressedList.add(compress(s));
        }

        return AttributeValue.builder()
                .bs(compressedList)
                .build();

    }

    public static AttributeValue compressed(@NotNull String value) throws IOException {

        var data = compress(value);

        return AttributeValue.builder()
                .b(data)
                .build();
    }

    private static @NotNull SdkBytes compress(@NotNull String value) throws IOException {
        var baos = new ByteArrayOutputStream();
        var gos = new GZIPOutputStream(baos);

        gos.write(value.getBytes(StandardCharsets.UTF_8));
        gos.close();

        var data = baos.toByteArray();

        baos.close();

        return SdkBytes.fromByteArray(data);
    }

    public static Object unwrap(@NotNull AttributeValue value) {


        if (value.s() != null) return value.s();
        if (value.n() != null) return value.n();
        if (value.ns() != null) return value.ns();
        if (value.bool() != null) return value.bool();

        throw new IllegalStateException("Not implemented!");
    }

}

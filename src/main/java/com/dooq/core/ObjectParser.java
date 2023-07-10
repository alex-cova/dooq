package com.dooq.core;

import com.dooq.engine.ParserCompiler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public abstract class ObjectParser<T> {

    public abstract T parse(Map<String, AttributeValue> value);

    public abstract T newInstance();

    public abstract Map<String, AttributeValue> write(T value);

    protected AttributeValue writeUUID(@Nullable UUID value) {
        if (value == null) return null;

        return AttributeValue.fromS(value.toString());
    }

    protected AttributeValue writeInt(int value) {
        return AttributeValue.fromN(String.valueOf(value));
    }

    protected AttributeValue writeFloat(float value) {
        return AttributeValue.fromN(String.valueOf(value));
    }

    protected AttributeValue writeLong(long value) {
        return AttributeValue.fromN(String.valueOf(value));
    }

    protected AttributeValue writeBool(boolean value) {
        return AttributeValue.fromBool(value);
    }

    protected AttributeValue writeString(@Nullable String value) {
        if (value == null) return null;

        return AttributeValue.fromS(value);
    }

    protected AttributeValue writeInteger(@Nullable Integer value) {
        if (value == null) return null;

        return AttributeValue.fromN(String.valueOf(value));
    }

    protected AttributeValue writeStringMap(@Nullable Map<String, String> value) {
        if (value == null) return null;

        return AttributeValue.fromM(value.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, b -> AttributeValue.fromS(b.getValue()))));
    }

    @SuppressWarnings("unchecked")
    protected AttributeValue writeMap(@Nullable Map<String, ?> value, Class<?> type) {
        if (value == null) return null;

        if (type == String.class) {
            return writeStringMap((Map<String, String>) value);
        }

        return AttributeValue.fromM(value.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, b -> lookUp(b.getValue()))));
    }


    protected AttributeValue lookUp(@Nullable Object value) {

        if (value == null) return null;

        if (value instanceof String string) {
            return writeString(string);
        }

        if (value instanceof UUID uuid) {
            return writeUUID(uuid);
        }

        if (value instanceof Long l) {
            return writeLong(l);
        }

        if (value instanceof Boolean bool) {
            return writeBoolean(bool);
        }

        if (value instanceof Integer i) {
            return writeInteger(i);
        }

        if (value instanceof Float f) {
            return writeFloat(f);
        }

        if (value instanceof BigDecimal bd) {
            return writeBigDecimal(bd);
        }

        System.err.println("Not found: " + value.getClass());

        return null;
    }

    protected <K> AttributeValue writeList(@Nullable List<K> value, Class<K> type) {

        if (value == null) return null;

        if (value.isEmpty()) return null;

        if (type == String.class) {
            return AttributeValue.fromL(value.stream()
                    .map(a -> writeString((String) a))
                    .filter(Objects::nonNull)
                    .toList());
        }

        if (type == Integer.class) {
            return AttributeValue.fromL(value.stream()
                    .map(a -> writeInteger((Integer) a))
                    .filter(Objects::nonNull)
                    .toList());
        }

        if (type == BigDecimal.class) {
            return AttributeValue.fromL(value.stream()
                    .map(a -> writeBigDecimal((BigDecimal) a))
                    .filter(Objects::nonNull)
                    .toList());
        }


        if (type == UUID.class) {
            return AttributeValue.fromL(value.stream()
                    .map(a -> writeUUID((UUID) a))
                    .filter(Objects::nonNull)
                    .toList());
        }

        if (type == Long.class) {
            return AttributeValue.fromL(value.stream()
                    .map(a -> writeLonger((Long) a))
                    .filter(Objects::nonNull)
                    .toList());
        }

        if (isComplex(type)) {

            ObjectParser<K> parser = ParserCompiler.getParser(type);

            return AttributeValue.fromL(value.stream()
                    .map(a -> AttributeValue.fromM(parser.write(a)))
                    .filter(Objects::nonNull)
                    .toList());
        }

        return AttributeValue.fromL(value.stream()
                .map(this::lookUp)
                .filter(Objects::nonNull)
                .toList());
    }

    protected AttributeValue writeSet(@Nullable Set<?> value, Class<?> type) {

        if (value == null) return null;

        return AttributeValue.fromSs(value.stream()
                .map(Object::toString)
                .toList());
    }

    protected AttributeValue writeStringSet(@Nullable Set<String> value) {
        if (value == null) return null;

        return AttributeValue.fromSs(new ArrayList<>(value));
    }

    protected <K> AttributeValue writeComplex(@Nullable K value, Class<K> type) {

        if (value == null) return null;

        ObjectParser<K> parser = ParserCompiler.getParser(type);

        return AttributeValue.fromM(parser.write(value));
    }

    protected AttributeValue writeStringList(@Nullable List<String> value) {
        if (value == null) return null;

        return AttributeValue.fromL(value.stream()
                .map(AttributeValue::fromS)
                .toList());
    }

    protected AttributeValue writeLonger(@Nullable Long value) {
        if (value == null) return null;

        return AttributeValue.fromN(String.valueOf(value));
    }

    protected AttributeValue writeFloater(@Nullable Float value) {
        if (value == null) return null;

        return AttributeValue.fromN(String.valueOf(value));
    }

    protected AttributeValue writeBoolean(@Nullable Boolean value) {
        if (value == null) return null;

        return AttributeValue.fromBool(value);
    }

    protected AttributeValue writeLocalDate(@Nullable LocalDate value) {
        if (value == null) return null;

        return AttributeValue.fromS(value.toString());
    }

    protected AttributeValue writeLocalTime(@Nullable LocalTime value) {
        if (value == null) return null;

        return AttributeValue.fromS(value.toString());
    }

    protected AttributeValue writeLocalDateTime(@Nullable LocalDateTime value) {
        if (value == null) return null;

        return AttributeValue.fromS(value.toString());
    }

    protected AttributeValue writeBigDecimal(@Nullable BigDecimal value) {
        if (value == null) return null;

        return AttributeValue.fromN(value.toPlainString());
    }

    protected int parseInt(@Nullable AttributeValue value) {
        if (value == null) return 0;
        return Integer.parseInt(value.n());
    }

    protected Integer parseInteger(@Nullable AttributeValue value) {
        if (value == null || Boolean.TRUE.equals(value.nul())) return null;

        if (value.n() == null) return null;

        return Integer.valueOf(value.n());
    }

    protected Long parseLonger(@Nullable AttributeValue value) {

        if (value == null || Boolean.TRUE.equals(value.nul())) return null;

        if (value.n() == null) return null;

        return Long.valueOf(value.n());
    }

    protected Float parseFloater(@Nullable AttributeValue value) {
        if (value == null || Boolean.TRUE.equals(value.nul())) return null;

        return Float.valueOf(value.n());
    }

    protected float parseFloat(@Nullable AttributeValue value) {
        if (value == null || Boolean.TRUE.equals(value.nul())) return 0.0f;

        return Float.parseFloat(value.n());
    }

    protected long parseLong(@Nullable AttributeValue value) {
        if (value == null) return 0;
        if (value.n() == null) return 0;

        return Long.parseLong(value.n());
    }

    protected LocalDate parseLocalDate(@Nullable AttributeValue value) {
        if (value == null || Boolean.TRUE.equals(value.nul())) return null;

        if (value.s() != null) {
            return LocalDate.parse(value.s());
        }

        if (value.n() != null) {
            return LocalDate.ofEpochDay(Long.parseLong(value.n()));
        }

        return null;
    }

    protected LocalDateTime parseLocalDateTime(@Nullable AttributeValue value) {
        if (value == null || Boolean.TRUE.equals(value.nul())) return null;

        if (value.s() != null) {
            return LocalDateTime.parse(value.s());
        }

        if (value.n() != null) {
            return LocalDateTime.ofEpochSecond(Long.parseLong(value.n()), 0, ZoneOffset.UTC);
        }

        return null;
    }

    protected String parseString(@Nullable AttributeValue value) {

        if (value == null || Boolean.TRUE.equals(value.nul())) return null;

        return value.s();
    }

    protected Boolean parseBoolean(@Nullable AttributeValue value) {
        if (value == null || Boolean.TRUE.equals(value.nul())) return null;

        return value.bool();
    }

    protected boolean parseBool(@Nullable AttributeValue value) {
        if (value == null) return false;

        return Boolean.TRUE.equals(value.bool());
    }

    protected BigDecimal parseBigDecimal(@Nullable AttributeValue value) {
        if (value == null || Boolean.TRUE.equals(value.nul())) return null;

        if (value.n() != null) {
            return new BigDecimal(value.n());
        }

        if (value.s() != null) {
            return new BigDecimal(value.s());
        }

        return null;
    }

    protected Set<String> parseStringSet(@Nullable AttributeValue value) {

        if (value == null || Boolean.TRUE.equals(value.nul())) return null;

        if (value.ss() == null) return null;

        return new HashSet<>(value.ss());
    }

    protected UUID parseUUID(@Nullable AttributeValue value) {
        if (value == null || Boolean.TRUE.equals(value.nul())) return null;

        if (value.s() == null) return null;

        try {
            return UUID.fromString(value.s());
        } catch (Exception ex) {
            Logger.getLogger(ObjectParser.class.getName())
                    .log(Level.WARNING, "Invalid UUID value: '%s'".formatted(value.s()));
        }

        return null;
    }

    protected List<String> parseStringList(@Nullable AttributeValue value) {
        if (value == null || Boolean.TRUE.equals(value.nul())) return null;

        if (value.hasL()) {
            return value.l()
                    .stream()
                    .map(this::parseString)
                    .toList();
        }

        if (value.hasSs()) {
            return value.ss();
        }

        return null;
    }

    protected <V> V parseComplex(@Nullable AttributeValue value, Class<V> type) {

        if (value == null || Boolean.TRUE.equals(value.nul())) return null;

        if (type.getName().startsWith("java")) {
            return lookUp(value, type);
        }

        if (value.m() != null) {
            var parser = ParserCompiler.getParser(type);

            return parser.parse(value.m());
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    protected <V> V lookUp(AttributeValue value, Class<V> type) {
        if (type == String.class) {
            return (V) parseString(value);
        }

        if (type == Integer.class) {
            return (V) parseInteger(value);
        }

        if (type == BigDecimal.class) {
            return (V) parseBigDecimal(value);
        }

        if (type == UUID.class) {
            return (V) parseUUID(value);
        }

        if (type == Boolean.class) {
            return (V) parseBoolean(value);
        }

        if (type == LocalDate.class) {
            return (V) parseLocalDate(value);
        }

        if (type == LocalDateTime.class) {
            return (V) parseLocalDateTime(value);
        }

        throw new IllegalStateException("Value not implemented: " + type);
    }

    protected Map<String, ?> parseMap(@Nullable AttributeValue value, Class<?> type) {

        if (value == null || Boolean.TRUE.equals(value.nul())) return null;

        if (value.m() == null) return null;

        if (isComplex(type)) {

            var parser = ParserCompiler.getParser(type);

            return value.m().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, v -> parseComplex(v.getValue(), type)));
        }

        Map<String, Object> resultMap = new HashMap<>(value.m().size());

        for (Map.Entry<String, AttributeValue> entry : value.m().entrySet()) {

            if (type == String.class) {
                resultMap.put(entry.getKey(), parseString(entry.getValue()));
            } else if (type == BigDecimal.class) {
                resultMap.put(entry.getKey(), parseBigDecimal(entry.getValue()));
            } else if (type == Integer.class) {
                resultMap.put(entry.getKey(), parseInteger(entry.getValue()));
            } else if (type == Long.class) {
                resultMap.put(entry.getKey(), parseLonger(entry.getValue()));
            } else if (type == UUID.class) {
                resultMap.put(entry.getKey(), parseUUID(entry.getValue()));
            } else if (type == Boolean.class) {
                resultMap.put(entry.getKey(), parseBool(entry.getValue()));
            }
        }

        return resultMap;

    }

    protected List<?> parseList(AttributeValue value, Class<?> type) {

        if (value == null || Boolean.TRUE.equals(value.nul())) return null;

        if (type == String.class) {
            return value.l().stream()
                    .map(this::parseString)
                    .filter(Objects::nonNull)
                    .toList();
        }

        if (type == BigDecimal.class) {
            return value.l().stream()
                    .map(this::parseBigDecimal)
                    .filter(Objects::nonNull)
                    .toList();
        }

        if (type == UUID.class) {
            return value.l().stream()
                    .map(this::parseUUID)
                    .filter(Objects::nonNull)
                    .toList();
        }

        if (isComplex(type)) {
            return value.l().stream()
                    .map(v -> parseComplex(v, type))
                    .filter(Objects::nonNull)
                    .toList();
        }

        return null;
    }

    protected Set<?> parseSet(AttributeValue value, Class<?> type) {

        if (value == null || Boolean.TRUE.equals(value.nul())) return null;

        if (value.ss() == null) return null;

        if (type == String.class) {
            return parseStringSet(value);
        }

        if (type == BigDecimal.class) {
            return value.ss().stream()
                    .map(BigDecimal::new)
                    .collect(Collectors.toSet());
        }

        return null;
    }

    private boolean isComplex(@NotNull Class<?> type) {
        return !type.getName().startsWith("java");
    }

}

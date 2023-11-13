package org.dooq.engine;

import org.dooq.api.ColumnAlias;
import org.dooq.api.PartitionKey;
import org.dooq.api.SortKey;
import org.dooq.core.FilteredMap;
import org.dooq.core.ObjectParser;
import org.dooq.util.ReflectionUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.beans.Transient;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;

/**
 * DynamoDB reflection-less high performance record parser compiler
 *
 * @author alex
 */
@ApiStatus.Experimental
public class ParserCompiler extends ClassLoader {

    public static boolean DEBUG = false;
    private static final ParserCompiler INSTANCE = new ParserCompiler();
    private final Map<Class<?>, ObjectParser<?>> CACHE = Collections.synchronizedMap(new HashMap<>());

    private ParserCompiler() {
    }

    private Class<?> defineNewClass(byte[] bytecode, String name) {

        if (DEBUG) {
            try {

                var file = new File("compiled");
                var ignored = file.mkdir();

                FileOutputStream outputStream = new FileOutputStream(new File(file, name + ".class"));
                outputStream.write(bytecode);
                outputStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return super.defineClass("org.dooq.engine." + name, bytecode, 0, bytecode.length);
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull ObjectParser<T> getParser(@NotNull Class<T> type) {

        var parser = INSTANCE.CACHE.get(type);

        if (parser != null) {
            return (ObjectParser<T>) parser;
        }

        if (DEBUG) {
            System.out.println("Creating parser for class: " + type);
        }

        boolean invalidConstructor = true;

        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                invalidConstructor = false;
                break;
            }
        }

        if (invalidConstructor) {
            throw new IllegalArgumentException("No args constructor is required for type '%s'".formatted(type));
        }

        ClassWriter writer = new ClassWriter(0);

        writer.visit(V17, ACC_PUBLIC, getParentName(type),
                null, Type.getInternalName(ObjectParser.class), null);

        defineNewInstance(writer, type);
        defineConstructor(writer);
        defineParser(writer, type);
        defineWriter(writer, type);

        writer.visitEnd();

        byte[] bytecode = writer.toByteArray();

        var parserInstance = createObject(bytecode, type);

        INSTANCE.CACHE.put(type, parserInstance);

        return parserInstance;
    }

    private static void defineNewInstance(@NotNull ClassWriter writer, @NotNull Class<?> type) {

        // Define the parse method
        MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "newInstance", "()Ljava/lang/Object;",
                "()L" + Type.getInternalName(type) + ";", null);

        visitor.visitCode();

        if (type.isRecord()) { //Return null

            visitor.visitInsn(Opcodes.ACONST_NULL);
            visitor.visitInsn(Opcodes.ARETURN);

            visitor.visitMaxs(1, 1);
            visitor.visitEnd();

            return;
        }

        visitor.visitTypeInsn(NEW, Type.getInternalName(type));
        visitor.visitInsn(DUP);
        visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(type), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);

        visitor.visitInsn(Opcodes.ARETURN);
        visitor.visitMaxs(2, 1);
        visitor.visitEnd();

    }

    private static void defineWriter(@NotNull ClassWriter writer, @NotNull Class<?> type) {

        var signature = "(L" + Type.getInternalName(type) + ";)Ljava/util/Map<Ljava/lang/String;L" + Type.getInternalName(AttributeValue.class) + ";>;";

        var descriptor = "(L" + Type.getInternalName(type) + ";)Ljava/util/Map;";

        // Define the parse method
        MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "write", descriptor, signature, null);


        visitor.visitCode();

        visitor.visitTypeInsn(NEW, Type.getInternalName(FilteredMap.class));
        visitor.visitInsn(DUP);
        visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(FilteredMap.class), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
        visitor.visitVarInsn(ASTORE, 2);

        var stacks = generateWriteMethods(visitor, type);

        visitor.visitMaxs(stacks + 1, 3);

        visitor.visitVarInsn(ALOAD, 2);
        visitor.visitInsn(Opcodes.ARETURN);

        visitor.visitEnd();


        // Write bridge

        visitor = writer.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC | ACC_BRIDGE, "write",
                "(Ljava/lang/Object;)Ljava/util/Map;", null, null);

        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitVarInsn(ALOAD, 1);
        visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(type));
        visitor.visitMethodInsn(INVOKEVIRTUAL, getParentName(type), "write", descriptor, false);

        visitor.visitInsn(ARETURN);
        visitor.visitMaxs(2, 2);
        visitor.visitEnd();
    }

    @Contract(pure = true)
    private static <T> @NotNull String getParentName(@NotNull Class<T> type) {
        return "org/dooq/engine/" + type.getSimpleName() + "Parser";
    }

    private static int generateWriteMethods(MethodVisitor visitor, @NotNull Class<?> type) {

        var methodMap = Arrays.stream(type.getDeclaredMethods())
                .filter(a -> a.getParameterCount() == 0)
                .collect(Collectors.toMap(method -> method.getName().toLowerCase(), ignored -> ignored));

        if (methodMap.isEmpty()) throw new IllegalStateException("No accessors found for class " + type);

        int stacks = 0;


        for (Field field : type.getDeclaredFields()) {

            if (field.isAnnotationPresent(Transient.class)) continue;
            if (Modifier.isTransient(field.getModifiers())) continue;

            var getMethod = methodMap.get("get" + field.getName().toLowerCase());

            if (field.getType() == boolean.class) {
                getMethod = methodMap.get("is" + field.getName().toLowerCase());
            }

            if (type.isRecord()) {
                getMethod = methodMap.get(field.getName().toLowerCase());
            }

            if (getMethod == null) {
                continue;
            }

            stacks += 2;

            computeWriter(visitor, getColumnName(field), field.getType(), getMethod, type, ReflectionUtils.getGenericType(field));
        }

        return stacks;
    }

    private static String getColumnName(@NotNull Field field) {
        var columnName = field.getName();
        String alias = "";

        if (field.isAnnotationPresent(ColumnAlias.class)) {
            alias = field.getAnnotation(ColumnAlias.class).value();
        }

        if (field.isAnnotationPresent(PartitionKey.class)) {
            alias = field.getAnnotation(PartitionKey.class).alias();
        }

        if (field.isAnnotationPresent(SortKey.class)) {
            alias = field.getAnnotation(SortKey.class).alias();
        }

        if (!alias.isEmpty()) {
            columnName = alias;
        }

        return columnName;
    }

    private static void computeWriter(MethodVisitor visitor, String name,
                                      @NotNull Class<?> valueType, @Nullable Method setMethod,
                                      Class<?> parentType, Class<?> generic) {

        if (valueType.isPrimitive()) {

            if (valueType == int.class) {
                handleWriterMethod(visitor, name, setMethod, "writeInt", parentType);
                return;
            }

            if (valueType == long.class) {
                handleWriterMethod(visitor, name, setMethod, "writeLong", parentType);
                return;
            }

            if (valueType == float.class) {
                handleWriterMethod(visitor, name, setMethod, "writeFloat", parentType);
                return;
            }

            if (valueType == boolean.class) {
                handleWriterMethod(visitor, name, setMethod, "writeBool", parentType);
                return;
            }

            return;
        }

        if (valueType == String.class) {
            handleWriterMethod(visitor, name, setMethod, "writeString", parentType);
            return;
        }


        if (valueType == Integer.class) {
            handleWriterMethod(visitor, name, setMethod, "writeInteger", parentType);
            return;
        }


        if (valueType == Long.class) {
            handleWriterMethod(visitor, name, setMethod, "writeLonger", parentType);
            return;
        }


        if (valueType == Float.class) {
            handleWriterMethod(visitor, name, setMethod, "writeFloater", parentType);
            return;
        }


        if (valueType == Boolean.class) {
            handleWriterMethod(visitor, name, setMethod, "writeBoolean", parentType);
            return;
        }

        if (valueType == UUID.class) {
            handleWriterMethod(visitor, name, setMethod, "writeUUID", parentType);
            return;
        }

        if (valueType == BigDecimal.class) {
            handleWriterMethod(visitor, name, setMethod, "writeBigDecimal", parentType);
            return;
        }

        if (valueType == List.class) {

            if (generic == String.class) {
                handleWriterMethod(visitor, name, setMethod, "writeStringList", parentType);
                return;
            }

            handleGenericWriteMethod(visitor, name, setMethod, "writeList", generic, parentType);
            return;

        }
        if (valueType == Set.class) {

            if (generic == String.class) {
                handleWriterMethod(visitor, name, setMethod, "writeStringSet", parentType);
                return;
            }

            handleGenericWriteMethod(visitor, name, setMethod, "writeSet", generic, parentType);
            return;
        }


        if (valueType == Map.class) {

            handleGenericWriteMethod(visitor, name, setMethod, "writeMap", generic, parentType);
            return;
        }

        if (valueType == LocalDateTime.class) {
            handleWriterMethod(visitor, name, setMethod, "writeLocalDateTime", parentType);
            return;
        }

        if (valueType == LocalDate.class) {
            handleWriterMethod(visitor, name, setMethod, "writeLocalDate", parentType);
            return;
        }

        if (valueType == LocalTime.class) {
            handleWriterMethod(visitor, name, setMethod, "writeLocalTime", parentType);
            return;
        }

        if (!valueType.getName().startsWith("java")) {
            handleGenericWriteMethod(visitor, name, setMethod, "writeComplex", valueType, parentType);
            return;
        }

        System.err.println("Not implemented: " + valueType + " in class " + parentType);

    }

    private static void handleWriterMethod(@NotNull MethodVisitor visitor, String name, @Nullable Method getMethod, String parser, @NotNull Class<?> parent) {

        Objects.requireNonNull(getMethod);

        visitor.visitVarInsn(ALOAD, 2);
        visitor.visitLdcInsn(name);
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitVarInsn(ALOAD, 1);

        visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(getMethod.getDeclaringClass()), getMethod.getName(), Type.getMethodDescriptor(getMethod), false);
        visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ObjectParser.class), parser, Type.getMethodDescriptor(getParserMethod(parser)), false);

        visitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);

        visitor.visitInsn(POP);

    }

    private static void handleGenericWriteMethod(@NotNull MethodVisitor visitor, String name, @Nullable Method getMethod,
                                                 String parser, Class<?> type, @NotNull Class<?> parent) {

        Objects.requireNonNull(getMethod);

        visitor.visitVarInsn(ALOAD, 2);
        visitor.visitLdcInsn(name);
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitVarInsn(ALOAD, 1);

        visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(getMethod.getDeclaringClass()), getMethod.getName(), Type.getMethodDescriptor(getMethod), false);

        visitor.visitLdcInsn(Type.getType(type));

        visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ObjectParser.class), parser, Type.getMethodDescriptor(getParserMethod(parser)), false);
        visitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);

        visitor.visitInsn(POP);
    }

    private static void handleMethod(@NotNull MethodVisitor visitor, String name, @Nullable Method setMethod, String parser, @NotNull Class<?> parent) {

        if (parent.isRecord()) {

            visitor.visitVarInsn(ALOAD, 0);
            visitor.visitVarInsn(ALOAD, 1);
            visitor.visitLdcInsn(name);

            visitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
            visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(AttributeValue.class));
            visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ObjectParser.class), parser, Type.getMethodDescriptor(getParserMethod(parser)), false);

            return;
        }

        Objects.requireNonNull(setMethod);

        visitor.visitVarInsn(ALOAD, 2);
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitVarInsn(ALOAD, 1);

        visitor.visitLdcInsn(name); //Load the key onto the stack

        visitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
        visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(AttributeValue.class));
        visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ObjectParser.class), parser, Type.getMethodDescriptor(getParserMethod(parser)), false);
        visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(setMethod.getDeclaringClass()), setMethod.getName(), Type.getMethodDescriptor(setMethod), false);

        if (setMethod.getReturnType() != void.class) {
            visitor.visitInsn(POP);
        }

    }


    private static void handleGenericMethod(@NotNull MethodVisitor visitor, String name, @Nullable Method setMethod, String parser, Class<?> type, @NotNull Class<?> parent) {


        if (parent.isRecord()) {

            visitor.visitVarInsn(ALOAD, 0);
            visitor.visitVarInsn(ALOAD, 1);
            visitor.visitLdcInsn(name);

            visitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
            visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(AttributeValue.class));

            visitor.visitLdcInsn(Type.getType(type));

            visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ObjectParser.class), parser, Type.getMethodDescriptor(getParserMethod(parser)), false);

            return;
        }

        Objects.requireNonNull(setMethod);

        visitor.visitVarInsn(ALOAD, 2);
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitVarInsn(ALOAD, 1);

        visitor.visitLdcInsn(name);

        visitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
        visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(AttributeValue.class));

        visitor.visitLdcInsn(Type.getType(type));

        visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ObjectParser.class), parser, Type.getMethodDescriptor(getParserMethod(parser)), false);
        visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(setMethod.getDeclaringClass()), setMethod.getName(), Type.getMethodDescriptor(setMethod), false);

        if (setMethod.getReturnType() != void.class) {
            visitor.visitInsn(POP);
        }
    }

    private static void handleComplex(@NotNull MethodVisitor visitor, String name, @Nullable Method setMethod, Class<?> type, @NotNull Class<?> parent) {

        if (parent.isRecord()) {

            visitor.visitVarInsn(ALOAD, 0);
            visitor.visitVarInsn(ALOAD, 1);

            visitor.visitLdcInsn(name);

            visitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
            visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(AttributeValue.class));

            visitor.visitLdcInsn(Type.getType(type));

            visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ObjectParser.class), "parseComplex", Type.getMethodDescriptor(getParserMethod("parseComplex")), false);
            visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(type));

            return;
        }

        Objects.requireNonNull(setMethod);

        visitor.visitVarInsn(ALOAD, 2);
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitVarInsn(ALOAD, 1);

        visitor.visitLdcInsn(name);

        visitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
        visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(AttributeValue.class));

        visitor.visitLdcInsn(Type.getType(type));

        visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ObjectParser.class), "parseComplex", Type.getMethodDescriptor(getParserMethod("parseComplex")), false);
        visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(type));
        visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(setMethod.getDeclaringClass()), setMethod.getName(), Type.getMethodDescriptor(setMethod), false);

        if (setMethod.getReturnType() != void.class) {
            visitor.visitInsn(POP);
        }

    }

    private static void defineParser(@NotNull ClassWriter writer, @NotNull Class<?> type) {

        if (type.isRecord()) {
            defineRecordParser(writer, type);
            return;
        }

        // Define the parse method
        MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "parse", "(Ljava/util/Map;)Ljava/lang/Object;",
                "(Ljava/util/Map<Ljava/lang/String;LAttributeValue;>;)L" + Type.getInternalName(type) + ";", null);

        visitor.visitCode();
        visitor.visitTypeInsn(NEW, Type.getInternalName(type));
        visitor.visitInsn(DUP);
        visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(type), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
        visitor.visitVarInsn(ASTORE, 2);

        var stacks = generateMethods(visitor, type);

        visitor.visitMaxs(stacks + 1, 3);


        visitor.visitVarInsn(ALOAD, 2);
        visitor.visitInsn(Opcodes.ARETURN);
        visitor.visitEnd();

    }

    private static void defineRecordParser(@NotNull ClassWriter writer, Class<?> type) {
        // Define the parse method
        MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "parse", "(Ljava/util/Map;)Ljava/lang/Object;",
                "(Ljava/util/Map<Ljava/lang/String;LAttributeValue;>;)L" + Type.getInternalName(type) + ";", null);

        visitor.visitCode();
        visitor.visitTypeInsn(NEW, Type.getInternalName(type));
        visitor.visitInsn(DUP);

        var stacks = generateMethods(visitor, type);


        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('(');

        for (RecordComponent recordComponent : type.getRecordComponents()) {
            stringBuilder.append(Type.getType(recordComponent.getType()));
        }

        stringBuilder.append(")V");

        visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(type), "<init>", stringBuilder.toString(), false);
        visitor.visitInsn(Opcodes.ARETURN);

        visitor.visitMaxs(stacks + 1, 3);
        visitor.visitEnd();
    }

    private static void defineConstructor(@NotNull ClassWriter writer) {
        MethodVisitor constructorMv = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructorMv.visitVarInsn(Opcodes.ALOAD, 0);
        constructorMv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(ObjectParser.class), "<init>", "()V", false); // Call the superclass constructor
        constructorMv.visitInsn(Opcodes.RETURN);
        constructorMv.visitMaxs(1, 1);
        constructorMv.visitEnd();
    }

    private static int generateMethods(MethodVisitor visitor, @NotNull Class<?> type) {

        var methodMap = Arrays.stream(type.getDeclaredMethods())
                .filter(a -> a.getParameterCount() == 1)
                .collect(Collectors.toMap(method -> method.getName().toLowerCase(), ignored -> ignored));

        if (methodMap.isEmpty()) throw new IllegalStateException("No modifiers found for class " + type);

        int stacks = 0;


        if (type.isRecord()) {

            for (RecordComponent component : type.getRecordComponents()) {
                compute(visitor, component.getName(), component.getType(), null, type, ReflectionUtils.getGenericType(component.getGenericType()));


                stacks += 2;
            }

        } else {

            for (Field field : type.getDeclaredFields()) {

                if (field.isAnnotationPresent(Transient.class)) continue;
                if (Modifier.isTransient(field.getModifiers())) continue;

                var setMethod = methodMap.get("set" + field.getName().toLowerCase());

                if (setMethod == null) {
                    continue;
                }

                //TODO handle autoboxing
                if (setMethod.getParameterTypes()[0] != field.getType()) {
                    throw new IllegalStateException("Incorrect mutator parameter type: '%s' expected '%s' from field"
                            .formatted(setMethod.getParameterTypes()[0], field.getType()));
                }

                stacks += 2;

                compute(visitor, getColumnName(field), field.getType(), setMethod, type, ReflectionUtils.getGenericType(field));
            }
        }

        return stacks;
    }


    private static void compute(MethodVisitor visitor, String name,
                                @NotNull Class<?> valueType, @Nullable Method setMethod,
                                Class<?> parentType, Class<?> generic) {

        if (valueType.isPrimitive()) {

            if (valueType == int.class) {
                handleMethod(visitor, name, setMethod, "parseInt", parentType);
                return;
            }

            if (valueType == long.class) {
                handleMethod(visitor, name, setMethod, "parseLong", parentType);
                return;
            }

            if (valueType == float.class) {
                handleMethod(visitor, name, setMethod, "parseFloat", parentType);
                return;
            }

            if (valueType == boolean.class) {
                handleMethod(visitor, name, setMethod, "parseBool", parentType);
                return;
            }

            return;
        }

        if (valueType == String.class) {
            handleMethod(visitor, name, setMethod, "parseString", parentType);
            return;
        }


        if (valueType == Integer.class) {
            handleMethod(visitor, name, setMethod, "parseInteger", parentType);
            return;
        }


        if (valueType == Long.class) {
            handleMethod(visitor, name, setMethod, "parseLonger", parentType);
            return;
        }


        if (valueType == Float.class) {
            handleMethod(visitor, name, setMethod, "parseFloater", parentType);
            return;
        }


        if (valueType == Boolean.class) {
            handleMethod(visitor, name, setMethod, "parseBoolean", parentType);
            return;
        }

        if (valueType == UUID.class) {
            handleMethod(visitor, name, setMethod, "parseUUID", parentType);
            return;
        }

        if (valueType == BigDecimal.class) {
            handleMethod(visitor, name, setMethod, "parseBigDecimal", parentType);
            return;
        }

        if (valueType == List.class) {

            if (generic == String.class) {
                handleMethod(visitor, name, setMethod, "parseStringList", parentType);
                return;
            }

            handleGenericMethod(visitor, name, setMethod, "parseList", generic, parentType);
            return;

        }
        if (valueType == Set.class) {

            if (generic == String.class) {
                handleMethod(visitor, name, setMethod, "parseStringSet", parentType);
                return;
            }

            handleGenericMethod(visitor, name, setMethod, "parseSet", generic, parentType);
            return;
        }


        if (valueType == Map.class) {

            handleGenericMethod(visitor, name, setMethod, "parseMap", generic, parentType);
            return;
        }

        if (valueType == LocalDateTime.class) {
            handleMethod(visitor, name, setMethod, "parseLocalDateTime", parentType);
            return;
        }

        if (valueType == LocalDate.class) {
            handleMethod(visitor, name, setMethod, "parseLocalDate", parentType);
            return;
        }

        if (valueType == LocalTime.class) {
            handleMethod(visitor, name, setMethod, "parseLocalTime", parentType);
            return;
        }


        if (!valueType.getName().startsWith("java")) {
            handleComplex(visitor, name, setMethod, valueType, parentType);
            return;
        }

        System.err.println("Not implemented: " + valueType);
    }

    static Map<String, Method> parserMethodMap = new HashMap<>();

    private static Method getParserMethod(String name) {

        if (parserMethodMap.isEmpty()) {
            for (Method declaredMethod : ObjectParser.class.getDeclaredMethods()) {
                parserMethodMap.put(declaredMethod.getName(), declaredMethod);
            }
        }

        return Objects.requireNonNull(parserMethodMap.get(name), "Missing parse method: " + name);
    }


    @SuppressWarnings("unchecked")
    private static <T> @NotNull ObjectParser<T> createObject(byte[] bytecode, @NotNull Class<T> type) {
        var clazz = INSTANCE.defineNewClass(bytecode, type.getSimpleName() + "Parser");

        try {
            var constructor = clazz.getConstructor();

            return (ObjectParser<T>) constructor.newInstance();
        } catch (VerifyError | Exception ex) {
            throw new RuntimeException("Failed to compile parser for class '%s'".formatted(type), ex);
        }
    }

}

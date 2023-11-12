package org.dooq.processor;

import org.dooq.api.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

@SupportedAnnotationTypes("org.dooq.api.DynamoDBTable")
public class TableAnnotationProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        List<Table> tables = Collections.emptyList();

        for (TypeElement annotation : annotations) {
            if (annotation.toString().equals("org.dooq.api.DynamoDBTable")) {
                tables = roundEnv.getElementsAnnotatedWith(annotation)
                        .stream()
                        .map(this::process)
                        .filter(Objects::nonNull)
                        .toList();

            }
        }

        if (!tables.isEmpty()) {

            var element = tables.get(0).element();

            final var pack = processingEnv.getElementUtils()
                    .getPackageOf(element)
                    .toString();

            var code = generateTables(pack, tables);


            writeOutClass(pack, "Tables", code, element);
        }

        return false;
    }

    private boolean ignorable(Set<Modifier> modifiers) {

        for (Modifier modifier : modifiers) {
            if (modifier == Modifier.FINAL) return true;
            if (modifier == Modifier.STATIC) return true;
        }

        return false;
    }

    private Table process(Element element) {

        if (element.getKind() != ElementKind.CLASS) {
            return null;
        }

        System.out.println("processing ->" + element);

        final var pack = processingEnv.getElementUtils()
                .getPackageOf(element)
                .toString();

        var annot = element.getAnnotation(DynamoDBTable.class);

        var localIndices = element.getAnnotation(LocalIndices.class);
        var globalIndices = element.getAnnotation(GlobalIndices.class);


        var builder = new StringBuilder();

        builder.append("package ")
                .append(pack)
                .append(";\n\n");

        builder.append("import org.dooq.api.*;\n")
                .append("import org.dooq.Key;\n")
                .append("import org.dooq.core.schema.Index;\n\n")
                .append("import java.util.List;\n")
                .append("import java.util.Map;\n")
                .append("\n\n");

        final var tableName = annot.value();
        final var recordName = tableName + "Record";
        final var keyName = tableName + "Key";

        builder.append("@javax.annotation.processing.Generated(value = \"org.dooq.processor.TableAnnotationProcessor\",\n\t date = \"%s\")"
                        .formatted(LocalDateTime.now()))
                .append("\n");

        builder.append("public final class ")
                .append(tableName)
                .append(" implements Table<%s, %s>".formatted(recordName, keyName))
                .append(" {\n\n");

        builder.append("\tpublic ")
                .append(tableName)
                .append("() {\n")
                .append("\t\tCOLUMNS = columns();\n");

        //TODO build indices

        builder.append("\t}\n\n");


        List<? extends Element> enclosedElements = element.getEnclosedElements();

        var partitionColumn = "";
        var sortColumn = "";

        VariableElement partition = null;
        VariableElement sort = null;

        // Process only the fields within the enclosed elements
        for (Element enclosedElement : enclosedElements) {
            if (enclosedElement instanceof VariableElement fieldElement) {
                // Process the field element

                if (ignorable(fieldElement.getModifiers())) continue;

                if (fieldElement.getAnnotation(org.dooq.api.PartitionKey.class) != null) {
                    partitionColumn = fieldElement.getSimpleName().toString().toUpperCase();
                    partition = fieldElement;
                }

                if (fieldElement.getAnnotation(org.dooq.api.SortKey.class) != null) {
                    sortColumn = fieldElement.getSimpleName().toString().toUpperCase();
                    sort = fieldElement;
                }

                var columnName = fieldElement.getSimpleName().toString();

                if (fieldElement.getAnnotation(org.dooq.api.ColumnAlias.class) != null) {
                    columnName = fieldElement.getAnnotation(org.dooq.api.ColumnAlias.class).value();
                }

                builder.append("\tpublic final Field<")
                        .append(fieldElement.asType().toString())
                        .append(", ")
                        .append(recordName)
                        .append(", ")
                        .append(keyName)
                        .append("> ")
                        .append(fieldElement.getSimpleName().toString().toUpperCase())
                        .append(" = FieldBuilder.of(\"")
                        .append(columnName)
                        .append("\", ")
                        .append(fieldElement.asType().toString())
                        .append(".class, this);\n");

            }
        }


        builder.append("\n\n")
                .append("\tprivate final List<Column<%s, %s>> COLUMNS;"
                        .formatted(recordName, keyName));

        if (globalIndices != null && localIndices != null) {
            builder.append("\n\n")
                    .append("private final Map<String, Index> INDICES;");
        }

        builder.append("\n\n")
                .append("""
                            @Override
                            public List<Column<%s, %s>> getColumns() {
                                return COLUMNS;
                            }
                        """.formatted(recordName, keyName));

        builder.append("\n\n")
                .append("""
                            @Override
                            public Column<%s, %s> getPartitionColumn() {
                                return %s;
                            }
                        """.formatted(recordName, keyName, partitionColumn));

        builder.append("\n\n")
                .append("""
                            @Override
                            public Column<%s, %s> getSortColumn() {
                                return %s;
                            }
                        """.formatted(recordName, keyName, sortColumn));

        builder.append("\n\n")
                .append("""
                            @Override
                            public Class<%s> getRecordType() {
                                return %s.class;
                            }
                        """.formatted(recordName, recordName));

        builder.append("\n\n")
                .append("""
                            @Override
                            public String getTableName() {
                                return "%s";
                            }
                        """.formatted(tableName));


        if (globalIndices != null || localIndices != null) {

            builder.append("\n\n")
                    .append("""
                            @Override
                            public Map<String, Index> getIndices() {
                                return INDICES;
                            }
                            """);
        }

        builder.append("\n}");


        var clazz = builder.toString();
        var key = generateKeyClass(pack, element, Objects.requireNonNull(partition), sort);
        var record = generateRecordClass(pack, element, partition, sort);

        System.out.println(clazz);
        System.out.println(key);
        System.out.println(record);

        writeOutClass(pack, tableName, clazz, element);
        writeOutClass(pack, tableName + "Key", key, element);
        writeOutClass(pack, tableName + "Record", record, element);

        return new Table(tableName, partitionColumn, element);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private String generateRecordClass(String pack, Element element, VariableElement partition, VariableElement sort) {
        var builder = new StringBuilder();

        builder.append("package ")
                .append(pack)
                .append(";\n\n");

        var annot = element.getAnnotation(DynamoDBTable.class);

        final var className = annot.value() + "Record";

        builder.append("import org.dooq.api.*;\n")
                .append("import org.dooq.Key;\n\n")
                .append("@javax.annotation.processing.Generated(value = \"org.dooq.processor.TableAnnotationProcessor\", date = \"%s\")\n"
                        .formatted(LocalDateTime.now()))
                .append("@DynamoDBTable(\"%s\")\n".formatted(annot.value()))
                .append("public class ")
                .append(className)
                .append(" extends AbstractRecord<%s> {\n\n".formatted(className));

        List<? extends Element> enclosedElements = element.getEnclosedElements();

        var setBuilder = new StringBuilder();
        var getBuilder = new StringBuilder();

        // Process only the fields within the enclosed elements
        for (Element enclosedElement : enclosedElements) {
            if (enclosedElement instanceof VariableElement fieldElement) {

                var partitionAnnot = fieldElement.getAnnotation(PartitionKey.class);
                var sortAnnot = fieldElement.getAnnotation(SortKey.class);

                if (partitionAnnot != null) {
                    if (!partitionAnnot.alias().isEmpty()) {
                        builder.append("\t@PartitionKey(alias = \"%s\")\n".formatted(partitionAnnot.alias()));
                    } else {
                        builder.append("\t@PartitionKey\n");
                    }
                }

                if (sortAnnot != null) {
                    if (!sortAnnot.alias().isEmpty()) {
                        builder.append("\t@SortKey(alias = \"%s\")\n".formatted(sortAnnot.alias()));
                    } else {
                        builder.append("\t@SortKey\n");
                    }
                }

                if (fieldElement.getAnnotation(org.dooq.api.ColumnAlias.class) != null) {
                    builder.append("\t@ColumnAlias(\"%s\")\n".formatted(fieldElement.getAnnotation(org.dooq.api.ColumnAlias.class).value()));
                }

                builder.append("\tprivate ")
                        .append(fieldElement.asType().toString())
                        .append(" ")
                        .append(fieldElement.getSimpleName().toString())
                        .append(";\n");

                var methodName = fieldElement.getSimpleName()
                        .toString();

                methodName = methodName.substring(0, 1).toUpperCase() +
                        methodName.substring(1);

                setBuilder.append("\tpublic void set")
                        .append(methodName)
                        .append("(")
                        .append(fieldElement.asType().toString())
                        .append(" ")
                        .append(fieldElement.getSimpleName().toString())
                        .append(") {\n")
                        .append("\t\tthis.")
                        .append(fieldElement.getSimpleName().toString())
                        .append(" = ")
                        .append(fieldElement.getSimpleName().toString())
                        .append(";\n")
                        .append("\t}\n\n");

                getBuilder.append("\t\tpublic ")
                        .append(fieldElement.asType().toString())
                        .append(" get")
                        .append(methodName)
                        .append("() {\n")
                        .append("\t\t\t return this.")
                        .append(fieldElement.getSimpleName().toString())
                        .append(";\n")
                        .append("\t\t}\n\n");

            }
        }

        builder.append("\npublic ")
                .append(className)
                .append("() {\n")
                .append("}\n\n");

        builder.append("""
                @Override
                public Key getKey() {
                """);

        if (sort != null) {
            builder.append("\treturn %s.of(%s, %s);"
                    .formatted(annot.value() + "Key", partition.getSimpleName(), sort.getSimpleName()));

        } else {
            builder.append("\treturn %s.of(%s);"
                            .formatted(annot.value() + "Key", partition.getSimpleName()))
                    .append("\n");
        }

        builder.append("\n}\n\n");

        builder.append("\n")
                .append(getBuilder);
        builder.append("\n")
                .append(setBuilder);

        builder.append("}");

        return builder.toString();
    }

    private String generateKeyClass(String pack, Element element, VariableElement partition, VariableElement sort) {
        var builder = new StringBuilder();

        builder.append("package ")
                .append(pack)
                .append(";\n\n");
        /*

        public class ProductKey extends Key {

    public static @NotNull ProductKey of(long contentId, String productUuid) {
        var map = new ProductKey();

        map.setPartitionKey(Tables.PRODUCT.CONTENTID, contentId);
        map.setSortingKey(Tables.PRODUCT.UUID, productUuid);

        return map;
    }
         */

        var annot = element.getAnnotation(DynamoDBTable.class);

        final var className = annot.value() + "Key";

        builder.append("import org.dooq.api.*;\n")
                .append("import org.dooq.Key;\n\n")
                .append("@javax.annotation.processing.Generated(value = \"org.dooq.processor.TableAnnotationProcessor\", date = \"%s\")\n"
                        .formatted(LocalDateTime.now()))
                .append("public class ")
                .append(className)
                .append(" extends Key {\n\n");

        builder.append("public static ")
                .append(className)
                .append(" of(")
                .append(partition.asType().toString())
                .append(" ")
                .append(partition.getSimpleName().toString());


        if (sort != null) {
            builder.append(", ")
                    .append(sort.asType().toString())
                    .append(" ")
                    .append(sort.getSimpleName().toString());
        }

        builder.append(") {\n")
                .append("\tvar map = new ")
                .append(className)
                .append("();\n\n");

        builder.append("\tmap.setPartitionKey(Tables.")
                .append(annot.value().toUpperCase())
                .append(".")
                .append(partition.getSimpleName().toString().toUpperCase())
                .append(", ")
                .append(partition.getSimpleName().toString())
                .append(");\n");

        if (sort != null) {
            builder.append("\tmap.setSortingKey(Tables.")
                    .append(annot.value().toUpperCase())
                    .append(".")
                    .append(sort.getSimpleName().toString().toUpperCase())
                    .append(", ")
                    .append(sort.getSimpleName().toString())
                    .append(");\n\n");
        }

        builder.append("\treturn map;\n")
                .append("}\n\n");

        builder.append("}");


        return builder.toString();
    }

    private String generateTables(String pack, List<Table> tables) {

        var builder = new StringBuilder();

        builder.append("package ")
                .append(pack)
                .append(";\n\n")
                .append("@javax.annotation.processing.Generated(value = \"org.dooq.processor.TableAnnotationProcessor\", date = \"%s\")\n"
                        .formatted(LocalDateTime.now()))
                .append("public class Tables {\n\n");

        for (Table table : tables) {
            builder.append("\tpublic static final ")
                    .append(table.name())
                    .append(" ")
                    .append(table.name().toUpperCase())
                    .append(" = new ")
                    .append(table.name())
                    .append("();\n");
        }

        builder.append("\n}");

        return builder.toString();

    }

    /**
     * Write out the generated code
     */
    private void writeOutClass(String packageName, String className, String code, Element element) {

        final var filer = processingEnv.getFiler();

        try {

            var source = filer
                    .createSourceFile(packageName + "." + className, element);

            try (Writer writer = source.openWriter()) {
                writer.write(code);
            }

        } catch (Exception ex) {

            if (ex instanceof javax.annotation.processing.FilerException ignored) {
                Logger.getLogger(TableAnnotationProcessor.class.getName())
                        .info("File already exists, skipping");
                return;
            }

            throw new RuntimeException(ex);

        }

    }


    record Table(String name, String firstField, Element element) {
    }

}
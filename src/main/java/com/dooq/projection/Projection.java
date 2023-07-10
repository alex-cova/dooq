package com.dooq.projection;

public class Projection {

    public record ProjectionResult1<V1>(V1 value1) {
    }

    public record ProjectionResult2<V1, V2>(V1 value1, V2 value2) {
    }

    public record ProjectionResult3<V1, V2, V3>(V1 value1, V2 value2, V3 value3) {
    }

    public record ProjectionResult4<V1, V2, V3, V4>(V1 value1, V2 value2, V3 value3, V4 value4) {
    }

    public record ProjectionResult5<V1, V2, V3, V4, V5>(V1 value1, V2 value2, V3 value3, V4 value4, V5 value5) {
    }

    public record ProjectionResult6<V1, V2, V3, V4, V5, V6>(V1 value1, V2 value2, V3 value3, V4 value4, V5 value5,
                                                            V6 value6) {
    }

    public record ProjectionResult7<V1, V2, V3, V4, V5, V6, V7>(V1 value1, V2 value2, V3 value3, V4 value4, V5 value5,
                                                                V6 value6, V7 value7) {
    }

    public record ProjectionResult8<V1, V2, V3, V4, V5, V6, V7, V8>(V1 value1, V2 value2, V3 value3, V4 value4,
                                                                    V5 value5,
                                                                    V6 value6, V7 value7, V8 value8) {
    }

    public record ProjectionResult9<V1, V2, V3, V4, V5, V6, V7, V8, V9>(V1 value1, V2 value2, V3 value3, V4 value4,
                                                                        V5 value5,
                                                                        V6 value6, V7 value7, V8 value8, V9 value9) {
    }

    public record ProjectionResult10<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10>(V1 value1, V2 value2, V3 value3,
                                                                              V4 value4,
                                                                              V5 value5, V6 value6, V7 value7,
                                                                              V8 value8,
                                                                              V9 value9, V10 value10) {
    }

}

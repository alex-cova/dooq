package com.dooq.core;

import com.dooq.Key;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

public record ParseResult(Map<String, AttributeValue> map, Key key) {

}

package com.dooq;

import org.jetbrains.annotations.NotNull;

public class MixerKey extends Key {

    public static @NotNull MixerKey of(String parentUuid, String uuid) {
        var key = new MixerKey();

        key.setPartitionKey(Tables.MIXER.PARENTUUID, parentUuid);
        key.setSortingKey(Tables.MIXER.UUID, uuid);

        return key;
    }
}

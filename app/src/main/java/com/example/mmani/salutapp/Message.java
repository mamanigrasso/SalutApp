package com.example.mmani.salutapp;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class Message {

    @JsonField
    public String description;

    @Override
    public String toString() {
        return description;
    }
}

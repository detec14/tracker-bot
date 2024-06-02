package com.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImageServiceResponse {
    @JsonProperty("image_name")
    String imageName;

    public ImageServiceResponse() {
        this.imageName = null;
    }

    public String getImageName() {
        return this.imageName;
    }
}

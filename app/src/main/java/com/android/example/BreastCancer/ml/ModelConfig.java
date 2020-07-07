//This file is to config model,
package com.android.example.BreastCancer.ml;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModelConfig {

//    public static final String MODEL_FILENAME = "2_July.tflite"; // Binary
    public static final String MODEL_FILENAME = "3_July.tflite"; // 3 Way

    public static final int INPUT_IMG_SIZE_WIDTH = 224, INPUT_IMG_SIZE_HEIGHT = 224; //Input shape that model takes
    private static final int FLOAT_TYPE_SIZE = 4, PIXEL_SIZE = 3;
    static final int MAX_CLASSIFICATION_RESULTS = 1;

    static final int MODEL_INPUT_SIZE = FLOAT_TYPE_SIZE * INPUT_IMG_SIZE_WIDTH * INPUT_IMG_SIZE_HEIGHT * PIXEL_SIZE;
    //List of labels (all the possible classes), my model carries out Binary Classification (0: Benign, 1: Malignant)
//    static final List<String> OUTPUT_LABELS = Collections.unmodifiableList(Arrays.asList("Benign",
//            "Malignant"));

    //Three way
    static final List<String> OUTPUT_LABELS = Collections.unmodifiableList(Arrays.asList("Benign",
            "Malignant", "None"));

    //Here you can tweak this Threshold, my model classifies image as Malignant when Confidence is > 0.6
    static final float CLASSIFICATION_THRESHOLD = 0.3f, IMAGE_STD = 255.0f;

}

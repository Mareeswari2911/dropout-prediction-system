package org.icbtcn.dropoutprediction.controller;
import org.icbtcn.dropoutprediction.model.StudentFeatures;
import org.icbtcn.dropoutprediction.util.KDDLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
@RestController
public class DataController {
    @GetMapping("/students")
    public List<StudentFeatures> getStudents() throws IOException {
        return KDDLoader.loadAndPreprocess(
                        "D:/mini project/dataset/train/train/enrollment_train.csv",
                        "D:/mini project/dataset/train/train/log_train.csv",
                        "D:/mini project/dataset/train/train/truth_train.csv"
                );

    }
}

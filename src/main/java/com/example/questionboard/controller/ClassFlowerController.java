package com.example.questionboard.controller;

import com.example.questionboard.dto.ClassFlowerResponse;
import com.example.questionboard.dto.ClassFlowerUpdateRequest;
import com.example.questionboard.repository.ClassFlowerRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/class-flowers")
public class ClassFlowerController {

    private final ClassFlowerRepository classFlowerRepository;

    public ClassFlowerController(ClassFlowerRepository classFlowerRepository) {
        this.classFlowerRepository = classFlowerRepository;
    }

    @GetMapping
    public List<ClassFlowerResponse> getAllClassFlowers() {
        return classFlowerRepository.findAll();
    }

    @PatchMapping("/{className}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateClassFlower(
            @PathVariable String className,
            @RequestBody ClassFlowerUpdateRequest request
    ) {
        classFlowerRepository.upsert(className, request.flowerType());
    }
}

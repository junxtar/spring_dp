package com.example.dp.domain.category.controller;

import com.example.dp.domain.category.dto.request.CategoryRequestDto;
import com.example.dp.domain.category.dto.response.CategoryResponseDto;
import com.example.dp.domain.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/cattegories")
    public ResponseEntity<CategoryResponseDto> createCategory(
        @RequestBody CategoryRequestDto requestDto) {
        CategoryResponseDto responseDto = categoryService.createCategory(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}

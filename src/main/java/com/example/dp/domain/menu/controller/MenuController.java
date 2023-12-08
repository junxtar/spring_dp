package com.example.dp.domain.menu.controller;

import com.example.dp.domain.menu.dto.response.MenuSimpleResponseDto;
import com.example.dp.domain.menu.service.MenuService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<List<MenuSimpleResponseDto>> getAllMenus(
        @RequestParam(name = "category", defaultValue = "") String categoryType,
        @RequestParam(name = "name", defaultValue = "") String menuName) {
        List<MenuSimpleResponseDto> responseDto = menuService.getMenus(categoryType, menuName);
        return ResponseEntity.ok(responseDto);
    }
}

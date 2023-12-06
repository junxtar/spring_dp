package com.example.dp.domain.menu.service;

import com.example.dp.domain.menu.dto.response.MenuDetailResponseDto;
import com.example.dp.domain.menu.dto.request.MenuRequestDto;

public interface MenuService {

    MenuDetailResponseDto createMenu(MenuRequestDto menuRequestDto);

    MenuDetailResponseDto updateMenu(Long menuId, MenuDetailResponseDto menuDetailResponseDto);

    void deleteMenu(Long menuId);

    MenuDetailResponseDto getAdminMenu(Long menuId);

    MenuDetailResponseDto getAdminMenus();
}

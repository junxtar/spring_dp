package com.example.dp.domain.admin.service.impl;

import com.example.dp.domain.admin.service.AdminMenuService;
import com.example.dp.domain.category.entity.Category;
import com.example.dp.domain.category.exception.CategoryErrorCode;
import com.example.dp.domain.category.exception.NotFoundCategoryException;
import com.example.dp.domain.category.repository.CategoryRepository;
import com.example.dp.domain.menu.dto.request.MenuRequestDto;
import com.example.dp.domain.menu.dto.response.MenuDetailResponseDto;
import com.example.dp.domain.menu.entity.Menu;
import com.example.dp.domain.menu.exception.ExistsMenuNameException;
import com.example.dp.domain.menu.exception.ForbiddenUpdateMenuException;
import com.example.dp.domain.menu.exception.InvalidInputException;
import com.example.dp.domain.menu.exception.MenuErrorCode;
import com.example.dp.domain.menu.exception.NotFoundMenuException;
import com.example.dp.domain.menu.repository.MenuRepository;
import com.example.dp.domain.menucategory.entity.MenuCategory;
import com.example.dp.domain.menucategory.repository.MenuCategoryRepository;
import com.example.dp.global.s3.AwsS3Util;
import com.example.dp.global.s3.AwsS3Util.ImagePath;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AdminMenuServiceImpl implements AdminMenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final AwsS3Util awsS3Util;

    @Transactional
    @Override
    public MenuDetailResponseDto createMenu(final MultipartFile multipartFile,
        final MenuRequestDto requestDto) throws IOException {

        if (menuRepository.existsByName(requestDto.getName())) {
            throw new ExistsMenuNameException(MenuErrorCode.EXISTS_MENU_NAME);
        }

        String imageName = awsS3Util.uploadImage(multipartFile, ImagePath.MENU);

        Menu menu = Menu.builder()
            .name(requestDto.getName())
            .description(requestDto.getDescription())
            .price(requestDto.getPrice())
            .quantity(requestDto.getQuantity())
            .imageName(imageName)
            .status(requestDto.getStatus())
            .build();

        menu = menuRepository.save(menu);

        addCategory(requestDto.getCategoryNameList(), menu);

        return new MenuDetailResponseDto(menu);
    }

    @Override
    @Transactional
    public MenuDetailResponseDto updateMenu(
        final Long menuId,
        final MenuRequestDto menuRequestDto) {
        Menu menu = findMenu(menuId);

        List<String> requestedCategories = menuRequestDto.getCategoryNameList();

        if (requestedCategories.isEmpty()) {
            throw new ForbiddenUpdateMenuException(MenuErrorCode.NOT_ENTER_CATEGORY);
        }

        List<String> currentCategories = menu.getMenuCategoryList().stream()
            .map(menuCategory -> menuCategory.getCategory().getType())
            .toList();

        List<String> categoriesToRemove = currentCategories.stream()
            .filter(category -> !requestedCategories.contains(category))
            .toList();
        removeCategory(categoriesToRemove, menu);

        List<String> categoriesToAdd = requestedCategories.stream()
            .filter(category -> !currentCategories.contains(category))
            .toList();
        addCategory(categoriesToAdd, menu);

        if (!menu.getName().equals(menuRequestDto.getName())
            && menuRepository.existsByName(menuRequestDto.getName())) {
            throw new ExistsMenuNameException(MenuErrorCode.EXISTS_MENU_NAME);
        }

        menu.update(menuRequestDto.getName(),
            menuRequestDto.getDescription(), menuRequestDto.getPrice(),
            menuRequestDto.getQuantity(), menuRequestDto.getStatus());

        return new MenuDetailResponseDto(menu);
    }

    @Override
    public void deleteMenu(final Long menuId) {
        Menu menu = findMenu(menuId);
        menuRepository.delete(menu);
    }

    @Override
    public MenuDetailResponseDto getAdminMenu(final Long menuId) {
        Menu menu = findMenu(menuId);
        return new MenuDetailResponseDto(menu);
    }

    @Override
    public List<MenuDetailResponseDto> getAdminMenus(String sort) {
        List<Menu> menus = menuRepository.findByOrderByCreatedAt();
        return filtering(menus,sort).map(MenuDetailResponseDto::new).toList();
    }

    private void addCategory(List<String> categoryNameList, Menu menu) {
        List<Category> categories = categoryRepository.findByTypeIn(categoryNameList);

        if (categoryNameList.size() != categories.size()) {
            throw new NotFoundCategoryException(CategoryErrorCode.NOT_FOUND_CATEGORY);
        }

        categories.forEach(category -> {
            MenuCategory menuCategory = MenuCategory.builder()
                .category(category)
                .menu(menu)
                .build();
            menu.addMenuCategory(menuCategory);
        });
    }

    private void removeCategory(List<String> categoryNameList, Menu menu) {
        List<MenuCategory> menuCategories = menuCategoryRepository.findByMenuAndCategory_TypeIn(
            menu, categoryNameList);

        menuCategories.forEach(menuCategory -> {
            menu.removeCategory(menuCategory);
            menuCategoryRepository.delete(menuCategory);
        });
    }

    public Menu findMenu(Long menuId) {
        return menuRepository.findById(menuId)
            .orElseThrow(() -> new NotFoundMenuException(MenuErrorCode.NOT_FOUND_MENU));
    }

    private Stream<Menu> filtering(List<Menu> menus, String sort) {
        if (sort.equals("recent")) {
            return menus.stream();
        } else if (sort.equals("likes")) {
            return menus.stream()
                .sorted((menu1, menu2) -> Integer.compare(menu2.getLikeCounts(),
                    menu1.getLikeCounts()));
        } else
            throw new InvalidInputException(MenuErrorCode.INVALID_INPUT);
    }

}

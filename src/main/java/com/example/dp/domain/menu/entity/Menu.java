package com.example.dp.domain.menu.entity;

import com.example.dp.domain.menu.dto.request.MenuRequestDto;
import com.example.dp.domain.menucategory.entity.MenuCategory;
import com.example.dp.domain.model.TimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_MENU")
public class Menu extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Boolean status;

    @OneToMany(mappedBy = "menu")
    private final List<MenuCategory> menuCategoryList = new ArrayList<>();

    @Builder
    private Menu(String name, String description, Integer price, Integer quantity, Boolean status) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.status = status;
    }

    public void update(final MenuRequestDto menuRequestDto) {
        this.name = menuRequestDto.getName();
        this.description = menuRequestDto.getDescription();
        this.price = menuRequestDto.getPrice();
        this.quantity = menuRequestDto.getQuantity();
        this.status = menuRequestDto.getStatus();
    }

    public void addMenuCategory(MenuCategory menuCategory) {
        this.menuCategoryList.add(menuCategory);
        menuCategory.setMenu(this);
    }
}

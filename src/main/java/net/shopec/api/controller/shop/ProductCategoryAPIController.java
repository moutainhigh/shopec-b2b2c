package net.shopec.api.controller.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.shopec.Setting;
import net.shopec.api.model.ApiResult;
import net.shopec.api.util.ResultUtils;
import net.shopec.entity.ProductCategory;
import net.shopec.service.ProductCategoryService;
import net.shopec.util.SystemUtils;

/**
 * 商品分类 - 接口类
 */
@RestController
@RequestMapping("/api/product_category")
public class ProductCategoryAPIController {

	@Inject
	private ProductCategoryService productCategoryService;

	/**
	 * 首页
	 */
	@GetMapping("/index")
	public ApiResult index() {
		Map<String, Object> data = new HashMap<>();
		List<Map<String, Object>> list = new ArrayList<>();
		for (ProductCategory productCategory : productCategoryService.findRoots()) {
			Map<String, Object> category = new HashMap<>();
			category.put("id", String.valueOf(productCategory.getId()));
			category.put("name", productCategory.getName());
			
			List<Map<String, Object>> items = new ArrayList<>();
			Set<ProductCategory> childrens = productCategory.getChildren();
			Setting setting = SystemUtils.getSetting();
			for (ProductCategory children : childrens) {
				Map<String, Object> item = new HashMap<>();
				item.put("id", String.valueOf(children.getId()));
				item.put("name", children.getName());
				item.put("image", StringUtils.isEmpty(children.getImage()) ? setting.getSiteUrl() + setting.getDefaultThumbnailProductImage() : setting.getSiteUrl() + children.getImage());
				items.add(item);
			}
			category.put("childrens", items);
			list.add(category);
		}
		data.put("list", list);
		return ResultUtils.ok(data);
	}

	

}

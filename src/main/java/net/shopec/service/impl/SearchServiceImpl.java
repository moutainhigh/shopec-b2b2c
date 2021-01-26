package net.shopec.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import net.shopec.entity.Article;
import net.shopec.entity.Product;
import net.shopec.entity.Sku;
import net.shopec.entity.Store;
import net.shopec.repository.ArticleRepository;
import net.shopec.repository.ProductRepository;
import net.shopec.repository.StoreRepository;
import net.shopec.service.ArticleService;
import net.shopec.service.ProductService;
import net.shopec.service.SearchService;
import net.shopec.service.SkuService;
import net.shopec.service.StoreService;

/**
 * Service - 搜索
 * 
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SearchServiceImpl implements SearchService {

	@Inject
	private ArticleRepository articleRepository;
	@Inject
	private StoreRepository storeRepository;
	@Inject
	private ProductRepository productRepository;
	@Inject
	private ArticleService articleService;
	@Inject
	private ProductService productService;
	@Inject
	private StoreService storeService;
	@Inject
	private SkuService skuService;

	@Override
	public void index(Class<?> type) {
		index(type, true);
	}

	@Override
	public void index(Class<?> type, boolean purgeAll) {
		// 清除所有
		productRepository.deleteAll();
		// 文章
		if (type.isAssignableFrom(Article.class)) {
			QueryWrapper<Article> queryArticle = new QueryWrapper<>();
			queryArticle.eq("is_publication", true);
			List<Article> articles = articleService.list(queryArticle);
			articleRepository.saveAll(articles);
		}
		
		// 商品
		if (type.isAssignableFrom(Product.class)) {
			// 清除所有
			productRepository.deleteAll();
			// 查询符合条件
			QueryWrapper<Product> queryProduct = new QueryWrapper<>();
			queryProduct.eq("is_active", true);
			queryProduct.eq("is_list", true);
			queryProduct.eq("is_marketable", true);
			List<Product> products = productService.list(queryProduct);
			for (Product product : products) {
				Product persistant = productService.find(product.getId());
				
				Store store = storeService.getById(persistant.getStore().getId());
				product.setStore(store);
				
				QueryWrapper<Sku> queryWrapper = new QueryWrapper<>();
				queryWrapper.eq("product_id", product.getId());
				Set<Sku> skus = new HashSet<>(skuService.list(queryWrapper));
				product.setSkus(skus);
				product.setProductImages(persistant.getProductImages());
				productRepository.save(product);
			}
		}
		
		// 店铺
		if (type.isAssignableFrom(Store.class)) {
			// 清除所有
			productRepository.deleteAll();
			QueryWrapper<Store> queryStore = new QueryWrapper<>();
			queryStore.eq("status", Store.Status.SUCCESS.ordinal());
			queryStore.eq("is_enabled", true);
			List<Store> stores = storeService.findAll();
			storeRepository.saveAll(stores);
		}
	}

}
package com.igomall.plugin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.igomall.entity.GiftAttribute;
import com.igomall.entity.Promotion;
import com.igomall.entity.Sku;
import com.igomall.entity.Store;

/**
 * Plugin - 赠品
 * 
 */
@Component("giftPromotionPlugin")
public class GiftPromotionPlugin extends PromotionPlugin {

	@Override
	public String getName() {
		return "赠品";
	}

	@Override
	public String getInstallUrl() {
		return "/admin/plugin/gift_promotion/install";
	}

	@Override
	public String getUninstallUrl() {
		return "/admin/plugin/gift_promotion/uninstall";
	}

	@Override
	public String getSettingUrl() {
		return "/admin/plugin/gift_promotion/setting";
	}

	@Override
	public String getAddUrl() {
		return "/business/gift_promotion/add";
	}

	@Override
	public String getEditUrl() {
		return "/business/gift_promotion/edit";
	}

	@Override
	public List<Sku> getGifts(Promotion promotion, Store store) {
		GiftAttribute giftAttribute = (GiftAttribute) promotion.getPromotionDefaultAttribute();
		return new ArrayList<>(giftAttribute.getGifts());
	}

}
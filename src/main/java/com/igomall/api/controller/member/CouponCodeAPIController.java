package com.igomall.api.controller.member;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.igomall.entity.Coupon;
import com.igomall.entity.CouponCode;
import com.igomall.entity.Member;
import com.igomall.service.CouponCodeService;
import com.igomall.service.CouponService;
import com.igomall.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.igomall.Page;
import com.igomall.Pageable;
import com.igomall.api.model.ApiResult;
import com.igomall.api.util.ResultUtils;

/**
 * Controller - 优惠码
 * 
 */
@RestController("memberApiCouponCodeController")
@RequestMapping("/api/member/coupon_code")
public class CouponCodeAPIController extends BaseAPIController {

	/**
	 * 每页记录数
	 */
	private static final int PAGE_SIZE = 12;

	@Inject
	private CouponService couponService;
	@Inject
	private CouponCodeService couponCodeService;
	@Inject
	private UserService userService;

	/**
	 * 兑换
	 */
	@GetMapping("/exchange")
	public ApiResult exchange(Integer pageNumber) {
		Pageable pageable = new Pageable(pageNumber, PAGE_SIZE);
		Page<Coupon> couponPage = couponService.findPage(true, true, false, pageable);
		Map<String, Object> data = new HashMap<String, Object>();
		for (Coupon coupon : couponPage.getContent()) {
			data.put("couponId", coupon.getId());
			data.put("couponName", coupon.getName());
			data.put("point", coupon.getPoint());
		}
		return ResultUtils.ok(data);
	}
	

	/**
	 * 兑换
	 */
	@PostMapping("/exchange")
	public ApiResult exchange(@RequestParam("couponId") Long couponId) {
		Member currentUser = userService.getCurrent(Member.class);
		Coupon coupon = couponService.find(couponId);
		if (coupon == null) {
			return ResultUtils.NOT_FOUND;
		}

		if (!coupon.getIsEnabled() || !coupon.getIsExchange() || coupon.hasExpired()) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}
		if (currentUser.getPoint() < coupon.getPoint()) {
			return ResultUtils.unprocessableEntity("member.couponCode.point");
		}
		CouponCode couponCode = couponCodeService.exchange(coupon, currentUser);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("couponCodeId", couponCode.getId());
		data.put("couponCode", couponCode.getCode());
		data.put("couponName", couponCode.getCoupon().getName());
		return ResultUtils.ok(data);
	}

	/**
	 * 列表
	 */
	@GetMapping(path = "/list")
	public ApiResult list(@RequestParam(name = "pageNumber", defaultValue = "1") Integer pageNumber) {
		Member currentUser = userService.getCurrent(Member.class);
		Pageable pageable = new Pageable(pageNumber, PAGE_SIZE);
		Page<CouponCode> couponCodePage = couponCodeService.findPage(currentUser, pageable);
		Map<String, Object> data = new HashMap<String, Object>();
		for (CouponCode couponCode : couponCodePage.getContent()) {
			data.put("couponCodeId", couponCode.getId());
			data.put("couponCode", couponCode.getCode());
			data.put("couponName", couponCode.getCoupon().getName());
		}
		return ResultUtils.ok(data);
	}

}
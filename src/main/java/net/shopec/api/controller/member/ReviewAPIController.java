package net.shopec.api.controller.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.shopec.Page;
import net.shopec.Pageable;
import net.shopec.Setting;
import net.shopec.api.model.ApiResult;
import net.shopec.api.util.ResultUtils;
import net.shopec.controller.member.ReviewController.ReviewEntryListForm;
import net.shopec.entity.Member;
import net.shopec.entity.Order;
import net.shopec.entity.OrderItem;
import net.shopec.entity.Review;
import net.shopec.entity.Review.Entry;
import net.shopec.entity.Sku;
import net.shopec.service.OrderItemService;
import net.shopec.service.OrderService;
import net.shopec.service.ReviewService;
import net.shopec.service.UserService;
import net.shopec.util.SpringUtils;
import net.shopec.util.SystemUtils;
import net.shopec.util.WebUtils;

/**
 * 评论 - 接口类
 */
@RestController("memberApiReviewController")
@RequestMapping("/api/member/review")
public class ReviewAPIController extends BaseAPIController {

	/**
	 * 每页记录数
	 */
	private static final int PAGE_SIZE = 10;

	@Inject
	private OrderService orderService;
	@Inject
	private OrderItemService orderItemService;
	@Inject
	private ReviewService reviewService;
	@Inject
	private UserService userService;
	
	
	/**
	 * 列表
	 */
	@GetMapping("/list")
	public ApiResult list(@RequestParam(name = "pageNumber", defaultValue = "1") Integer pageNumber) {
		Member currentUser = userService.getCurrent(Member.class);
		
		Setting setting = SystemUtils.getSetting();
		List<Map<String, Object>> data = new ArrayList<>();
		Pageable pageable = new Pageable(pageNumber, PAGE_SIZE);
		Page<Review> reviews = reviewService.findPage(currentUser, null, null, null, null, pageable);
		for (Review review : reviews.getContent()) {
			Map<String, Object> item = new HashMap<>();
			item.put("productImage", review.getProduct().getThumbnail() == null ? setting.getDefaultThumbnailProductImage() : review.getProduct().getThumbnail());
			item.put("productName", review.getProduct().getName());
			if (review.getSpecifications() != null) {
				item.put("specifications", review.getSpecifications());
			}
			item.put("score", review.getScore());
			item.put("content", review.getContent());
			data.add(item);
		}
		return ResultUtils.ok(data);
	}
	
	/**
	 * 保存
	 */
	@PostMapping("/save")
	public ApiResult save(@RequestParam("orderId")Long orderId, @RequestBody ReviewEntryListForm reviewEntryListForm) {
		Member currentUser = userService.getCurrent(Member.class);
		
		Setting setting = SystemUtils.getSetting();
		if (!setting.getIsReviewEnabled()) {
			return ResultUtils.unprocessableEntity("member.review.disabled");
		}
		Order order = orderService.find(orderId);
		if (order == null || !currentUser.equals(order.getMember()) || order.getIsReviewed() || CollectionUtils.isEmpty(order.getOrderItems())) {
			return ResultUtils.badRequest("已评论");
		}
		if (!Order.Status.RECEIVED.equals(order.getStatus()) && !Order.Status.COMPLETED.equals(order.getStatus())) {
			return ResultUtils.badRequest("状态不是已收货或已完成");
		}
		List<Entry> reviewEntries = reviewEntryListForm.getReviewEntryList();
		if (CollectionUtils.isEmpty(reviewEntries)) {
			return ResultUtils.UNPROCESSABLE_ENTITY;
		}

		for (Review.Entry reviewEntry : reviewEntries) {
			OrderItem orderItem = reviewEntry.getOrderItem();
			Review review = reviewEntry.getReview();
			if (!isValid(Review.Entry.class, "orderItem", orderItem) || !isValid(Review.Entry.class, "review", review)) {
				return ResultUtils.UNPROCESSABLE_ENTITY;
			}
			OrderItem pOrderItem = orderItemService.find(orderItem.getId());
			Sku sku = pOrderItem.getSku();
			if (sku == null) {
				continue;
			}
			if (!order.equals(pOrderItem.getOrder()) || !isValid(Review.class, "score", review.getScore()) || !isValid(Review.class, "content", review.getContent())) {
				return ResultUtils.UNPROCESSABLE_ENTITY;
			}
		}
		HttpServletRequest request = WebUtils.getRequest();
		reviewService.create(order, reviewEntries, request.getRemoteAddr(), currentUser);
		String ok = setting.getIsReviewCheck() ? "member.review.check" : "member.review.success";
		return ResultUtils.ok(SpringUtils.getMessage(ok));
	}
	
	/**
	 * 删除
	 */
	@PostMapping("/delete")
	public ApiResult delete(@RequestParam("reviewId") Long reviewId) {
		Review review = reviewService.find(reviewId);
		if (review == null) {
			return ResultUtils.NOT_FOUND;
		}

		reviewService.delete(review);
		return ResultUtils.ok();
	}

}

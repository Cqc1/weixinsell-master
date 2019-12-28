package cn.edu.jxnu.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.edu.jxnu.dto.CartDTO;
import cn.edu.jxnu.entity.ProductInfo;
import cn.edu.jxnu.enums.ProductStatusEnum;
import cn.edu.jxnu.enums.ResultEnum;
import cn.edu.jxnu.exception.SellException;
import cn.edu.jxnu.repository.ProductInfoRepository;
import cn.edu.jxnu.service.ProductService;

/**
 * 商品
 * 
 * @author 梦境迷离.
 * @version V1.0
 * @time 2018年4月13日
 */
@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductInfoRepository repository;

	// 使用CachePut更新，必须返回值必须一样，key不可省略，省略则使用默认的方法参数作为key,导致key不同
	// @Cacheable(cacheNames = "product", key = "#productId")
	@Override
	public ProductInfo findOne(String productId) {
		return repository.findOne(productId);
	}

	@Override
	public List<ProductInfo> findUpAll() {
		return repository.findByProductStatus(ProductStatusEnum.UP.getCode());
	}

	@Override
	public Page<ProductInfo> findAll(Pageable pageable) {
		return repository.findAll(pageable);
	}

	// @CachePut(cacheNames = "product", key = "#productInfo.getProductId()")
	@Override
	public ProductInfo save(ProductInfo productInfo) {
		return repository.save(productInfo);
	}

	// 加库存
	@Override
	@Transactional
	public void increaseStock(List<CartDTO> cartDTOList) {
		for (CartDTO cartDTO : cartDTOList) {
			ProductInfo productInfo = repository.findOne(cartDTO.getProductId());
			if (productInfo == null) {
				throw new SellException(ResultEnum.PRODUCT_NOT_EXIST);
			}
			Integer result = productInfo.getProductStock() + cartDTO.getProductQuantity();
			productInfo.setProductStock(result);

			repository.save(productInfo);
		}

	}

	// 减库存
	@Override
	@Transactional
	public void decreaseStock(List<CartDTO> cartDTOList) {
		for (CartDTO cartDTO : cartDTOList) {
			ProductInfo productInfo = repository.findOne(cartDTO.getProductId());
			if (productInfo == null) {
				throw new SellException(ResultEnum.PRODUCT_NOT_EXIST);
			}

			Integer result = productInfo.getProductStock() - cartDTO.getProductQuantity();
			if (result < 0) {
				throw new SellException(ResultEnum.PRODUCT_STOCK_ERROR);
			}

			productInfo.setProductStock(result);

			repository.save(productInfo);
		}
	}

	// 上架商品
	@Override
	public ProductInfo onSale(String productId) {
		ProductInfo productInfo = repository.findOne(productId);
		if (productInfo == null) {
			throw new SellException(ResultEnum.PRODUCT_NOT_EXIST);
		}
		if (productInfo.getProductStatusEnum() == ProductStatusEnum.UP) {
			throw new SellException(ResultEnum.PRODUCT_STATUS_ERROR);
		}

		// 更新
		productInfo.setProductStatus(ProductStatusEnum.UP.getCode());
		return repository.save(productInfo);
	}

	// 下架商品
	@Override
	public ProductInfo offSale(String productId) {
		ProductInfo productInfo = repository.findOne(productId);
		if (productInfo == null) {
			throw new SellException(ResultEnum.PRODUCT_NOT_EXIST);
		}
		if (productInfo.getProductStatusEnum() == ProductStatusEnum.DOWN) {
			throw new SellException(ResultEnum.PRODUCT_STATUS_ERROR);
		}

		// 更新
		productInfo.setProductStatus(ProductStatusEnum.DOWN.getCode());
		return repository.save(productInfo);
	}
}

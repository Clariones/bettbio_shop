/**
 *
 */
package com.salesmanager.web.populator.shoppingCart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.salesmanager.core.business.catalog.product.model.attribute.ProductOptionDescription;
import com.salesmanager.core.business.catalog.product.model.attribute.ProductOptionValueDescription;
import com.salesmanager.core.business.catalog.product.model.image.ProductImage;
import com.salesmanager.core.business.catalog.product.service.PricingService;
import com.salesmanager.core.business.catalog.product.service.ProductPricingContext;
import com.salesmanager.core.business.catalog.product.service.ProductPricingData;
import com.salesmanager.core.business.catalog.product.service.ProductSpecPricingData;
import com.salesmanager.core.business.catalog.product.service.price.BrandDiscountService;
import com.salesmanager.core.business.customer.model.Customer;
import com.salesmanager.core.business.generic.exception.ServiceException;
import com.salesmanager.core.business.merchant.model.MerchantStore;
import com.salesmanager.core.business.order.model.OrderSummary;
import com.salesmanager.core.business.order.model.OrderTotalSummary;
import com.salesmanager.core.business.reference.language.model.Language;
import com.salesmanager.core.business.shoppingcart.model.ShoppingCart;
import com.salesmanager.core.business.shoppingcart.service.ShoppingCartCalculationService;
import com.salesmanager.core.utils.AbstractDataPopulator;
import com.salesmanager.core.utils.ProductPriceUtils;
import com.salesmanager.web.entity.order.OrderTotal;
import com.salesmanager.web.entity.order.SubOrder;
import com.salesmanager.web.entity.shoppingcart.ShoppingCartAttribute;
import com.salesmanager.web.entity.shoppingcart.ShoppingCartData;
import com.salesmanager.web.entity.shoppingcart.ShoppingCartItem;
import com.salesmanager.web.utils.ImageFilePathUtils;


/**
 * @author Umesh A
 *
 */


public class ShoppingCartDataPopulator extends AbstractDataPopulator<ShoppingCart,ShoppingCartData>
{

    private static final Logger LOG = LoggerFactory.getLogger(ShoppingCartDataPopulator.class);

    private PricingService pricingService;

    private Customer customer;
    
    private  ShoppingCartCalculationService shoppingCartCalculationService;

	private BrandDiscountService brandDiscountService;
 
    @Override
    public ShoppingCartData createTarget()
    {

        return new ShoppingCartData();
    }



    public ShoppingCartCalculationService getOrderService() {
        return shoppingCartCalculationService;
    }



    public PricingService getPricingService() {
        return pricingService;
    }


    @Override
    public ShoppingCartData populate(final ShoppingCart shoppingCart,
                                     final ShoppingCartData cart, final MerchantStore store, final Language language) {

    	int cartQuantity = 0;
        cart.setCode(shoppingCart.getShoppingCartCode());
        Set<com.salesmanager.core.business.shoppingcart.model.ShoppingCartItem> items = shoppingCart.getLineItems();
        //List<ShoppingCartItem> shoppingCartItemsList=Collections.emptyList();
        try{
            if(items!=null) {
            	// 2017-3-28: new price rule
				List<Long> productIds = new ArrayList<Long>();
				for (com.salesmanager.core.business.shoppingcart.model.ShoppingCartItem curItem : items) {
					productIds.add(curItem.getProductId());
				}
				ProductPricingContext priceCtx = new ProductPricingContext();
				priceCtx.setCustomer(customer);
				List<ProductPricingData> productPriceDatas = pricingService.calculateProductListPrice(priceCtx, productIds);
				if (productPriceDatas != null) {
					for (com.salesmanager.core.business.shoppingcart.model.ShoppingCartItem curItem : items) {
						ProductPricingData prodPricData = ProductPriceUtils.getProductPriceData(productPriceDatas,
								curItem.getProductId());
						if (prodPricData == null) {
							continue;
						}

						ProductSpecPricingData specPriceData = ProductPriceUtils.getProductSpecPrice(prodPricData,
								curItem.getProductPrice().getId(), curItem.getSpec());
						if (specPriceData == null) {
							continue;
						}

						if (specPriceData.getFinalPrice() != null) {
							curItem.setItemPrice(specPriceData.getFinalPrice());
							curItem.setSubTotal(
									specPriceData.getFinalPrice().multiply(new BigDecimal(curItem.getQuantity())));
						}
					}
				}
            	
            	
                //shoppingCartItemsList=new ArrayList<ShoppingCartItem>();
                for(com.salesmanager.core.business.shoppingcart.model.ShoppingCartItem item : items) {

                    ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
                    shoppingCartItem.setCode(cart.getCode());
                    shoppingCartItem.setProductCode(item.getProduct().getCode());
                   // shoppingCartItem.setProductVirtual(item.isProductVirtual());

                    shoppingCartItem.setProductId(item.getProductId());
                    shoppingCartItem.setId(item.getId());
                    String pname=item.getProduct().getProductDescription().getName();
                    if(pname.length()>30){
                    	pname=pname.substring(0,27)+"...";
                    }
                    shoppingCartItem.setName(pname);
                    pname =item.getProduct().getProductDescription().getEnName();
                    if(pname !=null && pname.length()>30){
                    	pname=pname.substring(0,27)+"...";
                    	shoppingCartItem.setEnName(pname);
                    }
                    //shoppingCartItem.setEnName(item.getProduct().getProductDescription().getEnName());
                    shoppingCartItem.setFriendlyUrl(item.getProduct().getProductDescription().getSeUrl());
                    shoppingCartItem.setPrice(pricingService.getDisplayAmount(item.getItemPrice(),store));
                    shoppingCartItem.setQuantity(item.getQuantity());
                    shoppingCartItem.setSpecs(item.getSpec());
                    
                    
                    cartQuantity = cartQuantity + item.getQuantity();
                    
                    shoppingCartItem.setProductPrice(item.getItemPrice());
                    shoppingCartItem.setSubTotal(pricingService.getDisplayAmount(item.getSubTotal(), store));
                    ProductImage image = item.getProduct().getProductImage();
                    if(image!=null) {
                        String imagePath = ImageFilePathUtils.buildProductImageFilePath(store, item.getProduct().getSku(), image.getProductImage());
                        shoppingCartItem.setImage(imagePath);
                    }
                    Set<com.salesmanager.core.business.shoppingcart.model.ShoppingCartAttributeItem> attributes = item.getAttributes();
                    if(attributes!=null) {
                        List<ShoppingCartAttribute> cartAttributes = new ArrayList<ShoppingCartAttribute>();
                        for(com.salesmanager.core.business.shoppingcart.model.ShoppingCartAttributeItem attribute : attributes) {
                            ShoppingCartAttribute cartAttribute = new ShoppingCartAttribute();
                            cartAttribute.setId(attribute.getId());
                            cartAttribute.setAttributeId(attribute.getProductAttributeId());
                            cartAttribute.setOptionId(attribute.getProductAttribute().getProductOption().getId());
                            cartAttribute.setOptionValueId(attribute.getProductAttribute().getProductOptionValue().getId());
                            List<ProductOptionDescription> optionDescriptions = attribute.getProductAttribute().getProductOption().getDescriptionsSettoList();
                            List<ProductOptionValueDescription> optionValueDescriptions = attribute.getProductAttribute().getProductOptionValue().getDescriptionsSettoList();
                            if(!CollectionUtils.isEmpty(optionDescriptions) && !CollectionUtils.isEmpty(optionValueDescriptions)) {
                            	cartAttribute.setOptionName(optionDescriptions.get(0).getName());
                            	cartAttribute.setOptionValue(optionValueDescriptions.get(0).getName());
                            	cartAttributes.add(cartAttribute);
                            }
                        }
                        shoppingCartItem.setShoppingCartAttributes(cartAttributes);
                    }
                    List<SubOrder> listSubOrder = cart.getSubOrders();
                    if(listSubOrder == null) listSubOrder= new ArrayList<SubOrder>();
                    SubOrder subOrder = this.getSubOrder(listSubOrder, item.getProduct().getMerchantStore());
                    subOrder.setMerchantStore(item.getProduct().getMerchantStore());
                    List<ShoppingCartItem> shoppingCartItemsList = subOrder.getShoppingCartItems();
                    if(shoppingCartItemsList == null) shoppingCartItemsList = new ArrayList<ShoppingCartItem>();
                    shoppingCartItemsList.add(shoppingCartItem);
                    subOrder.setTotal(subOrder.getTotal().add(item.getSubTotal()));
                    subOrder.setShoppingCartItems(shoppingCartItemsList);                    
                    listSubOrder = setSubOrder(listSubOrder,subOrder);
                    //listSubOrder.add(subOrder);
                    cart.setSubOrders(listSubOrder);
                }
            }
           /** if(CollectionUtils.isNotEmpty(shoppingCartItemsList)){
                cart.setShoppingCartItems(shoppingCartItemsList);
            }*/

            OrderSummary summary = new OrderSummary();
            List<com.salesmanager.core.business.shoppingcart.model.ShoppingCartItem> productsList = new ArrayList<com.salesmanager.core.business.shoppingcart.model.ShoppingCartItem>();
            productsList.addAll(shoppingCart.getLineItems());
            summary.setProducts(productsList);
            OrderTotalSummary orderSummary = shoppingCartCalculationService.calculate(getCustomer(), shoppingCart,store, language );

            if(CollectionUtils.isNotEmpty(orderSummary.getTotals())) {
            	List<OrderTotal> totals = new ArrayList<OrderTotal>();
            	for(com.salesmanager.core.business.order.model.OrderTotal t : orderSummary.getTotals()) {
            		OrderTotal total = new OrderTotal();
            		total.setCode(t.getOrderTotalCode());
            		total.setValue(t.getValue());
            		totals.add(total);
            	}
            	cart.setTotals(totals);
            }
            
           // cart.setSubTotal(pricingService.getDisplayAmount(orderSummary.getSubTotal(), store));
            cart.setTotal(pricingService.getDisplayAmount(orderSummary.getTotal(), store));
            cart.setQuantity(cartQuantity);
            cart.setId(shoppingCart.getId());
        }
        catch(ServiceException ex){
            LOG.error( "Error while converting cart Model to cart Data.."+ex );
            throw new ConversionException( "Unable to create cart data", ex );
        }
        return cart;


    };




   



	private List<SubOrder> setSubOrder(List<SubOrder> subOrders,SubOrder subOrder){
    		int i=0;
    		for(SubOrder order:subOrders){
    			if(order.getMerchantStore().getId()==subOrder.getMerchantStore().getId()){
    				subOrders.remove(i);
    				subOrders.add(subOrder);
    				return subOrders;
    			}
    			i++;
    		}
    		subOrders.add(subOrder);
    		return subOrders;
    }

    public void setPricingService(final PricingService pricingService) {
        this.pricingService = pricingService;
    }






    public void setShoppingCartCalculationService(final ShoppingCartCalculationService shoppingCartCalculationService) {
        this.shoppingCartCalculationService = shoppingCartCalculationService;
    }


    private SubOrder getSubOrder(List<SubOrder> subOrders,MerchantStore merchantStore ) {
    	if (subOrders == null) return new SubOrder();
    	for(SubOrder sub :subOrders){
    		if(sub.getMerchantStore().getId()==merchantStore.getId()) return sub;
    	}
    	
    	return new SubOrder();
    }



	public Customer getCustomer() {
		return customer;
	}



	public void setCustomer(Customer customer) {
		this.customer = customer;
	}



	public BrandDiscountService getBrandDiscountService() {
		return brandDiscountService;
	}



	public void setBrandDiscountService(BrandDiscountService brandDiscountService) {
		this.brandDiscountService = brandDiscountService;
	}

}
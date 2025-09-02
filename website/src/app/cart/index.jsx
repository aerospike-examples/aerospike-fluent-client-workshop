import React from "react";
import styles from "./index.module.css";
import { useCart } from "../../context/CartContext";
import { useToast } from "../../context/ToastContext";
import { fixImgUrl } from "../../utils";

const Cart = () => {
    const { items, removeFromCart, updateQuantity, clearCart, getCartTotal } = useCart();
    const { showSuccess, showInfo } = useToast();

    const formatPrice = (priceInCents) => {
        if (priceInCents === null || priceInCents === undefined) {
            return "N/A";
        }
        
        if (typeof priceInCents !== 'number') {
            return priceInCents.toString();
        }
        
        const dollars = priceInCents / 100;
        return `$${dollars.toFixed(2)}`;
    };

    const handleQuantityChange = (productId, newQuantity) => {
        const item = items.find(item => item.productId === productId);
        const itemName = item ? item.name : 'Item';
        
        if (newQuantity <= 0) {
            removeFromCart(productId);
            showInfo(`${itemName} removed from cart`, 3000);
        } else {
            updateQuantity(productId, newQuantity);
            showInfo(`${itemName} quantity updated to ${newQuantity}`, 3000);
        }
    };

    const handleRemoveItem = (productId) => {
        const item = items.find(item => item.productId === productId);
        const itemName = item ? item.name : 'Item';
        removeFromCart(productId);
        showInfo(`${itemName} removed from cart`, 3000);
    };

    const handleClearCart = () => {
        clearCart();
        showSuccess('Cart cleared successfully', 3000);
    };

    const totalAmount = getCartTotal();

    if (items.length === 0) {
        return (
            <div className={styles.cartContainer}>
                <div className={styles.emptyCart}>
                    <h1>Your Cart</h1>
                    <p>Your cart is empty</p>
                    <a href="/" className={styles.continueShopping}>
                        Continue Shopping
                    </a>
                </div>
            </div>
        );
    }

    return (
        <div className={styles.cartContainer}>
            <div className={styles.cartHeader}>
                <h1>Your Cart ({items.length} items)</h1>
                <button 
                    className={styles.clearCartButton}
                    onClick={handleClearCart}
                >
                    Clear Cart
                </button>
            </div>

            <div className={styles.cartContent}>
                <div className={styles.cartItems}>
                    {items.map((item) => (
                        <div key={item.productId} className={styles.cartItem}>
                            <div className={styles.itemImage}>
                                <img 
                                    src={item.image ? fixImgUrl(item.image) : '/placeholder.jpg'} 
                                    alt={item.name}
                                />
                            </div>
                            
                            <div className={styles.itemDetails}>
                                <h3>{item.name}</h3>
                                <p className={styles.brandName}>{item.brandName}</p>
                                <p className={styles.price}>{formatPrice(item.price)}</p>
                            </div>
                            
                            <div className={styles.itemControls}>
                                <div className={styles.quantityControls}>
                                    <button 
                                        className={styles.quantityButton}
                                        onClick={() => handleQuantityChange(item.productId, item.quantity - 1)}
                                    >
                                        -
                                    </button>
                                    <span className={styles.quantity}>{item.quantity}</span>
                                    <button 
                                        className={styles.quantityButton}
                                        onClick={() => handleQuantityChange(item.productId, item.quantity + 1)}
                                    >
                                        +
                                    </button>
                                </div>
                                
                                <button 
                                    className={styles.removeButton}
                                    onClick={() => handleRemoveItem(item.productId)}
                                >
                                    Remove
                                </button>
                            </div>
                            
                            <div className={styles.itemTotal}>
                                {formatPrice(item.price * item.quantity)}
                            </div>
                        </div>
                    ))}
                </div>

                <div className={styles.cartSummary}>
                    <div className={styles.summaryContent}>
                        <h2>Order Summary</h2>
                        
                        <div className={styles.summaryRow}>
                            <span>Subtotal:</span>
                            <span>{formatPrice(totalAmount)}</span>
                        </div>
                        
                        <div className={styles.summaryRow}>
                            <span>Shipping:</span>
                            <span>Free</span>
                        </div>
                        
                        <div className={styles.summaryRow}>
                            <span>Tax:</span>
                            <span>Calculated at checkout</span>
                        </div>
                        
                        <div className={styles.summaryTotal}>
                            <span>Total:</span>
                            <span>{formatPrice(totalAmount)}</span>
                        </div>
                        
                        <button className={styles.checkoutButton}>
                            Proceed to Checkout
                        </button>
                        
                        <a href="/" className={styles.continueShopping}>
                            Continue Shopping
                        </a>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Cart;

import React from "react";
import styles from "./index.module.css";
import clsx from "clsx";
import { fixImgUrl } from "../../utils";

const Card = ({product, small = false}) => {
    let img = product?.images?.search?.resolutions[small ? "125X161" : "180X240"] ?? (small ? product?.image_125X161 : product?.image_180X240);
    
    const formatPrice = (priceInCents) => {
        // If price is missing, return null (don't display)
        if (priceInCents === null || priceInCents === undefined) {
            return null;
        }
        
        // If price is present but not a number, display as-is
        if (typeof priceInCents !== 'number') {
            return priceInCents.toString();
        }
        
        // Convert cents to dollars and format
        const dollars = priceInCents / 100;
        return `$${dollars.toFixed(2)}`;
    }
    
    const formattedPrice = formatPrice(product?.price);
    
    return (
        <a 
            href={`/product/${product.id}`} 
            className={clsx(styles.card, small && styles.cardSmall)} >
            <div 
                className={clsx(small ? styles.imgContainerSmall : styles.imgContainer)} >
                <img 
                    className={clsx(small ? styles.prodImgSmall : styles.prodImg)} 
                    src={img ? fixImgUrl(img) : img} />
            </div>
            <strong 
                className={styles.brand}
                >
                {product.brandName}
            </strong>
            <div className={styles.name}>
                <span>{product?.name}</span>
            </div>
            {formattedPrice && (
                <div className={styles.price}>
                    <span>{formattedPrice}</span>
                </div>
            )}
        </a>
    )
}

export default Card;
import React, { useEffect, useRef, useState } from "react";
import styles from "./index.module.css";
import clsx from "clsx";
import { Chevron } from "../../components/icons";

const ProductDetail = ({name, descriptors, price, productId, onAddToCart}) => {
    const [open, setOpen] = useState(false);
    const [height, setHeight] = useState(429);
    const [elemHeight, setElemHeight] = useState(0);

    const toggleOpen = () => {
        setOpen(!open);
        setHeight(open ? 429 : elemHeight + 32);
    }

    const descriptionRef = useRef(null);
    useEffect(() => {
        setElemHeight(descriptionRef?.current?.clientHeight)
    }, [])

    const formatHTML = (value, prepend = null) => {
        if(value) {
            if(value === "<p>NA</p>") return null;
            value = value
                .replaceAll("<a", "<span")
                .replaceAll("</a>", "</span>")
                .replaceAll("<strong><br /></strong>", "")
                .replace("<br /><br /></span>", "</span>")
                .replace("</p><br/><br/><p>", "</p><p>")
                .replace("<br /><br /></p>", "</p>");
            value = prepend ? value.replace(prepend[0], prepend[1]) : value;
        }
        return value
    }

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
    
    let description = formatHTML(descriptors?.description?.value);
    let style_note = formatHTML(descriptors?.style_note?.value);
    let size_fit_desc = formatHTML(descriptors?.size_fit_desc?.value, ["<p>", "<p><strong>Fit<br /></strong>"]);
    let materials_care_desc = formatHTML(descriptors?.materials_care_desc?.value, ["<p>", "<p><strong>Wash care<br /></strong>"]);

    const formattedPrice = formatPrice(price);

    const handleAddToCart = () => {
        if (onAddToCart && productId) {
            onAddToCart(productId, 1); // Add 1 quantity by default
        }
    };

    return (
        <div className={styles.prodDetail}>
            <h2>{name}</h2>
            <div className={styles.descContainer} style={{height: `${height}px`}}>
                {formattedPrice && (
                    <div className={styles.price}>
                        <strong>{formattedPrice}</strong>
                    </div>
                )}
                <div 
                    className={styles.description} 
                    dangerouslySetInnerHTML={{__html: description + (style_note ? style_note : "") + (size_fit_desc ? size_fit_desc : "") + (materials_care_desc ? materials_care_desc : "")}}
                    ref={descriptionRef} />
            </div>
            {elemHeight > 429 &&
            <div className={styles.more} onClick={toggleOpen}>
                <Chevron className={clsx(styles.chevron, open && styles.open)} />
            </div>}
            {formattedPrice && (
                <div className={styles.addToCartSection}>
                    <button 
                        className={styles.addToCartButton}
                        onClick={handleAddToCart}
                    >
                        Add to Cart
                    </button>
                </div>
            )}
        </div>
    )
}

export default ProductDetail;
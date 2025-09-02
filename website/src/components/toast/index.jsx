import React, { useEffect, useState } from 'react';
import styles from './index.module.css';
import { useToast } from '../../context/ToastContext';

const Toast = ({ toast }) => {
    const [isVisible, setIsVisible] = useState(false);
    const [isLeaving, setIsLeaving] = useState(false);
    const { removeToast } = useToast();

    useEffect(() => {
        // Trigger entrance animation
        const timer = setTimeout(() => setIsVisible(true), 10);
        return () => clearTimeout(timer);
    }, []);

    const handleClose = () => {
        setIsLeaving(true);
        setTimeout(() => {
            removeToast(toast.id);
        }, 300); // Match the CSS transition duration
    };

    const getIcon = () => {
        switch (toast.type) {
            case 'success':
                return '✓';
            case 'error':
                return '✕';
            case 'warning':
                return '⚠';
            case 'info':
                return 'ℹ';
            default:
                return '✓';
        }
    };

    return (
        <div 
            className={`${styles.toast} ${styles[toast.type]} ${isVisible ? styles.visible : ''} ${isLeaving ? styles.leaving : ''}`}
            onClick={handleClose}
        >
            <div className={styles.icon}>
                {getIcon()}
            </div>
            <div className={styles.message}>
                {toast.message}
            </div>
            <button 
                className={styles.closeButton}
                onClick={handleClose}
                aria-label="Close notification"
            >
                ×
            </button>
        </div>
    );
};

const ToastContainer = () => {
    const { toasts } = useToast();

    if (toasts.length === 0) return null;

    return (
        <div className={styles.toastContainer}>
            {toasts.map(toast => (
                <Toast key={toast.id} toast={toast} />
            ))}
        </div>
    );
};

export default ToastContainer;

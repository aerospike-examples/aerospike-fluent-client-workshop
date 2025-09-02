import React, { createContext, useContext, useReducer } from 'react';

const ToastContext = createContext();

const toastReducer = (state, action) => {
    switch (action.type) {
        case 'ADD_TOAST':
            return {
                ...state,
                toasts: [...state.toasts, action.payload]
            };
        case 'REMOVE_TOAST':
            return {
                ...state,
                toasts: state.toasts.filter(toast => toast.id !== action.payload.id)
            };
        case 'CLEAR_TOASTS':
            return {
                ...state,
                toasts: []
            };
        default:
            return state;
    }
};

const initialState = {
    toasts: []
};

export const ToastProvider = ({ children }) => {
    const [state, dispatch] = useReducer(toastReducer, initialState);

    const addToast = (message, type = 'success', duration = 3000) => {
        const toastId = Date.now() + Math.random();
        const toast = {
            message,
            type, // 'success', 'error', 'warning', 'info'
            duration,
            id: toastId
        };
        
        dispatch({ type: 'ADD_TOAST', payload: toast });

        // Auto-remove toast after duration
        if (duration > 0) {
            setTimeout(() => {
                removeToast(toastId);
            }, duration);
        }

        return toast;
    };

    const removeToast = (id) => {
        dispatch({ type: 'REMOVE_TOAST', payload: { id } });
    };

    const clearToasts = () => {
        dispatch({ type: 'CLEAR_TOASTS' });
    };

    const showSuccess = (message, duration) => addToast(message, 'success', duration);
    const showError = (message, duration) => addToast(message, 'error', duration);
    const showWarning = (message, duration) => addToast(message, 'warning', duration);
    const showInfo = (message, duration) => addToast(message, 'info', duration);

    const value = {
        toasts: state.toasts,
        addToast,
        removeToast,
        clearToasts,
        showSuccess,
        showError,
        showWarning,
        showInfo
    };

    return (
        <ToastContext.Provider value={value}>
            {children}
        </ToastContext.Provider>
    );
};

export const useToast = () => {
    const context = useContext(ToastContext);
    if (!context) {
        throw new Error('useToast must be used within a ToastProvider');
    }
    return context;
};

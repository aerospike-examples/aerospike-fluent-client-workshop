import React, { useState, useRef, useEffect } from 'react';
import styles from './index.module.css';

const SearchableDropdown = ({ 
    options = [], 
    value = '', 
    onChange, 
    placeholder = 'Select...', 
    label = '',
    className = '',
    disabled = false 
}) => {
    const [isOpen, setIsOpen] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [filteredOptions, setFilteredOptions] = useState(options);
    const [highlightedIndex, setHighlightedIndex] = useState(-1);
    const [displayValue, setDisplayValue] = useState(value);
    const [isSelectingOption, setIsSelectingOption] = useState(false);
    
    const dropdownRef = useRef(null);
    const inputRef = useRef(null);
    const listRef = useRef(null);

    // Update filtered options when options or search term changes
    useEffect(() => {

        if (searchTerm && searchTerm.trim() !== '') {
            const filtered = options.filter(option =>
                option.toLowerCase().startsWith(searchTerm.toLowerCase())
            );

            setFilteredOptions(filtered);
            setHighlightedIndex(filtered.length > 0 ? 0 : -1);
        } else {
            setFilteredOptions(options);
            setHighlightedIndex(-1);
        }
    }, [options, searchTerm]);

    // Update display value when external value changes
    useEffect(() => {
        if (!isOpen) {
            setDisplayValue(value);
            setSearchTerm('');
        }
    }, [value, isOpen]);

    // Close dropdown when clicking outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                handleClose();
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleInputChange = (e) => {
        const newValue = e.target.value;

        
        // Update both search term and display value
        setSearchTerm(newValue);
        setDisplayValue(newValue);
        
        if (!isOpen) {
            setIsOpen(true);
        }

        // Only update the actual value for exact matches or empty string
        const exactMatch = options.find(option => 
            option.toLowerCase() === newValue.toLowerCase()
        );
        
        if (exactMatch) {
            onChange(exactMatch);
        } else if (newValue === '') {
            onChange('');
        }
        // For partial matches, don't call onChange - let user continue typing or select from list
    };

    const handleOptionClick = (option) => {

        setIsSelectingOption(true);
        setDisplayValue(option);
        setSearchTerm('');
        onChange(option);
        setIsOpen(false);
        
        // Reset the selecting flag after a short delay
        setTimeout(() => {
            setIsSelectingOption(false);
            inputRef.current?.blur();
        }, 50);
    };

    const handleInputFocus = () => {

        setIsOpen(true);
        // Don't clear anything on focus - let user see current value and type to filter
    };

    const handleClose = () => {
        setIsOpen(false);
        setHighlightedIndex(-1);
        setSearchTerm('');
        
        // Only reset display value if we don't have a selected value
        if (value) {
            setDisplayValue(value);
        }
    };

    const handleClear = (e) => {
        e.preventDefault();
        e.stopPropagation();
        setDisplayValue('');
        setSearchTerm('');
        onChange('');
        inputRef.current?.focus();
    };

    const handleInputBlur = (e) => {

        
        // If we're in the middle of selecting an option, don't handle blur
        if (isSelectingOption) {

            return;
        }
        
        // Don't close immediately - wait to see if focus moves to another element
        setTimeout(() => {
            // Only close if focus has moved completely away from our component
            const activeElement = document.activeElement;
            const isWithinComponent = dropdownRef.current?.contains(activeElement);
            

            
            if (!isWithinComponent && isOpen && !isSelectingOption) {

                // Close dropdown but preserve the selected value
                setIsOpen(false);
                setHighlightedIndex(-1);
                setSearchTerm('');
                // Don't change displayValue - keep it as the selected value
                if (value) {
                    setDisplayValue(value);
                }
            }
        }, 100);
    };

    const handleKeyDown = (e) => {
        if (!isOpen) {
            if (e.key === 'Enter' || e.key === 'ArrowDown') {
                setIsOpen(true);
                e.preventDefault();
            }
            return;
        }

        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                setHighlightedIndex(prev => 
                    prev < filteredOptions.length - 1 ? prev + 1 : 0
                );
                break;
            
            case 'ArrowUp':
                e.preventDefault();
                setHighlightedIndex(prev => 
                    prev > 0 ? prev - 1 : filteredOptions.length - 1
                );
                break;
            
            case 'Enter':
                e.preventDefault();
                if (highlightedIndex >= 0 && filteredOptions[highlightedIndex]) {
                    handleOptionClick(filteredOptions[highlightedIndex]);
                } else if (filteredOptions.length === 1) {
                    // If only one option matches, select it
                    handleOptionClick(filteredOptions[0]);
                } else {
                    // If no exact match, keep the dropdown open and don't clear

                }
                break;
            
            case 'Escape':
                e.preventDefault();
                handleClose();
                inputRef.current?.blur();
                break;
            
            default:
                break;
        }
    };

    // Scroll highlighted option into view
    useEffect(() => {
        if (highlightedIndex >= 0 && listRef.current) {
            const highlightedElement = listRef.current.children[highlightedIndex];
            if (highlightedElement) {
                highlightedElement.scrollIntoView({
                    block: 'nearest',
                    behavior: 'smooth'
                });
            }
        }
    }, [highlightedIndex]);

    const hasFloatingLabel = label && (displayValue || isOpen || value);

    return (
        <div className={`${styles.searchableDropdown} ${value ? styles.hasValue : ''} ${className}`} ref={dropdownRef}>
            <div className={styles.inputWrapper}>
                {label && (
                    <label className={`${styles.floatingLabel} ${hasFloatingLabel ? styles.floated : ''}`}>
                        {label}
                    </label>
                )}
                <input
                    ref={inputRef}
                    type="text"
                    value={displayValue}
                    onChange={handleInputChange}
                    onFocus={handleInputFocus}
                    onBlur={handleInputBlur}
                    onKeyDown={handleKeyDown}
                    placeholder=""
                    className={styles.input}
                    disabled={disabled}
                    autoComplete="off"
                    role="combobox"
                    aria-expanded={isOpen}
                    aria-haspopup="listbox"
                    aria-autocomplete="list"
                />
                {value && (
                    <button
                        type="button"
                        className={styles.clearButton}
                        onClick={handleClear}
                        tabIndex={-1}
                        aria-label="Clear selection"
                    >
                        ×
                    </button>
                )}
                <div className={`${styles.arrow} ${isOpen ? styles.open : ''} ${value ? styles.withClear : ''}`}>
                    ▼
                </div>
            </div>
            
            {isOpen && (
                <div className={styles.dropdown}>
                    <ul 
                        ref={listRef}
                        className={styles.optionsList}
                        role="listbox"
                    >
                        {filteredOptions.length > 0 ? (
                            filteredOptions.map((option, index) => (
                                <li
                                    key={option}
                                    className={`${styles.option} ${
                                        index === highlightedIndex ? styles.highlighted : ''
                                    } ${option === value ? styles.selected : ''}`}
                                    onClick={() => handleOptionClick(option)}
                                    onMouseDown={(e) => e.preventDefault()}
                                    role="option"
                                    aria-selected={option === value}
                                >
                                    {option}
                                </li>
                            ))
                        ) : (
                            <li className={styles.noOptions}>
                                No brands found matching "{searchTerm}"
                            </li>
                        )}
                    </ul>
                </div>
            )}
        </div>
    );
};

export default SearchableDropdown;

/**
 * Upload Photo Handler для CV Generator
 * Обробляє завантаження та відображення фотографій
 */

document.addEventListener('DOMContentLoaded', function() {
    const photoInput = document.getElementById('photo');
    const fileLabel = document.getElementById('file-label');
    const fileInfo = document.getElementById('file-info');
    const fileName = document.getElementById('file-name');
    const fileSize = document.getElementById('file-size');
    const clearButton = document.getElementById('clear-file');

    // Константи
    const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    const ALLOWED_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];

    /**
     * Форматує розмір файлу в зручний формат
     */
    function formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    /**
     * Перевіряє валідність файлу
     */
    function validateFile(file) {
        const errors = [];

        // Перевірка типу файлу
        if (!ALLOWED_TYPES.includes(file.type)) {
            errors.push('Дозволені тільки файли зображень (JPG, PNG, WebP)');
        }

        // Перевірка розміру файлу
        if (file.size > MAX_FILE_SIZE) {
            errors.push(`Максимальний розмір файлу: ${formatFileSize(MAX_FILE_SIZE)}`);
        }

        // Перевірка мінімального розміру
        if (file.size < 1024) {
            errors.push('Файл занадто малий');
        }

        return {
            isValid: errors.length === 0,
            errors: errors
        };
    }

    /**
     * Оновлює відображення інформації про файл
     */
    function updateFileInfo(file) {
        const validation = validateFile(file);
        const sizeFormatted = formatFileSize(file.size);

        fileName.textContent = file.name;

        if (validation.isValid) {
            fileSize.innerHTML = `<span style="color: #28a745;">
                <i class="bi bi-check-circle"></i> Розмір: ${sizeFormatted} - Готово до завантаження
            </span>`;
            fileInfo.style.borderLeft = '4px solid #28a745';
            fileInfo.style.backgroundColor = '#d4edda';
        } else {
            fileSize.innerHTML = `<span style="color: #dc3545;">
                <i class="bi bi-exclamation-triangle"></i> ${validation.errors.join(', ')}
            </span>`;
            fileInfo.style.borderLeft = '4px solid #dc3545';
            fileInfo.style.backgroundColor = '#f8d7da';
        }

        return validation.isValid;
    }

    /**
     * Оновлює стиль кнопки завантаження
     */
    function updateButtonStyle(isFileSelected, isValid = true) {
        if (isFileSelected && isValid) {
            fileLabel.style.backgroundColor = '#d4edda';
            fileLabel.style.borderColor = '#28a745';
            fileLabel.style.color = '#155724';
            fileLabel.innerHTML = '<i class="bi bi-check"></i> Файл обрано';
        } else if (isFileSelected && !isValid) {
            fileLabel.style.backgroundColor = '#f8d7da';
            fileLabel.style.borderColor = '#dc3545';
            fileLabel.style.color = '#721c24';
            fileLabel.innerHTML = '<i class="bi bi-exclamation-triangle"></i> Помилка файлу';
        } else {
            resetButtonStyle();
        }
    }

    /**
     * Скидає стиль кнопки до початкового стану
     */
    function resetButtonStyle() {
        fileLabel.style.backgroundColor = '#f0f0f0';
        fileLabel.style.borderColor = '#ddd';
        fileLabel.style.color = '#333';

        // Відновлюємо оригінальний текст
        const isEditing = document.querySelector('[data-cv-editing="true"]');
        if (isEditing) {
            fileLabel.innerHTML = 'Обрати нове фото';
        } else {
            fileLabel.innerHTML = 'Виберіть файл';
        }
    }

    /**
     * Очищає обраний файл
     */
    function clearFile() {
        photoInput.value = '';
        fileInfo.style.display = 'none';
        resetButtonStyle();

        console.log('📷 File cleared by user');
    }

    /**
     * Обробляє вибір файлу
     */
    function handleFileSelection() {
        if (this.files && this.files[0]) {
            const file = this.files[0];

            console.log('📷 File selected:', {
                name: file.name,
                size: file.size,
                type: file.type,
                lastModified: new Date(file.lastModified).toLocaleString()
            });

            const isValid = updateFileInfo(file);
            updateButtonStyle(true, isValid);
            fileInfo.style.display = 'block';

            // Додаткова валідація через FileReader для перевірки, що файл дійсно є зображенням
            if (isValid && file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    console.log('📷 File successfully read, size:', e.target.result.length, 'bytes');
                };
                reader.onerror = function() {
                    console.error('📷 Error reading file');
                    fileSize.innerHTML = '<span style="color: #dc3545;">Помилка читання файлу</span>';
                };
                reader.readAsDataURL(file);
            }
        } else {
            clearFile();
        }
    }

    // Перевірка наявності необхідних елементів
    if (!photoInput || !fileLabel || !fileInfo || !fileName || !fileSize || !clearButton) {
        console.warn('📷 Photo upload: Не всі необхідні елементи знайдено на сторінці');
        return;
    }

    // Додаємо обробники подій
    photoInput.addEventListener('change', handleFileSelection);
    clearButton.addEventListener('click', clearFile);

    // Ефекти hover для кнопки
    fileLabel.addEventListener('mouseenter', function() {
        if (!photoInput.files || !photoInput.files[0]) {
            this.style.backgroundColor = '#e2e6ea';
        }
    });

    fileLabel.addEventListener('mouseleave', function() {
        if (!photoInput.files || !photoInput.files[0]) {
            this.style.backgroundColor = '#f0f0f0';
        }
    });

    // Drag & Drop функціональність
    fileLabel.addEventListener('dragover', function(e) {
        e.preventDefault();
        this.style.backgroundColor = '#e2e6ea';
        this.style.borderColor = '#007bff';
    });

    fileLabel.addEventListener('dragleave', function(e) {
        e.preventDefault();
        if (!photoInput.files || !photoInput.files[0]) {
            this.style.backgroundColor = '#f0f0f0';
            this.style.borderColor = '#ddd';
        }
    });

    fileLabel.addEventListener('drop', function(e) {
        e.preventDefault();

        const files = e.dataTransfer.files;
        if (files.length > 0) {
            photoInput.files = files;
            handleFileSelection.call(photoInput);
        }

        this.style.backgroundColor = '#f0f0f0';
        this.style.borderColor = '#ddd';
    });

    // Валідація форми перед відправкою
    const form = document.getElementById('cvForm') || document.querySelector('form');
    if (form) {
        form.addEventListener('submit', function(e) {
            if (photoInput.files && photoInput.files[0]) {
                const validation = validateFile(photoInput.files[0]);
                if (!validation.isValid) {
                    e.preventDefault();
                    alert('Помилка з файлом фото: ' + validation.errors.join(', '));
                    return false;
                }

                console.log('📷 Valid photo file will be submitted');
            }
        });
    }

    console.log('📷 Photo upload handler initialized successfully');
});
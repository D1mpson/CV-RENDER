function downloadPDF() {
    try {
        const { jsPDF } = window.jspdf;
        const cvContainer = document.querySelector('.cv-container');

        // Визначаємо колір фону CV і перевіряємо на rgba з прозорістю
        let backgroundColor = window.getComputedStyle(cvContainer).backgroundColor;

        // Конвертуємо rgba(0, 0, 0, 0) в solid color rgb(0, 0, 0)
        if (backgroundColor.includes('rgba')) {
            // Витягуємо RGB компоненти з rgba
            const rgbValues = backgroundColor.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)/i);
            if (rgbValues) {
                backgroundColor = `rgb(${rgbValues[1]}, ${rgbValues[2]}, ${rgbValues[3]})`;
            } else {
                // Якщо не вдалося витягти - встановлюємо чорний за замовчуванням
                backgroundColor = 'rgb(0, 0, 0)';
            }
        }

        // Приховуємо кнопки
        const actions = document.querySelector('.actions');
        const originalDisplay = actions.style.display;
        actions.style.display = 'none';

        html2canvas(cvContainer, {
            scale: 2,             // Підвищена якість
            useCORS: true,
            logging: false,
            allowTaint: true,
            imageTimeout: 0,
            backgroundColor: backgroundColor
        }).then(canvas => {
            const imgData = canvas.toDataURL('image/jpeg', 1.0);  // Максимальна якість
            const pdf = new jsPDF('p', 'mm', 'a4');

            const pageWidth = 210;    // А4 ширина
            const pageHeight = 297;   // А4 висота
            const margin = 10;        // Відступ 1 см з кожного боку

            const imgWidth = canvas.width;
            const imgHeight = canvas.height;

            const scale = Math.min(
                (pageWidth - 2 * margin) / imgWidth,
                (pageHeight - 2 * margin) / imgHeight
            );

            const scaledWidth = imgWidth * scale;
            const scaledHeight = imgHeight * scale;

            const marginX = (pageWidth - scaledWidth) / 2;
            const marginY = (pageHeight - scaledHeight) / 2;

            // Заливаємо весь аркуш кольором CV (вже без прозорості)
            // Конвертуємо rgb в hex для jsPDF
            let hexColor = '#000000'; // Чорний за замовчуванням
            const rgbValues = backgroundColor.match(/rgb\((\d+),\s*(\d+),\s*(\d+)\)/i);
            if (rgbValues) {
                const r = parseInt(rgbValues[1]).toString(16).padStart(2, '0');
                const g = parseInt(rgbValues[2]).toString(16).padStart(2, '0');
                const b = parseInt(rgbValues[3]).toString(16).padStart(2, '0');
                hexColor = `#${r}${g}${b}`;
            }

            pdf.setFillColor(hexColor);
            pdf.rect(0, 0, pageWidth, pageHeight, 'F');

            pdf.addImage(
                imgData,
                'JPEG',
                marginX,
                marginY,
                scaledWidth,
                scaledHeight
            );

            pdf.save(`CV_${document.querySelector('.name').textContent.replace(/\s+/g, '_')}.pdf`);

            // Показуємо кнопки назад
            actions.style.display = originalDisplay;

        }).catch(error => {
            console.error('Помилка при створенні PDF:', error);
            alert('Не вдалося створити PDF: ' + error.message);

            // Показуємо кнопки назад у випадку помилки
            actions.style.display = originalDisplay;
        });
    } catch (error) {
        console.error('Критична помилка при генерації PDF:', error);
        alert('Критична помилка при генерації PDF: ' + error.message);
    }
}
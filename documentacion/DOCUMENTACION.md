# Documentación de Tech-Flow

Esta carpeta contiene los documentos mantenidos a partir del código fuente, las
vistas FXML, `pom.xml`, las pruebas y los esquemas CSV del proyecto:

- `informe-tecnico/main.tex`: arquitectura, procesos, persistencia, pruebas y
  limitaciones conocidas.
Pendiente FALTA PONER EL NUEVO DIAGRAMA DE CLASES (SOLO ESTA EL ANTIGUO)
- `manual-instalacion/main.tex`: pasos para localizar y descomprimir el ZIP e
  iniciar Tech-Flow con `Tech-Flow.jar` o `ejecutar.bat`.
- `manual-usuario/main.tex`: guía de los flujos por rol, incluidas las columnas
  que requieren las importaciones CSV.
- `capturas/`: capturas de interfaz incluidas como evidencia visual.

## Regenerar los PDF

En Windows PowerShell, desde la raíz del proyecto:

```powershell
.\documentacion\compilar.ps1
```

Se requiere MiKTeX o TeX Live con `pdflatex` disponible en `PATH`. El script
compila dos veces cada documento para actualizar referencias y copia los
resultados como `informe-tecnico.pdf`, `manual-instalacion.pdf` y
`manual-usuario.pdf` dentro de sus carpetas respectivas. Los archivos auxiliares
de LaTeX se generan junto a cada `main.tex`.

## Verificación del proyecto

Desde la raíz del proyecto:

```powershell
mvn clean test
```

Los documentos describen el comportamiento del código y no sustituyen una
validación en el entorno donde se instalará la aplicación. La última ejecución
registrada durante esta revisión fue `mvn test`: 516 pruebas, 0 fallos, 0 errores
y 0 omitidas. Vuelve a ejecutar el comando tras modificar el código.

#!/usr/bin/env bash
# Mini menú interactivo para ejecutar los tests JUnit 5 de Tech-Flow
# Requiere: mvn en el PATH y haberse bajado las dependencias al menos una vez

set -u

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR" || exit 1

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Buscar clases de test (terminan en Test)
mapfile -t TEST_CLASSES < <(find src/test/java -name '*Test.java' -type f \
    | sed 's|src/test/java/||; s|\.java$||; s|/|.|g' \
    | sort)

if [ ${#TEST_CLASSES[@]} -eq 0 ]; then
    echo -e "${RED}No se encontraron clases de test en src/test/java${NC}"
    exit 1
fi

while true; do
    echo ""
    echo -e "${BLUE}=== Tech-Flow - Ejecutar Tests (JUnit 5) ===${NC}"
    echo "Elige una opción:"
    echo ""
    local i=1
    for cls in "${TEST_CLASSES[@]}"; do
        echo "  $i) $cls"
        i=$((i + 1))
    done
    echo "  a) Todos los tests de golpe"
    echo "  q) Salir"
    echo ""
    read -rp "Opción: " opc

    case "$opc" in
        q|Q)
            echo -e "${YELLOW}Saliendo...${NC}"
            break
            ;;
        a|A)
            echo -e "${BLUE}▶ Ejecutando TODOS los tests...${NC}"
            mvn test 2>&1 | tee /tmp/opencode/test-output.log
            echo ""
            echo -e "${BLUE}=== Resumen de fallos ===${NC}"
            grep -E 'Tests run:|FAILURE|ERROR|<<< FAILURE!' /tmp/opencode/test-output.log \
                || echo -e "${GREEN}Sin fallos detectados en el resumen.${NC}"
            ;;
        *)
            if [[ "$opc" =~ ^[0-9]+$ ]] && [ "$opc" -ge 1 ] && [ "$opc" -le ${#TEST_CLASSES[@]} ]; then
                cls="${TEST_CLASSES[$((opc - 1))]}"
                echo -e "${BLUE}▶ Ejecutando tests de: $cls${NC}"
                mvn test -Dtest="$cls" 2>&1 | tee /tmp/opencode/test-output.log
                echo ""
                echo -e "${BLUE}=== Resumen de fallos ===${NC}"
                grep -E 'Tests run:|FAILURE|ERROR|<<< FAILURE!' /tmp/opencode/test-output.log \
                    || echo -e "${GREEN}Sin fallos detectados en el resumen.${NC}"
            else
                echo -e "${RED}Opción no válida. Intenta de nuevo.${NC}"
            fi
            ;;
    esac
done
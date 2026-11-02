#!/bin/bash
# Script para lanzar la GUI de Tech-Flow (JavaFX)
set -e

cd /home/jorge/Documentos/Tech-Flow

echo "=== Compilando proyecto ==="
mvn -q compile

echo "=== Lanzando GUI JavaFX ==="
mvn javafx:run

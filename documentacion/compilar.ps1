$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$latex = Get-Command pdflatex -ErrorAction SilentlyContinue
if (-not $latex) { throw "No se encontró pdflatex. Instala MiKTeX o TeX Live." }
foreach ($folder in @("informe-tecnico", "manual-instalacion", "manual-usuario")) {
    Push-Location (Join-Path $root $folder)
    try {
        & $latex.Source -interaction=nonstopmode -halt-on-error main.tex
        & $latex.Source -interaction=nonstopmode -halt-on-error main.tex
        if ($LASTEXITCODE -ne 0) { throw "Falló $folder" }
        Copy-Item "main.pdf" ($folder + ".pdf") -Force
    } finally { Pop-Location }
}
Write-Output "PDF generados correctamente."

# Script para limpiar CSS obsoleto de componentes migrados a Bootstrap

$componentesConvertidos = @(
    "admin-layout",
    "admin-dashboard",
    "category-admin",
    "category-create",
    "category-edit",
    "category-edit-game",
    "company-admin-dashboard",
    "company-create",
    "company-admin-create",
    "global-commission",
    "company-commissions",
    "banner-admin",
    "company-admin-layout",
    "company-profile",
    "company-games-dashboard",
    "company-comments-dashboard",
    "videogame-create",
    "videogame-edit",
    "gamer-layout",
    "gamer-navbar",
    "gamer-dashboard",
    "gamer-library",
    "login",
    "register",
    "footer",
    "banner-carousel",
    "top-games-list",
    "game-search-results",
    "gamer-search-results",
    "company-search-results"
)

$basePath = "src\app\components"
$comentario = "/* Estilos manejados por Bootstrap */"

foreach ($componente in $componentesConvertidos) {
    $cssPath = Join-Path $basePath "$componente\$componente.component.css"
    
    if (Test-Path $cssPath) {
        Set-Content -Path $cssPath -Value $comentario -Encoding UTF8
        Write-Host "✓ Limpiado: $componente.component.css" -ForegroundColor Green
    } else {
        Write-Host "✗ No encontrado: $cssPath" -ForegroundColor Yellow
    }
}

Write-Host "`nProceso completado." -ForegroundColor Cyan

$fxmlDir = "c:\Users\ayoub\IdeaProjects\talentos\src\main\resources\fxml"
$ctrlDir = "c:\Users\ayoub\IdeaProjects\talentos\src\main\java\talentospidev\controllers"

# ===== PART 1: Add Events button to all FXMLs =====
Write-Host "=== PART 1: FXML FILES ==="
$fxmlFiles = Get-ChildItem $fxmlDir -Recurse -Filter "*.fxml"
foreach ($f in $fxmlFiles) {
    $content = [System.IO.File]::ReadAllText($f.FullName)
    if ($content -match "handleEvents") { continue }
    if ($content -notmatch "handleCourses") { continue }
    
    $content = $content -replace '(onAction="#handleCourses"[^/]*/>\s*\r?\n)', "`$1        <Button text=""Events"" styleClass=""sidebar-button"" onAction=""#handleEvents""/>`r`n"
    [System.IO.File]::WriteAllText($f.FullName, $content)
    Write-Host "FXML: $($f.Name)"
}

# ===== PART 2: Add handleEvents to all controllers =====
Write-Host "`n=== PART 2: CONTROLLERS ==="
$javaFiles = Get-ChildItem $ctrlDir -Recurse -Filter "*.java"
foreach ($f in $javaFiles) {
    if ($f.DirectoryName -match "events") { continue }
    $content = [System.IO.File]::ReadAllText($f.FullName)
    if ($content -match "handleEvents") { continue }
    if ($content -notmatch "handleCourses") { continue }
    
    $method = @"

    @javafx.fxml.FXML
    private void handleEvents() {
        talentospidev.models.User u = talentospidev.services.AuthService.getCurrentUser();
        boolean isRecruiter = u != null && (u.getRole() == talentospidev.models.User.Role.HR || u.getRole() == talentospidev.models.User.Role.ADMIN);
        talentospidev.utils.SceneUtil.switchScene(isRecruiter ? "Events/EventsFeed.fxml" : "Events/EventsBrowse.fxml");
    }

"@
    # Insert before the last closing brace
    $content = $content -replace '(void handleCourses\(\))', "$method    `$1"
    [System.IO.File]::WriteAllText($f.FullName, $content)
    Write-Host "CTRL: $($f.Name)"
}

Write-Host "`nAll done!"

$files = Get-ChildItem -Path src -Filter *.java -Recurse
foreach ($f in $files) {
    if ($f -is [System.IO.FileInfo]) {
        try {
            $content = [System.IO.File]::ReadAllText($f.FullName)
            [System.IO.File]::WriteAllText($f.FullName, $content, (New-Object System.Text.UTF8Encoding $False))
        } catch {}
    }
}

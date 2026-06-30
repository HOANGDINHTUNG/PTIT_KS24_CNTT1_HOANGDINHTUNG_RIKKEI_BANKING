Rename-Item -Path "src\main\java\com\re\rikkei_bank_manager\common\response\ApiResponse.java" -NewName "ApiResult.java" -ErrorAction SilentlyContinue

Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    $original = $content

    if ($content -match "ApiResponse" -or $content -match "@io.swagger") {
        # Avoid replacing swagger's ApiResponse temporarily
        $content = $content.Replace("io.swagger.v3.oas.annotations.responses.ApiResponse", "SWAGGER_API_RESPONSE_TMP")
        $content = $content.Replace("@ApiResponse(", "@SWAGGER_API_RESPONSE_TMP(")
        $content = $content.Replace("@ApiResponses(", "@SWAGGER_API_RESPONSE_TMP(")
        
        # Replace the project's ApiResponse 
        $content = $content.Replace("ApiResponse", "ApiResult")
        
        # Restore swagger's ApiResponse
        $content = $content.Replace("SWAGGER_API_RESPONSE_TMP", "ApiResponse")

        # Refactor fully qualified swagger annotations
        $content = $content.Replace("@io.swagger.v3.oas.annotations.tags.Tag", "@Tag")
        $content = $content.Replace("@io.swagger.v3.oas.annotations.Operation", "@Operation")
        $content = $content.Replace("@io.swagger.v3.oas.annotations.responses.ApiResponse", "@ApiResponse")
        
        # Inject standard imports if they don't exist
        if ($content -match "@Tag" -and -not ($content -match "io\.swagger\.v3\.oas\.annotations\.tags\.Tag")) {
            $content = $content -replace "(package [^;]+;)", "`$1`r`nimport io.swagger.v3.oas.annotations.Operation;`r`nimport io.swagger.v3.oas.annotations.responses.ApiResponse;`r`nimport io.swagger.v3.oas.annotations.tags.Tag;"
        }

        if ($original -ne $content) {
            Set-Content -Path $_.FullName -Value $content -Encoding UTF8
        }
    }
}

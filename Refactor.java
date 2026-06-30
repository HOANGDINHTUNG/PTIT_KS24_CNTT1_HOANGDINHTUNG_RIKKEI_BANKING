import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.nio.charset.StandardCharsets;

public class Refactor {
    static final String BASE = "d:/Documents/JAVA WEB SERVICE/PTIT_KS24_CNTT1_HOANGDINHTUNG/src/main/java/com/re/rikkei_bank_manager";
    
    static Map<String, String> FILE_TO_NEW_PATH = new LinkedHashMap<>();

    public static void main(String[] args) throws Exception {
        addMapping("account/Account.java", "entity.Account");
        addMapping("audit/AuditLog.java", "entity.AuditLog");
        addMapping("kyc/KycProfile.java", "entity.KycProfile");
        addMapping("auth/RefreshToken.java", "entity.RefreshToken");
        addMapping("role/Role.java", "entity.Role");
        addMapping("auth/TokenBlacklist.java", "entity.TokenBlacklist");
        addMapping("transaction/Transaction.java", "entity.Transaction");
        addMapping("user/User.java", "entity.User");

        addMapping("account/AccountRepository.java", "repository.AccountRepository");
        addMapping("audit/AuditLogRepository.java", "repository.AuditLogRepository");
        addMapping("kyc/KycProfileRepository.java", "repository.KycProfileRepository");
        addMapping("auth/RefreshTokenRepository.java", "repository.RefreshTokenRepository");
        addMapping("role/RoleRepository.java", "repository.RoleRepository");
        addMapping("auth/TokenBlacklistRepository.java", "repository.TokenBlacklistRepository");
        addMapping("transaction/TransactionRepository.java", "repository.TransactionRepository");
        addMapping("user/UserRepository.java", "repository.UserRepository");
        
        addMapping("security/SecurityConfig.java", "configuration.SecurityConfiguration");
        addMapping("config/DataInitializer.java", "configuration.DataInitializer");
        
        addMapping("auth/AuthController.java", "controller.AuthController");
        addMapping("common/TestController.java", "controller.TestController");
        
        addMapping("auth/dto/LoginRequest.java", "dto.request.LoginRequest");
        addMapping("auth/dto/TokenRefreshRequest.java", "dto.request.TokenRefreshRequest");
        
        addMapping("auth/dto/AuthResponse.java", "dto.response.AuthResponse");
        addMapping("user/dto/UserResponseDto.java", "dto.response.UserResponseDto");
        addMapping("common/response/ApiResponse.java", "dto.response.ApiResponse");
        addMapping("common/response/ErrorResponse.java", "dto.response.ErrorResponse");
        
        addMapping("common/enums/Currency.java", "entity.enums.Currency");
        addMapping("common/enums/KycStatus.java", "entity.enums.KycStatus");
        addMapping("common/enums/RoleName.java", "entity.enums.RoleName");
        addMapping("common/enums/TransactionStatus.java", "entity.enums.TransactionStatus");
        addMapping("common/enums/TransactionType.java", "entity.enums.TransactionType");
        
        addMapping("common/exception/BadRequestException.java", "exception.handler.BadRequestException");
        addMapping("common/exception/EmailAlreadyExistsException.java", "exception.handler.EmailAlreadyExistsException");
        addMapping("common/exception/ResourceNotFoundException.java", "exception.handler.ResourceNotFoundException");
        addMapping("common/exception/UnauthorizedException.java", "exception.handler.UnauthorizedException");
        addMapping("common/exception/UsernameAlreadyExistsException.java", "exception.handler.UsernameAlreadyExistsException");
        addMapping("common/exception/ValidationException.java", "exception.handler.ValidationException");
        addMapping("common/exception/MissingRequiredFieldException.java", "exception.handler.MissingRequiredFieldException");
        addMapping("common/exception/GlobalExceptionHandler.java", "exception.handler.GlobalExceptionHandler");
        
        addMapping("auth/JwtAuthenticationFilter.java", "security.AuthenticationFilter");
        addMapping("auth/JwtService.java", "security.TokenProvider");
        addMapping("auth/CustomUserDetails.java", "security.UserDetailsImpl");
        addMapping("auth/CustomUserDetailsService.java", "security.UserDetailsServiceImpl");
        
        addMapping("auth/AuthService.java", "service.impl.AuthService");
        
        Path basePath = Paths.get(BASE);
        List<Path> allJavaFiles = Files.walk(basePath)
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".java"))
            .collect(Collectors.toList());
            
        Map<String, String> oldFqcnToNewFqcn = new LinkedHashMap<>();
        
        for (Map.Entry<String, String> entry : FILE_TO_NEW_PATH.entrySet()) {
            String oldFqcn = getFqcn(entry.getKey());
            String newFqcn = getFqcn(entry.getValue());
            oldFqcnToNewFqcn.put(oldFqcn, newFqcn);
        }

        for (Path p : allJavaFiles) {
            String relative = basePath.relativize(p).toString().replace("\\\\", "/");
            String content = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
            
            for (Map.Entry<String, String> entry : oldFqcnToNewFqcn.entrySet()) {
                String oldFqcn = entry.getKey();
                String newFqcn = entry.getValue();
                content = content.replace("import " + oldFqcn + ";", "import " + newFqcn + ";");
                content = content.replace(oldFqcn, newFqcn);
            }

            content = content.replace("SecurityConfig", "SecurityConfiguration");
            content = content.replace("JwtAuthenticationFilter", "AuthenticationFilter");
            content = content.replace("JwtService", "TokenProvider");
            content = content.replace("CustomUserDetailsService", "UserDetailsServiceImpl");
            content = content.replace("CustomUserDetails", "UserDetailsImpl");

            String newRelative = FILE_TO_NEW_PATH.getOrDefault(relative, relative);
            String newPackage = "com.re.rikkei_bank_manager";
            if (newRelative.contains("/")) {
                newPackage += "." + getPackageFromRelativePath(newRelative);
            }
            
            content = content.replaceAll("(?s)package [a-zA-Z0-9_.]+;", "package " + newPackage + ";");

            Path dest = basePath.resolve(newRelative);
            Files.createDirectories(dest.getParent());
            Files.write(dest, content.getBytes(StandardCharsets.UTF_8));
            
            if (!newRelative.equals(relative)) {
                Files.delete(p);
            }
        }
        
        Files.walk(basePath).sorted(Comparator.reverseOrder())
             .filter(Files::isDirectory)
             .forEach(dir -> {
                 try {
                     if (Files.list(dir).count() == 0) Files.delete(dir);
                 } catch (Exception e) {}
             });
             
        System.out.println("Refactoring Done");
    }
    
    static void addMapping(String oldPath, String newFqcnPart) {
        String newRelativePath = newFqcnPart.replace(".", "/") + ".java";
        FILE_TO_NEW_PATH.put(oldPath, newRelativePath);
    }
    
    static String getFqcn(String relativePath) {
        String p = relativePath.replace(".java", "").replace("/", ".");
        return "com.re.rikkei_bank_manager." + p;
    }
    
    static String getPackageFromRelativePath(String relativePath) {
        int idx = relativePath.lastIndexOf("/");
        return idx == -1 ? "" : relativePath.substring(0, idx).replace("/", ".");
    }
}

Static analysis report
-------------------------
This report was for quickstart and bestpractices

src\main\java\application\ProjectApplication.java:10:   UseUtilityClass:        
This utility class has a non-private constructor
src\main\java\application\controller\ListingController.java:11: UnnecessaryImport:      
Unused import 'org.springframework.web.bind.annotation.*'
src\main\java\application\controller\RoommateController.java:12:        UnnecessaryImport:      
Unused import 'org.springframework.web.bind.annotation.*'
src\main\java\application\controller\UserController.java:8:     UnnecessaryImport:      
Unused import 'org.springframework.web.bind.annotation.*'
src\main\java\application\controller\UserController.java:73:    UnusedLocalVariable:    
Avoid unused local variables such as 'verifiedUser'.
src\main\java\application\model\Listing.java:3: UnnecessaryImport:      
Unused import 'jakarta.persistence.*'
src\main\java\application\model\Listing.java:5: UnnecessaryImport:      
Unused import 'lombok.*'
src\main\java\application\model\RoommateMatch.java:3:   UnnecessaryImport:      
Unused import 'jakarta.persistence.*'
src\main\java\application\model\RoommateMatch.java:5:   UnnecessaryImport:      
Unused import 'lombok.*'
src\main\java\application\model\RoommatePreference.java:3:      UnnecessaryImport:      
Unused import 'jakarta.persistence.*'
src\main\java\application\model\RoommatePreference.java:4:      UnnecessaryImport:      
Unused import 'lombok.*'
src\main\java\application\model\User.java:3:    UnnecessaryImport:     
Unused import 'jakarta.persistence.*'
src\main\java\application\model\User.java:4:    UnnecessaryImport:      
Unused import 'lombok.*'
src\main\java\application\security\JwtFilter.java:30:   GuardLogStatement:      
Logger calls should be surrounded by log level guards.
src\main\java\application\service\RoommateService.java:154:     UselessParentheses:    
Useless parentheses.
src\main\java\application\service\RoommateService.java:155:     UselessParentheses:    
Useless parentheses.
src\main\java\application\service\RoommateService.java:202:     UselessParentheses:     
Useless parentheses.

src\main\java\application\controller\UserController.java:73:    
UnusedLocalVariable:    Avoid unused local variables such as 'verifiedUser'.
src\main\java\application\repository\ListingRepository.java:10: ImplicitFunctionalInterface:   
Annotate this interface with @FunctionalInterface or with 
@SuppressWarnings("PMD.ImplicitFunctionalInterface") to clarify your intent.
src\main\java\application\security\JwtFilter.java:30:   GuardLogStatement:      
Logger calls should be surrounded by log level guards.
src\main\java\application\service\DatabaseSeeder.java:39:       
SystemPrintln:  Usage of System.out/err
src\main\java\application\service\DatabaseSeeder.java:41:       
SystemPrintln:  Usage of System.out/err


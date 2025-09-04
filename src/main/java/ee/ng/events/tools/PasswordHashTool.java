package ee.ng.events.tools;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


public class PasswordHashTool {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: PasswordHashTool <plainPassword>");
            return;
        }
        System.out.println(new BCryptPasswordEncoder().encode(args[0]));
    }
}
package com.github.testsymphony.cli.helper;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InteractiveConfirmation {
    
    public boolean confirmStop() {
        while (true) {
            System.out.print("Are you sure you want to stop recording? (y/yes/n/no): ");
            String confirm = GlobalScanner.SCANNER.nextLine().trim().toLowerCase();
            if ("y".equals(confirm) || "yes".equals(confirm)) {
                return true;
            } else if ("n".equals(confirm) || "no".equals(confirm)) {
                return false;
            } else {
                System.out.println("Please type 'yes' (or 'y') or 'no' (or 'n').");
            }
        }
    }
}

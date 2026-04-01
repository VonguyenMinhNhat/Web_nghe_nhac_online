import java.io.File;

public class SetupStructure {
    public static void main(String[] args) {
        String basePath = "src/main/resources/static";
        
        String[] directories = {
            basePath + "/pages",
            basePath + "/pages/admin",
            basePath + "/css",
            basePath + "/js"
        };
        
        for (String dir : directories) {
            File directory = new File(dir);
            if (!directory.exists()) {
                if (directory.mkdirs()) {
                    System.out.println("✓ Created: " + dir);
                } else {
                    System.out.println("✗ Failed to create: " + dir);
                }
            } else {
                System.out.println("✓ Exists: " + dir);
            }
        }
        
        System.out.println("\n✓ Directory structure setup complete!");
    }
}

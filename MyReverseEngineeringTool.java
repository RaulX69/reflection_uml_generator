import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MyReverseEngineeringTool {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Eroare: Lipseste fisierul .jar. Utilizare: java MyReverseEngineeringTool <fisier.jar> [-fqn] [-nomethods] [-noattributes]");
            return;
        }

        String jarPath = args[0];
        ToolConfig config = parseArgs(args);
        List<Class<?>> loadedClasses = new ArrayList<>();

        try {
            File file = new File(jarPath);
            URL jarUrl = file.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, MyReverseEngineeringTool.class.getClassLoader());

            JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class") && !entry.getName().equals("module-info.class")) {
                    String className = entry.getName().replace('/', '.').replace(".class", "");
                    try {
                        Class<?> cl = classLoader.loadClass(className);
                        loadedClasses.add(cl);
                    } catch (NoClassDefFoundError | ClassNotFoundException e) {}
                }
            }
            jarFile.close();
            classLoader.close();

            DiagramGenerator generator = new PlantUMLGenerator();
            String diagramOutput = generator.generate(loadedClasses, config);
            
            System.out.println(diagramOutput);

        } catch (Exception e) {
            System.out.println("Eroare la procesarea fisierului .jar: ");
        }
    }

    private static ToolConfig parseArgs(String[] args) {
        ToolConfig config = new ToolConfig();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i].toLowerCase();
            if (arg.equals("-fqn")) config.useFQN = true;
            if (arg.equals("-nomethods")) config.showMethods = false;
            if (arg.equals("-noattributes")) config.showAttributes = false;
        }
        return config;
    }
}
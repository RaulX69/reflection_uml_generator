import java.util.ArrayList;
import java.util.List;

public class ToolConfig {
    public boolean useFQN = false;
    public boolean showMethods = true;
    public boolean showAttributes = true;
    public List<String> ignoredPrefixes = new ArrayList<>();

    public ToolConfig() {
        ignoredPrefixes.add("java.");
    }

    public boolean isIgnored(Class<?> cl) {
        if (cl == null) return true;
        if (cl.isArray() || cl.isPrimitive()) return true;
        
        String className = cl.getName();
        for (String prefix : ignoredPrefixes) {
            if (className.startsWith(prefix)) return true;
        }
        return false;
    }

    public String formatName(Class<?> cl) {
        return useFQN ? cl.getName() : cl.getSimpleName();
    }
}
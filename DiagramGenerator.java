import java.util.List;

public interface DiagramGenerator {
    String generate(List<Class<?>> classes, ToolConfig config);
}
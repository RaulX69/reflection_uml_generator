import java.lang.reflect.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlantUMLGenerator implements DiagramGenerator {

    @Override
    public String generate(List<Class<?>> classes, ToolConfig config) {
        StringBuilder sb = new StringBuilder();
        StringBuilder relationsSb = new StringBuilder();
        
        sb.append("@startuml\n");
        sb.append("skinparam classAttributeIconSize 0\n\n");

        Set<Class<?>> parsedClasses = new HashSet<>(classes);
        Set<String> addedRelations = new HashSet<>();

        for (Class<?> cl : classes) {
            if (config.isIgnored(cl)) continue;

            String type = cl.isInterface() ? "interface" : cl.isEnum() ? "enum" : "class";
            String className = config.formatName(cl);

            sb.append(type).append(" \"").append(className).append("\" {\n");

            if (config.showAttributes) {
                for (Field field : cl.getDeclaredFields()) {
                    if (field.isSynthetic()) continue;
                    
                    if (cl.isEnum() && field.getType() == cl) {
                        sb.append("  ").append(field.getName()).append("\n");
                        continue;
                    }

                    String mod = getModifierSymbol(field.getModifiers());
                    String fieldType = getTypeName(field.getGenericType());
                    sb.append("  ").append(mod).append(" ").append(fieldType).append(" ").append(field.getName()).append("\n");

                    Class<?> targetType = field.getType();
                    if (parsedClasses.contains(targetType) && !targetType.equals(cl)) {
                        String rel = "\"" + className + "\" --> \"" + config.formatName(targetType) + "\" : " + field.getName() + "\n";
                        if (addedRelations.add(rel)) relationsSb.append(rel);
                    }
                }
            }

            if (config.showMethods) {
                for (Method method : cl.getDeclaredMethods()) {
                    if (method.isSynthetic()) continue;
                    
                    if (cl.isEnum() && (method.getName().equals("values") || method.getName().equals("valueOf"))) {
                        continue;
                    }

                    String mod = getModifierSymbol(method.getModifiers());
                    String returnType = getTypeName(method.getGenericReturnType());
                    
                    sb.append("  ").append(mod).append(" ").append(returnType).append(" ").append(method.getName()).append("(");
                    
                    Parameter[] params = method.getParameters();
                    for (int i = 0; i < params.length; i++) {
                        sb.append(getTypeName(params[i].getParameterizedType())).append(" ").append(params[i].getName());
                        if (i < params.length - 1) sb.append(", ");
                        
                        Class<?> paramType = params[i].getType();
                        if (parsedClasses.contains(paramType) && !paramType.equals(cl)) {
                             String rel = "\"" + className + "\" ..> \"" + config.formatName(paramType) + "\"\n";
                             if (addedRelations.add(rel)) relationsSb.append(rel);
                        }
                    }
                    sb.append(")\n");
                }
            }
            sb.append("}\n\n");

            Class<?> superclass = cl.getSuperclass();
            if (superclass != null && superclass != Object.class && superclass != Enum.class && !config.isIgnored(superclass)) {
                relationsSb.append("\"").append(config.formatName(superclass)).append("\" <|-- \"").append(className).append("\"\n");
            }

            for (Class<?> iface : cl.getInterfaces()) {
                if (!config.isIgnored(iface)) {
                    relationsSb.append("\"").append(config.formatName(iface)).append("\" <|.. \"").append(className).append("\"\n");
                }
            }
        }

        sb.append(relationsSb.toString());
        
        sb.append("@enduml\n");
        return sb.toString();
    }

    private String getModifierSymbol(int modifiers) {
        if (Modifier.isPublic(modifiers)) return "+";
        if (Modifier.isPrivate(modifiers)) return "-";
        if (Modifier.isProtected(modifiers)) return "#";
        return "~";
    }

    private String getTypeName(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Class<?> rawClass = (Class<?>) pType.getRawType();
            StringBuilder sb = new StringBuilder(rawClass.getSimpleName()).append("<");
            
            Type[] typeArgs = pType.getActualTypeArguments();
            for (int i = 0; i < typeArgs.length; i++) {
                sb.append(getTypeName(typeArgs[i]));
                if (i < typeArgs.length - 1) sb.append(", ");
            }
            sb.append(">");
            return sb.toString();
        } else if (type instanceof Class<?>) {
            return ((Class<?>) type).getSimpleName();
        }
        return type.getTypeName();
    }
}
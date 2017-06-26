package services.stateservices.facade;

import java.util.Map;
import java.util.HashMap;
import org.apache.commons.lang.StringUtils;

public class Struct {
    private Map<String, String> fields = new HashMap<>();
    public void add(String key, String field) {
        fields.put(key, field);
    }
    
    public String get(String key) {
        return fields.get(key);
    }
    
    @Override
    public String toString() {
        return String.join(", ", fields.values());
    }
}

package di.uniba.it.lodrecsys.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class used in order to pass multiple parameters through
 * other classes
 */
public class RequestStruct {
    public List<Object> params;

    public RequestStruct(Object... args) {
        params = new ArrayList<>();
        Collections.addAll(params, args);
    }
}

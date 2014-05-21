package di.uniba.it.lodrecsys.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by asuglia on 5/21/14.
 */
public class RequestStruct {
    public List<Object> params;

    public RequestStruct(Object... args) {
        params = new ArrayList<>();
        Collections.addAll(params, args);
    }
}

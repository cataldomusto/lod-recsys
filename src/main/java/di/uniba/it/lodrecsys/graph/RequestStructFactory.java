package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.entity.RequestStruct;
import jdk.nashorn.internal.ir.RuntimeNode;

import java.io.IOException;

/**
 * Created by asuglia on 6/18/14.
 */
public class RequestStructFactory {
    public static RequestStruct create(String specificModel, Object... params) throws IOException {
        RequestStruct request = null;

        if (specificModel.equals("UserItemGraph")) {
            request = new RequestStruct((int) params[0]);

        } else if (specificModel.equals("UserItemPriorGraph")) {
            request = new RequestStruct((int) params[0], (double) params[1]);

        }


        return request;

    }
}
